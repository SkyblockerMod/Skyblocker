package de.hysky.skyblocker.skyblock.profileviewer.inventory;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerPage;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerScreen;
import de.hysky.skyblocker.skyblock.profileviewer.inventory.itemLoaders.BackpackItemLoader;
import de.hysky.skyblocker.skyblock.profileviewer.inventory.itemLoaders.PetsInventoryItemLoader;
import de.hysky.skyblocker.skyblock.profileviewer.inventory.itemLoaders.WardrobeInventoryItemLoader;
import de.hysky.skyblocker.skyblock.profileviewer.utils.ProfileViewerUtils;
import de.hysky.skyblocker.skyblock.profileviewer.utils.SubPageSelectButton;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import it.unimi.dsi.fastutil.ints.IntIntPair;
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

public class InventoryPage implements ProfileViewerPage {
	private static final String[] INVENTORY_PAGES = {"inventory", "enderchest", "backpack", "wardrobe", "pets", "accessoryBag"};
	private static final int TOTAL_HEIGHT = 165;
	private static final Map<String, ItemStack> ICON_MAP = Map.ofEntries(
			Map.entry("wardrobe", Ico.L_CHESTPLATE),
			Map.entry("inventory", Ico.CHEST),
			Map.entry("backpack", ProfileViewerUtils.createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzYyZjNiM2EwNTQ4MWNkZTc3MjQwMDA1YzBkZGNlZTFjMDY5ZTU1MDRhNjJjZTA5Nzc4NzlmNTVhMzkzOTYxNDYifX19")),
			Map.entry("pets", Ico.BONE),
			Map.entry("enderchest", Ico.E_CHEST),
			Map.entry("accessoryBag", ProfileViewerUtils.createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTYxYTkxOGMwYzQ5YmE4ZDA1M2U1MjJjYjkxYWJjNzQ2ODkzNjdiNGQ4YWEwNmJmYzFiYTkxNTQ3MzA5ODVmZiJ9fX0="))
	);

	private static final Font textRenderer = Minecraft.getInstance().font;
	private final @Nullable ProfileViewerPage[] inventorySubPages = new ProfileViewerPage[6];
	private final List<SubPageSelectButton> inventorySelectButtons = new ArrayList<>();
	private int activePage = 0;

	public InventoryPage(JsonObject pProfile) {
		for (int i = 0; i < INVENTORY_PAGES.length; i++) {
			inventorySelectButtons.add(new SubPageSelectButton(this, -100, 0, i, ICON_MAP.getOrDefault(INVENTORY_PAGES[i], Ico.BARRIER)));
		}

		try {
			JsonObject inventoryData = pProfile.getAsJsonObject("inventory");
			if (inventoryData == null) return;
			inventorySubPages[0] = new PlayerInventory(inventoryData);
			if (inventoryData.has("ender_chest_contents")) inventorySubPages[1] = new Inventory(INVENTORY_PAGES[1], IntIntPair.of(5, 9), inventoryData.getAsJsonObject("ender_chest_contents"));
			if (inventoryData.has("backpack_contents")) inventorySubPages[2] = new Inventory(INVENTORY_PAGES[2], IntIntPair.of(5, 9), inventoryData.getAsJsonObject("backpack_contents"), new BackpackItemLoader());
			if (inventoryData.has("wardrobe_contents")) inventorySubPages[3] = new Inventory(INVENTORY_PAGES[3], IntIntPair.of(4, 9), inventoryData.getAsJsonObject("wardrobe_contents"), new WardrobeInventoryItemLoader(inventoryData));
			inventorySubPages[4] = new Inventory(INVENTORY_PAGES[4], IntIntPair.of(4, 9), pProfile, new PetsInventoryItemLoader());
			if (inventoryData.has("bag_contents") && inventoryData.getAsJsonObject("bag_contents").has("talisman_bag")) inventorySubPages[5] = new Inventory(INVENTORY_PAGES[5], IntIntPair.of(5, 9), inventoryData.getAsJsonObject("bag_contents").getAsJsonObject("talisman_bag"));
		} catch (Exception e) {
			ProfileViewerScreen.LOGGER.error("[Skyblocker Profile Viewer] Error while loading inventory data: ", e);
		}
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta, int rootX, int rootY) {
		int startingY = rootY + (TOTAL_HEIGHT - inventorySelectButtons.size() * 21) / 2;
		for (int i = 0; i < inventorySelectButtons.size(); i++) {
			inventorySelectButtons.get(i).setX(rootX);
			inventorySelectButtons.get(i).setY(startingY + i * 21);
			inventorySelectButtons.get(i).render(context, mouseX, mouseY, delta);
		}

		if (inventorySubPages[activePage] == null) {
			context.drawString(textRenderer, "No data...", rootX + 92, rootY + 72, Color.DARK_GRAY.getRGB(), false);
			return;
		}

		inventorySubPages[activePage].markWidgetsAsVisible();
		inventorySubPages[activePage].render(context, mouseX, mouseY, delta, rootX + 35, rootY + 6);
	}

	public void onNavButtonClick(SubPageSelectButton clickedButton) {
		if (inventorySubPages[activePage] != null) inventorySubPages[activePage].markWidgetsAsInvisible();
		for (SubPageSelectButton button : inventorySelectButtons) {
			button.setToggled(false);
		}
		activePage = clickedButton.getIndex();
		clickedButton.setToggled(true);
	}

	@Override
	public List<AbstractWidget> getButtons() {
		List<AbstractWidget> clickableWidgets = new ArrayList<>(inventorySelectButtons);
		for (ProfileViewerPage page : inventorySubPages) {
			if (page != null && page.getButtons() != null) clickableWidgets.addAll(page.getButtons());
		}
		return clickableWidgets;
	}

	@Override
	public void markWidgetsAsVisible() {
		if (inventorySubPages[activePage] != null) inventorySubPages[activePage].markWidgetsAsVisible();
		for (SubPageSelectButton button : inventorySelectButtons) {
			button.visible = true;
			button.active = true;
		}
	}

	@Override
	public void markWidgetsAsInvisible() {
		if (inventorySubPages[activePage] != null) inventorySubPages[activePage].markWidgetsAsInvisible();
		for (SubPageSelectButton button : inventorySelectButtons) {
			button.visible = false;
			button.active = false;
		}
	}
}
