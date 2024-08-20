package de.hysky.skyblocker.skyblock.item;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.BazaarPriceTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.LBinTooltip;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    // TODO: fix pet items
    public static void itemPriceLookup(ClientPlayerEntity player, @NotNull Slot slot) {
        ItemStack stack = ItemRepository.getItemStack(slot.getStack().getNeuName());
        if (stack != null && !stack.isEmpty()) {
            String itemName = Formatting.strip(stack.getName().getString());
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
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        TooltipInfoType.NPC.downloadIfEnabled(futureList);
        TooltipInfoType.BAZAAR.downloadIfEnabled(futureList);
        TooltipInfoType.LOWEST_BINS.downloadIfEnabled(futureList);
        TooltipInfoType.ONE_DAY_AVERAGE.downloadIfEnabled(futureList);
        TooltipInfoType.THREE_DAY_AVERAGE.downloadIfEnabled(futureList);
        CompletableFuture.allOf(futureList.toArray(CompletableFuture[]::new))
                .thenRun(() -> player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.helpers.itemPrice.refreshedItemPrices"))))
                .exceptionally(e -> {
                    ItemTooltip.LOGGER.error("[Skyblocker] Failed to refresh item prices", e);
                    player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.helpers.itemPrice.itemPriceRefreshFailed")));
                    return null;
                });
    }
}
