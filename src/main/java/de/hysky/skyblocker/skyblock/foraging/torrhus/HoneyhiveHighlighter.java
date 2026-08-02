package de.hysky.skyblocker.skyblock.foraging.torrhus;

import java.util.function.Predicate;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.foraging.AbstractBlockHighlighter;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class HoneyhiveHighlighter extends AbstractBlockHighlighter {
	private static final HoneyhiveHighlighter INSTANCE = new HoneyhiveHighlighter();

	private HoneyhiveHighlighter() {
		Predicate<BlockState> statePredicate = state -> state.is(Blocks.BEE_NEST) && state.getValueOrElse(BeehiveBlock.HONEY_LEVEL, 0) == BeehiveBlock.MAX_HONEY_LEVELS;

		super(statePredicate, ColorUtils.getFloatComponents(DyeColor.BLUE.getTextColor()));
	}

	@Init
	public static void initClass() {
		INSTANCE.init();
	}

	@Override
	protected boolean shouldProcess() {
		return Utils.isInTorrhusCanyon() && SkyblockerConfigManager.get().foraging.torrhusCanyon.enableHoneyhiveHighlighter;
	}
}
