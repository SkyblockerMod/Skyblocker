package de.hysky.skyblocker.skyblock.dwarven.profittrackers.corpse;

import it.unimi.dsi.fastutil.doubles.DoubleBooleanImmutablePair;
import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.List;

public class CorpseProfitScreen extends Screen {
	private static final int ENTRY_HEIGHT = 11;

	private final Screen parent;
	private final List<CorpseLoot> rewardsList = CorpseProfitTracker.getCurrentProfileRewards();
	private @Nullable CorpseList corpseList = null;
	private @Nullable RewardList rewardList = null;
	private final DoubleBooleanPair totalProfit = calculateTotalProfit(rewardsList);
	private boolean summaryView;

	public CorpseProfitScreen(Screen parent) {
		this(parent, true);
	}

	public CorpseProfitScreen(Screen parent, boolean summaryView) {
		super(Text.translatable("skyblocker.corpseTracker.screenTitle"));
		this.parent = parent;
		this.summaryView = summaryView;
	}

	@Override
	protected void init() {
		assert client != null;
		addDrawable((context, mouseX, mouseY, delta) -> {
			context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("skyblocker.corpseTracker.screenTitle").formatted(Formatting.BOLD), width / 2, (32 - client.textRenderer.fontHeight) / 2, Colors.WHITE);
		});

		if (summaryView) addDrawableChild(getRewardList());
		else addDrawableChild(getCorpseList());

		GridWidget gridWidget = new GridWidget();
		gridWidget.getMainPositioner().marginX(5).marginY(2);
		GridWidget.Adder adder = gridWidget.createAdder(2);

		Text totalProfitText = Text.translatable("skyblocker.corpseTracker.totalProfit",
				NumberFormat.getInstance().format(totalProfit.leftDouble()).formatted(totalProfit.leftDouble() >= 0 ? Formatting.GREEN : Formatting.RED), // Formatting.GOLD is filled in from parent if it's 0
				totalProfit.rightBoolean() ? Text.empty() : Text.literal("skyblocker.corpseTracker.incompletePriceData").formatted(Formatting.RED)
		).formatted(Formatting.GOLD, Formatting.BOLD);

		adder.add(new TextWidget(ButtonWidget.DEFAULT_WIDTH * 2 + 10, ENTRY_HEIGHT, totalProfitText, client.textRenderer).alignCenter(), 2);

		Text buttonText = summaryView ? Text.translatable("skyblocker.corpseTracker.historyView") : Text.translatable("skyblocker.corpseTracker.summaryView");
		adder.add(ButtonWidget.builder(buttonText, this::changeView).build());
		adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> close()).build());
		gridWidget.refreshPositions();
		SimplePositioningWidget.setPos(gridWidget, 0, this.height - 64, this.width, 64);
		gridWidget.forEachChild(this::addDrawableChild);
	}

	// Rebuilds the screen with the new view, the main difference being which list is displayed
	private void changeView(ButtonWidget button) {
		summaryView = !summaryView;
		clearAndInit();
	}

	// Lazy init
	@NotNull
	private CorpseList getCorpseList() {
		return corpseList == null ? corpseList = new CorpseList(MinecraftClient.getInstance(), width, height - 96, 32, ENTRY_HEIGHT, rewardsList) : corpseList;
	}

	// Lazy init
	@NotNull
	private RewardList getRewardList() {
		return rewardList == null ? rewardList = new RewardList(MinecraftClient.getInstance(), width, height - 96, 32, ENTRY_HEIGHT, rewardsList) : rewardList;
	}

	@NotNull
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
	public void close() {
		assert client != null;
		client.setScreen(parent);
	}
}
