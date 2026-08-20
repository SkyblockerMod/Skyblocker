package de.hysky.skyblocker.skyblock.profileviewer2.pages;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import net.minecraft.util.CommonColors;

import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.model.PlayerData;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.CollectionTiers;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.ButtonWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.CollectionItemWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.MinionWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.data.constants.ConstantData;

public final class CollectionsPage implements ProfileViewerPage<LoadingInformation> {
	private static final int VERTICAL_GRID_SPACING = 4;
	private static final int HORIZONTAL_GRID_SPACING = 2;
	private static final int SECTION_SPACING = 16;
	private static final int HEADING_CONTENT_SPACING = 8;
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
				this.buildCollectionLayout(info, "FARMING"),
				this.buildCollectionLayout(info, "MINING"),
				this.buildCollectionLayout(info, "COMBAT"),
				this.buildCollectionLayout(info, "FISHING"),
				this.buildCollectionLayout(info, "FORAGING"),
				this.buildCollectionLayout(info, "RIFT")
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
		tabButtons.forEach(button -> tabLayout.addChild(button));
		pageLayout.addChild(tabLayout, pageLayout.newCellSettings().alignVerticallyMiddle());

		// Add space between the tabs and the content
		pageLayout.addChild(SpacerElement.width(SECTION_SPACING));

		// One big frame layout with each tab's content essentially overlapping each other
		FrameLayout contentFrame = new FrameLayout();
		tabContentLayouts.forEach(layout -> contentFrame.addChild(layout, contentFrame.newChildLayoutSettings().alignVerticallyTop()));
		pageLayout.addChild(contentFrame);

		// Add all widgets
		pageLayout.visitWidgets(this.widgets::add);

		// Select farming by default
		selectTab(0, tabContentLayouts);

		return pageLayout;
	}

	private LayoutElement buildCollectionLayout(LoadingInformation info, String collectionCategory) {
		LinearLayout categoryLayout = LinearLayout.horizontal();
		categoryLayout.addChild(this.addCollectionItems(info, collectionCategory));
		categoryLayout.addChild(SpacerElement.width(SECTION_SPACING));
		categoryLayout.addChild(this.addMinionItems(info, collectionCategory));

		return categoryLayout;
	}

	private LayoutElement addCollectionItems(LoadingInformation info, String collectionCategory) {
		LinearLayout collectionsLayout = LinearLayout.vertical();
		GridLayout collectionsGridLayout = new GridLayout().rowSpacing(VERTICAL_GRID_SPACING).columnSpacing(HORIZONTAL_GRID_SPACING);
		GridLayout.RowHelper collectionsRowHelper = collectionsGridLayout.createRowHelper(7);

		List<String> categoryContents = CollectionTiers.getCollectionCategoryContents().getOrDefault(collectionCategory, List.of());
		Map<String, CollectionTiers.Report> profileCollections = categoryContents.stream()
				.map(itemId -> Map.entry(itemId, CollectionTiers.getCollectionReport(info.profile(), info.mainMember().id(), itemId)))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, _) -> a, LinkedHashMap::new));

		int totalCollections = categoryContents.size();
		int unlocked = 0;
		int maxed = 0;

		for (Map.Entry<String, CollectionTiers.Report> entry : profileCollections.entrySet()) {
			String itemId = entry.getKey();
			CollectionTiers.Report report = entry.getValue();

			if (report.total() > 0) {
				unlocked++;
			}

			if (report.tier() == CollectionTiers.getMaxTier(itemId)) {
				maxed++;
			}

			CollectionItemWidget widget = CollectionItemWidget.create(itemId, info, report);
			collectionsRowHelper.addChild(widget);
		}

		String title = WordUtils.capitalizeFully(collectionCategory) + " Collections";
		this.addSectionHeadings(collectionsLayout, title, totalCollections, unlocked, maxed);
		collectionsLayout.addChild(collectionsGridLayout);

		return collectionsLayout;
	}

	private LayoutElement addMinionItems(LoadingInformation info, String category) {
		LinearLayout minionsLayout = LinearLayout.vertical();
		GridLayout minionsGridLayout = new GridLayout().rowSpacing(VERTICAL_GRID_SPACING).columnSpacing(HORIZONTAL_GRID_SPACING);
		GridLayout.RowHelper minionsRowHelper = minionsGridLayout.createRowHelper(6);

		List<String> minions = ConstantData.getMinionConstants().categories().getOrDefault(category, List.of());
		List<String> craftedMinions = info.profile().members.values().stream()
				.map(member -> member.playerData.craftedMinions)
				.flatMap(Set::stream)
				.toList();
		Map<String, Integer> maxTiers = !NEURepoManager.isLoading() ? NEURepoManager.getConstants().getMisc().getMaxMinionLevel() : Map.of();

		int totalMinions = minions.size();
		int unlocked = 0;
		int maxed = 0;

		for (String minionId : minions) {
			// Default max tier to 11 since all minions have at least that many tiers
			int maxTier = maxTiers.getOrDefault(minionId + "_GENERATOR", 11);
			IntPredicate hasCraftedTier = tier -> PlayerData.hasCraftedMinionTier(craftedMinions, minionId, tier);
			List<Integer> tiersCrafted = IntStream.rangeClosed(1, maxTier)
					.filter(hasCraftedTier)
					.boxed()
					.toList();
			OptionalInt lowestUncraftedTier = IntStream.rangeClosed(1, maxTier)
					.filter(hasCraftedTier.negate())
					.min();

			if (!tiersCrafted.isEmpty()) {
				unlocked++;
			}

			if (tiersCrafted.size() == maxTier) {
				maxed++;
			}

			MinionWidget widget = MinionWidget.create(minionId, maxTier, tiersCrafted, lowestUncraftedTier);
			minionsRowHelper.addChild(widget);
		}

		String title = WordUtils.capitalizeFully(category) + " Minions";
		this.addSectionHeadings(minionsLayout, title, totalMinions, unlocked, maxed);
		minionsLayout.addChild(minionsGridLayout);

		return minionsLayout;
	}

	private void addSectionHeadings(LinearLayout layout, String title, int total, int unlocked, int maxed) {
		Font font = Minecraft.getInstance().font;

		// Title
		layout.addChild(
				new StringWidget(Component.literal(title).withColor(CommonColors.DARK_GRAY).withStyle(ChatFormatting.BOLD).withoutShadow(), font),
				layout.newCellSettings().alignHorizontallyCenter()
				);

		// Unlocked
		double unlockedPercentage = calculatePercentage(unlocked, total);
		String unlockedText = String.format(Locale.ENGLISH, "Unlocked: %d/%d (%s%%)", unlocked, total, Formatters.FLOAT_NUMBERS.format(unlockedPercentage));
		layout.addChild(
				new StringWidget(Component.literal(unlockedText).withColor(CommonColors.DARK_GRAY).withoutShadow(), font),
				layout.newCellSettings().alignHorizontallyCenter()
				);

		// Maxed
		double maxedPercentage = calculatePercentage(maxed, total);
		String maxedText = String.format(Locale.ENGLISH, "Maxed: %d/%d (%s%%)", maxed, total, Formatters.FLOAT_NUMBERS.format(maxedPercentage));
		layout.addChild(
				new StringWidget(Component.literal(maxedText).withColor(CommonColors.DARK_GRAY).withoutShadow(), font),
				layout.newCellSettings().alignHorizontallyCenter()
				);

		// Spacing
		layout.addChild(SpacerElement.height(HEADING_CONTENT_SPACING));
	}

	private static double calculatePercentage(double amount, double total) {
		return total > 0 ? (amount / total) * 100d : 0;
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
