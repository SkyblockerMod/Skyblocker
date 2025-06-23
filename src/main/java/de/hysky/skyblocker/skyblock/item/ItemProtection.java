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
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class ItemProtection {
	public static final Identifier ITEM_PROTECTION_TEX = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/item_protection.png");
	public static KeyBinding itemProtection;

	@Init
	public static void init() {
		itemProtection = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.itemProtection",
				GLFW.GLFW_KEY_V,
				"key.categories.skyblocker"
		));
		ClientCommandRegistrationCallback.EVENT.register(ItemProtection::registerCommand);
		UseEntityCallback.EVENT.register(ItemProtection::onEntityInteract);
	}

	public static boolean isItemProtected(ItemStack stack) {
		if (stack == null) return false;
		String itemUuid = ItemUtils.getItemUuid(stack);
		return SkyblockerConfigManager.get().general.protectedItems.contains(itemUuid);
	}

	private static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(ClientCommandManager.literal("skyblocker")
				.then(ClientCommandManager.literal("protectItem")
						.executes(context -> protectMyItem(context.getSource()))));
	}

	private static int protectMyItem(FabricClientCommandSource source) {
		ItemStack heldItem = source.getPlayer().getMainHandStack();

		if (Utils.isOnSkyblock()) {
			String itemUuid = ItemUtils.getItemUuid(heldItem);

			if (!itemUuid.isEmpty()) {
				ObjectOpenHashSet<String> protectedItems = SkyblockerConfigManager.get().general.protectedItems;

				if (!protectedItems.contains(itemUuid)) {
					protectedItems.add(itemUuid);
					SkyblockerConfigManager.save();

					source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemProtection.added", heldItem.getName())));
				} else {
					protectedItems.remove(itemUuid);
					SkyblockerConfigManager.save();

					source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemProtection.removed", heldItem.getName())));
				}
			} else {
				source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemProtection.noItemUuid")));
			}
		} else {
			source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemProtection.unableToProtect")));
		}

		return Command.SINGLE_SUCCESS;
	}

	public static void handleKeyPressed(ItemStack heldItem) {
		PlayerEntity playerEntity = MinecraftClient.getInstance().player;
		if (playerEntity == null) {
			return;
		}
		if (!Utils.isOnSkyblock()) {
			playerEntity.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemProtection.unableToProtect")), false);
			return;
		}

		if (heldItem.isEmpty()) {
			playerEntity.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemProtection.noItemUuid")), false);
			return;
		}

		String itemUuid = ItemUtils.getItemUuid(heldItem);
		if (!itemUuid.isEmpty()) {
			ObjectOpenHashSet<String> protectedItems = SkyblockerConfigManager.get().general.protectedItems;

			if (!protectedItems.contains(itemUuid)) {
				protectedItems.add(itemUuid);
				SkyblockerConfigManager.save();

				playerEntity.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemProtection.added", heldItem.getName())), false);
			} else {
				protectedItems.remove(itemUuid);
				SkyblockerConfigManager.save();

				playerEntity.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemProtection.removed", heldItem.getName())), false);
			}
		} else {
			playerEntity.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemProtection.noItemUuid")), false);
		}
	}

	public static void handleHotbarKeyPressed(ClientPlayerEntity player) {
		while (itemProtection.wasPressed()) {
			ItemStack heldItem = player.getMainHandStack();
			handleKeyPressed(heldItem);
		}
	}

	private static ActionResult onEntityInteract(PlayerEntity playerEntity, World world, Hand hand, Entity entity, @Nullable EntityHitResult entityHitResult) {
		if (!Utils.isOnSkyblock() || !world.isClient) return ActionResult.PASS;

		Location location = Utils.getLocation();
		if (!(location == Location.PRIVATE_ISLAND || location == Location.GARDEN)) {
			return ActionResult.PASS;
		}
		if (entity instanceof ItemFrameEntity itemFrame && itemFrame.getHeldItemStack().isEmpty()) {
			if (isItemProtected(playerEntity.getStackInHand(hand)) || HotbarSlotLock.isLocked(playerEntity.getInventory().getSelectedSlot())) {
				return ActionResult.FAIL;
			}
		}
		return ActionResult.PASS;
	}
}
