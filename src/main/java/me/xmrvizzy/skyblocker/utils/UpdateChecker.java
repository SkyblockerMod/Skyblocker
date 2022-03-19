package me.xmrvizzy.skyblocker.utils;

import com.google.gson.*;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.events.SkyblockJoinCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.util.VersionNumber;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {
    public static boolean shouldUpdate = false;
    public static Pattern pattern = Pattern.compile("v(\\d+)\\.(\\d+)\\.(\\d+)");
    public static Pattern localPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    public static Matcher matcher;
    public static VersionNumber localVersion = null;
    public static VersionNumber latestVersion = null;
    public static boolean shouldUpdate(){
        if (SkyblockerConfig.get().general.enableUpdateNotification){
            new Thread(() -> {
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
            }).start();
        }
        return shouldUpdate;
    }

    public static void init(){
        if (shouldUpdate()){
            SkyblockJoinCallback.EVENT.register(() -> {
                TranslatableText linkMessage = new TranslatableText("skyblocker.update.update_message");
                TranslatableText linkMessageEnding = new TranslatableText("skyblocker.update.update_message_end");
                TranslatableText link = new TranslatableText("skyblocker.update.update_link");
                TranslatableText hoverText = new TranslatableText("skyblocker.update.hover_text");
                linkMessage.append(link.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/mod/skyblocker-liap/versions")).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)))).append(linkMessageEnding);

                MinecraftClient.getInstance().player.sendMessage(linkMessage, false);
                return ActionResult.PASS;
            });
        }
    }
}
