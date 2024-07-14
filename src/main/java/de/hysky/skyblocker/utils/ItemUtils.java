package de.hysky.skyblocker.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.ObtainedDateTooltip;
import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.longs.LongBooleanPair;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class ItemUtils {
    public static final String ID = "id";
    public static final String UUID = "uuid";
    public static final Pattern NOT_DURABILITY = Pattern.compile("[^0-9 /]");
    public static final Predicate<String> FUEL_PREDICATE = line -> line.contains("Fuel: ");
    private static final Codec<RegistryEntry<Item>> EMPTY_ALLOWING_ITEM_CODEC = Registries.ITEM.getEntryCodec();
    public static final Codec<ItemStack> EMPTY_ALLOWING_ITEMSTACK_CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
            EMPTY_ALLOWING_ITEM_CODEC.fieldOf("id").forGetter(ItemStack::getRegistryEntry),
            Codec.INT.orElse(1).fieldOf("count").forGetter(ItemStack::getCount),
            ComponentChanges.CODEC.optionalFieldOf("components", ComponentChanges.EMPTY).forGetter(ItemStack::getComponentChanges)
    ).apply(instance, ItemStack::new)));

    private ItemUtils() {}

    public static LiteralArgumentBuilder<FabricClientCommandSource> dumpHeldItemCommand() {
        return literal("dumpHeldItem").executes(context -> {
            context.getSource().sendFeedback(Text.literal("[Skyblocker Debug] Held Item: " + SkyblockerMod.GSON_COMPACT.toJson(ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, context.getSource().getPlayer().getMainHandStack()).getOrThrow())));
            return Command.SINGLE_SUCCESS;
        });
    }

    /**
     * Gets the nbt in the custom data component of the item stack.
     * @return The {@link DataComponentTypes#CUSTOM_DATA custom data} of the itemstack,
     *         or an empty {@link NbtCompound} if the itemstack is missing a custom data component
     */
    @SuppressWarnings("deprecation")
	public static @NotNull NbtCompound getCustomData(@NotNull ComponentHolder stack) {
        return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt();
    }

    /**
     * Gets the Skyblock item id of the item stack.
     *
     * @param stack the item stack to get the internal name from
     * @return an optional containing the internal name of the item stack
     */
    public static @NotNull Optional<String> getItemIdOptional(@NotNull ItemStack stack) {
        NbtCompound customData = getCustomData(stack);
        return customData.contains(ID) ? Optional.of(customData.getString(ID)) : Optional.empty();
    }

    /**
     * Gets the Skyblock item id of the item stack.
     *
     * @param stack the item stack to get the internal name from
     * @return the internal name of the item stack, or an empty string if the item stack is null or does not have an internal name
     */
    public static @NotNull String getItemId(@NotNull ItemStack stack) {
        return getCustomData(stack).getString(ID);
    }

    /**
     * Gets the UUID of the item stack.
     *
     * @param stack the item stack to get the UUID from
     * @return an optional containing the UUID of the item stack
     */
    public static @NotNull Optional<String> getItemUuidOptional(@NotNull ItemStack stack) {
        NbtCompound customData = getCustomData(stack);
        return customData.contains(UUID) ? Optional.of(customData.getString(UUID)) : Optional.empty();
    }

    /**
     * Gets the UUID of the item stack.
     *
     * @param stack the item stack to get the UUID from
     * @return the UUID of the item stack, or an empty string if the item stack is null or does not have a UUID
     */
	public static @NotNull String getItemUuid(@NotNull ComponentHolder stack) {
        return getCustomData(stack).getString(UUID);
    }

    /**
     * Gets the bazaar sell price or the lowest bin based on the id of the item stack.
     *
     * @return An {@link LongBooleanPair} with the {@code left long} representing the item's price,
     * and the {@code right boolean} indicating if the price was based on complete data.
     */
    public static @NotNull DoubleBooleanPair getItemPrice(@NotNull ItemStack stack) {
        return getItemPrice(getItemId(stack));
    }

    /**
     * Gets the bazaar sell price or the lowest bin of the item with the specified id.
     *
     * @return An {@link LongBooleanPair} with the {@code left long} representing the item's price,
     * and the {@code right boolean} indicating if the price was based on complete data.
     */
    public static @NotNull DoubleBooleanPair getItemPrice(@Nullable String id) {
        JsonObject bazaarPrices = TooltipInfoType.BAZAAR.getData();
        JsonObject lowestBinPrices = TooltipInfoType.LOWEST_BINS.getData();

        if (id == null || id.isEmpty() || bazaarPrices == null || lowestBinPrices == null) return DoubleBooleanPair.of(0, false);

        if (bazaarPrices.has(id)) {
            JsonElement sellPrice = bazaarPrices.get(id).getAsJsonObject().get("sellPrice");
            boolean isPriceNull = sellPrice.isJsonNull();
            return DoubleBooleanPair.of(isPriceNull ? 0 : sellPrice.getAsDouble(), !isPriceNull);
        }

        if (lowestBinPrices.has(id)) {
            return DoubleBooleanPair.of(lowestBinPrices.get(id).getAsDouble(), true);
        }

        return DoubleBooleanPair.of(0, false);
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
     * @deprecated use {@link ObtainedDateTooltip#getTimestamp(ItemStack)} instead
     */
    @Deprecated
    public static String getTimestamp(ItemStack stack) {
        return ObtainedDateTooltip.getTimestamp(stack);
    }

    public static boolean hasCustomDurability(@NotNull ItemStack stack) {
        NbtCompound customData = getCustomData(stack);
        return !customData.isEmpty() && (customData.contains("drill_fuel") || customData.getString(ID).equals("PICKONIMBUS"));
    }

    @Nullable
    public static IntIntPair getDurability(@NotNull ItemStack stack) {
        NbtCompound customData = getCustomData(stack);
        if (customData.isEmpty()) return null;

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

    /**
     * Gets the first line of the lore that matches the specified predicate.
     * @return The first line of the lore that matches the predicate, or {@code null} if no line matches.
     */
    @Nullable
    public static String getLoreLineIf(ItemStack stack, Predicate<String> predicate) {
        for (Text line : getLore(stack)) {
            String string = line.getString();
            if (predicate.test(string)) {
                return string;
            }
        }

        return null;
    }

    /**
     * Gets the first line of the lore that matches the specified pattern, using {@link Matcher#matches()}.
     * @return A matcher that contains match results if the pattern was found in the lore, otherwise {@code null}.
     */
    @Nullable
    public static Matcher getLoreLineIfMatch(ItemStack stack, Pattern pattern) {
        Matcher matcher = pattern.matcher("");
        for (Text line : getLore(stack)) {
            if (matcher.reset(line.getString()).matches()) {
                return matcher;
            }
        }
        return null;
    }

	/**
	 * Gets the first line of the lore that matches the specified pattern, using {@link Matcher#find()}.
	 * @param pattern the pattern to search for
	 * @param stack the stack to search the lore of
	 * @return A {@link Matcher matcher} that contains match results if the pattern was found in the lore, otherwise {@code null}.
	 */
	@Nullable
	public static Matcher getLoreLineIfContainsMatch(ItemStack stack, Pattern pattern) {
		Matcher matcher = pattern.matcher("");
		for (Text line : getLore(stack)) {
			if (matcher.reset(line.getString()).find()) {
				return matcher;
			}
		}
		return null;
	}

    public static @NotNull List<Text> getLore(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).styledLines();
    }

    public static @NotNull PropertyMap propertyMapWithTexture(String textureValue) {
        return Codecs.GAME_PROFILE_PROPERTY_MAP.parse(JsonOps.INSTANCE, JsonParser.parseString("[{\"name\":\"textures\",\"value\":\"" + textureValue + "\"}]")).getOrThrow();
    }

    public static @NotNull String getHeadTexture(@NotNull ItemStack stack) {
        if (!stack.isOf(Items.PLAYER_HEAD) || !stack.contains(DataComponentTypes.PROFILE)) return "";

        ProfileComponent profile = stack.get(DataComponentTypes.PROFILE);
        if (profile == null) return "";

        return profile.properties().get("textures").stream()
                .map(Property::value)
                .findFirst()
                .orElse("");
    }

    public static @NotNull Optional<String> getHeadTextureOptional(ItemStack stack) {
        String texture = getHeadTexture(stack);
        if (texture.isBlank()) return Optional.empty();
        return Optional.of(texture);
    }

    public static @NotNull ItemStack getSkyblockerStack() {
        try {
            ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
            stack.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.of("SkyblockerStack"), Optional.of(java.util.UUID.randomUUID()), propertyMapWithTexture("e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdjYzY2ODc0MjNkMDU3MGQ1NTZhYzUzZTA2NzZjYjU2M2JiZGQ5NzE3Y2Q4MjY5YmRlYmVkNmY2ZDRlN2JmOCJ9fX0=")));
            return stack;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	/**
	 * Utility method.
	 */
	public static @NotNull String getConcatenatedLore(@NotNull ItemStack item) {
	    return concatenateLore(getLore(item));
	}

	/**
	 * Concatenates the lore of an item into one string.
	 * This is useful in case some pattern we're looking for is split into multiple lines, which would make it harder to regex.
	 */
	public static @NotNull String concatenateLore(@NotNull List<Text> lore) {
	    StringBuilder stringBuilder = new StringBuilder();
	    for (int i = 0; i < lore.size(); i++) {
	        stringBuilder.append(lore.get(i).getString());
	        if (i != lore.size() - 1) stringBuilder.append(" ");
	    }
	    return stringBuilder.toString();
	}
}
