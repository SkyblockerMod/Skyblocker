package me.xmrvizzy.skyblocker.skyblock.item.exotic;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.xmrvizzy.skyblocker.utils.Http;

import java.io.IOException;

public class DownloadColors {
    public static JsonObject ColorApiData;
    public static void init() throws IOException {
        try {
            String url = "https://hysky.de/api/color"; // Item Api Request Url
            String jsonResponse = Http.sendGetRequest(url);

            ColorApiData = JsonParser.parseString(jsonResponse).getAsJsonObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
