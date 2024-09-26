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
import de.hysky.skyblocker.utils.ApiUtils;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.ProfileUtils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.client.gui.screen.ingame.InventoryScreen.drawEntity;

public class ProfileViewerScreen extends Screen {
    public static final Logger LOGGER = LoggerFactory.getLogger(ProfileViewerScreen.class);
    private static final Text TITLE = Text.of("Skyblocker Profile Viewer");
    private static final String HYPIXEL_COLLECTIONS = "https://api.hypixel.net/v2/resources/skyblock/collections";
    private static final Identifier TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/base_plate.png");
    private static final int GUI_WIDTH = 322;
    private static final int GUI_HEIGHT = 180;
    private static Map<String, String[]> COLLECTIONS;
    private static Map<String, IntList> TIER_REQUIREMENTS;

    private String playerName;
    private JsonObject hypixelProfile;
    private JsonObject playerProfile;
    private boolean profileNotFound = false;

    private int activePage = 0;
    private static final String[] PAGE_NAMES = {"Skills", "Slayers", "Dungeons", "Inventories", "Collections"};
    private final ProfileViewerPage[] profileViewerPages = new ProfileViewerPage[PAGE_NAMES.length];
    private final List<ProfileViewerNavButton> profileViewerNavButtons = new ArrayList<>();
    private OtherClientPlayerEntity entity;
    private ProfileViewerTextWidget textWidget;

    public ProfileViewerScreen(String username) {
        super(TITLE);
        fetchPlayerData(username).thenRun(this::initialisePagesAndWidgets);

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
                        clearAndInit();
                    }
                });
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        synchronized (this) {
            super.render(context, mouseX, mouseY, delta);
        }

        int rootX = width / 2 - GUI_WIDTH / 2;
        int rootY = height / 2 - GUI_HEIGHT / 2 + 5;

        context.drawTexture(TEXTURE, rootX, rootY, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
        for (ProfileViewerNavButton button : profileViewerNavButtons) {
            button.setX(rootX + button.getIndex() * 28 + 4);
            button.setY(rootY - 28);
            button.render(context, mouseX, mouseY, delta);
        }


        if (textWidget != null) textWidget.render(context, textRenderer, rootX + 8, rootY + 120);
        drawPlayerEntity(context, playerName != null ? playerName : "Loading...", rootX, rootY, mouseX, mouseY);

        if (profileViewerPages[activePage] != null) {
            profileViewerPages[activePage].markWidgetsAsVisible();
            profileViewerPages[activePage].render(context, mouseX, mouseY, delta, rootX + 93, rootY + 7);
        } else {
            context.drawText(textRenderer, profileNotFound ? "No Profile" : "Loading...", rootX + 180, rootY + 80, Color.WHITE.getRGB(), true);
        }
    }

    private void drawPlayerEntity(DrawContext context, String username, int rootX, int rootY, int mouseX, int mouseY) {
        if (entity != null)
            drawEntity(context, rootX + 9, rootY + 16, rootX + 89, rootY + 124, 42, 0.0625F, mouseX, mouseY, entity);
        context.drawCenteredTextWithShadow(textRenderer, username.length() > 15 ? username.substring(0, 15) : username, rootX + 47, rootY + 14, Color.WHITE.getRGB());
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
                this.profileNotFound = true;
                LOGGER.warn("[Skyblocker Profile Viewer] Error while looking for profile", e);
            }
        });

        CompletableFuture<Void> playerFuture = CompletableFuture.runAsync(() -> {
    		String stringifiedUuid = ApiUtils.name2Uuid(username);

    		if (stringifiedUuid.isEmpty()) {
                this.playerName = "User not found";
                this.profileNotFound = true;
    		}

    		UUID uuid = UndashedUuid.fromStringLenient(stringifiedUuid);

    		//The fetch by name method can sometimes fail in weird cases and return a fake offline player
    		SkullBlockEntity.fetchProfileByUuid(uuid).thenAccept(profile -> {
                this.playerName = profile.get().getName();
                entity = new OtherClientPlayerEntity(MinecraftClient.getInstance().world, profile.get()) {
                    @Override
                    public SkinTextures getSkinTextures() {
                        PlayerListEntry playerListEntry = new PlayerListEntry(profile.get(), false);
                        return playerListEntry.getSkinTextures();
                    }

                    @Override
                    public boolean isPartVisible(PlayerModelPart modelPart) {
                        return !(modelPart.getName().equals(PlayerModelPart.CAPE.getName()));
                    }

                    @Override
                    public boolean isInvisibleTo(PlayerEntity player) {
                        return true;
                    }
                };
                entity.setCustomNameVisible(false);
    		}).exceptionally(ex -> {
                this.playerName = "User not found";
                this.profileNotFound = true;
                return null;
            }).join();
    	});

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
        profileViewerNavButtons.forEach(this::addDrawableChild);
        for (ProfileViewerPage profileViewerPage : profileViewerPages) {
            if (profileViewerPage != null && profileViewerPage.getButtons() != null) {
                for (ClickableWidget button : profileViewerPage.getButtons()) {
                    if (button != null) addDrawableChild(button);
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
                            .suggests((source, builder) -> CommandSource.suggestMatching(getPlayerSuggestions(source.getSource()), builder))
                            .executes(Scheduler.queueOpenScreenFactoryCommand(context -> new ProfileViewerScreen(StringArgumentType.getString(context, "username"))))
                    )
                    .executes(Scheduler.queueOpenScreenCommand(() -> new ProfileViewerScreen(MinecraftClient.getInstance().getSession().getUsername())));
            dispatcher.register(literalArgumentBuilder);
            dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(literalArgumentBuilder));
        });
    }

    private static void fetchCollectionsData() {
        CompletableFuture.runAsync(() -> {
            try {
                JsonObject jsonObject = JsonParser.parseString(Http.sendGetRequest(HYPIXEL_COLLECTIONS)).getAsJsonObject();
                if (jsonObject.get("success").getAsBoolean()) {
                    Map<String, String[]> collectionsMap = new HashMap<>();
                    Map<String, IntList> tierRequirementsMap = new HashMap<>();
                    JsonObject collections = jsonObject.getAsJsonObject("collections");
                    collections.entrySet().forEach(entry -> {
                        String category = entry.getKey();
                        JsonObject itemsObject = entry.getValue().getAsJsonObject().getAsJsonObject("items");
                        String[] items = itemsObject.keySet().toArray(new String[0]);
                        collectionsMap.put(category, items);
                        itemsObject.entrySet().forEach(itemEntry -> {
                            IntImmutableList tierReqs = IntImmutableList.toList(itemEntry.getValue().getAsJsonObject().getAsJsonArray("tiers").asList().stream()
                                    .mapToInt(tier -> tier.getAsJsonObject().get("amountRequired").getAsInt())
                            );
                            tierRequirementsMap.put(itemEntry.getKey(), tierReqs);
                        });
                    });
                    COLLECTIONS = collectionsMap;
                    TIER_REQUIREMENTS = tierRequirementsMap;
                }
            } catch (Exception e) {
                LOGGER.error("[Skyblocker Profile Viewer] Failed to fetch collections data", e);
            }
        });
    }

    public static Map<String, String[]> getCollections() {
        return COLLECTIONS;
    }

    public static Map<String, IntList> getTierRequirements() {
        return TIER_REQUIREMENTS;
    }

    /**
     * Ensures that "dummy" players aren't included in command suggestions
     */
    private static String[] getPlayerSuggestions(FabricClientCommandSource source) {
        return source.getPlayerNames().stream().filter(playerName -> playerName.matches("[A-Za-z0-9_]+")).toArray(String[]::new);
    }
}
