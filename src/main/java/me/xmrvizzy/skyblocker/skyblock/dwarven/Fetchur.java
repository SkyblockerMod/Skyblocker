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

    public Fetchur() {
        super("^§e\\[NPC] Fetchur§f: (?:its|theyre) ([a-zA-Z, \\-]*)$");
    }

    @Override
    public boolean isEnabled() {
        return SkyblockerConfig.get().locations.dwarvenMines.solveFetchur;
    }

    @Override
    public boolean onMessage(String[] groups) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        String answer = answers.getOrDefault(groups[1], groups[1]);
        client.player.sendMessage(Text.of("§e[NPC] Fetchur§f: " + answer), false);
        return true;
    }

    static {
        //It seems that hints on wiki aren't up to date.
        //I will assume doors weren't the only ones changed
        //and that admins did it to make hints more consistent.
        //Will need to verify if this true.
        //Changed most commas into "and"s.
        answers = new HashMap<>();
        answers.put("yellow and see-through", new TranslatableText("block.minecraft.yellow_stained_glass").getString());
        answers.put("circular and sometimes moves", new TranslatableText("item.minecraft.compass").getString());
        answers.put("expensive minerals", "Mithril");
        answers.put("useful during celebrations", new TranslatableText("item.minecraft.firework_rocket").getString());
        answers.put("hot and gives energy", "Cheap / Decent Coffee");
        answers.put("tall and can be opened", new TranslatableText("block.minecraft.oak_door").getString());
        answers.put("explosive, more than usual", "Superboom TNT");
        answers.put("wearable and grows", new TranslatableText("block.minecraft.pumpkin").getString());
        answers.put("shiny and makes sparks", new TranslatableText("item.minecraft.flint_and_steel").getString());
        answers.put("red and white and you can mine it", new TranslatableText("block.minecraft.nether_quartz_ore").getString());
        answers.put("round and green, or purple", new TranslatableText("item.minecraft.ender_pearl").getString());
        answers.put("red and Soft", new TranslatableText("block.minecraft.red_wool").getString());
    }
}