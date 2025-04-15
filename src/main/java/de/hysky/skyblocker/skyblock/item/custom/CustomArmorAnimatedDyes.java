package de.hysky.skyblocker.skyblock.item.custom;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.OkLabColor;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.command.argumenttypes.color.ColorArgumentType;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;

import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CustomArmorAnimatedDyes {
	private static final Object2ObjectOpenHashMap<AnimatedDye, AnimatedDyeStateTracker> STATE_TRACKER_MAP = new Object2ObjectOpenHashMap<>();
	private static final float DEFAULT_DELAY = 0;
	private static int frames;

	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register(CustomArmorAnimatedDyes::registerCommands);
		WorldRenderEvents.START.register(ignored -> ++frames);
		// have the animation restart on world change because why not?
		SkyblockEvents.LOCATION_CHANGE.register(ignored -> cleanTrackers());
	}

	private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("custom")
						.then(literal("animatedDye")
								.executes(context -> customizeAnimatedDye(context.getSource(), Integer.MIN_VALUE, Integer.MIN_VALUE, 0, false, 0))
								.then(argument("hex1", ColorArgumentType.hex())
										.then(argument("hex2", ColorArgumentType.hex())
												.then(argument("duration", FloatArgumentType.floatArg(0.01f, 2))
														.then(argument("cycleBack", BoolArgumentType.bool())
																.executes(context -> customizeAnimatedDye(context.getSource(), ColorArgumentType.getIntFromHex(context, "hex1"), ColorArgumentType.getIntFromHex(context, "hex2"), FloatArgumentType.getFloat(context, "duration"), BoolArgumentType.getBool(context, "cycleBack"), DEFAULT_DELAY))
																.then(argument("delay", FloatArgumentType.floatArg(0))
																		.executes(context ->customizeAnimatedDye(context.getSource(), ColorArgumentType.getIntFromHex(context, "hex1"), ColorArgumentType.getIntFromHex(context, "hex2"), FloatArgumentType.getFloat(context, "duration"), BoolArgumentType.getBool(context, "cycleBack"), FloatArgumentType.getFloat(context, "delay")))))))))));
	}

	private static int customizeAnimatedDye(FabricClientCommandSource source, int color1, int color2, float speed, boolean cycleBack, float delay) {
		ItemStack heldItem = source.getPlayer().getMainHandStack();

		if (Utils.isOnSkyblock() && heldItem != null && !heldItem.isEmpty()) {
			if (heldItem.isIn(ItemTags.DYEABLE)) {
				String itemUuid = ItemUtils.getItemUuid(heldItem);

				if (!itemUuid.isEmpty()) {
					Object2ObjectOpenHashMap<String, AnimatedDye> customAnimatedDyes = SkyblockerConfigManager.get().general.customAnimatedDyes;

					if (color1 == Integer.MIN_VALUE && color2 == Integer.MIN_VALUE) {
						if (customAnimatedDyes.containsKey(itemUuid)) {
							customAnimatedDyes.remove(itemUuid);
							SkyblockerConfigManager.save();
							source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.customAnimatedDyes.removed")));
						} else {
							source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customAnimatedDyes.neverHad")));
						}
					} else {
						AnimatedDye animatedDye = new AnimatedDye(List.of(new DyeFrame(color1, 0), new DyeFrame(color2, 1)), cycleBack, delay, speed);

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
		AnimatedDyeStateTracker trackedState = STATE_TRACKER_MAP.computeIfAbsent(animatedDye, CustomArmorAnimatedDyes::createStateTracker);

		if (trackedState.lastRecordedFrame == frames) {
			return trackedState.lastColor;
		}

		trackedState.lastRecordedFrame = frames;

		return animatedDye.interpolate(trackedState, MinecraftClient.getInstance().getRenderTickCounter());
	}

	private static AnimatedDyeStateTracker createStateTracker(AnimatedDye animatedDye) {
		AnimatedDyeStateTracker tracker = new AnimatedDyeStateTracker();
		if (animatedDye.delay() > 0) {
			if (animatedDye.cycleBack()) {
				tracker.onBackCycle = true;
				tracker.progress = Math.clamp(animatedDye.delay() / animatedDye.duration(), 0, 1);
			} else {
				tracker.progress = Math.clamp(1 - animatedDye.delay() / animatedDye.duration(), 0, 1);
			}
		}
		return tracker;
	}

	private static class AnimatedDyeStateTrackerOld {
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

		static AnimatedDyeStateTrackerOld create() {
			return new AnimatedDyeStateTrackerOld();
		}
	}

	private static class AnimatedDyeStateTracker {
		private float progress = 0;
		private boolean onBackCycle = false;
		private int lastColor = 0;
		private int lastRecordedFrame = 0;
	}

	public static void cleanTrackers() {
		STATE_TRACKER_MAP.clear();
	}

	public record DyeFrame(@SerialEntry int color, @SerialEntry float time) {}
	public record AnimatedDye(@SerialEntry List<DyeFrame> frames, @SerialEntry boolean cycleBack, @SerialEntry float delay, @SerialEntry float duration) {

		private int interpolate(AnimatedDyeStateTracker tracker, RenderTickCounter counter) {

			int dyeFrame = 0;
			while (dyeFrame < frames.size() - 1 && frames.get(dyeFrame + 1).time < tracker.progress) dyeFrame++;


			DyeFrame current = tracker.onBackCycle ? frames.get(dyeFrame + 1) : frames.get(dyeFrame);
			DyeFrame next = tracker.onBackCycle ? frames.get(dyeFrame) : frames.get(dyeFrame + 1);

			float progress = (tracker.progress - current.time) / (next.time - current.time);

			tracker.lastColor = OkLabColor.interpolate(current.color, next.color, progress);

			float v = counter.getLastDuration() * 0.05f / duration();
			if (tracker.onBackCycle) {
				tracker.progress -= v;
				if (tracker.progress <= 0f) {
					tracker.onBackCycle = false;
					tracker.progress = Math.abs(tracker.progress);
				}
			} else {
				tracker.progress += v;
				if (tracker.progress >= 1f) {
					if (cycleBack) {
						tracker.onBackCycle = true;
						tracker.progress = 2f - tracker.progress;
					} else {
						tracker.progress %= 1.f;
					}
				}
			}
			return tracker.lastColor;
		}
	}


	public record AnimatedDyeOld(@SerialEntry int color1, @SerialEntry int color2, @SerialEntry int samples, @SerialEntry boolean cycleBack, @SerialEntry int tickDelay) {

		private int interpolate(AnimatedDyeStateTrackerOld stateTracker) {
			if (stateTracker.shouldCycleBack(samples, cycleBack)) stateTracker.onBackCycle = true;

			if (stateTracker.onBackCycle) {
				float percent = (1f / samples) * stateTracker.getAndDecrement();

				//Go back to normal cycle once we've cycled all the way back
				if (stateTracker.sampleCounter == 0) stateTracker.onBackCycle = false;

				int interpolatedColor = OkLabColor.interpolate(color1, color2, percent);
				stateTracker.lastColor = interpolatedColor;

				return interpolatedColor;
			}

			//This will only happen if cycleBack is false
			if (stateTracker.sampleCounter == samples) stateTracker.sampleCounter = 0;

			float percent = (1f / samples) * stateTracker.getAndIncrement();
			int interpolatedColor = OkLabColor.interpolate(color1, color2, percent);

			stateTracker.lastColor = interpolatedColor;

			return interpolatedColor;
		}

	}
}
