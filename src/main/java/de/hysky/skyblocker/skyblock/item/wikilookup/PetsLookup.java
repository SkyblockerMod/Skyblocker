package de.hysky.skyblocker.skyblock.item.wikilookup;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.datafixers.util.Either;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

public class PetsLookup implements WikiLookup {
	private static final Pattern PET_ITEM_NAME = Pattern.compile("^\\[Lvl \\d+] (?<name>.+)$");
	public static final PetsLookup INSTANCE = new PetsLookup();

	private PetsLookup() {}

	@Override
	public void open(@NotNull Either<Slot, ItemStack> either, @NotNull PlayerEntity player, boolean useOfficial) {
		either.ifLeft(slot -> {
			String itemName = slot.getStack().getName().getString() + " Pet"; // Add Pet to the end of string for precise lookup
			Matcher matcher = PET_ITEM_NAME.matcher(itemName);

			if (matcher.matches()) {
				String petName = REPLACING_FUNCTION.apply(matcher.group("name").trim());
				String wikiLink = ItemRepository.getWikiLink(useOfficial) + "/" + petName;
				WikiLookup.openWikiLink(wikiLink, player);
			}
		});
	}

	@Override
	public boolean canSearch(@Nullable String title, @NotNull Either<Slot, ItemStack> either) {
		Optional<Slot> optional = either.left();
		if (optional.isEmpty()) return false;
		Slot slot = optional.get();
		if (slot.id <= 9 || slot.id >= 44) return false;
		if (slot.getStack().isOf(Items.BLACK_STAINED_GLASS_PANE)) return false;
		// Needed to trim because Pets menu title name is weird
		return StringUtils.isNotEmpty(title) && title.trim().matches("^Pets \\(\\d+/\\d+\\)$");
	}
}
