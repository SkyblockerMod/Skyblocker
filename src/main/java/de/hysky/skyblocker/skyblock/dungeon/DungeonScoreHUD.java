package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DungeonScoreHUD {
	private DungeonScoreHUD() {
	}

	public static void render(DrawContext context) {
		int x = SkyblockerConfigManager.get().locations.dungeons.scoreX;
		int y = SkyblockerConfigManager.get().locations.dungeons.scoreY;
		render(context, x, y);
	}

	public static void render(DrawContext context, int x, int y) {
		float scale = SkyblockerConfigManager.get().locations.dungeons.scoreScaling;
		MatrixStack matrixStack = context.getMatrices();
		matrixStack.push();
		matrixStack.scale(scale, scale, 0);
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal("Score: ").append(formatScore(DungeonScore.getScore())), (int) (x / scale), (int) (y / scale), 0xFFFFFFFF);
		matrixStack.pop();
	}

	private static Text formatScore(int score) {
		if (score < 100) return Text.literal(String.format("%03d", score)).withColor(0xDC1A1A).append(Text.literal(" (D) ").formatted(Formatting.GRAY));
		if (score < 160) return Text.literal(String.format("%03d", score)).withColor(0x4141FF).append(Text.literal(" (C) ").formatted(Formatting.GRAY));
		if (score < 230) return Text.literal(String.format("%03d", score)).withColor(0x7FCC19).append(Text.literal(" (B) ").formatted(Formatting.GRAY));
		if (score < 270) return Text.literal(String.format("%03d", score)).withColor(0x7F3FB2).append(Text.literal(" (A) ").formatted(Formatting.GRAY));
		if (score < 300) return Text.literal(String.format("%03d", score)).withColor(0xF1E252).append(Text.literal(" (S) ").formatted(Formatting.GRAY));
		return Text.literal(String.format("%03d", score)).withColor(0xF1E252).formatted(Formatting.BOLD).append(Text.literal(" (S+)").formatted(Formatting.GRAY));
	}
}
