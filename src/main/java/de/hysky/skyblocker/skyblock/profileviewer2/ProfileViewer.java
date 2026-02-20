package de.hysky.skyblocker.skyblock.profileviewer2;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import com.mojang.util.UndashedUuid;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ApiProfile;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ApiProfileResponse;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.LenientUuidTypeAdapter;
import de.hysky.skyblocker.utils.ApiUtils;
import de.hysky.skyblocker.utils.ProfileUtils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.item.component.ResolvableProfile;

public class ProfileViewer {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = SkyblockerMod.GSON.newBuilder()
			.registerTypeHierarchyAdapter(UUID.class, new LenientUuidTypeAdapter())
			.create();

	@Init
	public static void init() {
		if (!Debug.debugEnabled()) return;

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, commandContext) -> {
			LiteralArgumentBuilder<FabricClientCommandSource> pvCommandBuilder = ClientCommandManager.literal("pv2")
					.executes(Scheduler.queueOpenScreenFactoryCommand(context -> openProfileViewer(context.getSource(), context.getSource().getClient().getUser().getName())))
					.then(ClientCommandManager.argument("name", StringArgumentType.string())
							.suggests((context, builder) -> SharedSuggestionProvider.suggest(getPlayerSuggestions(context.getSource()), builder))
							.executes(Scheduler.queueOpenScreenFactoryCommand(context -> openProfileViewer(context.getSource(), StringArgumentType.getString(context, "name")))));
			dispatcher.register(pvCommandBuilder);
		});
	}

	/**
	 * Ensures that "dummy" players aren't included in command suggestions
	 */
	private static String[] getPlayerSuggestions(FabricClientCommandSource source) {
		return source.getOnlinePlayerNames().stream()
				.filter(playerName -> playerName.matches("[A-Za-z0-9_]+"))
				.toArray(String[]::new);
	}

	private static Screen openProfileViewer(FabricClientCommandSource source, String name) {
		Minecraft minecraft = source.getClient();
		// In a variable for stylistic purposes, do not change this
		@SuppressWarnings("unused")
		CompletableFuture<Void> dataFuture = loadData(name)
				.thenApplyAsync(pair -> {
					Optional<ApiProfileResponse> apiProfileResponse = pair.left();
					Optional<ApiProfile> selectedProfile = apiProfileResponse.map(ApiProfileResponse::getSelectedProfile);
					Optional<GameProfile> gameProfile = pair.right();
					boolean loadedSkyblockProfile = apiProfileResponse.isPresent();
					boolean hasSkyblockProfile = selectedProfile.isPresent();
					boolean loadedGameProfile = gameProfile.isPresent();

					if (!loadedSkyblockProfile) {
						return new ErrorProfileViewerScreen("Failed to load Skyblock profiles.");
					} else if (!hasSkyblockProfile) {
						return new ErrorProfileViewerScreen("This user has no Skyblock profiles.");
					} else if (!loadedGameProfile) {
						return new ErrorProfileViewerScreen("Player not found.");
					} else {
						// This should never throw since I presume the API cannot return a profile that the user is not a member of (given we request the user's profiles)
						ProfileMember member = Objects.requireNonNull(selectedProfile.get().members.get(gameProfile.get().id()), "profile member must not be null");

						LOGGER.info("[Skyblocker Profile Viewer] Successfully loaded the profile for {}!", name);
						return new ProfileViewerScreen(apiProfileResponse.get(), selectedProfile.get(), gameProfile.get(), member);
					}
				}, minecraft)
				.thenAcceptAsync(minecraft::setScreen, minecraft)
				.exceptionallyAsync(throwable -> {
					LOGGER.error("[Skyblocker Profile Viewer] Encountered an unknown exception when loading the data.", throwable);
					minecraft.setScreen(new ErrorProfileViewerScreen("Encountered an unknown error."));

					return null;
				}, minecraft);

		return new LoadingProfileViewerScreen(name);
	}

	/**
	 * {@return a {@link Pair} optionally containing the user's {@link ApiProfileResponse} and {@link GameProfile}}
	 */
	private static CompletableFuture<Pair<Optional<ApiProfileResponse>, Optional<GameProfile>>> loadData(String name) {
		Minecraft minecraft = Minecraft.getInstance();
		CompletableFuture<Pair<Optional<ApiProfileResponse>, Optional<GameProfile>>> dataFuture = CompletableFuture.supplyAsync(() -> ApiUtils.name2Uuid(name), Executors.newVirtualThreadPerTaskExecutor())
				.thenComposeAsync(uuid -> {
					if (uuid.isEmpty()) {
						return CompletableFuture.failedStage(new IllegalStateException("Invalid username"));
					}

					CompletableFuture<Optional<JsonObject>> skyblockProfileFuture = ProfileUtils.fetchFullProfileByUuid(uuid)
							.thenApply(Optional::ofNullable);
					ResolvableProfile resolvableProfile = ResolvableProfile.createUnresolved(UndashedUuid.fromString(uuid));
					CompletableFuture<Optional<GameProfile>> gameProfileFuture = minecraft.playerSkinRenderCache().lookup(resolvableProfile)
							.thenApply(optional -> optional.map(PlayerSkinRenderCache.RenderInfo::gameProfile));

					return skyblockProfileFuture.thenCombine(gameProfileFuture, Pair::of);
				}, Executors.newVirtualThreadPerTaskExecutor())
				.thenApplyAsync(pair -> Pair.of(pair.left().map(json -> GSON.fromJson(json, ApiProfileResponse.class)), pair.right()), Executors.newVirtualThreadPerTaskExecutor());

		return dataFuture;
	}
}
