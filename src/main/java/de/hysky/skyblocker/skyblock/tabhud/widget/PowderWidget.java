package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.utils.Formatters;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// this widget shows how much mithril and gemstone powder you have
// (dwarven mines and crystal hollows)
@RegisterWidget
public class PowderWidget extends TabHudWidget {
	private static final MutableText TITLE = Text.literal("Powders").formatted(Formatting.DARK_AQUA, Formatting.BOLD);
	private static final short UPDATE_INTERVAL = 2000;

	// Patterns to match the playerlist lines against
	private static final Pattern MITHRIL_PATTERN = Pattern.compile("Mithril: ([\\d,]+)");
	private static final Pattern GEMSTONE_PATTERN = Pattern.compile("Gemstone: ([\\d,]+)");
	private static final Pattern GLACITE_PATTERN = Pattern.compile("Glacite: ([\\d,]+)");
	// Amounts from last update
	private int lastMithril = 0;
	private int lastGemstone = 0;
	private int lastGlacite = 0;
	// The amount difference between the 2nd last and last update
	private int lastMithrilDiff = 0;
	private int lastGemstoneDiff = 0;
	private int lastGlaciteDiff = 0;
	// A bitfield to keep track of which powders have been updated.
	// First 3 bits represent mithril, gemstone and glacite respectively, with 1 being found and 0 being not found.
	// The 4th bit is for whether the current tick caused an update, which will change the value of lastUpdate when 1.
	private byte updated = 0b0000;
	private long lastUpdate = 0;

	public PowderWidget() {
		super("Powders", TITLE, Formatting.DARK_AQUA.getColorValue());
	}

	@Override
	public void updateContent(List<Text> lines) {
		Matcher matcher = Pattern.compile("").matcher(""); // Placeholder pattern and input to construct a matcher that can be reused
		long msAfterLastUpdate = Util.getMeasuringTimeMs() - lastUpdate;

		for (Text line : lines) {
			switch (matcher.reset(line.getString())) {
				case Matcher m when m.usePattern(MITHRIL_PATTERN).matches() -> {
					int mithril = parseAmount(m);
					// Generally this will only work if the update interval has passed, but we also don't want to stall the update if the amount has changed
					if (mithril != lastMithril || msAfterLastUpdate > UPDATE_INTERVAL) {
						lastMithrilDiff = mithril - lastMithril;
						updated |= 0b1000;
						addComponent(Components.iconTextComponent(Ico.MITHRIL, getTextToDisplay(lastMithrilDiff, line, Formatting.DARK_GREEN)));
						lastMithril = mithril;
					} else {
						addComponent(Components.iconTextComponent(Ico.MITHRIL, getTextToDisplay(lastMithrilDiff, line, Formatting.DARK_GREEN)));
					}
					updated |= 0b001;
				}
				case Matcher m when m.usePattern(GEMSTONE_PATTERN).matches() -> {
					int gemstone = parseAmount(m);
					// Generally this will only work if the update interval has passed, but we also don't want to stall the update if the amount has changed
					if (gemstone != lastGemstone || msAfterLastUpdate > UPDATE_INTERVAL) {
						lastGemstoneDiff = gemstone - lastGemstone;
						updated |= 0b1000;
						addComponent(Components.iconTextComponent(Ico.AMETHYST_SHARD, getTextToDisplay(lastGemstoneDiff, line, Formatting.LIGHT_PURPLE)));
						lastGemstone = gemstone;
					} else {
						addComponent(Components.iconTextComponent(Ico.AMETHYST_SHARD, getTextToDisplay(lastGemstoneDiff, line, Formatting.LIGHT_PURPLE)));
					}
					updated |= 0b010;
				}
				case Matcher m when m.usePattern(GLACITE_PATTERN).matches() -> {
					int glacite = parseAmount(m);
					// Generally this will only work if the update interval has passed, but we also don't want to stall the update if the amount has changed
					if (glacite != lastGlacite || msAfterLastUpdate > UPDATE_INTERVAL) {
						lastGlaciteDiff = glacite - lastGlacite;
						updated |= 0b1000;
						addComponent(Components.iconTextComponent(Ico.BLUE_ICE, getTextToDisplay(lastGlaciteDiff, line, Formatting.AQUA)));
						lastGlacite = glacite;
					} else {
						addComponent(Components.iconTextComponent(Ico.BLUE_ICE, getTextToDisplay(lastGlaciteDiff, line, Formatting.AQUA)));
					}
					updated |= 0b100;
				}
				default -> {}
			}
			if ((updated & 0b111) == 0b111) break; // All powder counts have been updated, no need to continue
		}
		if ((updated & 0b1000) == 0b1000) lastUpdate = Util.getMeasuringTimeMs();
		updated = 0b0000; // Reset the bitfield for the next tick
	}

	private int parseAmount(Matcher matcher) {
		return NumberUtils.toInt(matcher.group(1).replace(",", ""));
	}

	private MutableText getAppendix(int diff, Formatting formatting) {
		return Text.literal(" (" + Formatters.DIFF_NUMBERS.format(diff) + ")").formatted(formatting);
	}

	// Decides whether the appendix should be appended to the line
	private Text getTextToDisplay(int diff, Text line, Formatting formatting) {
		return diff != 0 ? line.copy().append(getAppendix(diff, formatting)) : line;
	}
}
