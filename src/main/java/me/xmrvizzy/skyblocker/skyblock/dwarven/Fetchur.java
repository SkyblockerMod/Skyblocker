package me.xmrvizzy.skyblocker.skyblock.dwarven;

import me.xmrvizzy.skyblocker.chat.ChatListener;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.HashMap;
import java.util.Map;

public class Fetchur extends ChatListener {
    private static Map<String, String> answers;

    @Override
    public boolean isEnabled() {
        return SkyblockerConfig.get().locations.dwarvenMines.solveFetchur;
    }

    @Override
    public boolean onMessage(String message) {
        if (message.contains("§e[NPC] Fetchur§f: its") || message.contains("§e[NPC] Fetchur§f: theyre")) {
            message = message.replace("§e[NPC] Fetchur§f: its ", "")
                    .replace("§e[NPC] Fetchur§f: theyre ", "");

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) {
                throw new RuntimeException("[Skyblocker] client.player cannot be null!");
            }

            String answer = answers.getOrDefault(message, message);
            client.player.sendMessage(Text.of("§e[NPC] Fetchur§f: §2" + answer), false);
            return true;
        }
        return false;
    }

    static {
        answers = new HashMap<>();
        answers.put("red and soft", new TranslatableText("block.minecraft.red_wool").getString());
        answers.put("yellow and see through", new TranslatableText("block.minecraft.yellow_stained_glass").getString());
        answers.put("circular and sometimes moves", new TranslatableText("item.minecraft.compass").getString());
        answers.put("expensive minerals", "Mithril");
        answers.put("useful during celebrations", new TranslatableText("item.minecraft.firework_rocket").getString());
        answers.put("hot and gives energy", "Cheap/Decent Coffee");
        answers.put("tall and can be opened", new TranslatableText("block.minecraft.oak_door").getString());
        answers.put("brown and fluffy", new TranslatableText("item.minecraft.rabbit_foot").getString());
        answers.put("explosive but more than usual", "Superboom TNT");
        answers.put("wearable and grows", new TranslatableText("block.minecraft.pumpkin").getString());
        answers.put("shiny and makes sparks", new TranslatableText("item.minecraft.flint_and_steel").getString());
        answers.put("red and white and you can mine it", new TranslatableText("block.minecraft.nether_quartz_ore").getString());
        answers.put("round and green, or purple", new TranslatableText("item.minecraft.ender_pearl").getString());
    }
}