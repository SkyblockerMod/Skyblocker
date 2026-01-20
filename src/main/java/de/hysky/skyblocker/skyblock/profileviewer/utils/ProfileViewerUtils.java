package de.hysky.skyblocker.skyblock.profileviewer.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerScreen;
import de.hysky.skyblocker.utils.ItemUtils;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

public class ProfileViewerUtils {
	public static ItemStack createSkull(String textureB64) {
		ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
		try {
			PropertyMap map = ItemUtils.propertyMapWithTexture(textureB64);
			ResolvableProfile profile = ResolvableProfile.createResolved(new GameProfile(UUID.randomUUID(), "skull", map));
			skull.set(DataComponents.PROFILE, profile);
		} catch (Exception e) {
			ProfileViewerScreen.LOGGER.error("[Skyblocker Profile Viewer] Failed to create skull", e);
		}
		return skull;
	}

	public static String numLetterFormat(double amount) {
		if (amount >= 1_000_000_000) {
			return String.format("%.4gB", amount / 1_000_000_000);
		} else if (amount >= 1_000_000) {
			return String.format("%.4gM", amount / 1_000_000);
		} else if (amount >= 1_000) {
			return String.format("%.4gK", amount / 1_000);
		} else {
			return String.valueOf((int) amount);
		}
	}
}
