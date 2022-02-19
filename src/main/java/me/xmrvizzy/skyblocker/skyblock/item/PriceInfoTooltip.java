package me.xmrvizzy.skyblocker.skyblock.item;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class PriceInfoTooltip {
    private static JsonObject npcPricesJson;
    private static JsonObject bazaarPricesJson;
    private static JsonObject avgPricesJson;
    private static JsonObject lowestPricesJson;
    private static JsonObject isMuseumJson;

    public static void onInjectTooltip(ItemStack stack, TooltipContext context, List<Text> lines) {
        int count = stack.getCount();
        String name = getInternalNameFromNBT(stack);
        String timestamp = getTimestamp(stack);
        List<String> listString = lines.stream()
                .map(Text::getString)
                .collect(Collectors.toList());

        try {
            if (SkyblockerConfig.get().general.itemTooltip.enableNPCPrice
                    && !listString.contains("NPC Price") && npcPricesJson != null && npcPricesJson.has(name)) {

                lines.add(new LiteralText(String.format("%-21s", "NPC Price:"))
                        .formatted(Formatting.YELLOW)
                        .append(getCoinsMessage(npcPricesJson.get(name).getAsDouble(), count)));
            }

            if ((!listString.contains("Avg. BIN Price") && avgPricesJson != null && avgPricesJson.has(name))
                    || (!listString.contains("Lowest BIN Price") && lowestPricesJson != null && lowestPricesJson.has(name))) {

                if (SkyblockerConfig.get().general.itemTooltip.enableLowestBIN) {
                    lines.add(new LiteralText(String.format("%-19s", "Lowest BIN Price:"))
                            .formatted(Formatting.GOLD)
                            .append(getCoinsMessage(lowestPricesJson.get(name).getAsDouble(), count)));
                }

                // Change format from Skytils to Moulberry's for Avg. BIN
                if (name.contains("PET-")) {
                    name = name.replace("PET-", "")
                            .replace("0", "COMMON")
                            .replace("1", "UNCOMMON")
                            .replace("2", "RARE")
                            .replace("3", "EPIC")
                            .replace("4", "LEGENDARY")
                            .replace("5", "MYTHIC")
                            .replace("-", ";");
                } else if (name.contains("ENCHANTED_BOOK-")) {
                    name = name.replace("ENCHANTED_BOOK-", "").replace("-", ";");
                } else {
                    name = name.replace(":", "-");
                }

                // has(name) check because Skytils keeps old data but Moulberry not
                if (SkyblockerConfig.get().general.itemTooltip.enableAvgBIN && avgPricesJson.has(name)) {
                    lines.add(new LiteralText(String.format("%-21s", "Avg. BIN Price:"))
                            .formatted(Formatting.GOLD)
                            .append(getCoinsMessage(avgPricesJson.get(name).getAsDouble(), count)));
                }
            } else if (SkyblockerConfig.get().general.itemTooltip.enableBazaarPrice
                    && (!listString.contains("Bazaar buy Price") || !listString.contains("Bazaar sell Price"))
                    && bazaarPricesJson != null && bazaarPricesJson.has(name)) {

                JsonObject getItem = bazaarPricesJson.getAsJsonObject(name);
                lines.add(new LiteralText(String.format("%-18s", "Bazaar buy Price:"))
                        .formatted(Formatting.GOLD)
                        .append(getCoinsMessage(getItem.get("buyPrice").getAsDouble(), count)));
                lines.add(new LiteralText(String.format("%-19s", "Bazaar sell Price:"))
                        .formatted(Formatting.GOLD)
                        .append(getCoinsMessage(getItem.get("sellPrice").getAsDouble(), count)));
            }

            if (SkyblockerConfig.get().general.itemTooltip.enableMuseumDate
                    && !listString.contains("Museum") && isMuseumJson != null && isMuseumJson.has(name)) {

                String itemCategory = isMuseumJson.get(name).toString().replaceAll("\"", "");
                String format = switch (itemCategory) {
                    case "Weapons" -> "%-18s";
                    case "Armor" -> "%-19s";
                    default -> "%-20s";
                };

                lines.add(new LiteralText(String.format(format, "Museum: (" + itemCategory + ")"))
                        .formatted(Formatting.LIGHT_PURPLE)
                        .append(new LiteralText(timestamp != null ? timestamp : "").formatted(Formatting.RED)));
            } else if (!listString.contains("Obtained") && timestamp != null
                    && SkyblockerConfig.get().general.itemTooltip.enableMuseumDate) {

                lines.add(new LiteralText(String.format("%-22s", "Obtained: "))
                        .formatted(Formatting.LIGHT_PURPLE)
                        .append(new LiteralText(timestamp).formatted(Formatting.RED)));
            }
        } catch (Exception e) {
            if (MinecraftClient.getInstance().player == null) {
                throw new RuntimeException("[Skyblocker] client.player cannot be null!");
            }
            MinecraftClient.getInstance().player.sendMessage(new LiteralText(e.toString()), false);
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
                NbtCompound enchants = ea.getCompound("enchantments");

                for (String enchName : enchants.getKeys()) {
                    internalName += "-" + enchName.toUpperCase() + "-" + enchants.getInt(enchName);
                    break;
                }
            }
        }
        return internalName;
    }

    private static Text getCoinsMessage(double price, int count) {
        if (count == 1) {
            String priceString = String.format(Locale.ENGLISH, "%1$,.0f", price);
            return new LiteralText(priceString + " Coins").formatted(Formatting.DARK_AQUA);
        } else {
            String priceString = String.format(Locale.ENGLISH, "%1$,.0f", price * count);
            LiteralText priceText = (LiteralText) new LiteralText(priceString + " Coins ").formatted(Formatting.DARK_AQUA);
            priceString = String.format(Locale.ENGLISH, "%1$,.0f", price);
            LiteralText priceText2 = (LiteralText)  new LiteralText( "(" + priceString + " each)").formatted(Formatting.GRAY);
            return priceText.append(priceText2);
        }
    }

    public static boolean firstRun = true;
    public static void init() {
        TimerTask repeatedTask = new TimerTask() {
            public void run() {
                if (SkyblockerConfig.get().general.itemTooltip.enableAvgBIN || PriceInfoTooltip.firstRun)
                    CompletableFuture.runAsync(PriceInfoTooltip::downloadAvgPrices);
                if (SkyblockerConfig.get().general.itemTooltip.enableLowestBIN || PriceInfoTooltip.firstRun)
                    CompletableFuture.runAsync(PriceInfoTooltip::downloadLowestPrices);
                if (SkyblockerConfig.get().general.itemTooltip.enableBazaarPrice || PriceInfoTooltip.firstRun)
                    CompletableFuture.runAsync(PriceInfoTooltip::downloadBazaarPrices);
                if (SkyblockerConfig.get().general.itemTooltip.enableNPCPrice || PriceInfoTooltip.firstRun)
                    CompletableFuture.runAsync(PriceInfoTooltip::downloadNPCPrices);
                if (SkyblockerConfig.get().general.itemTooltip.enableMuseumDate || PriceInfoTooltip.firstRun)
                    CompletableFuture.runAsync(PriceInfoTooltip::downloadIsMuseum);
            }
        };

        firstRun = false;
        Timer timer = new Timer("PriceInfoDownloader");
        timer.scheduleAtFixedRate(repeatedTask, 0L, 1000L * 60L);
    }

    private static void downloadAvgPrices() {
        JsonObject result = null;
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
            LogManager.getLogger(PriceInfoTooltip.class.getName()).warn("[Skyblocker] Failed to download auction item prices!", e);
        }
        avgPricesJson = result;
    }

    private static void downloadBazaarPrices() {
        JsonObject result = null;
        try {
            URL apiAddr = new URL("https://sky.shiiyu.moe/api/v2/bazaar");
            InputStreamReader reader = new InputStreamReader(apiAddr.openStream());
            result = new Gson().fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            LogManager.getLogger(PriceInfoTooltip.class.getName()).warn("[Skyblocker] Failed to download bazaar item prices!", e);
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
            LogManager.getLogger(PriceInfoTooltip.class.getName()).warn("[Skyblocker] Failed to download lb item prices!", e);
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
            LogManager.getLogger(PriceInfoTooltip.class.getName()).warn("[Skyblocker] Failed to download shop item prices!", e);
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
