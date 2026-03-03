package de.hysky.skyblocker.skyblock.profileviewer.collections;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerPage;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerScreen;
import de.hysky.skyblocker.skyblock.profileviewer.utils.SubPageSelectButton;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class CollectionsPage implements ProfileViewerPage {
	private static final String[] COLLECTION_CATEGORIES = {"MINING", "FARMING", "COMBAT", "FISHING", "FORAGING", "RIFT"};
	private static final int TOTAL_HEIGHT = 165;
	private static final Map<String, ItemStack> ICON_MAP = Map.ofEntries(
			Map.entry("MINING", Ico.STONE_PICKAXE),
			Map.entry("FARMING", Ico.GOLDEN_HOE),
			Map.entry("COMBAT", Ico.STONE_SWORD),
			Map.entry("FISHING", Ico.FISH_ROD),
			Map.entry("FORAGING", Ico.JUNGLE_SAPLING),
			// Map.entry("BOSS", Ico.WITHER), Not currently part of Collections API so skipping for now
			Map.entry("RIFT", Ico.MYCELIUM)
	);
	private static final Font textRenderer = Minecraft.getInstance().font;

	private final @Nullable GenericCategory[] collections = new GenericCategory[COLLECTION_CATEGORIES.length];
	private final List<SubPageSelectButton> collectionSelectButtons = new ArrayList<>();
	private int activePage = 0;


	public CollectionsPage(JsonObject hProfile, JsonObject pProfile) {
		for (int i = 0; i < COLLECTION_CATEGORIES.length; i++) {
			try {
				collectionSelectButtons.add(new SubPageSelectButton(this, -100, 0, i, ICON_MAP.getOrDefault(COLLECTION_CATEGORIES[i], Ico.BARRIER)));
				collections[i] = new GenericCategory(hProfile, pProfile, COLLECTION_CATEGORIES[i]);
			} catch (Exception e) {
				ProfileViewerScreen.LOGGER.error("[Skyblocker Profile Viewer] Error creating Collections Page", e);
			}
		}
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta, int rootX, int rootY) {
		int startingY = rootY + (TOTAL_HEIGHT - collectionSelectButtons.size() * 21) / 2;
		for (int i = 0; i < collectionSelectButtons.size(); i++) {
			collectionSelectButtons.get(i).setX(rootX);
			collectionSelectButtons.get(i).setY(startingY + i * 21);
			collectionSelectButtons.get(i).render(context, mouseX, mouseY, delta);
		}

		if (collections[activePage] == null) {
			context.drawString(textRenderer, "No data...", rootX + 92, rootY + 72, Color.DARK_GRAY.getRGB(), false);
			return;
		}

		collections[activePage].markWidgetsAsVisible();
		collections[activePage].render(context, mouseX, mouseY, delta, rootX + 35, rootY + 6);
	}

	public void onNavButtonClick(SubPageSelectButton selectButton) {
		if (collections[activePage] != null) collections[activePage].markWidgetsAsInvisible();
		for (SubPageSelectButton button : collectionSelectButtons) {
			button.setToggled(false);
		}
		activePage = selectButton.getIndex();
		selectButton.setToggled(true);
	}

	@Override
	public List<AbstractWidget> getButtons() {
		List<AbstractWidget> clickableWidgets = new ArrayList<>(collectionSelectButtons);
		for (ProfileViewerPage page : collections) {
			if (page != null && page.getButtons() != null) clickableWidgets.addAll(page.getButtons());
		}
		return clickableWidgets;
	}

	@Override
	public void markWidgetsAsVisible() {
		for (SubPageSelectButton button : collectionSelectButtons) {
			button.visible = true;
			button.active = true;
		}
	}

	@Override
	public void markWidgetsAsInvisible() {
		for (SubPageSelectButton button : collectionSelectButtons) {
			button.visible = false;
			button.active = false;
		}
	}
}
