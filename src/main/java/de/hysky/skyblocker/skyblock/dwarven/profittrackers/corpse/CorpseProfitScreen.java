package de.hysky.skyblocker.skyblock.dwarven.profittrackers.corpse;

import de.hysky.skyblocker.skyblock.dwarven.profittrackers.corpse.CorpseProfitTracker.CorpseLoot;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class CorpseProfitScreen extends Screen {
	private final Screen parent;
	private static final int ENTRY_HEIGHT = 11;

	public CorpseProfitScreen(Screen parent) {
		super(Text.of("Corpse Profit Screen"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		addDrawable((context, mouseX, mouseY, delta) -> {
			assert client != null;
			context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("skyblocker.corpseTracker.screenTitle").formatted(Formatting.BOLD), width / 2, (32 - client.textRenderer.fontHeight) / 2, 0xFFFFFF);
		});
		List<CorpseLoot> rewardsList = CorpseProfitTracker.getCurrentProfileRewards();
		RewardList corpseList = addDrawableChild(new RewardList(MinecraftClient.getInstance(), width, height - 96, 32, ENTRY_HEIGHT, rewardsList));
		//Grid code gratuitously stolen from WaypointsScreen. Same goes for the y and height above.
		GridWidget gridWidget = new GridWidget();
		gridWidget.getMainPositioner().marginX(5).marginY(2);
		GridWidget.Adder adder = gridWidget.createAdder(1);

		adder.add(ButtonWidget.builder(Text.translatable("skyblocker.corpseTracker.openHistory"), button -> Scheduler.queueOpenScreen(new CorpseProfitHistoryScreen(this))).build());
		adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> close()).build());
		gridWidget.refreshPositions();
		SimplePositioningWidget.setPos(gridWidget, 0, this.height - 64, this.width, 64);
		gridWidget.forEachChild(this::addDrawableChild);
	}

	@Override
	public void close() {
		assert client != null;
		client.setScreen(parent);
	}
}
