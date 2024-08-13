package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.MiningConfig;
import de.hysky.skyblocker.events.HudRenderEvents;
import de.hysky.skyblocker.mixins.accessors.PlayerListHudAccessor;
import de.hysky.skyblocker.skyblock.tabhud.util.Colors;
import de.hysky.skyblocker.skyblock.tabhud.widget.hud.HudCommsWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.hud.HudPowderWidget;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DwarvenHud {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static List<Commission> commissionList = new ArrayList<>();

    public static String mithrilPowder = "0";
    public static String gemStonePowder = "0";
    public static String glacitePowder = "0";

    private static final List<Pattern> COMMISSIONS = Stream.of(
            "(?:Titanium|Mithril|Hard Stone) Miner",
            "(?:Glacite Walker|Golden Goblin|(?<!Golden )Goblin|Goblin Raid|Treasure Hoarder|Automaton|Sludge|Team Treasurite Member|Yog|Boss Corleone|Thyst|Maniac|Mines) Slayer",
            "(?:Lava Springs|Cliffside Veins|Rampart's Quarry|Upper Mines|Royal Mines) Mithril",
            "(?:Lava Springs|Cliffside Veins|Rampart's Quarry|Upper Mines|Royal Mines) Titanium",
            "Goblin Raid",
            "(?:Star Sentry|Treasure Hoarder) Puncher",
            "(?<!Lucky )Raffle",
            "Lucky Raffle",
            "2x Mithril Powder Collector",
            "First Event",
            "(?:Ruby|Amber|Sapphire|Jade|Amethyst|Topaz|Onyx|Aquamarine|Citrine|Peridot) Gemstone Collector",
            "(?:Amber|Sapphire|Jade|Amethyst|Topaz) Crystal Hunter",
            "(?:Umber|Tungsten|Glacite|Scrap) Collector",
            "Mineshaft Explorer",
            "(?:Chest|Corpse) Looter").map(s -> Pattern.compile("(" + s + "): (\\d+\\.?\\d*%|DONE)")
    ).toList();
    private static final Pattern MITHRIL_PATTERN = Pattern.compile("Mithril: [0-9,]+");
    private static final Pattern GEMSTONE_PATTERN = Pattern.compile("Gemstone: [0-9,]+");
    private static final Pattern GLACITE_PATTERN = Pattern.compile("Glacite: [0-9,]+");

    @Init
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("skyblocker")
                .then(ClientCommandManager.literal("hud")
                        .then(ClientCommandManager.literal("dwarven")
                                .executes(Scheduler.queueOpenScreenCommand(DwarvenHudConfigScreen::new))
                        )
                )
        ));

        HudRenderEvents.AFTER_MAIN_HUD.register((context, tickCounter) -> {
            if (!SkyblockerConfigManager.get().mining.dwarvenHud.enabledCommissions && !SkyblockerConfigManager.get().mining.dwarvenHud.enabledPowder
                    || CLIENT.options.playerListKey.isPressed()
                    || CLIENT.player == null
                    || (!Utils.isInDwarvenMines() && !Utils.isInCrystalHollows())) {
                return;
            }

            render(HudCommsWidget.INSTANCE, HudPowderWidget.INSTANCE, context,
                    SkyblockerConfigManager.get().mining.dwarvenHud.commissionsX,
                    SkyblockerConfigManager.get().mining.dwarvenHud.commissionsY,
                    SkyblockerConfigManager.get().mining.dwarvenHud.powderX,
                    SkyblockerConfigManager.get().mining.dwarvenHud.powderY,
                    commissionList);
        });
    }

    protected static void render(HudCommsWidget hcw, HudPowderWidget hpw, DrawContext context, int comHudX, int comHudY, int powderHudX, int powderHudY, List<Commission> commissions) {
        switch (SkyblockerConfigManager.get().mining.dwarvenHud.style) {
            case SIMPLE -> renderSimple(hcw, hpw, context, comHudX, comHudY, powderHudX, powderHudY, commissions);
            case FANCY -> renderFancy(hcw, hpw, context, comHudX, comHudY, powderHudX, powderHudY, commissions);
            case CLASSIC -> renderClassic(context, comHudX, comHudY, powderHudX, powderHudY, commissions);
        }
    }

    /**
     * Renders hud to window without using the widget rendering
     * @param context DrawContext to draw the hud to
     * @param comHudX X coordinate of the commissions hud
     * @param comHudY Y coordinate of the commissions hud
     * @param powderHudX X coordinate of the powder hud
     * @param powderHudY Y coordinate of the powder hud
     * @param commissions the commissions to render to the commissions hud
     */
    @Deprecated(since = "1.20.3+1.20.6", forRemoval = true)
    private static void renderClassic(DrawContext context, int comHudX, int comHudY, int powderHudX, int powderHudY, List<Commission> commissions) {
        if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.enableHudBackground) {
            context.fill(comHudX, comHudY, comHudX + 200, comHudY + (20 * commissions.size()), 0x64000000);
            context.fill(powderHudX, powderHudY, powderHudX + 200, powderHudY + 40, 0x64000000);
        }
        if (SkyblockerConfigManager.get().mining.dwarvenHud.enabledCommissions) {
            int y = 0;
            for (Commission commission : commissions) {
                float percentage;
                if (!commission.progression().contains("DONE")) {
                    percentage = Float.parseFloat(commission.progression().substring(0, commission.progression().length() - 1));
                } else {
                    percentage = 100f;
                }

                context.drawTextWithShadow(CLIENT.textRenderer,
                        Text.literal(commission.commission + ": ").formatted(Formatting.AQUA).append(
                                Text.literal(commission.progression).withColor(Colors.pcntToCol(percentage))
                        ),
                        comHudX + 5, comHudY + y + 5, 0xFFFFFFFF);
                y += 20;
            }
        }
        if (SkyblockerConfigManager.get().mining.dwarvenHud.enabledPowder) {
            //render mithril powder then gemstone
            context.drawTextWithShadow(CLIENT.textRenderer,
                    Text.literal("Mithril: " + mithrilPowder).formatted(Formatting.AQUA),
                    powderHudX + 5, powderHudY + 5, 0xFFFFFFFF);
            context.drawTextWithShadow(CLIENT.textRenderer,
                    Text.literal("Gemstone: " + gemStonePowder).formatted(Formatting.DARK_PURPLE),
                    powderHudX + 5, powderHudY + 25, 0xFFFFFFFF);
        }
    }

    private static void renderSimple(HudCommsWidget hcw, HudPowderWidget hpw, DrawContext context, int comHudX, int comHudY, int powderHudX, int powderHudY, List<Commission> commissions) {
        if (SkyblockerConfigManager.get().mining.dwarvenHud.enabledCommissions) {
            hcw.updateData(commissions, false);
            hcw.update();
            hcw.setX(comHudX);
            hcw.setY(comHudY);
            hcw.render(context, SkyblockerConfigManager.get().uiAndVisuals.tabHud.enableHudBackground);
        }
        if (SkyblockerConfigManager.get().mining.dwarvenHud.enabledPowder) {
            hpw.update();
            hpw.setX(powderHudX);
            hpw.setY(powderHudY);
            hpw.render(context, SkyblockerConfigManager.get().uiAndVisuals.tabHud.enableHudBackground);
        }
    }

    private static void renderFancy(HudCommsWidget hcw, HudPowderWidget hpw, DrawContext context, int comHudX, int comHudY, int powderHudX, int powderHudY, List<Commission> commissions) {
        if (SkyblockerConfigManager.get().mining.dwarvenHud.enabledCommissions) {
            hcw.updateData(commissions, true);
            hcw.update();
            hcw.setX(comHudX);
            hcw.setY(comHudY);
            hcw.render(context, SkyblockerConfigManager.get().uiAndVisuals.tabHud.enableHudBackground);
        }
        if (SkyblockerConfigManager.get().mining.dwarvenHud.enabledPowder) {
            hpw.update();
            hpw.setX(powderHudX);
            hpw.setY(powderHudY);
            hpw.render(context, SkyblockerConfigManager.get().uiAndVisuals.tabHud.enableHudBackground);
        }
    }

    public static void update() {
        if (CLIENT.player == null || CLIENT.getNetworkHandler() == null
                || !SkyblockerConfigManager.get().mining.dwarvenHud.enabledCommissions
                && !SkyblockerConfigManager.get().mining.dwarvenHud.enabledPowder
                && SkyblockerConfigManager.get().mining.commissionWaypoints.mode == MiningConfig.CommissionWaypointMode.OFF
                || !Utils.isInCrystalHollows() && !Utils.isInDwarvenMines()) {
            return;
        }
        List<String> oldCommissionNames = commissionList.stream().map(Commission::commission).toList();
        boolean oldCompleted = commissionList.stream().anyMatch(commission -> commission.progression.equals("DONE"));
        commissionList = new ArrayList<>();
        for (PlayerListEntry playerListEntry : CLIENT.getNetworkHandler().getPlayerList().stream().sorted(PlayerListHudAccessor.getOrdering()).toList()) {
            if (playerListEntry.getDisplayName() == null) {
                continue;
            }
            //find commissions
            String name = playerListEntry.getDisplayName().getString().strip();
            for (Pattern pattern : COMMISSIONS) {
                Matcher matcher = pattern.matcher(name);
                if (matcher.matches()) {
                    commissionList.add(new Commission(matcher.group(1), matcher.group(2)));
                }
            }
            //find powder
            Matcher mithrilMatcher = MITHRIL_PATTERN.matcher(name);
            if (mithrilMatcher.matches()) {
                mithrilPowder = mithrilMatcher.group(0).split(": ")[1];
            }
            Matcher gemstoneMatcher = GEMSTONE_PATTERN.matcher(name);
            if (gemstoneMatcher.matches()) {
                gemStonePowder = gemstoneMatcher.group(0).split(": ")[1];
            }
            Matcher glaciteMatcher = GLACITE_PATTERN.matcher(name);
            if (glaciteMatcher.matches()) {
                glacitePowder = glaciteMatcher.group(0).split(": ")[1];
            }
        }
        List<String> newCommissionNames = commissionList.stream().map(Commission::commission).toList();
        boolean newCompleted = commissionList.stream().anyMatch(commission -> commission.progression.equals("DONE"));
        if (!oldCommissionNames.equals(newCommissionNames) || oldCompleted != newCompleted) {
            CommissionLabels.update(newCommissionNames, newCompleted);
        }
    }

    // steamroller tactics to get visibility from outside classes (HudCommsWidget)
    public record Commission(String commission, String progression) {
    }
}