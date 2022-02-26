package me.xmrvizzy.skyblocker.skyblock.item;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Month;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;

public class PriceInfoTooltip {
    private static final SkyblockerMod skyblocker = SkyblockerMod.getInstance();
    private static JsonObject npcPricesJson;
    private static JsonObject bazaarPricesJson;
    private static JsonObject oneDayAvgPricesJson;
    private static JsonObject threeDayAvgPricesJson;
    private static JsonObject lowestPricesJson;
    private static JsonObject isMuseumJson;
    private static boolean nullMsgSend = false;
    private final static Gson gson = new Gson();

    public static void onInjectTooltip(ItemStack stack, TooltipContext context, List<Text> lines) {
        int count = stack.getCount();
        String name = getInternalNameFromNBT(stack);
        String timestamp = getTimestamp(stack);
        List<String> listString = lines.stream()
                .map(Text::getString).toList();

        try {
            if (SkyblockerConfig.get().general.itemTooltip.enableNPCPrice && !listString.contains("NPC Price")) {
                if (npcPricesJson.has(name)) {
                    lines.add(new LiteralText(String.format("%-21s", "NPC Price:"))
                            .formatted(Formatting.YELLOW)
                            .append(getCoinsMessage(npcPricesJson.get(name).getAsDouble(), count)));
                }
            }

            if ((!listString.contains("Avg. BIN Price") && (threeDayAvgPricesJson.has(name) || oneDayAvgPricesJson.has(name)))
                    || (!listString.contains("Lowest BIN Price") && lowestPricesJson.has(name))) {

                if (SkyblockerConfig.get().general.itemTooltip.enableLowestBIN && lowestPricesJson.has(name)) {
                    lines.add(new LiteralText(String.format("%-19s", "Lowest BIN Price:"))
                            .formatted(Formatting.GOLD)
                            .append(getCoinsMessage(lowestPricesJson.get(name).getAsDouble(), count)));
                }

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
                    //name = "POTION";
                    name = "";
                } else if (name.contains("RUNE-")) {
                    //name = "RUNE";
                    name = "";
                } else {
                    name = name.replace(":", "-");
                }

                if (SkyblockerConfig.get().general.itemTooltip.enableAvgBIN && !name.isEmpty()
                        && (threeDayAvgPricesJson.has(name) || oneDayAvgPricesJson.has(name))) {
                    switch (SkyblockerConfig.get().general.itemTooltip.avg) {
                        case ONE_DAY -> lines.add(new LiteralText(String.format("%-19s", "1 Day Avg. Price:"))
                                        .formatted(Formatting.GOLD)
                                        .append(getCoinsMessage(oneDayAvgPricesJson.get(name).getAsDouble(), count)));
                        case THREE_DAY -> lines.add(new LiteralText(String.format("%-19s", "3 Day Avg. Price:"))
                                .formatted(Formatting.GOLD)
                                .append(getCoinsMessage(threeDayAvgPricesJson.get(name).getAsDouble(), count)));
                        case BOTH -> {
                            lines.add(new LiteralText(String.format("%-19s", "1 Day Avg. Price:"))
                                    .formatted(Formatting.GOLD)
                                    .append(getCoinsMessage(oneDayAvgPricesJson.get(name).getAsDouble(), count)));
                            lines.add(new LiteralText(String.format("%-19s", "3 Day Avg. Price:"))
                                    .formatted(Formatting.GOLD)
                                    .append(getCoinsMessage(threeDayAvgPricesJson.get(name).getAsDouble(), count)));
                        }
                    }
                }
            } else if (SkyblockerConfig.get().general.itemTooltip.enableBazaarPrice
                    && !listString.contains("Bazaar buy Price") || !listString.contains("Bazaar sell Price")) {

                if (bazaarPricesJson.has(name)) {
                    JsonObject getItem = bazaarPricesJson.getAsJsonObject(name);
                    lines.add(new LiteralText(String.format("%-18s", "Bazaar buy Price:"))
                            .formatted(Formatting.GOLD)
                            .append(getCoinsMessage(getItem.get("buyPrice").getAsDouble(), count)));
                    lines.add(new LiteralText(String.format("%-19s", "Bazaar sell Price:"))
                            .formatted(Formatting.GOLD)
                            .append(getCoinsMessage(getItem.get("sellPrice").getAsDouble(), count)));
                }
            }

            if (SkyblockerConfig.get().general.itemTooltip.enableMuseumDate && !listString.contains("Museum")) {
                if (isMuseumJson.has(name)) {
                    String itemCategory = isMuseumJson.get(name).toString().replaceAll("\"", "");
                    String format = switch (itemCategory) {
                        case "Weapons" -> "%-18s";
                        case "Armor" -> "%-19s";
                        default -> "%-20s";
                    };

                    lines.add(new LiteralText(String.format(format, "Museum: (" + itemCategory + ")"))
                            .formatted(Formatting.LIGHT_PURPLE)
                            .append(new LiteralText(timestamp != null ? timestamp : "").formatted(Formatting.RED)));
                }
            } else if (!listString.contains("Obtained") && timestamp != null && SkyblockerConfig.get().general.itemTooltip.enableMuseumDate) {
                lines.add(new LiteralText(String.format("%-22s", "Obtained: "))
                        .formatted(Formatting.LIGHT_PURPLE)
                        .append(new LiteralText(timestamp).formatted(Formatting.RED)));
            }
        } catch (NullPointerException ex) {
            if ((npcPricesJson == null || bazaarPricesJson == null || oneDayAvgPricesJson == null
                    || threeDayAvgPricesJson == null || lowestPricesJson == null || isMuseumJson == null)
                    && !nullMsgSend)  {
                MinecraftClient.getInstance().player.sendMessage(new TranslatableText("skyblocker.itemTooltip.NullMessage"), false);
                nullMsgSend = true;
                skyblocker.scheduler.schedule(() -> nullMsgSend = false, 1200);
            }
        } catch (Exception ex) {
            if (MinecraftClient.getInstance().player == null) {
                throw new RuntimeException("[Skyblocker] client.player cannot be null!");
            }
            MinecraftClient.getInstance().player.sendMessage(new LiteralText(ex.toString()), false);
        }

    }

    public static NbtCompound getInternalNameForItem(ItemStack stack) {
        if (stack == null) return null;
        return stack.getNbt();
    }

    public static String getTimestamp(ItemStack stack) {
        NbtCompound tag = getInternalNameForItem(stack);
        String internalName = null;
        if (tag != null && tag.contains("ExtraAttributes", 10)) {
            NbtCompound ea = tag.getCompound("ExtraAttributes");

            if (ea.contains("timestamp", 8)) {
                internalName = ea.getString("timestamp").replaceAll("\\s(.*)", "");
                int month = Integer.parseInt(internalName.replaceAll("(\\d+)/(\\d+)/(\\d+)", "$1"));
                internalName = StringUtils.capitalize(internalName.replaceAll("(\\d+)/(\\d+)/(\\d+)", Month.of(month) + " $2, 20$3").toLowerCase());
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
                    internalName += "-" + enchant.toUpperCase() + "-" + enchants.getInt(enchant);
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
                    internalName += "-" + ea.getString("potion").toUpperCase() + "-" + ea.getInt("potion_level")
                            + enhanced + extended + splash;
                }
            } else if ("RUNE".equals(internalName)) {
                if (ea.contains("runes")) {
                    NbtCompound runes = ea.getCompound("runes");
                    String rune = runes.getKeys().stream().findFirst().get();
                    internalName += "-" + rune.toUpperCase() + "-" + runes.getInt(rune);
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

    public static int minute = 0;
    public static void init() {
        skyblocker.scheduler.scheduleCyclic(() -> {
            {
                if ((SkyblockerConfig.get().general.itemTooltip.enableAvgBIN || minute == 0)
                        && (oneDayAvgPricesJson == null || threeDayAvgPricesJson == null || minute % 5 == 0)) {
                    SkyblockerConfig.Average avg;
                    if (oneDayAvgPricesJson == null || threeDayAvgPricesJson == null || minute == 0) {
                        avg = SkyblockerConfig.Average.BOTH;
                    } else {
                        avg = SkyblockerConfig.get().general.itemTooltip.avg;
                    }
                    switch (avg) {
                        case ONE_DAY -> CompletableFuture.runAsync(PriceInfoTooltip::download1DayAvgPrices);
                        case THREE_DAY -> CompletableFuture.runAsync(PriceInfoTooltip::download3DayAvgPrices);
                        case BOTH -> {
                            CompletableFuture.runAsync(PriceInfoTooltip::download1DayAvgPrices);
                            CompletableFuture.runAsync(PriceInfoTooltip::download3DayAvgPrices);
                        }
                    }
                }
                if (SkyblockerConfig.get().general.itemTooltip.enableLowestBIN || minute == 0) {
                    CompletableFuture.runAsync(PriceInfoTooltip::downloadLowestPrices);
                }
                if ((SkyblockerConfig.get().general.itemTooltip.enableBazaarPrice || minute == 0) && (bazaarPricesJson == null || minute % 3 == 0)) {
                    CompletableFuture.runAsync(PriceInfoTooltip::downloadBazaarPrices);
                }
                if ((SkyblockerConfig.get().general.itemTooltip.enableNPCPrice || minute == 0) && npcPricesJson == null) {
                    CompletableFuture.runAsync(PriceInfoTooltip::downloadNPCPrices);
                }
                if ((SkyblockerConfig.get().general.itemTooltip.enableMuseumDate || minute == 0) && isMuseumJson == null) {
                    CompletableFuture.runAsync(PriceInfoTooltip::downloadIsMuseum);
                }
                minute++;
            }
        }, 1200);
    }

    private static void download1DayAvgPrices() {
        JsonObject result = null;
        String avgDay = null;
        try {
            URL apiAddr = new URL("https://moulberry.codes/auction_averages_lbin/1day.json.gz");
            try (InputStream src = apiAddr.openStream()) {
                try (GZIPInputStream gzipOutput = new GZIPInputStream(src)) {
                    try (InputStreamReader reader = new InputStreamReader(gzipOutput)) {
                        result = new Gson().fromJson(reader, JsonObject.class);
                    }
                }
            }
        } catch (IOException e) {
            LogManager.getLogger(PriceInfoTooltip.class.getName()).warn("[Skyblocker] Failed to download average BIN prices!", e);
        }
        oneDayAvgPricesJson = result;
    }
    
    private static void download3DayAvgPrices() {
        JsonObject result = null;
        String avgDay = null;
        try {
            URL apiAddr = new URL("https://moulberry.codes/auction_averages_lbin/3day.json.gz");
            try (InputStream src = apiAddr.openStream()) {
                try (GZIPInputStream gzipOutput = new GZIPInputStream(src)) {
                    try (InputStreamReader reader = new InputStreamReader(gzipOutput)) {
                        result = new Gson().fromJson(reader, JsonObject.class);
                    }
                }
            }
        } catch (IOException e) {
            LogManager.getLogger(PriceInfoTooltip.class.getName()).warn("[Skyblocker] Failed to download average BIN prices!", e);
        }
        threeDayAvgPricesJson = result;
    }

    private static void downloadBazaarPrices() {
        JsonObject result = null;
        try {
            URL apiAddr = new URL("https://sky.shiiyu.moe/api/v2/bazaar");
            InputStreamReader reader = new InputStreamReader(apiAddr.openStream());
            result = new Gson().fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            LogManager.getLogger(PriceInfoTooltip.class.getName()).warn("[Skyblocker] Failed to download bazaar prices!", e);
        }
        bazaarPricesJson = result;
    }

    private static void downloadLowestPrices() {
        JsonObject result = null;
        try {
            URL apiAddr = new URL("https://sbe-stole-skytils.design/api/auctions/lowestbins");
            InputStreamReader reader = new InputStreamReader(apiAddr.openStream());
            result = new Gson().fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            LogManager.getLogger(PriceInfoTooltip.class.getName()).warn("[Skyblocker] Failed to download lowest BIN prices!", e);
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
            LogManager.getLogger(PriceInfoTooltip.class.getName()).warn("[Skyblocker] Failed to download NPC prices!", e);
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
            LogManager.getLogger(PriceInfoTooltip.class.getName()).warn("[Skyblocker] Failed to download museum items!", e);
        }
        isMuseumJson = result;
    }

}
