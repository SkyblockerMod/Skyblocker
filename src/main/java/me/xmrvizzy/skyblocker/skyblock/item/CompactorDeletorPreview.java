package me.xmrvizzy.skyblocker.skyblock.item;

import me.xmrvizzy.skyblocker.mixin.accessor.DrawContextInvoker;
import me.xmrvizzy.skyblocker.skyblock.itemlist.ItemRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CompactorDeletorPreview {

    private static final MinecraftClient mcClient = MinecraftClient.getInstance();
    private static final Map<String, int[]> personalCompactorTypeToSlot = new HashMap<>();
    // Lines, and slots per lines
    static {
        personalCompactorTypeToSlot.put("4000", new int[]{1,1});
        personalCompactorTypeToSlot.put("5000", new int[]{1,3});
        personalCompactorTypeToSlot.put("6000", new int[]{1,7});
        personalCompactorTypeToSlot.put("7000", new int[]{2,6});
        personalCompactorTypeToSlot.put("default", new int[]{1,6});
    }

    public static boolean displayCompactorDeletorPreview(DrawContextInvoker context, int x, int y, ItemStack stack) {
        String internalName = ItemRegistry.getInternalName(stack);

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

        List<Text> tooltips = Screen.getTooltipFromItem(mcClient, stack);
        for (int i = 0; i < tooltips.size(); i++) {
            if (tooltips.get(i).getString().isEmpty()) {
                lineCount += 1;
            }
            if (lineCount == 2) {
                targetIndex = i;
                break;
            }
        }
        if (targetIndex == -1) return false;
        List<TooltipComponent> components = new java.util.ArrayList<>(tooltips.stream().map(Text::asOrderedText).map(TooltipComponent::of).toList());

        // STUFF
        String internalID = ItemRegistry.getInternalName(stack);
        String compactorType = internalID.replaceFirst(prefix, "");
        int[] dimensions = personalCompactorTypeToSlot.containsKey(compactorType) ? personalCompactorTypeToSlot.get(compactorType) : personalCompactorTypeToSlot.get("default");

        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains("ExtraAttributes", 10)) {
            return false;
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

            context.invokeDrawTooltip(mcClient.textRenderer, components, x, y, HoveredTooltipPositioner.INSTANCE);
            return true;
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
        context.invokeDrawTooltip(mcClient.textRenderer, components, x, y, HoveredTooltipPositioner.INSTANCE);
        return true;
    }

    private static Integer getNumberAtEnd(String str, String attributesKey) {
        try {
            String numberPartOfTheString = str.replace(attributesKey, "");
            return Integer.parseInt(numberPartOfTheString);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
