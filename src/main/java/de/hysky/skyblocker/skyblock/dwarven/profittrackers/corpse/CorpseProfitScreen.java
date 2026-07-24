package de.hysky.skyblocker.skyblock.dwarven.profittrackers.corpse;

import it.unimi.dsi.fastutil.doubles.DoubleBooleanImmutablePair;
import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;

import java.text.NumberFormat;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class CorpseProfitScreen extends Screen {
	private static final int ENTRY_HEIGHT = 11;

	private final Screen parent;
	private final List<CorpseLoot> rewardsList = CorpseProfitTracker.getCurrentProfileRewards();
	private @Nullable CorpseList corpseList = null;
	private @Nullable RewardList rewardList = null;
	private final DoubleBooleanPair totalProfit = calculateTotalProfit(rewardsList);
	private boolean summaryView;

	private @Nullable HeaderAndFooterLayout layout;
	private @Nullable ContainerObjectSelectionList<?> selectionList;

	public CorpseProfitScreen(Screen parent) {
		this(parent, true);
	}

	public CorpseProfitScreen(Screen parent, boolean summaryView) {
		super(Component.translatable("skyblocker.corpseTracker.screenTitle"));
		this.parent = parent;
		this.summaryView = summaryView;
	}

	@Override
	protected void repositionElements() {
		if (layout == null || selectionList == null) return;
		layout.arrangeElements();
		selectionList.setSize(layout.getWidth(), layout.getContentHeight());
		selectionList.setPosition(layout.getX(), layout.getHeaderHeight());
		selectionList.refreshScrollAmount();
	}

	@Override
	protected void init() {
		layout = new HeaderAndFooterLayout(this, 25, 45);
		layout.addTitleHeader(Component.translatable("skyblocker.corpseTracker.screenTitle").withStyle(ChatFormatting.BOLD), this.minecraft.font);
		selectionList = summaryView ? getRewardList() : getCorpseList();
		layout.addToContents(selectionList);

		GridLayout gridWidget = new GridLayout();
		gridWidget.defaultCellSetting().paddingHorizontal(5).paddingVertical(2);
		GridLayout.RowHelper adder = gridWidget.createRowHelper(2);

		Component totalProfitText = Component.translatable("skyblocker.corpseTracker.totalProfit",
				NumberFormat.getInstance().format(totalProfit.leftDouble()).formatted(totalProfit.leftDouble() >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED), // Formatting.GOLD is filled in from parent if it's 0
				totalProfit.rightBoolean() ? Component.empty() : Component.literal("skyblocker.corpseTracker.incompletePriceData").withStyle(ChatFormatting.RED)
		).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);

		adder.addChild(new StringWidget(Button.DEFAULT_WIDTH * 2 + 10, ENTRY_HEIGHT, totalProfitText, minecraft.font).setMaxWidth(Button.DEFAULT_WIDTH * 2 + 10, StringWidget.TextOverflow.SCROLLING), 2, gridWidget.newCellSettings().alignHorizontallyCenter());

		Component buttonText = summaryView ? Component.translatable("skyblocker.corpseTracker.historyView") : Component.translatable("skyblocker.corpseTracker.summaryView");
		adder.addChild(Button.builder(buttonText, this::changeView).build());
		adder.addChild(Button.builder(CommonComponents.GUI_DONE, _ -> onClose()).build());

		layout.addToFooter(gridWidget);
		repositionElements();
		layout.visitWidgets(this::addRenderableWidget);
	}

	// Rebuilds the screen with the new view, the main difference being which list is displayed
	private void changeView(Button button) {
		summaryView = !summaryView;
		rebuildWidgets();
	}

	// Lazy init
	private CorpseList getCorpseList() {
		return corpseList == null ? corpseList = new CorpseList(Minecraft.getInstance(), width, height - 96, ENTRY_HEIGHT, rewardsList) : corpseList;
	}

	// Lazy init
	private RewardList getRewardList() {
		return rewardList == null ? rewardList = new RewardList(Minecraft.getInstance(), width, height - 96, 0, ENTRY_HEIGHT, rewardsList) : rewardList;
	}

	private static DoubleBooleanPair calculateTotalProfit(List<CorpseLoot> list) {
		double total = 0;
		boolean isPriceComplete = true;
		for (CorpseLoot loot : list) {
			total += loot.profit();
			if (!loot.isPriceDataComplete()) isPriceComplete = false;
		}
		return DoubleBooleanImmutablePair.of(total, isPriceComplete);
	}

	@Override
	public void onClose() {
		minecraft.gui.setScreen(parent);
	}
}
