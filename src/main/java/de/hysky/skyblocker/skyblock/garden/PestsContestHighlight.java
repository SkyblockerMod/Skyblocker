package de.hysky.skyblocker.skyblock.garden;

import static java.util.Map.entry;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.entity.decoration.ArmorStandEntity;
import org.apache.commons.lang3.StringUtils;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;

public class PestsContestHighlight {
	private static final Pattern CURRENT_CROP_PATTERN = Pattern.compile("^ [○☘] (?<crop>.+) .+$");
	private static final Pattern PEST_NAME_PATTERN = Pattern.compile("ൠ (?<name>.+) \\d+❤");
	private static String CURRENT_CROP_CONTEST;
	private static final Map<String, String> CROP_BY_PEST = Map.ofEntries(
			entry("Wheat", "Fly"),
			entry("Sugar Cane", "Mosquito"),
			entry("Carrot", "Cricket"),
			entry("Potato", "Locust"),
			entry("Melon", "Earthworm"),
			entry("Pumpkin", "Rat"),
			entry("Cocoa Beans", "Moth"),
			entry("Nether Wart", "Beetle"),
			entry("Cactus", "Mite"),
			entry("Mushroom", "Slug")
	);

	@Init
	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(PestsContestHighlight::update, 20);
	}

	private static void update() {
		// Check if scoreboard text contains no 'Jacob's Contest' should be enough
		// Detecting chat to clear CURRENT_CROP_CONTEST is not a good solution because of a scoreboard has delayed update rate
		if (Utils.STRING_SCOREBOARD.stream().noneMatch(s -> s.contains("Jacob's Contest"))) {
			CURRENT_CROP_CONTEST = null;
			return;
		}

		for (String line : Utils.STRING_SCOREBOARD) {
			Matcher matcher = CURRENT_CROP_PATTERN.matcher(line);

			if (matcher.matches()) {
				String crop = matcher.group("crop");

				if (!Objects.equals(CURRENT_CROP_CONTEST, crop)) {
					CURRENT_CROP_CONTEST = crop;
				}
			}
		}
	}

	/**
	 * Compares the armor stand name with current collected crop during Jacob's Contest.
	 */
	public static boolean isPestNameMatchCurrentContest(ArmorStandEntity entity) {
		if (StringUtils.isEmpty(CURRENT_CROP_CONTEST)) {
			return false;
		}

		// Get the list of the armor stands, then filter only pest name pattern
		Function<String, Matcher> matcherFunction = PEST_NAME_PATTERN::matcher;
		List<ArmorStandEntity> armorStandEntities = entity.getWorld().getEntitiesByClass(ArmorStandEntity.class, entity.getBoundingBox().expand(1, 2, 1), other -> matcherFunction.apply(other.getName().getString()).matches());
		String pestName = CROP_BY_PEST.get(CURRENT_CROP_CONTEST);

		if (armorStandEntities.isEmpty() || StringUtils.isEmpty(pestName)) {
			return false;
		}

		Matcher matcher = matcherFunction.apply(armorStandEntities.getFirst().getName().getString());

		// Found matched pest name
		if (matcher.matches()) {
			String name = matcher.group("name");
			return pestName.equals(name);
		}
		return false;
	}
}
