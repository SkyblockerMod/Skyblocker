package de.hysky.skyblocker.skyblock.item;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class CustomArmorAnimatedDyes {
	private static final Object2ObjectOpenHashMap<AnimatedDye, AnimatedDyeStateTracker> STATE_TRACKER_MAP = new Object2ObjectOpenHashMap<>();
	private static final Object2ObjectFunction<AnimatedDye, AnimatedDyeStateTracker> NEW_STATE_TRACKER = _dye -> AnimatedDyeStateTracker.create();
	private static final int DEFAULT_TICK_DELAY = 4;
	private static int ticks;

	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register(CustomArmorAnimatedDyes::registerCommands);
		ClientTickEvents.END_CLIENT_TICK.register(_client -> ++ticks);
	}

	private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("custom")
						.then(literal("animatedDye")
								.executes(context -> customizeAnimatedDye(context.getSource(), null, null, 0, false, 0))
								.then(argument("hex1", word())
										.then(argument("hex2", word())
												.then(argument("samples", IntegerArgumentType.integer(1))
														.then(argument("cycleBack", BoolArgumentType.bool())
																.executes(context -> customizeAnimatedDye(context.getSource(), getString(context, "hex1"), getString(context, "hex2"), IntegerArgumentType.getInteger(context, "samples"), BoolArgumentType.getBool(context, "cycleBack"), DEFAULT_TICK_DELAY))
																.then(argument("tickDelay", IntegerArgumentType.integer(0, 20))
																		.executes(context ->customizeAnimatedDye(context.getSource(), getString(context, "hex1"), getString(context, "hex2"), IntegerArgumentType.getInteger(context, "samples"), BoolArgumentType.getBool(context, "cycleBack"), IntegerArgumentType.getInteger(context, "tickDelay")))))))))));
	}

	private static int customizeAnimatedDye(FabricClientCommandSource source, String hex1, String hex2, int samples, boolean cycleBack, int tickDelay) {
		if (hex1 != null && hex2 != null && (!CustomArmorDyeColors.isHexadecimalColor(hex1) || !CustomArmorDyeColors.isHexadecimalColor(hex2))) {
			source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customAnimatedDyes.invalidHex")));

			return Command.SINGLE_SUCCESS;
		}

		ItemStack heldItem = source.getPlayer().getMainHandStack();

		if (Utils.isOnSkyblock() && heldItem != null && !heldItem.isEmpty()) {
			if (heldItem.isIn(ItemTags.DYEABLE)) {
				String itemUuid = ItemUtils.getItemUuid(heldItem);

				if (!itemUuid.isEmpty()) {
					Object2ObjectOpenHashMap<String, AnimatedDye> customAnimatedDyes = SkyblockerConfigManager.get().general.customAnimatedDyes;

					if (hex1 == null && hex2 == null) {
						if (customAnimatedDyes.containsKey(itemUuid)) {
							customAnimatedDyes.remove(itemUuid);
							SkyblockerConfigManager.save();
							source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.customAnimatedDyes.removed")));
						} else {
							source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customAnimatedDyes.neverHad")));
						}
					} else {
						AnimatedDye animatedDye = new AnimatedDye(Integer.decode("0x" + hex1.replace("#", "")), Integer.decode("0x" + hex2.replace("#", "")), samples, cycleBack, tickDelay);

						customAnimatedDyes.put(itemUuid, animatedDye);
						SkyblockerConfigManager.save();
						source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.customAnimatedDyes.added")));
					}
				} else {
					source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customAnimatedDyes.noItemUuid")));
				}
			} else {
				source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customAnimatedDyes.notDyeable")));
			}
		} else {
			source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customAnimatedDyes.unableToSetDye")));
		}

		return Command.SINGLE_SUCCESS;
	}

	public static int animateColorTransition(AnimatedDye animatedDye) {
		AnimatedDyeStateTracker trackedState = STATE_TRACKER_MAP.computeIfAbsent(animatedDye, NEW_STATE_TRACKER);

		if (trackedState.lastRecordedTick + animatedDye.tickDelay() > ticks) {
			return trackedState.lastColor;
		}

		trackedState.lastRecordedTick = ticks;

		return animatedDye.interpolate(trackedState);
	}

	//Credit to https://codepen.io/OliverBalfour/post/programmatically-making-gradients
	private static int interpolate(int firstColor, int secondColor, double percentage) {
		int r1 = MathHelper.square((firstColor >> 16) & 0xFF);
		int g1 = MathHelper.square((firstColor >> 8) & 0xFF);
		int b1 = MathHelper.square(firstColor & 0xFF);

		int r2 = MathHelper.square((secondColor >> 16) & 0xFF);
		int g2 = MathHelper.square((secondColor >> 8) & 0xFF);
		int b2 = MathHelper.square(secondColor & 0xFF);

		double inverse = 1d - percentage;

		int r3 = (int) Math.floor(Math.sqrt(r1 * inverse + r2 * percentage));
		int g3 = (int) Math.floor(Math.sqrt(g1 * inverse + g2 * percentage));
		int b3 = (int) Math.floor(Math.sqrt(b1 * inverse + b2 * percentage));

		return (r3 << 16) | (g3 << 8 ) | b3;
	}

	private static class AnimatedDyeStateTracker {
		private int sampleCounter;
		private boolean onBackCycle = false;
		private int lastColor = 0;
		private int lastRecordedTick = 0;

		boolean shouldCycleBack(int samples, boolean canCycleBack) {
			return canCycleBack && sampleCounter == samples;
		}
		
		int getAndDecrement() {
			return sampleCounter--;
		}
		
		int getAndIncrement() {
			return sampleCounter++;
		}

		static AnimatedDyeStateTracker create() {
			return new AnimatedDyeStateTracker();
		}
	}

	public record AnimatedDye(@SerialEntry int color1, @SerialEntry int color2, @SerialEntry int samples, @SerialEntry boolean cycleBack, @SerialEntry int tickDelay) {

		private int interpolate(AnimatedDyeStateTracker stateTracker) {
			if (stateTracker.shouldCycleBack(samples, cycleBack)) stateTracker.onBackCycle = true;

			if (stateTracker.onBackCycle) {
				double percent = (1d / (double) samples) * stateTracker.getAndDecrement();

				//Go back to normal cycle once we've cycled all the way back
				if (stateTracker.sampleCounter == 0) stateTracker.onBackCycle = false;

				int interpolatedColor = CustomArmorAnimatedDyes.interpolate(color1, color2, percent);
				stateTracker.lastColor = interpolatedColor;

				return interpolatedColor;
			}

			//This will only happen if cycleBack is false
			if (stateTracker.sampleCounter == samples) stateTracker.sampleCounter = 0;

			double percent = (1d / (double) samples) * stateTracker.getAndIncrement();
			int interpolatedColor = CustomArmorAnimatedDyes.interpolate(color1, color2, percent);

			stateTracker.lastColor = interpolatedColor;

			return interpolatedColor;
		}
	}
}
