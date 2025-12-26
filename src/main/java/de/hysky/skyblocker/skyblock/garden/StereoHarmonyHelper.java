package de.hysky.skyblocker.skyblock.garden;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hysky.skyblocker.utils.RegexListUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class StereoHarmonyHelper extends SimpleContainerSolver {
	private static final Pattern PEST_NAME_PATTERN = Pattern.compile("^When playing, (?<name>.+) Pests.*");
	public static final StereoHarmonyHelper INSTANCE = new StereoHarmonyHelper();

	private StereoHarmonyHelper() {
		super("Stereo Harmony");
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().farming.garden.enableStereoHarmonyHelperForContest;
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		List<ColorHighlight> highlights = new ArrayList<>();
		for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {
			//noinspection DataFlowIssue - stripFormatting will not return null (!null to !null)
			Matcher matcher = RegexListUtils.findInList(entry.getValue().skyblocker$getLoreStrings(), ChatFormatting::stripFormatting, PEST_NAME_PATTERN);

			if (matcher != null) {
				String pestName = matcher.group("name").trim();
				String crop = GardenConstants.CROP_BY_PEST.get(pestName);
				if (crop == null) continue;
				boolean isPlaying = ItemUtils.getLoreLineIf(entry.getValue(), text -> text.contains("PLAYING")) != null;

				if (Objects.equals(crop, CurrentJacobCrop.CURRENT_CROP_CONTEST)) {
					highlights.add(isPlaying ? ColorHighlight.yellow(entry.getIntKey()) : ColorHighlight.green(entry.getIntKey()));
				}
			}
		}
		return highlights;
	}
}
