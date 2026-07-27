package de.hysky.skyblocker.utils.command.suggestions;

import com.google.common.base.Suppliers;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class TextFieldSuggestions extends CommandSuggestions {

	private final CommandDispatcher<ClientSuggestionProvider> dispatcher;
	private @Nullable CommandContext<ClientSuggestionProvider> context;
	private static final Supplier<ClientSuggestionProvider> FAKE_PROVIDER_SUPPLIER = Suppliers.memoize(FakeClientSuggestionProvider::new);

	public TextFieldSuggestions(Minecraft minecraft, Screen screen, EditBox input, Font font, boolean onlyShowIfCursorPastError, int suggestionLineLimit, CommandNode<ClientSuggestionProvider> node) {
		super(minecraft, screen, input, font, true, onlyShowIfCursorPastError, 0, suggestionLineLimit, false, ARGB.black(0.5f));
		this.dispatcher = new CommandDispatcher<>();
		this.dispatcher.getRoot().addChild(node);
	}

	public TextFieldSuggestions(Screen screen, EditBox input, boolean onlyShowIfCursorPastError, int suggestionLineLimit, CommandNode<ClientSuggestionProvider> node) {
		this(Minecraft.getInstance(), screen, input, Minecraft.getInstance().font, onlyShowIfCursorPastError, suggestionLineLimit, node);
	}

	public static CommandBuildContext getContext() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return CommandBuildContext.simple(VanillaRegistries.createLookup(), FeatureFlagSet.of());
		return CommandBuildContext.simple(player.connection.registryAccess(), player.connection.enabledFeatures());
	}

	@Override
	public void showSuggestions(boolean immediateNarration) {
		super.showSuggestions(immediateNarration);
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
		int cursorPosition = this.input.getCursorPosition();
		CommandDispatcher<ClientSuggestionProvider> commands = dispatcher;
		if (this.currentParse == null) {
			this.currentParse = commands.parse(reader, minecraft.player != null ? minecraft.player.connection.getSuggestionsProvider() : FAKE_PROVIDER_SUPPLIER.get());
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

	/**
	 * Overridden to hide the {@code <argument>} usage hint that shows up if your cursor is at the end
	 */
	@Override
	protected List<FormattedCharSequence> fillNodeUsage(SuggestionContext<ClientSuggestionProvider> suggestionContext, Style usageFormat) {
		return List.of();
	}

	private static class FakeClientSuggestionProvider extends ClientSuggestionProvider {
		private final RegistryAccess registryAccess;

		public FakeClientSuggestionProvider() {
			// maybe dangerous? either that or 20 new imports to create a fake connection
			super(null, Minecraft.getInstance(), PermissionSet.NO_PERMISSIONS);
			this.registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
		}

		@Override
		public Set<ResourceKey<Level>> levels() {
			return Set.of();
		}

		@Override
		public Collection<String> getOnlinePlayerNames() {
			return List.of();
		}

		@Override
		public RegistryAccess registryAccess() {
			return registryAccess;
		}

		@Override
		public FeatureFlagSet enabledFeatures() {
			return FeatureFlagSet.of();
		}

		@Override
		public Collection<String> getAllTeams() {
			return List.of();
		}

		@Override
		public CompletableFuture<Suggestions> customSuggestion(CommandContext<?> context) {
			return Suggestions.empty();
		}
	}
}
