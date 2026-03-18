package de.hysky.skyblocker.skyblock.profileviewer.dungeons;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.profileviewer.utils.LevelFinder;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.render.GuiHelper;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class DungeonClassWidget {
	private final String className;
	private LevelFinder.LevelInfo classLevel;
	private static final int CLASS_CAP = 50;
	private JsonObject classData;
	private final FlexibleItemStack stack;
	private boolean active = false;

	private static final Identifier TEXTURE = SkyblockerMod.id("textures/gui/profile_viewer/icon_data_widget.png");
	private static final Identifier ACTIVE_TEXTURE = SkyblockerMod.id("textures/gui/item_protection.png");
	private static final Identifier BAR_FILL = SkyblockerMod.id("bars/bar_fill");
	private static final Identifier BAR_BACK = SkyblockerMod.id("bars/bar_back");

	private static final Font textRenderer = Minecraft.getInstance().font;
	private static final Map<String, FlexibleItemStack> CLASS_ICON = Map.ofEntries(
			Map.entry("Healer", Ico.S_POTION),
			Map.entry("Mage", Ico.B_ROD),
			Map.entry("Berserk", Ico.IRON_SWORD),
			Map.entry("Archer", Ico.BOW),
			Map.entry("Tank", Ico.CHESTPLATE)
	);

	public DungeonClassWidget(String className, JsonObject playerProfile) {
		this.className = className;
		stack = CLASS_ICON.getOrDefault(className, Ico.BARRIER);
		try {
			classData = playerProfile.getAsJsonObject("dungeons").getAsJsonObject("player_classes").getAsJsonObject(this.className.toLowerCase(Locale.ENGLISH));
			classLevel = LevelFinder.getLevelInfo("Catacombs", classData.get("experience").getAsLong());
			active = playerProfile.getAsJsonObject("dungeons").get("selected_dungeon_class").getAsString().equals(className.toLowerCase(Locale.ENGLISH));
		} catch (Exception _) {
			classLevel = LevelFinder.getLevelInfo("", 0);
		}
	}

	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int x, int y) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, 109, 26, 109, 26);
		graphics.item(stack.getStackOrThrow(), x + 3, y + 5);
		if (active) graphics.blit(RenderPipelines.GUI_TEXTURED, ACTIVE_TEXTURE, x + 3, y + 5, 0, 0, 16, 16, 16, 16);

		graphics.text(textRenderer, className + " " + classLevel.level, x + 31, y + 5, Color.WHITE.getRGB(), false);
		Color fillColor = classLevel.level >= CLASS_CAP ? Color.MAGENTA : Color.GREEN;
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BAR_BACK, x + 30, y + 15, 75, 6);
		GuiHelper.nineSliceColored(graphics, BAR_FILL, x + 30, y + 15, (int) (75 * classLevel.fill), 6, fillColor);

		if (mouseX > x + 30 && mouseX < x + 105 && mouseY > y + 12 && mouseY < y + 22) {
			List<Component> tooltipText = new ArrayList<>();
			tooltipText.add(Component.literal(this.className).withStyle(ChatFormatting.GREEN));
			tooltipText.add(Component.literal("XP: " + Formatters.INTEGER_NUMBERS.format(this.classLevel.xp)).withStyle(ChatFormatting.GOLD));
			graphics.setComponentTooltipForNextFrame(textRenderer, tooltipText, mouseX, mouseY);
		}
	}
}
