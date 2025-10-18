package de.hysky.skyblocker.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface ItemPriceUpdateEvent {
	void onPriceUpdate();

	/**
	 * An event that is fired when all prices are updated.
	 */
	Event<ItemPriceUpdateEvent> ON_PRICE_UPDATE = EventFactory.createArrayBacked(ItemPriceUpdateEvent.class, listeners -> () -> {
		for (ItemPriceUpdateEvent listener : listeners) {
			listener.onPriceUpdate();
		}
	});
}
