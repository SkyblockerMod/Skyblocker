package de.hysky.skyblocker.skyblock.item.wikilookup;

import com.mojang.datafixers.util.Either;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class PetItemLookup implements WikiLookup {
	private static final Pattern PET_ITEM_NAME = Pattern.compile("^\\[Lvl \\d+] (?<name>.+)$");
	private static final Function<String, Matcher> PET_MATCHER = itemName -> PET_ITEM_NAME.matcher(itemName + " Pet"); // Add Pet to the end of string for precise lookup
	private static final Predicate<ItemStack> PET_ITEM_FILTER = itemStack -> {
		String itemName = itemStack.getHoverName().getString();
		PetInfo petInfo = itemStack.getPetInfo();
		// Filter only items that has PetInfo stored and item name matches with [Lvl xx] PetName
		return !petInfo.isEmpty() || itemName.matches(PET_ITEM_NAME.pattern());
	};
	public static final PetItemLookup INSTANCE = new PetItemLookup();

	private PetItemLookup() {}

	@Override
	public void open(ItemStack itemStack, Player player, boolean useOfficial) {
		String itemName = itemStack.getHoverName().getString();
		PetInfo petInfo = itemStack.getPetInfo();

		lookupPetItem(PET_MATCHER.apply(petInfo.name().orElse(itemName)), player, useOfficial);
	}

	@Override
	public boolean canSearch(@Nullable String title, Either<Slot, ItemStack> either) {
		ItemStack itemStack = WikiLookupManager.mapEitherToItemStack(either);
		return PET_ITEM_FILTER.test(itemStack);
	}

	private static void lookupPetItem(Matcher matcher, Player player, boolean useOfficial) {
		if (matcher.matches()) {
			String petName = REPLACING_FUNCTION.apply(matcher.group("name").trim());
			WikiLookupManager.openWikiLinkName(petName, player, useOfficial);
		}
	}
}
