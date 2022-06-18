package me.xmrvizzy.skyblocker.skyblock.dwarven;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DwarvenHud {


    public static MinecraftClient client = MinecraftClient.getInstance();

    public static final List<Pattern> COMMISSIONS = List.of(
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
            "Chest Looter"
            ).stream().map(s -> Pattern.compile("^.*(" + s + "): (\\d+\\.?\\d*%|DONE)"))
            .collect(Collectors.toList());
    public static void init(){
        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
            if (SkyblockerConfig.get().locations.dwarvenMines.dwarvenHud.enabled) {
                int hudX = SkyblockerConfig.get().locations.dwarvenMines.dwarvenHud.x;
                int hudY = SkyblockerConfig.get().locations.dwarvenMines.dwarvenHud.y;
                List<Commission> commissions = new ArrayList<>();
                client.getNetworkHandler().getPlayerList().forEach(playerListEntry -> {
                    if (playerListEntry.getDisplayName() != null) {
                        for (Pattern pattern : COMMISSIONS) {
                            Matcher matcher = pattern.matcher(playerListEntry.getDisplayName().getString());
                            if (matcher.find()) {
                                commissions.add(new Commission(matcher.group(1), matcher.group(2)));
                            }

                        }
                    }
                });
                if (commissions.size() > 0){
                    if (SkyblockerConfig.get().locations.dwarvenMines.dwarvenHud.enableBackground)
                        DrawableHelper.fill(matrixStack, hudX, hudY, hudX + 200, hudY + (20 * commissions.size()), 0x64000000);
                    int y = 0;
                    for (Commission commission : commissions) {
                        client.textRenderer.drawWithShadow(matrixStack, new LiteralText(commission.commission).styled(style -> style.withColor(Formatting.AQUA)).append(new LiteralText(": " + commission.progression).styled(style -> style.withColor(Formatting.GREEN))), hudX + 5, hudY + y + 5, 0xFFFFFFFF);
                        y += 20;
                    }
                }
            }
        });
    }

    public static class Commission{
        String commission;
        String progression;

        public Commission(String commission, String progression){
            this.commission = commission;
            this.progression = progression;
        }
    }
}
