package de.hysky.skyblocker.skyblock.entity.glow.adder;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.skyblock.garden.CurrentJacobCrop;
import de.hysky.skyblocker.skyblock.garden.GardenConstants;
import de.hysky.skyblocker.skyblock.item.HeadTextures;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.Formatting;

public class GardenGlowAdder extends MobGlowAdder {
	private static final GardenGlowAdder INSTANCE = new GardenGlowAdder();
	private static final int PEST_COLOUR = 0xb62f00;
	private static final Pattern CURRENT_CROP_PATTERN = Pattern.compile("^ [○☘] (?<crop>.+) .+$");

	@Init
	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(GardenGlowAdder::update, 20);
	}

	@Override
	public int computeColour(Entity entity) {
		return switch (entity) {
			case ArmorStandEntity as when isPestHead(as) ->
					doesPestMatchCurrentContest(as) ?
							// Pests but during Jacob's Contest
							Formatting.GREEN.getColorValue() :
							// Default color
							PEST_COLOUR;
			default -> NO_GLOW;
		};
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().farming.garden.pestHighlighter && Utils.isInGarden();
	}

	/**
	 * Compares the armor items of an armor stand to the Pest head texture to determine if it is a Pest head.
	 */
	private static boolean isPestHead(ArmorStandEntity entity) {
		return entity.hasStackEquipped(EquipmentSlot.HEAD) && HeadTextures.PEST_HEADS
				.contains(ItemUtils.getHeadTexture(entity.getEquippedStack(EquipmentSlot.HEAD)));
	}

	private static void update() {
		// Check if scoreboard text contains no 'Jacob's Contest' should be enough
		// Detecting chat to clear CURRENT_CROP_CONTEST is not a good solution because of a scoreboard has delayed update rate
		if (!INSTANCE.isEnabled() || Utils.STRING_SCOREBOARD.stream().noneMatch(s -> s.contains("Jacob's Contest"))) {
			CurrentJacobCrop.CURRENT_CROP_CONTEST = null;
			return;
		}

		for (String line : Utils.STRING_SCOREBOARD) {
			Matcher matcher = CURRENT_CROP_PATTERN.matcher(line);

			if (matcher.matches()) {
				String crop = matcher.group("crop").trim();

				if (!Objects.equals(CurrentJacobCrop.CURRENT_CROP_CONTEST, crop)) {
					CurrentJacobCrop.CURRENT_CROP_CONTEST = crop;
				}
			}
		}
	}

	/**
	 * Matches the armor stand head with current collected crop during Jacob's Contest.
	 */
	public static boolean doesPestMatchCurrentContest(ArmorStandEntity entity) {
		if (StringUtils.isEmpty(CurrentJacobCrop.CURRENT_CROP_CONTEST)) {
			return false;
		}

		// Filter only pest head that matches by crop
		return entity.hasStackEquipped(EquipmentSlot.HEAD) && GardenConstants.PEST_HEAD_BY_CROP
				.get(CurrentJacobCrop.CURRENT_CROP_CONTEST)
				.contains(ItemUtils.getHeadTexture(entity.getEquippedStack(EquipmentSlot.HEAD)));
	}
}
