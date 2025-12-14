package de.hysky.skyblocker.skyblock.item.custom;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.command.argumenttypes.color.ColorArgumentType;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

public class CustomArmorDyeColors {
	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register(CustomArmorDyeColors::registerCommands);
	}

	private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
		dispatcher.register(ClientCommandManager.literal("skyblocker")
				.then(ClientCommandManager.literal("custom")
						.then(ClientCommandManager.literal("dyeColor")
								.executes(context -> customizeDyeColor(context.getSource(), Integer.MIN_VALUE))
								.then(ClientCommandManager.argument("hexCode", ColorArgumentType.hex())
										.executes(context -> customizeDyeColor(context.getSource(), ColorArgumentType.getIntFromHex(context, "hexCode")))))));
	}

	@SuppressWarnings("SameReturnValue")
	private static int customizeDyeColor(FabricClientCommandSource source, int color) {
		ItemStack heldItem = source.getPlayer().getMainHandItem();

		if (Utils.isOnSkyblock() && heldItem != null) {
			if (heldItem.is(ItemTags.DYEABLE)) {
				String itemUuid = heldItem.getUuid();

				if (!itemUuid.isEmpty()) {
					Object2IntOpenHashMap<String> customDyeColors = SkyblockerConfigManager.get().general.customDyeColors;

					if (color == Integer.MIN_VALUE) {
						if (customDyeColors.containsKey(itemUuid)) {
							SkyblockerConfigManager.update(config -> config.general.customDyeColors.removeInt(itemUuid));
							source.sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.customDyeColors.removed")));
						} else {
							source.sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.customDyeColors.neverHad")));
						}
					} else {
						SkyblockerConfigManager.update(config -> config.general.customDyeColors.put(itemUuid, color));
						source.sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.customDyeColors.added")));
					}
				} else {
					source.sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.customDyeColors.noItemUuid")));
				}
			} else {
				source.sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.customDyeColors.notDyeable")));
				return Command.SINGLE_SUCCESS;
			}
		} else {
			source.sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.customDyeColors.unableToSetColor")));
		}

		return Command.SINGLE_SUCCESS;
	}
}
