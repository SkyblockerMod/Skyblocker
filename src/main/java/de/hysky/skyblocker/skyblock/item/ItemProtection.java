package de.hysky.skyblocker.skyblock.item;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class ItemProtection {
	public static final Identifier ITEM_PROTECTION_TEX = SkyblockerMod.id("item_protection");
	public static KeyMapping itemProtection;

	@Init
	public static void init() {
		itemProtection = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.skyblocker.itemProtection",
				GLFW.GLFW_KEY_V,
				SkyblockerMod.KEYBINDING_CATEGORY
		));
		ClientCommandRegistrationCallback.EVENT.register(ItemProtection::registerCommand);
		UseEntityCallback.EVENT.register(ItemProtection::onEntityInteract);
	}

	public static void drawSlotIcon(GuiGraphicsExtractor graphics, int slotX, int slotY) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ItemProtection.ITEM_PROTECTION_TEX, slotX, slotY, 16, 16);
	}

	public static boolean isItemProtected(@Nullable ItemStack stack) {
		if (stack == null) return false;
		String itemUuid = stack.getUuid();
		return SkyblockerConfigManager.get().general.protectedItems.contains(itemUuid);
	}

	private static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
		dispatcher.register(ClientCommands.literal("skyblocker")
				.then(ClientCommands.literal("protectItem")
						.executes(context -> protectMyItem(context.getSource()))));
	}

	private static int protectMyItem(FabricClientCommandSource source) {
		ItemStack heldItem = source.getPlayer().getMainHandItem();

		if (Utils.isOnSkyblock()) {
			String itemUuid = heldItem.getUuid();

			if (!itemUuid.isEmpty()) {
				if (!SkyblockerConfigManager.get().general.protectedItems.contains(itemUuid)) {
					SkyblockerConfigManager.update(config -> config.general.protectedItems.add(itemUuid));
					source.sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.itemProtection.added", heldItem.getHoverName())));
				} else {
					SkyblockerConfigManager.update(config -> config.general.protectedItems.remove(itemUuid));
					source.sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.itemProtection.removed", heldItem.getHoverName())));
				}
			} else {
				source.sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.itemProtection.noItemUuid")));
			}
		} else {
			source.sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.itemProtection.unableToProtect")));
		}

		return Command.SINGLE_SUCCESS;
	}

	public static void handleKeyPressed(ItemStack heldItem) {
		boolean notifyConfiguration = SkyblockerConfigManager.get().general.itemProtection.displayChatNotification;

		Player playerEntity = Minecraft.getInstance().player;
		if (playerEntity == null) {
			return;
		}
		if (!Utils.isOnSkyblock()) {
			playerEntity.sendSystemMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.itemProtection.unableToProtect")));
			return;
		}

		if (heldItem.isEmpty()) {
			playerEntity.sendSystemMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.itemProtection.noItemUuid")));
			return;
		}

		String itemUuid = heldItem.getUuid();
		if (!itemUuid.isEmpty()) {

			if (!SkyblockerConfigManager.get().general.protectedItems.contains(itemUuid)) {
				SkyblockerConfigManager.update(config -> config.general.protectedItems.add(itemUuid));
				if (notifyConfiguration) {
					playerEntity.sendSystemMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.itemProtection.added", heldItem.getHoverName())));
				}
			} else {
				SkyblockerConfigManager.update(config -> config.general.protectedItems.remove(itemUuid));
				if (notifyConfiguration) {
					playerEntity.sendSystemMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.itemProtection.removed", heldItem.getHoverName())));
				}
			}
		} else {
			playerEntity.sendSystemMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.itemProtection.noItemUuid")));
		}
	}

	public static void handleHotbarKeyPressed(LocalPlayer player) {
		while (itemProtection.consumeClick()) {
			ItemStack heldItem = player.getMainHandItem();
			handleKeyPressed(heldItem);
		}
	}

	public static boolean isPersonalStorage(String screenTitle) {
		return screenTitle.equals("Storage") || screenTitle.startsWith("Storage (")
				|| screenTitle.equals("Rift Storage") || screenTitle.startsWith("Rift Storage (")
				|| screenTitle.startsWith("Ender Chest")
				|| screenTitle.startsWith("Chest")
				|| screenTitle.startsWith("Trapped Chest")
				|| (screenTitle.contains("Backpack") && screenTitle.contains("(Slot #"));
	}

	public static boolean isNpcSellMenu(AbstractContainerMenu menu) {
		for (Slot slot : menu.slots) {
			ItemStack stack = slot.getItem();
			if (stack.isEmpty()) continue;
			String name = stack.getHoverName().getString();
			if (name.equals("Sell Item") || name.equals("Sell Inventory")) return true;
			if (ItemUtils.getLoreLineIf(stack, text -> text.contains("buyback")) != null) return true;
		}
		return false;
	}

	public static boolean isNpcSellButton(Slot slot) {
		String name = slot.getItem().getHoverName().getString();
		return name.equals("Sell Item") || name.equals("Sell Inventory") || ItemUtils.getLoreLineIf(slot.getItem(), text -> text.contains("buyback")) != null;
	}

	private static InteractionResult onEntityInteract(Player playerEntity, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult entityHitResult) {
		if (!Utils.isOnSkyblock() || !world.isClientSide()) return InteractionResult.PASS;

		Location location = Utils.getLocation();
		if (!(location == Location.PRIVATE_ISLAND || location == Location.GARDEN)) {
			return InteractionResult.PASS;
		}
		if (entity instanceof ItemFrame itemFrame && itemFrame.getItem().isEmpty()) {
			if (isItemProtected(playerEntity.getItemInHand(hand)) || HotbarSlotLock.isLocked(playerEntity.getInventory().getSelectedSlot())) {
				return InteractionResult.FAIL;
			}
		}
		return InteractionResult.PASS;
	}
}
