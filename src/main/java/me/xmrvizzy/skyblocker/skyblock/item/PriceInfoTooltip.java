package me.xmrvizzy.skyblocker.skyblock.item;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class PriceInfoTooltip {
    private static JsonObject shopPricesJson;
    private static JsonObject bazaarPricesJson;
    private static JsonObject auctionPricesJson;
    private static JsonObject lbAuctionPricesJson;
    private static JsonObject isMuseumJson;

    public static void onInjectTooltip(ItemStack stack, TooltipContext context, List<Text> list) {
        String name = getInternalNameFromNBT(stack);
        String timestamp = getTimestamp(stack);
        List<String> listString = list.stream()
                .map(Text::getString)
                .collect(Collectors.toList());
        try {
            if (!listString.contains("NPC Price") && shopPricesJson != null && shopPricesJson.has(name)) {
                JsonElement getPrice = shopPricesJson.get(name);
                list.add(new LiteralText(String.format("%-23s", "NPC Price:")).formatted(Formatting.YELLOW).append(getCoinsMessage(getPrice.getAsDouble())));
            }
            if ((!listString.contains("Bazaar buy Price") || !listString.contains("Bazaar sell Price")) && bazaarPricesJson != null && bazaarPricesJson.has(name)) {
                JsonObject getItem = bazaarPricesJson.getAsJsonObject(name);
                list.add(new LiteralText(String.format("%-19s", "Bazaar buy Price:")).formatted(Formatting.GOLD).append(getCoinsMessage(getItem.get("buyPrice").getAsDouble())));
                list.add(new LiteralText(String.format("%-20s", "Bazaar sell Price:")).formatted(Formatting.GOLD).append(getCoinsMessage(getItem.get("sellPrice").getAsDouble())));
            } else if ((!listString.contains("Avg. BIN Price") && auctionPricesJson != null && auctionPricesJson.has(name)) || (!listString.contains("Lowest BIN Price") && lbAuctionPricesJson != null && lbAuctionPricesJson.has(name))) {
                if (!listString.contains("Lowest BIN Price") && lbAuctionPricesJson != null && lbAuctionPricesJson.has(name)) {
                    JsonElement getPrice = lbAuctionPricesJson.get(name);
                    list.add(new LiteralText(String.format("%-21s", "Lowest BIN Price:")).formatted(Formatting.GOLD).append(getCoinsMessage(getPrice.getAsDouble())));
                }
                if (!listString.contains("Avg. BIN Price") && auctionPricesJson != null && auctionPricesJson.has(name)) {
                    JsonElement getPrice = auctionPricesJson.get(name);
                    list.add(new LiteralText(String.format("%-22s", "Avg. BIN Price:")).formatted(Formatting.GOLD).append(getCoinsMessage(getPrice.getAsDouble())));
                }
            }

            if (!listString.contains("Museum") && isMuseumJson != null && isMuseumJson.has(name)) {
                String itemCategory = isMuseumJson.get(name).toString().replaceAll("\"", "");
                String format = switch (itemCategory) {
                    case "Weapons" -> "%-19s";
                    case "Armor" -> "%-20s";
                    default -> "%-21s";
                };
                list.add(new LiteralText(String.format(format, "Museum: (" + itemCategory + ")")).formatted(Formatting.LIGHT_PURPLE).append(new LiteralText(timestamp != null ? timestamp : "").formatted(Formatting.RED)));
            } else if (!listString.contains("Obtained") && timestamp != null) {
                list.add(new LiteralText(String.format("%-23s", "Obtained: ")).formatted(Formatting.LIGHT_PURPLE).append(new LiteralText(timestamp).formatted(Formatting.RED)));
            }
        } catch (Exception e) {
            assert MinecraftClient.getInstance().player != null;
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
                internalName = ea.getString("id").replaceAll(":", "-");
            } else {
                return null;
            }
            if ("ENCHANTED_BOOK".equals(internalName)) {
                NbtCompound enchants = ea.getCompound("enchantments");

                for (String enchName : enchants.getKeys()) {
                    internalName = enchName.toUpperCase() + ";" + enchants.getInt(enchName);
                    break;
                }
            }
        }
        return internalName;
    }

    private static Text getCoinsMessage(double price) {
        String priceString = String.format(Locale.ENGLISH, "%1$,.0f", price);
        return new LiteralText(priceString + " Coins").formatted(Formatting.DARK_AQUA);
    }

    public static void init() {
        new Thread(PriceInfoTooltip::downloadPrices).start();
        new Thread(PriceInfoTooltip::downloadLBPrices).start();
        new Thread(PriceInfoTooltip::downloadBazaarPrices).start();
        new Thread(PriceInfoTooltip::downloadShopPrices).start();
        new Thread(PriceInfoTooltip::downloadIsMuseum).start();
    }

    private static void downloadPrices() {
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
        auctionPricesJson = result;
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

    private static void downloadLBPrices() {
        JsonObject result = null;
        try {
            URL apiAddr = new URL("https://sbe-stole-skytils.design/api/auctions/lowestbins");
            InputStreamReader reader = new InputStreamReader(apiAddr.openStream());
            result = new Gson().fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            LogManager.getLogger(PriceInfoTooltip.class.getName()).warn("[Skyblocker] Failed to download lb item prices!", e);
        }
        lbAuctionPricesJson = result;
    }

    private static void downloadShopPrices() {
        JsonObject result = null;
        try {
            URL apiAddr = new URL("https://hysky.de/api/npcprice");
            InputStreamReader reader = new InputStreamReader(apiAddr.openStream());
            result = new Gson().fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            LogManager.getLogger(PriceInfoTooltip.class.getName()).warn("[Skyblocker] Failed to download shop item prices!", e);
        }
        shopPricesJson = result;
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
