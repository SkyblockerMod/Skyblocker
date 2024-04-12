package de.hysky.skyblocker.skyblock;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Constants;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Tips {
    private static final Random RANDOM = new Random();
    private static int previousTipIndex = -1;
    private static final List<Supplier<Text>> TIPS = List.of(
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
            getTipFactory("skyblocker.tips.issues", ClickEvent.Action.OPEN_URL, "https://github.com/SkyblockerMod/Skyblocker"),
            getTipFactory("skyblocker.tips.beta", ClickEvent.Action.OPEN_URL, "https://github.com/SkyblockerMod/Skyblocker/actions"),
            getTipFactory("skyblocker.tips.discord", ClickEvent.Action.OPEN_URL, "https://discord.gg/aNNJHQykck"),
            getTipFactory("skyblocker.tips.flameOverlay", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker config"),
            getTipFactory("skyblocker.tips.wikiLookup", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker config"),
            getTipFactory("skyblocker.tips.protectItem", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker protectItem"),
            getTipFactory("skyblocker.tips.fairySoulsEnigmaSoulsRelics", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker fairySouls"),
            getTipFactory("skyblocker.tips.quickNav", ClickEvent.Action.SUGGEST_COMMAND, "/skyblocker config")
    );

    private static boolean sentTip = false;

    private static Supplier<Text> getTipFactory(String key) {
        return () -> Text.translatable(key);
    }

    private static Supplier<Text> getTipFactory(String key, ClickEvent.Action clickAction, String value) {
        return () -> Text.translatable(key).styled(style -> style.withClickEvent(new ClickEvent(clickAction, value)));
    }

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register(Tips::registerTipsCommand);
        SkyblockEvents.JOIN.register(Tips::sendNextTip);
    }

    private static void registerTipsCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("tips")
                .then(literal("enable").executes(Tips::enableTips))
                .then(literal("disable").executes(Tips::disableTips))
                .then(literal("next").executes(Tips::nextTip))
        ));
    }

    private static void sendNextTip() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && SkyblockerConfigManager.get().general.enableTips && !sentTip) {
            client.player.sendMessage(nextTip(), false);
            sentTip = true;
        }
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

    private static int nextTip(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(nextTip());
        return Command.SINGLE_SUCCESS;
    }

    private static Text nextTip() {
        return Constants.PREFIX.get().append(Text.translatable("skyblocker.tips.tip", nextTipInternal()))
                .append(Text.translatable("skyblocker.tips.clickNextTip").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker tips next"))))
                .append(" ")
                .append(Text.translatable("skyblocker.tips.clickDisable").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker tips disable"))));
    }

    public static Text nextTipInternal() {
        int randomInt = RANDOM.nextInt(TIPS.size());
        while (randomInt == previousTipIndex) randomInt = RANDOM.nextInt(TIPS.size());
        previousTipIndex = randomInt;
        return TIPS.get(randomInt).get();
    }
}
