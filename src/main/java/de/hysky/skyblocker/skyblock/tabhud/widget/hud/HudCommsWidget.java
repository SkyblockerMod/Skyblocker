package de.hysky.skyblocker.skyblock.tabhud.widget.hud;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dwarven.DwarvenHud.Commission;
import de.hysky.skyblocker.skyblock.tabhud.util.Colors;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.ProgressComponent;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

// this widget shows the status of the king's commissions.
// (dwarven mines and crystal hollows)
// USE ONLY WITH THE DWARVEN HUD!

public class HudCommsWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Commissions").formatted(Formatting.DARK_AQUA, Formatting.BOLD);

    private List<Commission> commissions;
    private boolean isFancy;

    // disgusting hack to get around text renderer issues.
    // the ctor eventually tries to get the font's height, which doesn't work
    //   when called before the client window is created (roughly).
    // the rebdering god 2 from the fabricord explained that detail, thanks!
    public static final HudCommsWidget INSTANCE = new HudCommsWidget();
    public static final HudCommsWidget INSTANCE_CFG = new HudCommsWidget();

    // another repulsive hack to make this widget-like hud element work with the new widget class
    // DON'T USE WITH THE WIDGET SYSTEM, ONLY USE FOR DWARVENHUD!
    public HudCommsWidget() {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());
    }

    public void updateData(List<Commission> commissions, boolean isFancy) {
        this.commissions = commissions;
        this.isFancy = isFancy;
    }

    @Override
    public void updateContent() {
        for (Commission comm : commissions) {

            Text c = Text.literal(comm.commission());

            float p = 100f;
            if (!comm.progression().contains("DONE")) {
                p = Float.parseFloat(comm.progression().substring(0, comm.progression().length() - 1));
            }

            Component comp;
            var max = getCommissionMax(comm.commission());
            if (isFancy) {
                if (SkyblockerConfigManager.get().mining.dwarvenHud.showNumbers && max != null) {
                    comp = new ProgressComponent(Ico.BOOK, c, p, max, Colors.pcntToCol(p));
                } else {
                    comp = new ProgressComponent(Ico.BOOK, c, p, Colors.pcntToCol(p));
                }
            } else {
                if (SkyblockerConfigManager.get().mining.dwarvenHud.showNumbers && max != null) {
                    comp = new PlainTextComponent(
                            Text.literal(comm.commission() + ": ").append(
                                    Text.literal(p == 100f ? "DONE" : Math.round(max * (p / 100)) + "/" + max).withColor(Colors.pcntToCol(p))
                            )
                    );
                } else {
                    comp = new PlainTextComponent(
                            Text.literal(comm.commission() + ": ").append(
                                    Text.literal(comm.progression()).withColor(Colors.pcntToCol(p))
                            )
                    );
                }
            }
            this.addComponent(comp);
        }
    }

    /**
     * Gets the actions needed to complete a commission
     *
     * @param commission the string name of the commission
     * @return the actions needed to complete the commission
     */
    private static Integer getCommissionMax(String commission) {
        switch (commission) {
            case "Mithril Miner" -> {
                return 350;
            }
            case "Lava Springs Mithril", "Royal Mines Mithril", "Cliffside Veins Mithril", "Rampart's Quarry Mithril",
                 "Upper Mines Mithril" -> {
                return 250;
            }
            case "Titanium Miner" -> {
                return 15;
            }
            case "Lava Springs Titanium", "Royal Mines Titanium", "Cliffside Veins Titanium",
                 "Rampart's Quarry Titanium", "Upper Mines Titanium", "Treasure Hoarder Puncher", "Star Sentry Puncher",
                 "Maniac Slayer" -> {
                return 10;
            }
            case "Goblin Slayer" -> {
                return Utils.isInCrystalHollows() ? 13 : 100;
            }
            case "Glacite Walker Slayer", "Mines Slayer" -> {
                return 50;
            }
            case "Goblin Raid Slayer", "Lucky Raffle" -> {
                return 20;
            }
            case "Golden Goblin Slayer", "Boss Corleone Slayer", "Mineshaft Explorer", "Scrap Collector", "Goblin Raid",
                 "Raffle", "Jade Crystal Hunter", "Amber Crystal Hunter", "Topaz Crystal Hunter",
                 "Sapphire Crystal Hunter", "Amethyst Crystal Hunter" -> {
                return 1;
            }
            case "2x Mithril Powder Collector" -> {
                return 500;
            }
            case "Hard Stone Miner", "Jade Gemstone Collector", "Amber Gemstone Collector", "Topaz Gemstone Collector",
                 "Sapphire Gemstone Collector", "Amethyst Gemstone Collector", "Ruby Gemstone Collector" -> {
                return 1000;
            }
            case "Chest Looter", "Corpse Looter" -> {
                return 3;
            }
            case "Team Treasurite Member Slayer", "Yog Slayer", "Automaton Slayer" -> {
                return 13;
            }
            case "Sludge Slayer" -> {
                return 25;
            }
            case "Thyst Slayer" -> {
                return 5;
            }
            case "Glacite Collector", "Umber Collector", "Tungsten Collector", "Citrine Gemstone Collector",
                 "Peridot Gemstone Collector", "Onyx Gemstone Collector", "Aquamarine Gemstone Collector" -> {
                return 1500;
            }
            default -> {
                return null;
            }

        }
    }

}
