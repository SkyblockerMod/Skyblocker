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
    private static final List<Supplier<Text>> TIPS = List.of(
            () -> Text.translatable("skyblocker.tips.customArmorTrims").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker custom armorTrim")))
    );

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
        if (client.player != null && SkyblockerConfigManager.get().general.enableTips) {
            client.player.sendMessage(nextTip(), false);
        }
    }

    private static int enableTips(CommandContext<FabricClientCommandSource> context) {
        SkyblockerConfigManager.get().general.enableTips = true;
        context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.tips.enabled")).append(" ").append(Text.translatable("skyblocker.tips.clickDisable").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker tips disable")))));
        return Command.SINGLE_SUCCESS;
    }

    private static int disableTips(CommandContext<FabricClientCommandSource> context) {
        SkyblockerConfigManager.get().general.enableTips = false;
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

    private static Text nextTipInternal() {
        return TIPS.get(RANDOM.nextInt(TIPS.size())).get();
    }
}
