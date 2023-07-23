package me.xmrvizzy.skyblocker.skyblock.tabhud.widget.rift;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.Widget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.ProgressComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public class RiftProgressWidget extends Widget {

	private static final MutableText TITLE = Text.literal("Rift Progress").formatted(Formatting.BLUE, Formatting.BOLD);

	private static final Pattern TIMECHARMS_PATTERN = Pattern.compile("Timecharms: (?<current>[0-9]+)\\/(?<total>[0-9]+)");
	private static final Pattern ENIGMA_SOULS_PATTERN = Pattern.compile("Enigma Souls: (?<current>[0-9]+)\\/(?<total>[0-9]+)");
	private static final Pattern MONTEZUMA_PATTERN = Pattern.compile("Montezuma: (?<current>[0-9]+)\\/(?<total>[0-9]+)");

	public RiftProgressWidget() {
		super(TITLE, Formatting.BLUE.getColorValue());
    }

    @Override
    public void updateContent() {
		// After you progress further the tab adds more info so we need to be careful of
		// that
		// In beginning it only shows montezuma, then timecharms and enigma souls are
		// added
		String pos45 = PlayerListMgr.strAt(45); // Can be Montezuma or Timecharms
		String pos46 = PlayerListMgr.strAt(46); // Can be Enigma Souls or Empty
		String pos47 = PlayerListMgr.strAt(47); // Can be Montezuma or "Good to know" heading

		boolean hasTimecharms = false;
		boolean hasEnigmaSouls = false;
		int montezumaPos = 0;

		// Check each position to see what is or isn't there so we don't try adding
		// invalid components
		if (pos45.contains("Timecharms"))
			hasTimecharms = true;
		if (pos46.contains("Enigma Souls"))
			hasEnigmaSouls = true;

		// Small ternary to account for positions, defaults to -1 if it for some reason
		// does not exist (which shouldn't be the case!)
		montezumaPos = (pos47.contains("Montezuma")) ? 47 : (pos45.contains("Montezuma")) ? 45 : -1;

		if (hasTimecharms) {
			Matcher m = PlayerListMgr.regexAt(45, TIMECHARMS_PATTERN);

			int current = Integer.parseInt(m.group("current"));
			int total = Integer.parseInt(m.group("total"));
			float pcnt = ((float) current / (float) total) * 100f;
			Text progressText = Text.literal(current + "/" + total);

			ProgressComponent pc = new ProgressComponent(Ico.NETHER_STAR, Text.literal("Timecharms"), progressText,
					pcnt, pcntToCol(pcnt));

			this.addComponent(pc);
		}

		if (hasEnigmaSouls) {
			Matcher m = PlayerListMgr.regexAt(46, ENIGMA_SOULS_PATTERN);

			int current = Integer.parseInt(m.group("current"));
			int total = Integer.parseInt(m.group("total"));
			float pcnt = ((float) current / (float) total) * 100f;
			Text progressText = Text.literal(current + "/" + total);

			ProgressComponent pc = new ProgressComponent(Ico.HEART_OF_THE_SEA, Text.literal("Enigma Souls"),
					progressText, pcnt, pcntToCol(pcnt));

			this.addComponent(pc);
		}

		if (montezumaPos != -1) {
			Matcher m = PlayerListMgr.regexAt(montezumaPos, MONTEZUMA_PATTERN);

			int current = Integer.parseInt(m.group("current"));
			int total = Integer.parseInt(m.group("total"));
			float pcnt = ((float) current / (float) total) * 100f;
			Text progressText = Text.literal(current + "/" + total);

			ProgressComponent pc = new ProgressComponent(Ico.BONE, Text.literal("Montezuma"), progressText, pcnt,
					pcntToCol(pcnt));

			this.addComponent(pc);
		}

	}

	private static int pcntToCol(float pcnt) {
		return MathHelper.hsvToRgb(pcnt / 300f, 0.9f, 0.9f);
	}
}
