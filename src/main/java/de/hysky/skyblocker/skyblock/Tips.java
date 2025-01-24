package de.hysky.skyblocker.skyblock;

import com.demonwav.mcdev.annotations.Translatable;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Constants;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Tips {
    private static int currentTipIndex = 0;
    private static final List<Supplier<Text>> TIPS = new ArrayList<>(List.of(
            getTipFactory("skyblocker.tips.customItemNames", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker custom renameItem"),
            getTipFactory("skyblocker.tips.customArmorDyeColors", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker custom dyeColor"),
            getTipFactory("skyblocker.tips.customArmorTrims", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker custom armorTrim"),
            getTipFactory("skyblocker.tips.customAnimatedDyes", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker custom animatedDye"),
            getTipFactory("skyblocker.tips.fancyTabExtraInfo"),
            getTipFactory("skyblocker.tips.helpCommand", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker help"),
            getTipFactory("skyblocker.tips.discordRichPresence", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker config"),
            getTipFactory("skyblocker.tips.customDungeonSecretWaypoints", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker dungeons secrets addWaypoint"),
            getTipFactory("skyblocker.tips.shortcuts", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker shortcuts"),
            getTipFactory("skyblocker.tips.gallery", ClickEvent.Action.OPEN_URL, "https://hysky.de/skyblocker/gallery"),
            getTipFactory("skyblocker.tips.itemRarityBackground", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker config"),
            getTipFactory("skyblocker.tips.modMenuUpdate"),
            getTipFactory("skyblocker.tips.issues", ClickEvent.Action.OPEN_URL, "https://github.com/SkyblockerMod/Skyblocker/issues"),
            getTipFactory("skyblocker.tips.beta", ClickEvent.Action.OPEN_URL, "https://github.com/SkyblockerMod/Skyblocker/actions"),
            getTipFactory("skyblocker.tips.contribute", ClickEvent.Action.OPEN_URL, "https://github.com/SkyblockerMod/Skyblocker/wiki/contribute"),
            getTipFactory("skyblocker.tips.discord", ClickEvent.Action.OPEN_URL, "https://discord.gg/aNNJHQykck"),
            getTipFactory("skyblocker.tips.flameOverlay", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker config"),
            getTipFactory("skyblocker.tips.wikiLookup", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker config"),
            getTipFactory("skyblocker.tips.protectItem", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker protectItem"),
            getTipFactory("skyblocker.tips.fairySoulsEnigmaSoulsRelics", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker fairySouls"),
            getTipFactory("skyblocker.tips.quickNav", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker config"),
            getTipFactory("skyblocker.tips.waypoints", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker waypoint"),
            getTipFactory("skyblocker.tips.orderedWaypoints", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker waypoint ordered"),
            getTipFactory("skyblocker.tips.visitorHelper"),
            getTipFactory("skyblocker.tips.slotText"),
            getTipFactory("skyblocker.tips.profileViewer", ClickEvent.Action.SUGGEST_COMMAND, "/pv"),
            getTipFactory("skyblocker.tips.configSearch", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker config"),
            getTipFactory("skyblocker.tips.compactDamage", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker config"),
            getTipFactory("skyblocker.tips.skyblockerScreen", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker"),
            getTipFactory("skyblocker.tips.tipsClick", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker tips next"),
            getTipFactory("skyblocker.tips.eventNotifications", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker config"),
            getTipFactory("skyblocker.tips.signCalculator"),
            getTipFactory("skyblocker.tips.calculateCommand", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker calculate"),
            getTipFactory("skyblocker.tips.fancierBars", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker bars"),
            getTipFactory("skyblocker.tips.crystalWaypointsShare", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker crystalWaypoints share"),
            getTipFactory("skyblocker.tips.gardenMouseLock", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker config"),
            getTipFactory("skyblocker.tips.newYearCakesHelper"),
            getTipFactory("skyblocker.tips.accessoryHelper"),
            getTipFactory("skyblocker.tips.fancyAuctionHouseCheapHighlight"),
			getTipFactory("skyblocker.tips.viewRecipe")
    ));

    private static boolean sentTip = false;

    private static Supplier<Text> getTipFactory(@Translatable String key) {
        return () -> Text.translatable(key);
    }

    private static Supplier<Text> getTipFactory(@Translatable String key, ClickEvent.Action clickAction, String value) {
        return () -> Text.translatable(key).styled(style -> style.withClickEvent(new ClickEvent(clickAction, value)));
    }

    @Init
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register(Tips::registerTipsCommand);
        SkyblockEvents.JOIN.register(Tips::sendNextTip);
        Collections.shuffle(TIPS);
    }

    private static void registerTipsCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("tips")
                .then(literal("enable").executes(Tips::enableTips))
                .then(literal("disable").executes(Tips::disableTips))
                .then(literal("previous").executes(Tips::sendPreviousTipCommand))
                .then(literal("next").executes(Tips::sendNextTipCommand))
        ));
    }

    private static int enableTips(CommandContext<FabricClientCommandSource> context) {
        SkyblockerConfigManager.get().general.enableTips = true;
        SkyblockerConfigManager.save();
        context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.tips.enabled")).append(" ").append(Text.translatable("skyblocker.tips.clickDisable").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker tips disable")))));
        return Command.SINGLE_SUCCESS;
    }

    private static int disableTips(CommandContext<FabricClientCommandSource> context) {
        SkyblockerConfigManager.get().general.enableTips = false;
        SkyblockerConfigManager.save();
        context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.tips.disabled")).append(" ").append(Text.translatable("skyblocker.tips.clickEnable").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker tips enable")))));
        return Command.SINGLE_SUCCESS;
    }

    private static void sendNextTip() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && SkyblockerConfigManager.get().general.enableTips && !sentTip) {
            client.player.sendMessage(tipMessage(nextTip()), false);
            sentTip = true;
        }
    }

    private static int sendNextTipCommand(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(tipMessage(nextTip()));
        return Command.SINGLE_SUCCESS;
    }

    public static Text nextTip() {
        return Text.translatable("skyblocker.tips.tip", nextTipInternal());
    }

    private static Text nextTipInternal() {
        currentTipIndex++;
        currentTipIndex %= TIPS.size();
        return TIPS.get(currentTipIndex).get();
    }

    private static int sendPreviousTipCommand(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(tipMessage(previousTip()));
        return Command.SINGLE_SUCCESS;
    }

    public static Text previousTip() {
        return Text.translatable("skyblocker.tips.tip", previousTipInternal());
    }

    private static Text previousTipInternal() {
        currentTipIndex--;
        currentTipIndex += TIPS.size();
        currentTipIndex %= TIPS.size();
        return TIPS.get(currentTipIndex).get();
    }

    private static Text tipMessage(Text tip) {
        return Constants.PREFIX.get().append(tip)
                .append(" ")
                .append(Text.translatable("skyblocker.tips.clickPreviousTip").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker tips previous"))))
                .append(" ")
                .append(Text.translatable("skyblocker.tips.clickNextTip").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker tips next"))))
                .append(" ")
                .append(Text.translatable("skyblocker.tips.clickDisable").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker tips disable"))));
    }
}
