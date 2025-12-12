package de.hysky.skyblocker.skyblock.dungeon;

import java.awt.Color;

import org.jspecify.annotations.Nullable;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.HudHelper;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DungeonMapConfigScreen extends Screen {

	private int mapX = SkyblockerConfigManager.get().dungeons.dungeonMap.mapX;
	private int mapY = SkyblockerConfigManager.get().dungeons.dungeonMap.mapY;
	private int scoreX = SkyblockerConfigManager.get().dungeons.dungeonScore.scoreX;
	private int scoreY = SkyblockerConfigManager.get().dungeons.dungeonScore.scoreY;
	private static final Identifier MAP_BACKGROUND = Identifier.ofVanilla("textures/map/map_background.png");
	private final @Nullable Screen parent;

	protected DungeonMapConfigScreen() {
		this(null);
	}

	public DungeonMapConfigScreen(@Nullable Screen parent) {
		super(Text.literal("Dungeon Map Config"));
		this.parent = parent;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		renderHUDMap(context, mapX, mapY);
		renderHUDScore(context, scoreX, scoreY);
		context.drawCenteredTextWithShadow(textRenderer, "Right Click To Reset Position", width >> 1, height >> 1, Color.GRAY.getRGB());
	}

	@Override
	public boolean mouseDragged(Click click, double offsetX, double offsetY) {
		int mapSize = (int) (128 * SkyblockerConfigManager.get().dungeons.dungeonMap.mapScaling);
		float scoreScaling = SkyblockerConfigManager.get().dungeons.dungeonScore.scoreScaling;
		int scoreWidth = (int) (textRenderer.getWidth(DungeonScoreHUD.getFormattedScoreText()) * scoreScaling);
		int scoreHeight = (int) (textRenderer.fontHeight * scoreScaling);
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
	public boolean mouseClicked(Click click, boolean doubled) {
		if (click.button() == 1) {
			mapX = 2;
			mapY = 2;
			scoreX = Math.max((int) ((mapX + (64 * SkyblockerConfigManager.get().dungeons.dungeonMap.mapScaling)) - textRenderer.getWidth(DungeonScoreHUD.getFormattedScoreText()) * SkyblockerConfigManager.get().dungeons.dungeonScore.scoreScaling / 2), 0);
			scoreY = (int) (mapY + (128 * SkyblockerConfigManager.get().dungeons.dungeonMap.mapScaling) + 4);
		}

		return super.mouseClicked(click, doubled);
	}

	@Override
	public void close() {
		SkyblockerConfigManager.update(config -> {
			config.dungeons.dungeonMap.mapX = mapX;
			config.dungeons.dungeonMap.mapY = mapY;
			config.dungeons.dungeonScore.scoreX = scoreX;
			config.dungeons.dungeonScore.scoreY = scoreY;
		});

		this.client.setScreen(parent);
	}

	public void renderHUDMap(DrawContext context, int x, int y) {
		float scaling = SkyblockerConfigManager.get().dungeons.dungeonMap.mapScaling;
		int size = (int) (128 * scaling);
		context.drawTexture(RenderPipelines.GUI_TEXTURED, MAP_BACKGROUND, x, y, 0, 0, size, size, size, size);
	}

	public void renderHUDScore(DrawContext context, int x, int y) {
		DungeonScoreHUD.render(context, x, y);
	}
}
