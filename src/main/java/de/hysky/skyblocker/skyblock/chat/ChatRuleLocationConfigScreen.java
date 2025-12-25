package de.hysky.skyblocker.skyblock.chat;

import de.hysky.skyblocker.config.screens.powdertracker.ItemTickList;
import de.hysky.skyblocker.utils.Location;
import java.util.EnumSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;

public class ChatRuleLocationConfigScreen extends Screen {
	private final @Nullable Screen parent;
	private final ChatRule chatRule;
	private final EnumSet<Location> enabledLocations;

	public ChatRuleLocationConfigScreen(@Nullable Screen parent, ChatRule chatRule) {
		super(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locationsConfigScreen"));
		this.parent = parent;
		this.chatRule = chatRule;
		this.enabledLocations = EnumSet.copyOf(chatRule.getValidLocations()); // Copy the list so we can undo changes when necessary
	}

	@Override
	protected void init() {
		assert minecraft != null;
		addRenderableOnly((context, mouseX, mouseY, delta) -> {
			context.drawCenteredString(minecraft.font, Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locationsConfigScreen").withStyle(ChatFormatting.BOLD), width / 2, (32 - minecraft.font.lineHeight) / 2, CommonColors.WHITE);
			context.drawCenteredString(minecraft.font, Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.locationsConfigScreen.note"), width / 2, (38 - minecraft.font.lineHeight), CommonColors.WHITE);
		});

		ItemTickList<Location> itemTickList = addRenderableWidget(new ItemTickList<>(minecraft, width, height - 107, 43, 24, enabledLocations, EnumSet.complementOf(EnumSet.of(Location.UNKNOWN)), true).init());
		//Grid code gratuitously stolen from WaypointsScreen. Same goes for the y and heights above.
		GridLayout gridWidget = new GridLayout();
		gridWidget.defaultCellSetting().paddingHorizontal(5).paddingVertical(2);
		GridLayout.RowHelper adder = gridWidget.createRowHelper(2);

		adder.addChild(Button.builder(Component.translatable("text.skyblocker.reset"), button -> {
			enabledLocations.clear();
			itemTickList.clearAndInit();
		}).build());
		adder.addChild(Button.builder(Component.translatable("text.skyblocker.undo"), button -> {
			enabledLocations.clear();
			enabledLocations.addAll(chatRule.getValidLocations());
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
		chatRule.setValidLocations(enabledLocations);
	}

	@Override
	public void onClose() {
		assert minecraft != null;
		minecraft.setScreen(parent);
	}
}
