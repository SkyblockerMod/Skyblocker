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

public class DwarvenHud {


    public static MinecraftClient client = MinecraftClient.getInstance();

    public static final List<Pattern> COMMISSIONS = List.of(
            Pattern.compile("^.*((?:Titanium|Mithril|Hard Stone) Miner): (\\d+\\.?\\d*%|DONE)"),
            Pattern.compile("^.*((?:Ice Walker|Goblin|Goblin Raid|Automaton|Sludge|Team Treasuite Member|Yog|Boss Corleone|Thyst) Slayer): (\\d+\\.?\\d*%|DONE)"),
            Pattern.compile("^.*((?:Lava Springs|Cliffside Veins|Rampart's Quarry|Upper Mines|Royal Mines) Mithril): (\\d+\\.?\\d*%|DONE)"),
            Pattern.compile("^.*((?:Lava Springs|Cliffside Veins|Rampart's Quarry|Upper Mines|Royal Mines) Titanium): (\\d+\\.?\\d*%|DONE)"),
            Pattern.compile("^.*(Goblin Raid): (\\d+\\.?\\d*%|DONE)"),
            Pattern.compile("^.*((?:Powder Ghast|Star Sentry) Puncher): (\\d+\\.?\\d*%|DONE)"),
            Pattern.compile("^.*(Raffle): (\\d+\\.?\\d*%|DONE)"),
            Pattern.compile("^.*(Lucky Raffle): (\\d+\\.?\\d*%|DONE)"),
            Pattern.compile("^.*(2x Mithril Powder Collector): (\\d+\\.?\\d*%|DONE)"),
            Pattern.compile("^.*((?:Ruby|Amber|Sapphire|Jade|Amethyst|Topaz) Gemstone Collector): (\\d+\\.?\\d*%|DONE)"),
            Pattern.compile("^.*((?:Amber|Sapphire|Jade|Amethyst|Topaz) Crystal Hunter): (\\d+\\.?\\d*%|DONE)"),
            Pattern.compile("^.*(Chest Looter): (\\d+\\.?\\d*%|DONE)")
            );
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
                        DrawableHelper.fill(matrixStack, hudX, hudY, hudX + 150, hudY + 50, 0x64000000);
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
