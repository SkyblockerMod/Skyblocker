package de.hysky.skyblocker.skyblock.profileviewer.utils;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class ProfileViewerUtils {
    public static ItemStack createSkull(String textureB64) {
        ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
        try {
            PropertyMap map = new PropertyMap();
            map.put("textures", new Property("textures", textureB64));
            ProfileComponent profile = new ProfileComponent(Optional.of("skull"), Optional.of(UUID.randomUUID()), map);
            skull.set(DataComponentTypes.PROFILE, profile);
        } catch (Exception e) {
            ProfileViewerScreen.LOGGER.error("[Skyblocker Profile Viewer] Failed to create skull", e);
        }
        return skull;
    }

    public static final NumberFormat COMMA_FORMATTER = NumberFormat.getNumberInstance(Locale.US);
}
