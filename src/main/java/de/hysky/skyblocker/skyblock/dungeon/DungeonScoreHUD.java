package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.HudRenderEvents;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DungeonScoreHUD {
	private DungeonScoreHUD() {
	}

	@Init
	public static void init() {
		HudRenderEvents.AFTER_MAIN_HUD.register((context, tickCounter) -> render(context));
	}

	//This is 4+5 wide, needed to offset the extra width from bold numbers (3×1 wide) in S+ and the "+" (6 wide) so that it doesn't go off the screen if the score is S+ and the hud element is at the right edge of the screen
	private static final Text extraSpace = Text.literal(" ").append(Text.literal(" ").formatted(Formatting.BOLD));

	private static void render(DrawContext context) {
		if (Utils.isInDungeons() && DungeonScore.isDungeonStarted() && SkyblockerConfigManager.get().dungeons.dungeonScore.enableScoreHUD) {
			int x = SkyblockerConfigManager.get().dungeons.dungeonScore.scoreX;
			int y = SkyblockerConfigManager.get().dungeons.dungeonScore.scoreY;
			render(context, x, y);
		}
	}

	public static void render(DrawContext context, int x, int y) {
		float scale = SkyblockerConfigManager.get().dungeons.dungeonScore.scoreScaling;
		MatrixStack matrixStack = context.getMatrices();
		matrixStack.push();
		matrixStack.scale(scale, scale, 0);
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, getFormattedScoreText(), (int) (x / scale), (int) (y / scale), 0xFFFFFFFF);
		matrixStack.pop();
	}

	public static Text getFormattedScoreText() {
		return Text.translatable("skyblocker.dungeons.dungeonScore.scoreText", formatScore(DungeonScore.getScore()));
	}

	private static Text formatScore(int score) {
		if (score < 100) return Text.literal(String.format("%03d", score)).withColor(0xDC1A1A).append(Text.literal(" (D)").formatted(Formatting.GRAY)).append(extraSpace);
		if (score < 160) return Text.literal(String.format("%03d", score)).withColor(0x4141FF).append(Text.literal(" (C)").formatted(Formatting.GRAY)).append(extraSpace);
		if (score < 230) return Text.literal(String.format("%03d", score)).withColor(0x7FCC19).append(Text.literal(" (B)").formatted(Formatting.GRAY)).append(extraSpace);
		if (score < 270) return Text.literal(String.format("%03d", score)).withColor(0x7F3FB2).append(Text.literal(" (A)").formatted(Formatting.GRAY)).append(extraSpace);
		if (score < 300) return Text.literal(String.format("%03d", score)).withColor(0xF1E252).append(Text.literal(" (S)").formatted(Formatting.GRAY)).append(extraSpace);
		return Text.literal("").append(Text.literal(String.format("%03d", score)).withColor(0xF1E252).formatted(Formatting.BOLD)).append(Text.literal(" (S+)").formatted(Formatting.GRAY));
	}
}
