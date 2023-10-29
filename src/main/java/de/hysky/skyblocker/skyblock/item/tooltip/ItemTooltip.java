package de.hysky.skyblocker.skyblock.item.tooltip;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpHeaders;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class ItemTooltip {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ItemTooltip.class.getName());
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final SkyblockerConfig.ItemTooltip config = SkyblockerConfigManager.get().general.itemTooltip;
    private static final Map<InfoType, String> API_ADDRESSES = Map.of(
            InfoType.NPC, "https://hysky.de/api/npcprice",
            InfoType.BAZAAR, "https://hysky.de/api/bazaar",
            InfoType.LOWEST_BINS, "https://hysky.de/api/auctions/lowestbins",
            InfoType.ONE_DAY_AVERAGE, "https://moulberry.codes/auction_averages_lbin/1day.json",
            InfoType.THREE_DAY_AVERAGE, "https://moulberry.codes/auction_averages_lbin/3day.json",
            InfoType.MOTES, "https://hysky.de/api/motesprice",
            InfoType.MUSEUM, "https://hysky.de/api/museum",
            InfoType.COLOR, "https://hysky.de/api/color"
    );
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

        if (InfoType.NPC.isEnabled(config)) {
            if (InfoType.NPC.hasOrNullWarning(internalID)) {
                lines.add(Text.literal(String.format("%-21s", "NPC Price:"))
                        .formatted(Formatting.YELLOW)
                        .append(getCoinsMessage(InfoType.NPC.data.get(internalID).getAsDouble(), count)));
            }
        }

        boolean bazaarExist = false;

        if (InfoType.BAZAAR.isEnabled(config) && !bazaarOpened) {
            if (InfoType.BAZAAR.hasOrNullWarning(name)) {
                JsonObject getItem = InfoType.BAZAAR.data.getAsJsonObject(name);
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
        }

        // bazaarOpened & bazaarExist check for lbin, because Skytils keeps some bazaar item data in lbin api
        boolean lbinExist = false;
        if (InfoType.LOWEST_BINS.isEnabled(config) && !bazaarOpened && !bazaarExist) {
            if (InfoType.LOWEST_BINS.hasOrNullWarning(name)) {
                lines.add(Text.literal(String.format("%-19s", "Lowest BIN Price:"))
                        .formatted(Formatting.GOLD)
                        .append(getCoinsMessage(InfoType.LOWEST_BINS.data.get(name).getAsDouble(), count)));
                lbinExist = true;
            }
        }

        if (SkyblockerConfigManager.get().general.itemTooltip.enableAvgBIN) {
            if (InfoType.ONE_DAY_AVERAGE.data == null || InfoType.THREE_DAY_AVERAGE.data == null) {
                nullWarning();
            } else {
                /*
                  We are skipping check average prices for potions, runes
                  and enchanted books because there is no data for their in API.
                 */
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

                if (!neuName.isEmpty() && lbinExist) {
                    SkyblockerConfig.Average type = config.avg;

                    // "No data" line because of API not keeping old data, it causes NullPointerException
                    if (type == SkyblockerConfig.Average.ONE_DAY || type == SkyblockerConfig.Average.BOTH) {
                        lines.add(
                                Text.literal(String.format("%-19s", "1 Day Avg. Price:"))
                                        .formatted(Formatting.GOLD)
                                        .append(InfoType.ONE_DAY_AVERAGE.data.get(neuName) == null
                                                ? Text.literal("No data").formatted(Formatting.RED)
                                                : getCoinsMessage(InfoType.ONE_DAY_AVERAGE.data.get(neuName).getAsDouble(), count)
                                        )
                        );
                    }
                    if (type == SkyblockerConfig.Average.THREE_DAY || type == SkyblockerConfig.Average.BOTH) {
                        lines.add(
                                Text.literal(String.format("%-19s", "3 Day Avg. Price:"))
                                        .formatted(Formatting.GOLD)
                                        .append(InfoType.THREE_DAY_AVERAGE.data.get(neuName) == null
                                                ? Text.literal("No data").formatted(Formatting.RED)
                                                : getCoinsMessage(InfoType.THREE_DAY_AVERAGE.data.get(neuName).getAsDouble(), count)
                                        )
                        );
                    }
                }
            }
        }

        if (InfoType.MOTES.isEnabled(config) && Utils.isInTheRift()) {
            if (InfoType.MOTES.hasOrNullWarning(internalID)) {
                lines.add(Text.literal(String.format("%-20s", "Motes Price:"))
                        .formatted(Formatting.LIGHT_PURPLE)
                        .append(getMotesMessage(InfoType.MOTES.data.get(internalID).getAsInt(), count)));
            }
        }

        if (InfoType.MUSEUM.isEnabled(config) && !bazaarOpened) {
            String timestamp = getTimestamp(stack);

            if (InfoType.MUSEUM.hasOrNullWarning(internalID)) {
                String itemCategory = InfoType.MUSEUM.data.get(internalID).getAsString();
                String format = switch (itemCategory) {
                    case "Weapons" -> "%-18s";
                    case "Armor" -> "%-19s";
                    default -> "%-20s";
                };
                lines.add(Text.literal(String.format(format, "Museum: (" + itemCategory + ")"))
                        .formatted(Formatting.LIGHT_PURPLE)
                        .append(Text.literal(timestamp).formatted(Formatting.RED)));
            } else if (!timestamp.isEmpty()) {
                lines.add(Text.literal(String.format("%-21s", "Obtained: "))
                        .formatted(Formatting.LIGHT_PURPLE)
                        .append(Text.literal(timestamp).formatted(Formatting.RED)));
            }
        }

        if (InfoType.COLOR.isEnabled(config)) {
            if (InfoType.COLOR.data == null) {
                nullWarning();
            } else if (stack.getNbt() != null) {
                final NbtElement color = stack.getNbt().getCompound("display").get("color");

                if (color != null) {
                    String colorHex = String.format("%06X", Integer.parseInt(color.asString()));
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
        }
    }

    private static void addExoticTooltip(List<Text> lines, String internalID, NbtCompound nbt, String colorHex, String expectedHex, String existingTooltip) {
        if (expectedHex != null && !colorHex.equalsIgnoreCase(expectedHex) && !ExoticTooltip.isException(internalID, colorHex) && !ExoticTooltip.intendedDyed(nbt)) {
            final ExoticTooltip.DyeType type = ExoticTooltip.checkDyeType(colorHex);
            lines.add(1, Text.literal(existingTooltip + Formatting.DARK_GRAY + "(").append(type.getTranslatedText()).append(Formatting.DARK_GRAY + ")"));
        }
    }

    private static void nullWarning() {
        if (!sentNullWarning && client.player != null) {
            client.player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemTooltip.nullMessage")), false);
            sentNullWarning = true;
        }
    }

    /**
     * this method converts the "timestamp" variable into the same date format as Hypixel represents it in the museum.
     * Currently, there are two types of timestamps the legacy which is built like this
     * "dd/MM/yy hh:mm" ("25/04/20 16:38") and the current which is built like this
     * "MM/dd/yy hh:mm aa" ("12/24/20 11:08 PM"). Since Hypixel transforms the two formats into one format without
     * taking into account of their formats, we do the same. The final result looks like this
     * "MMMM dd, yyyy" (December 24, 2020).
     * Since the legacy format has a 25 as "month" SimpleDateFormat converts the 25 into 2 years and 1 month and makes
     * "25/04/20 16:38" -> "January 04, 2022" instead of "April 25, 2020".
     * This causes the museum rank to be much worse than it should be.
     *
     * @param stack the item under the pointer
     * @return if the item have a "Timestamp" it will be shown formated on the tooltip
     */
    public static String getTimestamp(ItemStack stack) {
        NbtCompound ea = ItemUtils.getExtraAttributes(stack);

        if (ea != null && ea.contains("timestamp", 8)) {
            SimpleDateFormat nbtFormat = new SimpleDateFormat("MM/dd/yy");

            try {
                Date date = nbtFormat.parse(ea.getString("timestamp"));
                SimpleDateFormat skyblockerFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
                return skyblockerFormat.format(date);
            } catch (ParseException e) {
                LOGGER.warn("[Skyblocker-tooltip] getTimestamp", e);
            }
        }

        return "";
    }

    public static String getInternalNameFromNBT(ItemStack stack, boolean internalIDOnly) {
        NbtCompound ea = ItemUtils.getExtraAttributes(stack);

        if (ea == null || !ea.contains(ItemUtils.ID, 8)) {
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
    public static int minute = -1;

    public static void init() {
        Scheduler.INSTANCE.scheduleCyclic(() -> {
            if (!Utils.isOnSkyblock() && 0 < minute++) {
                sentNullWarning = false;
                return;
            }

            List<CompletableFuture<Void>> futureList = new ArrayList<>();

            if (InfoType.NPC.isEnabled(config) && InfoType.NPC.data == null)
                futureList.add(CompletableFuture.runAsync(() -> downloadPrices(InfoType.NPC)));

            if (InfoType.BAZAAR.isEnabled(config) || SkyblockerConfigManager.get().locations.dungeons.dungeonChestProfit.enableProfitCalculator)
                futureList.add(CompletableFuture.runAsync(() -> downloadPrices(InfoType.BAZAAR)));

            if (InfoType.LOWEST_BINS.isEnabled(config) || SkyblockerConfigManager.get().locations.dungeons.dungeonChestProfit.enableProfitCalculator)
                futureList.add(CompletableFuture.runAsync(() -> downloadPrices(InfoType.LOWEST_BINS)));

            if (config.enableAvgBIN) {
                SkyblockerConfig.Average type = config.avg;

                if (type == SkyblockerConfig.Average.BOTH || InfoType.ONE_DAY_AVERAGE.data == null || InfoType.THREE_DAY_AVERAGE.data == null || minute % 5 == 0) {
                    futureList.add(CompletableFuture.runAsync(() -> {
                        downloadPrices(InfoType.ONE_DAY_AVERAGE);
                        downloadPrices(InfoType.THREE_DAY_AVERAGE);
                    }));
                } else if (type == SkyblockerConfig.Average.ONE_DAY) {
                    futureList.add(CompletableFuture.runAsync(() -> downloadPrices(InfoType.ONE_DAY_AVERAGE)));
                } else if (type == SkyblockerConfig.Average.THREE_DAY) {
                    futureList.add(CompletableFuture.runAsync(() -> downloadPrices(InfoType.THREE_DAY_AVERAGE)));
                }
            }

            if (InfoType.MOTES.isEnabled(config) && InfoType.MOTES.data == null)
                futureList.add(CompletableFuture.runAsync(() -> downloadPrices(InfoType.MOTES)));

            if (InfoType.MUSEUM.isEnabled(config) && InfoType.MUSEUM.data == null)
                futureList.add(CompletableFuture.runAsync(() -> downloadPrices(InfoType.MUSEUM)));

            if (InfoType.COLOR.isEnabled(config) && InfoType.COLOR.data == null)
                futureList.add(CompletableFuture.runAsync(() -> downloadPrices(InfoType.COLOR)));

            minute++;
            CompletableFuture.allOf(futureList.toArray(CompletableFuture[]::new))
                    .whenComplete((unused, throwable) -> sentNullWarning = false);
        }, 1200, true);
    }

    private static void downloadPrices(InfoType type) {
        try {
            String url = API_ADDRESSES.get(type);

            if (type.cacheable) {
                HttpHeaders headers = Http.sendHeadRequest(url);
                long combinedHash = Http.getEtag(headers).hashCode() + Http.getLastModified(headers).hashCode();

                switch (type) {
                    case NPC, MOTES, MUSEUM, COLOR:
                        if (type.hash == combinedHash) return;
                        else type.hash = combinedHash;
                }
            }

            type.setData(SkyblockerMod.GSON.fromJson(Http.sendGetRequest(url), JsonObject.class));
        } catch (Exception e) {
            LOGGER.warn("[Skyblocker] Failed to download " + type + " prices!", e);
        }
    }

    public static JsonObject getBazaarPrices() {
        return InfoType.BAZAAR.data;
    }

    public static JsonObject getLBINPrices() {
        return InfoType.LOWEST_BINS.data;
    }

    public enum InfoType {
        NPC(itemTooltip -> itemTooltip.enableNPCPrice, true),
        BAZAAR(itemTooltip -> itemTooltip.enableBazaarPrice, false),
        LOWEST_BINS(itemTooltip -> itemTooltip.enableLowestBIN, false),
        ONE_DAY_AVERAGE(itemTooltip -> itemTooltip.enableAvgBIN, false),
        THREE_DAY_AVERAGE(itemTooltip -> itemTooltip.enableAvgBIN, false),
        MOTES(itemTooltip -> itemTooltip.enableMotesPrice, true),
        MUSEUM(itemTooltip -> itemTooltip.enableMuseumDate, true),
        COLOR(itemTooltip -> itemTooltip.enableExoticCheck, true);

        private final Predicate<SkyblockerConfig.ItemTooltip> enabledPredicate;
        private JsonObject data;
        private final boolean cacheable;
        private long hash;

        InfoType(Predicate<SkyblockerConfig.ItemTooltip> enabledPredicate, boolean cacheable) {
            this(enabledPredicate, null, false);
        }

        InfoType(Predicate<SkyblockerConfig.ItemTooltip> enabledPredicate, JsonObject data, boolean cacheable) {
            this.enabledPredicate = enabledPredicate;
            this.data = data;
            this.cacheable = cacheable;
        }

        public boolean isEnabled(SkyblockerConfig.ItemTooltip config) {
            return enabledPredicate.test(config);
        }

        public boolean hasOrNullWarning(String memberName) {
            if (data == null) {
                nullWarning();
                return false;
            } else return data.has(memberName);
        }

        public void setData(JsonObject data) {
            this.data = data;
        }
    }
}