package de.hysky.skyblocker.skyblock.profileviewer.inventory.itemLoaders;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import de.hysky.skyblocker.skyblock.PetCache;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerScreen;
import de.hysky.skyblocker.skyblock.profileviewer.inventory.Pet;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.datafixer.LegacyItemStackFixer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.io.ByteArrayInputStream;
import java.util.*;

public class ItemLoader {

    public List<ItemStack> loadItems(JsonObject data) {
        NbtList containerContent = decompress(data);
        List<ItemStack> itemList = new ArrayList<>();

        for (int i = 0; i < containerContent.size(); i++) {
            NbtCompound nbt = containerContent.getCompound(i);
            if (nbt.getInt("id") == 0) {
                itemList.add(ItemStack.EMPTY);
                continue;
            }

            ItemStack stack = LegacyItemStackFixer.fixLegacyStack(nbt);

            if (stack.isEmpty()) {
            	ItemStack fallback = Ico.BARRIER.copy();

            	fallback.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Error: " + nbt.getCompound("tag").getCompound("ExtraAttributes").getString("id")));
                itemList.add(fallback);

                continue;
            }

            String itemId = stack.getSkyblockId();
            NbtCompound customData = ItemUtils.getCustomData(stack);

            if (itemId.equals("PET")) {
                PetCache.PetInfo petInfo = PetCache.PetInfo.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(customData.getString("petInfo"))).getOrThrow();
                Pet pet = new Pet(petInfo);
                itemList.add(pet.getIcon());
                continue;
            }

            // Attach an override for Aaron's Mod so that these ItemStacks will work with the mod's features even when not in Skyblock
            if (stack.contains(DataComponentTypes.CUSTOM_DATA)) {
                customData.put("aaron-mod", Util.make(new NbtCompound(), comp -> comp.putBoolean("alwaysDisplaySkyblockInfo", true)));
            }

            itemList.add(stack);
        }

        return itemList;
    }

    private static NbtList decompress(JsonObject data) {
        try {
            return NbtIo.readCompressed(new ByteArrayInputStream(Base64.getDecoder().decode(data.get("data").getAsString())), NbtSizeTracker.ofUnlimitedBytes()).getList("i", NbtElement.COMPOUND_TYPE);
        } catch (Exception e) {
            ProfileViewerScreen.LOGGER.error("[Skyblocker Profile Viewer] Failed to decompress item data", e);
        }
        return null;
    }
}
