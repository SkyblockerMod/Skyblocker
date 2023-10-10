package de.hysky.skyblocker.skyblock.tabhud.widget.rift;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.ProgressComponent;
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
        // After you progress further, the tab adds more info so we need to be careful
        // of that.
        // In beginning it only shows montezuma, then timecharms and enigma souls are
        // added.

        String pos44 = PlayerListMgr.strAt(44);

        // LHS short-circuits, so the RHS won't be evaluated on pos44 == null
        if (pos44 == null || !pos44.contains("Rift Progress")) {
            this.addComponent(new PlainTextComponent(Text.literal("No Progress").formatted(Formatting.GRAY)));
            return;
        }

        // let's try to be clever by assuming what progress item may appear where and
        // when to skip testing every slot for every thing.

        // always non-null, as this holds the topmost item.
        // if there is none, there shouldn't be a header.
        String pos45 = PlayerListMgr.strAt(45);

        // Can be Montezuma, Enigma Souls or Timecharms.
        // assume timecharms can only appear here and that they're the last thing to
        // appear, so if this exists, we know the rest.
        if (pos45.contains("Timecharms")) {
            addTimecharmsComponent(45);
            addEnigmaSoulsComponent(46);
            addMontezumaComponent(47);
            return;
        }

        // timecharms didn't appear at the top, so there's two or one entries.
        // assume that if there's two, souls is always top.
        String pos46 = PlayerListMgr.strAt(46);

        if (pos45.contains("Enigma Souls")) {
            addEnigmaSoulsComponent(45);
            if (pos46 != null) {
                // souls might appear alone.
                // if there's a second entry, it has to be montezuma
                addMontezumaComponent(46);
            }
        } else {
            // first entry isn't souls, so it's just montezuma and nothing else.
            addMontezumaComponent(45);
        }

    }

    private static int pcntToCol(float pcnt) {
        return MathHelper.hsvToRgb(pcnt / 300f, 0.9f, 0.9f);
    }

    private void addTimecharmsComponent(int pos) {
        Matcher m = PlayerListMgr.regexAt(pos, TIMECHARMS_PATTERN);

        int current = Integer.parseInt(m.group("current"));
        int total = Integer.parseInt(m.group("total"));
        float pcnt = ((float) current / (float) total) * 100f;
        Text progressText = Text.literal(current + "/" + total);

        ProgressComponent pc = new ProgressComponent(Ico.NETHER_STAR, Text.literal("Timecharms"), progressText,
                pcnt, pcntToCol(pcnt));

        this.addComponent(pc);
    }

    private void addEnigmaSoulsComponent(int pos) {
        Matcher m = PlayerListMgr.regexAt(pos, ENIGMA_SOULS_PATTERN);

        int current = Integer.parseInt(m.group("current"));
        int total = Integer.parseInt(m.group("total"));
        float pcnt = ((float) current / (float) total) * 100f;
        Text progressText = Text.literal(current + "/" + total);

        ProgressComponent pc = new ProgressComponent(Ico.HEART_OF_THE_SEA, Text.literal("Enigma Souls"),
                progressText, pcnt, pcntToCol(pcnt));

        this.addComponent(pc);
    }

    private void addMontezumaComponent(int pos) {
        Matcher m = PlayerListMgr.regexAt(pos, MONTEZUMA_PATTERN);

        int current = Integer.parseInt(m.group("current"));
        int total = Integer.parseInt(m.group("total"));
        float pcnt = ((float) current / (float) total) * 100f;
        Text progressText = Text.literal(current + "/" + total);

        ProgressComponent pc = new ProgressComponent(Ico.BONE, Text.literal("Montezuma"), progressText, pcnt,
                pcntToCol(pcnt));

        this.addComponent(pc);
    }
}
