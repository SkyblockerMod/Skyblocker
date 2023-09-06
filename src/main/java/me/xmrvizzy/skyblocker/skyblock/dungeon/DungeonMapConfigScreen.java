package me.xmrvizzy.skyblocker.skyblock.dungeon;

import me.shedaniel.autoconfig.AutoConfig;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.render.RenderHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.*;

public class DungeonMapConfigScreen extends Screen {

	private int hudX = SkyblockerConfig.get().locations.dungeons.mapX;
	private int hudY = SkyblockerConfig.get().locations.dungeons.mapY;

	protected DungeonMapConfigScreen() {
		super(Text.literal("Dungeon Map Config"));
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
		float scaling = SkyblockerConfig.get().locations.dungeons.mapScaling;
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
		SkyblockerConfig.get().locations.dungeons.mapX = hudX;
		SkyblockerConfig.get().locations.dungeons.mapY = hudY;
		AutoConfig.getConfigHolder(SkyblockerConfig.class).save();
		super.close();
	}
}
