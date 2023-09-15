package me.xmrvizzy.skyblocker.skyblock.item.exotic;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class DownloadItemApi {
    public static JsonObject ItemApiData;
    public static void init() throws IOException {
        try {
            URL url = new URL("https://api.hypixel.net/resources/skyblock/items"); // Item Api Request Url
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader api = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String lineReceived;
            StringBuffer fullJson = new StringBuffer();
            while ((lineReceived = api.readLine()) != null) {
                fullJson.append(lineReceived);
            }

            ItemApiData = new JsonObject();

            for (JsonElement element : new Gson().fromJson(fullJson.toString(), JsonObject.class).getAsJsonArray("items")) {
                JsonObject item = element.getAsJsonObject();
                ItemApiData.add(item.get("id").getAsString(), item);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
