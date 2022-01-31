package me.xmrvizzy.skyblocker.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.util.VersionNumber;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {
    public static boolean shouldUpdate(){
        Pattern pattern = Pattern.compile("v(\\d+)\\.(\\d+)\\.(\\d+)");
        Pattern localPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
        Matcher matcher;
        VersionNumber localVersion = null;
        VersionNumber latestVersion = null;
        Boolean shouldUpdate = false;

        try{
            URL url = new URL("https://api.modrinth.com/v2/project/skyblocker-liap/version");
            URLConnection request = url.openConnection();
            request.connect();

            JsonElement json = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));
            JsonArray jsonArray = json.getAsJsonArray();
            JsonObject versionJson = jsonArray.get(0).getAsJsonObject();
            matcher = pattern.matcher(versionJson.get("version_number").getAsString());
            if (matcher.find()){
                latestVersion = VersionNumber.parse(matcher.group(1) + "." + matcher.group(2) + "." + matcher.group(3));
            }
            matcher = localPattern.matcher(FabricLoader.getInstance().getModContainer(SkyblockerMod.NAMESPACE).get().getMetadata().getVersion().getFriendlyString());
            if (matcher.find()){
                localVersion = VersionNumber.parse(matcher.group(1) + "." + matcher.group(2) + "." + matcher.group(3));
            }
            if (localVersion != null && latestVersion != null)
                if (localVersion.compareTo(latestVersion) < 0) shouldUpdate = true;

        } catch (IOException e) {
            e.printStackTrace();
            shouldUpdate = false;
        }
        return shouldUpdate;
    }
}
