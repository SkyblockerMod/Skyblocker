package de.hysky.skyblocker.skyblock.profileviewer2;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import com.mojang.util.UndashedUuid;
import org.slf4j.Logger;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.item.component.ResolvableProfile;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ApiProfile;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ApiProfileResponse;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.EliteLeaderboards;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.LenientUuidTypeAdapter;
import de.hysky.skyblocker.utils.ApiUtils;
import de.hysky.skyblocker.utils.ProfileUtils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;

public class ProfileViewer {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = SkyblockerMod.GSON.newBuilder()
			.registerTypeHierarchyAdapter(UUID.class, new LenientUuidTypeAdapter())
			.create();
	public static final boolean ENABLED = Debug.debugEnabled();

	@Init
	public static void init() {
		if (!ENABLED) return;

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> {
			LiteralArgumentBuilder<FabricClientCommandSource> pvCommandBuilder = ClientCommands.literal("pv2")
					.executes(Scheduler.queueOpenScreenFactoryCommand(context -> openProfileViewer(context.getSource(), context.getSource().getClient().getUser().getName())))
					.then(ClientCommands.argument("name", StringArgumentType.string())
							.suggests((context, builder) -> SharedSuggestionProvider.suggest(getPlayerSuggestions(context.getSource()), builder))
							.executes(Scheduler.queueOpenScreenFactoryCommand(context -> openProfileViewer(context.getSource(), StringArgumentType.getString(context, "name")))));
			dispatcher.register(pvCommandBuilder);
		});
	}

	/// Ensures that "dummy" players aren't included in command suggestions
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
				.thenApplyAsync(loadedData -> {
					Optional<ApiProfileResponse> apiProfileResponse = loadedData.apiProfileResponse();
					Optional<ApiProfile> selectedProfile = apiProfileResponse.map(ApiProfileResponse::getSelectedProfile);
					Optional<GameProfile> gameProfile = loadedData.gameProfile();
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
						return new ProfileViewerScreen(apiProfileResponse.get(), selectedProfile.get(), gameProfile.get(), member, loadedData.leaderboards());
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

	/// {@return a {@link Pair} optionally containing the user's {@link ApiProfileResponse} and {@link GameProfile}}
	private static CompletableFuture<LoadedData> loadData(String name) {
		Minecraft minecraft = Minecraft.getInstance();
		return CompletableFuture.supplyAsync(() -> {
			String uuid = ApiUtils.name2Uuid(name);
			if (uuid.isEmpty()) {
				throw new IllegalStateException("Invalid username");
			}

			// Load skyblock profiles
			CompletableFuture<Optional<ApiProfileResponse>> skyblockProfileFuture = ProfileUtils.fetchFullProfileByUuid(uuid)
					.thenApply(Optional::ofNullable)
					.thenApply(opt -> opt.map(json -> GSON.fromJson(json, ApiProfileResponse.class)));

			// Load game profile
			ResolvableProfile resolvableProfile = ResolvableProfile.createUnresolved(UndashedUuid.fromString(uuid));
			CompletableFuture<Optional<GameProfile>> gameProfileFuture = minecraft.playerSkinRenderCache().lookup(resolvableProfile)
					.thenApply(optional -> optional.map(PlayerSkinRenderCache.RenderInfo::gameProfile));

			// Load leaderboards for current profile
			CompletableFuture<Map<String, Integer>> leaderboards = skyblockProfileFuture
					.thenCompose(apiProfileResponse -> apiProfileResponse.map(ApiProfileResponse::getSelectedProfile)
							.map(profile -> EliteLeaderboards.fetchPlayerLeaderboards(uuid, profile.profileId))
							.orElse(CompletableFuture.completedFuture(Map.of()))
							);

			return new LoadedData(skyblockProfileFuture.join(), leaderboards.join(), gameProfileFuture.join());
		}, SkyblockerMod.VIRTUAL_THREAD_EXECUTOR);
	}

	private record LoadedData(Optional<ApiProfileResponse> apiProfileResponse, Map<String, Integer> leaderboards, Optional<GameProfile> gameProfile) {}
}
