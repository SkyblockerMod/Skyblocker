package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import de.hysky.skyblocker.utils.Location;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public interface ScreenId extends StringRepresentable {

	record Loc(Location location) implements ScreenId {

		@Override
		public String getSerializedName() {
			return location().getSerializedName();
		}

		@Override
		public Component displayName() {
			return Component.literal(location().toString());
		}
	}

	record Named(String serializedName, Component displayName) implements ScreenId {
		@Override
		public String getSerializedName() {
			return serializedName();
		}
	}

	Component displayName();
}
