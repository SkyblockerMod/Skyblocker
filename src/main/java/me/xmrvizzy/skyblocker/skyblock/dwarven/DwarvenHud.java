package me.xmrvizzy.skyblocker.skyblock.dwarven;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DwarvenHud {


    public static MinecraftClient client = MinecraftClient.getInstance();
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
            "Chest Looter"
            ).map(s -> Pattern.compile("^.*(" + s + "): (\\d+\\.?\\d*%|DONE)"))
            .collect(Collectors.toList());
    public static void init(){
        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
            if (!SkyblockerConfig.get().locations.dwarvenMines.dwarvenHud.enabled || client.player == null || commissionList.isEmpty()) return;
            render(matrixStack, SkyblockerConfig.get().locations.dwarvenMines.dwarvenHud.x, SkyblockerConfig.get().locations.dwarvenMines.dwarvenHud.y, commissionList);
        });
    }

    public static void render(MatrixStack matrixStack, int hudX, int hudY, List<Commission> commissions) {
        if (commissions.size() > 0){
            if (SkyblockerConfig.get().locations.dwarvenMines.dwarvenHud.enableBackground)
                DrawableHelper.fill(matrixStack, hudX, hudY, hudX + 200, hudY + (20 * commissions.size()), 0x64000000);
            int y = 0;
            for (Commission commission : commissions) {
                client.textRenderer.drawWithShadow(matrixStack, Text.literal(commission.commission).styled(style -> style.withColor(Formatting.AQUA)).append(Text.literal(": " + commission.progression).styled(style -> style.withColor(Formatting.GREEN))), hudX + 5, hudY + y + 5, 0xFFFFFFFF);
                y += 20;
            }
        }
    }

    public static void update() {
        commissionList = new ArrayList<>();
        if (client.player == null || !SkyblockerConfig.get().locations.dwarvenMines.dwarvenHud.enabled) return;

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

    public static class Commission{
        String commission;
        String progression;

        public Commission(String commission, String progression){
            this.commission = commission;
            this.progression = progression;
        }
    }
}
