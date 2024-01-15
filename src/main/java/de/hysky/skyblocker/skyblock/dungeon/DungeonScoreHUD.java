package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DungeonScoreHUD {

	public static void init() {
		HudRenderCallback.EVENT.register(DungeonScoreHUD::onHudRender);
	}

	private static void onHudRender(DrawContext context, float tickDelta) {
		if (!Utils.isInDungeons() || !DungeonScore.isDungeonStarted()) return;

		int x = SkyblockerConfigManager.get().locations.dungeons.mapX;
		int y = SkyblockerConfigManager.get().locations.dungeons.mapY;
		int size = (int) (128 * SkyblockerConfigManager.get().locations.dungeons.mapScaling);
		context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
				Text.literal("Score: ").append(formatScore(DungeonScore.getScore())),
				x + (size >> 1),
				y + size + 5,
				0x00FFFFFF);
	}

	private static Text formatScore(int score) {
		if (score < 100) return Text.literal(String.valueOf(score)).withColor(0xDC1A1A).append(Text.literal(" (D)").formatted(Formatting.GRAY));
		if (score < 160) return Text.literal(String.valueOf(score)).withColor(0x4141FF).append(Text.literal(" (C)").formatted(Formatting.GRAY));
		if (score < 230) return Text.literal(String.valueOf(score)).withColor(0x7FCC19).append(Text.literal(" (B)").formatted(Formatting.GRAY));
		if (score < 270) return Text.literal(String.valueOf(score)).withColor(0x7F3FB2).append(Text.literal(" (A)").formatted(Formatting.GRAY));
		if (score < 300) return Text.literal(String.valueOf(score)).withColor(0xF1E252).append(Text.literal(" (S)").formatted(Formatting.GRAY));
		return Text.literal(String.valueOf(score)).withColor(0xF1E252).formatted(Formatting.BOLD).append(Text.literal(" (S+)").formatted(Formatting.GRAY));
	}
}
