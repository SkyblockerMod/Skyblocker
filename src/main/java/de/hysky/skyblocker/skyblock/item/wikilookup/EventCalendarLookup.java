package de.hysky.skyblocker.skyblock.item.wikilookup;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.datafixers.util.Either;

public class EventCalendarLookup implements WikiLookup {
	private static final Pattern CALENDAR_EVENT_NAME = Pattern.compile("^[0-9a-z ]*(?<event>.+)$");
	public static final EventCalendarLookup INSTANCE = new EventCalendarLookup();

	private EventCalendarLookup() {}

	@Override
	public void open(@NotNull ItemStack itemStack, @NotNull PlayerEntity player, boolean useOfficial) {
		Matcher matcher = CALENDAR_EVENT_NAME.matcher(itemStack.getName().getString());

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
	public boolean canSearch(@Nullable String title, @NotNull Either<Slot, ItemStack> either) {
		Optional<Slot> optional = either.left();
		if (optional.isEmpty()) return false;
		Slot slot = optional.get();
		if (slot.id <= 9 || slot.id >= 26) return false;
		if (slot.getStack().isOf(Items.BLACK_STAINED_GLASS_PANE)) return false;
		return StringUtils.isNotEmpty(title) && title.matches("^Calendar and Events$");
	}
}
