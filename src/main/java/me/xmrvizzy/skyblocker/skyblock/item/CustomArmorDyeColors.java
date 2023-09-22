package me.xmrvizzy.skyblocker.skyblock.item;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class CustomArmorDyeColors {
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register(CustomArmorDyeColors::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("skyblocker")
                .then(ClientCommandManager.literal("custom")
                        .then(ClientCommandManager.literal("dyeColor")
                                .executes(context -> customizeDyeColor(context.getSource(), null))
                                .then(ClientCommandManager.argument("hexCode", StringArgumentType.string())
                                        .executes(context -> customizeDyeColor(context.getSource(), StringArgumentType.getString(context, "hexCode")))))));
    }

    @SuppressWarnings("SameReturnValue")
    private static int customizeDyeColor(FabricClientCommandSource source, String hex) {
        ItemStack heldItem = source.getPlayer().getMainHandStack();
        NbtCompound nbt = (heldItem != null) ? heldItem.getNbt() : null;

        if (hex != null && !isHexadecimalColor(hex)) {
            source.sendError(Text.translatable("skyblocker.customDyeColors.invalidHex"));
            return Command.SINGLE_SUCCESS;
        }

        if (Utils.isOnSkyblock() && heldItem != null) {
            if (heldItem.getItem() instanceof DyeableItem) {
                if (nbt != null && nbt.contains("ExtraAttributes")) {
                    NbtCompound extraAttributes = nbt.getCompound("ExtraAttributes");
                    String itemUuid = extraAttributes.contains("uuid") ? extraAttributes.getString("uuid") : null;

                    if (itemUuid != null) {
                        Object2IntOpenHashMap<String> customDyeColors = SkyblockerConfig.get().general.customDyeColors;

                        if (hex == null) {
                            if (customDyeColors.containsKey(itemUuid)) {
                                customDyeColors.removeInt(itemUuid);
                                SkyblockerConfig.save();
                                source.sendFeedback(Text.translatable("skyblocker.customDyeColors.removed"));
                            } else {
                                source.sendFeedback(Text.translatable("skyblocker.customDyeColors.neverHad"));
                            }
                        } else {
                            customDyeColors.put(itemUuid, Integer.decode("0x" + hex.replace("#", "")).intValue());
                            SkyblockerConfig.save();
                            source.sendFeedback(Text.translatable("skyblocker.customDyeColors.added"));
                        }
                    } else {
                        source.sendError(Text.translatable("skyblocker.customDyeColors.noItemUuid"));
                    }
                }
            } else {
                source.sendError(Text.translatable("skyblocker.customDyeColors.notDyeable"));
                return Command.SINGLE_SUCCESS;
            }
        } else {
            source.sendError(Text.translatable("skyblocker.customDyeColors.unableToSetColor"));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static boolean isHexadecimalColor(String s) {
        return s.replace("#", "").chars().allMatch(c -> "0123456789ABCDEFabcdef".indexOf(c) >= 0) && s.replace("#", "").length() == 6;
    }
}
