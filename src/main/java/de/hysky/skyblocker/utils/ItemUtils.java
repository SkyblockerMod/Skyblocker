package de.hysky.skyblocker.utils;

import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.ObtainedDateTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.networth.NetworthCalculator;
import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.longs.LongBooleanPair;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.azureaaron.networth.Calculation;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemUtils.class);
    private static final Pattern STORED_PATTERN = Pattern.compile("Stored: ([\\d,]+)/\\S+");
    private static final Pattern STASH_COUNT_PATTERN = Pattern.compile("x([\\d,]+)$"); // This is used with Matcher#find, not #matches
    private static final short LOG_INTERVAL = 1000;
	private static long lastLog = Util.getMeasuringTimeMs();

    private ItemUtils() {}

    public static LiteralArgumentBuilder<FabricClientCommandSource> dumpHeldItemCommand() {
        return literal("dumpHeldItem").executes(context -> {
            context.getSource().sendFeedback(Text.literal("[Skyblocker Debug] Held Item: " + SkyblockerMod.GSON_COMPACT.toJson(ItemStack.CODEC.encodeStart(Utils.getRegistryWrapperLookup().getOps(JsonOps.INSTANCE), context.getSource().getPlayer().getMainHandStack()).getOrThrow())));
            return Command.SINGLE_SUCCESS;
        });
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> dumpHeldItemNetworthCalculationsCommand() {
        return literal("dumpHeldItemNetworthCalcs").executes(context -> {
            context.getSource().sendFeedback(Text.literal("[Skyblocker Debug] Held Item NW Calcs: " + SkyblockerMod.GSON_COMPACT.toJson(Calculation.LIST_CODEC.encodeStart(JsonOps.INSTANCE, NetworthCalculator.getItemNetworth(context.getSource().getPlayer().getMainHandStack()).calculations()).getOrThrow())));
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
     * @return an optional containing the Skyblock item id of the item stack
     */
    public static @NotNull Optional<String> getItemIdOptional(@NotNull ComponentHolder stack) {
        NbtCompound customData = getCustomData(stack);
        return customData.getString(ID);
    }

    /**
     * Gets the Skyblock item id of the item stack.
     *
     * @param stack the item stack to get the internal name from
     * @return the Skyblock item id of the item stack, or an empty string if the item stack does not have a Skyblock id
     */
    public static @NotNull String getItemId(@NotNull ComponentHolder stack) {
        return getCustomData(stack).getString(ID, "");
    }

    /**
     * Gets the UUID of the item stack.
     *
     * @param stack the item stack to get the UUID from
     * @return an optional containing the UUID of the item stack
     */
    public static @NotNull Optional<String> getItemUuidOptional(@NotNull ComponentHolder stack) {
        NbtCompound customData = getCustomData(stack);
        return customData.getString(UUID);
    }

    /**
     * Gets the UUID of the item stack.
     *
     * @param stack the item stack to get the UUID from
     * @return the UUID of the item stack, or an empty string if the item stack does not have a UUID
     */
    public static @NotNull String getItemUuid(@NotNull ComponentHolder stack) {
        return getCustomData(stack).getString(UUID, "");
    }

    /**
     * Gets the Skyblock api id of the item stack.
     * @return the Skyblock api id if of the item stack, or null if the item stack does not have a Skyblock id.
     */
    public static @NotNull String getSkyblockApiId(@NotNull ComponentHolder itemStack) {
        NbtCompound customData = getCustomData(itemStack);
        String id = customData.getString(ID, "");

        // Transformation to API format.
        //TODO future - remove this and just handle it directly for the NEU id conversion because this whole system is confusing and hard to follow
        if (customData.contains("is_shiny")) {
            return "SHINY_" + id;
        }

        switch (id) {
            case "ENCHANTED_BOOK" -> {
                if (customData.contains("enchantments")) {
                    NbtCompound enchants = customData.getCompoundOrEmpty("enchantments");
                    Optional<String> firstEnchant = enchants.getKeys().stream().findFirst();
                    String enchant = firstEnchant.orElse("");
                    return "ENCHANTMENT_" + enchant.toUpperCase(Locale.ENGLISH) + "_" + enchants.getInt(enchant, 0);
                }
            }
            case "PET" -> {
                if (customData.contains("petInfo")) {
                    PetInfo petInfo = PetInfo.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(customData.getString("petInfo", ""))).getOrThrow();
                    return "LVL_1_" + petInfo.tier() + "_" + petInfo.type();
                }
            }
            case "POTION" -> {
                String enhanced = customData.contains("enhanced") ? "_ENHANCED" : "";
                String extended = customData.contains("extended") ? "_EXTENDED" : "";
                String splash = customData.contains("splash") ? "_SPLASH" : "";
                if (customData.contains("potion") && customData.contains("potion_level")) {
                    return (customData.getString("potion", "") + "_" + id + "_" + customData.getInt("potion_level", 0)
                            + enhanced + extended + splash).toUpperCase(Locale.ENGLISH);
                }
            }
            case "RUNE" -> {
                if (customData.contains("runes")) {
                    NbtCompound runes = customData.getCompoundOrEmpty("runes");
                    String rune = runes.getKeys().stream().findFirst().orElse("");
                    return rune.toUpperCase(Locale.ENGLISH) + "_RUNE_" + runes.getInt(rune, 0);
                }
            }
            case "ATTRIBUTE_SHARD" -> {
                if (customData.contains("attributes")) {
                    NbtCompound shards = customData.getCompoundOrEmpty("attributes");
                    String shard = shards.getKeys().stream().findFirst().orElse("");
                    return id + "-" + shard.toUpperCase(Locale.ENGLISH) + "_" + shards.getInt(shard, 0);
                }
            }
            case "NEW_YEAR_CAKE" -> {
                return id + "_" + customData.getInt("new_years_cake", 0);
            }
            case "PARTY_HAT_CRAB", "PARTY_HAT_CRAB_ANIMATED", "BALLOON_HAT_2024", "BALLOON_HAT_2025" -> {
                return id + "_" + customData.getString("party_hat_color", "").toUpperCase(Locale.ENGLISH);
            }
            case "PARTY_HAT_SLOTH" -> {
                return id + "_" + customData.getString("party_hat_emoji", "").toUpperCase(Locale.ENGLISH);
            }
            case "MIDAS_SWORD" -> {
                if (customData.getInt("winning_bid", 0) >= 50000000) {
                    return id + "_50M";
                }
            }
            case "MIDAS_STAFF" -> {
                if (customData.getInt("winning_bid", 0) >= 100000000) {
                    return id + "_100M";
                }
            }
        }
        return id;
    }

    /**
     * Gets the NEU id from an id and an api id.
     *
     * @return the NEU id of the skyblock item, matching the id of the item gotten from {@link io.github.moulberry.repo.data.NEUItem#getSkyblockItemId() NEUItem#getSkyblockItemId()} or {@link ItemStack#getNeuName()},
     * or an empty string if stack is null
     */
    public static @NotNull String getNeuId(ItemStack stack) {
        if (stack == null) return "";
        String id = stack.getSkyblockId();
        NbtCompound customData = ItemUtils.getCustomData(stack);
        return switch (id) {
            case "ENCHANTED_BOOK" -> {
                NbtCompound enchantments = customData.getCompoundOrEmpty("enchantments");
                String enchant = enchantments.getKeys().stream().findFirst().orElse("");
                yield enchant.toUpperCase(Locale.ENGLISH) + ";" + enchantments.getInt(enchant, 0);
            }
            case "PET" -> {
                if (!customData.contains("petInfo")) yield id;
                PetInfo petInfo = PetInfo.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(customData.getString("petInfo", ""))).getOrThrow();
                yield petInfo.type() + ';' + petInfo.tierIndex();
            }
            case "RUNE" -> {
                NbtCompound runes = customData.getCompoundOrEmpty("runes");
                String rune = runes.getKeys().stream().findFirst().orElse("");
                yield rune.toUpperCase(Locale.ENGLISH) + "_RUNE;" + runes.getInt(rune, 0);
            }
            case "POTION" -> "POTION_" + customData.getString("potion", "").toUpperCase(Locale.ENGLISH) + ";" + customData.getInt("potion_level", 0);
            case "ATTRIBUTE_SHARD" -> "ATTRIBUTE_SHARD";
            case "PARTY_HAT_CRAB", "BALLOON_HAT_2024", "BALLOON_HAT_2025" -> id + "_" + customData.getString("party_hat_color", "").toUpperCase(Locale.ENGLISH);
            case "PARTY_HAT_CRAB_ANIMATED" -> "PARTY_HAT_CRAB_" + customData.getString("party_hat_color", "").toUpperCase(Locale.ENGLISH) + "_ANIMATED";
            case "PARTY_HAT_SLOTH" -> id + "_" + customData.getString("party_hat_emoji", "").toUpperCase(Locale.ENGLISH);
            default -> id.replace(":", "-");
        };
    }

    /**
     * Parses the {@code petInfo} field from a pet item that has it into the {@link PetInfo} record.
     *
     * @return the parsed {@link PetInfo} if successful, or {@link PetInfo#EMPTY}
     */
    @NotNull
    public static PetInfo getPetInfo(ComponentHolder stack) {
    	if (!getItemId(stack).equals("PET")) return PetInfo.EMPTY;

    	String petInfo = getCustomData(stack).getString("petInfo", "");

    	if (!petInfo.isEmpty()) {
    		try {
        		return PetInfo.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(petInfo))
        				.setPartial(PetInfo.EMPTY)
        				.getPartialOrThrow();
    		} catch (Exception ignored) {}
    	}

		return PetInfo.EMPTY;
    }

    /**
     * Gets the bazaar sell price or the lowest bin based on the id of the item stack.
     *
     * @return An {@link LongBooleanPair} with the {@code left long} representing the item's price,
     * and the {@code right boolean} indicating if the price was based on complete data.
     */
    public static @NotNull DoubleBooleanPair getItemPrice(@NotNull ItemStack stack) {
        return getItemPrice(stack.getSkyblockApiId(), false);
    }

    /**
     * @see #getItemPrice(String, boolean)
     */
    public static @NotNull DoubleBooleanPair getItemPrice(@Nullable String skyblockApiId) {
        return getItemPrice(skyblockApiId, false);
    }

    /**
     * Gets the bazaar sell price or the lowest bin of the item with the specified skyblock api id.
     *
     * @return An {@link LongBooleanPair} with the {@code left long} representing the item's price,
     * and the {@code right boolean} indicating if the price was based on complete data.
     */
    public static @NotNull DoubleBooleanPair getItemPrice(@Nullable String skyblockApiId, boolean useBazaarBuyPrice) {
        Object2ObjectMap<String, BazaarProduct> bazaarPrices = TooltipInfoType.BAZAAR.getData();
        Object2DoubleMap<String> lowestBinPrices = TooltipInfoType.LOWEST_BINS.getData();

        if (skyblockApiId == null || skyblockApiId.isEmpty() || bazaarPrices == null || lowestBinPrices == null) return DoubleBooleanPair.of(0, false);

        if (bazaarPrices.containsKey(skyblockApiId)) {
            BazaarProduct product = bazaarPrices.get(skyblockApiId);
            OptionalDouble price = useBazaarBuyPrice ? product.buyPrice() : product.sellPrice();

            return DoubleBooleanPair.of(price.orElse(0d), price.isPresent());
        }

        if (lowestBinPrices.containsKey(skyblockApiId)) {
            return DoubleBooleanPair.of(lowestBinPrices.getDouble(skyblockApiId), true);
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
        return !customData.isEmpty() && (customData.contains("drill_fuel") || customData.getString(ID, "").equals("PICKONIMBUS"));
    }

    @Nullable
    public static IntIntPair getDurability(@NotNull ItemStack stack) {
        NbtCompound customData = getCustomData(stack);
        if (customData.isEmpty()) return null;

        // TODO Calculate drill durability based on the drill_fuel flag, fuel_tank flag, and hotm level
        // TODO Cache the max durability and only update the current durability on inventory tick

        if (stack.getSkyblockId().equals("PICKONIMBUS")) {
            int pickonimbusDurability = customData.getInt("pickonimbus_durability", 0);

            return IntIntPair.of(customData.contains("pickonimbus_durability") ? pickonimbusDurability : 2000, 2000);
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
        return TextUtils.matchInList(getLore(stack), pattern);
    }

    /**
     * Gets the first line of the lore that matches the specified pattern, using {@link Matcher#find()}.
     * @param pattern the pattern to search for
     * @param stack the stack to search the lore of
     * @return A {@link Matcher matcher} that contains match results if the pattern was found in the lore, otherwise {@code null}.
     */
    @Nullable
    public static Matcher getLoreLineIfContainsMatch(ItemStack stack, Pattern pattern) {
        return TextUtils.findInList(getLore(stack), pattern);
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

	public static @NotNull ItemStack createSkull(String textureBase64) {
		GameProfile profile = new GameProfile(java.util.UUID.randomUUID(), "a");
		profile.getProperties().put("textures", new Property("textures", textureBase64));
		return createSkull(profile);
	}

	public static @NotNull ItemStack createSkull(GameProfile profile) {
		try {
			ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
			stack.set(DataComponentTypes.PROFILE, new ProfileComponent(profile));
			return stack;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static @NotNull ItemStack getSkyblockerStack() {
		return createSkull("e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdjYzY2ODc0MjNkMDU3MGQ1NTZhYzUzZTA2NzZjYjU2M2JiZGQ5NzE3Y2Q4MjY5YmRlYmVkNmY2ZDRlN2JmOCJ9fX0=");
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

    public static List<ItemStack> getArmor(LivingEntity entity) {
    	return AttributeModifierSlot.ARMOR.getSlots().stream()
    			.filter(es -> es.getType() == EquipmentSlot.Type.HUMANOID_ARMOR)
    			.map(entity::getEquippedStack)
    			.toList();
    }

    /**
     * Finds the number of items stored in a sack based on the tooltip lines.
     * @param itemStack The item stack these lines belong to. This is used for logging purposes.
     * @param lines The tooltip lines to search in. This isn't equivalent to the item's lore.
     * @return An {@link OptionalInt} containing the number of items stored in the sack, or an empty {@link OptionalInt} if the item is not a sack or the amount could not be found.
     */
    @NotNull
    public static OptionalInt getItemCountInSack(@NotNull ItemStack itemStack, @NotNull List<Text> lines) {
        return getItemCountInSack(itemStack, lines, false);
    }

    /**
     * Finds the number of items stored in a sack from a list of texts.
     * @param itemStack The item stack this list of texts belong to. This is used for logging purposes.
     * @param lines A list of text lines that represent the tooltip of the item stack.
     * @param isLore Whether the lines are from the item's lore or not. This is useful to figure out which line to look at, as lore and tooltip lines are different due to the first line being the item's name.
     * @return An {@link OptionalInt} containing the number of items stored in the sack, or an empty {@link OptionalInt} if the item is not a sack or the amount could not be found.
     */
    @NotNull
    public static OptionalInt getItemCountInSack(@NotNull ItemStack itemStack, @NotNull List<Text> lines, boolean isLore) {
        // Gemstone sack is a special case, it has a different 2nd line.
		if (lines.size() >= 2 && StringUtils.endsWithAny(lines.get(isLore ? 0 : 1).getString(), "Sack", "Gemstones")) {
			// Example line: empty[style={color=dark_purple,!italic}, siblings=[literal{Stored: }[style={color=gray}], literal{0}[style={color=dark_gray}], literal{/20k}[style={color=gray}]]
            // Which equals: `Stored: 0/20k`
			Matcher matcher = TextUtils.matchInList(lines, STORED_PATTERN);
			if (matcher == null) {
				// Log a warning every second if the amount couldn't be found, to prevent spamming the logs every frame (which can be hundreds of times per second)
				if (Util.getMeasuringTimeMs() - lastLog > LOG_INTERVAL) {
					LOGGER.warn("Failed to find stored amount in sack tooltip for item `{}`", Debug.DumpFormat.JSON.format(itemStack).getString()); // This is a very unintended way of serializing the item stack, but it's so much cleaner than actually using the codec
					lastLog = Util.getMeasuringTimeMs();
				}
				return OptionalInt.empty();
			} else return RegexUtils.parseOptionalIntFromMatcher(matcher, 1);
		}
		return OptionalInt.empty();
    }

    /**
     * Finds the number of items stored in a stash based on item's name.
     * @param itemStack The item stack.
     * @return An {@link OptionalInt} containing the number of items stored in the stash, or an empty {@link OptionalInt} if the item is not a stash or the amount could not be found.
     */
    @NotNull
    public static OptionalInt getItemCountInStash(@NotNull ItemStack itemStack) {
        return getItemCountInStash(itemStack.getName());
    }

    /**
     * Finds the number of items stored in a stash based on item's name.
     * @param itemName The name of the item to look in.
     * @return An {@link OptionalInt} containing the number of items stored in the stash, or an empty {@link OptionalInt} if the item is not a stash or the amount could not be found.
     */
    @NotNull
    public static OptionalInt getItemCountInStash(@NotNull Text itemName) {
        return RegexUtils.findIntFromMatcher(STASH_COUNT_PATTERN.matcher(itemName.getString()));
    }
}
