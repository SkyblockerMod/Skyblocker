package de.hysky.skyblocker.utils.command.suggestions;

import com.google.common.base.Suppliers;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import de.hysky.skyblocker.utils.command.CommandUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class TextFieldSuggestions extends CommandSuggestions {

	private final CommandDispatcher<ClientSuggestionProvider> dispatcher;
	private final Mode mode;
	private @Nullable CommandContext<ClientSuggestionProvider> context;

	protected TextFieldSuggestions(Minecraft minecraft, Screen screen, EditBox input, Font font, boolean onlyShowIfCursorPastError, int suggestionLineLimit, @Nullable CommandNode<ClientSuggestionProvider> node, boolean commandOnly) {
		super(minecraft, screen, input, font, true, onlyShowIfCursorPastError, 0, suggestionLineLimit, false, ARGB.black(0.5f));
		if (node != null) {
			this.dispatcher = new CommandDispatcher<>();
			this.dispatcher.getRoot().addChild(node);
			this.mode = Mode.CUSTOM_NODE;
		} else {
			Commands.validate();
			this.dispatcher = minecraft.player == null ? CommandUtils.getOfflineCommandDispatcher() : minecraft.player.connection.getCommands();
			this.mode = commandOnly ? Mode.VANILLA_COMMANDS_ONLY : Mode.VANILLA;
		}
	}

	public static TextFieldSuggestions ofSpecificNode(Minecraft minecraft, Screen screen, EditBox input, Font font, boolean onlyShowIfCursorPastError, int suggestionLineLimit, CommandNode<ClientSuggestionProvider> node) {
		return new TextFieldSuggestions(minecraft, screen, input, font, onlyShowIfCursorPastError, suggestionLineLimit, node, true);
	}

	public static TextFieldSuggestions ofVanillaDispatcher(Minecraft minecraft, Screen screen, EditBox input, Font font, boolean onlyShowIfCursorPastError, int suggestionLineLimit, boolean commandOnly) {
		return new TextFieldSuggestions(minecraft, screen, input, font, onlyShowIfCursorPastError, suggestionLineLimit, null, commandOnly);
	}

	@Override
	public void showSuggestions(boolean immediateNarration) {
		super.showSuggestions(immediateNarration);
		updatePosition();
	}

	public final void updatePosition() {
		if (suggestions != null) {
			suggestions.rect = new Rect2i(
					suggestions.rect.getX(),
					input.getY() - suggestions.rect.getHeight(),
					suggestions.rect.getWidth(),
					suggestions.rect.getHeight()
			);
		}
	}

	/**
	 * Overridden to change the position.
	 */
	@Override
	public void extractUsage(GuiGraphicsExtractor graphics) {
		int y = 0;

		for (FormattedCharSequence line : this.commandUsage) {
			int lineY = this.input.getBottom() + 12 * y;
			graphics.fill(commandUsagePosition - 1, lineY, this.commandUsagePosition + this.commandUsageWidth + 1, lineY + 12, this.fillColor);
			graphics.text(this.font, line, this.commandUsagePosition, lineY + 2, -1);
			y++;
		}
	}

	public <V> Optional<V> getArgument(String argument, Class<V> type) {
		return Optional.ofNullable(context).map(c -> c.getArgument(argument, type));
	}

	@Override
	public void updateCommandInfo() {
		String command = this.input.getValue();
		if (this.currentParse != null && !this.currentParse.getReader().getString().equals(command)) {
			this.currentParse = null;
			this.context = null;
		}

		if (!this.keepSuggestions) {
			this.input.setSuggestion(null);
			this.suggestions = null;
		}

		commandUsage.clear();
		StringReader reader = new StringReader(command);
		boolean startsWithSlash = reader.canRead() && reader.peek() == '/' && mode != Mode.CUSTOM_NODE; // do not allow slashes at all with a custom node
		if (startsWithSlash) {
			reader.skip();
		}
		int cursorPosition = this.input.getCursorPosition();
		if (startsWithSlash || mode != Mode.VANILLA) {
			CommandDispatcher<ClientSuggestionProvider> commands = dispatcher;
			if (this.currentParse == null) {
				this.currentParse = commands.parse(reader, minecraft.player != null ? minecraft.player.connection.getSuggestionsProvider() : CommandUtils.getOfflineSuggestionProvider());
				CommandSyntaxException parseException = Commands.getParseException(currentParse);
				if (mode == Mode.CUSTOM_NODE && parseException != null && parseException.getType() == CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument() && command.endsWith(" ")) {
					input.setValue(command.trim());
					return;
				}
				if (currentParse.getExceptions().isEmpty()) this.context = currentParse.getContext().build(command);
			}

			int parseStart = this.onlyShowIfCursorPastError ? reader.getCursor() : 1;
			if (cursorPosition >= parseStart && (this.suggestions == null || !this.keepSuggestions)) {
				this.pendingSuggestions = commands.getCompletionSuggestions(this.currentParse, cursorPosition);
				this.pendingSuggestions.thenAccept(suggestionResult -> {
					if (this.pendingSuggestions.isDone()) {
						this.updateUsageInfo(this.currentParse, suggestionResult);
					}
				});
			}
		}
	}

	/**
	 * Overridden to hide the {@code <argument>} usage hint that shows up if your cursor is at the end
	 */
	@Override
	protected List<FormattedCharSequence> fillNodeUsage(SuggestionContext<ClientSuggestionProvider> suggestionContext, Style usageFormat) {
		return mode != Mode.CUSTOM_NODE ? super.fillNodeUsage(suggestionContext, usageFormat) : List.of();
	}

	private enum Mode {
		VANILLA,
		VANILLA_COMMANDS_ONLY,
		CUSTOM_NODE
	}
}
