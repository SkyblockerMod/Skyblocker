package me.xmrvizzy.skyblocker.skyblock.dwarven;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Fetchur {

    public static Map<String, List<String>> getAnswers() {
        Map<String, List<String>> answers = new HashMap<>();
        answers.put("yellow, see-through", Arrays.asList(new TranslatableText("block.minecraft.yellow_stained_glass").getString()));
        answers.put("circular and sometimes moves", Arrays.asList(new TranslatableText("item.minecraft.compass").getString()));
        answers.put("circlular and sometimes moves", Arrays.asList(new TranslatableText("item.minecraft.compass").getString()));
        answers.put("expensive minerals", Arrays.asList("Mithril"));
        answers.put("useful during celebrations", Arrays.asList(new TranslatableText("item.minecraft.firework_rocket").getString()));
        answers.put("hot, gives energy", Arrays.asList("Cheap Coffee", "Decent Coffee"));
        answers.put("tall, can be opened", Arrays.asList(new TranslatableText("block.minecraft.oak_door").getString()));
        answers.put("explosive, more than usual", Arrays.asList("Superboom TNT"));
        answers.put("wearable, grows", Arrays.asList(new TranslatableText("block.minecraft.pumpkin").getString()));
        answers.put("shiny, makes sparks", Arrays.asList(new TranslatableText("item.minecraft.flint_and_steel").getString()));
        answers.put("red and white and you can mine it", Arrays.asList(new TranslatableText("block.minecraft.nether_quartz_ore").getString()));
        answers.put("round and green, or purple", Arrays.asList(new TranslatableText("item.minecraft.ender_pearl").getString()));
        answers.put("red and Soft", Arrays.asList(new TranslatableText("block.minecraft.red_wool").getString()));
        return answers;
    }

    public static void solve(String message, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        for (String key : getAnswers().keySet()) {
            if (message.contains(key)) {
                Text text = Text.of(message + " " + Formatting.GREEN + getAnswers().get(key).toString());
                client.player.sendMessage(text, false);
                ci.cancel();
                break;
            }
        }
    }
}