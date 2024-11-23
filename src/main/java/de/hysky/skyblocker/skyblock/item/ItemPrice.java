package de.hysky.skyblocker.skyblock.item;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.info.DataTooltipInfoType;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.searchoverlay.SearchOverManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
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

	/**
	 * An event that is fired when all prices are updated.
	 */
	public static final Event<OnPriceUpdate> ON_PRICE_UPDATE = EventFactory.createArrayBacked(OnPriceUpdate.class, listeners -> () -> {
		for (OnPriceUpdate listener : listeners) {
			listener.onPriceUpdate();
		}
	});

    public static void itemPriceLookup(ClientPlayerEntity player, @NotNull Slot slot) {
        ItemStack stack = slot.getStack();
        String skyblockApiId = stack.getSkyblockApiId();
        ItemStack neuStack = ItemRepository.getItemStack(stack.getNeuName());
        if (neuStack != null && !neuStack.isEmpty()) {
            String itemName = Formatting.strip(neuStack.getName().getString());

            // Handle Pets
            if (stack.getSkyblockId().equals("PET")) {
                itemName = itemName.replaceFirst("\\[Lvl \\d+ ➡ \\d+] ", "");
            }

            // Handle Enchanted Books
            if (itemName.equals("Enchanted Book")) {
                itemName = SearchOverManager.capitalizeFully(skyblockApiId.replace("ENCHANTMENT_", "").replaceAll("_\\d+", ""));
            }

            // Search up the item in the bazaar or auction house
            if (TooltipInfoType.BAZAAR.hasOrNullWarning(skyblockApiId)) {
                MessageScheduler.INSTANCE.sendMessageAfterCooldown("/bz " + itemName);
            } else if (TooltipInfoType.LOWEST_BINS.hasOrNullWarning(skyblockApiId)) {
                MessageScheduler.INSTANCE.sendMessageAfterCooldown("/ahsearch " + itemName);
            }
        } else {
            player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.helpers.itemPrice.itemPriceLookupFailed")), false);
        }
    }

    public static void refreshItemPrices(ClientPlayerEntity player) {
        player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.helpers.itemPrice.refreshingItemPrices")), false);
        CompletableFuture.allOf(Stream.of(TooltipInfoType.NPC, TooltipInfoType.BAZAAR, TooltipInfoType.LOWEST_BINS, TooltipInfoType.ONE_DAY_AVERAGE, TooltipInfoType.THREE_DAY_AVERAGE)
                        .map(DataTooltipInfoType::downloadIfEnabled)
                        .toArray(CompletableFuture[]::new)
        ).thenRun(() -> {
	        ON_PRICE_UPDATE.invoker().onPriceUpdate();
	        player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.helpers.itemPrice.refreshedItemPrices")), false);
		}).exceptionally(e -> {
			ItemTooltip.LOGGER.error("[Skyblocker Item Price] Failed to refresh item prices", e);
			player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.helpers.itemPrice.itemPriceRefreshFailed")), false);
			return null;
		});
    }

	@FunctionalInterface
	public interface OnPriceUpdate {
		void onPriceUpdate();
	}
}
