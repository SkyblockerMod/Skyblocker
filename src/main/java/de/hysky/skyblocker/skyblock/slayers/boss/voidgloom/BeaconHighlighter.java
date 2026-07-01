package de.hysky.skyblocker.skyblock.slayers.boss.voidgloom;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.WorldEvents;
import de.hysky.skyblocker.mixins.accessors.EnderManAccessor;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.LevelRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BeaconHighlighter {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final float[] RED_COLOR_COMPONENTS = { 1.0f, 0.0f, 0.0f };
	private static final long BEACON_DURATION_MS = 5000L;
	private static boolean beaconThrown;
	private static final Object2ObjectMap<UUID, List<Vec3>> BEACON_ENTITY_PATHS = new Object2ObjectOpenHashMap<>();
	private static final Object2LongOpenHashMap<BlockPos> BEACONS = new Object2LongOpenHashMap<>();

	/**
	 * Initializes the beacon highlighting system.
	 * {@link BeaconHighlighter#extractRendering(PrimitiveCollector)} is called to extract the beacon highlight for rendering.
	 */
	@Init
	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(BeaconHighlighter::updateBeaconEntities, 1);
		WorldEvents.BLOCK_STATE_UPDATE.register(BeaconHighlighter::onBlockStateUpdate);
		LevelRenderExtractionCallback.EVENT.register(BeaconHighlighter::extractRendering);
		ClientPlayConnectionEvents.JOIN.register((_, _, _) -> reset());
		ClientReceiveMessageEvents.ALLOW_GAME.register(BeaconHighlighter::onMessage);
	}

	private static void reset() {
		beaconThrown = false;
		BEACON_ENTITY_PATHS.clear();
		BEACONS.clear();
	}

	@SuppressWarnings("ConstantValue")
	public static <T> void onThrowBeacon(SyncedDataHolder dataHolder, SynchedEntityData.DataItem<T> dataItem, SynchedEntityData.DataValue<T> newValue) {
		if (Utils.isInTheEnd() && SkyblockerConfigManager.get().slayers.endermanSlayer.enableYangGlyphsNotification
				&& dataHolder instanceof Entity entity && SlayerManager.isSelectedBoss(entity.getUUID())
				&& dataItem.getAccessor() == EnderManAccessor.getDATA_CARRY_STATE()
				&& dataItem.getValue() instanceof Optional<?> value && value.isPresent() && value.get() instanceof BlockState state && state.is(Blocks.BEACON)
				&& ((Optional<?>) newValue.value()).isEmpty()) {
			beaconThrown = true;
			CLIENT.gui.hud.setTimes(5, 20, 10);
			CLIENT.gui.hud.setTitle(Component.literal("Yang Glyph!").withStyle(ChatFormatting.RED));
			CLIENT.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 100f, 0.1f);
		}
	}

	public static void onEntitySpawn(ArmorStand entity) {
		if (Utils.isInTheEnd() && SlayerManager.isFightingSlayerType(SlayerType.VOIDGLOOM) && beaconThrown
				&& entity.hasItemInSlot(EquipmentSlot.HEAD) && entity.getItemBySlot(EquipmentSlot.HEAD).is(Items.BEACON)) {
			BEACON_ENTITY_PATHS.computeIfAbsent(entity.getUUID(), _ -> new ArrayList<>()).add(entity.getEyePosition());
		}
	}

	private static void updateBeaconEntities() {
		if (CLIENT.level == null || !Utils.isInTheEnd() || !SlayerManager.isFightingSlayerType(SlayerType.VOIDGLOOM) || !SkyblockerConfigManager.get().slayers.endermanSlayer.highlightBeacons) return;

		for (Map.Entry<UUID, List<Vec3>> beaconEntityPath : BEACON_ENTITY_PATHS.entrySet()) {
			Entity entity = CLIENT.level.getEntity(beaconEntityPath.getKey());
			if (entity != null && !beaconEntityPath.getValue().getLast().equals(entity.position())) {
				beaconEntityPath.getValue().add(entity.getEyePosition());
			}
		}
	}

	private static void onBlockStateUpdate(BlockPos pos, @Nullable BlockState oldState, BlockState newState) {
		if (Utils.isInTheEnd() && SlayerManager.isFightingSlayerType(SlayerType.VOIDGLOOM) && SkyblockerConfigManager.get().slayers.endermanSlayer.highlightBeacons) {
			BEACONS.removeLong(pos);

			if (newState.is(Blocks.BEACON)) {
				BEACONS.put(pos.immutable(), System.currentTimeMillis());
				beaconThrown = false;
			} else if (oldState == null || oldState.is(Blocks.BEACON)) {
				List<Vec3> closestPath = BEACON_ENTITY_PATHS.values().stream()
						.min(Comparator.comparingDouble(path -> path.getLast().distanceToSqr(Vec3.atCenterOf(pos))))
						.filter(path -> path.getLast().distanceToSqr(Vec3.atCenterOf(pos)) < 4)
						.orElse(null);
				BEACON_ENTITY_PATHS.values().removeIf(path -> path == closestPath);
			}
		}
	}

	private static boolean onMessage(Component text, boolean overlay) {
		if (Utils.isInTheEnd() && !overlay) {
			String message = text.getString();

			if (message.contains("SLAYER QUEST COMPLETE!") || message.contains("NICE! SLAYER BOSS SLAIN!")) reset();
		}

		return true;
	}

	/**
	 * Renders the beacon glow around it. It is rendered in a red color with 50% opacity, and
	 * is visible through walls.
	 */
	private static void extractRendering(PrimitiveCollector collector) {
		if (Utils.isInTheEnd() && SkyblockerConfigManager.get().slayers.endermanSlayer.highlightBeacons && SlayerManager.isFightingSlayerType(SlayerType.VOIDGLOOM)) {
			for (List<Vec3> beaconEntityPath : BEACON_ENTITY_PATHS.values()) {
				if (beaconEntityPath.size() > 1) collector.submitLinesFromPoints(beaconEntityPath.toArray(Vec3[]::new), RED_COLOR_COMPONENTS, 1f, 10f, true);
			}

			for (Object2LongMap.Entry<BlockPos> beacon : BEACONS.object2LongEntrySet()) {
				collector.submitFilledBox(beacon.getKey(), RED_COLOR_COMPONENTS, 0.6f, true);

				long elapsed = System.currentTimeMillis() - beacon.getLongValue();
				float remainingSec = (BEACON_DURATION_MS - elapsed) / 1000f;
				if (remainingSec >= 0) {
					Component text = Component.literal(String.format("%.1fs", remainingSec)).withStyle(ChatFormatting.AQUA);
					collector.submitText(text, Vec3.atCenterOf(beacon.getKey().above()), 3, true);
				}
			}
		}
	}
}
