package de.hysky.skyblocker.skyblock.profileviewer2.pages;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.text.WordUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.network.chat.Component;

import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.CollectionTiers;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.ButtonWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.CollectionItemWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.Formatters;

public final class CollectionsPage implements ProfileViewerPage<LoadingInformation> {
	private static final int COLUMNS = 7;
	private static final int VERTICAL_SPACING = 4;
	private static final int HORIZONTAL_SPACING = 2;
	private final List<AbstractWidget> widgets = new ArrayList<>();

	@Override
	public FlexibleItemStack getIcon() {
		return Ico.PAINTING;
	}

	@Override
	public Component getName() {
		return Component.literal("Collections");
	}

	@Override
	public CompletableFuture<LayoutElement> load(LoadingInformation info) {
		return CompletableFuture.completedFuture(info)
				.thenApplyAsync(this::buildWidgets, Minecraft.getInstance());
	}

	@Override
	public LayoutElement buildWidgets(LoadingInformation info) {
		LinearLayout pageLayout = LinearLayout.horizontal();

		// Create each tab and its layout
		List<LayoutElement> tabContentLayouts = List.of(
				this.addCollectionItems(info, "FARMING"),
				this.addCollectionItems(info, "MINING"),
				this.addCollectionItems(info, "COMBAT"),
				this.addCollectionItems(info, "FISHING"),
				this.addCollectionItems(info, "FORAGING"),
				this.addCollectionItems(info, "RIFT")
				);
		List<ButtonWidget> tabButtons = List.of(
				new ButtonWidget(Ico.GOLDEN_HOE, _ -> selectTab(0, tabContentLayouts)),
				new ButtonWidget(Ico.STONE_PICKAXE, _ -> selectTab(1, tabContentLayouts)),
				new ButtonWidget(Ico.STONE_SWORD, _ -> selectTab(2, tabContentLayouts)),
				new ButtonWidget(Ico.FISH_ROD, _ -> selectTab(3, tabContentLayouts)),
				new ButtonWidget(Ico.JUNGLE_SAPLING, _ -> selectTab(4, tabContentLayouts)),
				new ButtonWidget(Ico.MYCELIUM, _ -> selectTab(5, tabContentLayouts))
				);

		// Collection category tabs
		LinearLayout tabLayout = LinearLayout.vertical().spacing(1);
		tabButtons.forEach(button -> this.widgets.add(tabLayout.addChild(button)));
		pageLayout.addChild(tabLayout, pageLayout.newCellSettings().alignVerticallyMiddle());

		// Add space between the tabs and the content
		pageLayout.addChild(SpacerElement.width(16));

		// One big frame layout with each tab's content essentially overlapping each other
		FrameLayout contentFrame = new FrameLayout();
		tabContentLayouts.forEach(layout -> contentFrame.addChild(layout, contentFrame.newChildLayoutSettings().alignVerticallyTop()));
		pageLayout.addChild(contentFrame);

		// Select farming by default
		selectTab(0, tabContentLayouts);

		return pageLayout;
	}

	private LayoutElement addCollectionItems(LoadingInformation info, String collectionCategory) {
		LinearLayout collectionsLayout = LinearLayout.vertical();
		GridLayout collectionsGridLayout = new GridLayout().rowSpacing(VERTICAL_SPACING).columnSpacing(HORIZONTAL_SPACING);
		GridLayout.RowHelper collectionsRowHelper = collectionsGridLayout.createRowHelper(COLUMNS);
		Font font = Minecraft.getInstance().font;

		double totalCollections = CollectionTiers.getCollectionCategoryContents().getOrDefault(collectionCategory, List.of()).size();
		Map<String, CollectionTiers.Report> profileCollections = CollectionTiers.getCollectionCategoryContents().getOrDefault(collectionCategory, List.of()).stream()
				.map(itemId -> Map.entry(itemId, CollectionTiers.getCollectionReport(info.profile(), info.mainMember().id(), itemId)))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, _) -> a, LinkedHashMap::new));

		// Title
		String title = WordUtils.capitalizeFully(collectionCategory) + " Collections";
		this.widgets.add(collectionsLayout.addChild(
				new StringWidget(Component.literal(title).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD).withoutShadow(), font),
				collectionsLayout.newCellSettings().alignHorizontallyCenter()
				));

		// Unlocked text
		double collectionsUnlocked = profileCollections.values().stream()
				.filter(report -> report.total() > 0)
				.count();
		double unlockedPercentage = totalCollections > 0 ? (collectionsUnlocked / totalCollections) * 100f : 0;
		String unlockedText = String.format(Locale.ENGLISH, "Unlocked: %.0f/%.0f (%s%%)", collectionsUnlocked, totalCollections, Formatters.FLOAT_NUMBERS.format(unlockedPercentage));
		this.widgets.add(collectionsLayout.addChild(
				new StringWidget(Component.literal(unlockedText).withStyle(ChatFormatting.DARK_GRAY).withoutShadow(), font),
				collectionsLayout.newCellSettings().alignHorizontallyCenter()
				));

		// Maxed text
		double collectionsMaxed = profileCollections.entrySet().stream()
				.filter(entry -> entry.getValue().tier() == CollectionTiers.getMaxTier(entry.getKey()))
				.count();
		double maxedPercentage = totalCollections > 0 ? (collectionsMaxed / totalCollections) * 100f : 0;
		String maxedText = String.format(Locale.ENGLISH, "Maxed: %.0f/%.0f (%s%%)", collectionsMaxed, totalCollections, Formatters.FLOAT_NUMBERS.format(maxedPercentage));
		this.widgets.add(collectionsLayout.addChild(
				new StringWidget(Component.literal(maxedText).withStyle(ChatFormatting.DARK_GRAY).withoutShadow(), font),
				collectionsLayout.newCellSettings().alignHorizontallyCenter()
				));

		// Add space between heading text & collection displays
		collectionsLayout.addChild(SpacerElement.height(8));

		for (Map.Entry<String, CollectionTiers.Report> entry : profileCollections.entrySet()) {
			String itemId = entry.getKey();
			String neuId = itemId.replace(':', '-');
			FlexibleItemStack icon = ItemRepository.getItemStack(neuId, Ico.BARRIER);
			CollectionTiers.Report report = entry.getValue();

			CollectionItemWidget widget = new CollectionItemWidget(itemId, icon, info, report);
			this.widgets.add(collectionsRowHelper.addChild(widget));
		}

		// Added last to it appears last, yes this helps to understand the code!
		collectionsLayout.addChild(collectionsGridLayout);

		return collectionsLayout;
	}

	// TODO
	private LayoutElement addMinionItems() {
		return null;
	}

	private static void selectTab(int index, List<LayoutElement> tabContentLayouts) {
		for (int i = 0; i < tabContentLayouts.size(); i++) {
			boolean shouldBeVisible = i == index;
			tabContentLayouts.get(i).visitWidgets(widget -> widget.visible = shouldBeVisible);
		}
	}

	@Override
	public List<AbstractWidget> getWidgets() {
		return this.widgets;
	}
}
