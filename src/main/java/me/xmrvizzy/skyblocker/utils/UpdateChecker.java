package me.xmrvizzy.skyblocker.utils;

import com.google.gson.*;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.util.VersionNumber;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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
        boolean shouldUpdate = false;
        if (SkyblockerConfig.get().general.enableUpdateNotification){
            try{
                URL url = new URL("https://api.modrinth.com/v2/project/skyblocker-liap/version");

                InputStreamReader reader = new InputStreamReader(url.openStream());
                JsonObject versionJson = new Gson().fromJson(reader, JsonElement.class).getAsJsonArray().get(0).getAsJsonObject();
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
            }
        }
        return shouldUpdate;
    }
}
