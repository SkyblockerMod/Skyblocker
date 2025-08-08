package de.hysky.skyblocker.debug;

import com.google.gson.JsonElement;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.mixins.accessors.InGameHudInvoker;
import de.hysky.skyblocker.skyblock.events.EventNotifications;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.networth.NetworthCalculator;
import net.azureaaron.networth.Calculation;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.Slot;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.ErrorReporter;
import net.minecraft.world.biome.Biome;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Debug {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final boolean DEBUG_ENABLED = Boolean.parseBoolean(System.getProperty("skyblocker.debug", "false"));
	//This is necessary to not spam the chat with 20 messages per second
	private static boolean keyDown = false;

	public static boolean debugEnabled() {
		return DEBUG_ENABLED || FabricLoader.getInstance().isDevelopmentEnvironment() || SnapshotDebug.isInSnapshot();
	}

	/**
	 * Used for checking if unit tests are being run.
	 */
	public static boolean isTestEnvironment() {
		return Boolean.getBoolean("IS_TEST_ENV");
	}

	public static boolean webSocketDebug() {
		return SkyblockerConfigManager.get().debug.webSocketDebug;
	}

	@Init
	public static void init() {
		if (!debugEnabled()) return;
		SnapshotDebug.init();
		KeyBinding dumpNearbyEntitiesKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.skyblocker.debug.dumpNearbyEntities", GLFW.GLFW_KEY_I, "key.categories.skyblocker"));
		KeyBinding dumpHoveredItemKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.skyblocker.debug.dumpHoveredItem", GLFW.GLFW_KEY_U, "key.categories.skyblocker"));
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				literal(SkyblockerMod.NAMESPACE).then(literal("debug")
						.then(dumpPlayersCommand())
						.then(ItemUtils.dumpHeldItemCommand())
						.then(ItemUtils.dumpHeldItemNetworthCalculationsCommand())
						.then(toggleShowingInvisibleArmorStands())
						.then(dumpArmorStandHeadTextures())
						.then(toggleWebSocketDebug())
						.then(EventNotifications.debugToasts())
						.then(dumpBiome())
						.then(dumpActionBar())
						.then(auditMixins())
				)
		));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.world == null) return;
			if (dumpNearbyEntitiesKey.wasPressed() && !keyDown) {
				client.world.getOtherEntities(client.player, client.player.getBoundingBox().expand(SkyblockerConfigManager.get().debug.dumpRange))
						.stream()
						.map(entity -> {
							NbtWriteView writeView = NbtWriteView.create(new ErrorReporter.Logging(LOGGER), Utils.getRegistryWrapperLookup());
							entity.writeData(writeView);

							return writeView.getNbt();
						})
						.map(NbtHelper::toPrettyPrintedText)
						.forEach(text -> client.player.sendMessage(text, false));
				keyDown = true;
			} else if (!dumpNearbyEntitiesKey.wasPressed() && keyDown) {
				keyDown = false;
			}
		});
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof HandledScreen<?> handledScreen)) return;
			ScreenKeyboardEvents.afterKeyPress(screen).register((_screen, key, scancode, modifier) -> {
				Slot focusedSlot = ((HandledScreenAccessor) handledScreen).getFocusedSlot();
				if (dumpHoveredItemKey.matchesKey(key, scancode) && client.player != null && focusedSlot != null && focusedSlot.hasStack()) {
					if (!Screen.hasShiftDown()) {
						client.player.sendMessage(Constants.PREFIX.get().append("Hovered Item: ").append(SkyblockerConfigManager.get().debug.dumpFormat.format(focusedSlot.getStack())), false);
					} else {
						client.player.sendMessage(Constants.PREFIX.get().append("Held Item NW Calcs: ").append(Text.literal(SkyblockerMod.GSON_COMPACT.toJson(Calculation.LIST_CODEC.encodeStart(JsonOps.INSTANCE, NetworthCalculator.getItemNetworth(focusedSlot.getStack()).calculations()).getOrThrow()))), false);
					}
				}
			});
		});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> dumpPlayersCommand() {
		return literal("dumpPlayers")
				.executes(context -> {
					context.getSource().getWorld().getPlayers().forEach(player -> context.getSource().sendFeedback(Text.of("'" + player.getName().getString() + "'")));
					return Command.SINGLE_SUCCESS;
				});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> toggleShowingInvisibleArmorStands() {
		return literal("toggleShowingInvisibleArmorStands")
				.executes(context -> {
					SkyblockerConfigManager.update(config -> config.debug.showInvisibleArmorStands = !config.debug.showInvisibleArmorStands);
					context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.debug.toggledShowingInvisibleArmorStands", SkyblockerConfigManager.get().debug.showInvisibleArmorStands)));
					return Command.SINGLE_SUCCESS;
				});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> toggleWebSocketDebug() {
		return literal("toggleWebSocketDebug")
				.executes(context -> {
					SkyblockerConfigManager.update(config -> config.debug.webSocketDebug = !SkyblockerConfigManager.get().debug.webSocketDebug);
					context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.debug.toggledWebSocketDebug", SkyblockerConfigManager.get().debug.webSocketDebug)));
					return Command.SINGLE_SUCCESS;
				});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> dumpArmorStandHeadTextures() {
		return literal("dumpArmorStandHeadTextures")
				.executes(context -> {
					List<ArmorStandEntity> armorStands = context.getSource().getWorld().getEntitiesByClass(ArmorStandEntity.class, context.getSource().getPlayer().getBoundingBox().expand(8d), EntityPredicates.NOT_MOUNTED);

					for (ArmorStandEntity armorStand : armorStands) {
						Iterable<ItemStack> equippedItems = ItemUtils.getArmor(armorStand);

						for (ItemStack stack : equippedItems) {
							ItemUtils.getHeadTextureOptional(stack).ifPresent(texture -> context.getSource().sendFeedback(Text.of(texture)));
						}
					}

					return Command.SINGLE_SUCCESS;
				});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> dumpBiome() {
		return literal("dumpBiome")
				.executes(context -> {
					FabricClientCommandSource source = context.getSource();
					RegistryEntry<Biome> biome = source.getWorld().getBiome(source.getPlayer().getBlockPos());

					if (biome != null && biome.value() != null) {
						String biomeData = Biome.CODEC.encodeStart(JsonOps.INSTANCE, biome.value())
								.map(JsonElement::toString)
								.setPartial("")
								.getPartialOrThrow();
						source.sendFeedback(Constants.PREFIX.get().append(Text.literal(String.format("Biome ID: %s, Data: %s", biome.getIdAsString(), biomeData))));
					}

					return Command.SINGLE_SUCCESS;
				});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> dumpActionBar() {
		return literal("dumpActionBar")
				.executes(context -> {
					FabricClientCommandSource source = context.getSource();
					Text actionBar = ((InGameHudInvoker) (source.getClient().inGameHud)).getOverlayMessage();

					if (actionBar != null) {
						Text pretty = NbtHelper.toPrettyPrintedText(TextCodecs.CODEC.encodeStart(Utils.getRegistryWrapperLookup().getOps(NbtOps.INSTANCE), actionBar).getOrThrow());
						source.sendFeedback(Constants.PREFIX.get().append("Action Bar: ").append(pretty));
					}

					return Command.SINGLE_SUCCESS;
				});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> auditMixins() {
		return literal("auditMixins")
				.executes(context -> {
					MixinEnvironment.getCurrentEnvironment().audit();

					return Command.SINGLE_SUCCESS;
				});
	}

	public enum DumpFormat {
		JSON {
			@Override
			public Text format(ItemStack stack) {
				return Text.literal(SkyblockerMod.GSON_COMPACT.toJson(ItemUtils.EMPTY_ALLOWING_ITEMSTACK_CODEC.encodeStart(Utils.getRegistryWrapperLookup().getOps(JsonOps.INSTANCE), stack).getOrThrow()));
			}
		},
		SNBT {
			@Override
			public Text format(ItemStack stack) {
				return NbtHelper.toPrettyPrintedText(ItemUtils.EMPTY_ALLOWING_ITEMSTACK_CODEC.encodeStart(MinecraftClient.getInstance().player.getRegistryManager().getOps(NbtOps.INSTANCE), stack).getOrThrow());
			}
		};

		public abstract Text format(ItemStack stack);
	}
}
