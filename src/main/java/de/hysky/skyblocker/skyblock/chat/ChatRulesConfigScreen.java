package de.hysky.skyblocker.skyblock.chat;

import de.hysky.skyblocker.utils.ChildScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class ChatRulesConfigScreen extends ChildScreen {
	private ChatRulesConfigListWidget chatRulesConfigListWidget;
	private HeaderAndFooterLayout layout;

	public ChatRulesConfigScreen(@Nullable Screen parent) {
		super(Component.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen"), parent);
	}

	@Override
	protected void init() {
		layout = new HeaderAndFooterLayout(this);
		layout.addTitleHeader(this.title, font);
		chatRulesConfigListWidget = layout.addToContents(new ChatRulesConfigListWidget(minecraft, this, width, layout.getContentHeight(), layout.getHeaderHeight()));
		LinearLayout footerLayout = layout.addToFooter(new LinearLayout(0, 0, LinearLayout.Orientation.HORIZONTAL));
		footerLayout.defaultCellSetting().paddingHorizontal(5).paddingVertical(2);
		footerLayout.addChild(Button.builder(CommonComponents.GUI_CANCEL, _ -> {
			reopenParent();
		}).build());
		footerLayout.addChild(Button.builder(Component.translatable("skyblocker.config.chat.chatRules.screen.new"),
				_ -> chatRulesConfigListWidget.addRuleAfterSelected()
		).build());
		footerLayout.addChild(Button.builder(CommonComponents.GUI_DONE, _ -> {
			chatRulesConfigListWidget.saveRules();
			reopenParent();
		}).build());

		layout.arrangeElements();
		layout.visitWidgets(this::addRenderableWidget);
	}

	@Override
	protected void repositionElements() {
		layout.arrangeElements();
		chatRulesConfigListWidget.setRectangle(width, layout.getContentHeight(), 0, layout.getHeaderHeight());
		chatRulesConfigListWidget.refreshScrollAmount();
	}

	@Override
	public void onClose() {
		if (!chatRulesConfigListWidget.hasChanges()) {
			reopenParent();
			return;
		}
		minecraft.setScreen(new ConfirmScreen(confirmedAction -> {
			if (confirmedAction) {
				reopenParent();
			} else {
				minecraft.setScreen(this);
			}
		}, Component.translatable("text.skyblocker.quit_config"), Component.translatable("text.skyblocker.quit_config_sure"), Component.translatable("text.skyblocker.quit_discard"), CommonComponents.GUI_CANCEL));
	}
}
