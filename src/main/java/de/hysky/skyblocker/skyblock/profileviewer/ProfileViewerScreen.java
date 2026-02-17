package de.hysky.skyblocker.skyblock.profileviewer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.util.UndashedUuid;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.profileviewer.collections.CollectionsPage;
import de.hysky.skyblocker.skyblock.profileviewer.dungeons.DungeonsPage;
import de.hysky.skyblocker.skyblock.profileviewer.inventory.InventoryPage;
import de.hysky.skyblocker.skyblock.profileviewer.skills.SkillsPage;
import de.hysky.skyblocker.skyblock.profileviewer.slayers.SlayersPage;
import de.hysky.skyblocker.skyblock.profileviewer.utils.Collection;
import de.hysky.skyblocker.utils.ApiAuthentication;
import de.hysky.skyblocker.utils.ApiUtils;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.ProfileUtils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static net.minecraft.client.gui.screens.inventory.InventoryScreen.renderEntityInInventoryFollowsMouse;

public class ProfileViewerScreen extends Screen {
	public static final Logger LOGGER = LoggerFactory.getLogger(ProfileViewerScreen.class);
	private static final Component TITLE = Component.nullToEmpty("Skyblocker Profile Viewer");
	private static final String HYPIXEL_COLLECTIONS = "https://api.hypixel.net/v2/resources/skyblock/collections";
	private static final Identifier TEXTURE = SkyblockerMod.id("textures/gui/profile_viewer/base_plate.png");
	private static final int GUI_WIDTH = 322;
	private static final int GUI_HEIGHT = 189;
	private static Map<String, List<Collection>> collections = Map.of();

	private String playerName;
	private JsonObject hypixelProfile;
	private JsonObject playerProfile;
	private boolean profileNotFound = false;
	private String errorMessage = "No Profile";

	private int activePage = 0;
	private static final String[] PAGE_NAMES = {"Skills", "Slayers", "Dungeons", "Inventories", "Collections"};
	private final ProfileViewerPage[] profileViewerPages = new ProfileViewerPage[PAGE_NAMES.length];
	private final List<ProfileViewerNavButton> profileViewerNavButtons = new ArrayList<>();
	private @Nullable RemotePlayer entity;
	private ProfileViewerTextWidget textWidget;

	public ProfileViewerScreen(String username) {
		super(TITLE);
		fetchPlayerData(username).thenRun(this::initialisePagesAndWidgets).exceptionally(err -> {
			LOGGER.error("[Skyblocker Profile Viewer] An error occurred while fetching player data!", err);
			errorMessage = "Unable to get player data!";
			profileNotFound = true;
			return null;
		});

		for (int i = 0; i < PAGE_NAMES.length; i++) {
			profileViewerNavButtons.add(new ProfileViewerNavButton(this, PAGE_NAMES[i], i, i == 0));
		}
	}

	private void initialisePagesAndWidgets() {
		if (profileNotFound) return;

		textWidget = new ProfileViewerTextWidget(hypixelProfile, playerProfile);

		CompletableFuture<Void> skillsFuture = CompletableFuture.runAsync(() -> profileViewerPages[0] = new SkillsPage(hypixelProfile, playerProfile));
		CompletableFuture<Void> slayersFuture = CompletableFuture.runAsync(() -> profileViewerPages[1] = new SlayersPage(playerProfile));
		CompletableFuture<Void> dungeonsFuture = CompletableFuture.runAsync(() -> profileViewerPages[2] = new DungeonsPage(playerProfile));
		CompletableFuture<Void> inventoriesFuture = CompletableFuture.runAsync(() -> profileViewerPages[3] = new InventoryPage(playerProfile));
		CompletableFuture<Void> collectionsFuture = CompletableFuture.runAsync(() -> profileViewerPages[4] = new CollectionsPage(hypixelProfile, playerProfile));

		CompletableFuture.allOf(skillsFuture, slayersFuture, dungeonsFuture, inventoriesFuture, collectionsFuture)
				.thenRun(() -> {
					synchronized (this) {
						rebuildWidgets();
					}
				}).exceptionally(err -> {
					LOGGER.error("[Skyblocker Profile Viewer] An error occurred while initializing widgets!", err);
					errorMessage = "Unable to process player data!";
					profileNotFound = true;
					return null;
				});
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		synchronized (this) {
			super.render(context, mouseX, mouseY, delta);
		}

		int rootX = width / 2 - GUI_WIDTH / 2;
		int rootY = height / 2 - GUI_HEIGHT / 2 + 5;

		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, rootX, rootY, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
		for (ProfileViewerNavButton button : profileViewerNavButtons) {
			button.setX(rootX + button.getIndex() * 28 + 4);
			button.setY(rootY - 28);
			button.render(context, mouseX, mouseY, delta);
		}


		if (textWidget != null) textWidget.render(context, font, rootX + 8, rootY + 120, mouseX, mouseY);
		drawPlayerEntity(context, playerName != null ? playerName : "Loading...", rootX, rootY, mouseX, mouseY);

		if (profileViewerPages[activePage] != null) {
			profileViewerPages[activePage].markWidgetsAsVisible();
			profileViewerPages[activePage].render(context, mouseX, mouseY, delta, rootX + 93, rootY + 7);
		} else {
			context.drawCenteredString(font, profileNotFound ? errorMessage : "Loading...", rootX + 200, rootY + 80, Color.WHITE.getRGB());
		}
	}

	private void drawPlayerEntity(GuiGraphics context, String username, int rootX, int rootY, int mouseX, int mouseY) {
		if (entity != null)
			renderEntityInInventoryFollowsMouse(context, rootX + 9, rootY + 16, rootX + 89, rootY + 124, 42, 0.0625F, mouseX, mouseY, entity);
		context.drawCenteredString(font, username.length() > 15 ? username.substring(0, 15) : username, rootX + 47, rootY + 14, Color.WHITE.getRGB());
	}

	private CompletableFuture<Void> fetchPlayerData(String username) {
		CompletableFuture<Void> profileFuture = ProfileUtils.fetchFullProfile(username).thenAccept(profiles -> {
			try {
				Optional<JsonObject> selectedProfile = profiles.getAsJsonArray("profiles").asList().stream()
						.map(JsonElement::getAsJsonObject)
						.filter(profile -> profile.getAsJsonPrimitive("selected").getAsBoolean())
						.findFirst();

				if (selectedProfile.isPresent()) {
					this.hypixelProfile = selectedProfile.get();
					this.playerProfile = hypixelProfile.getAsJsonObject("members").get(ApiUtils.name2Uuid(username)).getAsJsonObject();
				}
			} catch (Exception e) {
				this.errorMessage = ApiAuthentication.getToken() == null ? "Invalid Skyblocker token" : "Skyblock profile not found";
				this.profileNotFound = true;
				LOGGER.warn("[Skyblocker Profile Viewer] Error while looking for profile", e);
			}
		});

		CompletableFuture<Void> playerFuture = CompletableFuture.runAsync(() -> {
			String stringifiedUuid = ApiUtils.name2Uuid(username);

			if (stringifiedUuid.isEmpty()) {
				// "Player not found" doesn't fit on the screen lol
				this.playerName = "User not found";
				this.errorMessage = "Player UUID not found";
				this.profileNotFound = true;
			}

			UUID uuid = UndashedUuid.fromStringLenient(stringifiedUuid);

			//The fetch by name method can sometimes fail in weird cases and return a fake offline player
			Minecraft.getInstance().playerSkinRenderCache().lookup(ResolvableProfile.createUnresolved(uuid)).thenAccept(entry -> {
				this.playerName = entry.get().gameProfile().name();
				entity = new RemotePlayer(Minecraft.getInstance().level, entry.get().gameProfile()) {
					@Override
					public PlayerSkin getSkin() {
						PlayerInfo playerListEntry = new PlayerInfo(entry.get().gameProfile(), false);
						return playerListEntry.getSkin();
					}

					@Override
					public boolean isModelPartShown(PlayerModelPart modelPart) {
						return !(modelPart.equals(PlayerModelPart.CAPE));
					}

					@Override
					public boolean isInvisibleTo(Player player) {
						return true;
					}
				};
				entity.setCustomNameVisible(false);
			}).exceptionally(ex -> {
				// "Player not found" doesn't fit on the screen lol
				this.playerName = "User not found";
				this.errorMessage = "Player skin not found";
				this.profileNotFound = true;
				return null;
			}).join();
		}, Executors.newVirtualThreadPerTaskExecutor());

		return CompletableFuture.allOf(profileFuture, playerFuture);
	}

	public void onNavButtonClick(ProfileViewerNavButton clickedButton) {
		if (profileViewerPages[activePage] != null) profileViewerPages[activePage].markWidgetsAsInvisible();
		for (ProfileViewerNavButton button : profileViewerNavButtons) {
			button.setToggled(false);
		}
		activePage = clickedButton.getIndex();
		clickedButton.setToggled(true);
	}

	@Override
	public void init() {
		profileViewerNavButtons.forEach(this::addRenderableWidget);
		for (ProfileViewerPage profileViewerPage : profileViewerPages) {
			if (profileViewerPage != null && profileViewerPage.getButtons() != null) {
				for (AbstractWidget button : profileViewerPage.getButtons()) {
					if (button != null) addRenderableWidget(button);
				}
			}
		}
	}

	@Init
	public static void initClass() {
		fetchCollectionsData(); // caching on launch

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			LiteralArgumentBuilder<FabricClientCommandSource> literalArgumentBuilder = ClientCommandManager.literal("pv")
					.then(ClientCommandManager.argument("username", StringArgumentType.string())
							.suggests((source, builder) -> SharedSuggestionProvider.suggest(getPlayerSuggestions(source.getSource()), builder))
							.executes(Scheduler.queueOpenScreenFactoryCommand(context -> new ProfileViewerScreen(StringArgumentType.getString(context, "username"))))
					)
					.executes(Scheduler.queueOpenScreenCommand(() -> new ProfileViewerScreen(Minecraft.getInstance().getUser().getName())));
			dispatcher.register(literalArgumentBuilder);
			dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(literalArgumentBuilder));
		});
	}

	private static void fetchCollectionsData() {
		CompletableFuture.runAsync(() -> {
			try {
				JsonObject jsonObject = JsonParser.parseString(Http.sendGetRequest(HYPIXEL_COLLECTIONS)).getAsJsonObject();
				collections = Collection.parse(jsonObject);
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Profile Viewer] Failed to fetch collections data", e);
			}
		}, Executors.newVirtualThreadPerTaskExecutor());
	}

	public static Map<String, List<Collection>> getCollections() {
		return collections;
	}

	/**
	 * Ensures that "dummy" players aren't included in command suggestions
	 */
	private static String[] getPlayerSuggestions(FabricClientCommandSource source) {
		return source.getOnlinePlayerNames().stream().filter(playerName -> playerName.matches("[A-Za-z0-9_]+")).toArray(String[]::new);
	}
}
