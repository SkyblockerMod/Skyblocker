package me.xmrvizzy.skyblocker.skyblock.dungeon;

import java.awt.Color;

import me.shedaniel.autoconfig.AutoConfig;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.RenderUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class DungeonMapConfigScreen extends Screen {
	
	private int hudX = SkyblockerConfig.get().locations.dungeons.mapX;
	private int hudY = SkyblockerConfig.get().locations.dungeons.mapY;
	
	protected DungeonMapConfigScreen(Text title) {
		super(title);
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		renderBackground(matrices);
		DungeonMap.renderHUDMap(matrices, hudX, hudY);
		drawCenteredTextWithShadow(matrices, textRenderer, "Right Click To Reset Position", width / 2, height / 2, Color.GRAY.getRGB());
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		float scaling = SkyblockerConfig.get().locations.dungeons.mapScaling;
    	int size = (int) (128 * scaling);
		if(RenderUtils.pointExistsInArea((int) mouseX, (int) mouseY, hudX, hudY, hudX + size, hudY + size) && button == 0) {
			hudX = (int) Math.max(Math.min(mouseX - (size / 2), this.width - size), 0);
			hudY = (int) Math.max(Math.min(mouseY - (size / 2), this.height - size), 0);
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if(button == 1) {
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
