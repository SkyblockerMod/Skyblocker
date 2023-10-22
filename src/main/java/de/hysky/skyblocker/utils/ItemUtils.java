package de.hysky.skyblocker.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ItemUtils {
    public static final String EXTRA_ATTRIBUTES = "ExtraAttributes";
    public static final String ID = "id";
    public static final String UUID = "uuid";
    public static final Pattern NOT_DURABILITY = Pattern.compile("[^0-9 /]");
    public static final Predicate<String> FUEL_PREDICATE = line -> line.contains("Fuel: ");

    public static List<Text> getTooltips(ItemStack item) {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player == null || item == null ? Collections.emptyList() : item.getTooltip(client.player, TooltipContext.Default.BASIC);
    }

    @Nullable
    public static String getTooltip(ItemStack item, Predicate<String> predicate) {
        for (Text line : getTooltips(item)) {
            String string = line.getString();
            if (predicate.test(string)) {
                return string;
            }
        }

        return null;
    }

    /**
     * Gets the {@code ExtraAttributes} NBT tag from the item stack.
     *
     * @param stack the item stack to get the {@code ExtraAttributes} NBT tag from
     * @return an optional containing the {@code ExtraAttributes} NBT tag of the item stack
     */
    public static Optional<NbtCompound> getExtraAttributesOptional(@NotNull ItemStack stack) {
        return Optional.ofNullable(stack.getSubNbt(EXTRA_ATTRIBUTES));
    }

    /**
     * Gets the {@code ExtraAttributes} NBT tag from the item stack.
     *
     * @param stack the item stack to get the {@code ExtraAttributes} NBT tag from
     * @return the {@code ExtraAttributes} NBT tag of the item stack, or null if the item stack is null or does not have an {@code ExtraAttributes} NBT tag
     */
    @Nullable
    public static NbtCompound getExtraAttributes(@NotNull ItemStack stack) {
        return stack.getSubNbt(EXTRA_ATTRIBUTES);
    }

    /**
     * Gets the internal name of the item stack from the {@code ExtraAttributes} NBT tag.
     *
     * @param stack the item stack to get the internal name from
     * @return an optional containing the internal name of the item stack
     */
    public static Optional<String> getItemIdOptional(@NotNull ItemStack stack) {
        return getExtraAttributesOptional(stack).map(extraAttributes -> extraAttributes.getString(ID));
    }

    /**
     * Gets the internal name of the item stack from the {@code ExtraAttributes} NBT tag.
     *
     * @param stack the item stack to get the internal name from
     * @return the internal name of the item stack, or an empty string if the item stack is null or does not have an internal name
     */
    public static String getItemId(@NotNull ItemStack stack) {
        NbtCompound extraAttributes = getExtraAttributes(stack);
        return extraAttributes != null ? extraAttributes.getString(ID) : "";
    }

    /**
     * Gets the UUID of the item stack from the {@code ExtraAttributes} NBT tag.
     *
     * @param stack the item stack to get the UUID from
     * @return an optional containing the UUID of the item stack
     */
    public static Optional<String> getItemUuidOptional(@NotNull ItemStack stack) {
        return getExtraAttributesOptional(stack).map(extraAttributes -> extraAttributes.getString(UUID));
    }

    /**
     * Gets the UUID of the item stack from the {@code ExtraAttributes} NBT tag.
     *
     * @param stack the item stack to get the UUID from
     * @return the UUID of the item stack, or an empty string if the item stack is null or does not have a UUID
     */
    public static String getItemUuid(@NotNull ItemStack stack) {
        NbtCompound extraAttributes = getExtraAttributes(stack);
        return extraAttributes != null ? extraAttributes.getString(UUID) : "";
    }

    public static boolean hasCustomDurability(@NotNull ItemStack stack) {
        NbtCompound extraAttributes = getExtraAttributes(stack);
        return extraAttributes != null && (extraAttributes.contains("drill_fuel") || extraAttributes.getString(ID).equals("PICKONIMBUS"));
    }

    @Nullable
    public static IntIntPair getDurability(@NotNull ItemStack stack) {
        NbtCompound extraAttributes = getExtraAttributes(stack);
        if (extraAttributes == null) return null;

        // TODO Calculate drill durability based on the drill_fuel flag, fuel_tank flag, and hotm level

        int pickonimbusDurability = extraAttributes.getInt("pickonimbus_durability");
        if (pickonimbusDurability > 0) {
            return IntIntPair.of(pickonimbusDurability, 5000);
        }

        String drillFuel = Formatting.strip(getTooltip(stack, FUEL_PREDICATE));
        if (drillFuel != null) {
            String[] drillFuelStrings = NOT_DURABILITY.matcher(drillFuel).replaceAll("").trim().split("/");
            return IntIntPair.of(Integer.parseInt(drillFuelStrings[0]), Integer.parseInt(drillFuelStrings[1]) * 1000);
        }

        return null;
    }

    public static ItemStack getSkyblockerStack() {
        try {
            return ItemStack.fromNbt(StringNbtReader.parse("{id:\"minecraft:player_head\",Count:1,tag:{SkullOwner:{Id:[I;-300151517,-631415889,-1193921967,-1821784279],Properties:{textures:[{Value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdjYzY2ODc0MjNkMDU3MGQ1NTZhYzUzZTA2NzZjYjU2M2JiZGQ5NzE3Y2Q4MjY5YmRlYmVkNmY2ZDRlN2JmOCJ9fX0=\"}]}}}}"));
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
