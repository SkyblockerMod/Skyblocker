package de.hysky.skyblocker.skyblock.profileviewer.rework.pages;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.profileviewer.model.SlayerBoss;
import de.hysky.skyblocker.skyblock.profileviewer.model.SlayerData;
import de.hysky.skyblocker.skyblock.profileviewer.rework.ProfileViewerWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

final class SlayerWidget implements ProfileViewerWidget {
	private static final Identifier ICON_DATA_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/icon_data_widget.png");

	private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
	private final SlayerData.Slayer slayer;
	private final SlayerBoss slayerData;

	SlayerWidget(
			SlayerData.Slayer slayer,
			SlayerBoss slayerData
	) {
		this.slayer = slayer;
		this.slayerData = slayerData;
	}

	public static final int WIDTH = 109;
	public static final int HEIGHT = 26;

	@Override
	public void render(DrawContext drawContext,
					   int x, int y, int mouseX, int mouseY, float deltaTicks) {
		drawContext.drawTexture(RenderPipelines.GUI_TEXTURED, ICON_DATA_TEXTURE, x, y, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
		drawContext.drawItem(slayer.getDropIcon(), x + 3, y + 5);
		drawContext.drawText(textRenderer, "§aKills: §r" + slayerData.getTotalBossKills(), x + 31, y + 4, -1, false);
		drawContext.drawText(textRenderer, slayerData.getTierWithMostKills() == -1 ? "No Data" : "§cT" + (slayerData.getTierWithMostKills() + 1) + " Kills: §r" + slayerData.getBossKillsByZeroIndexedTier(slayerData.getTierWithMostKills()), x + 31, y + 14, -1, false);

		// TODO: add helper for hover selection
		if (mouseX > x + 30 && mouseX < x + 105 && mouseY > y + 14 && mouseY < y + 21) {
			List<Text> tooltipText = new ArrayList<>();
			for (int i = 0; i <= 4; i++) {
				tooltipText.add(Text.literal("§cT" + (i + 1) + " Kills: §r" + slayerData.getBossKillsByZeroIndexedTier(i)));
			}
			drawContext.drawTooltip(textRenderer, tooltipText, mouseX, mouseY);
		}

	}

	@Override
	public int getHeight() {
		return HEIGHT;
	}

	@Override
	public int getWidth() {
		return WIDTH;
	}
}
