package de.hysky.skyblocker.skyblock.garden;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import net.minecraft.item.ItemStack;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class StereoHarmonyHelper extends SimpleContainerSolver {
	private static final Pattern PEST_NAME_PATTERN = Pattern.compile("^When playing, (?<name>.+) Pests are(?: more)*");
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
			var matcher = ItemUtils.getLoreLineIfMatch(entry.getValue(), PEST_NAME_PATTERN);

			if (matcher != null) {
				var pestName = matcher.group("name");
				var crop = GardenConstants.CROP_BY_PEST.get(pestName);

				if (Objects.equals(crop, CurrentJacobCrop.CURRENT_CROP_CONTEST)) {
					highlights.add(ColorHighlight.green(entry.getIntKey()));
				}
			}
		}
		return highlights;
	}
}
