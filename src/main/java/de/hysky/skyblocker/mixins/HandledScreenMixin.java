package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.InventorySearch;
import de.hysky.skyblocker.skyblock.PetCache;
import de.hysky.skyblocker.skyblock.experiment.UltrasequencerSolver;
import de.hysky.skyblocker.skyblock.garden.visitor.VisitorHelper;
import de.hysky.skyblocker.skyblock.item.ItemPrice;
import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.skyblock.item.background.ItemBackgroundManager;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextManager;
import de.hysky.skyblocker.skyblock.item.tooltip.BackpackPreview;
import de.hysky.skyblocker.skyblock.item.tooltip.CompactorDeletorPreview;
import de.hysky.skyblocker.skyblock.item.wikilookup.WikiLookupManager;
import de.hysky.skyblocker.skyblock.museum.MuseumItemCache;
import de.hysky.skyblocker.skyblock.museum.MuseumManager;
import de.hysky.skyblocker.skyblock.quicknav.QuickNav;
import de.hysky.skyblocker.skyblock.quicknav.QuickNavButton;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.container.ContainerSolver;
import de.hysky.skyblocker.utils.container.ContainerSolverManager;
import de.hysky.skyblocker.utils.container.StackDisplayModifier;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {
	@Unique
	private static final Identifier GENERIC_CONTAINER_TEXTURE = Identifier.ofVanilla("textures/gui/container/generic_54.png");

	/**
	 * This is the slot id returned for when a click is outside the screen's bounds
	 */
	@Unique
	private static final int OUT_OF_BOUNDS_SLOT = -999;

	@Unique
	private static final Set<String> FILLER_ITEMS = Set.of(
			" ", // Empty menu item
			"Locked Page",
			"Quick Crafting Slot",
			"Locked Backpack Slot 2", //Regular expressions won't be utilized here since the search by contains is based on plain text rather than regex syntax
			"Locked Backpack Slot 3",
			"Locked Backpack Slot 4",
			"Locked Backpack Slot 5",
			"Locked Backpack Slot 6",
			"Locked Backpack Slot 7",
			"Locked Backpack Slot 8",
			"Locked Backpack Slot 9",
			"Locked Backpack Slot 10",
			"Locked Backpack Slot 11",
			"Locked Backpack Slot 12",
			"Locked Backpack Slot 13",
			"Locked Backpack Slot 14",
			"Locked Backpack Slot 15",
			"Locked Backpack Slot 16",
			"Locked Backpack Slot 17",
			"Locked Backpack Slot 18",
			"Preparing"
	);

	@Shadow
	@Nullable
	protected Slot focusedSlot;

	@Shadow
	@Final
	protected T handler;

	@Shadow
	protected abstract List<Text> getTooltipFromItem(ItemStack stack);

	@Shadow
	protected int x;
	@Shadow
	protected int y;
	@Shadow
	protected int backgroundWidth;
	@Unique
	private List<QuickNavButton> quickNavButtons;

	protected HandledScreenMixin(Text title) {
		super(title);
	}

	@Inject(method = "init", at = @At("RETURN"))
	private void skyblocker$initQuickNav(CallbackInfo ci) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().quickNav.enableQuickNav && client != null && client.player != null && !client.player.isCreative()) {
			for (QuickNavButton quickNavButton : quickNavButtons = QuickNav.init(getTitle().getString().trim())) {
				addSelectableChild(quickNavButton);
			}
		}
	}

	@SuppressWarnings("unused")
	@Inject(method = "init", at = @At("TAIL"))
	private void skyblocker$initMuseumOverlay(CallbackInfo ci) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.museumOverlay && client != null && client.player != null && getTitle().getString().contains("Museum")) {
			int overlayWidth = MuseumManager.BACKGROUND_WIDTH; // width of the overlay
			int spacing = MuseumManager.SPACING; // space between inventory and overlay

			// Default: center inventory
			int inventoryX = (this.width - this.backgroundWidth) / 2;

			// If overlay would go off the right edge, shift inventory left
			if (inventoryX + this.backgroundWidth + spacing + overlayWidth > this.width) {
				inventoryX = this.width - (this.backgroundWidth + overlayWidth + spacing);
				if (inventoryX < 0) inventoryX = 0;
			}
			this.x = inventoryX;

			new MuseumManager(this, this.x, this.y, this.backgroundWidth);
		}
	}

	@WrapOperation(method = "renderBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawBackground(Lnet/minecraft/client/gui/DrawContext;FII)V"))
	private void skyblocker$DrawMuseumOverlayBackground(HandledScreen<?> instance, DrawContext context, float delta, int mouseX, int mouseY, Operation<Void> original) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.museumOverlay && client != null && client.player != null && getTitle().getString().contains("Museum")) {
			// Custom museum overlay background drawing
			int rows = 6;
			context.drawTexture(RenderPipelines.GUI_TEXTURED, GENERIC_CONTAINER_TEXTURE, this.x, this.y, 0.0F, 0.0F, this.backgroundWidth, rows * 18 + 17, 256, 256);
			context.drawTexture(RenderPipelines.GUI_TEXTURED, GENERIC_CONTAINER_TEXTURE, this.x, this.y + rows * 18 + 17, 0.0F, 126.0F, this.backgroundWidth, 96, 256, 256);
		} else {
			// Call vanilla
			original.call(instance, context, delta, mouseX, mouseY);
		}
	}

	@Inject(at = @At("HEAD"), method = "keyPressed")
	public void skyblocker$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (this.client != null && this.client.player != null && this.focusedSlot != null && keyCode != 256 && !this.client.options.inventoryKey.matchesKey(keyCode, scanCode) && Utils.isOnSkyblock()) {
			SkyblockerConfig config = SkyblockerConfigManager.get();

			// Wiki lookup
			WikiLookupManager.handleWikiLookup(this.getTitle().getString(), Either.left(this.focusedSlot), this.client.player, keyCode, scanCode);

			//item protection
			if (ItemProtection.itemProtection.matchesKey(keyCode, scanCode)) {
				ItemProtection.handleKeyPressed(this.focusedSlot.getStack());
			}
			//Item Price Lookup
			if (config.helpers.itemPrice.enableItemPriceLookup && ItemPrice.ITEM_PRICE_LOOKUP.matchesKey(keyCode, scanCode)) {
				ItemPrice.itemPriceLookup(client.player, this.focusedSlot);
			}
		}
	}

	@ModifyExpressionValue(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseClicked(DDI)Z"))
	public boolean skyblocker$passThroughSearchFieldUnfocusedClicks(boolean superClicked, double mouseX, double mouseY, int button) {
		//Handle Search Field clicks - as of 1.21.4 the game will only send clicks to the selected element rather than trying to send one to each and stopping when the first returns true (if any).
		if (!superClicked) {
			Optional<ClickableWidget> searchField = Screens.getButtons(this).stream()
					.filter(InventorySearch.SearchTextFieldWidget.class::isInstance)
					.findFirst();

			if (searchField.isPresent() && searchField.get().mouseClicked(mouseX, mouseY, button)) {
				return true;
			}
		}

		return superClicked;
	}

	/**
	 * Draws the unselected tabs in front of the background blur, but behind the main inventory, similar to creative inventory tabs
	 */
	@Inject(method = "renderBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawBackground(Lnet/minecraft/client/gui/DrawContext;FII)V"))
	private void skyblocker$drawUnselectedQuickNavButtons(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (quickNavButtons != null) for (QuickNavButton quickNavButton : quickNavButtons) {
			// Render the button behind the main inventory background if it's not toggled or if it's still fading in
			if (!quickNavButton.toggled() || quickNavButton.getAlpha() < 255) {
				quickNavButton.setRenderInFront(false);
				quickNavButton.render(context, mouseX, mouseY, delta);
			}
		}
	}

	/**
	 * Draws the selected tab in front of the background blur and the main inventory, similar to creative inventory tabs
	 */
	@Inject(method = "renderBackground", at = @At("RETURN"))
	private void skyblocker$drawSelectedQuickNavButtons(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (quickNavButtons != null) for (QuickNavButton quickNavButton : quickNavButtons) {
			if (quickNavButton.toggled()) {
				quickNavButton.setRenderInFront(true);
				quickNavButton.render(context, mouseX, mouseY, delta);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Inject(method = "drawMouseoverTooltip", at = @At("HEAD"))
	private void skyblocker$beforeTooltipDrawn(CallbackInfo ci, @Local(argsOnly = true) DrawContext context) {
		ContainerSolverManager.onDraw(context, (HandledScreen<GenericContainerScreenHandler>) (Object) this, this.handler.slots);
	}

	@SuppressWarnings("DataFlowIssue")
	// makes intellij be quiet about this.focusedSlot maybe being null. It's already null checked in mixined method.
	@WrapOperation(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/util/Identifier;)V"))
	private void skyblocker$drawMouseOverTooltip(
			DrawContext context,
			TextRenderer textRenderer,
			List<Text> text,
			Optional<TooltipData> data,
			int x,
			int y,
			Identifier texture,
			Operation<Void> original,
			@Local(ordinal = 0) ItemStack stack
	) {
		// Hide tooltips from items that have been visually replaced by a container solver with air (since the Slot#hasStack still passes)
		if (ContainerSolverManager.getCurrentSolver() instanceof StackDisplayModifier && stack.isEmpty()) {
			return;
		}

		if (!Utils.isOnSkyblock() || text.isEmpty()) {
			original.call(context, textRenderer, text, data, x, y, texture);
			return;
		}

		var name = text.getFirst().getString();

		// Hide Empty Tooltips
		if (SkyblockerConfigManager.get().uiAndVisuals.hideEmptyTooltips && name.isBlank()) {
			return;
		}

		// Backpack Preview
		boolean shiftDown = SkyblockerConfigManager.get().uiAndVisuals.backpackPreviewWithoutShift ^ Screen.hasShiftDown();
		if (shiftDown && getTitle().getString().equals("Storage") && focusedSlot.inventory != client.player.getInventory() && BackpackPreview.renderPreview(context, this, focusedSlot.getIndex(), x, y)) {
			return;
		}

		// Compactor Preview
		if (SkyblockerConfigManager.get().uiAndVisuals.compactorDeletorPreview) {
			Matcher matcher = CompactorDeletorPreview.NAME.matcher(ItemUtils.getItemId(stack));
			if (matcher.matches() && CompactorDeletorPreview.drawPreview(context, stack, getTooltipFromItem(stack), matcher.group("type"), matcher.group("size"), x, y)) {
				return;
			}
		}

		original.call(context, textRenderer, text, data, x, y, texture);
	}

	@ModifyVariable(method = "drawMouseoverTooltip", at = @At(value = "STORE"))
	private ItemStack skyblocker$modifyTooltipDisplayStack(ItemStack stack) {
		return skyblocker$modifyDisplayStack(focusedSlot, stack, ContainerSolverManager.getCurrentSolver());
	}

	@ModifyVariable(method = "drawSlot", at = @At(value = "LOAD", ordinal = 3), ordinal = 0)
	private ItemStack skyblocker$modifyDisplayStack(ItemStack stack, @Local(argsOnly = true) Slot slot) {
		return skyblocker$modifyDisplayStack(slot, stack, ContainerSolverManager.getCurrentSolver());
	}

	@Unique
	private ItemStack skyblocker$modifyDisplayStack(Slot slot, ItemStack stack, ContainerSolver solver) {
		if (solver instanceof StackDisplayModifier modifier && solver.isSolverSlot(slot, this)) {
			return modifier.modifyDisplayStack(slot.getIndex(), stack);
		}
		return stack;
	}

	/**
	 * The naming of this method in yarn is half true, its mostly to handle slot/item interactions (which are mouse or keyboard clicks)
	 * For example, using the drop key bind while hovering over an item will invoke this method to drop the players item
	 *
	 * @implNote This runs before {@link ScreenHandler#onSlotClick(int, int, SlotActionType, net.minecraft.entity.player.PlayerEntity)}
	 */
	@Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;clickSlot(IIILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V"), cancellable = true)
	private void skyblocker$onSlotClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
		if (!Utils.isOnSkyblock()) return;

		// Item Protection
		// When you try and drop the item by picking it up then clicking outside the screen
		if (slotId == OUT_OF_BOUNDS_SLOT && ItemProtection.isItemProtected(this.handler.getCursorStack())) {
			ci.cancel();
			return;
		}

		if (slot == null) return;
		String title = getTitle().getString();
		ContainerSolver currentSolver = ContainerSolverManager.getCurrentSolver();
		ItemStack stack = skyblocker$modifyDisplayStack(slot, slot.getStack(), currentSolver);

		boolean isTitleEmptyOrFiller = FILLER_ITEMS.contains(stack.getName().getString());
		if (isTitleEmptyOrFiller) {
			var tooltip = stack.getTooltip(Item.TooltipContext.DEFAULT, MinecraftClient.getInstance().player, TooltipType.BASIC).stream().map(Text::getString).toList();
			String lore = String.join("\n", tooltip);
			isTitleEmptyOrFiller = lore.isBlank() || FILLER_ITEMS.contains(tooltip.getFirst());
		}


		// Prevent clicks on filler items
		if (SkyblockerConfigManager.get().uiAndVisuals.hideEmptyTooltips && isTitleEmptyOrFiller &&
				// Allow clicks in Ultrasequencer and Superpairs
				(!UltrasequencerSolver.INSTANCE.test(title) || SkyblockerConfigManager.get().helpers.experiments.enableUltrasequencerSolver)) {
			ci.cancel();
			return;
		}
		// Item Protection
		// When you click your drop key while hovering over an item
		if (actionType == SlotActionType.THROW && ItemProtection.isItemProtected(stack)) {
			ci.cancel();
			return;
		}
		// Prevent salvaging
		// TODO in future maybe also block clicking the salvage button if a protected item manages to get into the menu
		if (title.equals("Salvage Items") && ItemProtection.isItemProtected(stack)) {
			ci.cancel();
			return;
		}
		// Prevent Trading
		if (title.startsWith("You  ") && ItemProtection.isItemProtected(stack)) { //Terrible way to detect the trade menu lol
			ci.cancel();
			return;
		}
		// Prevent Auctioning
		if ((title.equals("Create BIN Auction") || title.equals("Create Auction")) && ItemProtection.isItemProtected(stack)) {
			ci.cancel();
			return;
		}

		switch (this.handler) {
			case GenericContainerScreenHandler genericContainerScreenHandler when genericContainerScreenHandler.getRows() == 6 -> {
				VisitorHelper.onSlotClick(slot, slotId, title, genericContainerScreenHandler.getSlot(13));
				// Prevent selling to NPC shops
				ItemStack sellStack = this.handler.slots.get(49).getStack();
				if (sellStack.getName().getString().equals("Sell Item") || ItemUtils.getLoreLineIf(sellStack, text -> text.contains("buyback")) != null) {
					if (slotId != 49 && ItemProtection.isItemProtected(stack)) {
						ci.cancel();
						return;
					}
				}
			}

			case GenericContainerScreenHandler genericContainerScreenHandler when title.equals(MuseumItemCache.DONATION_CONFIRMATION_SCREEN_TITLE) -> //Museum Item Cache donation tracking
					MuseumItemCache.handleClick(slot, slotId, genericContainerScreenHandler.slots);

			case null, default -> {}
		}

		//Pet Caching
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && title.startsWith("Pets")) {
			PetCache.handlePetEquip(slot, slotId);
		}

		if (currentSolver != null) {
			boolean disallowed = ContainerSolverManager.onSlotClick(slotId, stack, button);

			if (disallowed) ci.cancel();
		}
	}

	@Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawItem(Lnet/minecraft/item/ItemStack;III)V"))
	private void skyblocker$drawOnItem(DrawContext context, Slot slot, CallbackInfo ci) {
		if (Utils.isOnSkyblock()) {
			ItemBackgroundManager.drawBackgrounds(slot.getStack(), context, slot.x, slot.y);
		}

		// Item Protection
		if (ItemProtection.isItemProtected(slot.getStack())) {
			context.drawTexture(RenderPipelines.GUI_TEXTURED, ItemProtection.ITEM_PROTECTION_TEX, slot.x, slot.y, 0, 0, 16, 16, 16, 16);
		}

		// Search - darken non-matching slots
		if (InventorySearch.isSearching() && !InventorySearch.slotMatches(slot)) {
			context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x88_000000);
		}
	}

	@Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawStackOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V"))
	private void skyblocker$drawSlotText(DrawContext context, Slot slot, CallbackInfo ci) {
		if (Utils.isOnSkyblock()) {
			SlotTextManager.renderSlotText(context, textRenderer, slot);
		}
	}
}
