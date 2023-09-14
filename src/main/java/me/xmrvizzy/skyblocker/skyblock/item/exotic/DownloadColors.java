package me.xmrvizzy.skyblocker.skyblock.item.exotic;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.xmrvizzy.skyblocker.utils.Http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DownloadColors {
    public static JsonObject ItemApiData;
    public static void init() throws IOException {
        try {
            String url = "https://api.hypixel.net/resources/skyblock/items"; // Item Api Request Url
            String jsonResponse = Http.sendGetRequest(url);

            ItemApiData = new JsonObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
