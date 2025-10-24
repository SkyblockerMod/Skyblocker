package de.hysky.skyblocker.skyblock.item.wikilookup;

import com.mojang.datafixers.util.Either;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PetItemLookup implements WikiLookup {
	private static final Pattern PET_ITEM_NAME = Pattern.compile("^\\[Lvl \\d+] (?<name>.+)$");
	private static final Function<String, Matcher> PET_MATCHER = itemName -> PET_ITEM_NAME.matcher(itemName + " Pet"); // Add Pet to the end of string for precise lookup
	private static final Predicate<ItemStack> PET_ITEM_FILTER = itemStack -> {
		String itemName = itemStack.getName().getString();
		PetInfo petInfo = itemStack.getPetInfo();
		// Filter only items that has PetInfo stored and item name matches with [Lvl xx] PetName
		return !petInfo.isEmpty() || itemName.matches(PET_ITEM_NAME.pattern());
	};
	public static final PetItemLookup INSTANCE = new PetItemLookup();

	private PetItemLookup() {}

	@Override
	public void open(@NotNull ItemStack itemStack, @NotNull PlayerEntity player, boolean useOfficial) {
		String itemName = itemStack.getName().getString();
		PetInfo petInfo = itemStack.getPetInfo();

		lookupPetItem(PET_MATCHER.apply(petInfo.name().orElse(itemName)), player, useOfficial);
	}

	@Override
	public boolean canSearch(@Nullable String title, @NotNull Either<Slot, ItemStack> either) {
		ItemStack itemStack = WikiLookupManager.mapEitherToItemStack(either);
		return PET_ITEM_FILTER.test(itemStack);
	}

	private static void lookupPetItem(Matcher matcher, @NotNull PlayerEntity player, boolean useOfficial) {
		if (matcher.matches()) {
			String petName = REPLACING_FUNCTION.apply(matcher.group("name").trim());
			WikiLookupManager.openWikiLinkName(petName, player, useOfficial);
		}
	}
}
