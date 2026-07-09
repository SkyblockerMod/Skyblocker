package de.hysky.skyblocker.skyblock.profileviewer2.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.ProfileItemStorage;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.ButtonWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.InventoryWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.PaginationWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class InventoryPage implements ProfileViewerPage<Pair<LoadingInformation, ProfileItemStorage>> {
	private final List<AbstractWidget> widgets = new ArrayList<>();

	@Override
	public FlexibleItemStack getIcon() {
		return Ico.E_CHEST;
	}

	@Override
	public Component getName() {
		return Component.literal("Inventory");
	}

	@Override
	public CompletableFuture<LayoutElement> load(LoadingInformation info) {
		return CompletableFuture.completedFuture(info)
				.thenCombineAsync(info.itemStorage(), (loadingInfo, itemStorage) -> Pair.of(loadingInfo, itemStorage))
				.thenApplyAsync(this::buildWidgets, Minecraft.getInstance());
	}

	@SuppressWarnings("unused")
	@Override
	public LayoutElement buildWidgets(Pair<LoadingInformation, ProfileItemStorage> data) {
		LoadingInformation info = data.left();
		ProfileItemStorage itemStorage = data.right();

		LinearLayout pageLayout = LinearLayout.horizontal();
		List<LayoutElement> tabContentLayouts = List.of(
				this.buildInventoryLayout(itemStorage),
				this.buildEnderChestLayout(itemStorage),
				this.buildBackpackLayout(itemStorage),
				this.buildGenericWardrobeLayout("Armour Sets", itemStorage.armourSets()),
				this.buildGenericWardrobeLayout("Equipment Sets", itemStorage.equipmentSets()),
				this.buildPetsLayout(itemStorage),
				this.buildAccessoryBagLayout(itemStorage)
				);
		List<ButtonWidget> tabButtons = List.of(
				new ButtonWidget(Ico.CHEST, _ -> selectTab(0, tabContentLayouts)),
				new ButtonWidget(Ico.E_CHEST, _ -> selectTab(1, tabContentLayouts)),
				new ButtonWidget(Ico.JUMBO_BACKPACK, _ -> selectTab(2, tabContentLayouts)),
				new ButtonWidget(Ico.L_CHESTPLATE, _ -> selectTab(3, tabContentLayouts)),
				new ButtonWidget(Ico.BREWING_STAND, _ -> selectTab(4, tabContentLayouts)),
				new ButtonWidget(Ico.BONE, _ -> selectTab(5, tabContentLayouts)),
				new ButtonWidget(Ico.ACCESSORY_BAG, _ -> selectTab(6, tabContentLayouts))
				// Fishing Bag
				// Potion Bag
				// Quiver
				// Sack of Sacks
				// Personal Vault
				);

		LinearLayout tabLayout = LinearLayout.vertical().spacing(1);
		tabButtons.forEach(button -> this.widgets.add(tabLayout.addChild(button)));
		pageLayout.addChild(tabLayout, pageLayout.newCellSettings().alignHorizontallyCenter());

		pageLayout.addChild(SpacerElement.width(16));

		FrameLayout inventoryFrame = new FrameLayout();
		tabContentLayouts.forEach(layout -> inventoryFrame.addChild(layout, LayoutSettings.defaults().alignHorizontallyCenter()));

		pageLayout.addChild(inventoryFrame);

		// Select main page by default
		selectTab(0, tabContentLayouts);

		return pageLayout;
	}

	private LayoutElement buildInventoryLayout(ProfileItemStorage itemStorage) {
		// TODO translatable names
		LinearLayout layout = LinearLayout.vertical();
		this.widgets.add(layout.addChild(new InventoryWidget(Component.literal("Inventory"), 4, 9, List.of(itemStorage.inventory()), true)));
		layout.addChild(SpacerElement.height(6));

		LinearLayout gearLayout = LinearLayout.horizontal().spacing(4);
		this.widgets.add(gearLayout.addChild(new InventoryWidget(Component.literal("Armour"), 1, 4, List.of(itemStorage.armour()), false)));
		this.widgets.add(gearLayout.addChild(new InventoryWidget(Component.literal("Equipment"), 1, 4, List.of(itemStorage.equipment()), false)));
		layout.addChild(gearLayout, layout.newCellSettings().alignHorizontallyCenter());

		return layout;
	}

	private LayoutElement buildEnderChestLayout(ProfileItemStorage itemStorage) {
		List<List<ItemStack>> pages = divideIntoPages(itemStorage.enderChestContents(), 5 * 9);

		return this.buildPaginatedLayout(Component.literal("Ender Chest"), pages);
	}

	private LayoutElement buildBackpackLayout(ProfileItemStorage itemStorage) {
		List<List<ItemStack>> pages = itemStorage.backpacks().values().stream()
				.map(ProfileItemStorage.Backpack::contents)
				.toList();

		return this.buildPaginatedLayout(Component.literal("Backpack"), pages);
	}

	private LayoutElement buildGenericWardrobeLayout(String name, List<ItemStack> items) {
		// Padding (of empty items) is added to the pages to ensure that the ordering logic works when the wardrobe page is not full
		List<List<ItemStack>> unorderedPages = divideIntoPages(items, 4 * 9, true);
		List<List<ItemStack>> orderedPages = new ArrayList<>();

		for (int page = 0; page < unorderedPages.size(); page++) {
			List<ItemStack> unorderedPage = unorderedPages.get(page);
			List<ItemStack> orderedPage = new ArrayList<>();

			for (int offset = 0; offset < 4; offset++) {
				for (int i = offset; i < unorderedPage.size(); i += 4) {
					orderedPage.add(unorderedPage.get(i));
				}
			}

			orderedPages.add(List.copyOf(orderedPage));
		}

		return this.buildPaginatedLayout(Component.literal(name), 4, orderedPages);
	}

	private LayoutElement buildPetsLayout(ProfileItemStorage itemStorage) {
		List<List<ItemStack>> pages = divideIntoPages(itemStorage.pets(), 5 * 9);

		return this.buildPaginatedLayout(Component.literal("Pets"), pages);
	}

	private LayoutElement buildAccessoryBagLayout(ProfileItemStorage itemStorage) {
		List<List<ItemStack>> pages = divideIntoPages(itemStorage.bags().accessories(), 5 * 9);

		return this.buildPaginatedLayout(Component.literal("Accessory Bag"), pages);
	}

	private LayoutElement buildPaginatedLayout(Component name, List<List<ItemStack>> pages) {
		return this.buildPaginatedLayout(name, 5, pages);
	}

	private LayoutElement buildPaginatedLayout(Component name, int rows, List<List<ItemStack>> pages) {
		LinearLayout layout = LinearLayout.vertical();
		InventoryWidget inventory = new InventoryWidget(name, rows, 9, pages, false);
		this.widgets.add(layout.addChild(inventory));

		if (pages.size() > 1) {
			layout.addChild(SpacerElement.height(8));

			LayoutElement pageButtonLayout = this.buildPageButtonLayout(inventory);
			layout.addChild(pageButtonLayout, layout.newCellSettings().alignHorizontallyCenter());
		}

		return layout;
	}

	private LayoutElement buildPageButtonLayout(InventoryWidget inventory) {
		LinearLayout layout = LinearLayout.horizontal().spacing(8);
		StringWidget pageText = new StringWidget(Component.literal(String.format("Page %d/%d", inventory.getPage(), inventory.getMaxPages())), Minecraft.getInstance().font);
		Button.OnPress backwards = _ -> {
			inventory.backwards();
			pageText.setMessage(Component.literal(String.format("Page %d/%d", inventory.getPage(), inventory.getMaxPages())));
		};
		Button.OnPress forwards = _ -> {
			inventory.forwards();
			pageText.setMessage(Component.literal(String.format("Page %d/%d", inventory.getPage(), inventory.getMaxPages())));
		};

		this.widgets.add(layout.addChild(new PaginationWidget(false, backwards)));
		this.widgets.add(layout.addChild(pageText, layout.newCellSettings().alignVerticallyMiddle()));
		this.widgets.add(layout.addChild(new PaginationWidget(true, forwards)));

		return layout;
	}

	private static void selectTab(int index, List<LayoutElement> tabContentLayouts) {
		for (int i = 0; i < tabContentLayouts.size(); i++) {
			boolean shouldBeVisible = i == index;
			tabContentLayouts.get(i).visitWidgets(widget -> widget.visible = shouldBeVisible);
		}
	}

	private static List<List<ItemStack>> divideIntoPages(List<ItemStack> items, int pageSize) {
		return divideIntoPages(items, pageSize, false);
	}

	private static List<List<ItemStack>> divideIntoPages(List<ItemStack> items, int pageSize, boolean padding) {
		int pageCount = (int) Math.ceil((double) items.size() / (double) pageSize);

		if (padding) {
			int maxSize = pageCount * pageSize;
			int missingItems = maxSize - items.size();
			List<ItemStack> appendage = Collections.nCopies(missingItems, ItemStack.EMPTY);

			items = new ArrayList<>(items);
			items.addAll(appendage);
		}

		List<List<ItemStack>> pages = new ArrayList<>(pageCount);

		for (int i = 0; i < pageCount; i++) {
			int fromIndex = i * pageSize;
			int toIndex = Math.min(fromIndex + pageSize, items.size());

			pages.add(items.subList(fromIndex, toIndex));
		}

		return List.copyOf(pages);
	}

	@Override
	public List<AbstractWidget> getWidgets() {
		return this.widgets;
	}
}
