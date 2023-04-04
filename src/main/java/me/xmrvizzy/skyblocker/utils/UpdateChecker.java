package me.xmrvizzy.skyblocker.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.util.VersionNumber;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {
    public static final Pattern pattern = Pattern.compile("v(.*)\\+.*");
    public static final Pattern localPattern = Pattern.compile("(.*)\\+.*");
    public static Matcher matcher;
    private static VersionNumber latestVersion = null;
    private static VersionNumber localVersion = null;
    public static void shouldUpdate(){
        if (!SkyblockerConfig.get().general.enableUpdateNotification) return;
        try {
            URI uri = new URI("https://api.modrinth.com/v2/project/skyblocker-liap/version");
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
            CompletableFuture<HttpResponse<String>> response = HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString());
            response.thenAccept(httpResponse -> {
                JsonObject versionJson = new Gson().fromJson(httpResponse.body(), JsonElement.class).getAsJsonArray().get(0).getAsJsonObject();
                matcher = pattern.matcher(versionJson.get("version_number").getAsString());
                if (matcher.find()) {
                    latestVersion = VersionNumber.parse(matcher.group(1));
                }
                matcher = localPattern.matcher(FabricLoader.getInstance().getModContainer(SkyblockerMod.NAMESPACE).get().getMetadata().getVersion().getFriendlyString());
                if (matcher.find()) {
                    localVersion = VersionNumber.parse(matcher.group(1));
                }
                if (latestVersion != null && localVersion != null) {
                    if (localVersion.compareTo(latestVersion) < 0) {
                        MutableText linkMessage = Text.translatable("skyblocker.update.update_message");
                        MutableText linkMessageEnding = Text.translatable("skyblocker.update.update_message_end");
                        MutableText link = Text.translatable("skyblocker.update.update_link");
                        MutableText hoverText = Text.translatable("skyblocker.update.hover_text");
                        linkMessage.append(link.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/mod/skyblocker-liap/versions")).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)))).append(linkMessageEnding);

                        MinecraftClient.getInstance().player.sendMessage(linkMessage, false);
                    }
                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void init(){
        SkyblockEvents.JOIN.register(UpdateChecker::shouldUpdate);
    }
}
