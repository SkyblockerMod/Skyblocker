package de.hysky.skyblocker.skyblock.slayers.partycounter;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class PartySlayerCounterInit {
	private static final Logger LOGGER = LogUtils.getLogger();

	private PartySlayerCounterInit() {
	}

	@Init
	public static void init() {
		PartyTracker.init();
		PartySlayerCounter.initialize();
		SlayerKillDetector.init();

		Scheduler.INSTANCE.scheduleCyclic(PartySlayerCounterInit::onTick, 10);
		HudRenderCallback.EVENT.register((graphics, tickCounter) -> PartyCounterWidget.render(graphics));

		ClientCommandRegistrationCallback.EVENT.register(PartySlayerCounterInit::registerCommands);
		LOGGER.info("[Skyblocker] Party Slayer Counter initialized");
	}

	private static void onTick() {
		if (!Utils.isOnSkyblock()) return;
		if (!SkyblockerConfigManager.get().slayers.partySlayerCounter.enablePartyCounter) return;

		PartyTracker.requestPartyInfo();
		SlayerKillDetector.tick();
	}

	private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
		dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("partyslayercounter")
						.executes(PartySlayerCounterInit::showCounts)
						.then(literal("add")
								.then(ClientCommandManager.argument("player", StringArgumentType.word())
										.executes(ctx -> addKills(ctx, 1))
										.then(ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
												.executes(ctx -> addKills(ctx, IntegerArgumentType.getInteger(ctx, "amount"))))))
						.then(literal("remove")
								.then(ClientCommandManager.argument("player", StringArgumentType.word())
										.executes(ctx -> removeKills(ctx, 1))
										.then(ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
												.executes(ctx -> removeKills(ctx, IntegerArgumentType.getInteger(ctx, "amount"))))))
						.then(literal("mode")
								.then(literal("auto").executes(ctx -> setMode(ctx, CounterMode.AUTO)))
								.then(literal("manual").executes(ctx -> setMode(ctx, CounterMode.MANUAL))))
						.then(literal("clear")
								.executes(PartySlayerCounterInit::clearCounts))));
	}

	private static int showCounts(CommandContext<FabricClientCommandSource> ctx) {
		Map<String, Integer> counts = PartySlayerCounter.getKillCounts();
		if (counts.isEmpty()) {
			ctx.getSource().sendFeedback(Constants.PREFIX.get()
					.append(Component.translatable("skyblocker.partySlayerCounter.noCounts").withStyle(ChatFormatting.YELLOW)));
			return 1;
		}

		ctx.getSource().sendFeedback(Constants.PREFIX.get()
				.append(Component.translatable("skyblocker.partySlayerCounter.header").withStyle(ChatFormatting.GOLD)));

		List<Map.Entry<String, Integer>> sorted = new ArrayList<>(counts.entrySet());
		sorted.sort(Comparator.comparingInt(e -> -e.getValue()));
		for (Map.Entry<String, Integer> entry : sorted) {
			ctx.getSource().sendFeedback(Component.literal("  " + entry.getKey() + ": ").withStyle(ChatFormatting.WHITE)
					.append(Component.literal(String.valueOf(entry.getValue())).withStyle(ChatFormatting.GREEN)));
		}

		ctx.getSource().sendFeedback(Component.literal("  Total: ").withStyle(ChatFormatting.YELLOW)
				.append(Component.literal(String.valueOf(PartySlayerCounter.getTotalKills())).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));

		return 1;
	}

	private static int addKills(CommandContext<FabricClientCommandSource> ctx, int amount) {
		String player = StringArgumentType.getString(ctx, "player");
		int newCount = PartySlayerCounter.addKillCount(player, amount);
		ctx.getSource().sendFeedback(Constants.PREFIX.get()
				.append(Component.translatable("skyblocker.partySlayerCounter.added", amount, player, newCount).withStyle(ChatFormatting.GREEN)));
		return 1;
	}

	private static int removeKills(CommandContext<FabricClientCommandSource> ctx, int amount) {
		String player = StringArgumentType.getString(ctx, "player");
		int newCount = PartySlayerCounter.removeKillCount(player, amount);
		ctx.getSource().sendFeedback(Constants.PREFIX.get()
				.append(Component.translatable("skyblocker.partySlayerCounter.removed", amount, player, newCount).withStyle(ChatFormatting.RED)));
		return 1;
	}

	private static int setMode(CommandContext<FabricClientCommandSource> ctx, CounterMode mode) {
		SkyblockerConfigManager.update(config -> config.slayers.partySlayerCounter.counterMode = mode);
		ctx.getSource().sendFeedback(Constants.PREFIX.get()
				.append(Component.translatable("skyblocker.partySlayerCounter.modeSet", mode.name()).withStyle(ChatFormatting.AQUA)));
		return 1;
	}

	private static int clearCounts(CommandContext<FabricClientCommandSource> ctx) {
		PartySlayerCounter.clearCounter();
		ctx.getSource().sendFeedback(Constants.PREFIX.get()
				.append(Component.translatable("skyblocker.partySlayerCounter.cleared").withStyle(ChatFormatting.YELLOW)));
		return 1;
	}
}
