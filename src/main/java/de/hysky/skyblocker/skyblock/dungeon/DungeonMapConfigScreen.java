package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;

public class DungeonMapConfigScreen extends Screen {

	private int mapX = SkyblockerConfigManager.get().locations.dungeons.mapX;
	private int mapY = SkyblockerConfigManager.get().locations.dungeons.mapY;
	private int scoreX = SkyblockerConfigManager.get().locations.dungeons.dungeonScore.scoreX;
	private int scoreY = SkyblockerConfigManager.get().locations.dungeons.dungeonScore.scoreY;
	private static final Identifier MAP_BACKGROUND = new Identifier("textures/map/map_background.png");
	private final Screen parent;

	protected DungeonMapConfigScreen() {
		this(null);
	}

	public DungeonMapConfigScreen(Screen parent) {
		super(Text.literal("Dungeon Map Config"));
		this.parent = parent;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		renderBackground(context, mouseX, mouseY, delta);
		renderHUDMap(context, mapX, mapY);
		renderHUDScore(context, scoreX, scoreY);
		context.drawCenteredTextWithShadow(textRenderer, "Right Click To Reset Position", width >> 1, height >> 1, Color.GRAY.getRGB());
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		int mapSize = (int) (128 * SkyblockerConfigManager.get().locations.dungeons.mapScaling);
		float scoreScaling = SkyblockerConfigManager.get().locations.dungeons.dungeonScore.scoreScaling;
		int scoreWidth = (int) (textRenderer.getWidth("Score: 300 (S+)") * scoreScaling);
		int scoreHeight = (int) (textRenderer.fontHeight * scoreScaling);
		if (RenderHelper.pointIsInArea(mouseX, mouseY, mapX, mapY, mapX + mapSize, mapY + mapSize) && button == 0) {
			mapX = (int) Math.max(Math.min(mouseX - (mapSize >> 1), this.width - mapSize), 0);
			mapY = (int) Math.max(Math.min(mouseY - (mapSize >> 1), this.height - mapSize), 0);
		} else if (RenderHelper.pointIsInArea(mouseX, mouseY, scoreX, scoreY, scoreX + scoreWidth, scoreY + scoreHeight) && button == 0) {
			scoreX = (int) Math.max(Math.min(mouseX - (scoreWidth >> 1), this.width - scoreWidth), 0);
			scoreY = (int) Math.max(Math.min(mouseY - (scoreHeight >> 1), this.height - scoreHeight), 0);
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 1) {
			mapX = 2;
			mapY = 2;
			scoreX = Math.max((int) ((mapX + (64 * SkyblockerConfigManager.get().locations.dungeons.mapScaling)) - textRenderer.getWidth("Score: 300 (S+)") * SkyblockerConfigManager.get().locations.dungeons.dungeonScore.scoreScaling / 2), 0);
			scoreY = (int) (mapY + (128 * SkyblockerConfigManager.get().locations.dungeons.mapScaling) + 4);
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void close() {
		SkyblockerConfigManager.get().locations.dungeons.mapX = mapX;
		SkyblockerConfigManager.get().locations.dungeons.mapY = mapY;
		SkyblockerConfigManager.get().locations.dungeons.dungeonScore.scoreX = scoreX;
		SkyblockerConfigManager.get().locations.dungeons.dungeonScore.scoreY = scoreY;
		SkyblockerConfigManager.save();

		this.client.setScreen(parent);
	}

	public void renderHUDMap(DrawContext context, int x, int y) {
		float scaling = SkyblockerConfigManager.get().locations.dungeons.mapScaling;
		int size = (int) (128 * scaling);
		context.drawTexture(MAP_BACKGROUND, x, y, 0, 0, size, size, size, size);
	}

	public void renderHUDScore(DrawContext context, int x, int y) {
		DungeonScoreHUD.render(context, x, y);
	}
}
