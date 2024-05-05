package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.TableComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Map.entry;

// this widget shows info about the current jacob's contest (garden only)

public class JacobsContestWidget extends HudWidget {

    private static final MutableText TITLE = Text.literal("Jacob's Contest").formatted(Formatting.YELLOW,
            Formatting.BOLD);

    //TODO Properly match the contest placement and display it
    private static final Pattern CROP_PATTERN = Pattern.compile("(?<fortune>[☘○]) (?<crop>[A-Za-z ]+).*");

    public static final Map<String, ItemStack> FARM_DATA = Map.ofEntries(
            entry("Wheat", new ItemStack(Items.WHEAT)),
            entry("Sugar Cane", new ItemStack(Items.SUGAR_CANE)),
            entry("Carrot", new ItemStack(Items.CARROT)),
            entry("Potato", new ItemStack(Items.POTATO)),
            entry("Melon", new ItemStack(Items.MELON_SLICE)),
            entry("Pumpkin", new ItemStack(Items.PUMPKIN)),
            entry("Cocoa Beans", new ItemStack(Items.COCOA_BEANS)),
            entry("Nether Wart", new ItemStack(Items.NETHER_WART)),
            entry("Cactus", new ItemStack(Items.CACTUS)),
            entry("Mushroom", new ItemStack(Items.RED_MUSHROOM))
    );

    public JacobsContestWidget() {
        super(TITLE, Formatting.YELLOW.getColorValue());
    }

    @Override
    public void updateContent() {
        Text jacobStatus = PlayerListMgr.textAt(76);

        if (jacobStatus.getString().equals("ACTIVE")) {
            this.addComponent(new IcoTextComponent(Ico.CLOCK, jacobStatus));
        } else {
            this.addSimpleIcoText(Ico.CLOCK, "Starts in:", Formatting.GOLD, 76);
        }

        TableComponent tc = new TableComponent(1, 3, Formatting.YELLOW.getColorValue());

        for (int i = 77; i < 80; i++) {
            Matcher item = PlayerListMgr.regexAt(i, CROP_PATTERN);
            IcoTextComponent itc;
            if (item == null) {
                itc = new IcoTextComponent();
            } else {
                String cropName = item.group("crop").trim(); //Trimming is needed because during a contest the space separator will be caught
                if (item.group("fortune").equals("☘")) {
                    itc = new IcoTextComponent(FARM_DATA.get(cropName), Text.literal(cropName).append(Text.literal(" ☘").formatted(Formatting.GOLD)));
                } else {
                    itc = new IcoTextComponent(FARM_DATA.get(cropName), Text.of(cropName));
                }
            }
            tc.addToCell(0, i - 77, itc);
        }
        this.addComponent(tc);
    }
}
