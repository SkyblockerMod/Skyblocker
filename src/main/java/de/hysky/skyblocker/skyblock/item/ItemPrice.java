package de.hysky.skyblocker.skyblock.item;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.ItemPriceUpdateEvent;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.info.DataTooltipInfoType;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import com.mojang.brigadier.Command;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ItemPrice {
	public static final KeyMapping ITEM_PRICE_LOOKUP = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.skyblocker.itemPriceLookup",
			GLFW.GLFW_KEY_F6,
			SkyblockerMod.KEYBINDING_CATEGORY
	));

	/**
	 * <h2>Crucial init method, do not remove.</h2>
	 *
	 * <p>This is required due to the way keybindings are registered via Fabric api and lazy static initialization.</p>
	 * <p>
	 *     Key bindings are required to be registered before {@link net.minecraft.client.Minecraft#options MinecraftClient#options} is initialized.
	 *     This is probably due to how fabric adds key binding options to the key binding options screen.
	 *     Since {@link #ITEM_PRICE_LOOKUP} is a static field, it is initialized lazily, which means it is only initialized when the class is accessed for the first time.
	 *     That first time is generally when the player is already in the game and tries to use the key bindings in a handled screen, which is much later than the possible initialization period.
	 *     This causes an {@link IllegalStateException} to be thrown from {@link net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl#registerKeyBinding(KeyMapping) KeyBindingRegistryImpl#registerKeybinding} and the game to crash.
	 * </p>
	 */
	@SuppressWarnings("UnstableApiUsage") //For the javadoc reference.
	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
					.then(ClientCommandManager.literal("refreshPrices").executes(context -> {
						refreshItemPrices(context.getSource().getPlayer());
						return Command.SINGLE_SUCCESS;
					})));
		});
	}

	public static void itemPriceLookup(LocalPlayer player, Slot slot) {
		ItemStack stack = slot.getItem();
		itemPriceLookup(player, stack);
	}

	public static void itemPriceLookup(LocalPlayer player, ItemStack stack) {
		String skyblockApiId = stack.getSkyblockApiId();
		ItemStack neuStack = ItemRepository.getItemStack(stack.getNeuName());
		if (neuStack != null && !neuStack.isEmpty()) {
			String itemName = ChatFormatting.stripFormatting(neuStack.getHoverName().getString());

			// Handle Pets
			if (stack.getSkyblockId().equals("PET")) {
				itemName = itemName.replaceFirst("\\[Lvl \\d+ ➡ \\d+] ", "");
			}

			// Handle Enchanted Books
			if (itemName.equals("Enchanted Book")) {
				itemName = stack.skyblocker$getLoreStrings().stream().findFirst().orElse("");
			}

			// Search up the item in the bazaar or auction house
			if (TooltipInfoType.BAZAAR.hasOrNullWarning(skyblockApiId)) {
				MessageScheduler.INSTANCE.sendMessageAfterCooldown("/bz " + itemName, true);
				return;
			} else if (TooltipInfoType.LOWEST_BINS.hasOrNullWarning(skyblockApiId)) {
				MessageScheduler.INSTANCE.sendMessageAfterCooldown("/ahsearch " + itemName, true);
				return;
			}
		}

		player.displayClientMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.config.helpers.itemPrice.itemPriceLookupFailed")), false);
	}

	private static void refreshItemPrices(LocalPlayer player) {
		player.displayClientMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.config.helpers.itemPrice.refreshingItemPrices")), false);
		CompletableFuture.allOf(Stream.of(TooltipInfoType.NPC, TooltipInfoType.BAZAAR, TooltipInfoType.LOWEST_BINS, TooltipInfoType.ONE_DAY_AVERAGE, TooltipInfoType.THREE_DAY_AVERAGE)
						.map(DataTooltipInfoType::downloadIfEnabled)
						.toArray(CompletableFuture[]::new)
		).thenRun(() -> {
			ItemPriceUpdateEvent.ON_PRICE_UPDATE.invoker().onPriceUpdate();
			player.displayClientMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.config.helpers.itemPrice.refreshedItemPrices")), false);
		}).exceptionally(e -> {
			ItemTooltip.LOGGER.error("[Skyblocker Item Price] Failed to refresh item prices", e);
			player.displayClientMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.config.helpers.itemPrice.itemPriceRefreshFailed")), false);
			return null;
		});
	}
}
