package me.xmrvizzy.skyblocker.skyblock.item;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Http;
import me.xmrvizzy.skyblocker.skyblock.item.exotic.CheckExotic;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.skyblock.item.exotic.CheckExotic;
import me.xmrvizzy.skyblocker.utils.scheduler.Scheduler;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PriceInfoTooltip {
    private static final Logger LOGGER = LoggerFactory.getLogger(PriceInfoTooltip.class.getName());
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static JsonObject npcPricesJson;
    private static JsonObject bazaarPricesJson;
    private static JsonObject oneDayAvgPricesJson;
    private static JsonObject threeDayAvgPricesJson;
    private static JsonObject lowestPricesJson;
    private static JsonObject isMuseumJson;
    private static JsonObject motesPricesJson;
    private static boolean nullMsgSend = false;
    private final static Gson gson = new Gson();
    private static final Map<String, String> apiAddresses;
    private static long npcHash = 0;
    private static long museumHash = 0;
    private static long motesHash = 0;

    public static void onInjectTooltip(ItemStack stack, TooltipContext context, List<Text> lines) {
        if (!Utils.isOnSkyblock() || client.player == null) return;

        String name = getInternalNameFromNBT(stack, false);
        String internalID = getInternalNameFromNBT(stack, true);
        String neuName = name;
        if (name == null || internalID == null) return;

        if(name.startsWith("ISSHINY_")){
            name = "SHINY_" + internalID;
            neuName = internalID;
        }

        if (lines.size() == 0) {
            return;
        }

        if (SkyblockerConfig.get().general.itemTooltip.enableExoticCheck) {
            final NbtElement Color = stack.getNbt().getCompound("display").get("color");

            if (Color != null) {
                String colorHex = String.format("%06X", Integer.parseInt(Color.asString()));
                String expectedHex = CheckExotic.getExpectedHex(internalID);

                boolean correctLine = false;
                for (int i = 0; i < lines.size(); i++) {
                    String existingTooltip = String.valueOf(lines.get(i));
                    if (existingTooltip.startsWith("Color: ")) {
                        correctLine = true;

                        if (!colorHex.equalsIgnoreCase(expectedHex)  && !CheckExotic.checkExceptions(internalID, colorHex)) {
                            final String type = CheckExotic.checkDyeType(colorHex);
                            lines.add(1, Text.literal(existingTooltip + Formatting.DARK_GRAY + " (" + CheckExotic.FormattingColor(type) + CheckExotic.getTranslatatedText(type).getString() + Formatting.DARK_GRAY  + ")"));
                        }
                        break;
                    }
                }

                if (!correctLine) {
                    if (!colorHex.equalsIgnoreCase(expectedHex) && !CheckExotic.checkExceptions(internalID, colorHex)) {
                        final String type = CheckExotic.checkDyeType(colorHex);
                        lines.add(1, Text.literal(Formatting.DARK_GRAY + "(" + CheckExotic.FormattingColor(type) + CheckExotic.getTranslatatedText(type).getString() + Formatting.DARK_GRAY + ")"));
                    }
                }
            }
        }

        int count = stack.getCount();
        boolean bazaarOpened = lines.stream().anyMatch(each -> each.getString().contains("Buy price:") || each.getString().contains("Sell price:"));

        if (SkyblockerConfig.get().general.itemTooltip.enableNPCPrice) {
            if (npcPricesJson == null) {
                nullWarning();
            } else if (npcPricesJson.has(internalID)) {
                lines.add(Text.literal(String.format("%-21s", "NPC Price:"))
                        .formatted(Formatting.YELLOW)
                        .append(getCoinsMessage(npcPricesJson.get(internalID).getAsDouble(), count)));
            }
        }

        if (SkyblockerConfig.get().general.itemTooltip.enableMotesPrice && Utils.isInTheRift()) {
            if (motesPricesJson == null) {
                nullWarning();
            } else if (motesPricesJson.has(internalID)) {
                lines.add(Text.literal(String.format("%-20s", "Motes Price:"))
                        .formatted(Formatting.LIGHT_PURPLE)
                        .append(getMotesMessage(motesPricesJson.get(internalID).getAsInt(), count)));
            }
        }

        boolean bazaarExist = false;

        if (SkyblockerConfig.get().general.itemTooltip.enableBazaarPrice && !bazaarOpened) {
            if (bazaarPricesJson == null) {
                nullWarning();
            } else if (bazaarPricesJson.has(name)) {
                JsonObject getItem = bazaarPricesJson.getAsJsonObject(name);
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
        if (SkyblockerConfig.get().general.itemTooltip.enableLowestBIN && !bazaarOpened && !bazaarExist) {
            if (lowestPricesJson == null) {
                nullWarning();
            } else if (lowestPricesJson.has(name)) {
                lines.add(Text.literal(String.format("%-19s", "Lowest BIN Price:"))
                        .formatted(Formatting.GOLD)
                        .append(getCoinsMessage(lowestPricesJson.get(name).getAsDouble(), count)));
                lbinExist = true;
            }
        }

        if (SkyblockerConfig.get().general.itemTooltip.enableAvgBIN) {
            if (threeDayAvgPricesJson == null || oneDayAvgPricesJson == null) {
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
                    SkyblockerConfig.Average type = SkyblockerConfig.get().general.itemTooltip.avg;

                    // "No data" line because of API not keeping old data, it causes NullPointerException
                    if (type == SkyblockerConfig.Average.ONE_DAY || type == SkyblockerConfig.Average.BOTH) {
                        lines.add(
                                Text.literal(String.format("%-19s", "1 Day Avg. Price:"))
                                        .formatted(Formatting.GOLD)
                                        .append(oneDayAvgPricesJson.get(neuName) == null
                                                ? Text.literal("No data").formatted(Formatting.RED)
                                                : getCoinsMessage(oneDayAvgPricesJson.get(neuName).getAsDouble(), count)
                                        )
                        );
                    }
                    if (type == SkyblockerConfig.Average.THREE_DAY || type == SkyblockerConfig.Average.BOTH) {
                        lines.add(
                                Text.literal(String.format("%-19s", "3 Day Avg. Price:"))
                                        .formatted(Formatting.GOLD)
                                        .append(threeDayAvgPricesJson.get(neuName) == null
                                                ? Text.literal("No data").formatted(Formatting.RED)
                                                : getCoinsMessage(threeDayAvgPricesJson.get(neuName).getAsDouble(), count)
                                        )
                        );
                    }
                }
            }
        }

        if (SkyblockerConfig.get().general.itemTooltip.enableMuseumDate && !bazaarOpened) {
            if (isMuseumJson == null) {
                nullWarning();
            } else {
                String timestamp = getTimestamp(stack);

                if (isMuseumJson.has(internalID)) {
                    String itemCategory = isMuseumJson.get(internalID).getAsString();
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
        }
    }

    private static void nullWarning() {
        if (!nullMsgSend && client.player != null) {
            client.player.sendMessage(Text.translatable("skyblocker.itemTooltip.nullMessage"), false);
            nullMsgSend = true;
        }
    }

    public static NbtCompound getItemNBT(ItemStack stack) {
        if (stack == null) return null;
        return stack.getNbt();
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
        NbtCompound tag = getItemNBT(stack);

        if (tag != null && tag.contains("ExtraAttributes", 10)) {
            NbtCompound ea = tag.getCompound("ExtraAttributes");

            if (ea.contains("timestamp", 8)) {
                SimpleDateFormat nbtFormat = new SimpleDateFormat("MM/dd/yy");

                try {
                    Date date = nbtFormat.parse(ea.getString("timestamp"));
                    SimpleDateFormat skyblockerFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
                    return skyblockerFormat.format(date);
                } catch (ParseException e) {
                    LOGGER.warn("[Skyblocker-tooltip] getTimestamp", e);
                }
            }
        }

        return "";
    }

    public static String getInternalNameFromNBT(ItemStack stack, boolean internalIDOnly) {
        NbtCompound tag = getItemNBT(stack);
        if (tag == null || !tag.contains("ExtraAttributes", 10)) {
            return null;
        }
        NbtCompound ea = tag.getCompound("ExtraAttributes");

        if (!ea.contains("id", 8)) {
            return null;
        }
        String internalName = ea.getString("id");

        if (internalIDOnly) {
            return internalName;
        }

        // Transformation to API format.
        if (ea.contains("is_shiny")){
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
                    JsonObject petInfo = gson.fromJson(ea.getString("petInfo"), JsonObject.class);
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
        float motesMultiplier = SkyblockerConfig.get().locations.rift.mcGrubberStacks * 0.05f + 1;

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
                nullMsgSend = false;
                return;
            }

            List<CompletableFuture<Void>> futureList = new ArrayList<>();
            if (SkyblockerConfig.get().general.itemTooltip.enableAvgBIN) {
                SkyblockerConfig.Average type = SkyblockerConfig.get().general.itemTooltip.avg;

                if (type == SkyblockerConfig.Average.BOTH || oneDayAvgPricesJson == null || threeDayAvgPricesJson == null || minute % 5 == 0) {
                    futureList.add(CompletableFuture.runAsync(() -> {
                        oneDayAvgPricesJson = downloadPrices("1 day avg");
                        threeDayAvgPricesJson = downloadPrices("3 day avg");
                    }));
                } else if (type == SkyblockerConfig.Average.ONE_DAY) {
                    futureList.add(CompletableFuture.runAsync(() -> oneDayAvgPricesJson = downloadPrices("1 day avg")));
                } else if (type == SkyblockerConfig.Average.THREE_DAY) {
                    futureList.add(CompletableFuture.runAsync(() -> threeDayAvgPricesJson = downloadPrices("3 day avg")));
                }
            }
            if (SkyblockerConfig.get().general.itemTooltip.enableLowestBIN || SkyblockerConfig.get().locations.dungeons.dungeonChestProfit.enableProfitCalculator)
                futureList.add(CompletableFuture.runAsync(() -> lowestPricesJson = downloadPrices("lowest bins")));

            if (SkyblockerConfig.get().general.itemTooltip.enableBazaarPrice || SkyblockerConfig.get().locations.dungeons.dungeonChestProfit.enableProfitCalculator)
                futureList.add(CompletableFuture.runAsync(() -> bazaarPricesJson = downloadPrices("bazaar")));

            if (SkyblockerConfig.get().general.itemTooltip.enableNPCPrice && npcPricesJson == null)
                futureList.add(CompletableFuture.runAsync(() -> npcPricesJson = downloadPrices("npc")));

            if (SkyblockerConfig.get().general.itemTooltip.enableMuseumDate && isMuseumJson == null)
                futureList.add(CompletableFuture.runAsync(() -> isMuseumJson = downloadPrices("museum")));

            if (SkyblockerConfig.get().general.itemTooltip.enableMotesPrice && motesPricesJson == null)
                futureList.add(CompletableFuture.runAsync(() -> motesPricesJson = downloadPrices("motes")));

            minute++;
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
                    .whenComplete((unused, throwable) -> nullMsgSend = false);
        }, 1200);
    }

    private static JsonObject downloadPrices(String type) {
        try {
            String url = apiAddresses.get(type);
            
            if (type.equals("npc") || type.equals("museum") || type.equals("motes")) {
                HttpHeaders headers = Http.sendHeadRequest(url);
                long combinedHash = Http.getEtag(headers).hashCode() + Http.getLastModified(headers).hashCode();
            	
                switch (type) {
                    case "npc": if (npcHash == combinedHash) return npcPricesJson; else npcHash = combinedHash;
                    case "museum": if (museumHash == combinedHash) return isMuseumJson; else museumHash = combinedHash;
                    case "motes": if (motesHash == combinedHash) return motesPricesJson; else motesHash = combinedHash;
                }
            }
            
            String apiResponse = Http.sendGetRequest(url);
            
            return new Gson().fromJson(apiResponse, JsonObject.class);
        } catch (Exception e) {
            LOGGER.warn("[Skyblocker] Failed to download " + type + " prices!", e);
            return null;
        }
    }
    
    public static JsonObject getBazaarPrices() {
    	return bazaarPricesJson;
    }
    
    public static JsonObject getLBINPrices() {
    	return lowestPricesJson;
    }

    static {
        apiAddresses = new HashMap<>();
        apiAddresses.put("1 day avg", "https://moulberry.codes/auction_averages_lbin/1day.json");
        apiAddresses.put("3 day avg", "https://moulberry.codes/auction_averages_lbin/3day.json");
        apiAddresses.put("bazaar", "https://hysky.de/api/bazaar");
        apiAddresses.put("lowest bins", "https://hysky.de/api/auctions/lowestbins");
        apiAddresses.put("npc", "https://hysky.de/api/npcprice");
        apiAddresses.put("museum", "https://hysky.de/api/museum");
        apiAddresses.put("motes", "https://hysky.de/api/motesprice");
    }
}