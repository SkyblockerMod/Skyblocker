package de.hysky.skyblocker.skyblock.chat;

import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.awt.Color;
import java.util.List;

public class ChatRulesConfigListWidget extends ElementListWidget<ChatRulesConfigListWidget.AbstractChatRuleEntry> {
	private static final int ROW_HEIGHT = 26;

	private final ChatRulesConfigScreen screen;
	private boolean hasChanged;

	public ChatRulesConfigListWidget(MinecraftClient client, ChatRulesConfigScreen screen, int width, int height, int y) {
		super(client, width, height, y, ROW_HEIGHT);
		this.screen = screen;
		this.hasChanged = false;

		updateEntries();
	}

	public void updateEntries() {
		clearEntries();
		addEntry(new LabelsEntry());
		for (int i = 0; i < ChatRulesHandler.chatRuleList.getData().size(); i++) {
			addEntry(new ChatRuleEntry(i));
		}
	}

	@Override
	public int getRowWidth() {
		return 320;
	}

	protected void addRuleAfterSelected() {
		hasChanged = true;
		int newIndex = Math.max(children().indexOf(getSelectedOrNull()), 0);

		ChatRulesHandler.chatRuleList.getData().add(newIndex, new ChatRule());
		children().add(newIndex + 1, new ChatRuleEntry(newIndex));
		if (newIndex + 1 >= this.children().size()) return;
		AbstractChatRuleEntry entry = this.children().get(newIndex + 1);
		// I hate this
		Scheduler.INSTANCE.schedule(() -> setSelected(entry), 0);
	}

	@Override
	protected boolean removeEntry(AbstractChatRuleEntry entry) {
		boolean bl = super.removeEntry(entry);
		hasChanged = bl;
		return bl;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) return true;
		setSelected(null);
		return false;
	}

	@Override
	protected boolean isSelectedEntry(int index) {
		if (children().size() <= index) return false;
		return this.getSelectedOrNull() == this.children().get(index);
	}

	protected void saveRules() {
		hasChanged = false;
		ChatRulesHandler.saveChatRules();
	}

	protected boolean hasChanges() {
		return (hasChanged || children().stream().anyMatch(AbstractChatRuleEntry::hasChanged));
	}

	protected abstract static class AbstractChatRuleEntry extends ElementListWidget.Entry<ChatRulesConfigListWidget.AbstractChatRuleEntry> {
		public boolean hasChanged() {
			return false;
		}
	}

	private class LabelsEntry extends AbstractChatRuleEntry {
		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of();
		}

		@Override
		public List<? extends Element> children() {
			return List.of();
		}

		@Override
		public void render(DrawContext context, int index, int eY, int eX, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleName"), width / 2 - 100, eY + 5, Colors.WHITE);
			context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.ruleEnabled"), width / 2 - 10, eY + 5, Colors.WHITE);
			context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("skyblocker.config.chat.chatRules.screen.modify"), width / 2 + 77, eY + 5, Colors.WHITE);
			context.fill(getRowLeft(), eY + 15, getRowRight(), eY + 16, Colors.LIGHT_GRAY);
		}
	}

	private class ChatRuleEntry extends AbstractChatRuleEntry {
		// Data
		private final int chatRuleIndex;
		private final ChatRule chatRule;

		// Widgets
		private final List<? extends Element> children;
		private final DirectionalLayoutWidget layout;

		private int x;
		private int y;

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			super.mouseClicked(mouseX, mouseY, button);
			setSelected(this);
			return true;
		}

		private ChatRuleEntry(int chatRuleIndex) {
			this.chatRuleIndex = chatRuleIndex;
			this.chatRule = ChatRulesHandler.chatRuleList.getData().get(chatRuleIndex);

			layout = new DirectionalLayoutWidget(0, 0, DirectionalLayoutWidget.DisplayAxis.HORIZONTAL);
			layout.getMainPositioner().marginRight(10);
			layout.getMainPositioner().marginTop(1);

			ButtonWidget enabledButton = layout.add(ButtonWidget.builder(enabledButtonText(), this::toggleEnabled).size(50, 20).build());

			ButtonWidget openConfigButton = layout.add(ButtonWidget.builder(Text.translatable("skyblocker.config.chat.chatRules.screen.editRule"), a -> client.setScreen(new ChatRuleConfigScreen(screen, chatRuleIndex))).size(50, 20).tooltip(Tooltip.of(Text.translatable("skyblocker.config.chat.chatRules.screen.editRule.@Tooltip"))).build());

			ButtonWidget deleteButton = layout.add(ButtonWidget.builder(Text.translatable("selectServer.delete"), a ->
				client.setScreen(new ConfirmScreen(this::deleteEntry, Text.translatable("skyblocker.config.chat.chatRules.screen.deleteQuestion"), Text.translatable("skyblocker.config.chat.chatRules.screen.deleteWarning", chatRule.getName()), Text.translatable("selectServer.deleteButton"), ScreenTexts.CANCEL))
			).size(50, 20).build());

			children = List.of(enabledButton, openConfigButton, deleteButton);
		}

		private Text enabledButtonText() {
			if (chatRule.getEnabled()) {
				return Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.true").withColor(Color.GREEN.getRGB());
			} else {
				return Text.translatable("skyblocker.config.chat.chatRules.screen.ruleScreen.false").withColor(Color.RED.getRGB());
			}
		}
		private void toggleEnabled(ButtonWidget button) {
			hasChanged = true;
			chatRule.setEnabled(!chatRule.getEnabled());
			button.setMessage(enabledButtonText());
		}

		private void deleteEntry(boolean confirmedAction) {
			if (confirmedAction) {
				ChatRulesHandler.chatRuleList.getData().remove(chatRuleIndex);
				removeEntry(this);
			}

			client.setScreen(screen);
			updateEntries();
		}

		@Override
		public void render(DrawContext context, int index, int eY, int eX, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			if (this.y != eY || this.x != eX) {
				this.x = eX;
				this.y = eY;
				layout.setPosition(eX + 125, eY);
				layout.refreshPositions();
			}
			// Widgets
			layout.forEachChild((child) -> child.render(context, mouseX, mouseY, tickDelta));
			// Text
			context.drawCenteredTextWithShadow(client.textRenderer, chatRule.getName(), eX + 60, eY + 7, Colors.WHITE);
		}

		@Override
		public boolean hasChanged() {
			return chatRule.getEnabled() != ChatRulesHandler.chatRuleList.getData().get(chatRuleIndex).getEnabled();
		}

		@Override
		public List<? extends Element> children() {
			return children;
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of();
		}
	}
}
