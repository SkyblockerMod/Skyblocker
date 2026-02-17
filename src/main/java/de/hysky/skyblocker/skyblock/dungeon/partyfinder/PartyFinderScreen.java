package de.hysky.skyblocker.skyblock.dungeon.partyfinder;

import com.google.gson.JsonObject;
import com.mojang.authlib.properties.PropertyMap;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.utils.ItemUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class PartyFinderScreen extends Screen {
	protected static final Logger LOGGER = LoggerFactory.getLogger(PartyFinderScreen.class);
	protected static final Identifier BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("social_interactions/background");
	protected static final Identifier SEARCH_ICON_TEXTURE = Identifier.withDefaultNamespace("icon/search");
	protected static final Component SEARCH_TEXT = Component.translatable("gui.socialInteractions.search_hint").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);
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

	public ChestMenu getHandler() {
		return handler;
	}

	private Component name;
	private ChestMenu handler;
	private final Inventory inventory;
	private Page currentPage;
	public PartyEntryListWidget partyEntryListWidget;

	public FinderSettingsContainer getSettingsContainer() {
		return settingsContainer;
	}

	private FinderSettingsContainer settingsContainer;

	private int refreshSlotId = -1;

	private EditBox searchField;
	private Button refreshButton;

	private Button previousPageButton;
	private int prevPageSlotId = -1;

	private Button nextPageButton;
	private int nextPageSlotId = -1;

	protected Button partyFinderButton;
	protected int partyButtonSlotId = -1;

	private Button settingsButton;
	private int settingsButtonSlotId = -1;

	private Button createPartyButton;
	private int createPartyButtonSlotId = -1;

	private boolean dirty = false;
	private boolean resetScroll = false;
	private long dirtiedTime;

	public void markDirty() {
		if (sign != null) return;
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
				try (BufferedReader skullTextureReader = client.getResourceManager().openAsReader(SkyblockerMod.id("dungeons/catacombs/floorskulls.json"))) {
					JsonObject json = SkyblockerMod.GSON.fromJson(skullTextureReader, JsonObject.class);
					json.getAsJsonObject("normal").asMap().forEach((s, tex) -> floorIconsNormal.put(s, ItemUtils.propertyMapWithTexture(tex.getAsString())));
					json.getAsJsonObject("master").asMap().forEach((s, tex) -> floorIconsMaster.put(s, ItemUtils.propertyMapWithTexture(tex.getAsString())));
					LOGGER.debug("[Skyblocker] Dungeons floor skull textures json loaded");
				} catch (Exception e) {
					LOGGER.error("[Skyblocker] Failed to load dungeons floor skull textures json", e);
				}
			}, Executors.newVirtualThreadPerTaskExecutor());
		});
	}

	public PartyFinderScreen(ChestMenu handler, Inventory inventory, Component title) {
		super(title);
		this.handler = handler;
		this.inventory = inventory;
		name = title;
	}

	@Override
	protected void init() {
		super.init();
		int topRowButtonsHeight = 20;

		// AbstractEntry list widget, pretty much every position is based on this guy since it centers automagically
		int widget_height = (int) (this.height * 0.8);
		int entryListTopY = Math.max(43, (int) (height * 0.1));
		this.partyEntryListWidget = new PartyEntryListWidget(minecraft, width, widget_height, entryListTopY, 68);

		// Search field
		this.searchField = new EditBox(font, partyEntryListWidget.getRowLeft() + 12, entryListTopY - 12, partyEntryListWidget.getRowWidth() - 12 * 3 - 6, 12, Component.literal("Search..."));
		searchField.setHint(SEARCH_TEXT);
		searchField.setResponder(s -> partyEntryListWidget.setSearch(s));
		// Refresh button
		refreshButton = Button.builder(Component.literal("⟳").setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)), (a) -> {
					if (refreshSlotId != -1) {
						clickAndWaitForServer(refreshSlotId);
						resetScroll = true;
					}
				})
				.pos(searchField.getX() + searchField.getWidth() + 12 * 2, searchField.getY())
				.size(12, 12).build();
		refreshButton.active = false;

		// Prev and next page buttons
		previousPageButton = Button.builder(Component.literal("←"), (a) -> {
					if (prevPageSlotId != -1) {
						clickAndWaitForServer(prevPageSlotId);
						resetScroll = true;
					}
				})
				.pos(searchField.getX() + searchField.getWidth(), searchField.getY())
				.size(12, 12).build();
		previousPageButton.active = false;
		nextPageButton = Button.builder(Component.literal("→"), (a) -> {
					if (nextPageSlotId != -1) {
						clickAndWaitForServer(nextPageSlotId);
						resetScroll = true;
					}
				})
				.pos(searchField.getX() + searchField.getWidth() + 12, searchField.getY())
				.size(12, 12).build();
		nextPageButton.active = false;

		// Settings container
		if (this.settingsContainer == null) this.settingsContainer = new FinderSettingsContainer(partyEntryListWidget.getRowLeft(), entryListTopY - 12, widget_height + 12);
		else settingsContainer.setRectangle(partyEntryListWidget.getRowWidth() - 2, widget_height + 12, partyEntryListWidget.getRowLeft(), entryListTopY - 12);


		// Buttons at the top
		int searchButtonMargin = 2;
		int searchButtonWidth = (partyEntryListWidget.getRowWidth() + 6) / 3 - 2 * searchButtonMargin;


		partyFinderButton = Button.builder(Component.translatable("skyblocker.partyFinder.tabs.partyFinder"), (a) -> {
					if (partyButtonSlotId != -1) {
						setCurrentPage(Page.FINDER);
						clickAndWaitForServer(partyButtonSlotId);
					}
				})
				.pos(partyEntryListWidget.getRowLeft(), entryListTopY - 39)
				.size(searchButtonWidth + searchButtonMargin, topRowButtonsHeight).build();

		settingsButton = Button.builder(Component.translatable("skyblocker.partyFinder.tabs.searchSettings"), (a) -> {
					if (settingsButtonSlotId != -1) {
						setCurrentPage(Page.SETTINGS);
						clickAndWaitForServer(settingsButtonSlotId);
					}
				})
				.pos(partyEntryListWidget.getRowLeft() + searchButtonWidth + 3 * searchButtonMargin, entryListTopY - 39)
				.size(searchButtonWidth, topRowButtonsHeight).build();

		createPartyButton = Button.builder(Component.translatable("skyblocker.partyFinder.tabs.createParty"), (a) -> {
					if (createPartyButtonSlotId != -1) {
						clickAndWaitForServer(createPartyButtonSlotId);
					}
				})
				.pos(partyEntryListWidget.getRowLeft() + searchButtonWidth * 2 + 5 * searchButtonMargin, entryListTopY - 39)
				.size(searchButtonWidth, topRowButtonsHeight).build();
		createPartyButton.active = false;


		addRenderableWidget(partyEntryListWidget);
		addRenderableWidget(searchField);
		addRenderableWidget(refreshButton);
		addRenderableWidget(previousPageButton);
		addRenderableWidget(nextPageButton);
		addRenderableWidget(partyFinderButton);
		addRenderableWidget(settingsButton);
		addRenderableWidget(createPartyButton);
		addRenderableWidget(settingsContainer);
		if (Debug.debugEnabled()) {
			addRenderableWidget(Button.builder(Component.nullToEmpty("DEBUG"), (a) -> DEBUG = !DEBUG).bounds(width - 40, 0, 40, 20).build());
		}

		dirtiedTime = System.currentTimeMillis();


		// Used when resizing
		setCurrentPage(currentPage);

		if (currentPage != Page.SIGN) update();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		if (!settingsContainer.canInteract(null)) {
			context.fill(0, 0, width, height, 0x40000000);
		}
		super.render(context, mouseX, mouseY, delta);

		if (searchField.visible) {
			context.blitSprite(RenderPipelines.GUI_TEXTURED, SEARCH_ICON_TEXTURE, partyEntryListWidget.getRowLeft() + 1, searchField.getY() + 1, 10, 10);
		}
		if (DEBUG) {
			context.drawString(font, currentPage.toString(), 0, 0, CommonColors.WHITE, true);
			context.drawString(font, "Truly a party finder", 20, 20, CommonColors.WHITE, true);
			if (sign != null) {
				context.drawString(font, "You are in a sign btw", 20, 30, CommonColors.WHITE, true);
			} else {
				context.drawString(font, String.valueOf(refreshSlotId), width - 25, 30, CommonColors.WHITE, true);
				context.drawString(font, String.valueOf(prevPageSlotId), width - 25, 40, CommonColors.WHITE, true);
				context.drawString(font, String.valueOf(nextPageSlotId), width - 25, 50, CommonColors.WHITE, true);
				for (int i = 0; i < handler.slots.size(); i++) {
					context.renderItem(handler.slots.get(i).getItem(), (i % 9) * 16, (i / 9) * 16);
				}
				context.drawString(font, String.valueOf(settingsButtonSlotId), settingsButton.getX() + settingsButton.getWidth() / 2, Math.max(0, settingsButton.getY() - 8), CommonColors.WHITE, true);
			}
		}
		if (isWaitingForServer()) {
			String s = "Waiting for server...";
			context.drawString(font, s, this.width - font.width(s) - 5, this.height - font.lineHeight - 2, CommonColors.WHITE, true);
		}
	}

	@Override
	public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
		this.renderTransparentBackground(context);
		int i = partyEntryListWidget.getRowWidth() + 16 + 6;
		context.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, partyEntryListWidget.getRowLeft() - 8, partyEntryListWidget.getY() - 12 - 8, i, partyEntryListWidget.getBottom() - partyEntryListWidget.getY() + 16 + 12);
	}

	@Override
	public void onClose() {
		assert this.minecraft != null;
		assert this.minecraft.player != null;
		if (currentPage != Page.SIGN)
			this.minecraft.player.closeContainer();
		else {
			ClientPacketListener networkHandler = this.minecraft.getConnection();
			if (networkHandler != null && sign != null) {
				List<String> originalText = Arrays.stream(sign.getText(signFront).getMessages(true)).map(Component::getString).toList();
				networkHandler.send(new ServerboundSignUpdatePacket(sign.getBlockPos(), signFront, originalText.getFirst(), originalText.get(1), originalText.get(2), originalText.get(3)));
			}
		}
		super.onClose();
	}

	public void setCurrentPage(Page page) {
		this.currentPage = page;
		if (page == Page.FINDER) {
			partyEntryListWidget.visible = true;

			partyFinderButton.active = false;
			partyFinderButton.setMessage(partyFinderButton.getMessage().copy().setStyle(Style.EMPTY.withUnderlined(true)));
			settingsButton.active = true;
			settingsButton.setMessage(settingsButton.getMessage().copy().setStyle(Style.EMPTY.withUnderlined(false)));
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
			partyFinderButton.setMessage(partyFinderButton.getMessage().copy().setStyle(Style.EMPTY.withUnderlined(false)));
			settingsButton.active = false;
			settingsButton.setMessage(settingsButton.getMessage().copy().setStyle(Style.EMPTY.withUnderlined(true)));
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
	public void updateHandler(ChestMenu handler, Component name) {
		this.handler = handler;
		this.name = name;
		closedSign();
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
		waitingForServer = false;
		if (!settingsContainer.handleSign(sign, front)) abort();
	}

	public void closedSign() {
		this.sign = null;
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

	@SuppressWarnings("deprecation")
	private void updatePartyFinderPage() {
		previousPageButton.active = false;
		nextPageButton.active = false;
		List<PartyEntry> parties = new ArrayList<>();

		if (currentPage != Page.FINDER) setCurrentPage(Page.FINDER);
		if (handler.slots.stream().anyMatch(slot -> slot.hasItem() && slot.getItem().is(Items.BEDROCK))) {
			parties.add(new PartyEntry.NoParties());
		} else {
			for (Slot slot : handler.slots) {
				if (slot.index > (handler.getRowCount() - 1) * 9 - 1 || !slot.hasItem()) continue;
				ItemStack stack = slot.getItem();
				if (stack.is(Items.PLAYER_HEAD)) {
					assert this.minecraft != null;
					parties.add(new PartyEntry(stack.getHoverName(), ItemUtils.getLore(stack), this, slot.index));
				} else if (stack.is(Items.ARROW) && stack.getHoverName().getString().toLowerCase(Locale.ENGLISH).contains("previous")) {
					prevPageSlotId = slot.index;
					previousPageButton.active = true;
				} else if (stack.is(Items.ARROW) && stack.getHoverName().getString().toLowerCase(Locale.ENGLISH).contains("next")) {
					nextPageSlotId = slot.index;
					nextPageButton.active = true;
				}
			}
		}

		ItemStack yourPartyStack = null;
		int deListSlotId = -1;
		for (int i = (handler.getRowCount() - 1) * 9; i < handler.getRowCount() * 9; i++) {
			Slot slot = handler.slots.get(i);
			if (!slot.hasItem()) continue;
			if (slot.getItem().is(Items.EMERALD_BLOCK)) {
				refreshSlotId = slot.index;
				refreshButton.active = true;
			} else if (slot.getItem().is(Items.REDSTONE_BLOCK)) {
				createPartyButtonSlotId = slot.index;
				createPartyButton.active = true;
			} else if (slot.getItem().is(Items.NETHER_STAR)) {
				settingsButtonSlotId = slot.index;
			} else if (slot.getItem().is(Items.BOOKSHELF)) {
				deListSlotId = slot.index;
			} else if (slot.getItem().is(Items.PLAYER_HEAD)) {
				assert this.minecraft != null;
				yourPartyStack = slot.getItem();
			}
		}

		assert minecraft != null;
		String playerName = minecraft.getUser().getName();

		// It's possible for the party to show up in the search results before it does next to the delist button.
		// This means that it will have the "You are in this party" error text, but it'll still be possible to click and delist.
		if (yourPartyStack == null && deListSlotId != -1) {
			yourPartyStack = parties.stream()
					.filter(party -> party.partyLeader != null
							&& party.partyLeader.name.getString().equals(playerName)).findFirst()
					.map(party -> party.slotID)
					.map(slotId -> handler.slots.get(slotId))
					.map(Slot::getItem).orElse(null);
		}

		if (yourPartyStack != null) {
			Component title = yourPartyStack.getHoverName();
			if (deListSlotId != -1) {
				// Such a wacky thing lol
				title = Component.literal(playerName + "'s party");
			}
			// Remove the party if it's already in the list from the search results
			parties.removeIf(party -> party.partyLeader != null
					&& party.partyLeader.name.getString().equals((playerName)));
			parties.add(new PartyEntry.YourParty(title, ItemUtils.getLore(yourPartyStack), this, deListSlotId));
		}
		this.partyEntryListWidget.setEntries(parties);

		if (resetScroll) {
			resetScroll = false;
			partyEntryListWidget.setScrollAmount(0);
		}
	}

	private boolean aborted = false;

	public boolean isAborted() {
		return aborted;
	}

	public void abort() {
		assert this.minecraft != null;
		if (currentPage == Page.SIGN) {
			assert this.minecraft.player != null;
			this.minecraft.player.openTextEdit(sign, signFront);
		} else this.minecraft.setScreen(new ContainerScreen(handler, inventory, title));
		this.minecraft.getToastManager().addToast(new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.translatable("skyblocker.partyFinder.error.name"), Component.translatable("skyblocker.partyFinder.error.message")));
		aborted = true;
	}

	@Override
	public void removed() {
		assert this.minecraft != null;
		if (this.minecraft.player == null || aborted || currentPage == Page.SIGN) {
			return;
		}
		this.handler.removed(this.minecraft.player);
	}

	@Override
	public final void tick() {
		super.tick();
		// Slight delay to make sure all slots are received, because they are most of the time sent one at a time
		if (dirty && System.currentTimeMillis() - dirtiedTime > 60) update();
		assert this.minecraft != null && this.minecraft.player != null;
		if (!this.minecraft.player.isAlive() || this.minecraft.player.isRemoved() && currentPage != Page.SIGN) {
			this.minecraft.player.closeContainer();
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (settingsContainer != null && settingsContainer.hasOpenOption()) {
			return settingsContainer.mouseClicked(click, doubled);
		}
		return super.mouseClicked(click, doubled);
	}

	public void clickAndWaitForServer(int slotID) {
		//System.out.println("hey");
		assert minecraft != null;
		assert minecraft.gameMode != null;
		minecraft.gameMode.handleInventoryMouseClick(handler.containerId, slotID, 0, ClickType.PICKUP, minecraft.player);
		waitingForServer = true;
	}

	public boolean isWaitingForServer() {
		return waitingForServer;
	}

	public Minecraft getClient() {
		assert this.minecraft != null;
		return this.minecraft;
	}

	public enum Page {
		FINDER,
		SETTINGS,
		SIGN
	}
}
