package de.hysky.skyblocker.skyblock.item.custom;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.CodecUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import io.github.moulberry.repo.NEURepoFile;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.ExtraCodecs;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class RepoDyeColors {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final Map<String, Integer> STATIC_DYES = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
	public static final Map<String, List<Integer>> ANIMATED_DYES = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());

	@Init
	public static void init() {
		NEURepoManager.runAsyncAfterLoad(RepoDyeColors::loadDyes);
	}

	public static void loadDyes() {
		NEURepoManager.runAsyncAfterLoad(() -> {
			STATIC_DYES.clear();
			ANIMATED_DYES.clear();

			NEURepoFile file = NEURepoManager.file("constants/dyes.json");
			if (file == null) return;
			try (InputStream stream = file.stream()) {
				Dyes dyes = Dyes.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(new String(stream.readAllBytes()))).getOrThrow();
				STATIC_DYES.putAll(dyes.staticDyes);
				ANIMATED_DYES.putAll(dyes.animatedDyes);
				LOGGER.info("[Skyblocker] Successfully loaded {} static dyes and {} animated dyes from repo.", STATIC_DYES.size(), ANIMATED_DYES.size());
			} catch (Exception ex) {
				LOGGER.info("[Skyblocker] Failed to load dyes from repo", ex);
			}
		});
	}

	private record Dyes(Object2ObjectMap<String, List<Integer>> animatedDyes, Map<String, Integer> staticDyes) {
		private static final Codec<Dyes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				CodecUtils.object2ObjectMapCodec(Codec.STRING, ExtraCodecs.STRING_RGB_COLOR.listOf()).fieldOf("animated").forGetter(Dyes::animatedDyes),
				Codec.unboundedMap(Codec.STRING, ExtraCodecs.STRING_RGB_COLOR).fieldOf("static").forGetter(Dyes::staticDyes)
		).apply(instance, Dyes::new));
	}
}
