package de.hysky.skyblocker.skyblock.dungeon;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;

import org.jetbrains.annotations.Nullable;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.HudHelper;

public class DungeonMapConfigScreen extends Screen {

	private int mapX = SkyblockerConfigManager.get().dungeons.dungeonMap.mapX;
	private int mapY = SkyblockerConfigManager.get().dungeons.dungeonMap.mapY;
	private int scoreX = SkyblockerConfigManager.get().dungeons.dungeonScore.scoreX;
	private int scoreY = SkyblockerConfigManager.get().dungeons.dungeonScore.scoreY;
	private static final ResourceLocation EXAMPLE_MAP = SkyblockerMod.id("textures/gui/example_dungeon_map.png");
	private final @Nullable Screen parent;

	protected DungeonMapConfigScreen() {
		this(null);
	}

	public DungeonMapConfigScreen(@Nullable Screen parent) {
		super(Component.literal("Dungeon Map Config"));
		this.parent = parent;
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		renderHUDMap(context, mapX, mapY);
		renderHUDScore(context, scoreX, scoreY);
		context.drawCenteredString(font, "Right Click To Reset Position", width >> 1, height >> 1, Color.GRAY.getRGB());
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
		int mapSize = (int) (128 * SkyblockerConfigManager.get().dungeons.dungeonMap.mapScaling);
		float scoreScaling = SkyblockerConfigManager.get().dungeons.dungeonScore.scoreScaling;
		int scoreWidth = (int) (font.width(DungeonScoreHUD.getFormattedScoreText()) * scoreScaling);
		int scoreHeight = (int) (font.lineHeight * scoreScaling);
		if (HudHelper.pointIsInArea(click.x(), click.y(), mapX, mapY, mapX + mapSize, mapY + mapSize) && click.button() == 0) {
			mapX = (int) Math.max(Math.min(click.x() - (mapSize >> 1), this.width - mapSize), 0);
			mapY = (int) Math.max(Math.min(click.y() - (mapSize >> 1), this.height - mapSize), 0);
		} else if (HudHelper.pointIsInArea(click.x(), click.y(), scoreX, scoreY, scoreX + scoreWidth, scoreY + scoreHeight) && click.button() == 0) {
			scoreX = (int) Math.max(Math.min(click.x() - (scoreWidth >> 1), this.width - scoreWidth), 0);
			scoreY = (int) Math.max(Math.min(click.y() - (scoreHeight >> 1), this.height - scoreHeight), 0);
		}
		return super.mouseDragged(click, offsetX, offsetY);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (click.button() == 1) {
			mapX = 2;
			mapY = 2;
			scoreX = Math.max((int) ((mapX + (64 * SkyblockerConfigManager.get().dungeons.dungeonMap.mapScaling)) - font.width(DungeonScoreHUD.getFormattedScoreText()) * SkyblockerConfigManager.get().dungeons.dungeonScore.scoreScaling / 2), 0);
			scoreY = (int) (mapY + (128 * SkyblockerConfigManager.get().dungeons.dungeonMap.mapScaling) + 4);
		}

		return super.mouseClicked(click, doubled);
	}

	@Override
	public void onClose() {
		SkyblockerConfigManager.update(config -> {
			config.dungeons.dungeonMap.mapX = mapX;
			config.dungeons.dungeonMap.mapY = mapY;
			config.dungeons.dungeonScore.scoreX = scoreX;
			config.dungeons.dungeonScore.scoreY = scoreY;
		});

		this.minecraft.setScreen(parent);
	}

	public void renderHUDMap(GuiGraphics context, int x, int y) {
		float scaling = SkyblockerConfigManager.get().dungeons.dungeonMap.mapScaling;
		int size = (int) (128 * scaling);
		context.blit(RenderPipelines.GUI_TEXTURED, EXAMPLE_MAP, x, y, 0, 0, size, size, size, size);

		if (SkyblockerConfigManager.get().dungeons.dungeonMap.showOutline) HudHelper.drawBorder(context, x, y, size, size, CommonColors.LIGHT_GRAY);
	}

	public void renderHUDScore(GuiGraphics context, int x, int y) {
		DungeonScoreHUD.render(context, x, y);
	}
}
