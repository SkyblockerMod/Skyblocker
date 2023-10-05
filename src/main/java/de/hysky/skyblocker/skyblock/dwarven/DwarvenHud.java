package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.skyblock.tabhud.widget.hud.HudCommsWidget;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
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

    public static final List<Pattern> COMMISSIONS = Stream.of(
                    "(?:Titanium|Mithril|Hard Stone) Miner",
                    "(?:Ice Walker|Goblin|Goblin Raid|Automaton|Sludge|Team Treasurite Member|Yog|Boss Corleone|Thyst) Slayer",
                    "(?:Lava Springs|Cliffside Veins|Rampart's Quarry|Upper Mines|Royal Mines) Mithril",
                    "(?:Lava Springs|Cliffside Veins|Rampart's Quarry|Upper Mines|Royal Mines) Titanium",
                    "Goblin Raid",
                    "(?:Powder Ghast|Star Sentry) Puncher",
                    "(?<!Lucky )Raffle",
                    "Lucky Raffle",
                    "2x Mithril Powder Collector",
                    "(?:Ruby|Amber|Sapphire|Jade|Amethyst|Topaz) Gemstone Collector",
                    "(?:Amber|Sapphire|Jade|Amethyst|Topaz) Crystal Hunter",
                    "Chest Looter").map(s -> Pattern.compile("^.*(" + s + "): (\\d+\\.?\\d*%|DONE)"))
            .collect(Collectors.toList());

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("skyblocker")
                .then(ClientCommandManager.literal("hud")
                        .then(ClientCommandManager.literal("dwarven")
                                .executes(Scheduler.queueOpenScreenCommand(DwarvenHudConfigScreen::new))))));

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if (!SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.enabled
                    || client.options.playerListKey.isPressed()
                    || client.player == null
                    || commissionList.isEmpty()) {
                return;
            }
            render(HudCommsWidget.INSTANCE, context, SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.x,
                    SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.y, commissionList);
        });
    }

    public static IntIntPair getDimForConfig(List<Commission> commissions) {
        return switch (SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.style) {
            case SIMPLE -> {
                HudCommsWidget.INSTANCE_CFG.updateData(commissions, false);
                yield IntIntPair.of(
                        HudCommsWidget.INSTANCE_CFG.getWidth(),
                        HudCommsWidget.INSTANCE_CFG.getHeight());
            }
            case FANCY -> {
                HudCommsWidget.INSTANCE_CFG.updateData(commissions, true);
                yield IntIntPair.of(
                        HudCommsWidget.INSTANCE_CFG.getWidth(),
                        HudCommsWidget.INSTANCE_CFG.getHeight());
            }
            default -> IntIntPair.of(200, 20 * commissions.size());
        };
    }

    public static void render(HudCommsWidget hcw, DrawContext context, int hudX, int hudY, List<Commission> commissions) {

        switch (SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.style) {
            case SIMPLE -> renderSimple(hcw, context, hudX, hudY, commissions);
            case FANCY -> renderFancy(hcw, context, hudX, hudY, commissions);
            case CLASSIC -> renderClassic(context, hudX, hudY, commissions);
        }
    }

    public static void renderClassic(DrawContext context, int hudX, int hudY, List<Commission> commissions) {
        if (SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.enableBackground) {
            context.fill(hudX, hudY, hudX + 200, hudY + (20 * commissions.size()), 0x64000000);
        }

        int y = 0;
        for (Commission commission : commissions) {
            context
                    .drawTextWithShadow(client.textRenderer,
                            Text.literal(commission.commission + ": ")
                                    .styled(style -> style.withColor(Formatting.AQUA))
                                    .append(Text.literal(commission.progression)
                                            .styled(style -> style.withColor(Formatting.GREEN))),
                            hudX + 5, hudY + y + 5, 0xFFFFFFFF);
            y += 20;
        }
    }

    public static void renderSimple(HudCommsWidget hcw, DrawContext context, int hudX, int hudY, List<Commission> commissions) {
        hcw.updateData(commissions, false);
        hcw.update();
        hcw.setX(hudX);
        hcw.setY(hudY);
        hcw.render(context,
                SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.enableBackground);
    }

    public static void renderFancy(HudCommsWidget hcw, DrawContext context, int hudX, int hudY, List<Commission> commissions) {
        hcw.updateData(commissions, true);
        hcw.update();
        hcw.setX(hudX);
        hcw.setY(hudY);
        hcw.render(context,
                SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.enableBackground);
    }

    public static void update() {
        commissionList = new ArrayList<>();
        if (client.player == null || client.getNetworkHandler() == null || !SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.enabled)
            return;

        client.getNetworkHandler().getPlayerList().forEach(playerListEntry -> {
            if (playerListEntry.getDisplayName() != null) {
                for (Pattern pattern : COMMISSIONS) {
                    Matcher matcher = pattern.matcher(playerListEntry.getDisplayName().getString());
                    if (matcher.find()) {
                        commissionList.add(new Commission(matcher.group(1), matcher.group(2)));
                    }

                }
            }
        });
    }

    // steamroller tactics to get visibility from outside classes (HudCommsWidget)
    public record Commission(String commission, String progression) {
    }
}