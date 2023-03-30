package me.xmrvizzy.skyblocker.skyblock.dwarven;

import me.xmrvizzy.skyblocker.chat.ChatFilterResult;
import me.xmrvizzy.skyblocker.chat.ChatPatternListener;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class Fetchur extends ChatPatternListener {
    private static final Map<String, String> answers;

    public Fetchur() {
        super("^§e\\[NPC] Fetchur§f: (?:its|theyre) ([a-zA-Z, \\-]*)$");
    }

    @Override
    public ChatFilterResult state() {
        return SkyblockerConfig.get().locations.dwarvenMines.solveFetchur ? ChatFilterResult.FILTER : ChatFilterResult.PASS;
    }

    @Override
    public boolean onMatch(Text message, Matcher matcher) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;
        String riddle = matcher.group(1);
        String answer = answers.getOrDefault(riddle, riddle);
        client.player.sendMessage(Text.of("§e[NPC] Fetchur§f: " + answer), false);
        return true;
    }

    static {
        answers = new HashMap<>();
        answers.put("red and soft", Text.translatable("block.minecraft.red_wool").getString());
        answers.put("yellow and see through", Text.translatable("block.minecraft.yellow_stained_glass").getString());
        answers.put("circular and sometimes moves", Text.translatable("item.minecraft.compass").getString());
        // TODO remove when typo fixed by hypixel
        answers.put("circlular and sometimes moves", Text.translatable("item.minecraft.compass").getString());
        answers.put("expensive minerals", "Mithril");
        answers.put("useful during celebrations", Text.translatable("item.minecraft.firework_rocket").getString());
        answers.put("hot and gives energy", "Cheap / Decent Coffee");
        answers.put("tall and can be opened", Text.translatable("block.minecraft.oak_door").getString());
        answers.put("brown and fluffy", Text.translatable("item.minecraft.rabbit_foot").getString());
        answers.put("explosive but more than usual", "Superboom TNT");
        answers.put("wearable and grows", Text.translatable("block.minecraft.pumpkin").getString());
        answers.put("shiny and makes sparks", Text.translatable("item.minecraft.flint_and_steel").getString());
        answers.put("red and white and you can mine it", Text.translatable("block.minecraft.nether_quartz_ore").getString());
        answers.put("round and green, or purple", Text.translatable("item.minecraft.ender_pearl").getString());
    }
}
