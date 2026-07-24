package de.hysky.skyblocker.utils.render.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import de.hysky.skyblocker.utils.command.suggestions.TextFieldSuggestions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class AutocompleteEditBox extends EditBox {

	private final TextFieldSuggestions suggestions;
	private final boolean autoTrim;
	private final @Nullable String argument;
	private @Nullable Consumer<String> responder;

	public AutocompleteEditBox(Minecraft minecraft, Screen screen, Font font, int width, int height, Component narration, boolean onlyShowIfCursorPastError, int suggestionLineLimit, CommandNode<ClientSuggestionProvider> node, boolean autoTrim) {
		super(font, width, height, narration);
		this.suggestions = new TextFieldSuggestions(minecraft, screen, this, font, onlyShowIfCursorPastError, suggestionLineLimit, node);
		this.autoTrim = autoTrim;
		suggestions.setAllowSuggestions(true);
		super.setResponder(this::onUpdate);
		if (node instanceof ArgumentCommandNode<?,?> argumentCommandNode) argument = argumentCommandNode.getName();
		else argument = null;
	}

	@Override
	public void setResponder(Consumer<String> responder) {
		this.responder = responder;
	}

	public boolean isValid() {
		return suggestions.currentParse != null && suggestions.currentParse.getExceptions().isEmpty();
	}

	private void onUpdate(String string) {
		if (autoTrim && !string.trim().equals(string)) setValue(string.trim());
		else {
			suggestions.updateCommandInfo();
			if (responder != null) responder.accept(string);
		}
	}

	public <V> Optional<V> getParsedValue(Class<V> type) {
		return argument == null ? Optional.empty() : getParsedValue(argument, type);
	}

	public <V> Optional<V> getParsedValue(String argument, Class<V> type) {
		return suggestions.getArgument(argument, type);
	}


	@Override
	public void setFocused(boolean focused) {
		super.setFocused(focused);
		if (!focused) suggestions.hide();
		else suggestions.updateCommandInfo();
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return super.isMouseOver(mouseX, mouseY) || (suggestions.suggestions != null && suggestions.suggestions.rect.contains((int) mouseX, (int) mouseY));
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		if (suggestions.mouseScrolled(scrollY)) return true;
		return super.mouseScrolled(x, y, scrollX, scrollY);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (suggestions.mouseClicked(event)) return true;
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
		if (visible) suggestions.extractRenderState(graphics, mouseX, mouseY);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (suggestions.keyPressed(event)) return true;
		if (this.isActive() && this.isFocused() && event.key() == InputConstants.KEY_ESCAPE) {
			setFocused(false);
			return true;
		}
		return super.keyPressed(event);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private int width = 150;
		private int height = 20;
		private int suggestionLineLimit = 7;
		private boolean onlyShowIfCursorPastError = true;
		private boolean autoTrim = true;

		public Builder width(int width) {
			this.width = width;
			return this;
		}

		public Builder height(int height) {
			this.height = height;
			return this;
		}

		public Builder suggestionLineLimit(int suggestionLineLimit) {
			this.suggestionLineLimit = suggestionLineLimit;
			return this;
		}

		public Builder onlyShowIfCursorPastError(boolean onlyShowIfCursorPastError) {
			this.onlyShowIfCursorPastError = onlyShowIfCursorPastError;
			return this;
		}

		public Builder autoTrim(boolean autoTrim) {
			this.autoTrim = autoTrim;
			return this;
		}

		public AutocompleteEditBox build(Screen screen, Component narration, CommandNode<ClientSuggestionProvider> node) {
			return build(Minecraft.getInstance(), Minecraft.getInstance().font,  screen, narration, node);
		}

		public AutocompleteEditBox build(Minecraft minecraft, Font font, Screen screen, Component narration, ArgumentBuilder<ClientSuggestionProvider, ?> builder) {
			return build(minecraft, font, screen, narration, builder.build());
		}

		public <T> Argument<T> buildArg(Minecraft minecraft, Font font, Screen screen, Component narration, ArgumentType<T> argumentType) {
			return new Argument<>(minecraft, screen, font, width, height, narration, onlyShowIfCursorPastError, suggestionLineLimit, argumentType, autoTrim);
		}

		public AutocompleteEditBox build(Minecraft minecraft, Font font, Screen screen, Component narration, CommandNode<ClientSuggestionProvider> node) {
			return new AutocompleteEditBox(minecraft, screen, font, width, height, narration, onlyShowIfCursorPastError, suggestionLineLimit, node, autoTrim);
		}
	}

	public static class Argument<T> extends AutocompleteEditBox {

		public Argument(Minecraft minecraft, Screen screen, Font font, int width, int height, Component narration, boolean onlyShowIfCursorPastError, int suggestionLineLimit, ArgumentType<T> argumentType, boolean autoTrim) {
			super(minecraft, screen, font, width, height, narration, onlyShowIfCursorPastError, suggestionLineLimit, RequiredArgumentBuilder.<ClientSuggestionProvider, T>argument("argument", argumentType).build(), autoTrim);

		}

		public Optional<T> getParsedValue() {
			return (Optional<T>) getParsedValue(Object.class);
		}

		public void setValueResponder(Consumer<T> valueResponder) {
			setResponder(_ -> getParsedValue().ifPresent(valueResponder));
		}

		public void setOptionalValueResponder(Consumer<@Nullable T> optionalValueResponder) {
			setResponder(_ -> optionalValueResponder.accept(getParsedValue().orElse(null)));
		}
	}
}
