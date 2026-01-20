package de.hysky.skyblocker.skyblock.item.wikilookup;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import com.mojang.datafixers.util.Either;

public class EventCalendarLookup implements WikiLookup {
	private static final Pattern CALENDAR_EVENT_NAME = Pattern.compile("^[0-9a-z ]*(?<event>.+)$");
	public static final EventCalendarLookup INSTANCE = new EventCalendarLookup();

	private EventCalendarLookup() {}

	@Override
	public void open(ItemStack itemStack, Player player, boolean useOfficial) {
		Matcher matcher = CALENDAR_EVENT_NAME.matcher(itemStack.getHoverName().getString());

		if (matcher.matches()) {
			String eventName = matcher.group("event").trim();

			// Redirect to mayors page
			if (eventName.equals("Election Over!") || eventName.equals("Election Booth Opens")) {
				eventName = "Mayors";
			}
			// Strip bonus events
			else if (eventName.startsWith("Bonus")) {
				eventName = eventName.substring(6);
			}

			String formattedEvent = REPLACING_FUNCTION.apply(eventName);
			WikiLookupManager.openWikiLinkName(formattedEvent, player, useOfficial);
		}
	}

	@Override
	public boolean canSearch(@Nullable String title, Either<Slot, ItemStack> either) {
		Optional<Slot> optional = either.left();
		if (optional.isEmpty()) return false;
		Slot slot = optional.get();
		if (slot.index <= 9 || slot.index >= 26) return false;
		if (slot.getItem().is(Items.BLACK_STAINED_GLASS_PANE)) return false;
		return StringUtils.isNotEmpty(title) && title.matches("^Calendar and Events$");
	}
}
