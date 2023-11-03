package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.*;

public class DungeonMapConfigScreen extends Screen {

	private int hudX = SkyblockerConfigManager.get().locations.dungeons.mapX;
	private int hudY = SkyblockerConfigManager.get().locations.dungeons.mapY;
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
		DungeonMap.renderHUDMap(context, hudX, hudY);
		context.drawCenteredTextWithShadow(textRenderer, "Right Click To Reset Position", width >> 1, height >> 1, Color.GRAY.getRGB());
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		float scaling = SkyblockerConfigManager.get().locations.dungeons.mapScaling;
		int size = (int) (128 * scaling);
		if (RenderHelper.pointIsInArea(mouseX, mouseY, hudX, hudY, hudX + size, hudY + size) && button == 0) {
			hudX = (int) Math.max(Math.min(mouseX - (size >> 1), this.width - size), 0);
			hudY = (int) Math.max(Math.min(mouseY - (size >> 1), this.height - size), 0);
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 1) {
			hudX = 2;
			hudY = 2;
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void close() {
		SkyblockerConfigManager.get().locations.dungeons.mapX = hudX;
		SkyblockerConfigManager.get().locations.dungeons.mapY = hudY;
		SkyblockerConfigManager.save();

		this.client.setScreen(parent);
	}
}
