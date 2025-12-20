package de.hysky.skyblocker.skyblock.chat;

import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class ChatRulesConfigScreen extends Screen {
	private ChatRulesConfigListWidget chatRulesConfigListWidget;
	private ThreePartsLayoutWidget layout;
	private final Screen parent;

	public ChatRulesConfigScreen(Screen parent) {
		super(Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen"));
		this.parent = parent;
	}

	@Override
	public @Nullable GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
		return null;
	}

	@Override
	protected void init() {
		layout = new ThreePartsLayoutWidget(this);
		layout.addHeader(this.title, textRenderer);
		chatRulesConfigListWidget = layout.addBody(new ChatRulesConfigListWidget(client, this, width, layout.getContentHeight(), layout.getHeaderHeight()));
		DirectionalLayoutWidget footerLayout = layout.addFooter(new DirectionalLayoutWidget(0, 0, DirectionalLayoutWidget.DisplayAxis.HORIZONTAL));
		footerLayout.getMainPositioner().marginX(5).marginY(2);
		footerLayout.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
			if (client != null) close();
		}).build());
		footerLayout.add(ButtonWidget.builder(Text.translatable("skyblocker.config.chat.chatRules.screen.new"),
				buttonNew -> chatRulesConfigListWidget.addRuleAfterSelected()
		).build());
		footerLayout.add(ButtonWidget.builder(ScreenTexts.DONE, button -> {
			chatRulesConfigListWidget.saveRules();
			if (client != null) {
				close();
			}
		}).build());

		layout.refreshPositions();
		layout.forEachChild(this::addDrawableChild);
	}

	@Override
	protected void refreshWidgetPositions() {
		layout.refreshPositions();
		chatRulesConfigListWidget.setDimensionsAndPosition(width, layout.getContentHeight(), 0, layout.getHeaderHeight());
		chatRulesConfigListWidget.refreshScroll();
	}

	@Override
	public void close() {
		assert client != null;
		if (!chatRulesConfigListWidget.hasChanges()) {
			this.client.setScreen(parent);
			return;
		}
		client.setScreen(new ConfirmScreen(confirmedAction -> {
			if (confirmedAction) {
				this.client.setScreen(parent);
			} else {
				client.setScreen(this);
			}
		}, Text.translatable("text.skyblocker.quit_config"), Text.translatable("text.skyblocker.quit_config_sure"), Text.translatable("text.skyblocker.quit_discard"), ScreenTexts.CANCEL));
	}
}
