package de.hysky.skyblocker.skyblock.waypoint;

import com.google.common.primitives.Floats;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.CodecUtils;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientBlockPosArgumentType;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientPosArgument;
import de.hysky.skyblocker.utils.command.argumenttypes.color.ColorArgumentType;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class OrderedWaypoints {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Codec<Map<String, OrderedWaypointGroup>> SERIALIZATION_CODEC = Codec.unboundedMap(Codec.STRING, OrderedWaypointGroup.CODEC).xmap(Object2ObjectOpenHashMap::new, Object2ObjectOpenHashMap::new);
	private static final String PREFIX = "[Skyblocker::OrderedWaypoints::v1]";
	private static final Path PATH = SkyblockerMod.CONFIG_DIR.resolve("ordered_waypoints.json");
	private static final Map<String, OrderedWaypointGroup> WAYPOINTS = new Object2ObjectOpenHashMap<>();
	private static final Semaphore SEMAPHORE = new Semaphore(1);
	private static final Object2IntOpenHashMap<String> INDEX_STORE = new Object2IntOpenHashMap<>();
	private static final int RADIUS = 2;
	private static final float[] LIGHT_GRAY = { 192 / 255f, 192 / 255f, 192 / 255f };

	private static CompletableFuture<Void> loaded;
	private static boolean showAll;

	@Init
	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(_client -> load());
		ClientLifecycleEvents.CLIENT_STOPPING.register(_client -> save());
		ClientCommandRegistrationCallback.EVENT.register(OrderedWaypoints::registerCommands);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(OrderedWaypoints::render);
	}

	private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("waypoints")
						.then(literal("ordered")
								.then(literal("add")
										.then(argument("groupName", word())
												.suggests((source, builder) -> CommandSource.suggestMatching(WAYPOINTS.keySet(), builder))
												.then(argument("pos", ClientBlockPosArgumentType.blockPos())
														.executes(context -> addWaypoint(context.getSource(), getString(context, "groupName"), context.getArgument("pos", ClientPosArgument.class), Integer.MIN_VALUE, Integer.MIN_VALUE))
														.then(argument("hex", ColorArgumentType.hex())
																.executes(context -> addWaypoint(context.getSource(), getString(context, "groupName"), context.getArgument("pos", ClientPosArgument.class), Integer.MIN_VALUE, ColorArgumentType.getIntFromHex(context, "hex")))))))
								.then(literal("addAt")
										.then(argument("groupName", word())
												.suggests((source, builder) -> CommandSource.suggestMatching(WAYPOINTS.keySet(), builder))
												.then(argument("index", IntegerArgumentType.integer(0))
														.then(argument("pos", ClientBlockPosArgumentType.blockPos())
																.executes(context -> addWaypoint(context.getSource(), getString(context, "groupName"), context.getArgument("pos", ClientPosArgument.class), IntegerArgumentType.getInteger(context, "index"), Integer.MIN_VALUE))
																.then(argument("hex", ColorArgumentType.hex())
																		.executes(context -> addWaypoint(context.getSource(), getString(context, "groupName"), context.getArgument("pos", ClientPosArgument.class), IntegerArgumentType.getInteger(context, "index"), ColorArgumentType.getIntFromHex(context, "hex"))))))))
								.then(literal("remove")
										.then(argument("groupName", word())
												.suggests((source, builder) -> CommandSource.suggestMatching(WAYPOINTS.keySet(), builder))
												.executes(context -> removeWaypointGroup(context.getSource(), getString(context, "groupName")))
												.then(argument("pos", ClientBlockPosArgumentType.blockPos())
														.executes(context -> removeWaypoint(context.getSource(), getString(context, "groupName"), context.getArgument("pos", ClientPosArgument.class), Integer.MIN_VALUE)))))
								.then(literal("removeAt")
										.then(argument("groupName", word())
												.suggests((source, builder) -> CommandSource.suggestMatching(WAYPOINTS.keySet(), builder))
												.then(argument("index", IntegerArgumentType.integer(0))
														.executes(context -> removeWaypoint(context.getSource(), getString(context, "groupName"), null, IntegerArgumentType.getInteger(context, "index"))))))
								.then(literal("toggle")
										.then(argument("groupName", word())
												.suggests((source, builder) -> CommandSource.suggestMatching(WAYPOINTS.keySet(), builder))
												.executes(context -> toggleGroup(context.getSource(), getString(context, "groupName")))))
								.then(literal("showAll")
										.executes(context -> showAll(context.getSource())))
								.then(literal("import")
										.then(literal("coleWeight")
												.then(argument("groupName", word())
														.executes(context -> fromColeWeightFormat(context.getSource(), getString(context, "groupName")))))
										.then(literal("skyblocker")
												.executes(context -> fromSkyblockerFormat(context.getSource()))))
								.then(literal("export")
										.executes(context -> export(context.getSource()))))));
	}

	private static int addWaypoint(FabricClientCommandSource source, String groupName, ClientPosArgument posArgument, int index, int color) {
		BlockPos pos = posArgument.toAbsoluteBlockPos(source);

		SEMAPHORE.acquireUninterruptibly();

		float[] colorComponents = color != Integer.MIN_VALUE ? ColorUtils.getFloatComponents(color) : new float[0];

		OrderedWaypointGroup group = WAYPOINTS.computeIfAbsent(groupName, name -> new OrderedWaypointGroup(name, true, new ObjectArrayList<>()));
		OrderedWaypoint waypoint = new OrderedWaypoint(pos, colorComponents);

		if (index != Integer.MIN_VALUE) {
			int indexToAddAt = Math.clamp(index, 0, group.waypoints().size());

			group.waypoints().add(indexToAddAt, waypoint);
			INDEX_STORE.removeInt(group.name());
			source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.addAt.success", group.name(), indexToAddAt)));
		} else {
			group.waypoints().add(waypoint);
			INDEX_STORE.removeInt(group.name());
			source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.add.success", group.name(), pos.toShortString())));
		}

		SEMAPHORE.release();

		return Command.SINGLE_SUCCESS;
	}

	private static int removeWaypointGroup(FabricClientCommandSource source, String groupName) {
		if (WAYPOINTS.containsKey(groupName)) {
			SEMAPHORE.acquireUninterruptibly();
			WAYPOINTS.remove(groupName);
			INDEX_STORE.removeInt(groupName);
			SEMAPHORE.release();
			source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.removeGroup.success", groupName)));
		} else {
			source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.groupNonExistent", groupName)));
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int removeWaypoint(FabricClientCommandSource source, String groupName, ClientPosArgument posArgument, int index) {
		if (WAYPOINTS.containsKey(groupName)) {
			SEMAPHORE.acquireUninterruptibly();
			OrderedWaypointGroup group = WAYPOINTS.get(groupName);

			if (posArgument != null) {
				BlockPos pos = posArgument.toAbsoluteBlockPos(source);

				group.waypoints().removeIf(waypoint -> waypoint.getPos().equals(pos));
				INDEX_STORE.removeInt(group.name());
				source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.remove.success", pos.toShortString(), group.name())));
			}

			if (index != Integer.MIN_VALUE) {
				int indexToRemove = Math.clamp(index, 0, group.waypoints().size() - 1);

				group.waypoints().remove(indexToRemove);
				INDEX_STORE.removeInt(group.name());
				source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.removeAt.success", indexToRemove, group.name())));
			}

			SEMAPHORE.release();
		} else {
			source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.groupNonExistent", groupName)));
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int toggleGroup(FabricClientCommandSource source, String groupName) {
		if (WAYPOINTS.containsKey(groupName)) {
			SEMAPHORE.acquireUninterruptibly();
			WAYPOINTS.put(groupName, WAYPOINTS.get(groupName).toggle());
			SEMAPHORE.release();
			source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.toggle.success", groupName)));
		} else {
			source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.groupNonExistent", groupName)));
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int showAll(FabricClientCommandSource source) {
		source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.showAll")));
		showAll = !showAll;

		return Command.SINGLE_SUCCESS;
	}

	private static void render(WorldRenderContext wrc) {
		if ((Utils.isInCrystalHollows() || Utils.isInDwarvenMines()) && loaded.isDone() && SEMAPHORE.tryAcquire()) {
			for (OrderedWaypointGroup group : WAYPOINTS.values()) {
				if (group.enabled()) {
					List<OrderedWaypoint> waypoints = group.waypoints();
					if (waypoints.isEmpty()) continue;

					if (!showAll) {
						ClientPlayerEntity player = MinecraftClient.getInstance().player;
						int centreIndex = INDEX_STORE.computeIfAbsent(group.name(), name -> 0);

						for (int i = 0; i < waypoints.size(); i++) {
							OrderedWaypoint waypoint = waypoints.get(i);

							if (waypoint.getPos().isWithinDistance(player.getPos(), RADIUS)) {
								centreIndex = i;
								INDEX_STORE.put(group.name(), i);

								break;
							}
						}

						int previousIndex = (centreIndex - 1 + waypoints.size()) % waypoints.size();
						int currentIndex = (centreIndex + waypoints.size()) % waypoints.size();
						int nextIndex = (centreIndex + 1) % waypoints.size();

						OrderedWaypoint previous = waypoints.get(previousIndex);
						OrderedWaypoint current = waypoints.get(currentIndex);
						OrderedWaypoint next = waypoints.get(nextIndex);

						previous.render(wrc, RelativeIndex.PREVIOUS, previousIndex);
						current.render(wrc, RelativeIndex.CURRENT, currentIndex);
						next.render(wrc, RelativeIndex.NEXT, nextIndex);

						RenderHelper.renderLineFromCursor(wrc, Vec3d.ofCenter(next.getPos().up()), LIGHT_GRAY, 1f, 5f);
					} else {
						for (int i = 0; i < waypoints.size(); i++) {
							//Render them as white by default
							waypoints.get(i).render(wrc, RelativeIndex.CURRENT, i);
						}
					}
				}
			}

			SEMAPHORE.release();
		}
	}

	private static void load() {
		loaded = CompletableFuture.runAsync(() -> {
			try (BufferedReader reader = Files.newBufferedReader(PATH)) {
				WAYPOINTS.putAll(SERIALIZATION_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow());
			} catch (NoSuchFileException ignored) {
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Ordered Waypoints] Failed to load the waypoints! :(", e);
			}
		});
	}

	private static void save() {
		try (BufferedWriter writer = Files.newBufferedWriter(PATH)) {
			SkyblockerMod.GSON.toJson(SERIALIZATION_CODEC.encodeStart(JsonOps.INSTANCE, WAYPOINTS).getOrThrow(), writer);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Ordered Waypoints] Failed to save the waypoints! :(", e);
		}
	}

	private static int export(FabricClientCommandSource source) {
		try {
			String json = new Gson().toJson(SERIALIZATION_CODEC.encodeStart(JsonOps.INSTANCE, WAYPOINTS).getOrThrow());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			GZIPOutputStream gzip = new GZIPOutputStream(out);

			gzip.write(json.getBytes());
			gzip.close();

			String encoded = new String(Base64.getEncoder().encode(out.toByteArray()));
			String exportCode = PREFIX + encoded;

			MinecraftClient.getInstance().keyboard.setClipboard(exportCode);
			source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.export.success")));
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Ordered Waypoints] Failed to export waypoints!", e);
			source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.export.fail")));
		}

		return Command.SINGLE_SUCCESS;
	}

	//TODO in future handle for when the group names clash?
	private static int fromSkyblockerFormat(FabricClientCommandSource source) {
		try {
			String importCode = MinecraftClient.getInstance().keyboard.getClipboard();

			if (importCode.startsWith(PREFIX)) {
				String encoded = importCode.replace(PREFIX, "");
				byte[] decoded = Base64.getDecoder().decode(encoded);

				String json = new String(new GZIPInputStream(new ByteArrayInputStream(decoded)).readAllBytes());
				Map<String, OrderedWaypointGroup> importedWaypoints = SERIALIZATION_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();

				SEMAPHORE.acquireUninterruptibly();
				WAYPOINTS.putAll(importedWaypoints);
				source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.import.skyblocker.success")));
				SEMAPHORE.release();
			} else {
				source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.import.skyblocker.unknownFormatHeader")));
			}
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Ordered Waypoints] Failed to import waypoints!", e);
			source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.import.skyblocker.fail")));
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int fromColeWeightFormat(FabricClientCommandSource source, String groupName) {
		try {
			if (WAYPOINTS.containsKey(groupName)) {
				source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.import.coleWeight.groupAlreadyExists", groupName)));

				return Command.SINGLE_SUCCESS;
			}

			String json = MinecraftClient.getInstance().keyboard.getClipboard();
			List<ColeWeightWaypoint> coleWeightWaypoints = ColeWeightWaypoint.LIST_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
			ObjectArrayList<OrderedWaypoint> convertedWaypoints = new ObjectArrayList<>();

			for (ColeWeightWaypoint waypoint : coleWeightWaypoints) {
				if (waypoint.x().isPresent() && waypoint.y().isPresent() && waypoint.z().isPresent()) {
					//I think Cole Weight ignores the colors and overrides them so we will comment this out
					//float[] colorComponents = (waypoint.r().isPresent() && waypoint.g().isPresent() && waypoint.b().isPresent()) ? new float[] { waypoint.r().get() / 255f, waypoint.g().get() / 255f, waypoint.b().get() / 255f } : new float[0];

					convertedWaypoints.add(new OrderedWaypoint(new BlockPos(waypoint.x().getAsInt(), waypoint.y().getAsInt(), waypoint.z().getAsInt()), new float[0]));
				}
			}

			SEMAPHORE.acquireUninterruptibly();
			WAYPOINTS.put(groupName, new OrderedWaypointGroup(groupName, true, convertedWaypoints));
			source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.import.coleWeight.success")));
			SEMAPHORE.release();
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Ordered Waypoints] Failed to import waypoints from the Cole Weight format!", e);
			source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.waypoints.ordered.import.coleWeight.fail")));
		}

		return Command.SINGLE_SUCCESS;
	}

	private record OrderedWaypointGroup(String name, boolean enabled, ObjectArrayList<OrderedWaypoint> waypoints) {
		static final Codec<OrderedWaypointGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("name").forGetter(OrderedWaypointGroup::name),
				Codec.BOOL.fieldOf("enabled").forGetter(OrderedWaypointGroup::enabled),
				OrderedWaypoint.LIST_CODEC.fieldOf("waypoints").xmap(ObjectArrayList::new, ObjectArrayList::new).forGetter(OrderedWaypointGroup::waypoints))
				.apply(instance, OrderedWaypointGroup::new));

		OrderedWaypointGroup toggle() {
			return new OrderedWaypointGroup(name, !enabled, waypoints);
		}
	}

	private static class OrderedWaypoint extends Waypoint {
		static final Codec<OrderedWaypoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				BlockPos.CODEC.fieldOf("pos").forGetter(OrderedWaypoint::getPos),
				Codec.floatRange(0, 1).listOf().xmap(Floats::toArray, FloatArrayList::new).optionalFieldOf("colorComponents", new float[0]).forGetter(inst -> inst.colorComponents.length == 3 ? inst.colorComponents : new float[0]))
				.apply(instance, OrderedWaypoint::new));
		static final Codec<List<OrderedWaypoint>> LIST_CODEC = CODEC.listOf();
		static final float[] RED = { 1f, 0f, 0f };
		static final float[] WHITE = { 1f, 1f, 1f };
		static final float[] GREEN = { 0f, 1f, 0f };

		private RelativeIndex relativeIndex;
		private int waypointIndex;

		OrderedWaypoint(BlockPos pos, float[] colorComponents) {
			super(pos, () -> SkyblockerConfigManager.get().uiAndVisuals.waypoints.waypointType, colorComponents);
		}

		private BlockPos getPos() {
			return this.pos;
		}

		@Override
		public float[] getColorComponents() {
			if (this.colorComponents.length != 3) {
				return switch (this.relativeIndex) {
					case PREVIOUS -> RED;
					case CURRENT -> WHITE;
					case NEXT -> GREEN;
				};
			}

			return this.colorComponents;
		}

		private void render(WorldRenderContext context, RelativeIndex relativeIndex, int waypointIndex) {
			this.relativeIndex = relativeIndex;
			this.waypointIndex = waypointIndex;

			render(context);
		}

		@Override
		public void render(WorldRenderContext context) {
			super.render(context);
			RenderHelper.renderText(context, Text.of(String.valueOf(waypointIndex)), Vec3d.ofCenter(pos.up(2)), true);
		}
	}

	private record ColeWeightWaypoint(OptionalInt x, OptionalInt y, OptionalInt z, OptionalInt r, OptionalInt g, OptionalInt b, Optional<Options> options) {
		static final Codec<ColeWeightWaypoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				CodecUtils.optionalInt(Codec.INT.optionalFieldOf("x")).forGetter(ColeWeightWaypoint::x),
				CodecUtils.optionalInt(Codec.INT.optionalFieldOf("y")).forGetter(ColeWeightWaypoint::y),
				CodecUtils.optionalInt(Codec.INT.optionalFieldOf("z")).forGetter(ColeWeightWaypoint::z),
				CodecUtils.optionalInt(Codec.INT.optionalFieldOf("r")).forGetter(ColeWeightWaypoint::r),
				CodecUtils.optionalInt(Codec.INT.optionalFieldOf("g")).forGetter(ColeWeightWaypoint::g),
				CodecUtils.optionalInt(Codec.INT.optionalFieldOf("b")).forGetter(ColeWeightWaypoint::b),
				Options.CODEC.optionalFieldOf("options").forGetter(ColeWeightWaypoint::options))
				.apply(instance, ColeWeightWaypoint::new));
		static final Codec<List<ColeWeightWaypoint>> LIST_CODEC = CODEC.listOf();

		//Even though we don't import the name this is still here incase that eventually changes
		record Options(Optional<String> name) {
			static final Codec<Options> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.STRING.optionalFieldOf("name").forGetter(Options::name))
					.apply(instance, Options::new));
		}
	}

	private enum RelativeIndex {
		PREVIOUS,
		CURRENT,
		NEXT
	}
}
