package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatMessageListener;
import net.minecraft.text.Text;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("RegExpRepeatedSpace") // followup messages have 2 leading spaces
public class SweepDetailsListener implements ChatMessageListener {
	protected static final Pattern SWEEP_DETAILS = Pattern.compile("Sweep Details: ([\\d.]+)∮ Sweep");
	protected static final Pattern TREE_TOUGHNESS = Pattern.compile("  (.+?) Tree Toughness: ([\\d.]+) ([\\d.]+) Logs");
	protected static final Pattern AXE_THROW_PENALTY = Pattern.compile("  Axe throw: (-\\d+)% Sweep ([\\d.]+) Logs");
	protected static final Pattern WRONG_STYLE_PENALTY = Pattern.compile("  Wrong Style: (-\\d+)% Sweep ([\\d.]+) Logs ([a-zA-Z ]*)!!");

	public static boolean active = false;
	public static float lastMatch = -1;

	public static float maxSweep = -1;
	public static float lastSweep = -1;
	public static String lastTreeType = "Unknown";
	public static String toughness;
	public static String logs;
	public static boolean axePenalty;
	public static float axePenaltyAmount;
	public static boolean stylePenalty;
	public static float stylePenaltyAmount;
	public static String correctStyle;

	private static void resetStats() {
		active = false;
		lastMatch = -1;
		maxSweep = -1;
		lastSweep = -1;
		lastTreeType = "Unknown";
		axePenalty = false;
		stylePenalty = false;
	}

	@Override
	public ChatFilterResult onMessage(Text message, String asString) {
		if (!SweepDetailsHudWidget.LOCATIONS.contains(Utils.getLocation())) return ChatFilterResult.PASS;
		if (!SkyblockerConfigManager.get().foraging.galatea.enableSweepDetailsWidget) return ChatFilterResult.PASS;
		String msg = message.getString();

		Matcher sweepDetails = SWEEP_DETAILS.matcher(msg);
		if (sweepDetails.matches()) {
			resetStats();
			active = true;
			lastMatch = System.currentTimeMillis();

			String rawMaxSweep = sweepDetails.group(1);
			if (NumberUtils.isCreatable(rawMaxSweep)) {
				maxSweep = Float.parseFloat(rawMaxSweep);
			} else {
				maxSweep = -1;
			}
			lastSweep = maxSweep;

			return ChatFilterResult.FILTER;
		}

		if (active && System.currentTimeMillis() > lastMatch + 1_000) active = false;
		if (!active) return ChatFilterResult.PASS;

		Matcher treeToughness = TREE_TOUGHNESS.matcher(msg);
		if (treeToughness.matches()) {

			lastTreeType = treeToughness.group(1);
			toughness = treeToughness.group(2);
			logs = treeToughness.group(3);
			return ChatFilterResult.FILTER;
		}

		Matcher axeThrow = AXE_THROW_PENALTY.matcher(msg);
		if (axeThrow.matches()) {
			axePenalty = true;

			String rawAxePenaltyAmount = axeThrow.group(1);
			if (NumberUtils.isCreatable(rawAxePenaltyAmount)) {
				axePenaltyAmount = Float.parseFloat(rawAxePenaltyAmount);
				lastSweep *= -axePenaltyAmount / 100;
			} else {
				axePenaltyAmount = -1;
			}

			logs = axeThrow.group(2);
			return ChatFilterResult.FILTER;
		}

		Matcher wrongStyle = WRONG_STYLE_PENALTY.matcher(msg);
		if (wrongStyle.matches()) {
			stylePenalty = true;

			String rawStylePenaltyAmount = wrongStyle.group(1);
			if (NumberUtils.isCreatable(rawStylePenaltyAmount)) {
				stylePenaltyAmount = Float.parseFloat(rawStylePenaltyAmount);
				lastSweep *= -stylePenaltyAmount / 100;
				System.out.println(lastSweep);
			} else {
				stylePenaltyAmount = -1;
			}

			logs = wrongStyle.group(2);
			correctStyle = wrongStyle.group(3);
			return ChatFilterResult.FILTER;
		}

		return ChatFilterResult.PASS;
	}
}
