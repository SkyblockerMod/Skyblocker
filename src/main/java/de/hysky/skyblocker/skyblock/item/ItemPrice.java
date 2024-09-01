package de.hysky.skyblocker.skyblock.item;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.info.DataTooltipInfoType;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ItemPrice {
    public static final KeyBinding ITEM_PRICE_LOOKUP = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.itemPriceLookup",
            GLFW.GLFW_KEY_F6,
            "key.categories.skyblocker"
    ));
    public static final KeyBinding ITEM_PRICE_REFRESH = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.itemPriceRefresh",
            GLFW.GLFW_KEY_Z,
            "key.categories.skyblocker"
    ));

    public static void itemPriceLookup(ClientPlayerEntity player, @NotNull Slot slot) {
        ItemStack stack = ItemRepository.getItemStack(slot.getStack().getNeuName());
        if (stack != null && !stack.isEmpty()) {
            String itemName = Formatting.strip(stack.getName().getString()).replaceFirst("\\[Lvl \\d+ ➡ \\d+] ", "");
            if (TooltipInfoType.BAZAAR.getData() != null) {
                MessageScheduler.INSTANCE.sendMessageAfterCooldown("/bz " + itemName);
            } else if (TooltipInfoType.LOWEST_BINS.getData() != null) {
                MessageScheduler.INSTANCE.sendMessageAfterCooldown("/ahsearch " + itemName);
            }
        } else {
            player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.helpers.itemPrice.itemPriceLookupFailed")));
        }
    }

    public static void refreshItemPrices(ClientPlayerEntity player) {
        player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.helpers.itemPrice.refreshingItemPrices")));
        CompletableFuture.allOf(Stream.of(TooltipInfoType.NPC, TooltipInfoType.BAZAAR, TooltipInfoType.LOWEST_BINS, TooltipInfoType.ONE_DAY_AVERAGE, TooltipInfoType.THREE_DAY_AVERAGE)
                        .map(DataTooltipInfoType::downloadIfEnabled)
                        .toArray(CompletableFuture[]::new)
                ).thenRun(() -> player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.helpers.itemPrice.refreshedItemPrices"))))
                .exceptionally(e -> {
                    ItemTooltip.LOGGER.error("[Skyblocker Item Price] Failed to refresh item prices", e);
                    player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.helpers.itemPrice.itemPriceRefreshFailed")));
                    return null;
                });
    }
}
