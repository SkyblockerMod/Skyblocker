package de.hysky.skyblocker.utils;

import com.google.gson.JsonElement;
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
import de.hysky.skyblocker.skyblock.hunting.Attribute;
import de.hysky.skyblocker.skyblock.hunting.Attributes;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.CraftPriceTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.ObtainedDateTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.networth.NetworthCalculator;
import io.github.moulberry.repo.data.NEUItem;
import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.longs.LongBooleanPair;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.azureaaron.networth.Calculation;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class ItemUtils {
	public static final String ID = "id";
	public static final String UUID = "uuid";
	public static final Pattern NOT_DURABILITY = Pattern.compile("[^0-9 /]");
	public static final Predicate<String> FUEL_PREDICATE = line -> line.contains("Fuel: ");
	private static final Codec<Holder<Item>> EMPTY_ALLOWING_ITEM_CODEC = BuiltInRegistries.ITEM.holderByNameCodec();
	public static final Codec<ItemStack> EMPTY_ALLOWING_ITEMSTACK_CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
			EMPTY_ALLOWING_ITEM_CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder),
			Codec.INT.orElse(1).fieldOf("count").forGetter(ItemStack::getCount),
			DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch)
	).apply(instance, ItemStack::new)));
	private static final Logger LOGGER = LoggerFactory.getLogger(ItemUtils.class);
	private static final Pattern STORED_PATTERN = Pattern.compile("Stored: ([\\d,]+)/\\S+");
	private static final Pattern GEMSTONES_SACK_AMOUNT_PATTERN = Pattern.compile(" Amount: ([\\d,]+)");
	private static final Pattern STASH_COUNT_PATTERN = Pattern.compile("x([\\d,]+)$"); // This is used with Matcher#find, not #matches
	private static final Pattern HUNTING_BOX_COUNT_PATTERN = Pattern.compile("Owned: (?<shards>[\\d,]+) Shards?");
	private static final short LOG_INTERVAL = 1000;
	private static long lastLog = Util.getMillis();

	private ItemUtils() {}

	public static LiteralArgumentBuilder<FabricClientCommandSource> dumpHeldItemCommand() {
		return literal("dumpHeldItem").executes(context -> {
			context.getSource().sendFeedback(Component.literal("[Skyblocker Debug] Held Item: " + SkyblockerMod.GSON_COMPACT.toJson(ItemStack.CODEC.encodeStart(Utils.getRegistryWrapperLookup().createSerializationContext(JsonOps.INSTANCE), context.getSource().getPlayer().getMainHandItem()).getOrThrow())));
			return Command.SINGLE_SUCCESS;
		});
	}

	public static LiteralArgumentBuilder<FabricClientCommandSource> dumpHeldItemNetworthCalculationsCommand() {
		return literal("dumpHeldItemNetworthCalcs").executes(context -> {
			context.getSource().sendFeedback(Component.literal("[Skyblocker Debug] Held Item NW Calcs: " + SkyblockerMod.GSON_COMPACT.toJson(Calculation.LIST_CODEC.encodeStart(JsonOps.INSTANCE, NetworthCalculator.getItemNetworth(context.getSource().getPlayer().getMainHandItem()).calculations()).getOrThrow())));
			return Command.SINGLE_SUCCESS;
		});
	}

	/**
	 * Gets the nbt in the custom data component of the item stack.
	 * @return The {@link DataComponents#CUSTOM_DATA custom data} of the itemstack,
	 *         or an empty {@link CompoundTag} if the itemstack is missing a custom data component
	 */
	public static CompoundTag getCustomData(DataComponentHolder stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	/**
	 * Gets the Skyblock item id of the item stack.
	 *
	 * @param stack the item stack to get the internal name from
	 * @return an optional containing the Skyblock item id of the item stack
	 */
	public static Optional<String> getItemIdOptional(DataComponentHolder stack) {
		CompoundTag customData = getCustomData(stack);
		return customData.getString(ID);
	}

	/**
	 * Gets the Skyblock item id of the item stack.
	 *
	 * @param stack the item stack to get the internal name from
	 * @return the Skyblock item id of the item stack, or an empty string if the item stack does not have a Skyblock id
	 *
	 * @deprecated use {@link ItemStack#getSkyblockId()}
	 */
	@Deprecated(since = "5.8.0")
	public static String getItemId(DataComponentHolder stack) {
		return getCustomData(stack).getStringOr(ID, "");
	}

	/**
	 * Gets the UUID of the item stack.
	 *
	 * @param stack the item stack to get the UUID from
	 * @return an optional containing the UUID of the item stack
	 */
	public static Optional<String> getItemUuidOptional(DataComponentHolder stack) {
		CompoundTag customData = getCustomData(stack);
		return customData.getString(UUID);
	}

	/**
	 * Gets the UUID of the item stack.
	 *
	 * @param stack the item stack to get the UUID from
	 * @return the UUID of the item stack, or an empty string if the item stack does not have a UUID
	 *
	 * @deprecated use {@link ItemStack#getUuid()}
	 */
	@Deprecated(since = "5.8.0")
	public static String getItemUuid(DataComponentHolder stack) {
		return getCustomData(stack).getStringOr(UUID, "");
	}

	/**
	 * Gets the Skyblock api id of the item stack.
	 * @return the Skyblock api id if of the item stack, or null if the item stack does not have a Skyblock id.
	 *
	 * @deprecated use {@link ItemStack#getSkyblockApiId()} instead
	 */
	@Deprecated(since = "5.8.0")
	public static String getSkyblockApiId(DataComponentHolder itemStack) {
		CompoundTag customData = getCustomData(itemStack);
		String id = customData.getStringOr(ID, "");

		// Transformation to API format.
		//TODO future - remove this and just handle it directly for the NEU id conversion because this whole system is confusing and hard to follow
		if (customData.contains("is_shiny")) {
			return "SHINY_" + id;
		}

		// Some repo items have their IDs set to their internal names
		if (id.contains(";") && !NEURepoManager.isLoading()) {
			return NEURepoManager.getConstants().getBazaarStocks().getBazaarStockOrDefault(id);
		}

		switch (id) {
			case "ENCHANTED_BOOK" -> {
				if (customData.contains("enchantments")) {
					CompoundTag enchants = customData.getCompoundOrEmpty("enchantments");
					Optional<String> firstEnchant = enchants.keySet().stream().findFirst();
					String enchant = firstEnchant.orElse("");
					return "ENCHANTMENT_" + enchant.toUpperCase(Locale.ENGLISH) + "_" + enchants.getIntOr(enchant, 0);
				}
			}
			case "PET" -> {
				if (customData.contains("petInfo")) {
					PetInfo petInfo = PetInfo.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(customData.getStringOr("petInfo", ""))).getOrThrow();
					return "LVL_1_" + petInfo.tier() + "_" + petInfo.type();
				}
			}
			case "POTION" -> {
				String enhanced = customData.contains("enhanced") ? "_ENHANCED" : "";
				String extended = customData.contains("extended") ? "_EXTENDED" : "";
				String splash = customData.contains("splash") ? "_SPLASH" : "";
				if (customData.contains("potion") && customData.contains("potion_level")) {
					return (customData.getStringOr("potion", "") + "_" + id + "_" + customData.getIntOr("potion_level", 0)
							+ enhanced + extended + splash).toUpperCase(Locale.ENGLISH);
				}
			}
			case "RUNE" -> {
				if (customData.contains("runes")) {
					CompoundTag runes = customData.getCompoundOrEmpty("runes");
					String rune = runes.keySet().stream().findFirst().orElse("");
					return rune.toUpperCase(Locale.ENGLISH) + "_RUNE_" + runes.getIntOr(rune, 0);
				}
			}
			case "ATTRIBUTE_SHARD" -> {
				Attribute attribute = Attributes.getAttributeFromItemName(itemStack);

				if (attribute != null) return attribute.apiId();
			}
			case "NEW_YEAR_CAKE" -> {
				return id + "_" + customData.getIntOr("new_years_cake", 0);
			}
			case "PARTY_HAT_CRAB", "PARTY_HAT_CRAB_ANIMATED", "BALLOON_HAT_2024", "BALLOON_HAT_2025" -> {
				return id + "_" + customData.getStringOr("party_hat_color", "").toUpperCase(Locale.ENGLISH);
			}
			case "PARTY_HAT_SLOTH" -> {
				return id + "_" + customData.getStringOr("party_hat_emoji", "").toUpperCase(Locale.ENGLISH);
			}
			case "MIDAS_SWORD" -> {
				if (customData.getIntOr("winning_bid", 0) >= 50000000) {
					return id + "_50M";
				}
			}
			case "MIDAS_STAFF" -> {
				if (customData.getIntOr("winning_bid", 0) >= 100000000) {
					return id + "_100M";
				}
			}
			case "" -> {
				Screen currentScreen = Minecraft.getInstance().screen;
				if (currentScreen instanceof ContainerScreen container && container.getTitle().getString().startsWith("Superpairs")) {
					ItemLore lore = itemStack.get(DataComponents.LORE);
					if (lore == null) return id;
					List<Component> lines = lore.lines();
					if (lines.size() < 3) return id;
					return EnchantedBookUtils.getApiIdByName(lines.get(2));
				}

				// Get proper id for books in the Bazaar
				if (itemStack instanceof ItemStack realStack) {
					if (!realStack.is(Items.ENCHANTED_BOOK)) return id;
					Component stackName = itemStack.get(DataComponents.CUSTOM_NAME);
					if (stackName == null) return id;
					return EnchantedBookUtils.getApiIdByName(stackName);
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
	 *
	 * @deprecated use {@link ItemStack#getNeuName()} instead
	 */
	@Deprecated(since = "5.8.0")
	public static String getNeuId(ItemStack stack) {
		if (stack == null) return "";
		String id = stack.getSkyblockId();
		CompoundTag customData = ItemUtils.getCustomData(stack);
		return switch (id) {
			case "ENCHANTED_BOOK" -> {
				CompoundTag enchantments = customData.getCompoundOrEmpty("enchantments");
				String enchant = enchantments.keySet().stream().findFirst().orElse("");
				yield enchant.toUpperCase(Locale.ENGLISH) + ";" + enchantments.getIntOr(enchant, 0);
			}
			case "PET" -> {
				if (!customData.contains("petInfo")) yield id;
				PetInfo petInfo = PetInfo.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(customData.getStringOr("petInfo", ""))).getOrThrow();
				yield petInfo.type() + ';' + petInfo.tierIndex();
			}
			case "RUNE" -> {
				CompoundTag runes = customData.getCompoundOrEmpty("runes");
				String rune = runes.keySet().stream().findFirst().orElse("");
				yield rune.toUpperCase(Locale.ENGLISH) + "_RUNE;" + runes.getIntOr(rune, 0);
			}
			case "POTION" -> "POTION_" + customData.getStringOr("potion", "").toUpperCase(Locale.ENGLISH) + ";" + customData.getIntOr("potion_level", 0);
			case "ATTRIBUTE_SHARD" -> {
				Attribute attribute = Attributes.getAttributeFromItemName(stack);
				if (attribute == null) yield id;
				yield attribute.neuId();
			}
			case "PARTY_HAT_CRAB", "BALLOON_HAT_2024", "BALLOON_HAT_2025" -> id + "_" + customData.getStringOr("party_hat_color", "").toUpperCase(Locale.ENGLISH);
			case "PARTY_HAT_CRAB_ANIMATED" -> "PARTY_HAT_CRAB_" + customData.getStringOr("party_hat_color", "").toUpperCase(Locale.ENGLISH) + "_ANIMATED";
			case "PARTY_HAT_SLOTH" -> id + "_" + customData.getStringOr("party_hat_emoji", "").toUpperCase(Locale.ENGLISH);
			default -> id.replace(":", "-");
		};
	}

	/**
	 * Parses the {@code petInfo} field from a pet item that has it into the {@link PetInfo} record.
	 *
	 * @return the parsed {@link PetInfo} if successful, or {@link PetInfo#EMPTY}
	 *
	 * @deprecated use {@link ItemStack#getPetInfo()} instead
	 */
	@Deprecated(since = "5.8.0")
	public static PetInfo getPetInfo(ItemStack stack) {
		if (!stack.getSkyblockId().equals("PET")) return PetInfo.EMPTY;

		String petInfo = getCustomData(stack).getStringOr("petInfo", "");

		if (!petInfo.isEmpty()) {
			try {
				JsonElement jsonElement = JsonParser.parseString(petInfo);

				// Add item name into PetInfo to be used for wiki lookup
				jsonElement.getAsJsonObject().addProperty("name", stack.getHoverName().getString());
				return PetInfo.CODEC.parse(JsonOps.INSTANCE, jsonElement)
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
	public static DoubleBooleanPair getItemPrice(ItemStack stack) {
		return getItemPrice(stack.getSkyblockApiId(), false);
	}

	/**
	 * @see #getItemPrice(String, boolean)
	 */
	public static DoubleBooleanPair getItemPrice(@Nullable String skyblockApiId) {
		return getItemPrice(skyblockApiId, false);
	}

	/**
	 * Gets the bazaar sell price or the lowest bin of the item with the specified skyblock api id.
	 *
	 * @return An {@link LongBooleanPair} with the {@code left long} representing the item's price,
	 * and the {@code right boolean} indicating if the price was based on complete data.
	 */
	public static DoubleBooleanPair getItemPrice(@Nullable String skyblockApiId, boolean useBazaarBuyPrice) {
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

	public static double getCraftCost(String skyblockApiId) {
		NEUItem neuItem = NEURepoManager.getItemByNeuId(skyblockApiId);
		if (neuItem != null && !neuItem.getRecipes().isEmpty()) {
			return CraftPriceTooltip.getItemCost(neuItem.getRecipes().getFirst(), 0);
		}
		return 0;
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
	 *
	 * @deprecated use {@link ObtainedDateTooltip#getTimestamp(ItemStack)} instead
	 */
	@Deprecated(since = "1.21.0")
	public static String getTimestamp(ItemStack stack) {
		return ObtainedDateTooltip.getTimestamp(stack);
	}

	public static boolean hasCustomDurability(ItemStack stack) {
		CompoundTag customData = getCustomData(stack);
		return !customData.isEmpty() && (customData.contains("drill_fuel") || customData.getStringOr(ID, "").equals("PICKONIMBUS"));
	}

	public static @Nullable IntIntPair getDurability(ItemStack stack) {
		CompoundTag customData = getCustomData(stack);
		if (customData.isEmpty()) return null;

		// TODO Calculate drill durability based on the drill_fuel flag, fuel_tank flag, and hotm level
		// TODO Cache the max durability and only update the current durability on inventory tick

		if (stack.getSkyblockId().equals("PICKONIMBUS")) {
			int pickonimbusDurability = customData.getIntOr("pickonimbus_durability", 0);

			return IntIntPair.of(customData.contains("pickonimbus_durability") ? pickonimbusDurability : 2000, 2000);
		}

		String drillFuel = ChatFormatting.stripFormatting(getLoreLineIf(stack, FUEL_PREDICATE));
		if (drillFuel != null) {
			String[] drillFuelStrings = NOT_DURABILITY.matcher(drillFuel).replaceAll("").trim().split("/");
			return IntIntPair.of(Integer.parseInt(drillFuelStrings[0]), Integer.parseInt(drillFuelStrings[1]) * 1000);
		}

		return null;
	}

	/**
	 * Gets the first line of the lore that contains the specified substring.
	 * @return The first line of the lore that contains the substring, or {@code null} if no line contains the substring.
	 */
	public static @Nullable String getLoreLineContains(ItemStack stack, String substring) {
		for (String line : stack.skyblocker$getLoreStrings()) {
			if (line.contains(substring)) {
				return line;
			}
		}

		return null;
	}

	/**
	 * Gets the first line of the lore that matches the specified predicate.
	 * @return The first line of the lore that matches the predicate, or {@code null} if no line matches.
	 */
	public static @Nullable String getLoreLineIf(ItemStack stack, Predicate<String> predicate) {
		for (String line : stack.skyblocker$getLoreStrings()) {
			if (predicate.test(line)) {
				return line;
			}
		}

		return null;
	}

	/**
	 * Gets the first line of the lore that matches the specified pattern, using {@link Matcher#matches()}.
	 * @return A matcher that contains match results if the pattern was found in the lore, otherwise {@code null}.
	 */
	public static @Nullable Matcher getLoreLineIfMatch(ItemStack stack, Pattern pattern) {
		return RegexListUtils.matchInList(stack.skyblocker$getLoreStrings(), pattern);
	}

	/**
	 * Gets the first lines of the lore that matches each pattern, using {@link Matcher#matches()}.
	 * @see RegexListUtils#matchInList(List, Pattern...)
	 */
	public static List<Matcher> getLoreLineIfMatch(ItemStack stack, Pattern... patterns) {
		return RegexListUtils.matchInList(stack.skyblocker$getLoreStrings(), patterns);
	}

	/**
	 * Gets the first line of the lore that matches the specified pattern, using {@link Matcher#find()}.
	 * @param pattern the pattern to search for
	 * @param stack the stack to search the lore of
	 * @return A {@link Matcher matcher} that contains match results if the pattern was found in the lore, otherwise {@code null}.
	 */
	public static @Nullable Matcher getLoreLineIfContainsMatch(ItemStack stack, Pattern pattern) {
		return RegexListUtils.findInList(stack.skyblocker$getLoreStrings(), pattern);
	}

	/**
	 * @deprecated Consider using {@link ItemStack#skyblocker$getLoreStrings()} which caches text to string conversions.
	 */
	@Deprecated
	public static List<Component> getLore(ItemStack stack) {
		return stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).styledLines();
	}

	public static PropertyMap propertyMapWithTexture(String textureValue) {
		return ExtraCodecs.PROPERTY_MAP.parse(JsonOps.INSTANCE, JsonParser.parseString("[{\"name\":\"textures\",\"value\":\"" + textureValue + "\"}]")).getOrThrow();
	}

	public static String getHeadTexture(ItemStack stack) {
		if (!stack.is(Items.PLAYER_HEAD) || !stack.has(DataComponents.PROFILE)) return "";

		ResolvableProfile profile = stack.get(DataComponents.PROFILE);
		if (profile == null) return "";

		return profile.partialProfile().properties().get("textures").stream()
				.filter(Objects::nonNull)
				.map(Property::value)
				.findFirst()
				.orElse("");
	}

	public static Optional<String> getHeadTextureOptional(ItemStack stack) {
		String texture = getHeadTexture(stack);
		if (texture.isBlank()) return Optional.empty();
		return Optional.of(texture);
	}

	public static String toTextureBase64(String textureUUID) {
		//noinspection HttpUrlsUsage
		String str = "{textures:{SKIN:{url:\"http://textures.minecraft.net/texture/"+textureUUID+"\"}}}";
		return Base64.getEncoder().encodeToString(str.getBytes());
	}

	public static ItemStack createSkull(String textureBase64) {
		GameProfile profile = new GameProfile(java.util.UUID.randomUUID(), "a", propertyMapWithTexture(textureBase64));
		return createSkull(profile);
	}

	public static ItemStack createSkull(GameProfile profile) {
		try {
			ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
			stack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile));
			return stack;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static ItemStack getSkyblockerStack() {
		return createSkull("e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdjYzY2ODc0MjNkMDU3MGQ1NTZhYzUzZTA2NzZjYjU2M2JiZGQ5NzE3Y2Q4MjY5YmRlYmVkNmY2ZDRlN2JmOCJ9fX0=");
	}

	public static ItemStack getSkyblockerForgeStack() {
		return createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJkZGQ4OWE2YWU5NTdmNzY2ZDMwMDAxMWZmNDQ3MTQ4MWMzYmI2MWI2NzYwNzhhOGM2YzNjNDA4MzIwMWI1YzIifX19");
	}

	/**
	 * Utility method.
	 */
	public static String getConcatenatedLore(ItemStack item) {
		return concatenateLore(getLore(item));
	}

	/**
	 * Concatenates the lore of an item into one string.
	 * This is useful in case some pattern we're looking for is split into multiple lines, which would make it harder to regex.
	 */
	public static String concatenateLore(List<Component> lore) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < lore.size(); i++) {
			stringBuilder.append(lore.get(i).getString());
			if (i != lore.size() - 1) stringBuilder.append(" ");
		}
		return stringBuilder.toString();
	}

	public static boolean isSoulbound(ItemStack stack) {
		return getLore(stack).stream().anyMatch(lore -> lore.getString().toLowerCase(Locale.ENGLISH).contains("soulbound"));
	}

	public static List<ItemStack> getArmor(LivingEntity entity) {
		return EquipmentSlotGroup.ARMOR.slots().stream()
				.filter(es -> es.getType() == EquipmentSlot.Type.HUMANOID_ARMOR)
				.map(entity::getItemBySlot)
				.toList();
	}

	/**
	 * Finds the number of items stored in a sack from a list of strings.
	 *
	 * @param itemStack The item stack this list of strings belong to. This is used for logging purposes.
	 * @param lines     A list of string lines that represent the tooltip of the item stack.
	 * @return An {@link OptionalInt} containing the number of items stored in the sack, or an empty {@link OptionalInt} if the item is not a sack or the amount could not be found.
	 */
	public static OptionalInt getItemCountInSack(ItemStack itemStack, List<String> lines) {
		// Gemstones sack is a special case, it has a different 2nd line.
		if (lines.size() < 2 || !Strings.CS.endsWithAny(lines.getFirst(), "Sack", "Gemstones")) {
			return OptionalInt.empty();
		}

		// Use the proper item amount in the Gemstones Sack when sorting by Rough/Flawed/Fine
		if (itemStack.getHoverName().getString().endsWith("Gemstone")) {
			Matcher matcher = RegexListUtils.matchInList(lines, GEMSTONES_SACK_AMOUNT_PATTERN);
			if (matcher != null) {
				return RegexUtils.parseOptionalIntFromMatcher(matcher, 1);
			}
		}

		// Example line: empty[style={color=dark_purple,!italic}, siblings=[literal{Stored: }[style={color=gray}], literal{0}[style={color=dark_gray}], literal{/20k}[style={color=gray}]]
		// Which equals: `Stored: 0/20k`
		Matcher matcher = RegexListUtils.matchInList(lines, STORED_PATTERN);
		if (matcher != null) {
			return RegexUtils.parseOptionalIntFromMatcher(matcher, 1);
		}

		// Log a warning every second if the amount couldn't be found, to prevent spamming the logs every frame (which can be hundreds of times per second)
		if (Util.getMillis() - lastLog > LOG_INTERVAL) {
			LOGGER.warn("Failed to find stored amount in sack tooltip for item `{}`", Debug.DumpFormat.JSON.format(itemStack).getString()); // This is a very unintended way of serializing the item stack, but it's so much cleaner than actually using the codec
			lastLog = Util.getMillis();
		}

		return OptionalInt.empty();
	}

	/**
	 * Finds the number of items stored in a stash based on item's name.
	 * @param itemStack The item stack.
	 * @return An {@link OptionalInt} containing the number of items stored in the stash, or an empty {@link OptionalInt} if the item is not a stash or the amount could not be found.
	 */
	public static OptionalInt getItemCountInStash(ItemStack itemStack) {
		return getItemCountInStash(itemStack.getHoverName());
	}

	/**
	 * Finds the number of items stored in a stash based on item's name.
	 * @param itemName The name of the item to look in.
	 * @return An {@link OptionalInt} containing the number of items stored in the stash, or an empty {@link OptionalInt} if the item is not a stash or the amount could not be found.
	 */
	public static OptionalInt getItemCountInStash(Component itemName) {
		return RegexUtils.findIntFromMatcher(STASH_COUNT_PATTERN.matcher(itemName.getString()));
	}

	/**
	 * Finds the number of shards the player owns inside of the hunting box.
	 */
	public static OptionalInt getItemCountInHuntingBox(ItemStack stack) {
		Matcher matcher = ItemUtils.getLoreLineIfContainsMatch(stack, HUNTING_BOX_COUNT_PATTERN);

		return matcher != null ? RegexUtils.parseOptionalIntFromMatcher(matcher, "shards") : OptionalInt.empty();
	}

	/**
	 * Gets the proper item count for Enchanted Books in Superpairs.
	 * For all other items, returns empty.
	 */
	public static OptionalInt getItemCountInSuperpairs(ItemStack stack) {
		Screen currentScreen = Minecraft.getInstance().screen;
		if (currentScreen instanceof ContainerScreen container && container.getTitle().getString().startsWith("Superpairs")) {
			if (stack.getHoverName().getString().contains("Enchanted Book")) return OptionalInt.of(1);
		}
		return OptionalInt.empty();
	}

	/**
	 * @deprecated Use {@link ItemStack#getSkyblockRarity()} which caches the result.
	 */
	@Deprecated(since = "5.8.0")
	public static SkyblockItemRarity getItemRarity(ItemStack stack) {
		if (stack.isEmpty()) return SkyblockItemRarity.UNKNOWN;

		if (!stack.getSkyblockId().equals("PET")) {
			return ItemUtils.getLore(stack)
					.reversed()
					.stream()
					.map(Component::getString)
					.map(SkyblockItemRarity::containsName)
					.flatMap(Optional::stream)
					.findFirst()
					.orElse(SkyblockItemRarity.UNKNOWN);
		} else {
			PetInfo info = stack.getPetInfo();
			if (info.isEmpty()) return SkyblockItemRarity.UNKNOWN;
			return info.item().isPresent() && info.item().get().equals("PET_ITEM_TIER_BOOST") ? info.rarity().next() : info.rarity();
		}
	}

	/**
	 * Gets a placeholder Barrier {@link ItemStack}, used to display items that could not be found in the item repository.
	 */
	public static ItemStack getNamedPlaceholder(String itemName) {
		ItemStack stack = new ItemStack(Items.BARRIER);
		stack.set(DataComponents.CUSTOM_NAME, Component.literal(itemName));
		return stack;
	}
}
