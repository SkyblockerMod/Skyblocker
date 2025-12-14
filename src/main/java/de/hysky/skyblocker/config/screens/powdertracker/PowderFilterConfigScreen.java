package de.hysky.skyblocker.config.screens.powdertracker;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dwarven.profittrackers.PowderMiningTracker;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;

public class PowderFilterConfigScreen extends Screen {
	private final @Nullable Screen parent;
	private final List<String> filters;
	private final List<String> allItems;

	public PowderFilterConfigScreen(@Nullable Screen parent, List<String> allItems) {
		super(Component.nullToEmpty("Powder Mining Tracker Filter Config"));
		this.parent = parent;
		this.filters = new ArrayList<>(SkyblockerConfigManager.get().mining.crystalHollows.powderTrackerFilter); // Copy the list so we can undo changes when necessary
		this.allItems = allItems;
	}

	@Override
	protected void init() {
		addRenderableOnly((context, mouseX, mouseY, delta) -> {
			assert minecraft != null;
			context.drawCenteredString(minecraft.font, Component.translatable("skyblocker.config.mining.crystalHollows.powderTrackerFilter.screenTitle").withStyle(ChatFormatting.BOLD), width / 2, (32 - minecraft.font.lineHeight) / 2, CommonColors.WHITE);
		});
		ItemTickList<String> itemTickList = addRenderableWidget(new ItemTickList<>(Minecraft.getInstance(), width, height - 96, 32, 24, filters, allItems).init());
		//Grid code gratuitously stolen from WaypointsScreen. Same goes for the y and heights above.
		GridLayout gridWidget = new GridLayout();
		gridWidget.defaultCellSetting().paddingHorizontal(5).paddingVertical(2);
		GridLayout.RowHelper adder = gridWidget.createRowHelper(2);

		adder.addChild(Button.builder(Component.translatable("text.skyblocker.reset"), button -> {
			filters.clear();
			itemTickList.clearAndInit();
		}).build());
		adder.addChild(Button.builder(Component.translatable("text.skyblocker.undo"), button -> {
			filters.clear();
			filters.addAll(SkyblockerConfigManager.get().mining.crystalHollows.powderTrackerFilter);
			itemTickList.clearAndInit();
		}).build());
		adder.addChild(Button.builder(CommonComponents.GUI_DONE, button -> {
								saveFilters();
								onClose();
							})
							.width((Button.DEFAULT_WIDTH * 2) + 10)
							.build(), 2);
		gridWidget.arrangeElements();
		FrameLayout.centerInRectangle(gridWidget, 0, this.height - 64, this.width, 64);
		gridWidget.visitWidgets(this::addRenderableWidget);
	}

	public void saveFilters() {
		SkyblockerConfigManager.update(config -> config.mining.crystalHollows.powderTrackerFilter = filters);
		PowderMiningTracker.INSTANCE.recalculateAll();
	}

	@Override
	public void onClose() {
		assert minecraft != null;
		minecraft.setScreen(parent);
	}
}
