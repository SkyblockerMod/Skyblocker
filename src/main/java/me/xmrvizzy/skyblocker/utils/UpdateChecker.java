package me.xmrvizzy.skyblocker.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.spongepowered.asm.util.VersionNumber;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {
    public static Pattern pattern = Pattern.compile("v(\\d+)\\.(\\d+)\\.(\\d+)");
    public static Pattern localPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    public static Matcher matcher;
    public static VersionNumber localVersion = null;
    public static VersionNumber latestVersion = null;

    public static void init(){
        if (!SkyblockerMod.getInstance().CONFIG.general.enableUpdateNotification()) return;
        try {
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SkyblockEvents.JOIN.register(() -> {
            if (localVersion != null && latestVersion != null) {
                if (localVersion.compareTo(latestVersion) == -1) MinecraftClient.getInstance().player.sendMessage(Text.translatable("skyblocker.update.update_message"), false);
            }
        });

    }
}
