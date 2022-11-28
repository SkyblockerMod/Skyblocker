package me.xmrvizzy.skyblocker.skyblock.dwarven;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.hud.Hud;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DwarvenHud {


    public static MinecraftClient client = MinecraftClient.getInstance();
    public static MutableText commissionText = MutableText.of(TextContent.EMPTY);
    public static Identifier hudId = new Identifier("skyblocker", "dwarven_hud");


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
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("skyblocker")
                .then(ClientCommandManager.literal("hud")
                        .then(ClientCommandManager.literal("dwarven")
                                .executes(context -> {
                                    client.send(() -> client.setScreen(new DwarvenHudConfigScreen(Text.of("Dwarven HUD Config"))));
                                    return 1;
                                })))));

        Hud.add(hudId, () ->
                Containers
                        .verticalFlow(Sizing.content(), Sizing.content())
                        .padding(Insets.of(3))
                        .positioning(Positioning.absolute(0,0))
        );

        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
            if (!SkyblockerMod.getInstance().CONFIG.dwarvenMines.dwarvenHud.enabled() || client.player == null || commissionText.equals(Text.of(""))) return;
            var hud = (FlowLayout) Hud.getComponent(hudId);
            if (SkyblockerMod.getInstance().CONFIG.dwarvenMines.dwarvenHud.enableBackground()) {
                hud.surface(Surface.VANILLA_TRANSLUCENT);
            }
            hud.clearChildren();
            hud.positioning(Positioning.absolute(SkyblockerMod.getInstance().CONFIG.dwarvenMines.dwarvenHud.x(), SkyblockerMod.getInstance().CONFIG.dwarvenMines.dwarvenHud.y()));
            hud.child(Components.label(commissionText));
        });
    }

    public static void update() {
        List<Commission> commissionList = new ArrayList<>();
        commissionText = (MutableText) Text.of("");
        if (client.player == null || !SkyblockerMod.getInstance().CONFIG.dwarvenMines.dwarvenHud.enabled()) return;

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

        for (int i = 0; i < commissionList.size(); i++) {
            commissionText.append(commissionList.get(i).commission).append(": ").append(Text.literal(commissionList.get(i).progression).styled(style -> style.withColor(Formatting.GREEN)));
            if (i != (commissionList.size() - 1)) commissionText.append("\n");
        }
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
