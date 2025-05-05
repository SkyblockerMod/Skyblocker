package de.hysky.skyblocker.utils.networth;

import java.io.ByteArrayInputStream;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.azureaaron.networth.item.ItemMetadataRetriever;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtSizeTracker;

record SkyblockItemMetadataRetriever(IntList cakeBagCakeYears) implements ItemMetadataRetriever {

	static SkyblockItemMetadataRetriever of(NbtCompound customData, String itemId) {
		return new SkyblockItemMetadataRetriever(getCakeBagCakeYears(customData, itemId));
	}

	private static IntList getCakeBagCakeYears(NbtCompound customData, String itemId) {
		if (itemId.equals("NEW_YEAR_CAKE_BAG") && customData.contains("new_year_cake_bag_data")) {
			try {
				NbtCompound uncompressed = NbtIo.readCompressed(new ByteArrayInputStream(customData.getByteArray("new_year_cake_bag_data").orElse(new byte[0])), NbtSizeTracker.ofUnlimitedBytes());
				NbtList items = uncompressed.getListOrEmpty("i");
				IntList cakeYears = new IntArrayList();

				for (NbtElement element : items) {
					if (element instanceof NbtCompound compound && compound.getCompoundOrEmpty("tag").contains("ExtraAttributes")) {
						NbtCompound extraAttributes = compound.getCompoundOrEmpty("tag").getCompoundOrEmpty("ExtraAttributes");
						int cakeYear = extraAttributes.getInt("new_years_cake", 0); //You can only put new year cakes in the bag so we don't need to check for it being one

						cakeYears.add(cakeYear);
					}
				}

				return cakeYears;
			} catch (Exception ignored) {}
		}

		return IntList.of();
	}
}
