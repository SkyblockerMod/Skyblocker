package de.hysky.skyblocker.skyblock.profileviewer2.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;

import de.hysky.skyblocker.skyblock.dungeon.DungeonClass;
import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.CroesusWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.DailyRunsWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.DungeonLevelBarWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.DungeonRunsWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.DungeonSecretsWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.FloorRunsWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.FlexibleItemStack;

import static de.hysky.skyblocker.skyblock.profileviewer2.pages.SkillsPage.LEVEL_BAR_WIDTH;

public final class CatacombsPage implements ProfileViewerPage<LoadingInformation> {
	private static final int VERTICAL_SPACING = 2;
	private static final int HORIZONTAL_SPACING = VERTICAL_SPACING * 2;
	private final List<AbstractWidget> widgets = new ArrayList<>();

	@Override
	public FlexibleItemStack getIcon() {
		return Ico.CATACOMBS;
	}

	@Override
	public Component getName() {
		return Component.literal("Catacombs");
	}

	@Override
	public CompletableFuture<LayoutElement> load(LoadingInformation info) {
		return CompletableFuture.completedFuture(info)
				.thenApplyAsync(this::buildWidgets, Minecraft.getInstance());
	}

	@Override
	public LayoutElement buildWidgets(LoadingInformation info) {
		LinearLayout pageLayout = LinearLayout.horizontal().spacing(HORIZONTAL_SPACING);

		LinearLayout levelsLayout = LinearLayout.vertical().spacing(VERTICAL_SPACING);
		levelsLayout.addChild(DungeonLevelBarWidget.createCatacombs(LEVEL_BAR_WIDTH, info));
		levelsLayout.addChild(DungeonLevelBarWidget.createClassAverage(LEVEL_BAR_WIDTH, info));
		levelsLayout.addChild(DungeonLevelBarWidget.createClass(LEVEL_BAR_WIDTH, info, DungeonClass.HEALER));
		levelsLayout.addChild(DungeonLevelBarWidget.createClass(LEVEL_BAR_WIDTH, info, DungeonClass.MAGE));
		levelsLayout.addChild(DungeonLevelBarWidget.createClass(LEVEL_BAR_WIDTH, info, DungeonClass.BERSERK));
		levelsLayout.addChild(DungeonLevelBarWidget.createClass(LEVEL_BAR_WIDTH, info, DungeonClass.ARCHER));
		levelsLayout.addChild(DungeonLevelBarWidget.createClass(LEVEL_BAR_WIDTH, info, DungeonClass.TANK));
		pageLayout.addChild(levelsLayout, pageLayout.newCellSettings().alignHorizontallyCenter());

		LinearLayout statsLayout = LinearLayout.vertical().spacing(VERTICAL_SPACING);

		LinearLayout row1 = LinearLayout.horizontal().spacing(HORIZONTAL_SPACING);
		row1.addChild(new DungeonSecretsWidget(LEVEL_BAR_WIDTH, info));
		row1.addChild(new CroesusWidget(LEVEL_BAR_WIDTH, info));
		statsLayout.addChild(row1);

		LinearLayout row2 = LinearLayout.horizontal().spacing(HORIZONTAL_SPACING);
		row2.addChild(new DungeonRunsWidget(LEVEL_BAR_WIDTH, info));
		row2.addChild(new DailyRunsWidget(LEVEL_BAR_WIDTH, info));
		statsLayout.addChild(row2);

		// Have floor runs at the bottom of the stats section
		statsLayout.addChild(new FloorRunsWidget((LEVEL_BAR_WIDTH * 2) + HORIZONTAL_SPACING, 94, info));

		// Add stats half to main layout
		pageLayout.addChild(statsLayout, pageLayout.newCellSettings().alignHorizontallyCenter());

		// Add all widgets
		pageLayout.visitWidgets(this.widgets::add);

		return pageLayout;
	}

	@Override
	public List<AbstractWidget> getWidgets() {
		return this.widgets;
	}

	@Override
	public boolean centred() {
		return true;
	}
}
