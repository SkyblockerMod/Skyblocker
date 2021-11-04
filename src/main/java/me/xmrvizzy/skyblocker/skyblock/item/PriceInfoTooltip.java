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
import java.util.zip.GZIPInputStream;

public class PriceInfoTooltip {
    public static JsonObject shopPricesJson;
    public static JsonObject bazaarPricesJson;
    public static JsonObject auctionPricesJson;
    public static JsonObject ismuseumJson;

    public static void onInjectTooltip(ItemStack stack, TooltipContext context, List<Text> list) {
        String name = getInternalnameFromNBT(stack);
        String timestamp = getTimestamp(stack);
        try {
            if (!list.toString().contains("NPC Price") && shopPricesJson != null && shopPricesJson.has(name) ){
                JsonElement getPrice = shopPricesJson.get(name);
                String price = String.format(Locale.ENGLISH,"%1$,.2f",getPrice.getAsDouble());
                list.add(new LiteralText(String.format("%-22s", "NPC Price:")).formatted(Formatting.YELLOW).append(new LiteralText(price + " Coins").formatted(Formatting.DARK_AQUA)));
            }
            if(!list.toString().contains("Avg. BIN Price") && auctionPricesJson != null && auctionPricesJson.has(name) ){
                JsonElement getPrice = auctionPricesJson.get(name);
                String price = String.format(Locale.ENGLISH,"%1$,.2f",getPrice.getAsDouble());

                list.add(new LiteralText(String.format("%-22s", "Avg. BIN Price:")).formatted(Formatting.GOLD).append(new LiteralText(price + " Coins").formatted(Formatting.DARK_AQUA)));
            } else if((!list.toString().contains("Bazaar buy Price") || !list.toString().contains("Bazaar sell Price")) && bazaarPricesJson != null && bazaarPricesJson.has(name) ){
                JsonObject getItem = bazaarPricesJson.getAsJsonObject(name);
                String buyprice = String.format(Locale.ENGLISH,"%1$,.2f",getItem.get("buyPrice").getAsDouble());
                String sellprice = String.format(Locale.ENGLISH,"%1$,.2f",getItem.get("sellPrice").getAsDouble());
                list.add(new LiteralText(String.format("%-19s", "Bazaar buy Price:")).formatted(Formatting.GOLD).append(new LiteralText(buyprice + " Coins").formatted(Formatting.DARK_AQUA)));
                list.add(new LiteralText(String.format("%-20s", "Bazaar sell Price:")).formatted(Formatting.GOLD).append(new LiteralText(sellprice + " Coins").formatted(Formatting.DARK_AQUA)));
            }
            if (!list.toString().contains("Museum") && ismuseumJson != null && timestamp !=null ){
                list.add(new LiteralText(String.format(ismuseumJson.has(name)?"%-21s":"%-22s", (ismuseumJson.has(name))?"Museum: (Special)":"Museum:")).formatted(Formatting.LIGHT_PURPLE).append(new LiteralText(timestamp+"").formatted(Formatting.LIGHT_PURPLE)));
            }
        }catch(Exception e) {
            MinecraftClient.getInstance().player.sendMessage(new LiteralText(e.toString()), false);
        }

	}
    public static NbtCompound getInternalNameForItem(ItemStack stack) {
        if(stack == null) return null;
        return stack.getNbt();
    }

    public static String getTimestamp(ItemStack stack) {
        NbtCompound tag = getInternalNameForItem(stack);
        String internalname = null;
        if(tag != null && tag.contains("ExtraAttributes", 10)) {
            NbtCompound ea = tag.getCompound("ExtraAttributes");

            if (ea.contains("timestamp", 8)) {
                internalname = ea.getString("timestamp").replaceAll("\\s(.*)", "");
                int month = Integer.parseInt(internalname.replaceAll("(\\d+)/(\\d+)/(\\d+)", "$1"));
                internalname = StringUtils.capitalize(internalname.replaceAll("(\\d+)/(\\d+)/(\\d+)", Month.of(month)+" $2, 20$3").toLowerCase());
            }
        }
        return internalname;
    }

    public static String getInternalnameFromNBT(ItemStack stack) {
        NbtCompound tag = getInternalNameForItem(stack);
        String internalname = null;
        if(tag != null && tag.contains("ExtraAttributes", 10)) {
            NbtCompound  ea = tag.getCompound("ExtraAttributes");

            if(ea.contains("id", 8)) {
                internalname = ea.getString("id").replaceAll(":", "-");
            } else {
                return null;
            }
            if("ENCHANTED_BOOK".equals(internalname)) {
                NbtCompound enchants = ea.getCompound("enchantments");

                for(String enchname : enchants.getKeys()) {
                    internalname = enchname.toUpperCase() + ";" + enchants.getInt(enchname);
                    break;
                }
            }
        }
        return internalname;
    }

    public static void init() {
        new Thread(PriceInfoTooltip::downloadPrices).start();
        new Thread(PriceInfoTooltip::downloadbazaarPrices).start();
        new Thread(PriceInfoTooltip::downloadshopPrices).start();
        new Thread(PriceInfoTooltip::downloadismuseum).start();
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
        }
        catch(IOException e) {
            LogManager.getLogger(PriceInfoTooltip.class.getName()).warn("[Skyblocker] Failed to download auction item prices!", e);
        }
        auctionPricesJson = result;
    }
    private static void downloadbazaarPrices() {
        JsonObject result = null;
        try {
            URL apiAddr = new URL("https://sky.shiiyu.moe/api/v2/bazaar");
            InputStreamReader reader = new InputStreamReader(apiAddr.openStream());
            result = new Gson().fromJson(reader, JsonObject.class);
        }
        catch(IOException e) {
            LogManager.getLogger(PriceInfoTooltip.class.getName()).warn("[Skyblocker] Failed to download bazaar item prices!", e);
        }
        bazaarPricesJson = result;
    }
    private static void downloadshopPrices() {
        JsonObject result = null;
        try {
            URL apiAddr = new URL("https://hysky.de/api/npcprice");
            InputStreamReader reader = new InputStreamReader(apiAddr.openStream());
            result = new Gson().fromJson(reader, JsonObject.class);
        }
        catch(IOException e) {
            LogManager.getLogger(PriceInfoTooltip.class.getName()).warn("[Skyblocker] Failed to download shop item prices!", e);
        }
        shopPricesJson = result;
    }
    private static void downloadismuseum() {
        JsonObject result = null;
        try {
            URL apiAddr = new URL("https://hysky.de/api/ismuseum");
            InputStreamReader reader = new InputStreamReader(apiAddr.openStream());
            result = new Gson().fromJson(reader, JsonObject.class);
        }
        catch(IOException e) {
            LogManager.getLogger(PriceInfoTooltip.class.getName()).warn("[Skyblocker] Failed to download museum items!", e);
        }
        ismuseumJson = result;
    }

}
