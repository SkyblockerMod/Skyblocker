package de.hysky.skyblocker.skyblock.chat;

import de.hysky.skyblocker.utils.scheduler.Scheduler;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

public class ChatRulesConfigListWidget extends ContainerObjectSelectionList<ChatRulesConfigListWidget.AbstractChatRuleEntry> {
	private static final int ROW_HEIGHT = 26;

	private final ChatRulesConfigScreen screen;
	private boolean hasChanged;

	public ChatRulesConfigListWidget(Minecraft client, ChatRulesConfigScreen screen, int width, int height, int y) {
		super(client, width, height, y, ROW_HEIGHT);
		this.screen = screen;
		this.hasChanged = false;

		updateEntries();
	}

	public void updateEntries() {
		clearEntries();
		addEntry(new LabelsEntry());
		for (int i = 0; i < ChatRulesHandler.CHAT_RULE_LIST.getData().size(); i++) {
			addEntry(new ChatRuleEntry(i));
		}
	}

	@Override
	public int getRowWidth() {
		return 320;
	}

	@Override
	protected boolean entriesCanBeSelected() {
		return true;
	}

	protected void addRuleAfterSelected() {
		hasChanged = true;
		int newIndex = Math.max(children().indexOf(getSelected()), 0);

		ChatRulesHandler.CHAT_RULE_LIST.getData().add(newIndex, new ChatRule());
		updateEntries();
		if (newIndex + 1 >= this.children().size()) return;
		AbstractChatRuleEntry entry = this.children().get(newIndex + 1);
		// I hate this
		Scheduler.INSTANCE.schedule(() -> setSelected(entry), 0);
	}

	@Override
	protected void removeEntry(AbstractChatRuleEntry entry) {
		super.removeEntry(entry);
		hasChanged = true;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (super.mouseClicked(click, doubled)) return true;
		setSelected(null);
		return false;
	}

	protected void saveRules() {
		hasChanged = false;
		ChatRulesHandler.saveChatRules();
	}

	protected boolean hasChanges() {
		return (hasChanged || children().stream().anyMatch(AbstractChatRuleEntry::hasChanged));
	}

	protected abstract static class AbstractChatRuleEntry extends ContainerObjectSelectionList.Entry<ChatRulesConfigListWidget.AbstractChatRuleEntry> {
		public boolean hasChanged() {
			return false;
		}
	}

	private class LabelsEntry extends AbstractChatRuleEntry {
		@Override
		public List<? extends NarratableEntry> narratables() {
			return List.of();
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of();
		}

		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
			graphics.centeredText(minecraft.font, Component.translatable("skyblocker.config.chat.chatRules.screen.ruleName"), ChatRulesConfigListWidget.this.getWidth() / 2 - 100, this.getY() + 5, CommonColors.WHITE);
			graphics.centeredText(minecraft.font, Component.translatable("skyblocker.config.chat.chatRules.screen.ruleEnabled"), ChatRulesConfigListWidget.this.getWidth() / 2 - 10, this.getY() + 5, CommonColors.WHITE);
			graphics.centeredText(minecraft.font, Component.translatable("skyblocker.config.chat.chatRules.screen.modify"), ChatRulesConfigListWidget.this.getWidth() / 2 + 77, this.getY() + 5, CommonColors.WHITE);
			graphics.fill(getRowLeft(), getY() + 15, getRowRight(), getY() + 16, CommonColors.LIGHT_GRAY);
		}
	}

	private class ChatRuleEntry extends AbstractChatRuleEntry {
		// Data
		private final int chatRuleIndex;
		private final ChatRule chatRule;

		// Widgets
		private final StringWidget nameWidget;
		private final List<? extends GuiEventListener> children;
		private final LinearLayout layout;

		@Override
		public void setX(int x) {
			super.setX(x);
			layout.setX(x + 125);
			layout.arrangeElements();
		}

		@Override
		public void setY(int y) {
			super.setY(y);
			layout.setY(y);
			layout.arrangeElements();
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
			super.mouseClicked(click, doubled);
			setSelected(this);
			return true;
		}

		private ChatRuleEntry(int chatRuleIndex) {
			this.chatRuleIndex = chatRuleIndex;
			this.chatRule = ChatRulesHandler.CHAT_RULE_LIST.getData().get(chatRuleIndex);

			nameWidget = new StringWidget(Component.literal(chatRule.getName()), minecraft.font);
			nameWidget.setMaxWidth(110, StringWidget.TextOverflow.SCROLLING);
			layout = new LinearLayout(0, 0, LinearLayout.Orientation.HORIZONTAL);
			layout.defaultCellSetting().paddingRight(10).paddingTop(3).alignHorizontallyCenter();

			Checkbox enabledCheck = layout.addChild(Checkbox.builder(Component.empty(), minecraft.font)
					.selected(chatRule.getEnabled())
					.onValueChange((_, value) -> {
						hasChanged = true;
						chatRule.setEnabled(value);
					})
					.build());
			enabledCheck.setWidth(50);

			Button openConfigButton = layout.addChild(Button.builder(Component.translatable("skyblocker.config.chat.chatRules.screen.editRule"), _ -> minecraft.setScreen(new ChatRuleConfigScreen(screen, chatRuleIndex))).size(50, 20).tooltip(Tooltip.create(Component.translatable("skyblocker.config.chat.chatRules.screen.editRule.@Tooltip"))).build());

			Button deleteButton = layout.addChild(Button.builder(Component.translatable("selectServer.delete"), _ ->
				minecraft.setScreen(new ConfirmScreen(this::deleteEntry, Component.translatable("skyblocker.config.chat.chatRules.screen.deleteQuestion"), Component.translatable("skyblocker.config.chat.chatRules.screen.deleteWarning", chatRule.getName()), Component.translatable("selectServer.deleteButton"), CommonComponents.GUI_CANCEL))
			).size(50, 20).build());

			children = List.of(enabledCheck, openConfigButton, deleteButton);
		}

		private void deleteEntry(boolean confirmedAction) {
			if (confirmedAction) {
				ChatRulesHandler.CHAT_RULE_LIST.getData().remove(chatRuleIndex);
				removeEntry(this);
			}

			minecraft.setScreen(screen);
			updateEntries();
		}

		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
			// Text
			nameWidget.setX(getX() + 10);
			nameWidget.setY(getY() + 8);
			nameWidget.extractRenderState(graphics, mouseX, mouseY, a);
			// Widgets
			layout.visitWidgets(child -> child.extractRenderState(graphics, mouseX, mouseY, a));
		}

		@Override
		public boolean hasChanged() {
			return chatRule.getEnabled() != ChatRulesHandler.CHAT_RULE_LIST.getData().get(chatRuleIndex).getEnabled();
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return children;
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return List.of();
		}
	}
}
