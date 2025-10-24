package de.hysky.skyblocker.compatibility;

import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import org.joml.Matrix3x2fStack;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ModernUICompatibility {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final boolean MODERNUI_ENABLED = FabricLoader.getInstance().isModLoaded("modernui");

	private static boolean isTextEngineEnabled() {
		if (!MODERNUI_ENABLED) return false;
		MethodHandles.Lookup lookup = MethodHandles.publicLookup();
		Class<?> clazz;
		try {
			clazz = Class.forName("icyllis.modernui.mc.ModernUIMod");
		} catch (ClassNotFoundException e) {
			LOGGER.error("[Skyblocker ModernUI Compat] Could not find icyllis.modernui.mc.ModernUIMod", e);
			return true;
		}
		try {
			MethodHandle methodHandle = lookup.findStatic(
					clazz,
					"isTextEngineEnabled",
					MethodType.methodType(boolean.class));
			return Boolean.TRUE.equals(methodHandle.invoke());
		} catch (NoSuchMethodException | IllegalAccessException e) {
			LOGGER.error("[Skyblocker ModernUI Compat] Could not find isTextEngineEnabled method", e);
			return true;
		} catch (Throwable e) {
			LOGGER.error("[Skyblocker ModernUI Compat] Could not invoke isTextEngineEnabled", e);
			return true;
		}
	}

	// text engine changes require game reboot so it's good enough to check only once
	private static final boolean IS_TEXT_ENGINE_ENABLED = isTextEngineEnabled();

	public static boolean drawOutlinedText(DrawContext context, OrderedText text, OrderedText outlineText, int x, int y, int color, int outlineColor) {
		if (!IS_TEXT_ENGINE_ENABLED) return false;

		final float offset = 0.5f; // default value of ModernTextRenderer.sOutlineOffset

		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		Matrix3x2fStack pose = context.getMatrices();

		// https://github.com/BloCamLimb/ModernUI-MC/blob/3.12.0.4/common/src/main/java/icyllis/modernui/mc/text/mixin/MixinContextualBar.java
		pose.pushMatrix()
				.translate(offset, 0);
		context.drawText(textRenderer, outlineText, x, y, outlineColor, false);
		pose.popMatrix();
		pose.pushMatrix()
				.translate(offset, offset);
		context.drawText(textRenderer, outlineText, x, y, outlineColor, false);
		pose.popMatrix();
		pose.pushMatrix()
				.translate(offset, -offset);
		context.drawText(textRenderer, outlineText, x, y, outlineColor, false);
		pose.popMatrix();
		pose.pushMatrix()
				.translate(-offset, 0);
		context.drawText(textRenderer, outlineText, x, y, outlineColor, false);
		pose.popMatrix();
		pose.pushMatrix()
				.translate(-offset, offset);
		context.drawText(textRenderer, outlineText, x, y, outlineColor, false);
		pose.popMatrix();
		pose.pushMatrix()
				.translate(-offset, -offset);
		context.drawText(textRenderer, outlineText, x, y, outlineColor, false);
		pose.popMatrix();
		pose.pushMatrix()
				.translate(0, offset);
		context.drawText(textRenderer, outlineText, x, y, outlineColor, false);
		pose.popMatrix();
		pose.pushMatrix()
				.translate(0, -offset);
		context.drawText(textRenderer, outlineText, x, y, outlineColor, false);
		pose.popMatrix();

		context.drawText(textRenderer, text, x, y, color, false);
		return true;
	}
}
