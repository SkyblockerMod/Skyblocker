package de.hysky.skyblocker.skyblock.searchOverlay;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Http;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;

import java.net.http.HttpHeaders;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SearchOverManager {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    private static final String THREE_DAY_AVERAGE = "https://moulberry.codes/auction_averages_lbin/3day.json";
    private static final Pattern BAZAAR_ENCHANTMENT_PATTERN = Pattern.compile("ENCHANTMENT_(\\D*)_(\\d+)");
    private static final String[] ROMAN_NUMERALS = new String[]{
            "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI",
            "XII", "XIII", "XIV", "XV", "XVI", "XVII","XVIII", "XIX", "XX"
    };

    public static boolean visible = false;
    public static String search = "";
    private static @Nullable SignBlockEntity Sign = null;
    private static boolean SignFront = true;

    private static boolean IsAuction;


    public static Map<String,String> itemNameLookup = new HashMap<>();
    public static HashSet<String> bazaarItems =new HashSet<>();
    public static HashSet<String> auctionItems =new HashSet<>();

    public static String[] suggestionsArray = {};
    public static void init() {
        //get bazaar items
        System.out.println("is there somethin");
        try {
            String response = Http.sendGetRequest("https://api.hypixel.net/v2/resources/skyblock/items");
            System.out.println("response:");
            JsonArray items = JsonParser.parseString(response).getAsJsonObject().getAsJsonArray("items");
            System.out.println("jsonItem:");
            for (JsonElement entry : items) {
                if (entry.isJsonObject()) {
                    JsonObject item = entry.getAsJsonObject();
                    String itemId = item.get("id").getAsString();
                    String itemName = item.get("name").getAsString();
                    itemNameLookup.put(itemId,itemName);
                }

            }
        } catch (Exception e) {
            //can not get items skyblock items
        }
        try (Http.ApiResponse response = Http.sendHypixelRequest("skyblock/bazaar", "")) {
            JsonObject products = JsonParser.parseString(response.content()).getAsJsonObject().get("products").getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : products.entrySet()) {
                if (entry.getValue().isJsonObject()) {
                    JsonObject product = entry.getValue().getAsJsonObject();
                    String id = product.get("product_id").getAsString();
                    Matcher matcher = BAZAAR_ENCHANTMENT_PATTERN.matcher(id);
                    if (matcher.matches()) {//format enchantments
                        String name = matcher.group(1).replace("_", " ");
                        name = capitalizeFully(name);
                        int enchantLevel = Integer.parseInt(matcher.group(2));
                        String level = "";
                        if (enchantLevel > 0){
                            level = ROMAN_NUMERALS[enchantLevel-1];
                        }
                        bazaarItems.add(name +  " " + level);
                    }else{//look up id for name
                        String name = itemNameLookup.get(product.get("product_id").getAsString());
                        if (name != null){
                            name = trimItemColor(name);
                            bazaarItems.add(name);
                        }
                    }
                }

            }


        } catch (Exception e) {
           //can not get items for bazaar search //todo log
        }
        //get auction items
        try {
            JsonObject AuctionData = SkyblockerMod.GSON.fromJson(Http.sendGetRequest(THREE_DAY_AVERAGE), JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : AuctionData.entrySet()) {
                String id = entry.getKey();
                id = id.split("[+;-]")[0];
                String name = itemNameLookup.get(id);
                if (name != null){
                    name = trimItemColor(name);
                    auctionItems.add(name);
                }
            }


        } catch (Exception e) {
           //can not find ah todo logger
        }

    }

    private static String capitalizeFully(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return Arrays.stream(str.split("\\s+"))
                .map(t -> t.substring(0, 1).toUpperCase() + t.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    private static String trimItemColor(String string){
        if (string.isEmpty()) return string;
        if (string.startsWith("§") ){
            return string.substring(2);
        }else {
            return string;
        }
    }

    public static void updateSign(SignBlockEntity sign, boolean front, boolean isAuction) {
        visible= true;
        SignFront = front;
        Sign = sign;
        IsAuction = isAuction;
        if (SkyblockerConfigManager.get().general.searchOverlay.keepPreviousSearches){
            Text[] messages = Sign.getText(SignFront).getMessages(CLIENT.shouldFilterText());
            search = messages[0].getString();
            if(!messages[1].getString().isEmpty()){
                if (!search.endsWith(" ")){
                    search += " ";
                }
                 search += messages[1].getString();
            }
        }else{
            search = "";
        }

        suggestionsArray = new String[]{};

    }
    protected static void updateSearch(String newValue) {
        search = newValue;
        //update the suggestion values
        int totalSuggestions = SkyblockerConfigManager.get().general.searchOverlay.maxSuggestions;
        suggestionsArray = new String[totalSuggestions];
        if (newValue.isBlank() || totalSuggestions == 0) return; //do not search for empty value
        if (IsAuction){
            suggestionsArray = auctionItems.stream().filter(item -> item.toLowerCase().contains(search.toLowerCase())).limit(totalSuggestions).toList().toArray(suggestionsArray);
        }else {
            suggestionsArray = bazaarItems.stream().filter(item -> item.toLowerCase().contains(search.toLowerCase())).limit(totalSuggestions).toList().toArray(suggestionsArray);
        }
    }
    protected  static String getSuggestion(int index){
         if (suggestionsArray.length> index && suggestionsArray[index] != null ){
            return suggestionsArray[index];
        }else{//there are no suggestions yet
            return "";
        }
    }
    protected  static String getHistory(int index){
        if (IsAuction){
            if (SkyblockerConfigManager.get().general.searchOverlay.auctionHistory.size() >index){
                return  SkyblockerConfigManager.get().general.searchOverlay.auctionHistory.get(index);
            }

        }else{
            if (SkyblockerConfigManager.get().general.searchOverlay.bazaarHistory.size() >index){
                return  SkyblockerConfigManager.get().general.searchOverlay.bazaarHistory.get(index);
            }
        }
        return  null;
    }
    private static void saveHistory(){
        //save to history
        int historyLength = SkyblockerConfigManager.get().general.searchOverlay.historyLength;
        if (IsAuction){
            SkyblockerConfigManager.get().general.searchOverlay.auctionHistory.add(0,search);
            if (SkyblockerConfigManager.get().general.searchOverlay.auctionHistory.size() >historyLength) {
                SkyblockerConfigManager.get().general.searchOverlay.auctionHistory = SkyblockerConfigManager.get().general.searchOverlay.auctionHistory.subList(0, historyLength);
            }
        }else{
            SkyblockerConfigManager.get().general.searchOverlay.bazaarHistory.add(0,search);
            if (SkyblockerConfigManager.get().general.searchOverlay.bazaarHistory.size() >historyLength) {
                SkyblockerConfigManager.get().general.searchOverlay.bazaarHistory = SkyblockerConfigManager.get().general.searchOverlay.bazaarHistory.subList(0, historyLength);
            }
        }
        SkyblockerConfigManager.save();
    }

    protected static void pushSearch() {
        //save to history
        if (!search.isEmpty()){
            saveHistory();
        }
        //splits text into 2 lines max = 30 chars
        StringBuilder line0 = new StringBuilder();
        String line1;
        if (search.length() <= 15){
            line0 = new StringBuilder(search);
            line1 = "";
        }else {
            String[] words = search.split(" ");
            for (String word : words){
                if (line0.isEmpty()) {
                    line0 = new StringBuilder(word);
                    continue;
                }
                if (line0.length() + word.length() < 14 ){ //max 15 but including space is 14
                    line0.append(" ").append(word);
                }
                else {
                    break;
                }
            }
            line1 = search.substring(line0.length(),Math.min(search.length(),30));
        }

        // send packet to update sign
        if (CLIENT.player != null || Sign != null) {
            Text[] messages = Sign.getText(SignFront).getMessages(CLIENT.shouldFilterText());
            CLIENT.player.networkHandler.sendPacket(new UpdateSignC2SPacket(Sign.getPos(), SignFront,
                    line0.toString(),
                    line1,
                    messages[2].getString(),
                    messages[3].getString()
            ));
        }
    }

}
