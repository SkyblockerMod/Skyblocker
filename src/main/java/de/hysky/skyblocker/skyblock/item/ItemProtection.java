package de.hysky.skyblocker.skyblock.item;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class ItemProtection {
	///TO-DO fix image reference for fancy of classic display
	public static final Identifier ITEM_PROTECTION_TEX = SkyblockerMod.id("textures/gui/classic_item_protected.png");
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

	public static boolean isItemProtected(ItemStack stack) {
		if (stack == null) return false;
		else {
			String itemUuid = stack.getUuid();
			if (!itemUuid.isEmpty()) return SkyblockerConfigManager.get().general.protectedItems.contains(itemUuid);
			else {
				String SkyblockId = stack.getSkyblockId();
				return SkyblockerConfigManager.get().general.protectedItems.contains(SkyblockId);
			}
		}
	}

	private static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
		dispatcher.register(ClientCommands.literal("skyblocker")
				.then(ClientCommands.literal("protectItem")
						.executes(context -> protectMyItem(context.getSource()))));
	}

	private static int protectMyItem(FabricClientCommandSource source) {
		ItemStack heldItem = source.getPlayer().getMainHandItem();
		String itemUuid = heldItem.getUuid();
		String skyblockId = heldItem.getSkyblockId();

		if (Utils.isOnSkyblock()) {
			if (!itemUuid.isEmpty()) {
				if (!SkyblockerConfigManager.get().general.protectedItems.contains(itemUuid)) {
					SkyblockerConfigManager.update(config -> config.general.protectedItems.add(itemUuid));
					source.sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.itemProtection.uniqueAdded", heldItem.getHoverName())));
				} else {
					SkyblockerConfigManager.update(config -> config.general.protectedItems.remove(itemUuid));
					source.sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.itemProtection.uniqueRemoved", heldItem.getHoverName())));
				}
			} else {
				if (!SkyblockerConfigManager.get().general.protectedItems.contains(skyblockId)) {
					SkyblockerConfigManager.update(config -> config.general.protectedItems.add(skyblockId));
					source.sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.itemProtection.genericAdded", heldItem.getHoverName())));
				} else {
					SkyblockerConfigManager.update(config -> config.general.protectedItems.remove(skyblockId));
					source.sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.itemProtection.genericRemoved", heldItem.getHoverName())));
				}
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
		String skyblockId = heldItem.getSkyblockId();
		if (!itemUuid.isEmpty()) {
			if (!SkyblockerConfigManager.get().general.protectedItems.contains(itemUuid)) {
				SkyblockerConfigManager.update(config -> config.general.protectedItems.add(itemUuid));
				if (notifyConfiguration) {
					playerEntity.sendSystemMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.itemProtection.uniqueAdded", heldItem.getHoverName())));
				}
			} else {
				SkyblockerConfigManager.update(config -> config.general.protectedItems.remove(itemUuid));
				if (notifyConfiguration) {
					playerEntity.sendSystemMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.itemProtection.uniqueRemoved", heldItem.getHoverName())));
				}
			}
		} else {
			if (!SkyblockerConfigManager.get().general.protectedItems.contains(skyblockId)) {
				SkyblockerConfigManager.update(config -> config.general.protectedItems.add(skyblockId));
				if (notifyConfiguration) {
					playerEntity.sendSystemMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.itemProtection.genericAdded", heldItem.getHoverName())));
				}
			} else {
				SkyblockerConfigManager.update(config -> config.general.protectedItems.remove(skyblockId));
				if (notifyConfiguration) {
					playerEntity.sendSystemMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.itemProtection.genericRemoved", heldItem.getHoverName())));
				}
			}
		}
	}

	public static void handleHotbarKeyPressed(LocalPlayer player) {
		while (itemProtection.consumeClick()) {
			ItemStack heldItem = player.getMainHandItem();
			handleKeyPressed(heldItem);
		}
	}

	private static InteractionResult onEntityInteract(Player playerEntity, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult entityHitResult) {
		if (!Utils.isOnSkyblock() || !world.isClientSide()) return InteractionResult.PASS;

		Location location = Utils.getLocation();
		if (!(location == Location.PRIVATE_ISLAND || location == Location.GARDEN)) {
			return InteractionResult.PASS;
		}
		if (entity instanceof ItemFrame itemFrame && itemFrame.getItem().isEmpty()) {
			if (isItemProtected(playerEntity.getItemInHand(hand)) || HotbarSlotLock.isLocked(playerEntity.getInventory().getSelectedSlot())) {
				playerEntity.sendSystemMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.itemProtection.triggered")));
				return InteractionResult.FAIL;
			}
		}
		return InteractionResult.PASS;
	}
}

