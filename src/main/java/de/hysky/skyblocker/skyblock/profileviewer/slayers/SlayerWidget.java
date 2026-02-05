package de.hysky.skyblocker.skyblock.profileviewer.slayers;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.profileviewer.utils.LevelFinder;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.render.HudHelper;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class SlayerWidget {
	private final String slayerName;
	private final LevelFinder.LevelInfo slayerLevel;
	private JsonObject slayerData = null;

	private static final Identifier TEXTURE = SkyblockerMod.id("textures/gui/profile_viewer/icon_data_widget.png");
	private static final Identifier BAR_FILL = SkyblockerMod.id("bars/bar_fill");
	private static final Identifier BAR_BACK = SkyblockerMod.id("bars/bar_back");
	private final Identifier item;
	private final ItemStack drop;
	public static final Map<String, Identifier> HEAD_ICON = Map.ofEntries(
			Map.entry("Zombie", SkyblockerMod.id("textures/gui/profile_viewer/zombie.png")),
			Map.entry("Spider", SkyblockerMod.id("textures/gui/profile_viewer/spider.png")),
			Map.entry("Wolf", SkyblockerMod.id("textures/gui/profile_viewer/wolf.png")),
			Map.entry("Enderman", SkyblockerMod.id("textures/gui/profile_viewer/enderman.png")),
			Map.entry("Vampire", SkyblockerMod.id("textures/gui/profile_viewer/vampire.png")),
			Map.entry("Blaze", SkyblockerMod.id("textures/gui/profile_viewer/blaze.png"))
	);

	private static final Map<String, ItemStack> DROP_ICON = Map.ofEntries(
			Map.entry("Zombie", Ico.FLESH),
			Map.entry("Spider", Ico.STRING),
			Map.entry("Wolf", Ico.MUTTON),
			Map.entry("Enderman", Ico.E_PEARL),
			Map.entry("Vampire", Ico.REDSTONE),
			Map.entry("Blaze", Ico.B_POWDER)
	);

	public SlayerWidget(String slayer, long xp, JsonObject playerProfile) {
		this.slayerName = slayer;
		this.slayerLevel = LevelFinder.getLevelInfo(slayer, xp);
		this.item = HEAD_ICON.get(slayer);
		this.drop = DROP_ICON.getOrDefault(slayer, Ico.BARRIER);
		try {
			this.slayerData = playerProfile.getAsJsonObject("slayer").getAsJsonObject("slayer_bosses").getAsJsonObject(this.slayerName.toLowerCase(Locale.ENGLISH));
		} catch (Exception ignored) {}
	}

	public void render(GuiGraphics context, int mouseX, int mouseY, int x, int y) {
		Font font = Minecraft.getInstance().font;
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, 109, 26, 109, 26);
		context.blit(RenderPipelines.GUI_TEXTURED, this.item, x + 1, y + 3, 0, 0, 20, 20, 20, 20);
		context.drawString(font, slayerName + " " + slayerLevel.level, x + 31, y + 5, Color.white.hashCode(), false);

		int col2 = x + 113;
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, col2, y, 0, 0, 109, 26, 109, 26);
		context.renderItem(this.drop, col2 + 3, y + 5);
		context.drawString(font, "§aKills: §r" + findTotalKills(), col2 + 30, y + 4, Color.white.hashCode(), true);
		context.drawString(font, findTopTierKills(), findTopTierKills().equals("No Data") ? col2 + 30 : col2 + 29, y + 15, Color.white.hashCode(), true);

		context.blitSprite(RenderPipelines.GUI_TEXTURED, BAR_BACK, x + 30, y + 15, 75, 6);
		Color fillColor = slayerLevel.fill == 1 ? Color.MAGENTA : Color.green;
		HudHelper.renderNineSliceColored(context, BAR_FILL, x + 30, y + 15, (int) (75 * slayerLevel.fill), 6, fillColor);

		if (mouseX > x + 30 && mouseX < x + 105 && mouseY > y + 12 && mouseY < y + 22) {
			List<Component> tooltipText = new ArrayList<>();
			tooltipText.add(Component.literal(this.slayerName).withStyle(ChatFormatting.GREEN));
			tooltipText.add(Component.literal("XP: " + Formatters.INTEGER_NUMBERS.format(this.slayerLevel.xp)).withStyle(ChatFormatting.GOLD));
			context.setComponentTooltipForNextFrame(font, tooltipText, mouseX, mouseY);
		}
	}

	private int findTotalKills() {
		try {
			int totalKills = 0;
			for (String key : this.slayerData.keySet()) {
				if (key.startsWith("boss_kills_tier_")) totalKills += this.slayerData.get(key).getAsInt();
			}
			return totalKills;
		} catch (Exception e) {
			return 0;
		}
	}

	private String findTopTierKills() {
		try {
			for (int tier = 4; tier >= 0; tier--) {
				String key = "boss_kills_tier_" + tier;
				if (this.slayerData.has(key)) return "§cT" + (tier + 1) + " Kills: §r" + this.slayerData.get(key).getAsInt();
			}
		} catch (Exception ignored) {}
		return "No Data";
	}
}
