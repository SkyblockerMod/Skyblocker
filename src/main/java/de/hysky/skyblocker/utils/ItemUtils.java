package de.hysky.skyblocker.utils;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ItemUtils {
    public static final String ID = "id";
    public static final String UUID = "uuid";
    private static final DateTimeFormatter OBTAINED_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy").withZone(ZoneId.systemDefault()).localizedBy(Locale.ENGLISH);
    private static final DateTimeFormatter OLD_OBTAINED_DATE_FORMAT = DateTimeFormatter.ofPattern("M/d/yy h:m a").withZone(ZoneId.of("UTC")).localizedBy(Locale.ENGLISH);
    public static final Pattern NOT_DURABILITY = Pattern.compile("[^0-9 /]");
    public static final Predicate<String> FUEL_PREDICATE = line -> line.contains("Fuel: ");
    private static final Gson GSON = new Gson(); //GSON Instance with no config
    private static final Codec<RegistryEntry<Item>> EMPTY_ALLOWING_ITEM_CODEC = Registries.ITEM.getEntryCodec();
    public static final Codec<ItemStack> EMPTY_ALLOWING_ITEMSTACK_CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
            EMPTY_ALLOWING_ITEM_CODEC.fieldOf("id").forGetter(ItemStack::getRegistryEntry),
            Codec.INT.orElse(1).fieldOf("count").forGetter(ItemStack::getCount),
            ComponentChanges.CODEC.optionalFieldOf("components", ComponentChanges.EMPTY).forGetter(ItemStack::getComponentChanges)
    ).apply(instance, ItemStack::new)));

    public static LiteralArgumentBuilder<FabricClientCommandSource> dumpHeldItemCommand() {
        return literal("dumpHeldItem").executes(context -> {
            context.getSource().sendFeedback(Text.literal("[Skyblocker Debug] Held Item: " + GSON.toJson(ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, context.getSource().getPlayer().getMainHandStack()).getOrThrow())));
            return Command.SINGLE_SUCCESS;
        });
    }

    @SuppressWarnings("deprecation")
	public static NbtCompound getCustomData(@NotNull ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt();
    }

    /**
     * Gets the Skyblock item id of the item stack.
     *
     * @param stack the item stack to get the internal name from
     * @return an optional containing the internal name of the item stack
     */
	public static Optional<String> getItemIdOptional(@NotNull ItemStack stack) {
        NbtCompound customData = getCustomData(stack);
        return customData.contains(ID) ? Optional.of(customData.getString(ID)) : Optional.empty();
    }

    /**
     * Gets the Skyblock item id of the item stack.
     *
     * @param stack the item stack to get the internal name from
     * @return the internal name of the item stack, or an empty string if the item stack is null or does not have an internal name
     */
	public static String getItemId(@NotNull ItemStack stack) {
        return getCustomData(stack).getString(ID);
    }

    /**
     * Gets the UUID of the item stack.
     *
     * @param stack the item stack to get the UUID from
     * @return an optional containing the UUID of the item stack
     */
	public static Optional<String> getItemUuidOptional(@NotNull ItemStack stack) {
        NbtCompound customData = getCustomData(stack);
        return customData.contains(UUID) ? Optional.of(customData.getString(UUID)) : Optional.empty();
    }

    /**
     * Gets the UUID of the item stack.
     *
     * @param stack the item stack to get the UUID from
     * @return the UUID of the item stack, or an empty string if the item stack is null or does not have a UUID
     */
	public static String getItemUuid(@NotNull ItemStack stack) {
        return getCustomData(stack).getString(UUID);
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
        NbtCompound customData = getCustomData(stack);

        if (customData != null && customData.contains("timestamp", NbtElement.LONG_TYPE)) {
            Instant date = Instant.ofEpochMilli(customData.getLong("timestamp"));
            return OBTAINED_DATE_FORMATTER.format(date);
        }

        if (customData != null && customData.contains("timestamp", NbtElement.STRING_TYPE)) {
            TemporalAccessor date = OLD_OBTAINED_DATE_FORMAT.parse(customData.getString("timestamp"));
            return OBTAINED_DATE_FORMATTER.format(date);
        }

        return "";
    }

    public static boolean hasCustomDurability(@NotNull ItemStack stack) {
        NbtCompound customData = getCustomData(stack);
        return customData != null && (customData.contains("drill_fuel") || customData.getString(ID).equals("PICKONIMBUS"));
    }

    @Nullable
    public static IntIntPair getDurability(@NotNull ItemStack stack) {
        NbtCompound customData = getCustomData(stack);
        if (customData == null) return null;

        // TODO Calculate drill durability based on the drill_fuel flag, fuel_tank flag, and hotm level
        // TODO Cache the max durability and only update the current durability on inventory tick

        int pickonimbusDurability = customData.getInt("pickonimbus_durability");
        if (pickonimbusDurability > 0) {
            return IntIntPair.of(pickonimbusDurability, 5000);
        }

        String drillFuel = Formatting.strip(getLoreLineIf(stack, FUEL_PREDICATE));
        if (drillFuel != null) {
            String[] drillFuelStrings = NOT_DURABILITY.matcher(drillFuel).replaceAll("").trim().split("/");
            return IntIntPair.of(Integer.parseInt(drillFuelStrings[0]), Integer.parseInt(drillFuelStrings[1]) * 1000);
        }

        return null;
    }

    @Nullable
    public static String getLoreLineIf(ItemStack item, Predicate<String> predicate) {
        for (Text line : getLore(item)) {
            String string = line.getString();
            if (predicate.test(string)) {
                return string;
            }
        }

        return null;
    }

    @Nullable
    public static Matcher getLoreLineIfMatch(ItemStack item, Pattern pattern) {
        for (Text line : getLore(item)) {
            String string = line.getString();
            Matcher matcher = pattern.matcher(string);
            if (matcher.matches()) {
                return matcher;
            }
        }

        return null;
    }

    public static List<Text> getLore(ItemStack item) {
        return item.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).styledLines();
    }
    
    public static PropertyMap propertyMapWithTexture(String textureValue) {
        return Codecs.GAME_PROFILE_PROPERTY_MAP.parse(JsonOps.INSTANCE, JsonParser.parseString("[{\"name\":\"textures\",\"value\":\"" + textureValue + "\"}]")).getOrThrow();
    }

    public static String getHeadTexture(ItemStack stack) {
        if (!stack.isOf(Items.PLAYER_HEAD) || !stack.contains(DataComponentTypes.PROFILE)) return "";

        ProfileComponent profile = stack.get(DataComponentTypes.PROFILE);
        String texture = profile.properties().get("textures").stream()
                .map(Property::value)
                .findFirst()
                .orElse("");

        return texture;
    }

    public static ItemStack getSkyblockerStack() {
        try {
            ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
            stack.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.of("SkyblockerStack"), Optional.of(java.util.UUID.randomUUID()), propertyMapWithTexture("e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdjYzY2ODc0MjNkMDU3MGQ1NTZhYzUzZTA2NzZjYjU2M2JiZGQ5NzE3Y2Q4MjY5YmRlYmVkNmY2ZDRlN2JmOCJ9fX0=")));
            return stack;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
