package de.hysky.skyblocker.utils.networth;

import java.io.ByteArrayInputStream;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.azureaaron.networth.item.ItemMetadataRetriever;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

record SkyblockItemMetadataRetriever(IntList cakeBagCakeYears) implements ItemMetadataRetriever {

	static SkyblockItemMetadataRetriever of(CompoundTag customData, String itemId) {
		return new SkyblockItemMetadataRetriever(getCakeBagCakeYears(customData, itemId));
	}

	private static IntList getCakeBagCakeYears(CompoundTag customData, String itemId) {
		if (itemId.equals("NEW_YEAR_CAKE_BAG") && customData.contains("new_year_cake_bag_data")) {
			try {
				CompoundTag uncompressed = NbtIo.readCompressed(new ByteArrayInputStream(customData.getByteArray("new_year_cake_bag_data").orElse(new byte[0])), NbtAccounter.unlimitedHeap());
				ListTag items = uncompressed.getListOrEmpty("i");
				IntList cakeYears = new IntArrayList();

				for (Tag element : items) {
					if (element instanceof CompoundTag compound && compound.getCompoundOrEmpty("tag").contains("ExtraAttributes")) {
						CompoundTag extraAttributes = compound.getCompoundOrEmpty("tag").getCompoundOrEmpty("ExtraAttributes");
						int cakeYear = extraAttributes.getIntOr("new_years_cake", 0); //You can only put new year cakes in the bag so we don't need to check for it being one

						cakeYears.add(cakeYear);
					}
				}

				return cakeYears;
			} catch (Exception ignored) {}
		}

		return IntList.of();
	}
}
