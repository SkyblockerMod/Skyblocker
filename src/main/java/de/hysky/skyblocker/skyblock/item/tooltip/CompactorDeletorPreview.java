package de.hysky.skyblocker.skyblock.item.tooltip;

import de.hysky.skyblocker.mixins.accessors.DrawContextInvoker;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CompactorDeletorPreview {
    /**
     * The width and height in slots of the compactor/deletor
     */
    private static final Map<String, IntIntPair> DIMENSIONS = Map.of(
            "4000", IntIntPair.of(1, 1),
            "5000", IntIntPair.of(1, 3),
            "6000", IntIntPair.of(1, 7),
            "7000", IntIntPair.of(2, 6)
    );
    private static final IntIntPair DEFAULT_DIMENSION = IntIntPair.of(1, 6);
    public static final Pattern NAME = Pattern.compile("PERSONAL_(?<type>COMPACTOR|DELETOR)_(?<size>\\d+)");
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static boolean drawPreview(DrawContext context, ItemStack stack, String type, String size, int x, int y) {
        List<Text> tooltips = Screen.getTooltipFromItem(client, stack);
        int targetIndex = getTargetIndex(tooltips);
        if (targetIndex == -1) return false;

        // Get items in compactor or deletor
        NbtCompound customData = ItemUtils.getCustomData(stack);
        // Get the slots and their items from the nbt, which is in the format personal_compact_<slot_number> or personal_deletor_<slot_number>
        List<IntObjectPair<ItemStack>> slots = customData.getKeys().stream().filter(slot -> slot.contains(type.toLowerCase().substring(0, 7))).map(slot -> IntObjectPair.of(Integer.parseInt(slot.substring(17)), ItemRepository.getItemStack(customData.getString(slot)))).toList();

        List<TooltipComponent> components = tooltips.stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());
        IntIntPair dimensions = DIMENSIONS.getOrDefault(size, DEFAULT_DIMENSION);

        // If there are no items in compactor or deletor
        if (slots.isEmpty()) {
            int slotsCount = dimensions.leftInt() * dimensions.rightInt();
            components.add(targetIndex, TooltipComponent.of(Text.literal(slotsCount + (slotsCount == 1 ? " slot" : " slots")).formatted(Formatting.GRAY).asOrderedText()));

            ((DrawContextInvoker) context).invokeDrawTooltip(client.textRenderer, components, x, y, HoveredTooltipPositioner.INSTANCE);
            return true;
        }

        // Add the preview tooltip component
        components.add(targetIndex, new CompactorPreviewTooltipComponent(slots, dimensions));

        if (customData.contains("PERSONAL_DELETOR_ACTIVE")) {
            components.add(targetIndex, TooltipComponent.of(Text.literal("Active: ")
                    .append(customData.getBoolean("PERSONAL_DELETOR_ACTIVE") ? Text.literal("YES").formatted(Formatting.BOLD).formatted(Formatting.GREEN) : Text.literal("NO").formatted(Formatting.BOLD).formatted(Formatting.RED)).asOrderedText()));
        }
        ((DrawContextInvoker) context).invokeDrawTooltip(client.textRenderer, components, x, y, HoveredTooltipPositioner.INSTANCE);
        return true;
    }

    /**
     * Finds the target index to insert the preview component, which is the second empty line
     */
    private static int getTargetIndex(List<Text> tooltips) {
        int targetIndex = -1;
        int lineCount = 0;
        for (int i = 0; i < tooltips.size(); i++) {
            if (tooltips.get(i).getString().isEmpty()) {
                lineCount += 1;
            }
            if (lineCount == 2) {
                targetIndex = i;
                break;
            }
        }
        return targetIndex;
    }
}
