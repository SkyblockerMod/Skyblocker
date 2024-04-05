package de.hysky.skyblocker.skyblock.item.tooltip;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.MuseumItemCache;
import de.hysky.skyblocker.skyblock.item.tooltip.AccessoriesHelper.AccessoryReport;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ItemTooltip {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ItemTooltip.class.getName());
    private static final MinecraftClient client = MinecraftClient.getInstance();
    protected static final SkyblockerConfig.ItemTooltip config = SkyblockerConfigManager.get().general.itemTooltip;
    private static volatile boolean sentNullWarning = false;

    public static void getTooltip(ItemStack stack, TooltipContext context, List<Text> lines) {
        if (!Utils.isOnSkyblock() || client.player == null) return;

        String name = getInternalNameFromNBT(stack, false);
        String internalID = getInternalNameFromNBT(stack, true);
        String neuName = name;
        if (name == null || internalID == null) return;

        if (name.startsWith("ISSHINY_")) {
            name = "SHINY_" + internalID;
            neuName = internalID;
        }

        if (lines.isEmpty()) {
            return;
        }

        int count = stack.getCount();
        boolean bazaarOpened = lines.stream().anyMatch(each -> each.getString().contains("Buy price:") || each.getString().contains("Sell price:"));

        if (TooltipInfoType.NPC.isTooltipEnabledAndHasOrNullWarning(internalID)) {
            lines.add(Text.literal(String.format("%-21s", "NPC Sell Price:"))
                    .formatted(Formatting.YELLOW)
                    .append(getCoinsMessage(TooltipInfoType.NPC.getData().get(internalID).getAsDouble(), count)));
        }

        boolean bazaarExist = false;

        if (TooltipInfoType.BAZAAR.isTooltipEnabledAndHasOrNullWarning(name) && !bazaarOpened) {
            JsonObject getItem = TooltipInfoType.BAZAAR.getData().getAsJsonObject(name);
            lines.add(Text.literal(String.format("%-18s", "Bazaar buy Price:"))
                    .formatted(Formatting.GOLD)
                    .append(getItem.get("buyPrice").isJsonNull()
                            ? Text.literal("No data").formatted(Formatting.RED)
                            : getCoinsMessage(getItem.get("buyPrice").getAsDouble(), count)));
            lines.add(Text.literal(String.format("%-19s", "Bazaar sell Price:"))
                    .formatted(Formatting.GOLD)
                    .append(getItem.get("sellPrice").isJsonNull()
                            ? Text.literal("No data").formatted(Formatting.RED)
                            : getCoinsMessage(getItem.get("sellPrice").getAsDouble(), count)));
            bazaarExist = true;
        }

        // bazaarOpened & bazaarExist check for lbin, because Skytils keeps some bazaar item data in lbin api
        boolean lbinExist = false;
        if (TooltipInfoType.LOWEST_BINS.isTooltipEnabledAndHasOrNullWarning(name) && !bazaarOpened && !bazaarExist) {
            lines.add(Text.literal(String.format("%-19s", "Lowest BIN Price:"))
                    .formatted(Formatting.GOLD)
                    .append(getCoinsMessage(TooltipInfoType.LOWEST_BINS.getData().get(name).getAsDouble(), count)));
            lbinExist = true;
        }

        if (SkyblockerConfigManager.get().general.itemTooltip.enableAvgBIN) {
            if (TooltipInfoType.ONE_DAY_AVERAGE.getData() == null || TooltipInfoType.THREE_DAY_AVERAGE.getData() == null) {
                nullWarning();
            } else {
                /*
                  We are skipping check average prices for potions, runes
                  and enchanted books because there is no data for their in API.
                 */
                neuName = getNeuName(internalID, neuName);

                if (!neuName.isEmpty() && lbinExist) {
                    SkyblockerConfig.Average type = config.avg;

                    // "No data" line because of API not keeping old data, it causes NullPointerException
                    if (type == SkyblockerConfig.Average.ONE_DAY || type == SkyblockerConfig.Average.BOTH) {
                        lines.add(
                                Text.literal(String.format("%-19s", "1 Day Avg. Price:"))
                                        .formatted(Formatting.GOLD)
                                        .append(TooltipInfoType.ONE_DAY_AVERAGE.getData().get(neuName) == null
                                                ? Text.literal("No data").formatted(Formatting.RED)
                                                : getCoinsMessage(TooltipInfoType.ONE_DAY_AVERAGE.getData().get(neuName).getAsDouble(), count)
                                        )
                        );
                    }
                    if (type == SkyblockerConfig.Average.THREE_DAY || type == SkyblockerConfig.Average.BOTH) {
                        lines.add(
                                Text.literal(String.format("%-19s", "3 Day Avg. Price:"))
                                        .formatted(Formatting.GOLD)
                                        .append(TooltipInfoType.THREE_DAY_AVERAGE.getData().get(neuName) == null
                                                ? Text.literal("No data").formatted(Formatting.RED)
                                                : getCoinsMessage(TooltipInfoType.THREE_DAY_AVERAGE.getData().get(neuName).getAsDouble(), count)
                                        )
                        );
                    }
                }
            }
        }

        final Map<Integer, String> itemTierFloors = new HashMap<>() {{
            put(1, "F1");
            put(2, "F2");
            put(3, "F3");
            put(4, "F4/M1");
            put(5, "F5/M2");
            put(6, "F6/M3");
            put(7, "F7/M4");
            put(8, "M5");
            put(9, "M6");
            put(10, "M7");
        }};

        if (SkyblockerConfigManager.get().general.dungeonQuality) {
            NbtCompound ea = ItemUtils.getExtraAttributes(stack);
            if (ea != null && ea.contains("baseStatBoostPercentage")) {
                int baseStatBoostPercentage = ea.getInt("baseStatBoostPercentage");
                boolean maxQuality = baseStatBoostPercentage == 50;
                if (maxQuality) {
                    lines.add(Text.literal(String.format("%-17s", "Item Quality:") + baseStatBoostPercentage + "/50").formatted(Formatting.RED).formatted(Formatting.BOLD));
                } else {
                    lines.add(Text.literal(String.format("%-21s", "Item Quality:") + baseStatBoostPercentage + "/50").formatted(Formatting.BLUE));
                }
                if (ea.contains("item_tier")) {     // sometimes it just isn't here?
                    int itemTier = ea.getInt("item_tier");
                    if (maxQuality) {
                        lines.add(Text.literal(String.format("%-17s", "Floor Tier:") + itemTier + " (" + itemTierFloors.get(itemTier) + ")").formatted(Formatting.RED).formatted(Formatting.BOLD));
                    } else {
                        lines.add(Text.literal(String.format("%-21s", "Floor Tier:") + itemTier + " (" + itemTierFloors.get(itemTier) + ")").formatted(Formatting.BLUE));
                    }
                }
            }
        }

        if (TooltipInfoType.MOTES.isTooltipEnabledAndHasOrNullWarning(internalID)) {
            lines.add(Text.literal(String.format("%-20s", "Motes Price:"))
                    .formatted(Formatting.LIGHT_PURPLE)
                    .append(getMotesMessage(TooltipInfoType.MOTES.getData().get(internalID).getAsInt(), count)));
        }

        if (TooltipInfoType.OBTAINED.isTooltipEnabled()) {
            String timestamp = ItemUtils.getTimestamp(stack);

            if (!timestamp.isEmpty()) {
                lines.add(Text.literal(String.format("%-21s", "Obtained: "))
                        .formatted(Formatting.LIGHT_PURPLE)
                        .append(Text.literal(timestamp).formatted(Formatting.RED)));
            }
        }

        if (TooltipInfoType.MUSEUM.isTooltipEnabledAndHasOrNullWarning(internalID) && !bazaarOpened) {
            String itemCategory = TooltipInfoType.MUSEUM.getData().get(internalID).getAsString();
            String format = switch (itemCategory) {
                case "Weapons" -> "%-18s";
                case "Armor" -> "%-19s";
                default -> "%-20s";
            };

            //Special case the special category so that it doesn't always display not donated
            if (itemCategory.equals("Special")) {
                lines.add(Text.literal(String.format(format, "Museum: (" + itemCategory + ")"))
                        .formatted(Formatting.LIGHT_PURPLE));
            } else {
                NbtCompound extraAttributes = ItemUtils.getExtraAttributes(stack);
                boolean isInMuseum = (extraAttributes.contains("donated_museum") && extraAttributes.getBoolean("donated_museum")) || MuseumItemCache.hasItemInMuseum(internalID);

                Formatting donatedIndicatorFormatting = isInMuseum ? Formatting.GREEN : Formatting.RED;

                lines.add(Text.literal(String.format(format, "Museum (" + itemCategory + "):"))
                        .formatted(Formatting.LIGHT_PURPLE)
                        .append(Text.literal(isInMuseum ? "✔" : "✖").formatted(donatedIndicatorFormatting, Formatting.BOLD))
                        .append(Text.literal(isInMuseum ? " Donated" : " Not Donated").formatted(donatedIndicatorFormatting)));
            }
        }

        if (TooltipInfoType.COLOR.isTooltipEnabledAndHasOrNullWarning(internalID) && stack.getNbt() != null) {
            String uuid = ItemUtils.getItemUuid(stack);
            boolean hasCustomDye = SkyblockerConfigManager.get().general.customDyeColors.containsKey(uuid) || SkyblockerConfigManager.get().general.customAnimatedDyes.containsKey(uuid);

            if (!hasCustomDye && stack.getItem() instanceof DyeableItem item && item.hasColor(stack)) {
                String colorHex = String.format("%06X", item.getColor(stack));
                String expectedHex = ExoticTooltip.getExpectedHex(internalID);

                boolean correctLine = false;
                for (Text text : lines) {
                    String existingTooltip = text.getString() + " ";
                    if (existingTooltip.startsWith("Color: ")) {
                        correctLine = true;

                        addExoticTooltip(lines, internalID, stack.getNbt(), colorHex, expectedHex, existingTooltip);
                        break;
                    }
                }

                if (!correctLine) {
                    addExoticTooltip(lines, internalID, stack.getNbt(), colorHex, expectedHex, "");
                }
            }
        }

        if (TooltipInfoType.ACCESSORIES.isTooltipEnabledAndHasOrNullWarning(internalID)) {
            Pair<AccessoryReport, String> report = AccessoriesHelper.calculateReport4Accessory(internalID);

            if (report.left() != AccessoryReport.INELIGIBLE) {
                MutableText title = Text.literal(String.format("%-19s", "Accessory: ")).withColor(0xf57542);

                Text stateText = switch (report.left()) {
                    case HAS_HIGHEST_TIER -> Text.literal("✔ Collected").formatted(Formatting.GREEN);
                    case IS_GREATER_TIER -> Text.literal("✦ Upgrade ").withColor(0x218bff).append(Text.literal(report.right()).withColor(0xf8f8ff));
                    case HAS_GREATER_TIER -> Text.literal("↑ Upgradable ").withColor(0xf8d048).append(Text.literal(report.right()).withColor(0xf8f8ff));
                    case OWNS_BETTER_TIER -> Text.literal("↓ Downgrade ").formatted(Formatting.GRAY).append(Text.literal(report.right()).withColor(0xf8f8ff));
                    case MISSING -> Text.literal("✖ Missing").formatted(Formatting.RED);

                    //Should never be the case
                    default -> Text.literal("? Unknown").formatted(Formatting.GRAY);
                };

                lines.add(title.append(stateText));
            }
        }
    }

    @NotNull
    public static String getNeuName(String internalID, String neuName) {
        switch (internalID) {
            case "PET" -> {
                neuName = neuName.replaceAll("LVL_\\d*_", "");
                String[] parts = neuName.split("_");
                String type = parts[0];
                neuName = neuName.replaceAll(type + "_", "");
                neuName = neuName + "-" + type;
                neuName = neuName.replace("UNCOMMON", "1")
                        .replace("COMMON", "0")
                        .replace("RARE", "2")
                        .replace("EPIC", "3")
                        .replace("LEGENDARY", "4")
                        .replace("MYTHIC", "5")
                        .replace("-", ";");
            }
            case "RUNE" -> neuName = neuName.replaceAll("_(?!.*_)", ";");
            case "POTION" -> neuName = "";
            case "ATTRIBUTE_SHARD" ->
                    neuName = internalID + "+" + neuName.replace("SHARD-", "").replaceAll("_(?!.*_)", ";");
            default -> neuName = neuName.replace(":", "-");
        }
        return neuName;
    }

    private static void addExoticTooltip(List<Text> lines, String internalID, NbtCompound nbt, String colorHex, String expectedHex, String existingTooltip) {
        if (expectedHex != null && !colorHex.equalsIgnoreCase(expectedHex) && !ExoticTooltip.isException(internalID, colorHex) && !ExoticTooltip.intendedDyed(nbt)) {
            final ExoticTooltip.DyeType type = ExoticTooltip.checkDyeType(colorHex);
            lines.add(1, Text.literal(existingTooltip + Formatting.DARK_GRAY + "(").append(type.getTranslatedText()).append(Formatting.DARK_GRAY + ")"));
        }
    }

    public static void nullWarning() {
        if (!sentNullWarning && client.player != null) {
            LOGGER.warn(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemTooltip.nullMessage")).getString());
            sentNullWarning = true;
        }
    }

    // TODO What in the world is this?
    public static String getInternalNameFromNBT(ItemStack stack, boolean internalIDOnly) {
        NbtCompound ea = ItemUtils.getExtraAttributes(stack);

        if (ea == null || !ea.contains(ItemUtils.ID, NbtElement.STRING_TYPE)) {
            return null;
        }
        String internalName = ea.getString(ItemUtils.ID);

        if (internalIDOnly) {
            return internalName;
        }

        // Transformation to API format.
        if (ea.contains("is_shiny")) {
            return "ISSHINY_" + internalName;
        }

        switch (internalName) {
            case "ENCHANTED_BOOK" -> {
                if (ea.contains("enchantments")) {
                    NbtCompound enchants = ea.getCompound("enchantments");
                    Optional<String> firstEnchant = enchants.getKeys().stream().findFirst();
                    String enchant = firstEnchant.orElse("");
                    return "ENCHANTMENT_" + enchant.toUpperCase(Locale.ENGLISH) + "_" + enchants.getInt(enchant);
                }
            }
            case "PET" -> {
                if (ea.contains("petInfo")) {
                    JsonObject petInfo = SkyblockerMod.GSON.fromJson(ea.getString("petInfo"), JsonObject.class);
                    return "LVL_1_" + petInfo.get("tier").getAsString() + "_" + petInfo.get("type").getAsString();
                }
            }
            case "POTION" -> {
                String enhanced = ea.contains("enhanced") ? "_ENHANCED" : "";
                String extended = ea.contains("extended") ? "_EXTENDED" : "";
                String splash = ea.contains("splash") ? "_SPLASH" : "";
                if (ea.contains("potion") && ea.contains("potion_level")) {
                    return (ea.getString("potion") + "_" + internalName + "_" + ea.getInt("potion_level")
                            + enhanced + extended + splash).toUpperCase(Locale.ENGLISH);
                }
            }
            case "RUNE" -> {
                if (ea.contains("runes")) {
                    NbtCompound runes = ea.getCompound("runes");
                    Optional<String> firstRunes = runes.getKeys().stream().findFirst();
                    String rune = firstRunes.orElse("");
                    return rune.toUpperCase(Locale.ENGLISH) + "_RUNE_" + runes.getInt(rune);
                }
            }
            case "ATTRIBUTE_SHARD" -> {
                if (ea.contains("attributes")) {
                    NbtCompound shards = ea.getCompound("attributes");
                    Optional<String> firstShards = shards.getKeys().stream().findFirst();
                    String shard = firstShards.orElse("");
                    return internalName + "-" + shard.toUpperCase(Locale.ENGLISH) + "_" + shards.getInt(shard);
                }
            }
        }
        return internalName;
    }

    private static Text getCoinsMessage(double price, int count) {
        // Format the price string once
        String priceString = String.format(Locale.ENGLISH, "%1$,.1f", price);

        // If count is 1, return a simple message
        if (count == 1) {
            return Text.literal(priceString + " Coins").formatted(Formatting.DARK_AQUA);
        }

        // If count is greater than 1, include the "each" information
        String priceStringTotal = String.format(Locale.ENGLISH, "%1$,.1f", price * count);
        MutableText message = Text.literal(priceStringTotal + " Coins ").formatted(Formatting.DARK_AQUA);
        message.append(Text.literal("(" + priceString + " each)").formatted(Formatting.GRAY));

        return message;
    }

    private static Text getMotesMessage(int price, int count) {
        float motesMultiplier = SkyblockerConfigManager.get().locations.rift.mcGrubberStacks * 0.05f + 1;

        // Calculate the total price
        int totalPrice = price * count;
        String totalPriceString = String.format(Locale.ENGLISH, "%1$,.1f", totalPrice * motesMultiplier);

        // If count is 1, return a simple message
        if (count == 1) {
            return Text.literal(totalPriceString.replace(".0", "") + " Motes").formatted(Formatting.DARK_AQUA);
        }

        // If count is greater than 1, include the "each" information
        String eachPriceString = String.format(Locale.ENGLISH, "%1$,.1f", price * motesMultiplier);
        MutableText message = Text.literal(totalPriceString.replace(".0", "") + " Motes ").formatted(Formatting.DARK_AQUA);
        message.append(Text.literal("(" + eachPriceString.replace(".0", "") + " each)").formatted(Formatting.GRAY));

        return message;
    }

    // If these options is true beforehand, the client will get first data of these options while loading.
    // After then, it will only fetch the data if it is on Skyblock.
    public static int minute = 0;

    public static void init() {
        Scheduler.INSTANCE.scheduleCyclic(() -> {
            if (!Utils.isOnSkyblock() && 0 < minute) {
                sentNullWarning = false;
                return;
            }

            if (++minute % 60 == 0) {
                sentNullWarning = false;
            }

            List<CompletableFuture<Void>> futureList = new ArrayList<>();

            TooltipInfoType.NPC.downloadIfEnabled(futureList);
            TooltipInfoType.BAZAAR.downloadIfEnabled(futureList);
            TooltipInfoType.LOWEST_BINS.downloadIfEnabled(futureList);

            if (config.enableAvgBIN) {
                SkyblockerConfig.Average type = config.avg;

                if (type == SkyblockerConfig.Average.BOTH || TooltipInfoType.ONE_DAY_AVERAGE.getData() == null || TooltipInfoType.THREE_DAY_AVERAGE.getData() == null || minute % 5 == 0) {
                    TooltipInfoType.ONE_DAY_AVERAGE.download(futureList);
                    TooltipInfoType.THREE_DAY_AVERAGE.download(futureList);
                } else if (type == SkyblockerConfig.Average.ONE_DAY) {
                    TooltipInfoType.ONE_DAY_AVERAGE.download(futureList);
                } else if (type == SkyblockerConfig.Average.THREE_DAY) {
                    TooltipInfoType.THREE_DAY_AVERAGE.download(futureList);
                }
            }

            TooltipInfoType.MOTES.downloadIfEnabled(futureList);
            TooltipInfoType.MUSEUM.downloadIfEnabled(futureList);
            TooltipInfoType.COLOR.downloadIfEnabled(futureList);
            TooltipInfoType.ACCESSORIES.downloadIfEnabled(futureList);

            CompletableFuture.allOf(futureList.toArray(CompletableFuture[]::new)).exceptionally(e -> {
                LOGGER.error("Encountered unknown error while downloading tooltip data", e);
                return null;
            });
        }, 1200, true);
    }
}