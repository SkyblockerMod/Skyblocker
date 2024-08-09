package de.hysky.skyblocker.skyblock.profileviewer.inventory.itemLoaders;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
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
import net.minecraft.util.Util;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import org.slf4j.Logger;

public class ItemLoader {
	private static final Logger LOGGER = LogUtils.getLogger();

	public List<ItemStack> loadItems(JsonObject data) {
		NbtList containerContent = decompress(data);
		List<CompletableFuture<ItemStack>> futures = new ArrayList<>();

		for (int i = 0; i < containerContent.size(); i++) {
			futures.add(loadItem(containerContent.getCompound(i)));
		}

		CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

		return futures.stream().map(cf -> cf.isCompletedExceptionally() ? Ico.BARRIER.copy() : cf.join()).toList();
	}

	private CompletableFuture<ItemStack> loadItem(NbtCompound nbt) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				if (nbt.getInt("id") == 0) {
					return ItemStack.EMPTY;
				}

				ItemStack stack = LegacyItemStackFixer.fixLegacyStack(nbt);

				if (stack.isEmpty()) return Ico.BARRIER.copy();

				NbtCompound customData = ItemUtils.getCustomData(stack);
				String itemId = ItemUtils.getItemId(stack);

				if (itemId.equals("PET")) {
					PetCache.PetInfo petInfo = PetCache.PetInfo.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(customData.getString("petInfo"))).getOrThrow();
					Pet pet = new Pet(petInfo);

					return pet.getIcon();
				}

				//Attach an override for Aaron's Mod so that these ItemStacks will work with the mod's features even when not in Skyblock
				if (stack.contains(DataComponentTypes.CUSTOM_DATA)) {
					customData.put("aaron-mod", Util.make(new NbtCompound(), comp -> comp.putBoolean("alwaysDisplaySkyblockInfo", true)));
				}

				return stack;
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Profile Viewer] Failed to load item with compound: {}", nbt, e);
			}

			return Ico.BARRIER.copy();
		}, Executors.newVirtualThreadPerTaskExecutor());
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
