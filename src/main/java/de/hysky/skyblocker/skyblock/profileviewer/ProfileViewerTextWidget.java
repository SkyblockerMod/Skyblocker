package de.hysky.skyblocker.skyblock.profileviewer;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.skyblock.profileviewer.utils.ProfileViewerUtils;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

import java.util.List;

public class ProfileViewerTextWidget {
	private static final int ROW_GAP = 9;

	private String PROFILE_NAME = "UNKNOWN";
	private int SKYBLOCK_LEVEL = 0;
	private double PURSE = 0;
	private double BANK = 0;
	private List<String> cuteNames;
	private boolean dropOpen = false;
	private int boxX, boxY, boxW, boxH;
	private List<JsonObject> profiles;
	private List<ItemStack> modeIcons;

	public ProfileViewerTextWidget(List<JsonObject> allProfiles, int currentIndex, JsonObject hypixelProfile, JsonObject playerProfile) {
		try {
			this.profiles = allProfiles;
			this.cuteNames = allProfiles.stream().map(p -> p.get("cute_name").getAsString()).toList();
			this.modeIcons = allProfiles.stream().map(p -> {
				String gm = p.has("game_mode") ? p.get("game_mode").getAsString() : "";
				return switch (gm) {
					case "bingo" -> new ItemStack(Items.FILLED_MAP);
					case "island" -> new ItemStack(Items.GRASS_BLOCK);
					case "ironman" -> new ItemStack(Items.IRON_INGOT);
					default -> ItemStack.EMPTY;
				};
			}).toList();
			this.PROFILE_NAME = cuteNames.get(currentIndex);
			this.SKYBLOCK_LEVEL = playerProfile.getAsJsonObject("leveling").get("experience").getAsInt() / 100;
			this.PURSE = playerProfile.getAsJsonObject("currencies").get("coin_purse").getAsDouble();
			this.BANK = hypixelProfile.getAsJsonObject("banking").get("balance").getAsDouble();
		} catch (Exception ignored) {}
	}

	public void render(DrawContext context, TextRenderer textRenderer, int root_x, int root_y, ProfileViewerScreen screen, int mouseX, int mouseY) {
		// Profile Icon
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.scale(0.75f, 0.75f, 1);
		int rootAdjustedX = (int) ((root_x) / 0.75f);
		int rootAdjustedY = (int) ((root_y) / 0.75f);
		context.drawItem(Ico.PAINTING, rootAdjustedX, rootAdjustedY);
		matrices.pop();

		int widest = 0;
		for (int i = 0; i < cuteNames.size(); i++) {
			boolean sel = profiles.get(i).getAsJsonPrimitive("selected").getAsBoolean();
			ItemStack ico = modeIcons.get(i);

			int txtW = textRenderer.getWidth((sel ? "★ " : "") + cuteNames.get(i));
			int fullW = txtW + (ico.isEmpty() ? 0 : 14);
			widest = Math.max(widest, fullW);
		}
		int dropWidth = widest + 2;

		boxX = root_x + 14;
		boxY = root_y + 2;
		boxW = textRenderer.getWidth(PROFILE_NAME) + 4;
		boxH = textRenderer.fontHeight + 2;

		boolean hoverName = mouseX >= boxX && mouseX <= boxX + boxW && mouseY >= boxY && mouseY <= boxY + boxH;
		String color = dropOpen ? Formatting.YELLOW.toString() : Formatting.WHITE.toString();
		String display = color + (hoverName ? Formatting.UNDERLINE.toString() : "") + PROFILE_NAME + Formatting.RESET;

		context.fill(boxX - 1, boxY - 1, boxX + boxW + 1, boxY + boxH + 1, 0x55000000);
		context.drawText(textRenderer, display, boxX + 2, boxY + 1, Colors.WHITE, true);

		if (dropOpen && cuteNames.size() > 1) {
			MatrixStack stack = context.getMatrices();
			stack.push();
			stack.translate(0, 0, 300);

			int x0 = boxX;
			int y0 = boxY + boxH + 2;
			int totalH = cuteNames.size() * (boxH + 2) - 2;

			context.fill(x0 - 1, y0 - 1, x0 + dropWidth + 1, y0 + totalH + 1, 0xAA080808);
			context.fill(x0 - 1, y0 - 1, x0 + dropWidth + 1, y0, 0xFFFFFFFF);
			context.fill(x0 - 1, y0 + totalH, x0 + dropWidth + 1, y0 + totalH + 1, 0xFFFFFFFF);
			context.fill(x0 - 1, y0 - 1, x0, y0 + totalH + 1, 0xFFFFFFFF);
			context.fill(x0 + dropWidth, y0 - 1, x0 + dropWidth + 1, y0 + totalH + 1, 0xFFFFFFFF);

			int y = y0;
			int i = 0;
			for (String name : cuteNames) {
				boolean sel = profiles.get(i).getAsJsonPrimitive("selected").getAsBoolean();
				ItemStack ico = modeIcons.get(i);
				String label = (sel ? "★ " : "") + cuteNames.get(i);

				boolean hover = mouseX >= x0 && mouseX <= x0 + dropWidth && mouseY >= y && mouseY <= y + boxH;
				if (hover) context.fill(x0, y, x0 + dropWidth, y + boxH, 0x33FFFFFF);

				int col = sel ? Colors.YELLOW : Colors.WHITE;

				int textX = x0 + 4;
				if (!ico.isEmpty()) {
					float scale = textRenderer.fontHeight / 16.0f;

					MatrixStack m = context.getMatrices();
					m.push();
					m.translate(x0 + 3, y + 1, 300);
					m.scale(scale, scale, 1.0f);
					context.drawItem(ico, 0, 0);
					m.pop();
					textX += (int) (14 * scale) + 4;
				}

				context.drawText(textRenderer, label, textX, y + 1, col, false);
				y += boxH + 2;
				i++;
			}
			stack.pop();
		}
		int baseY = root_y + 6;
		context.drawText(textRenderer, "§aLevel:§r " + SKYBLOCK_LEVEL, root_x + 2, baseY + ROW_GAP, Colors.WHITE, true);
		context.drawText(textRenderer, "§6Purse:§r " + ProfileViewerUtils.numLetterFormat(PURSE), root_x + 2, baseY + ROW_GAP * 2, Colors.WHITE, true);
		context.drawText(textRenderer, "§6Bank:§r " + ProfileViewerUtils.numLetterFormat(BANK), root_x + 2, baseY + ROW_GAP * 3, Colors.WHITE, true);
		context.drawText(textRenderer, "§6NW:§r " + "Soon™", root_x + 2, baseY + ROW_GAP * 4, Colors.WHITE, true);
	}

	public boolean mouseClicked(double mx, double my, int btn, ProfileViewerScreen screen) {
		if (btn != 0) return false;

		if (mx >= boxX && mx <= boxX + boxW && my >= boxY && my <= boxY + boxH) {
			dropOpen = !dropOpen;
			return true;
		}

		if (!dropOpen) return false;

		int y = boxY + boxH + 2;
		for (int i = 0; i < cuteNames.size(); i++, y += boxH + 2) {
			if (mx >= boxX && mx <= boxX + boxW && my >= y && my <= y + boxH) {
				if (!cuteNames.get(i).equals(PROFILE_NAME)) {
					screen.selectProfile(i);
				}
				dropOpen = false;
				return true;
			}
		}

		dropOpen = false;
		return false;
	}
}
