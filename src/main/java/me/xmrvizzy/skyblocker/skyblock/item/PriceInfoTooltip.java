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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;

public class PriceInfoTooltip {
    private static final Logger LOGGER = LoggerFactory.getLogger(PriceInfoTooltip.class.getName());
    private static final SkyblockerMod skyblocker = SkyblockerMod.getInstance();
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static JsonObject npcJson;
    private static JsonObject bazaarJson;
    private static JsonObject oneDayAvgJson;
    private static JsonObject threeDayAvgJson;
    private static JsonObject lowestBINJson;
    private static JsonObject museumJson;
    private static boolean nullMsgSend = false;
    private final static Gson gson = new Gson();

    public static void onInjectTooltip(ItemStack stack, TooltipContext context, List<Text> lines) {
        if (!Utils.isOnSkyblock || client.player == null) return;

        String name = getInternalNameFromNBT(stack);
        if (name == null) return;

        int count = stack.getCount();
        String timestamp = getTimestamp(stack);
        boolean bazaarOpened = lines.stream().anyMatch(each ->
                each.getString().contains("Buy price:") || each.getString().contains("Sell price:"));

        if (SkyblockerConfig.get().general.itemTooltip.enableNPCPrice) {
            if (npcJson == null) {
                nullMessage();
            } else if (npcJson.has(name)) {
                lines.add(new LiteralText(String.format("%-21s", "NPC Price:"))
                        .formatted(Formatting.YELLOW)
                        .append(getCoinsMessage(npcJson.get(name).getAsDouble(), count)));
            }
        }

        boolean bazaarExist = false;
        if (SkyblockerConfig.get().general.itemTooltip.enableBazaarPrice && !bazaarOpened) {
            if (bazaarJson == null) {
                nullMessage();
            } else if (bazaarJson.has(name)) {
                JsonObject getItem = bazaarJson.getAsJsonObject(name);
                lines.add(new LiteralText(String.format("%-18s", "Bazaar buy Price:"))
                        .formatted(Formatting.GOLD)
                        .append(getItem.get("buyPrice").isJsonNull()
                                ? new TranslatableText("skyblocker.itemTooltip.noData")
                                : getCoinsMessage(getItem.get("buyPrice").getAsDouble(), count)));
                lines.add(new LiteralText(String.format("%-19s", "Bazaar sell Price:"))
                        .formatted(Formatting.GOLD)
                        .append(getItem.get("sellPrice").isJsonNull()
                                ? new TranslatableText("skyblocker.itemTooltip.noData")
                                : getCoinsMessage(getItem.get("sellPrice").getAsDouble(), count)));
                bazaarExist = true;
            }
        }

        // bazaarOpened & bazaarExist check for lbin, because Skytils keeps some bazaar item data in lbin api
        if (SkyblockerConfig.get().general.itemTooltip.enableLowestBIN && !bazaarOpened && !bazaarExist) {
            if (lowestBINJson == null) {
                nullMessage();
            } else if (lowestBINJson.has(name)) {
                lines.add(new LiteralText(String.format("%-19s", "Lowest BIN Price:"))
                        .formatted(Formatting.GOLD)
                        .append(getCoinsMessage(lowestBINJson.get(name).getAsDouble(), count)));
            }
        }

        if (SkyblockerConfig.get().general.itemTooltip.enableAvgBIN) {
            if (threeDayAvgJson == null || oneDayAvgJson == null) {
                nullMessage();
            } else if (threeDayAvgJson.has(name) || oneDayAvgJson.has(name)) {
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
                            .append(oneDayAvgJson.get(name) == null
                                    ? new TranslatableText("skyblocker.itemTooltip.noData")
                                    : getCoinsMessage(oneDayAvgJson.get(name).getAsDouble(), count)));
                }
                if (!name.isEmpty() && (type == SkyblockerConfig.Average.THREE_DAY || type == SkyblockerConfig.Average.BOTH)) {
                    lines.add(new LiteralText(String.format("%-19s", "3 Day Avg. Price:"))
                            .formatted(Formatting.GOLD)
                            .append(threeDayAvgJson.get(name) == null
                                    ? new TranslatableText("skyblocker.itemTooltip.noData")
                                    : getCoinsMessage(threeDayAvgJson.get(name).getAsDouble(), count)));
                }
            }
        }

        if (SkyblockerConfig.get().general.itemTooltip.enableMuseumDate && !bazaarOpened) {
            if (museumJson == null) {
                nullMessage();
            } else if (museumJson.has(name)) {
                String itemCategory = museumJson.get(name).toString().replaceAll("\"", "");
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

    public static void nullMessage() {
        if (!nullMsgSend) {
            client.player.sendMessage(new TranslatableText("skyblocker.itemTooltip.nullMessage"), false);
            nullMsgSend = true;
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
                    internalName += "-" + ea.getString("potion").toUpperCase(Locale.ENGLISH)
                            + "-" + ea.getInt("potion_level")
                            + enhanced + extended + splash;
                }
            } else if ("RUNE".equals(internalName)) {
                if (ea.contains("runes")) {
                    NbtCompound runes = ea.getCompound("runes");
                    String rune = ea.getCompound("runes").getKeys().stream().findFirst().get();
                    internalName += "-" + rune.toUpperCase(Locale.ENGLISH) + "-" + runes.getInt(rune);
                }
            }

        }
        return internalName;
    }

    private static Text getCoinsMessage(double price, int count) {
        String slotPrice = String.format(Locale.ENGLISH, "%1$,.1f", price * count);
        LiteralText slotText = (LiteralText) new LiteralText(slotPrice + " Coins ").formatted(Formatting.DARK_AQUA);

        if (count != 1) {
            String unitPrice = String.format(Locale.ENGLISH, "%1$,.1f", price);
            slotText.append(new LiteralText( "(" + unitPrice + " each)").formatted(Formatting.GRAY));
        }
        return slotText;
    }

    /**
     * If these options is true beforehand, the client will get first data of these options while loading.
     * After then, it will only fetch the data if it is on Skyblock.
     */
    private final static HashMap<String, String> downloadList;

    private static int minute = -1;

    public static void init() {
        skyblocker.scheduler.scheduleCyclic(() -> {
            if (!Utils.isOnSkyblock && 0 < minute++) {
                nullMsgSend = false;
                return;
            }

            List<CompletableFuture<Void>> futureList = new ArrayList<>();

            if (SkyblockerConfig.get().general.itemTooltip.enableAvgBIN
                    && (oneDayAvgJson == null || threeDayAvgJson == null || minute % 5 == 0)) {
                SkyblockerConfig.Average type = SkyblockerConfig.get().general.itemTooltip.avg;

                if (oneDayAvgJson == null || type == SkyblockerConfig.Average.ONE_DAY || type == SkyblockerConfig.Average.BOTH)
                    futureList.add(CompletableFuture.runAsync(() ->
                            oneDayAvgJson = download("average BIN prices", "1day.json.gz")));

                if (threeDayAvgJson == null || type == SkyblockerConfig.Average.THREE_DAY || type == SkyblockerConfig.Average.BOTH)
                    futureList.add(CompletableFuture.runAsync(() ->
                            threeDayAvgJson = download("average BIN prices", "3day.json.gz")));
            }
            if (SkyblockerConfig.get().general.itemTooltip.enableLowestBIN) {
                futureList.add(CompletableFuture.runAsync(() ->
                        lowestBINJson = download("lowest BIN prices", null)));
            }
            if (SkyblockerConfig.get().general.itemTooltip.enableBazaarPrice) {
                futureList.add(CompletableFuture.runAsync(() ->
                        bazaarJson = download("bazaar prices", null)));
            }
            if (SkyblockerConfig.get().general.itemTooltip.enableNPCPrice && npcJson == null) {
                futureList.add(CompletableFuture.runAsync(() ->
                        npcJson = download("NPC prices", null)));
            }
            if (SkyblockerConfig.get().general.itemTooltip.enableMuseumDate && museumJson == null) {
                futureList.add(CompletableFuture.runAsync(() ->
                        museumJson = download("museum items", null)));
            }

            minute++;
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
                    .whenComplete((unused, throwable) -> nullMsgSend = false);
        }, 1200);
    }

    private static JsonObject download(String key, String avgType) {
        String rest = avgType == null ? "" : avgType;
        try {
            URL apiAddr = new URL(downloadList.get(key).concat(rest));
            try (InputStream src = apiAddr.openStream()) {
                GZIPInputStream gzipOutput = avgType == null ? null : new GZIPInputStream(src);
                try (InputStreamReader reader = new InputStreamReader(avgType == null ? src : gzipOutput)) {
                    return new Gson().fromJson(reader, JsonObject.class);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("[Skyblocker] Failed to download " + key + "!", e);
            return null;
        }
    }

    static {
        downloadList = new HashMap<>();
        downloadList.put("average BIN prices", "https://moulberry.codes/auction_averages_lbin/");
        downloadList.put("lowest BIN prices", "https://skytils.gg/api/auctions/lowestbins");
        downloadList.put("bazaar prices", "https://hysky.de/api/bazaar");
        downloadList.put("NPC prices", "https://hysky.de/api/npcprice");
        downloadList.put("museum items", "https://hysky.de/api/museum");
    }

}