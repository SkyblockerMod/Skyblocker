package de.hysky.skyblocker.skyblock.chat;

import de.hysky.skyblocker.config.screens.powdertracker.ItemTickList;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ChatRuleLocationConfigScreen extends Screen {
	@Nullable
	private final Screen parent;
	private final ChatRule chatRule;
	private final EnumSet<Location> enabledLocations;

	public ChatRuleLocationConfigScreen(@Nullable Screen parent, ChatRule chatRule) {
		super(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locationsConfigScreen"));
		this.parent = parent;
		this.chatRule = chatRule;
		this.enabledLocations = EnumSet.copyOf(chatRule.getValidLocations()); // Copy the list so we can undo changes when necessary
	}

	@Override
	protected void init() {
		assert client != null;
		addDrawable((context, mouseX, mouseY, delta) -> {
			context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locationsConfigScreen").formatted(Formatting.BOLD), width / 2, (32 - client.textRenderer.fontHeight) / 2, Colors.WHITE);
			context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locationsConfigScreen.note"), width / 2, (38 - client.textRenderer.fontHeight), Colors.WHITE);
		});

		ItemTickList<Location> itemTickList = addDrawableChild(new ItemTickList<>(client, width, height - 107, 43, 24, enabledLocations, EnumSet.complementOf(EnumSet.of(Location.UNKNOWN)), true).init());
		//Grid code gratuitously stolen from WaypointsScreen. Same goes for the y and heights above.
		GridWidget gridWidget = new GridWidget();
		gridWidget.getMainPositioner().marginX(5).marginY(2);
		GridWidget.Adder adder = gridWidget.createAdder(2);

		adder.add(ButtonWidget.builder(Text.translatable("text.skyblocker.reset"), button -> {
			enabledLocations.clear();
			itemTickList.clearAndInit();
		}).build());
		adder.add(ButtonWidget.builder(Text.translatable("text.skyblocker.undo"), button -> {
			enabledLocations.clear();
			enabledLocations.addAll(chatRule.getValidLocations());
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
		chatRule.setValidLocations(enabledLocations);
	}

	@Override
	public void close() {
		assert client != null;
		client.setScreen(parent);
	}
}
