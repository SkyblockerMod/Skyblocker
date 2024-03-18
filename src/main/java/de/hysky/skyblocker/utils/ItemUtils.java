package de.hysky.skyblocker.utils;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.hysky.skyblocker.mixin.accessor.ItemStackAccessor;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ItemUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemUtils.class);
    public static final String EXTRA_ATTRIBUTES = "ExtraAttributes";
    public static final String ID = "id";
    public static final String UUID = "uuid";
    private static final DateTimeFormatter OBTAINED_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy").withZone(ZoneId.systemDefault()).localizedBy(Locale.ENGLISH);
    private static final SimpleDateFormat OLD_OBTAINED_DATE_FORMAT = new SimpleDateFormat("MM/dd/yy");
    public static final Pattern NOT_DURABILITY = Pattern.compile("[^0-9 /]");
    public static final Predicate<String> FUEL_PREDICATE = line -> line.contains("Fuel: ");

    public static LiteralArgumentBuilder<FabricClientCommandSource> dumpHeldItemNbtCommand() {
        return literal("dumpHeldItemNbt").executes(context -> {
            context.getSource().sendFeedback(Text.literal("[Skyblocker Debug] Held Item Nbt: " + context.getSource().getPlayer().getMainHandStack().writeNbt(new NbtCompound())));
            return Command.SINGLE_SUCCESS;
        });
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

    /**
     * This method converts the "timestamp" variable into the same date format as Hypixel represents it in the museum.
     * Currently, there are two types of string timestamps the legacy which is built like this
     * "dd/MM/yy hh:mm" ("25/04/20 16:38") and the current which is built like this
     * "MM/dd/yy hh:mm aa" ("12/24/20 11:08 PM"). Since Hypixel transforms the two formats into one format without
     * taking into account of their formats, we do the same. The final result looks like this
     * "MMMM dd, yyyy" (December 24, 2020).
     * Since the legacy format has a 25 as "month" SimpleDateFormat converts the 25 into 2 years and 1 month and makes
     * "25/04/20 16:38" -> "January 04, 2022" instead of "April 25, 2020".
     * This causes the museum rank to be much worse than it should be.
     * <p>
     * This also handles the long timestamp format introduced in January 2024 where the timestamp is in epoch milliseconds.
     *
     * @param stack the item under the pointer
     * @return if the item have a "Timestamp" it will be shown formated on the tooltip
     */
    public static String getTimestamp(ItemStack stack) {
        NbtCompound ea = getExtraAttributes(stack);

        if (ea != null && ea.contains("timestamp", NbtElement.LONG_TYPE)) {
            Instant date = Instant.ofEpochMilli(ea.getLong("timestamp"));

            return OBTAINED_DATE_FORMATTER.format(date);
        }

        if (ea != null && ea.contains("timestamp", NbtElement.STRING_TYPE)) {
            try {
                Instant date = OLD_OBTAINED_DATE_FORMAT.parse(ea.getString("timestamp")).toInstant();

                return OBTAINED_DATE_FORMATTER.format(date);
            } catch (ParseException e) {
                LOGGER.warn("[Skyblocker Item Utils] Encountered an unknown exception while parsing time stamp of item {} with extra attributes {}", stack, ea, e);
            }
        }

        return "";
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
        // TODO Cache the max durability and only update the current durability on inventory tick

        int pickonimbusDurability = extraAttributes.getInt("pickonimbus_durability");
        if (pickonimbusDurability > 0) {
            return IntIntPair.of(pickonimbusDurability, 5000);
        }

        String drillFuel = Formatting.strip(getNbtTooltip(stack, FUEL_PREDICATE));
        if (drillFuel != null) {
            String[] drillFuelStrings = NOT_DURABILITY.matcher(drillFuel).replaceAll("").trim().split("/");
            return IntIntPair.of(Integer.parseInt(drillFuelStrings[0]), Integer.parseInt(drillFuelStrings[1]) * 1000);
        }

        return null;
    }

    @Nullable
    public static String getNbtTooltip(ItemStack item, Predicate<String> predicate) {
        for (Text line : getNbtTooltips(item)) {
            String string = line.getString();
            if (predicate.test(string)) {
                return string;
            }
        }

        return null;
    }

    @Nullable
    public static Matcher getNbtTooltip(ItemStack item, Pattern pattern) {
        for (Text line : getNbtTooltips(item)) {
            String string = line.getString();
            Matcher matcher = pattern.matcher(string);
            if (matcher.matches()) {
                return matcher;
            }
        }

        return null;
    }

    public static List<Text> getNbtTooltips(ItemStack item) {
        NbtCompound displayNbt = item.getSubNbt("display");
        if (displayNbt == null || !displayNbt.contains("Lore", NbtElement.LIST_TYPE)) {
            return Collections.emptyList();
        }

        return displayNbt.getList("Lore", NbtElement.STRING_TYPE).stream().map(NbtElement::asString).map(Text.Serialization::fromJson).filter(Objects::nonNull).map(text -> Texts.setStyleIfAbsent(text, ItemStackAccessor.getLORE_STYLE())).map(Text.class::cast).toList();
    }

    public static ItemStack getSkyblockerStack() {
        try {
            return ItemStack.fromNbt(StringNbtReader.parse("{id:\"minecraft:player_head\",Count:1,tag:{SkullOwner:{Id:[I;-300151517,-631415889,-1193921967,-1821784279],Properties:{textures:[{Value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdjYzY2ODc0MjNkMDU3MGQ1NTZhYzUzZTA2NzZjYjU2M2JiZGQ5NzE3Y2Q4MjY5YmRlYmVkNmY2ZDRlN2JmOCJ9fX0=\"}]}}}}"));
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
