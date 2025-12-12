package de.hysky.skyblocker.skyblock.item.custom;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.apache.commons.text.WordUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.EnumUtils;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.type.ProfileComponent;

public class CustomAnimatedHelmetTextures {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Object2ObjectOpenHashMap<String, AnimatedHead> ANIMATED_HEADS = new Object2ObjectOpenHashMap<>();
	private static final Object2ObjectOpenHashMap<AnimatedHead, AnimatedHeadStateTracker> STATE_TRACKERS = new Object2ObjectOpenHashMap<>();
	private static int ticks = 0;

	@Init
	public static void init() {
		NEURepoManager.runAsyncAfterLoad(CustomAnimatedHelmetTextures::loadAnimatedHeads);
		ClientTickEvents.END_CLIENT_TICK.register(_client -> ticks++);
	}

	private static void loadAnimatedHeads() {
		try (InputStream stream = NEURepoManager.file("constants/animatedskulls.json").stream()) {
			String data = new String(stream.readAllBytes());
			Map<String, AnimatedHead> animatedHeads = AnimatedHead.MAP_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(data).getAsJsonObject().get("skins")).getOrThrow();

			// Update the map on the main thread to prevent CMEs and proper operation ordering
			CLIENT.execute(() -> {
				ANIMATED_HEADS.clear();
				ANIMATED_HEADS.putAll(animatedHeads);
				STATE_TRACKERS.clear();
			});
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Custom Animated Helmet Textures] Failed to load animated heads file.", e);
		}
	}

	public static Set<String> getAnimatedHeadIds() {
		return ANIMATED_HEADS.keySet();
	}

	/**
	 * Formats the id of an animated head into a user-friendly name. This is also what NEU does as far as I know.
	 */
	public static String formatName(String id) {
		return WordUtils.capitalizeFully(id.replace('_', ' '));
	}

	public static @Nullable ProfileComponent animateHeadTexture(String id) {
		AnimatedHead head = ANIMATED_HEADS.get(id);

		if (head != null) {
			AnimatedHeadStateTracker tracker = STATE_TRACKERS.computeIfAbsent(head, AnimatedHeadStateTracker::new);

			if (tracker.lastRecordedTick == ticks) {
				return tracker.getCurrentFrame();
			}

			tracker.advanceTo(ticks);
			tracker.lastRecordedTick = ticks;

			return tracker.getCurrentFrame();
		}

		return null;
	}

	/**
	 * @param tickThreshold The amount of ticks between each frame.
	 */
	private record AnimatedHead(int tickThreshold, List<ProfileComponent> frames) {
		private static final Codec<ProfileComponent> FRAME_CODEC = Codec.STRING.xmap(AnimatedHead::fromString, AnimatedHead::toString);
		// The ticks value must always be positive since it is used as a divisor and the ticks should not be negative anyways.
		// However, there seems to be an entry that violates this (seems to be a testing thing) so we will just do a max to make it at least 1.
		// In the future this could instead use Codecs#POSITIVE_INT.
		private static final Codec<AnimatedHead> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.xmap(i -> Math.max(i, 1), Function.identity()).fieldOf("ticks").forGetter(AnimatedHead::tickThreshold),
				FRAME_CODEC.listOf().validate(AnimatedHead::validateFrames).fieldOf("textures").forGetter(AnimatedHead::frames)
				).apply(instance, AnimatedHead::new));
		private static final Codec<Map<String, AnimatedHead>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, CODEC);

		/**
		 * Decodes the profile from the texture string. Formatted as {@code <uuid>:<texture>}.
		 */
		private static ProfileComponent fromString(String string) {
			String[] split = string.split(":");
			UUID uuid = UUID.fromString(split[0]);
			PropertyMap propertyMap = ItemUtils.propertyMapWithTexture(split[1]);

			return ProfileComponent.ofStatic(new GameProfile(uuid, "custom", propertyMap));
		}

		/**
		 * Not really necessary but ensures compliance with Codec's contracts.
		 */
		private static String toString(ProfileComponent profile) {
			return profile.getGameProfile().id() + ":" + profile.getGameProfile().properties().get("textures").iterator().next().value();
		}

		/**
		 * Requires that each animated head have at least a single frame to prevent any potential problems with iterating the list
		 * since the animation code expects that.
		 */
		private static DataResult<List<ProfileComponent>> validateFrames(List<ProfileComponent> frames) {
			return !frames.isEmpty() ? DataResult.success(frames) : DataResult.error(() -> "Expected at least a single frame for an animated head but there was none!");
		}
	}

	private static class AnimatedHeadStateTracker {
		private final AnimatedHead head;
		private int lastRecordedTick = 0;
		/**
		 * The current frame/texture to show, this will always be non-null.
		 */
		private ProfileComponent currentFrame;

		private AnimatedHeadStateTracker(AnimatedHead head) {
			this.head = head;
			this.currentFrame = head.frames().getFirst();
		}

		/**
		 * Advances the state to what it would be at the number {@code ticks}.
		 */
		private void advanceTo(int ticks) {
			int advancedIndex = ticks / this.head.tickThreshold();

			// The cycle method conveniently handles looping for us so we don't need to cap the index or anything,
			// it can just infinitely increase.
			this.currentFrame = EnumUtils.cycle(this.head.frames(), advancedIndex);
		}

		private ProfileComponent getCurrentFrame() {
			return Objects.requireNonNull(this.currentFrame);
		}
	}
}
