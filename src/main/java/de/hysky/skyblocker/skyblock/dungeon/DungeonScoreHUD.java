package de.hysky.skyblocker.skyblock.dungeon;

import org.joml.Matrix3x2fStack;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class DungeonScoreHUD {
	private static final Identifier DUNGEON_SCORE = SkyblockerMod.id("dungeon_score");

	private DungeonScoreHUD() {
	}

	@Init
	public static void init() {
		HudElementRegistry.attachElementAfter(VanillaHudElements.OVERLAY_MESSAGE, DUNGEON_SCORE, (context, tickCounter) -> render(context));
	}

	//This is 4+5 wide, needed to offset the extra width from bold numbers (3×1 wide) in S+ and the "+" (6 wide) so that it doesn't go off the screen if the score is S+ and the hud element is at the right edge of the screen
	private static final Component extraSpace = Component.literal(" ").append(Component.literal(" ").withStyle(ChatFormatting.BOLD));

	private static void render(GuiGraphics context) {
		if (Utils.isInDungeons() && DungeonScore.isDungeonStarted() && SkyblockerConfigManager.get().dungeons.dungeonScore.enableScoreHUD) {
			int x = SkyblockerConfigManager.get().dungeons.dungeonScore.scoreX;
			int y = SkyblockerConfigManager.get().dungeons.dungeonScore.scoreY;
			render(context, x, y);
		}
	}

	public static void render(GuiGraphics context, int x, int y) {
		float scale = SkyblockerConfigManager.get().dungeons.dungeonScore.scoreScaling;
		Matrix3x2fStack matrixStack = context.pose();
		matrixStack.pushMatrix();
		matrixStack.scale(scale, scale);
		context.drawString(Minecraft.getInstance().font, getFormattedScoreText(), (int) (x / scale), (int) (y / scale), 0xFFFFFFFF);
		matrixStack.popMatrix();
	}

	public static Component getFormattedScoreText() {
		return Component.translatable("skyblocker.dungeons.dungeonScore.scoreText", formatScore(DungeonScore.getScore()));
	}

	private static Component formatScore(int score) {
		if (score < 100) return Component.literal(String.format("%03d", score)).withColor(0xDC1A1A).append(Component.literal(" (D)").withStyle(ChatFormatting.GRAY)).append(extraSpace);
		if (score < 160) return Component.literal(String.format("%03d", score)).withColor(0x4141FF).append(Component.literal(" (C)").withStyle(ChatFormatting.GRAY)).append(extraSpace);
		if (score < 230) return Component.literal(String.format("%03d", score)).withColor(0x7FCC19).append(Component.literal(" (B)").withStyle(ChatFormatting.GRAY)).append(extraSpace);
		if (score < 270) return Component.literal(String.format("%03d", score)).withColor(0x7F3FB2).append(Component.literal(" (A)").withStyle(ChatFormatting.GRAY)).append(extraSpace);
		if (score < 300) return Component.literal(String.format("%03d", score)).withColor(0xF1E252).append(Component.literal(" (S)").withStyle(ChatFormatting.GRAY)).append(extraSpace);
		return Component.literal("").append(Component.literal(String.format("%03d", score)).withColor(0xF1E252).withStyle(ChatFormatting.BOLD)).append(Component.literal(" (S+)").withStyle(ChatFormatting.GRAY));
	}
}
