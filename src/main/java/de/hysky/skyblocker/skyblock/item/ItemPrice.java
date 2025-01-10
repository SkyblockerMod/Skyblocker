package de.hysky.skyblocker.skyblock.item;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.ItemPriceUpdateEvent;
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
import org.apache.commons.text.WordUtils;
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
	 * <h2>Crucial init method, do not remove.</h2>
	 *
	 * <p>This is required due to the way keybindings are registered via Fabric api and lazy static initialization.</p>
	 * <p>
	 *     Key bindings are required to be registered before {@link net.minecraft.client.MinecraftClient#options MinecraftClient#options} is initialized.
	 *     This is probably due to how fabric adds key binding options to the key binding options screen.
	 *     Since {@link #ITEM_PRICE_LOOKUP} and {@link #ITEM_PRICE_REFRESH} are static fields, they are initialized lazily, which means they are only initialized when the class is accessed for the first time.
	 *     That first time is generally when the player is already in the game and tries to use the key bindings in a handled screen, which is much later than the possible initialization period.
	 *     This causes an {@link IllegalStateException} to be thrown from {@link net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl#registerKeyBinding(KeyBinding) KeyBindingRegistryImpl#registerKeybinding} and the game to crash.
	 * </p>
	 */
	@SuppressWarnings("UnstableApiUsage") //For the javadoc reference.
	@Init
	public static void init() {}

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
                itemName = WordUtils.capitalizeFully(skyblockApiId.replace("ENCHANTMENT_", "").replaceAll("_\\d+", ""));
            }

            // Search up the item in the bazaar or auction house
            if (TooltipInfoType.BAZAAR.hasOrNullWarning(skyblockApiId)) {
                MessageScheduler.INSTANCE.sendMessageAfterCooldown("/bz " + itemName, true);
            } else if (TooltipInfoType.LOWEST_BINS.hasOrNullWarning(skyblockApiId)) {
                MessageScheduler.INSTANCE.sendMessageAfterCooldown("/ahsearch " + itemName, true);
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
	        ItemPriceUpdateEvent.ON_PRICE_UPDATE.invoker().onPriceUpdate();
	        player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.helpers.itemPrice.refreshedItemPrices")), false);
		}).exceptionally(e -> {
			ItemTooltip.LOGGER.error("[Skyblocker Item Price] Failed to refresh item prices", e);
			player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.helpers.itemPrice.itemPriceRefreshFailed")), false);
			return null;
		});
    }
}
