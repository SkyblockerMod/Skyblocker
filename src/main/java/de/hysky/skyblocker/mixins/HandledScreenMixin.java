package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.PetCache;
import de.hysky.skyblocker.skyblock.experiment.ChronomatronSolver;
import de.hysky.skyblocker.skyblock.experiment.ExperimentSolver;
import de.hysky.skyblocker.skyblock.experiment.SuperpairsSolver;
import de.hysky.skyblocker.skyblock.experiment.UltrasequencerSolver;
import de.hysky.skyblocker.skyblock.garden.VisitorHelper;
import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.skyblock.item.ItemRarityBackgrounds;
import de.hysky.skyblocker.skyblock.item.MuseumItemCache;
import de.hysky.skyblocker.skyblock.item.WikiLookup;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextManager;
import de.hysky.skyblocker.skyblock.item.tooltip.BackpackPreview;
import de.hysky.skyblocker.skyblock.item.tooltip.CompactorDeletorPreview;
import de.hysky.skyblocker.skyblock.quicknav.QuickNav;
import de.hysky.skyblocker.skyblock.quicknav.QuickNavButton;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.gui.ContainerSolver;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {
	/**
	 * This is the slot id returned for when a click is outside the screen's bounds
	 */
	@Unique
	private static final int OUT_OF_BOUNDS_SLOT = -999;

	@Unique
	private static final Identifier ITEM_PROTECTION = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/item_protection.png");

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

	@Inject(at = @At("HEAD"), method = "keyPressed")
	public void skyblocker$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (this.client != null && this.focusedSlot != null && keyCode != 256) {
			//wiki lookup
			if (!this.client.options.inventoryKey.matchesKey(keyCode, scanCode) && WikiLookup.wikiLookup.matchesKey(keyCode, scanCode) && client.player != null) {
				WikiLookup.openWiki(this.focusedSlot, client.player);
			}
			//item protection
			if (!this.client.options.inventoryKey.matchesKey(keyCode, scanCode) && ItemProtection.itemProtection.matchesKey(keyCode, scanCode)) {
				ItemProtection.handleKeyPressed(this.focusedSlot.getStack());
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "mouseClicked")
	public void skyblocker$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (SkyblockerConfigManager.get().farming.garden.visitorHelper && (Utils.getLocationRaw().equals("garden") && !getTitle().getString().contains("Logbook") || getTitle().getString().startsWith("Bazaar"))) {
			VisitorHelper.onMouseClicked(mouseX, mouseY, button, this.textRenderer);
		}
	}

	/**
	 * Draws the unselected tabs in front of the background blur, but behind the main inventory, similar to creative inventory tabs
	 */
	@Inject(method = "renderBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawBackground(Lnet/minecraft/client/gui/DrawContext;FII)V"))
	private void skyblocker$drawUnselectedQuickNavButtons(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (quickNavButtons != null) for (QuickNavButton quickNavButton : quickNavButtons) {
			if (!quickNavButton.toggled()) {
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
				quickNavButton.render(context, mouseX, mouseY, delta);
			}
		}
	}

	@SuppressWarnings("DataFlowIssue")
	// makes intellij be quiet about this.focusedSlot maybe being null. It's already null checked in mixined method.
	@Inject(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V"), cancellable = true)
	public void skyblocker$drawMouseOverTooltip(DrawContext context, int x, int y, CallbackInfo ci, @Local(ordinal = 0) ItemStack stack) {
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

	@ModifyVariable(method = "drawMouseoverTooltip", at = @At(value = "LOAD", ordinal = 0))
	private ItemStack skyblocker$experimentSolvers$replaceTooltipDisplayStack(ItemStack stack) {
		return skyblocker$experimentSolvers$getStack(focusedSlot, stack);
	}

	@ModifyVariable(method = "drawSlot", at = @At(value = "LOAD", ordinal = 3), ordinal = 0)
	private ItemStack skyblocker$experimentSolvers$replaceDisplayStack(ItemStack stack, DrawContext context, Slot slot) {
		return skyblocker$experimentSolvers$getStack(slot, stack);
	}

	/**
	 * Redirects getStack calls to account for different stacks in experiment solvers.
	 */
	@Unique
	private ItemStack skyblocker$experimentSolvers$getStack(Slot slot, @NotNull ItemStack stack) {
		ContainerSolver currentSolver = SkyblockerMod.getInstance().containerSolverManager.getCurrentSolver();
		if ((currentSolver instanceof SuperpairsSolver || currentSolver instanceof UltrasequencerSolver) && ((ExperimentSolver) currentSolver).getState() == ExperimentSolver.State.SHOW && slot.inventory instanceof SimpleInventory) {
			ItemStack itemStack = ((ExperimentSolver) currentSolver).getSlots().get(slot.getIndex());
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
		ItemStack stack = skyblocker$experimentSolvers$getStack(slot, slot.getStack());
		ContainerSolver currentSolver = SkyblockerMod.getInstance().containerSolverManager.getCurrentSolver();

		// Prevent clicks on filler items
		if (SkyblockerConfigManager.get().uiAndVisuals.hideEmptyTooltips && FILLER_ITEMS.contains(stack.getName().getString()) &&
				// Allow clicks in Ultrasequencer and Superpairs
				(!UltrasequencerSolver.INSTANCE.getName().matcher(title).matches() || SkyblockerConfigManager.get().helpers.experiments.enableUltrasequencerSolver)) {
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
		if (title.equals("Salvage Items") && ItemProtection.isItemProtected(stack)) {
			ci.cancel();
			return;
		}

		switch (this.handler) {
			case GenericContainerScreenHandler genericContainerScreenHandler when genericContainerScreenHandler.getRows() == 6 -> {
				VisitorHelper.onSlotClick(slot, slotId, title, genericContainerScreenHandler.getSlot(13).getStack());

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
			boolean disallowed = SkyblockerMod.getInstance().containerSolverManager.onSlotClick(slotId, stack);

			if (disallowed) ci.cancel();
		}

		// Experiment Solvers
		if (currentSolver instanceof ExperimentSolver experimentSolver && experimentSolver.getState() == ExperimentSolver.State.SHOW && slot.inventory instanceof SimpleInventory) {
			switch (experimentSolver) {
				case ChronomatronSolver chronomatronSolver -> {
					Item item = chronomatronSolver.getChronomatronSlots().get(chronomatronSolver.getChronomatronCurrentOrdinal());
					if ((stack.isOf(item) || ChronomatronSolver.TERRACOTTA_TO_GLASS.get(stack.getItem()) == item) && chronomatronSolver.incrementChronomatronCurrentOrdinal() >= chronomatronSolver.getChronomatronSlots().size()) {
						chronomatronSolver.setState(ExperimentSolver.State.END);
					}
				}

				case SuperpairsSolver superpairsSolver -> {
					superpairsSolver.setSuperpairsPrevClickedSlot(slot.getIndex());
					superpairsSolver.setSuperpairsCurrentSlot(ItemStack.EMPTY);
				}

				case UltrasequencerSolver ultrasequencerSolver when slot.getIndex() == ultrasequencerSolver.getUltrasequencerNextSlot() -> {
					int count = ultrasequencerSolver.getSlots().get(ultrasequencerSolver.getUltrasequencerNextSlot()).getCount() + 1;
					ultrasequencerSolver.getSlots().entrySet().stream().filter(entry -> entry.getValue().getCount() == count).findAny().map(Map.Entry::getKey).ifPresentOrElse(ultrasequencerSolver::setUltrasequencerNextSlot, () -> ultrasequencerSolver.setState(ExperimentSolver.State.END));
				}

				default -> { /*Do Nothing*/ }
			}
		}
	}

	@Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawItem(Lnet/minecraft/item/ItemStack;III)V"))
	private void skyblocker$drawItemRarityBackground(DrawContext context, Slot slot, CallbackInfo ci) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().general.itemInfoDisplay.itemRarityBackgrounds)
			ItemRarityBackgrounds.tryDraw(slot.getStack(), context, slot.x, slot.y);
		// Item protection
		if (ItemProtection.isItemProtected(slot.getStack())) {
			RenderSystem.enableBlend();
			context.drawTexture(ITEM_PROTECTION, slot.x, slot.y, 0, 0, 16, 16, 16, 16);
			RenderSystem.disableBlend();
		}
	}

	@Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V"))
	private void skyblocker$drawSlotText(DrawContext context, Slot slot, CallbackInfo ci) {
		List<SlotText> textList = SlotTextManager.getText(slot);
		if (textList.isEmpty()) return;
		MatrixStack matrices = context.getMatrices();

		for (SlotText slotText : textList) {
			matrices.push();
			matrices.translate(0.0f, 0.0f, 200.0f);
			int length = textRenderer.getWidth(slotText.text());
			if (length > 16) {
				matrices.scale(16.0f / length, 16.0f / length, 1.0f); //Make them fit in the slot. FYI, a slot is sized 16×16.
				final float x = (slot.x * length / 16.0f) - slot.x; //Save in a variable to not recalculate
				switch (slotText.position()) {
					case TOP_LEFT, TOP_RIGHT -> matrices.translate(x, (slot.y * length / 16.0f) - slot.y, 0.0f);
					case BOTTOM_LEFT, BOTTOM_RIGHT -> matrices.translate(x, ((slot.y + 16f - textRenderer.fontHeight + 2f + 0.7f) * length / 16.0f) - slot.y, 0.0f);
				}
			} else {
				switch (slotText.position()) {
					case TOP_LEFT -> { /*Do Nothing*/ }
					case TOP_RIGHT -> matrices.translate(16f - length, 0.0f, 0.0f);
					case BOTTOM_LEFT -> matrices.translate(0.0f, 16f - textRenderer.fontHeight + 2f, 0.0f);
					case BOTTOM_RIGHT -> matrices.translate(16f - length, 16f - textRenderer.fontHeight + 2f, 0.0f);
				}
			}
			context.drawText(textRenderer, slotText.text(), slot.x, slot.y, 0xFFFFFF, true);
			matrices.pop();
		}
	}
}
