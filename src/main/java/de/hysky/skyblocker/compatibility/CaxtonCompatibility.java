package de.hysky.skyblocker.compatibility;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;

public final class CaxtonCompatibility {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final boolean CAXTON_ENABLED = FabricLoader.getInstance().isModLoaded("caxton");

	private static @Nullable MethodHandle createHandle() {
		if (!CAXTON_ENABLED) return null;
		MethodHandles.Lookup lookup = MethodHandles.publicLookup();
		Class<?> clazz;
		try {
			clazz = Class.forName("xyz.flirora.caxton.render.Voepfxo");
		} catch (ClassNotFoundException e) {
			LOGGER.error("[Skyblocker Caxton Compat] Could not find xyz.flirora.caxton.render.Voepfxo", e);
			return null;
		}
		try {
			return lookup.findStatic(
					clazz,
					"drawText4Way",
					MethodType.methodType(void.class, DrawContext.class, TextRenderer.class, OrderedText.class, float.class, float.class, int.class, int.class));
		} catch (NoSuchMethodException | IllegalAccessException e) {
			LOGGER.error("[Skyblocker Caxton Compat] Could not find drawText4Way method", e);
			return null;
		}
	}

	private static Optional<RenderPipeline> getCaxtonPipeline(String name) {
		if (!CAXTON_ENABLED) return Optional.empty();
		Class<?> clazz;
		try {
			clazz = Class.forName("xyz.flirora.caxton.render.CaxtonShaders");
		} catch (ClassNotFoundException e) {
			LOGGER.error("[Skyblocker Caxton Compat] Could not find xyz.flirora.caxton.render.CaxtonShaders", e);
			return Optional.empty();
		}
		try {
			return Optional.of((RenderPipeline) clazz.getField(name).get(null));
		} catch (IllegalAccessException | NoSuchFieldException e) {
			LOGGER.error("[Skyblocker Caxton Compat] Could not find {} shader", name, e);
			return Optional.empty();
		}
	}

	private static final MethodHandle HANDLE = createHandle();
	private static boolean errored = false;

	public static boolean drawOutlinedText(DrawContext context, OrderedText text, float x, float y, int color, int outlineColor) {
		if (HANDLE == null || errored) return false;
		try {
			HANDLE.invoke(context, MinecraftClient.getInstance().textRenderer, text, x, y, color, outlineColor);
		} catch (Throwable e) {
			LOGGER.error("[Skyblocker Caxton Compat] Could not invoke drawText4Way", e);
			errored = true;
			return false;
		}
		return true;
	}

	public static Optional<RenderPipeline> getSeeThroughTextPipeline() {
		return getCaxtonPipeline("TEXT_SEE_THROUGH");
	}

	public static Optional<RenderPipeline> getTextPipeline() {
		return getCaxtonPipeline("TEXT");
	}
}
