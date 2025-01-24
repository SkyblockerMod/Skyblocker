package de.hysky.skyblocker.config.screens.powdertracker;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dwarven.PowderMiningTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PowderFilterConfigScreen extends Screen {
	@Nullable
	private final Screen parent;
	private final List<String> filters;
	private final List<String> allItems;

	public PowderFilterConfigScreen(@Nullable Screen parent, List<String> allItems) {
		super(Text.of("Powder Mining Tracker Filter Config"));
		this.parent = parent;
		this.filters = new ArrayList<>(SkyblockerConfigManager.get().mining.crystalHollows.powderTrackerFilter); // Copy the list so we can undo changes when necessary
		this.allItems = allItems;
	}

	@Override
	protected void init() {
		addDrawable((context, mouseX, mouseY, delta) -> {
			assert client != null;
			context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("skyblocker.config.mining.crystalHollows.powderTrackerFilter.screenTitle").formatted(Formatting.BOLD), width / 2, (32 - client.textRenderer.fontHeight) / 2, 0xFFFFFF);
		});
		ItemTickList itemTickList = addDrawableChild(new ItemTickList(MinecraftClient.getInstance(), width, height - 96, 32, 24, filters, allItems).init());
		//Grid code gratuitously stolen from WaypointsScreen. Same goes for the y and heights above.
		GridWidget gridWidget = new GridWidget();
		gridWidget.getMainPositioner().marginX(5).marginY(2);
		GridWidget.Adder adder = gridWidget.createAdder(2);

		adder.add(ButtonWidget.builder(Text.translatable("text.skyblocker.reset"), button -> {
			filters.clear();
			itemTickList.clearAndInit();
		}).build());
		adder.add(ButtonWidget.builder(Text.translatable("text.skyblocker.undo"), button -> {
			filters.clear();
			filters.addAll(SkyblockerConfigManager.get().mining.crystalHollows.powderTrackerFilter);
			itemTickList.clearAndInit();
		}).build());
		adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> {
			                      saveFilters();
			                      close();
		                      })
		                      .width((ButtonWidget.DEFAULT_WIDTH * 2) + 10)
		                      .build(), 2);
		gridWidget.refreshPositions();
		SimplePositioningWidget.setPos(gridWidget, 0, this.height - 64, this.width, 64);
		gridWidget.forEachChild(this::addDrawableChild);
	}

	public void saveFilters() {
		SkyblockerConfigManager.get().mining.crystalHollows.powderTrackerFilter = filters;
		SkyblockerConfigManager.save();
		PowderMiningTracker.recalculateAll();
	}

	@Override
	public void close() {
		assert client != null;
		client.setScreen(parent);
	}
}
