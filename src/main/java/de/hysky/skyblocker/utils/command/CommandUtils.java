package de.hysky.skyblocker.utils.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.RootCommandNode;
import de.hysky.skyblocker.config.configs.QuickNavigationConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class CommandUtils {
	private static @Nullable CommandDispatcher<ClientSuggestionProvider> offlineCommandDispatcher;
	private static @Nullable ClientSuggestionProvider offlineSuggestionProvider;
	public static final Command<FabricClientCommandSource> noOp = _ -> -1;

	public static CommandBuildContext newContext() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return CommandBuildContext.simple(VanillaRegistries.createLookup(), FeatureFlagSet.of());
		return CommandBuildContext.simple(player.connection.registryAccess(), player.connection.enabledFeatures());
	}

	public static CommandDispatcher<ClientSuggestionProvider> getOfflineCommandDispatcher() {
		if (offlineCommandDispatcher == null) {
			CommandBuildContext context = newContext();
			RootCommandNode<ClientSuggestionProvider> root = unsafeCast(new Commands(Commands.CommandSelection.ALL, context).getDispatcher().getRoot());
			OfflineCommandDispatcher<ClientSuggestionProvider> dispatcher = new OfflineCommandDispatcher<>(root);
			ClientCommandRegistrationCallback.EVENT.invoker().register(unsafeCast(dispatcher), context);
			offlineCommandDispatcher = dispatcher;
		}
		return offlineCommandDispatcher;
	}

	public static ClientSuggestionProvider getOfflineSuggestionProvider() {
		if (offlineSuggestionProvider == null) {
			offlineSuggestionProvider = new FakeClientSuggestionProvider();
		}
		return offlineSuggestionProvider;
	}

	private static <A, B> A unsafeCast(B b) {
		return (A) b;
	}



	private static class FakeClientSuggestionProvider extends ClientSuggestionProvider {
		private final RegistryAccess registryAccess;

		private FakeClientSuggestionProvider() {
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

	private static class OfflineCommandDispatcher<S> extends CommandDispatcher<S> {
		public OfflineCommandDispatcher(RootCommandNode<S> root) {
			super(root);
		}
		@Override
		public int execute(ParseResults<S> parse) {
			throw new UnsupportedOperationException();
		}
	}
}
