package de.hysky.skyblocker.events;

import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.purse.PurseChangeCause;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@Environment(EnvType.CLIENT)
public final class SkyblockEvents {
	public static final Event<SkyblockJoin> JOIN = EventFactory.createArrayBacked(SkyblockJoin.class, callbacks -> () -> {
		for (SkyblockEvents.SkyblockJoin callback : callbacks) {
			callback.onSkyblockJoin();
		}
	});

	public static final Event<SkyblockLeave> LEAVE = EventFactory.createArrayBacked(SkyblockLeave.class, callbacks -> () -> {
		for (SkyblockLeave callback : callbacks) {
			callback.onSkyblockLeave();
		}
	});

	public static final Event<SkyblockLocationChange> LOCATION_CHANGE = EventFactory.createArrayBacked(SkyblockLocationChange.class, callbacks -> location -> {
		for (SkyblockLocationChange callback : callbacks) {
			callback.onSkyblockLocationChange(location);
		}
	});

	public static final Event<SkyblockAreaChange> AREA_CHANGE = EventFactory.createArrayBacked(SkyblockAreaChange.class, callbacks -> area -> {
		for (SkyblockAreaChange callback : callbacks) {
			callback.onSkyblockAreaChange(area);
		}
	});

	/**
	 * Called when the player's Skyblock profile changes.
	 *
	 * @implNote This is called upon receiving the chat message for the profile change rather than the exact moment of profile change, so it may be delayed by a few seconds.
	 */
	public static final Event<ProfileChange> PROFILE_CHANGE = EventFactory.createArrayBacked(ProfileChange.class, callbacks -> (prev, profile) -> {
		for (ProfileChange callback : callbacks) {
			callback.onSkyblockProfileChange(prev, profile);
		}
	});

	public static final Event<PurseChange> PURSE_CHANGE = EventFactory.createArrayBacked(PurseChange.class, callbacks -> (diff, cause) -> {
		for (PurseChange callback : callbacks) {
			callback.onPurseChange(diff, cause);
		}
	});

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface SkyblockJoin {
		void onSkyblockJoin();
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface SkyblockLeave {
		void onSkyblockLeave();
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface SkyblockLocationChange {
		void onSkyblockLocationChange(Location location);
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface SkyblockAreaChange {
		void onSkyblockAreaChange(Area area);
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface ProfileChange {
		void onSkyblockProfileChange(String prevProfileId, String profileId);
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface PurseChange {
		void onPurseChange(double diff, PurseChangeCause cause);
	}
}
