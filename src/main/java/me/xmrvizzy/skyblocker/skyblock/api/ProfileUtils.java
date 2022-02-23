package me.xmrvizzy.skyblocker.skyblock.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import marcono1234.gson.recordadapter.RecordTypeAdapterFactory;
import me.xmrvizzy.skyblocker.skyblock.api.records.PlayerProfiles;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class ProfileUtils {
    public static PlayerProfiles getProfiles(String name){
        try {
            URL url = new URL("https://sky.shiiyu.moe/api/v2/profile/" + name);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            Gson gson = new GsonBuilder()
                    .registerTypeAdapterFactory(RecordTypeAdapterFactory.builder().allowMissingComponentValues().create())
                    .serializeNulls()
                    .create();
            return gson.fromJson(reader, PlayerProfiles.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
