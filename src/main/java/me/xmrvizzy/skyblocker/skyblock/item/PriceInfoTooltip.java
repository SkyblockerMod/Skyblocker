package me.xmrvizzy.skyblocker.skyblock.item;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;


import me.xmrvizzy.skyblocker.SkyblockerMod;

public class PriceInfoTooltip {
    
    public static void onInjectTooltip(ItemStack stack, TooltipContext context, List<Text> list) {
        String name = getInternalNameForItem(stack);
        
        try {

            if(SkyblockerMod.prices != null && SkyblockerMod.prices.containsKey(name)){
                if(!list.toString().contains("Lowest BIN Price")){
                    Double price = round((Double)SkyblockerMod.prices.get(name), 2);
                   
                    list.add(new LiteralText("Lowest BIN Price: " + price).formatted(Formatting.GOLD));
                }
            }
        }catch(Exception e) {

        }
    
	}
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
    
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    public static String getInternalNameForItem(ItemStack stack) {
        if(stack == null) return null;
        CompoundTag tag = stack.getTag();
        return getInternalnameFromNBT(tag);
    }

    public static String getInternalnameFromNBT(CompoundTag tag) {
        String internalname = null;
        if(tag != null && tag.contains("ExtraAttributes", 10)) {
            CompoundTag  ea = tag.getCompound("ExtraAttributes");

            if(ea.contains("id", 8)) {
                internalname = ea.getString("id").replaceAll(":", "-");
            } else {
                return null;
            }


            if("ENCHANTED_BOOK".equals(internalname)) {
                CompoundTag enchants = ea.getCompound("enchantments");

                for(String enchname : enchants.getKeys()) {
                    internalname = enchname.toUpperCase() + ";" + enchants.getInt(enchname);
                    break;
                }
            }
        }

        return internalname;
    }

    public static Map downloadPrices() {
        try {
            downloadUsingStream("https://moulberry.codes/auction_averages_lbin/1day.json.gz", "1day.json.gz");
            decompressGzipFile("1day.json.gz", "1day.json");
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(Paths.get("1day.json"));
              // convert JSON file to map
            Map<?, ?> map = gson.fromJson(reader, Map.class);
            return map;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static void decompressGzipFile(String gzipFile, String newFile) {
        try {
            FileInputStream fis = new FileInputStream(gzipFile);
            GZIPInputStream gis = new GZIPInputStream(fis);
            FileOutputStream fos = new FileOutputStream(newFile);
            byte[] buffer = new byte[1024];
            int len;
            while((len = gis.read(buffer)) != -1){
                fos.write(buffer, 0, len);
            }
            //close resources
            fos.close();
            gis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    private static void downloadUsingStream(String urlStr, String file) throws IOException{
        URL url = new URL(urlStr);
        BufferedInputStream bis = new BufferedInputStream(url.openStream());
        FileOutputStream fis = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int count=0;
        while((count = bis.read(buffer,0,1024)) != -1)
        {
            fis.write(buffer, 0, count);
        }
        fis.close();
        bis.close();
    }
}
