package de.hysky.skyblocker.compatibility;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import com.mojang.logging.LogUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.FormattedCharSequence;

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
					MethodType.methodType(void.class, GuiGraphicsExtractor.class, Font.class, FormattedCharSequence.class, float.class, float.class, int.class, int.class));
		} catch (NoSuchMethodException | IllegalAccessException e) {
			LOGGER.error("[Skyblocker Caxton Compat] Could not find drawText4Way method", e);
			return null;
		}
	}

	private static final @Nullable MethodHandle HANDLE = createHandle();
	private static boolean errored = false;

	public static boolean drawOutlinedText(GuiGraphicsExtractor graphics, FormattedCharSequence text, float x, float y, int color, int outlineColor) {
		if (HANDLE == null || errored) return false;
		try {
			HANDLE.invoke(graphics, Minecraft.getInstance().font, text, x, y, color, outlineColor);
		} catch (Throwable e) {
			LOGGER.error("[Skyblocker Caxton Compat] Could not invoke drawText4Way", e);
			errored = true;
			return false;
		}
		return true;
	}
}
