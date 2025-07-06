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
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import org.jetbrains.annotations.VisibleForTesting;

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
												.then(argument("duration", FloatArgumentType.floatArg(0.1f, 10f))
														.then(argument("cycleBack", BoolArgumentType.bool())
																.executes(context -> customizeAnimatedDye(context.getSource(), ColorArgumentType.getIntFromHex(context, "hex1"), ColorArgumentType.getIntFromHex(context, "hex2"), FloatArgumentType.getFloat(context, "duration"), BoolArgumentType.getBool(context, "cycleBack"), DEFAULT_DELAY))
																.then(argument("delay", FloatArgumentType.floatArg(0))
																		.executes(context -> customizeAnimatedDye(context.getSource(), ColorArgumentType.getIntFromHex(context, "hex1"), ColorArgumentType.getIntFromHex(context, "hex2"), FloatArgumentType.getFloat(context, "duration"), BoolArgumentType.getBool(context, "cycleBack"), FloatArgumentType.getFloat(context, "delay")))))))))));
	}

	private static int customizeAnimatedDye(FabricClientCommandSource source, int color1, int color2, float duration, boolean cycleBack, float delay) {
		ItemStack heldItem = source.getPlayer().getMainHandStack();

		if (Utils.isOnSkyblock() && heldItem != null && !heldItem.isEmpty()) {
			if (heldItem.isIn(ItemTags.DYEABLE)) {
				String itemUuid = ItemUtils.getItemUuid(heldItem);

				if (!itemUuid.isEmpty()) {
					Object2ObjectOpenHashMap<String, AnimatedDye> customAnimatedDyes = SkyblockerConfigManager.get().general.customAnimatedDyes;

					if (color1 == Integer.MIN_VALUE && color2 == Integer.MIN_VALUE) {
						if (customAnimatedDyes.containsKey(itemUuid)) {
							SkyblockerConfigManager.update(config -> config.general.customAnimatedDyes.remove(itemUuid));
							source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.customAnimatedDyes.removed")));
						} else {
							source.sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.customAnimatedDyes.neverHad")));
						}
					} else {
						AnimatedDye animatedDye = new AnimatedDye(List.of(new Keyframe(color1, 0), new Keyframe(color2, 1)), cycleBack, delay, duration);

						SkyblockerConfigManager.update(config -> config.general.customAnimatedDyes.put(itemUuid, animatedDye));
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
		AnimatedDyeStateTracker trackedState = STATE_TRACKER_MAP.computeIfAbsent(animatedDye, AnimatedDyeStateTracker::new);

		if (trackedState.lastRecordedFrame == frames) {
			return trackedState.lastColor;
		}

		trackedState.lastRecordedFrame = frames;

		return trackedState.interpolate(animatedDye, MinecraftClient.getInstance().getRenderTickCounter().getDynamicDeltaTicks());
	}

	@VisibleForTesting
	static class AnimatedDyeStateTracker {
		private float progress = 0;
		private boolean onBackCycle = false;
		private int lastColor = 0;
		private int lastRecordedFrame = 0;

		@VisibleForTesting
		AnimatedDyeStateTracker(AnimatedDye animatedDye) {
			if (animatedDye.delay() > 0) {
				if (animatedDye.cycleBack()) {
					onBackCycle = true;
					progress = animatedDye.delay() / animatedDye.duration();
				} else {
					progress = 1 - animatedDye.delay() / animatedDye.duration();
				}
				progress = clamp(progress);
			}
		}

		@VisibleForTesting
		int interpolate(AnimatedDye animatedDye, float deltaTicks) {
			update(animatedDye, deltaTicks);

			int keyframe = 0;
			// keyframe cannot be the last keyframe, or else keyframe + 1 will be out of bounds, so we check for less than size - 2
			while (keyframe < animatedDye.keyframes.size() - 2 && animatedDye.keyframes.get(keyframe + 1).time < progress) keyframe++;

			Keyframe current = onBackCycle ? animatedDye.keyframes.get(keyframe + 1) : animatedDye.keyframes.get(keyframe);
			Keyframe next = onBackCycle ? animatedDye.keyframes.get(keyframe) : animatedDye.keyframes.get(keyframe + 1);

			float colorProgress = (progress - current.time) / (next.time - current.time);
			colorProgress = clamp(colorProgress);

			return lastColor = OkLabColor.interpolate(current.color, next.color, colorProgress);
		}

		private void update(AnimatedDye animatedDye, float deltaTicks) {
			float v = deltaTicks * 0.05f / animatedDye.duration;
			if (onBackCycle) {
				progress -= v;
				if (progress <= 0f) {
					onBackCycle = false;
					progress = Math.abs(progress);
				}
			} else {
				progress += v;
				if (progress >= 1f) {
					if (animatedDye.cycleBack) {
						onBackCycle = true;
						progress = 2f - progress;
					} else {
						progress %= 1f;
					}
				}
			}

			// Sanity clamp because I got some pretty weird errors with progress being greater than 1
			progress = clamp(progress);
		}

		private static float clamp(float progress) {
			return Math.clamp(progress, 0, 1);
		}
	}

	public static void cleanTrackers() {
		STATE_TRACKER_MAP.clear();
	}

	public record Keyframe(@SerialEntry int color, @SerialEntry float time) {}
	public record AnimatedDye(@SerialEntry List<Keyframe> keyframes, @SerialEntry boolean cycleBack, @SerialEntry float delay, @SerialEntry float duration) {}
}
