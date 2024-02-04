package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.util.Colors;
import de.hysky.skyblocker.skyblock.tabhud.widget.hud.HudCommsWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.hud.HudPowderWidget;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DwarvenHud {

    public static final MinecraftClient client = MinecraftClient.getInstance();
    public static List<Commission> commissionList = new ArrayList<>();

    public static String mithrilPowder = "0";
    public static String gemStonePowder = "0";

    public static final List<Pattern> COMMISSIONS = Stream.of(
                    "(?:Titanium|Mithril|Hard Stone) Miner",
                    "(?:Ice Walker|Golden Goblin|(?<!Golden )Goblin|Goblin Raid|Automaton|Sludge|Team Treasurite Member|Yog|Boss Corleone|Thyst) Slayer",
                    "(?:Lava Springs|Cliffside Veins|Rampart's Quarry|Upper Mines|Royal Mines) Mithril",
                    "(?:Lava Springs|Cliffside Veins|Rampart's Quarry|Upper Mines|Royal Mines) Titanium",
                    "Goblin Raid",
                    "(?:Powder Ghast|Star Sentry) Puncher",
                    "(?<!Lucky )Raffle",
                    "Lucky Raffle",
                    "2x Mithril Powder Collector",
                    "First Event",
                    "(?:Ruby|Amber|Sapphire|Jade|Amethyst|Topaz) Gemstone Collector",
                    "(?:Amber|Sapphire|Jade|Amethyst|Topaz) Crystal Hunter",
                    "Chest Looter").map(s -> Pattern.compile("^.*(" + s + "): (\\d+\\.?\\d*%|DONE)"))
            .collect(Collectors.toList());
    public static final Pattern MITHRIL_PATTERN = Pattern.compile("Mithril Powder: [0-9,]+");
    public static final Pattern GEMSTONE_PATTERN = Pattern.compile("Gemstone Powder: [0-9,]+");

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("skyblocker")
                .then(ClientCommandManager.literal("hud")
                        .then(ClientCommandManager.literal("dwarven")
                                .executes(Scheduler.queueOpenScreenCommand(DwarvenHudConfigScreen::new))))));

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if ((!SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.enabledCommissions && !SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.enabledPowder)
                    || client.options.playerListKey.isPressed()
                    || client.player == null
                    || (!Utils.isInDwarvenMines() && !Utils.isInCrystalHollows())) {
                return;
            }

            render(HudCommsWidget.INSTANCE, HudPowderWidget.INSTANCE, context,
                    SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.x,
                    SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.y,
                    SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.powderX,
                    SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.powderY,
                    commissionList);
        });
    }

    /**
     * Gets the dimensions (width, height) for the commissions hud and the powder hud
     * @param commissions what commissions to get the dimensions for
     * @return a {@link Pair} of {@link IntIntPair} with the first pair being for the commissions hud and the second pair being for the powder hud
     */
    public static Pair<IntIntPair,IntIntPair> getDimForConfig(List<Commission> commissions) {
        return switch (SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.style) {
            case SIMPLE -> {
                HudCommsWidget.INSTANCE_CFG.updateData(commissions, false);
                yield Pair.of(
                        IntIntPair.of(
                                HudCommsWidget.INSTANCE_CFG.getWidth(),
                                HudCommsWidget.INSTANCE_CFG.getHeight()),
                        IntIntPair.of(
                                HudPowderWidget.INSTANCE_CFG.getWidth(),
                                HudPowderWidget.INSTANCE_CFG.getHeight())
                );
            }
            case FANCY -> {
                HudCommsWidget.INSTANCE_CFG.updateData(commissions, true);
                yield Pair.of(
                        IntIntPair.of(
                                HudCommsWidget.INSTANCE_CFG.getWidth(),
                                HudCommsWidget.INSTANCE_CFG.getHeight()),
                        IntIntPair.of(
                                HudPowderWidget.INSTANCE_CFG.getWidth(),
                                HudPowderWidget.INSTANCE_CFG.getHeight())
                );
            }
            default -> Pair.of(
                    IntIntPair.of(
                            200,
                            20 * commissions.size()),
                    IntIntPair.of(
                            200,
                           40)
            );
        };
    }

    public static void render(HudCommsWidget hcw, HudPowderWidget hpw, DrawContext context, int comHudX, int comHudY, int powderHudX, int powderHudY, List<Commission> commissions) {

        switch (SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.style) {
            case SIMPLE -> renderSimple(hcw,hpw, context, comHudX, comHudY,powderHudX,powderHudY, commissions);
            case FANCY -> renderFancy(hcw,hpw, context, comHudX, comHudY,powderHudX,powderHudY, commissions);
            case CLASSIC -> renderClassic(context, comHudX, comHudY,powderHudX,powderHudY, commissions);
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
    public static void renderClassic(DrawContext context, int comHudX, int comHudY, int powderHudX, int powderHudY, List<Commission> commissions) {
        if (SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.enableBackground) {
            context.fill(comHudX, comHudY, comHudX + 200, comHudY + (20 * commissions.size()), 0x64000000);
            context.fill(powderHudX, powderHudY, powderHudX + 200, powderHudY + 40, 0x64000000);
        }
        if (SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.enabledCommissions) {
            int y = 0;
            for (Commission commission : commissions) {
                float percentage;
                if (!commission.progression().contains("DONE")) {
                    percentage = Float.parseFloat(commission.progression().substring(0, commission.progression().length() - 1));
                } else {
                    percentage = 100f;
                }

                context
                        .drawTextWithShadow(client.textRenderer,
                                Text.literal(commission.commission + ": ").formatted(Formatting.AQUA)
                                        .append(Text.literal(commission.progression).formatted(Colors.hypixelProgressColor(percentage))),
                                comHudX + 5, comHudY + y + 5, 0xFFFFFFFF);
                y += 20;
            }
        }
        if(SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.enabledPowder) {
            //render mithril powder then gemstone
            context
                    .drawTextWithShadow(client.textRenderer,
                            Text.literal("Mithril: " + mithrilPowder).formatted(Formatting.AQUA),
                            powderHudX + 5, powderHudY + 5, 0xFFFFFFFF);
            context
                    .drawTextWithShadow(client.textRenderer,
                            Text.literal("Gemstone: " + gemStonePowder).formatted(Formatting.DARK_PURPLE),
                            powderHudX + 5, powderHudY + 25, 0xFFFFFFFF);
        }
    }

    public static void renderSimple(HudCommsWidget hcw, HudPowderWidget hpw, DrawContext context, int comHudX, int comHudY, int powderHudX, int powderHudY, List<Commission> commissions) {
        if (SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.enabledCommissions) {
            hcw.updateData(commissions, false);
            hcw.update();
            hcw.setX(comHudX);
            hcw.setY(comHudY);
            hcw.render(context,
                    SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.enableBackground);
        }
        if (SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.enabledPowder) {
            hpw.update();
            hpw.setX(powderHudX);
            hpw.setY(powderHudY);
            hpw.render(context,
                    SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.enableBackground);
        }
    }

    public static void renderFancy(HudCommsWidget hcw, HudPowderWidget hpw, DrawContext context, int comHudX, int comHudY, int powderHudX, int powderHudY, List<Commission> commissions) {
        if (SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.enabledCommissions) {
            hcw.updateData(commissions, true);
            hcw.update();
            hcw.setX(comHudX);
            hcw.setY(comHudY);
            hcw.render(context,
                    SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.enableBackground);
        }
        if (SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.enabledPowder) {
            hpw.update();
            hpw.setX(powderHudX);
            hpw.setY(powderHudY);
            hpw.render(context,
                    SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.enableBackground);
        }
    }

    public static void update() {
        if (client.player == null || client.getNetworkHandler() == null || !SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.enabledCommissions || (!Utils.isInCrystalHollows()
                && !Utils.isInDwarvenMines()))
            return;

        commissionList = new ArrayList<>();

        client.getNetworkHandler().getPlayerList().forEach(playerListEntry -> {
            if (playerListEntry.getDisplayName() != null) {
                //find commissions
                for (Pattern pattern : COMMISSIONS) {
                    Matcher matcher = pattern.matcher(playerListEntry.getDisplayName().getString());
                    if (matcher.find()) {
                        commissionList.add(new Commission(matcher.group(1), matcher.group(2)));
                    }

                }
                //find powder
                Matcher mithrilMatcher = MITHRIL_PATTERN.matcher(playerListEntry.getDisplayName().getString());
                if (mithrilMatcher.find()){
                    mithrilPowder = mithrilMatcher.group(0).split(": ")[1];
                }
                Matcher gemstoneMatcher = GEMSTONE_PATTERN.matcher(playerListEntry.getDisplayName().getString());
                if (gemstoneMatcher.find()){
                    gemStonePowder = gemstoneMatcher.group(0).split(": ")[1];
                }
            }
        });
    }

    // steamroller tactics to get visibility from outside classes (HudCommsWidget)
    public record Commission(String commission, String progression) {
    }
}