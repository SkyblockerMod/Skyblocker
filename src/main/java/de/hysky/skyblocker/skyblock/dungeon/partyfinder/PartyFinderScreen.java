package de.hysky.skyblocker.skyblock.dungeon.partyfinder;

import com.google.gson.JsonObject;
import com.mojang.authlib.properties.PropertyMap;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.ItemUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PartyFinderScreen extends Screen {
    protected static final Logger LOGGER = LoggerFactory.getLogger(PartyFinderScreen.class);
    protected static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("social_interactions/background");
    protected static final Identifier SEARCH_ICON_TEXTURE = Identifier.ofVanilla("icon/search");
    protected static final Text SEARCH_TEXT = Text.translatable("gui.socialInteractions.search_hint").formatted(Formatting.ITALIC).formatted(Formatting.GRAY);
    public static boolean isInKuudraPartyFinder = false;

    public static boolean DEBUG = false;
    public static final List<String> possibleInventoryNames = List.of(
            "party finder",
            "search settings",
            "select floor",
            "select type",
            "class level range",
            "dungeon level range",
            "sort"
    );

    public GenericContainerScreenHandler getHandler() {
        return handler;
    }

    private Text name;
    private GenericContainerScreenHandler handler;
    private final PlayerInventory inventory;
    private Page currentPage;
    public PartyEntryListWidget partyEntryListWidget;

    public FinderSettingsContainer getSettingsContainer() {
        return settingsContainer;
    }

    private FinderSettingsContainer settingsContainer;

    private int refreshSlotId = -1;

    private TextFieldWidget searchField;
    private ButtonWidget refreshButton;

    private ButtonWidget previousPageButton;
    private int prevPageSlotId = -1;

    private ButtonWidget nextPageButton;
    private int nextPageSlotId = -1;

    protected ButtonWidget partyFinderButton;
    protected int partyButtonSlotId = -1;

    private ButtonWidget settingsButton;
    private int settingsButtonSlotId = -1;

    private ButtonWidget createPartyButton;
    private int createPartyButtonSlotId = -1;

    private boolean dirty = false;
    private long dirtiedTime;
    public boolean justOpenedSign = false;

    public void markDirty() {
        if (justOpenedSign) return;
        dirtiedTime = System.currentTimeMillis();
        dirty = true;
    }

    private boolean waitingForServer = false;

    public static Map<String, PropertyMap> floorIconsNormal = null;
    public static Map<String, PropertyMap> floorIconsMaster = null;

    @Init
    public static void initClass() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            //Checking when this is loaded probably isn't necessary as the maps are always null checked
            CompletableFuture.runAsync(() -> {
                floorIconsNormal = new HashMap<>();
                floorIconsMaster = new HashMap<>();
                try (BufferedReader skullTextureReader = client.getResourceManager().openAsReader(Identifier.of(SkyblockerMod.NAMESPACE, "dungeons/catacombs/floorskulls.json"))) {
                    JsonObject json = SkyblockerMod.GSON.fromJson(skullTextureReader, JsonObject.class);
                    json.getAsJsonObject("normal").asMap().forEach((s, tex) -> floorIconsNormal.put(s, ItemUtils.propertyMapWithTexture(tex.getAsString())));
                    json.getAsJsonObject("master").asMap().forEach((s, tex) -> floorIconsMaster.put(s, ItemUtils.propertyMapWithTexture(tex.getAsString())));
                    LOGGER.debug("[Skyblocker] Dungeons floor skull textures json loaded");
                } catch (Exception e) {
                    LOGGER.error("[Skyblocker] Failed to load dungeons floor skull textures json", e);
                }
            });
        });
    }

    public PartyFinderScreen(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(title);
        this.handler = handler;
        this.inventory = inventory;
        name = title;
    }

    @Override
    protected void init() {
        super.init();
        int topRowButtonsHeight = 20;

        // Entry list widget, pretty much every position is based on this guy since it centers automagically
        int widget_height = (int) (this.height * 0.8);
        int entryListTopY = Math.max(43, (int) (height * 0.1));
        this.partyEntryListWidget = new PartyEntryListWidget(client, width, widget_height, entryListTopY, 68);

        // Search field
        this.searchField = new TextFieldWidget(textRenderer, partyEntryListWidget.getRowLeft() + 12, entryListTopY - 12, partyEntryListWidget.getRowWidth() - 12 * 3 - 6, 12, Text.literal("Search..."));
        searchField.setPlaceholder(SEARCH_TEXT);
        searchField.setChangedListener(s -> partyEntryListWidget.setSearch(s));
        // Refresh button
        refreshButton = ButtonWidget.builder(Text.literal("⟳").setStyle(Style.EMPTY.withColor(Formatting.GREEN)), (a) -> {
                    if (refreshSlotId != -1) {
                        clickAndWaitForServer(refreshSlotId);
                    }
                })
                .position(searchField.getX() + searchField.getWidth() + 12 * 2, searchField.getY())
                .size(12, 12).build();
        refreshButton.active = false;

        // Prev and next page buttons
        previousPageButton = ButtonWidget.builder(Text.literal("←"), (a) -> {
                    if (prevPageSlotId != -1) {
                        clickAndWaitForServer(prevPageSlotId);
                    }
                })
                .position(searchField.getX() + searchField.getWidth(), searchField.getY())
                .size(12, 12).build();
        previousPageButton.active = false;
        nextPageButton = ButtonWidget.builder(Text.literal("→"), (a) -> {
                    if (nextPageSlotId != -1) {
                        clickAndWaitForServer(nextPageSlotId);
                    }
                })
                .position(searchField.getX() + searchField.getWidth() + 12, searchField.getY())
                .size(12, 12).build();
        nextPageButton.active = false;

        // Settings container
        if (this.settingsContainer == null) this.settingsContainer = new FinderSettingsContainer(partyEntryListWidget.getRowLeft(), entryListTopY - 12, widget_height + 12);
        else settingsContainer.setDimensionsAndPosition(partyEntryListWidget.getRowWidth() - 2, widget_height + 12, partyEntryListWidget.getRowLeft(), entryListTopY - 12);


        // Buttons at the top
        int searchButtonMargin = 2;
        int searchButtonWidth = (partyEntryListWidget.getRowWidth() + 6) / 3 - 2 * searchButtonMargin;


        partyFinderButton = ButtonWidget.builder(Text.translatable("skyblocker.partyFinder.tabs.partyFinder"), (a) -> {
                    if (partyButtonSlotId != -1) {
                        setCurrentPage(Page.FINDER);
                        clickAndWaitForServer(partyButtonSlotId);
                    }
                })
                .position(partyEntryListWidget.getRowLeft(), entryListTopY - 39)
                .size(searchButtonWidth + searchButtonMargin, topRowButtonsHeight).build();

        settingsButton = ButtonWidget.builder(Text.translatable("skyblocker.partyFinder.tabs.searchSettings"), (a) -> {
                    if (settingsButtonSlotId != -1) {
                        setCurrentPage(Page.SETTINGS);
                        clickAndWaitForServer(settingsButtonSlotId);
                    }
                })
                .position(partyEntryListWidget.getRowLeft() + searchButtonWidth + 3 * searchButtonMargin, entryListTopY - 39)
                .size(searchButtonWidth, topRowButtonsHeight).build();

        createPartyButton = ButtonWidget.builder(Text.translatable("skyblocker.partyFinder.tabs.createParty"), (a) -> {
                    if (createPartyButtonSlotId != -1) {
                        clickAndWaitForServer(createPartyButtonSlotId);
                    }
                })
                .position(partyEntryListWidget.getRowLeft() + searchButtonWidth * 2 + 5 * searchButtonMargin, entryListTopY - 39)
                .size(searchButtonWidth, topRowButtonsHeight).build();
        createPartyButton.active = false;


        addDrawableChild(partyEntryListWidget);
        addDrawableChild(searchField);
        addDrawableChild(refreshButton);
        addDrawableChild(previousPageButton);
        addDrawableChild(nextPageButton);
        addDrawableChild(partyFinderButton);
        addDrawableChild(settingsButton);
        addDrawableChild(createPartyButton);
        addDrawableChild(settingsContainer);
        addDrawableChild(ButtonWidget.builder(Text.of("DEBUG"), (a) -> DEBUG = !DEBUG).dimensions(width - 40, 0, 40, 20).build());

        dirtiedTime = System.currentTimeMillis();


        // Used when resizing
        setCurrentPage(currentPage);

        if (currentPage != Page.SIGN) update();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (searchField.visible) {
            context.drawGuiTexture(SEARCH_ICON_TEXTURE, partyEntryListWidget.getRowLeft() + 1, searchField.getY() + 1, 10, 10);
        }
        if (DEBUG) {
            context.drawText(textRenderer, "Truly a party finder", 20, 20, 0xFFFFFFFF, true);
            context.drawText(textRenderer, currentPage.toString(), 0, 0, 0xFFFFFFFF, true);
            context.drawText(textRenderer, String.valueOf(refreshSlotId), width - 25, 30, 0xFFFFFFFF, true);
            context.drawText(textRenderer, String.valueOf(prevPageSlotId), width - 25, 40, 0xFFFFFFFF, true);
            context.drawText(textRenderer, String.valueOf(nextPageSlotId), width - 25, 50, 0xFFFFFFFF, true);
            for (int i = 0; i < handler.slots.size(); i++) {
                context.drawItem(handler.slots.get(i).getStack(), (i % 9) * 16, (i / 9) * 16);
            }
        }
        if (isWaitingForServer()) {
            String s = "Waiting for server...";
            context.drawText(textRenderer, s, this.width - textRenderer.getWidth(s) - 5, this.height - textRenderer.fontHeight - 2, 0xFFFFFFFF, true);
        }
        if (!settingsContainer.canInteract(null)) {
            context.fill(0, 0, width, height, 50, 0x40000000);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        int i = partyEntryListWidget.getRowWidth() + 16 + 6;
        context.drawGuiTexture(BACKGROUND_TEXTURE, partyEntryListWidget.getRowLeft() - 8, partyEntryListWidget.getY() - 12 - 8, i, partyEntryListWidget.getBottom() - partyEntryListWidget.getY() + 16 + 12);
    }

    @Override
    public void close() {
        assert this.client != null;
        assert this.client.player != null;
        if (currentPage != Page.SIGN)
            this.client.player.closeHandledScreen();
        else {
            ClientPlayNetworkHandler networkHandler = this.client.getNetworkHandler();
            if (networkHandler != null && sign != null) {
                List<String> originalText = Arrays.stream(sign.getText(signFront).getMessages(true)).map(Text::getString).toList();
                networkHandler.sendPacket(new UpdateSignC2SPacket(sign.getPos(), signFront, originalText.getFirst(), originalText.get(1), originalText.get(2), originalText.get(3)));
            }
        }
        super.close();
    }

    public void setCurrentPage(Page page) {
        this.currentPage = page;
        if (page == Page.FINDER) {
            partyEntryListWidget.visible = true;

            partyFinderButton.active = false;
            partyFinderButton.setMessage(partyFinderButton.getMessage().copy().setStyle(Style.EMPTY.withUnderline(true)));
            settingsButton.active = true;
            settingsButton.setMessage(settingsButton.getMessage().copy().setStyle(Style.EMPTY.withUnderline(false)));
            createPartyButton.active = true;

            searchField.active = true;
            searchField.visible = true;
            settingsContainer.setVisible(false);
            refreshButton.visible = true;
            previousPageButton.visible = true;
            nextPageButton.visible = true;
        } else if (page == Page.SETTINGS || page == Page.SIGN) {
            partyEntryListWidget.visible = false;

            partyFinderButton.active = page != Page.SIGN;
            partyFinderButton.setMessage(partyFinderButton.getMessage().copy().setStyle(Style.EMPTY.withUnderline(false)));
            settingsButton.active = false;
            settingsButton.setMessage(settingsButton.getMessage().copy().setStyle(Style.EMPTY.withUnderline(true)));
            createPartyButton.active = false;

            searchField.active = false;
            searchField.visible = false;
            settingsContainer.setVisible(true);
            refreshButton.visible = false;
            previousPageButton.visible = false;
            nextPageButton.visible = false;
        }
    }

    // Called when the handler object/title gets changed
    public void updateHandler(GenericContainerScreenHandler handler, Text name) {
        this.handler = handler;
        this.name = name;
        markDirty();
    }

    public boolean isSignFront() {
        return signFront;
    }

    public @Nullable SignBlockEntity getSign() {
        return sign;
    }

    private boolean signFront = true;
    private @Nullable SignBlockEntity sign = null;

    public void updateSign(SignBlockEntity sign, boolean front) {
        setCurrentPage(Page.SIGN);
        signFront = front;
        this.sign = sign;
        justOpenedSign = true;
        waitingForServer = false;
        if (!settingsContainer.handleSign(sign, front)) abort();
    }

    public void update() {
        dirty = false;
        waitingForServer = false;
        String titleText = name.getString();
        if (titleText.contains("Party Finder")) {
            updatePartyFinderPage();
        } else {
            if (currentPage != Page.SETTINGS) setCurrentPage(Page.SETTINGS);
            if (!this.settingsContainer.handle(this, titleText)) {
                abort();
            }
        }

    }

    private void updatePartyFinderPage() {
        previousPageButton.active = false;
        nextPageButton.active = false;
        List<PartyEntry> parties = new ArrayList<>();
        if (currentPage != Page.FINDER) setCurrentPage(Page.FINDER);
        if (handler.slots.stream().anyMatch(slot -> slot.hasStack() && slot.getStack().isOf(Items.BEDROCK))) {
            parties.add(new PartyEntry.NoParties());
        } else {
            for (Slot slot : handler.slots) {
                if (slot.id > (handler.getRows() - 1) * 9 - 1 || !slot.hasStack()) continue;
                if (slot.getStack().isOf(Items.PLAYER_HEAD)) {
                    assert this.client != null;
                    parties.add(new PartyEntry(ItemUtils.getLore(slot.getStack()), this, slot.id));
                } else if (slot.getStack().isOf(Items.ARROW) && slot.getStack().getName().getString().toLowerCase().contains("previous")) {
                    prevPageSlotId = slot.id;
                    previousPageButton.active = true;
                } else if (slot.getStack().isOf(Items.ARROW) && slot.getStack().getName().getString().toLowerCase().contains("next")) {
                    nextPageSlotId = slot.id;
                    nextPageButton.active = true;
                }
            }
        }
        int deListSlotId = -1;
        List<Text> tooltips = null;
        for (int i = (handler.getRows() - 1) * 9; i < handler.getRows() * 9; i++) {
            Slot slot = handler.slots.get(i);
            if (!slot.hasStack()) continue;
            if (slot.getStack().isOf(Items.EMERALD_BLOCK)) {
                refreshSlotId = slot.id;
                refreshButton.active = true;
            } else if (slot.getStack().isOf(Items.REDSTONE_BLOCK)) {
                createPartyButtonSlotId = slot.id;
                createPartyButton.active = true;
            } else if (slot.getStack().isOf(Items.NETHER_STAR)) {
                settingsButtonSlotId = slot.id;
                if (DEBUG)
                    settingsButton.setMessage(settingsButton.getMessage().copy().append(Text.of(" " + settingsButtonSlotId)));
            } else if (slot.getStack().isOf(Items.BOOKSHELF)) {
                deListSlotId = slot.id;
            } else if (slot.getStack().isOf(Items.PLAYER_HEAD)) {
                assert this.client != null;
                tooltips = new ArrayList<>(ItemUtils.getLore(slot.getStack()));
            }
        }
        if (tooltips != null) {
            //LOGGER.info("Your Party tooltips");
            //tooltips.forEach(text -> LOGGER.info(text.toString()));
            if (deListSlotId != -1) {
                // Such a wacky thing lol
                tooltips.set(0, Text.literal(MinecraftClient.getInstance().getSession().getUsername() + "'s party"));
            }
            parties.add(new PartyEntry.YourParty(tooltips, this, deListSlotId));
        }
        this.partyEntryListWidget.setEntries(parties);
        //List<ItemStack> temp = handler.slots.stream().map(Slot::getStack).toList();//for (int i = 0; i < temp.size(); i++) System.out.println(i + " " + temp.get(i).toString() + " " + temp.get(i).getName().getString());

    }

    private boolean aborted = false;

    public boolean isAborted() {
        return aborted;
    }

    public void abort() {
        assert this.client != null;
        if (currentPage == Page.SIGN) {
            assert this.client.player != null;
            this.client.player.openEditSignScreen(sign, signFront);
        } else this.client.setScreen(new GenericContainerScreen(handler, inventory, title));
        this.client.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable("skyblocker.partyFinder.error.name"), Text.translatable("skyblocker.partyFinder.error.message")));
        aborted = true;
    }

    @Override
    public void removed() {
        assert this.client != null;
        if (this.client.player == null || aborted || currentPage == Page.SIGN) {
            return;
        }
        ((ScreenHandler) this.handler).onClosed(this.client.player);
    }

    @Override
    public final void tick() {
        super.tick();
        // Slight delay to make sure all slots are received, because they are most of the time sent one at a time
        if (dirty && System.currentTimeMillis() - dirtiedTime > 60) update();
        assert this.client != null && this.client.player != null;
        if (!this.client.player.isAlive() || this.client.player.isRemoved() && currentPage != Page.SIGN) {
            this.client.player.closeHandledScreen();
        }
    }

    public void clickAndWaitForServer(int slotID) {
        //System.out.println("hey");
        assert client != null;
        assert client.interactionManager != null;
        client.interactionManager.clickSlot(handler.syncId, slotID, 0, SlotActionType.PICKUP, client.player);
        waitingForServer = true;
    }

    public boolean isWaitingForServer() {
        return waitingForServer;
    }

    public @NotNull MinecraftClient getClient() {
        assert this.client != null;
        return this.client;
    }

    public enum Page {
        FINDER,
        SETTINGS,
        SIGN
    }
}
