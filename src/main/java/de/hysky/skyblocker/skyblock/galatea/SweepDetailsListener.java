package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatMessageListener;
import net.minecraft.text.Text;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("RegExpRepeatedSpace") // followup messages have 2 leading spaces
public class SweepDetailsListener implements ChatMessageListener {
	protected static final Pattern SWEEP_DETAILS = Pattern.compile("Sweep Details: (\\d+(?:\\.\\d+)?)∮ Sweep");
	protected static final Pattern TREE_TOUGHNESS = Pattern.compile("  (Fig|Mangrove) Tree Toughness: (\\d+(?:\\.\\d+)?) (\\d*(?:\\.\\d+)?) Logs");
	protected static final Pattern AXE_THROW_PENALTY = Pattern.compile("  Axe throw: (-\\d+)% Sweep (\\d+(?:\\.\\d+)?) Logs");
	protected static final Pattern WRONG_STYLE_PENALTY = Pattern.compile("  Wrong Style: (-\\d+)% Sweep (\\d+(?:\\.\\d+)?) Logs ([a-zA-Z ]*)!!");
	protected static final Pattern TREE_GIFT = Pattern.compile("\\s+TREE GIFT");

	public static boolean active = false;
	public static float lastMatch = -1;

	public static float maxSweep = -1;
	public static float lastSweep = -1;
	public static String lastTreeType;
	public static String toughness;
	public static String logs;
	public static boolean axePenalty;
	public static float  axePenaltyAmount;
	public static boolean stylePenalty;
	public static float stylePenaltyAmount;
	public static String correctStyle;

	private static void resetStats() {
		active = false;
		lastMatch = -1;
		maxSweep = -1;
		lastSweep = -1;
		axePenalty = false;
		stylePenalty = false;
	}

	@Override
	public ChatFilterResult onMessage(Text message, String asString) {
		if (!Utils.isInGalatea()) return ChatFilterResult.PASS;
		String msg = message.getString();

		Matcher sweepDetails = SWEEP_DETAILS.matcher(msg);
		if (sweepDetails.matches() && sweepDetails.groupCount() == 1) {
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

		Matcher treeToughness = TREE_TOUGHNESS.matcher(msg);
		if (treeToughness.matches() && treeToughness.groupCount() == 3) {

			lastTreeType = treeToughness.group(1);
			toughness = treeToughness.group(2);
			logs = treeToughness.group(3);
			return ChatFilterResult.FILTER;
		}

		Matcher axeThrow = AXE_THROW_PENALTY.matcher(msg);
		if (axeThrow.matches() && axeThrow.groupCount() == 2) {
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
		if (wrongStyle.matches() && wrongStyle.groupCount() == 3) {
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

		// todo: remove this since it doesn't work well
		// (sweep details can sometimes come in after the tree gift)
		Matcher treeGift = TREE_GIFT.matcher(msg.strip());
		System.out.println(msg);
		if (treeGift.matches()) {
			System.out.println("TREE GIFT");
			resetStats();
			return ChatFilterResult.PASS;
		}

		return ChatFilterResult.PASS;
	}
}
