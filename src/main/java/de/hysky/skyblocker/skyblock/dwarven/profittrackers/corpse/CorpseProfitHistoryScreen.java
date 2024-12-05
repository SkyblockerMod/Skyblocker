package de.hysky.skyblocker.skyblock.dwarven.profittrackers.corpse;

import de.hysky.skyblocker.skyblock.dwarven.profittrackers.corpse.CorpseProfitTracker.CorpseLoot;
import it.unimi.dsi.fastutil.doubles.DoubleBooleanImmutablePair;
import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.NumberFormat;
import java.util.List;

public class CorpseProfitHistoryScreen extends Screen {
	private final Screen parent;
	private static final int ENTRY_HEIGHT = 11;

	public CorpseProfitHistoryScreen(Screen parent) {
		super(Text.of("Corpse Profit History Screen"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		addDrawable((context, mouseX, mouseY, delta) -> {
			assert client != null;
			context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("skyblocker.corpseTracker.screenTitle").formatted(Formatting.BOLD), width / 2, (32 - client.textRenderer.fontHeight) / 2, 0xFFFFFF);
		});
		List<CorpseLoot> rewardsList = CorpseProfitTracker.getCurrentProfileRewards();
		CorpseList corpseList = addDrawableChild(new CorpseList(MinecraftClient.getInstance(), width, height - 96, 32, ENTRY_HEIGHT, rewardsList));
		//Grid code gratuitously stolen from WaypointsScreen. Same goes for the y and height above.
		GridWidget gridWidget = new GridWidget();
		gridWidget.getMainPositioner().marginX(5).marginY(2);
		GridWidget.Adder adder = gridWidget.createAdder(1);
		DoubleBooleanPair totalProfit = calculateTotalProfit(rewardsList);
		MutableText totalProfitText = Text.empty()
		                              .append(Text.literal("Total Profit: ").formatted(Formatting.GOLD))
		                              .append(Text.literal(NumberFormat.getInstance().format(totalProfit.leftDouble())).formatted(totalProfit.leftDouble() >= 0 ? Formatting.GREEN : Formatting.RED));
		if (!totalProfit.rightBoolean()) {
			totalProfitText.append(Text.literal(" (Incomplete Price Data)").formatted(Formatting.RED));
		}
		adder.add(new TextWidget(ButtonWidget.DEFAULT_WIDTH, ENTRY_HEIGHT, totalProfitText, client.textRenderer).alignCenter());

		adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> close()).build());
		gridWidget.refreshPositions();
		SimplePositioningWidget.setPos(gridWidget, 0, this.height - 64, this.width, 64);
		gridWidget.forEachChild(this::addDrawableChild);
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
	public void close() {
		assert client != null;
		client.setScreen(parent);
	}
}
