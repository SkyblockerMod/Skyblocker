package me.xmrvizzy.skyblocker.mixin;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.mixin.accessor.DrawContextInvoker;
import me.xmrvizzy.skyblocker.skyblock.BackpackPreview;
import me.xmrvizzy.skyblocker.skyblock.experiment.ChronomatronSolver;
import me.xmrvizzy.skyblocker.skyblock.experiment.ExperimentSolver;
import me.xmrvizzy.skyblocker.skyblock.experiment.SuperpairsSolver;
import me.xmrvizzy.skyblocker.skyblock.experiment.UltrasequencerSolver;
import me.xmrvizzy.skyblocker.skyblock.item.CompactorPreviewTooltipComponent;
import me.xmrvizzy.skyblocker.skyblock.item.WikiLookup;
import me.xmrvizzy.skyblocker.skyblock.itemlist.ItemRegistry;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.render.gui.ContainerSolver;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    @Shadow
    @Nullable
    protected Slot focusedSlot;

    @Unique
    private static final Map<String, int[]> personalCompactorTypeToSlot = new HashMap<>();
    // Lines, and slots per lines
    static {
        personalCompactorTypeToSlot.put("4000", new int[]{1,1});
        personalCompactorTypeToSlot.put("5000", new int[]{1,3});
        personalCompactorTypeToSlot.put("6000", new int[]{1,7});
        personalCompactorTypeToSlot.put("7000", new int[]{2,6});
        personalCompactorTypeToSlot.put("default", new int[]{1,6});
    }

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("HEAD"), method = "keyPressed")
    public void skyblocker$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.client != null && this.focusedSlot != null && keyCode != 256 && !this.client.options.inventoryKey.matchesKey(keyCode, scanCode) && WikiLookup.wikiLookup.matchesKey(keyCode, scanCode)) {
            WikiLookup.openWiki(this.focusedSlot);
        }
    }

    @Inject(at = @At("HEAD"), method = "drawMouseoverTooltip", cancellable = true)
    public void skyblocker$drawMouseOverTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
        // Hide Empty Tooltips
        if (Utils.isOnSkyblock() && SkyblockerConfig.get().general.hideEmptyTooltips && this.focusedSlot != null && focusedSlot.getStack().getName().getString().equals(" ")) {
            ci.cancel();
        }

        // Backpack Preview
        boolean shiftDown = SkyblockerConfig.get().general.backpackPreviewWithoutShift ^ Screen.hasShiftDown();
        if (this.client != null && this.client.player != null && this.focusedSlot != null && shiftDown && this.getTitle().getString().equals("Storage") && this.focusedSlot.inventory != this.client.player.getInventory() && BackpackPreview.renderPreview(context, this.focusedSlot.getIndex(), x, y)) {
            ci.cancel();
        }
    }

    @Redirect(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;", ordinal = 0))
    private ItemStack skyblocker$experimentSolvers$replaceTooltipDisplayStack(Slot slot) {
        return skyblocker$experimentSolvers$getStack(slot, null);
    }

    @ModifyVariable(method = "drawSlot", at = @At(value = "LOAD", ordinal = 4), ordinal = 0)
    private ItemStack skyblocker$experimentSolvers$replaceDisplayStack(ItemStack stack, DrawContext context, Slot slot) {
        return skyblocker$experimentSolvers$getStack(slot, stack);
    }

    @Inject(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V"), cancellable = true)
    private void skyblocker$addTooltipComponent(DrawContext context, int x, int y, CallbackInfo ci) {
        if (this.focusedSlot == null || this.client == null) return;
        ItemStack stack = this.focusedSlot.getStack();
        String internalName = ItemRegistry.getInternalName(stack);
        // PERSONAL COMPACTOR
        if (internalName.contains("PERSONAL_COMPACTOR_") || internalName.contains("PERSONAL_DELETOR_")) {
            String prefix;
            String itemSlotPrefix;
            if (internalName.contains("PERSONAL_COMPACTOR_")) {
                prefix = "PERSONAL_COMPACTOR_";
                itemSlotPrefix = "personal_compact_";
            } else {
                prefix = "PERSONAL_DELETOR_";
                itemSlotPrefix = "personal_deletor_";
            }

            // Find the line to insert component
            int targetIndex = -1;
            int lineCount = 0;
            List<Text> tooltips = Screen.getTooltipFromItem(this.client, stack);
            for (int i = 0; i < tooltips.size(); i++) {
                if (tooltips.get(i).getString().isEmpty()) {
                    lineCount += 1;
                }
                if (lineCount == 2) {
                    targetIndex = i;
                    break;
                }
            }
            if (targetIndex == -1) return;
            List<TooltipComponent> components = new java.util.ArrayList<>(tooltips.stream().map(Text::asOrderedText).map(TooltipComponent::of).toList());

            // STUFF
            String internalID = ItemRegistry.getInternalName(stack);
            String compactorType = internalID.replaceFirst(prefix, "");
            int[] dimensions = personalCompactorTypeToSlot.containsKey(compactorType) ? personalCompactorTypeToSlot.get(compactorType) : personalCompactorTypeToSlot.get("default");

            NbtCompound nbt = stack.getNbt();
            if (nbt == null || !nbt.contains("ExtraAttributes", 10)) {
                return;
            }
            NbtCompound extraAttributes = nbt.getCompound("ExtraAttributes");
            Set<String> attributesKeys = extraAttributes.getKeys();
            List<String> compactorItems = attributesKeys.stream().filter(s -> s.contains(itemSlotPrefix)).toList();
            Map<Integer, ItemStack> slotAndItem = new HashMap<>();

            if (compactorItems.isEmpty()) {
                int slotsCount = (dimensions[0] * dimensions[1]);
                components.add(targetIndex, TooltipComponent.of(Text.literal(
                         slotsCount + (slotsCount == 1 ? " slot": " slots"))
                        .fillStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)).asOrderedText()));

                ((DrawContextInvoker) context).invokeDrawTooltip(textRenderer, components, x, y, HoveredTooltipPositioner.INSTANCE);
                ci.cancel();
                return;
            }

            compactorItems.forEach(s -> slotAndItem.put(getNumberAtEnd(s, itemSlotPrefix), ItemRegistry.getItemStack(extraAttributes.getString(s))));


            components.add(targetIndex, new CompactorPreviewTooltipComponent(slotAndItem, dimensions));
            components.add(targetIndex, TooltipComponent.of(Text.literal(" ").append(
                            Text.literal("Contents:").fillStyle(Style.EMPTY
                                    .withItalic(true)))
                    .asOrderedText()));
            if (attributesKeys.stream().anyMatch(s -> s.contains("PERSONAL_DELETOR_ACTIVE"))) {
                MutableText isActiveText = Text.literal("Active: ");
                if (extraAttributes.getBoolean("PERSONAL_DELETOR_ACTIVE")) {
                    components.add(targetIndex, TooltipComponent.of(isActiveText.append(
                                    Text.literal("YES").fillStyle(Style.EMPTY.withBold(true).withColor(Formatting.GREEN))
                            ).asOrderedText()
                    ));
                } else {
                    components.add(targetIndex, TooltipComponent.of(isActiveText.append(
                                    Text.literal("NO").fillStyle(Style.EMPTY.withBold(true).withColor(Formatting.RED))
                            ).asOrderedText()
                    ));
                }
            }
            ((DrawContextInvoker) context).invokeDrawTooltip(textRenderer, components, x, y, HoveredTooltipPositioner.INSTANCE);
            ci.cancel();

        }
    }

    @Unique
    private ItemStack skyblocker$experimentSolvers$getStack(Slot slot, ItemStack stack) {
        ContainerSolver currentSolver = SkyblockerMod.getInstance().containerSolverManager.getCurrentSolver();
        if ((currentSolver instanceof SuperpairsSolver || currentSolver instanceof UltrasequencerSolver) && ((ExperimentSolver) currentSolver).getState() == ExperimentSolver.State.SHOW && slot.inventory instanceof SimpleInventory) {
            ItemStack itemStack = ((ExperimentSolver) currentSolver).getSlots().get(slot.getIndex());
            return itemStack == null ? slot.getStack() : itemStack;
        }
        return (stack != null) ? stack : slot.getStack();
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;clickSlot(IIILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V"))
    private void skyblocker$experimentSolvers$onSlotClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (slot != null) {
            ContainerSolver currentSolver = SkyblockerMod.getInstance().containerSolverManager.getCurrentSolver();
            if (currentSolver instanceof ExperimentSolver experimentSolver && experimentSolver.getState() == ExperimentSolver.State.SHOW && slot.inventory instanceof SimpleInventory) {
                if (experimentSolver instanceof ChronomatronSolver chronomatronSolver) {
                    Item item = chronomatronSolver.getChronomatronSlots().get(chronomatronSolver.getChronomatronCurrentOrdinal());
                    if ((slot.getStack().isOf(item) || ChronomatronSolver.TERRACOTTA_TO_GLASS.get(slot.getStack().getItem()) == item) && chronomatronSolver.incrementChronomatronCurrentOrdinal() >= chronomatronSolver.getChronomatronSlots().size()) {
                        chronomatronSolver.setState(ExperimentSolver.State.END);
                    }
                } else if (experimentSolver instanceof SuperpairsSolver superpairsSolver) {
                    superpairsSolver.setSuperpairsPrevClickedSlot(slot.getIndex());
                    superpairsSolver.setSuperpairsCurrentSlot(ItemStack.EMPTY);
                } else if (experimentSolver instanceof UltrasequencerSolver ultrasequencerSolver && slot.getIndex() == ultrasequencerSolver.getUltrasequencerNextSlot()) {
                    int count = ultrasequencerSolver.getSlots().get(ultrasequencerSolver.getUltrasequencerNextSlot()).getCount() + 1;
                    ultrasequencerSolver.getSlots().entrySet().stream().filter(entry -> entry.getValue().getCount() == count).findAny().map(Map.Entry::getKey).ifPresentOrElse(ultrasequencerSolver::setUltrasequencerNextSlot, () -> ultrasequencerSolver.setState(ExperimentSolver.State.END));
                }
            }
        }
    }

    @Unique
    private static Integer getNumberAtEnd(String str, String attributesKey) {
        try {
            String numberPartOfTheString = str.replace(attributesKey, "");
            return Integer.parseInt(numberPartOfTheString);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
