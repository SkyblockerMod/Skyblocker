package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.InventorySearch;
import de.hysky.skyblocker.skyblock.PetCache;
import de.hysky.skyblocker.skyblock.experiment.ExperimentSolver;
import de.hysky.skyblocker.skyblock.experiment.SuperpairsSolver;
import de.hysky.skyblocker.skyblock.experiment.UltrasequencerSolver;
import de.hysky.skyblocker.skyblock.garden.visitor.VisitorHelper;
import de.hysky.skyblocker.skyblock.item.*;
import de.hysky.skyblocker.skyblock.item.background.ItemBackgroundManager;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextManager;
import de.hysky.skyblocker.skyblock.item.tooltip.BackpackPreview;
import de.hysky.skyblocker.skyblock.item.tooltip.CompactorDeletorPreview;
import de.hysky.skyblocker.skyblock.quicknav.QuickNav;
import de.hysky.skyblocker.skyblock.quicknav.QuickNavButton;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.container.ContainerSolver;
import de.hysky.skyblocker.utils.container.ContainerSolverManager;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
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
	/**
	 * This is the slot id returned for when a click is outside the screen's bounds
	 */
	@Unique
	private static final int skyblocker$OUT_OF_BOUNDS_SLOT = -999;

	@Unique
	private static final Set<String> skyblocker$FILLER_ITEMS = Set.of(
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

	@Unique
	private List<QuickNavButton> skyblocker$quickNavButtons;

	protected HandledScreenMixin(Text title) {
		super(title);
	}

	@Inject(method = "init", at = @At("RETURN"))
	private void initQuickNav(CallbackInfo ci) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().quickNav.enableQuickNav && client != null && client.player != null && !client.player.isCreative()) {
			for (QuickNavButton quickNavButton : skyblocker$quickNavButtons = QuickNav.init(getTitle().getString().trim())) {
				addSelectableChild(quickNavButton);
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "keyPressed")
	public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (this.client != null && this.client.player != null && this.focusedSlot != null && keyCode != 256 && !this.client.options.inventoryKey.matchesKey(keyCode, scanCode) && Utils.isOnSkyblock()) {
			SkyblockerConfig config = SkyblockerConfigManager.get();
			//wiki lookup
			if (config.general.wikiLookup.enableWikiLookup) {
				if (WikiLookup.officialWikiLookup.matchesKey(keyCode, scanCode)) {
					WikiLookup.openWiki(this.focusedSlot, client.player, true);
				} else if (WikiLookup.fandomWikiLookup.matchesKey(keyCode, scanCode)) {
					WikiLookup.openWiki(this.focusedSlot, client.player, false);
				}
			}
			//item protection
			if (ItemProtection.itemProtection.matchesKey(keyCode, scanCode)) {
				ItemProtection.handleKeyPressed(this.focusedSlot.getStack());
			}
			//Item Price Lookup
			if (config.helpers.itemPrice.enableItemPriceLookup && ItemPrice.ITEM_PRICE_LOOKUP.matchesKey(keyCode, scanCode)) {
				ItemPrice.itemPriceLookup(client.player, this.focusedSlot);
			}
			//Refresh Item Prices
			if (config.helpers.itemPrice.enableItemPriceRefresh && ItemPrice.ITEM_PRICE_REFRESH.matchesKey(keyCode, scanCode)) {
				ItemPrice.refreshItemPrices(this.client.player);
			}
		}
	}

	@ModifyExpressionValue(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseClicked(DDI)Z"))
	public boolean passThroughSearchFieldUnfocusedClicks(boolean superClicked, double mouseX, double mouseY, int button) {
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
	private void drawUnselectedQuickNavButtons(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (skyblocker$quickNavButtons != null) for (QuickNavButton quickNavButton : skyblocker$quickNavButtons) {
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
	private void drawSelectedQuickNavButtons(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (skyblocker$quickNavButtons != null) for (QuickNavButton quickNavButton : skyblocker$quickNavButtons) {
			if (quickNavButton.toggled()) {
				quickNavButton.setRenderInFront(true);
				quickNavButton.render(context, mouseX, mouseY, delta);
			}
		}
	}

	@SuppressWarnings("DataFlowIssue")
	// makes intellij be quiet about this.focusedSlot maybe being null. It's already null checked in mixined method.
	@Inject(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/util/Identifier;)V"), cancellable = true)
	public void drawMouseOverTooltip(DrawContext context, int x, int y, CallbackInfo ci, @Local(ordinal = 0) ItemStack stack) {
		if (!Utils.isOnSkyblock()) return;

		// Hide Empty Tooltips
		if (SkyblockerConfigManager.get().uiAndVisuals.hideEmptyTooltips && stack.getName().getString().equals(" ")) {
			ci.cancel();
		}

		// Backpack Preview
		boolean shiftDown = SkyblockerConfigManager.get().uiAndVisuals.backpackPreviewWithoutShift ^ Screen.hasShiftDown();
		if (shiftDown && getTitle().getString().equals("Storage") && focusedSlot.inventory != client.player.getInventory() && BackpackPreview.renderPreview(context, this, focusedSlot.getIndex(), x, y)) {
			ci.cancel();
		}

		// Compactor Preview
		if (SkyblockerConfigManager.get().uiAndVisuals.compactorDeletorPreview) {
			Matcher matcher = CompactorDeletorPreview.NAME.matcher(ItemUtils.getItemId(stack));
			if (matcher.matches() && CompactorDeletorPreview.drawPreview(context, stack, getTooltipFromItem(stack), matcher.group("type"), matcher.group("size"), x, y)) {
				ci.cancel();
			}
		}
	}

	@ModifyVariable(method = "drawMouseoverTooltip", at = @At(value = "STORE"))
	private ItemStack modifyToolTipStackForExperimentSolver(ItemStack stack) {
		return getExperimentSolverStack(focusedSlot, stack);
	}

	@ModifyVariable(method = "drawSlot", at = @At(value = "LOAD", ordinal = 3), ordinal = 0)
	private ItemStack replaceDisplayStackForExperimentSolver(ItemStack stack, @Local(argsOnly = true) Slot slot) {
		return getExperimentSolverStack(slot, stack);
	}

	/**
	 * Avoids getting currentSolver again when it's already in the scope for some usages of this method.
	 *
	 * @see #getExperimentSolverStack(Slot, ItemStack, ContainerSolver)
	 */
	@Unique
	private ItemStack getExperimentSolverStack(Slot slot, @NotNull ItemStack stack) {
		return getExperimentSolverStack(slot, stack, ContainerSolverManager.getCurrentSolver());
	}

	/**
	 * Redirects getStack calls to account for different stacks in experiment solvers.
	 */
	@Unique
	private ItemStack getExperimentSolverStack(Slot slot, @NotNull ItemStack stack, ContainerSolver currentSolver) {
		if (currentSolver instanceof ExperimentSolver experimentSolver && (experimentSolver instanceof SuperpairsSolver || experimentSolver instanceof UltrasequencerSolver) && experimentSolver.getState() == ExperimentSolver.State.SHOW && slot.inventory instanceof SimpleInventory) {
			ItemStack itemStack = experimentSolver.getSlots().get(slot.getIndex());
			return itemStack == null ? stack : itemStack;
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
	private void onSlotClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
		if (!Utils.isOnSkyblock()) return;

		// Item Protection
		// When you try and drop the item by picking it up then clicking outside the screen
		if (slotId == skyblocker$OUT_OF_BOUNDS_SLOT && ItemProtection.isItemProtected(this.handler.getCursorStack())) {
			ci.cancel();
			return;
		}

		if (slot == null) return;
		String title = getTitle().getString();
		ContainerSolver currentSolver = ContainerSolverManager.getCurrentSolver();
		ItemStack stack = getExperimentSolverStack(slot, slot.getStack(), currentSolver);

		// Prevent clicks on filler items
		if (SkyblockerConfigManager.get().uiAndVisuals.hideEmptyTooltips && skyblocker$FILLER_ITEMS.contains(stack.getName().getString()) &&
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

			case GenericContainerScreenHandler genericContainerScreenHandler when title.equals(MuseumItemCache.DONATION_CONFIRMATION_SCREEN_TITLE) -> {
				//Museum Item Cache donation tracking
				MuseumItemCache.handleClick(slot, slotId, genericContainerScreenHandler.slots);
			}

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

	@Inject(at = @At("HEAD"), method = "mouseClicked")
	public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (VisitorHelper.shouldRender()) {
			VisitorHelper.handleMouseClick(mouseX, mouseY, button, this.textRenderer);
		}
	}

	@Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawItem(Lnet/minecraft/item/ItemStack;III)V"))
	private void drawOnItem(DrawContext context, Slot slot, CallbackInfo ci) {
		if (Utils.isOnSkyblock()) {
			ItemBackgroundManager.drawBackgrounds(slot.getStack(), context, slot.x, slot.y);
		}

		// Item Protection
		if (ItemProtection.isItemProtected(slot.getStack())) {
			context.drawTexture(RenderLayer::getGuiTextured, ItemProtection.ITEM_PROTECTION_TEX, slot.x, slot.y, 0, 0, 16, 16, 16, 16);
		}

		// Search
		// Darken the slots
		if (InventorySearch.isSearching() && !InventorySearch.slotMatches(slot)) {
			context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 100, 0x88_000000);
		}
	}

	@Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawStackOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V"))
	private void drawSlotText(DrawContext context, Slot slot, CallbackInfo ci) {
		if (Utils.isOnSkyblock()) {
			SlotTextManager.renderSlotText(context, textRenderer, slot);
		}
	}
}
