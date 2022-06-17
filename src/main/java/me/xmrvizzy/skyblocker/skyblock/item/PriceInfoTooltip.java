package me.xmrvizzy.skyblocker.skyblock.item;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;

public class PriceInfoTooltip {
    private static final Logger LOGGER = LoggerFactory.getLogger(PriceInfoTooltip.class.getName());
    private static final SkyblockerMod skyblocker = SkyblockerMod.getInstance();
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static JsonObject npcPricesJson;
    private static JsonObject bazaarPricesJson;
    private static JsonObject oneDayAvgPricesJson;
    private static JsonObject threeDayAvgPricesJson;
    private static JsonObject lowestPricesJson;
    private static JsonObject isMuseumJson;
    private static boolean nullMsgSend = false;
    private final static Gson gson = new Gson();

    public static void onInjectTooltip(ItemStack stack, TooltipContext context, List<Text> lines) {
        if (!Utils.isOnSkyblock || client.player == null) return;

        String name = getInternalNameFromNBT(stack);
        if (name == null) return;

        int count = stack.getCount();
        String timestamp = getTimestamp(stack);
        boolean bazaarOpened = lines.stream().anyMatch(each -> each.getString().contains("Buy price:") || each.getString().contains("Sell price:"));

        if (SkyblockerConfig.get().general.itemTooltip.enableNPCPrice) {
            if (npcPricesJson == null) {
                if (!nullMsgSend) {
                    client.player.sendMessage(new TranslatableText("skyblocker.itemTooltip.nullMessage"), false);
                    nullMsgSend = true;
                }
            } else if (npcPricesJson.has(name)) {
                lines.add(new LiteralText(String.format("%-21s", "NPC Price:"))
                        .formatted(Formatting.YELLOW)
                        .append(getCoinsMessage(npcPricesJson.get(name).getAsDouble(), count)));
            }
        }

        boolean bazaarExist = false;
        if (SkyblockerConfig.get().general.itemTooltip.enableBazaarPrice && !bazaarOpened) {
            if (bazaarPricesJson == null) {
                if (!nullMsgSend) {
                    client.player.sendMessage(new TranslatableText("skyblocker.itemTooltip.nullMessage"), false);
                    nullMsgSend = true;
                }
            } else if (bazaarPricesJson.has(name)) {
                JsonObject getItem = bazaarPricesJson.getAsJsonObject(name);
                lines.add(new LiteralText(String.format("%-18s", "Bazaar buy Price:"))
                        .formatted(Formatting.GOLD)
                        .append(getItem.get("buyPrice").isJsonNull()
                                ? new LiteralText("No data").formatted(Formatting.RED)
                                : getCoinsMessage(getItem.get("buyPrice").getAsDouble(), count)));
                lines.add(new LiteralText(String.format("%-19s", "Bazaar sell Price:"))
                        .formatted(Formatting.GOLD)
                        .append(getItem.get("sellPrice").isJsonNull()
                                ? new LiteralText("No data").formatted(Formatting.RED)
                                : getCoinsMessage(getItem.get("sellPrice").getAsDouble(), count)));
                bazaarExist = true;
            }
        }

        // bazaarOpened & bazaarExist check for lbin, because Skytils keeps some bazaar item data in lbin api
        if (SkyblockerConfig.get().general.itemTooltip.enableLowestBIN && !bazaarOpened && !bazaarExist) {
            if (lowestPricesJson == null) {
                if (!nullMsgSend) {
                    client.player.sendMessage(new TranslatableText("skyblocker.itemTooltip.nullMessage"), false);
                    nullMsgSend = true;
                }
            } else if (lowestPricesJson.has(name)) {
                lines.add(new LiteralText(String.format("%-19s", "Lowest BIN Price:"))
                        .formatted(Formatting.GOLD)
                        .append(getCoinsMessage(lowestPricesJson.get(name).getAsDouble(), count)));
            }
        }

        if (SkyblockerConfig.get().general.itemTooltip.enableAvgBIN) {
            if (threeDayAvgPricesJson == null || oneDayAvgPricesJson == null) {
                if (!nullMsgSend) {
                    client.player.sendMessage(new TranslatableText("skyblocker.itemTooltip.nullMessage"), false);
                    nullMsgSend = true;
                }
            } else if (threeDayAvgPricesJson.has(name) || oneDayAvgPricesJson.has(name)) {
                /*
                  We are skipping check average prices for potions and runes
                  because there is no data for their in API.
                 */
                if (name.contains("PET-")) {
                    name = name.replace("PET-", "")
                            .replace("COMMON", "0")
                            .replace("UNCOMMON", "1")
                            .replace("RARE", "2")
                            .replace("EPIC", "3")
                            .replace("LEGENDARY", "4")
                            .replace("MYTHIC", "5")
                            .replace("-", ";");
                } else if (name.contains("ENCHANTED_BOOK-")) {
                    name = name.replace("ENCHANTED_BOOK-", "").replace("-", ";");
                } else if (name.contains("POTION-")) {
                    name = "";
                } else if (name.contains("RUNE-")) {
                    name = "";
                } else {
                    name = name.replace(":", "-");
                }

                SkyblockerConfig.Average type = SkyblockerConfig.get().general.itemTooltip.avg;

                // "No data" line because of API not keeping old data, it causes NullPointerException
                if (!name.isEmpty() && (type == SkyblockerConfig.Average.ONE_DAY || type == SkyblockerConfig.Average.BOTH)) {
                    lines.add(new LiteralText(String.format("%-19s", "1 Day Avg. Price:"))
                            .formatted(Formatting.GOLD)
                            .append(oneDayAvgPricesJson.get(name) == null
                                    ? new LiteralText("No data").formatted(Formatting.RED)
                                    : getCoinsMessage(oneDayAvgPricesJson.get(name).getAsDouble(), count)));
                }
                if (!name.isEmpty() && (type == SkyblockerConfig.Average.THREE_DAY || type == SkyblockerConfig.Average.BOTH)) {
                    lines.add(new LiteralText(String.format("%-19s", "3 Day Avg. Price:"))
                            .formatted(Formatting.GOLD)
                            .append(threeDayAvgPricesJson.get(name) == null
                                    ? new LiteralText("No data").formatted(Formatting.RED)
                                    : getCoinsMessage(threeDayAvgPricesJson.get(name).getAsDouble(), count)));
                }
            }
        }

        if (SkyblockerConfig.get().general.itemTooltip.enableMuseumDate && !bazaarOpened) {
            if (isMuseumJson == null) {
                if (!nullMsgSend) {
                    client.player.sendMessage(new TranslatableText("skyblocker.itemTooltip.nullMessage"), false);
                    nullMsgSend = true;
                }
            } else if (isMuseumJson.has(name)) {
                String itemCategory = isMuseumJson.get(name).toString().replaceAll("\"", "");
                String format = switch (itemCategory) {
                    case "Weapons" -> "%-18s";
                    case "Armor" -> "%-19s";
                    default -> "%-20s";
                };
                lines.add(new LiteralText(String.format(format, "Museum: (" + itemCategory + ")"))
                        .formatted(Formatting.LIGHT_PURPLE)
                        .append(new LiteralText(timestamp != null ? timestamp : "").formatted(Formatting.RED)));
            } else if (timestamp != null) {
                lines.add(new LiteralText(String.format("%-21s", "Obtained: "))
                        .formatted(Formatting.LIGHT_PURPLE)
                        .append(new LiteralText(timestamp).formatted(Formatting.RED)));
            }
        }
    }

    public static NbtCompound getInternalNameForItem(ItemStack stack) {
        if (stack == null) return null;
        return stack.getNbt();
    }

    /**
     * this method converts the "timestamp" variable into the same date format as Hypixel represents it in the museum.
     * Currently there are two types of timestamps the legacy which is built like this
     * "dd/MM/yy hh:mm" ("25/04/20 16:38") and the current which is built like this
     * "MM/dd/yy hh:mm aa" ("12/24/20 11:08 PM"). Since Hypixel transforms the two formats into one format without
     * taking into account of their formats, we do the same. The final result looks like this
     * "MMMM dd, yyyy" (December 24, 2020).
     * Since the legacy format has a 25 as "month" SimpleDateFormat converts the 25 into 2 years and 1 month and makes
     * "25/04/20 16:38" -> "January 04, 2022" instead of "April 25, 2020".
     * This causes the museum rank to be much worse than it should be.
     *
     * @param stack the item under the pointer
     * @return if the item have an "Timestamp" it will be shown formated on the tooltip
     */
    public static String getTimestamp(ItemStack stack) {
        NbtCompound tag = getInternalNameForItem(stack);
        String internalName = null;
        if (tag != null && tag.contains("ExtraAttributes", 10)) {
            NbtCompound ea = tag.getCompound("ExtraAttributes");

            if (ea.contains("timestamp", 8)) {
                internalName = ea.getString("timestamp");
                SimpleDateFormat dt = new SimpleDateFormat("MM/dd/yy");

                try {
                    Date date = dt.parse(internalName);
                    SimpleDateFormat dt1 = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
                    internalName = dt1.format(date);
                } catch (ParseException e) {
                    LOGGER.warn("[Skyblocker-tooltip] getTimestamp", e);
                }
            }
        }
        return internalName;
    }

    public static String getInternalNameFromNBT(ItemStack stack) {
        NbtCompound tag = getInternalNameForItem(stack);
        String internalName = null;
        if (tag != null && tag.contains("ExtraAttributes", 10)) {
            NbtCompound ea = tag.getCompound("ExtraAttributes");

            if (ea.contains("id", 8)) {
                internalName = ea.getString("id");
            } else {
                return null;
            }

            if ("ENCHANTED_BOOK".equals(internalName)) {
                if (ea.contains("enchantments")) {
                    NbtCompound enchants = ea.getCompound("enchantments");
                    String enchant = enchants.getKeys().stream().findFirst().get();
                    internalName += "-" + enchant.toUpperCase(Locale.ENGLISH) + "-" + enchants.getInt(enchant);
                }
            } else if ("PET".equals(internalName)) {
                if (ea.contains("petInfo")) {
                    JsonObject petInfo = gson.fromJson(ea.getString("petInfo"), JsonObject.class);
                    internalName += "-" + petInfo.get("type").getAsString() + "-" + petInfo.get("tier").getAsString();
                }
            } else if ("POTION".equals(internalName)) {
                String enhanced = ea.contains("enhanced") ? "-ENHANCED" : "";
                String extended = ea.contains("extended") ? "-EXTENDED" : "";
                String splash = ea.contains("splash") ? "-SPLASH" : "";
                if (ea.contains("potion") && ea.contains("potion_level")) {
                    internalName += "-" + ea.getString("potion").toUpperCase(Locale.ENGLISH) + "-" + ea.getInt("potion_level")
                            + enhanced + extended + splash;
                }
            } else if ("RUNE".equals(internalName)) {
                if (ea.contains("runes")) {
                    NbtCompound runes = ea.getCompound("runes");
                    String rune = runes.getKeys().stream().findFirst().get();
                    internalName += "-" + rune.toUpperCase(Locale.ENGLISH) + "-" + runes.getInt(rune);
                }
            }

        }
        return internalName;
    }

    private static Text getCoinsMessage(double price, int count) {
        if (count == 1) {
            String priceString = String.format(Locale.ENGLISH, "%1$,.1f", price);
            return new LiteralText(priceString + " Coins").formatted(Formatting.DARK_AQUA);
        } else {
            String priceString = String.format(Locale.ENGLISH, "%1$,.1f", price * count);
            LiteralText priceText = (LiteralText) new LiteralText(priceString + " Coins ").formatted(Formatting.DARK_AQUA);
            priceString = String.format(Locale.ENGLISH, "%1$,.1f", price);
            LiteralText priceText2 = (LiteralText)  new LiteralText( "(" + priceString + " each)").formatted(Formatting.GRAY);
            return priceText.append(priceText2);
        }
    }

    // If these options is true beforehand, the client will get first data of these options while loading.
    // After then, it will only fetch the data if it is on Skyblock.
    public static int minute = -1;
    public static void init() {
        skyblocker.scheduler.scheduleCyclic(() -> {
            if (!Utils.isOnSkyblock && 0 < minute++) {
                nullMsgSend = false;
                return;
            }

            List<CompletableFuture<Void>> futureList = new ArrayList<>();
            if ((SkyblockerConfig.get().general.itemTooltip.enableAvgBIN) && (oneDayAvgPricesJson == null || threeDayAvgPricesJson == null || minute % 5 == 0)) {
                SkyblockerConfig.Average type = SkyblockerConfig.get().general.itemTooltip.avg;

                if (type == SkyblockerConfig.Average.BOTH || oneDayAvgPricesJson == null || threeDayAvgPricesJson == null) {
                    futureList.add(CompletableFuture.runAsync(() -> downloadAvgPrices(SkyblockerConfig.Average.THREE_DAY)));
                    futureList.add(CompletableFuture.runAsync(() -> downloadAvgPrices(SkyblockerConfig.Average.ONE_DAY)));
                } else {
                    futureList.add(CompletableFuture.runAsync(() -> downloadAvgPrices(type)));
                }
            }
            if (SkyblockerConfig.get().general.itemTooltip.enableLowestBIN) {
                futureList.add(CompletableFuture.runAsync(PriceInfoTooltip::downloadLowestPrices));
            }
            if (SkyblockerConfig.get().general.itemTooltip.enableBazaarPrice) {
                futureList.add(CompletableFuture.runAsync(PriceInfoTooltip::downloadBazaarPrices));
            }
            if (SkyblockerConfig.get().general.itemTooltip.enableNPCPrice && npcPricesJson == null) {
                futureList.add(CompletableFuture.runAsync(PriceInfoTooltip::downloadNPCPrices));
            }
            if (SkyblockerConfig.get().general.itemTooltip.enableMuseumDate && isMuseumJson == null) {
                futureList.add(CompletableFuture.runAsync(PriceInfoTooltip::downloadIsMuseum));
            }
            minute++;
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
                    .whenComplete((unused, throwable) -> nullMsgSend = false);
        }, 1200);
    }

    private static void downloadAvgPrices(SkyblockerConfig.Average type) {
        JsonObject result = null;
        String avgDay = null;
        switch (type) {
            case ONE_DAY -> avgDay = "1day.json.gz";
            case THREE_DAY -> avgDay = "3day.json.gz";
        }
        try {
            URL apiAddr = new URL("https://moulberry.codes/auction_averages_lbin/" + avgDay);
            try (InputStream src = apiAddr.openStream()) {
                try (GZIPInputStream gzipOutput = new GZIPInputStream(src)) {
                    try (InputStreamReader reader = new InputStreamReader(gzipOutput)) {
                        result = new Gson().fromJson(reader, JsonObject.class);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.warn("[Skyblocker] Failed to download average BIN prices!", e);
        }
        switch (type) {
            case ONE_DAY -> oneDayAvgPricesJson = result;
            case THREE_DAY -> threeDayAvgPricesJson = result;
        }
    }

    private static void downloadBazaarPrices() {
        JsonObject result = null;
        try {
            URL apiAddr = new URL("https://hysky.de/api/bazaar");
            InputStreamReader reader = new InputStreamReader(apiAddr.openStream());
            result = new Gson().fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            LOGGER.warn("[Skyblocker] Failed to download bazaar prices!", e);
        }
        bazaarPricesJson = result;
    }

    private static void downloadLowestPrices() {
        JsonObject result = null;
        try {
            URL apiAddr = new URL("https://skytils.gg/api/auctions/lowestbins");
            InputStreamReader reader = new InputStreamReader(apiAddr.openStream());
            result = new Gson().fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            LOGGER.warn("[Skyblocker] Failed to download lowest BIN prices!", e);
        }
        lowestPricesJson = result;
    }

    private static void downloadNPCPrices() {
        JsonObject result = null;
        try {
            URL apiAddr = new URL("https://hysky.de/api/npcprice");
            InputStreamReader reader = new InputStreamReader(apiAddr.openStream());
            result = new Gson().fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            LOGGER.warn("[Skyblocker] Failed to download NPC prices!", e);
        }
        npcPricesJson = result;
    }

    private static void downloadIsMuseum() {
        JsonObject result = null;
        try {
            URL apiAddr = new URL("https://hysky.de/api/museum");
            InputStreamReader reader = new InputStreamReader(apiAddr.openStream());
            result = new Gson().fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            LOGGER.warn("[Skyblocker] Failed to download museum items!", e);
        }
        isMuseumJson = result;
    }

}
