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
import de.hysky.skyblocker.utils.render.HudHelper;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jspecify.annotations.Nullable;
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

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen {
	@Unique
	private static final Identifier GENERIC_CONTAINER_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");

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
	protected @Nullable Slot hoveredSlot;

	@Shadow
	@Final
	protected T menu;

	@Shadow
	protected abstract List<Component> getTooltipFromContainerItem(ItemStack stack);

	@Shadow
	protected int leftPos;
	@Shadow
	protected int topPos;
	@Shadow
	protected int imageWidth;
	@Unique
	private List<QuickNavButton> quickNavButtons;

	protected AbstractContainerScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("RETURN"))
	private void skyblocker$initQuickNav(CallbackInfo ci) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().quickNav.enableQuickNav && minecraft != null && minecraft.player != null && !minecraft.player.isCreative()) {
			for (QuickNavButton quickNavButton : quickNavButtons = QuickNav.init(getTitle().getString().trim())) {
				addWidget(quickNavButton);
			}
		}
	}

	@SuppressWarnings("unused")
	@Inject(method = "init", at = @At("TAIL"))
	private void skyblocker$initMuseumOverlay(CallbackInfo ci) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.museumOverlay && minecraft != null && minecraft.player != null && getTitle().getString().contains("Museum")) {
			int overlayWidth = MuseumManager.BACKGROUND_WIDTH; // width of the overlay
			int spacing = MuseumManager.SPACING; // space between inventory and overlay

			// Default: center inventory
			int inventoryX = (this.width - this.imageWidth) / 2;

			// If overlay would go off the right edge, shift inventory left
			if (inventoryX + this.imageWidth + spacing + overlayWidth > this.width) {
				inventoryX = this.width - (this.imageWidth + overlayWidth + spacing);
				if (inventoryX < 0) inventoryX = 0;
			}
			this.leftPos = inventoryX;

			new MuseumManager(this, this.leftPos, this.topPos, this.imageWidth);
		}
	}

	@WrapOperation(method = "renderBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V"))
	private void skyblocker$DrawMuseumOverlayBackground(AbstractContainerScreen<?> instance, GuiGraphics context, float delta, int mouseX, int mouseY, Operation<Void> original) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.museumOverlay && minecraft != null && minecraft.player != null && getTitle().getString().contains("Museum")) {
			// Custom museum overlay background drawing
			int rows = 6;
			context.blit(RenderPipelines.GUI_TEXTURED, GENERIC_CONTAINER_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, rows * 18 + 17, 256, 256);
			context.blit(RenderPipelines.GUI_TEXTURED, GENERIC_CONTAINER_TEXTURE, this.leftPos, this.topPos + rows * 18 + 17, 0.0F, 126.0F, this.imageWidth, 96, 256, 256);
		} else {
			// Call vanilla
			original.call(instance, context, delta, mouseX, mouseY);
		}
	}

	@Inject(at = @At("HEAD"), method = "keyPressed")
	public void skyblocker$keyPressed(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
		if (this.minecraft != null && this.minecraft.player != null && this.hoveredSlot != null && !input.isEscape() && !this.minecraft.options.keyInventory.matches(input) && Utils.isOnSkyblock()) {
			SkyblockerConfig config = SkyblockerConfigManager.get();

			// Wiki lookup
			WikiLookupManager.handleWikiLookup(this.getTitle().getString(), Either.left(this.hoveredSlot), this.minecraft.player, input);

			//item protection
			if (ItemProtection.itemProtection.matches(input)) {
				ItemProtection.handleKeyPressed(this.hoveredSlot.getItem());
			}
			//Item Price Lookup
			if (config.helpers.itemPrice.enableItemPriceLookup && ItemPrice.ITEM_PRICE_LOOKUP.matches(input)) {
				ItemPrice.itemPriceLookup(minecraft.player, this.hoveredSlot);
			}
		}
	}

	@ModifyExpressionValue(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z"))
	public boolean skyblocker$passThroughSearchFieldUnfocusedClicks(boolean superClicked, MouseButtonEvent click, boolean doubled) {
		//Handle Search Field clicks - as of 1.21.4 the game will only send clicks to the selected element rather than trying to send one to each and stopping when the first returns true (if any).
		if (!superClicked) {
			Optional<AbstractWidget> searchField = Screens.getButtons(this).stream()
					.filter(InventorySearch.SearchTextFieldWidget.class::isInstance)
					.findFirst();

			if (searchField.isPresent() && searchField.get().mouseClicked(click, doubled)) {
				return true;
			}
		}

		return superClicked;
	}

	/**
	 * Draws the unselected tabs in front of the background blur, but behind the main inventory, similar to creative inventory tabs
	 */
	@Inject(method = "renderBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V"))
	private void skyblocker$drawUnselectedQuickNavButtons(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
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
	private void skyblocker$drawSelectedQuickNavButtons(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (quickNavButtons != null) for (QuickNavButton quickNavButton : quickNavButtons) {
			if (quickNavButton.toggled()) {
				quickNavButton.setRenderInFront(true);
				quickNavButton.render(context, mouseX, mouseY, delta);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Inject(method = "renderTooltip", at = @At("HEAD"))
	private void skyblocker$beforeTooltipDrawn(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics context) {
		ContainerSolverManager.onDraw(context, (AbstractContainerScreen<ChestMenu>) (Object) this, this.menu.slots);
	}

	@SuppressWarnings("DataFlowIssue")
	// makes intellij be quiet about this.focusedSlot maybe being null. It's already null checked in mixined method.
	@WrapOperation(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V"))
	private void skyblocker$drawMouseOverTooltip(
			GuiGraphics context,
			Font textRenderer,
			List<Component> text,
			Optional<TooltipComponent> data,
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
		boolean shiftDown = SkyblockerConfigManager.get().uiAndVisuals.backpackPreviewWithoutShift ^ HudHelper.hasShiftDown();
		if (shiftDown && getTitle().getString().equals("Storage") && hoveredSlot.container != minecraft.player.getInventory() && BackpackPreview.renderPreview(context, this, hoveredSlot.getContainerSlot(), x, y)) {
			return;
		}

		// Compactor Preview
		if (SkyblockerConfigManager.get().uiAndVisuals.compactorDeletorPreview) {
			Matcher matcher = CompactorDeletorPreview.NAME.matcher(stack.getSkyblockId());
			if (matcher.matches() && CompactorDeletorPreview.drawPreview(context, stack, getTooltipFromContainerItem(stack), matcher.group("type"), matcher.group("size"), x, y)) {
				return;
			}
		}

		original.call(context, textRenderer, text, data, x, y, texture);
	}

	@ModifyVariable(method = "renderTooltip", at = @At(value = "STORE"))
	private ItemStack skyblocker$modifyTooltipDisplayStack(ItemStack stack) {
		return skyblocker$modifyDisplayStack(hoveredSlot, stack, ContainerSolverManager.getCurrentSolver());
	}

	@ModifyVariable(method = "renderSlot", at = @At(value = "LOAD", ordinal = 3), ordinal = 0)
	private ItemStack skyblocker$modifyDisplayStack(ItemStack stack, @Local(argsOnly = true) Slot slot) {
		return skyblocker$modifyDisplayStack(slot, stack, ContainerSolverManager.getCurrentSolver());
	}

	@Unique
	private ItemStack skyblocker$modifyDisplayStack(Slot slot, ItemStack stack, ContainerSolver solver) {
		if (solver instanceof StackDisplayModifier modifier && solver.isSolverSlot(slot, this)) {
			return modifier.modifyDisplayStack(slot.getContainerSlot(), stack);
		}
		return stack;
	}

	/**
	 * The naming of this method in yarn is half true, its mostly to handle slot/item interactions (which are mouse or keyboard clicks)
	 * For example, using the drop key bind while hovering over an item will invoke this method to drop the players item
	 *
	 * @implNote This runs before {@link AbstractContainerMenu#clicked(int, int, ClickType, net.minecraft.world.entity.player.Player)}
	 */
	@Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleInventoryMouseClick(IIILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V"), cancellable = true)
	private void skyblocker$onSlotClick(Slot slot, int slotId, int button, ClickType actionType, CallbackInfo ci) {
		if (!Utils.isOnSkyblock()) return;

		// Item Protection
		// When you try and drop the item by picking it up then clicking outside the screen
		if (slotId == OUT_OF_BOUNDS_SLOT && ItemProtection.isItemProtected(this.menu.getCarried())) {
			ci.cancel();
			return;
		}

		if (slot == null) return;
		String title = getTitle().getString();
		ContainerSolver currentSolver = ContainerSolverManager.getCurrentSolver();
		ItemStack stack = skyblocker$modifyDisplayStack(slot, slot.getItem(), currentSolver);

		boolean isTitleEmptyOrFiller = FILLER_ITEMS.contains(stack.getHoverName().getString());
		if (isTitleEmptyOrFiller) {
			var tooltip = stack.getTooltipLines(Item.TooltipContext.EMPTY, Minecraft.getInstance().player, TooltipFlag.NORMAL).stream().map(Component::getString).toList();
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
		if (actionType == ClickType.THROW && ItemProtection.isItemProtected(stack)) {
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
		boolean isInAuctionGUI = title.endsWith("Auction House") // "Co-op Auction House" in Co-op profile
				|| title.equals("Create Auction")
				|| title.equals("Create BIN Auction");
		if (isInAuctionGUI && ItemProtection.isItemProtected(stack)) {
			ci.cancel();
			return;
		}

		switch (this.menu) {
			case ChestMenu genericContainerScreenHandler when genericContainerScreenHandler.getRowCount() == 6 -> {
				VisitorHelper.onSlotClick(slot, slotId, title, genericContainerScreenHandler.getSlot(13));
				// Prevent selling to NPC shops
				ItemStack sellStack = this.menu.slots.get(49).getItem();
				if (sellStack.getHoverName().getString().equals("Sell Item") || ItemUtils.getLoreLineIf(sellStack, text -> text.contains("buyback")) != null) {
					if (slotId != 49 && ItemProtection.isItemProtected(stack)) {
						ci.cancel();
						return;
					}
				}
			}

			case ChestMenu genericContainerScreenHandler when title.equals(MuseumItemCache.DONATION_CONFIRMATION_SCREEN_TITLE) -> //Museum Item Cache donation tracking
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

	@Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;III)V"))
	private void skyblocker$drawOnItem(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics context, @Local(argsOnly = true) Slot slot) {
		if (Utils.isOnSkyblock()) {
			ItemBackgroundManager.drawBackgrounds(slot.getItem(), context, slot.x, slot.y);
		}

		// Item Protection
		if (ItemProtection.isItemProtected(slot.getItem())) {
			context.blit(RenderPipelines.GUI_TEXTURED, ItemProtection.ITEM_PROTECTION_TEX, slot.x, slot.y, 0, 0, 16, 16, 16, 16);
		}

		// Search - darken non-matching slots
		if (InventorySearch.isSearching() && !InventorySearch.slotMatches(slot)) {
			context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x88_000000);
		}
	}

	@Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"))
	private void skyblocker$drawSlotText(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics context, @Local(argsOnly = true) Slot slot) {
		if (Utils.isOnSkyblock()) {
			SlotTextManager.renderSlotText(context, font, slot);
		}
	}
}
