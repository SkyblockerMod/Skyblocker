package de.hysky.skyblocker.skyblock.entity.glow.adder;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.apache.commons.lang3.StringUtils;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.skyblock.garden.CurrentJacobCrop;
import de.hysky.skyblocker.skyblock.garden.GardenConstants;
import de.hysky.skyblocker.skyblock.item.HeadTextures;
import de.hysky.skyblocker.skyblock.VacuumCache;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;

public class GardenGlowAdder extends MobGlowAdder {
	private static final GardenGlowAdder INSTANCE = new GardenGlowAdder();
	private static final int PEST_COLOUR = 0xB62F00;
	private static final Pattern CURRENT_CROP_PATTERN = Pattern.compile("^ [○☘] (?<crop>.+) .+$");

	@Init
	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(GardenGlowAdder::update, 20);
	}

	@Override
	public int computeColour(Entity entity) {
		return switch (entity) {
			case ArmorStand as when isPestHead(as) ->
					doesPestMatchCurrentContest(as) ?
							// Pests but during Jacob's Contest
							ChatFormatting.GREEN.getColor() :
							// Pests from currently playing vinyl
							doesPestMatchCurrentVinyl(as) ?
									ChatFormatting.AQUA.getColor() :
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
	private static boolean isPestHead(ArmorStand entity) {
		return entity.hasItemInSlot(EquipmentSlot.HEAD) && HeadTextures.PEST_HEADS
				.contains(ItemUtils.getHeadTexture(entity.getItemBySlot(EquipmentSlot.HEAD)));
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
	public static boolean doesPestMatchCurrentContest(ArmorStand entity) {
		if (StringUtils.isEmpty(CurrentJacobCrop.CURRENT_CROP_CONTEST)) {
			return false;
		}

		// Filter only pest head that matches by crop
		return entity.hasItemInSlot(EquipmentSlot.HEAD) && GardenConstants.PEST_HEAD_BY_CROP
				.get(CurrentJacobCrop.CURRENT_CROP_CONTEST)
				.contains(ItemUtils.getHeadTexture(entity.getItemBySlot(EquipmentSlot.HEAD)));
	}

	/**
	 * Matches the armor stand head with currently playing vinyl outside of Jacob's Contest.
	 */
	public static boolean doesPestMatchCurrentVinyl(ArmorStand entity) {
		if (!SkyblockerConfigManager.get().farming.garden.vinylHighlighter)
			return false;

		String vinyl = VacuumCache.getVinyl();

		// Only applies outside of Jacob's Contests
		if (!StringUtils.isEmpty(CurrentJacobCrop.CURRENT_CROP_CONTEST) || vinyl.isEmpty()) {
			return false;
		}

		// Filter only pest head that matches by name
		return entity.hasItemInSlot(EquipmentSlot.HEAD) && GardenConstants.PEST_HEAD_BY_CROP
				.get(GardenConstants.CROP_BY_VINYL.get(vinyl))
				.contains(ItemUtils.getHeadTexture(entity.getItemBySlot(EquipmentSlot.HEAD)));
	}
}
