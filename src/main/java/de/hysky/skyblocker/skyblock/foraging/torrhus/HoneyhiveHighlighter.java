package de.hysky.skyblocker.skyblock.foraging.torrhus;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.foraging.AbstractBlockHighlighter;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;

public final class HoneyhiveHighlighter extends AbstractBlockHighlighter {
	private static final HoneyhiveHighlighter INSTANCE = new HoneyhiveHighlighter();
	private static final String LOOT_PROMPT = "Click to loot!";
	/**
	 * Hives highlighted because of their "Click to loot!" armor stand, keyed by the armor stand's entity id.
	 * Some hives never get their honey level synced by the server, so the prompt is the only indication that they're full.
	 */
	private final Int2ObjectMap<BlockPos> promptedHives = new Int2ObjectOpenHashMap<>();

	private HoneyhiveHighlighter() {
		// Beehives attached to protected trees aren't Honeyhives and can be identifed since they're attached above via a Birch Fence
		BiPredicate<ClientLevel, BlockPos> posPredicate = (level, pos) -> !level.getBlockState(pos.above()).is(Blocks.BIRCH_FENCE);
		Predicate<BlockState> statePredicate = state -> state.is(Blocks.BEE_NEST) && state.getValueOrElse(BeehiveBlock.HONEY_LEVEL, 0) == BeehiveBlock.MAX_HONEY_LEVELS;

		super(posPredicate, statePredicate, ColorUtils.getFloatComponents(DyeColor.BLUE.getTextColor()));
	}

	@Init
	public static void initClass() {
		INSTANCE.init();
		ClientEntityEvents.ENTITY_UNLOAD.register((entity, _) -> INSTANCE.removePrompt(entity.getId()));
	}

	public static void onArmorStandUpdate(ArmorStand armorStand) {
		INSTANCE.updatePrompt(armorStand);
	}

	private void updatePrompt(ArmorStand armorStand) {
		if (!shouldProcess()) return;

		if (armorStand.hasCustomName() && LOOT_PROMPT.equals(ChatFormatting.stripFormatting(armorStand.getName().getString()))) {
			if (promptedHives.containsKey(armorStand.getId())) return;

			// The prompt floats above the hive, check a few blocks down from the armor stand to find it
			BlockPos.MutableBlockPos pos = armorStand.blockPosition().mutable().move(0, 1, 0);
			for (int i = 0; i < 4; i++, pos.move(0, -1, 0)) {
				if (armorStand.level().getBlockState(pos).is(Blocks.BEE_NEST)) {
					BlockPos hive = pos.immutable();
					promptedHives.put(armorStand.getId(), hive);
					highlightedBlocks.add(hive);
					return;
				}
			}
		} else {
			removePrompt(armorStand.getId());
		}
	}

	private void removePrompt(int entityId) {
		BlockPos hive = promptedHives.remove(entityId);
		ClientLevel level = Minecraft.getInstance().level;

		// Keep the highlight if the block state alone justifies it, the prompt may just have gone out of tracking range
		if (hive != null && (level == null || !statePredicate.test(level.getBlockState(hive)))) {
			highlightedBlocks.remove(hive);
		}
	}

	@Override
	public void reset() {
		super.reset();
		promptedHives.clear();
	}

	@Override
	protected boolean shouldProcess() {
		return Utils.isInTorrhusCanyon() && SkyblockerConfigManager.get().foraging.torrhusCanyon.enableHoneyhiveHighlighter;
	}
}
