package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class Fetchur extends ChatPatternListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(Fetchur.class);

    private static final Map<String, String> answers;

    public Fetchur() {
        super("^\\[NPC] Fetchur: (?:its|theyre) ([a-zA-Z, \\-]*)$");
    }

    @Override
    public ChatFilterResult state() {
        return SkyblockerConfigManager.get().locations.dwarvenMines.solveFetchur ? ChatFilterResult.FILTER : ChatFilterResult.PASS;
    }

    @Override
    public boolean onMatch(Text message, Matcher matcher) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;
        LOGGER.info("Original Fetchur message: {}", message.getString());
        String riddle = matcher.group(1);
        String answer = answers.getOrDefault(riddle, riddle);
        client.player.sendMessage(Text.of("§e[NPC] Fetchur§f: " + answer), false);
        return true;
    }

    static {
        answers = new HashMap<>();

        answers.put("yellow and see through", Text.translatable("block.minecraft.yellow_stained_glass").getString());
        answers.put("circular and sometimes moves", Text.translatable("item.minecraft.compass").getString());
        answers.put("expensive minerals", "Mithril");
        answers.put("useful during celebrations", Text.translatable("item.minecraft.firework_rocket").getString());
        answers.put("hot and gives energy", "Cheap / Decent / Black Coffee");
        answers.put("tall and can be opened", String.format("%s / %s",
                Text.translatable("block.minecraft.oak_door").getString(),
                Text.translatable("block.minecraft.iron_door").getString()));
        answers.put("brown and fluffy", Text.translatable("item.minecraft.rabbit_foot").getString());
        answers.put("explosive but more than usual", "Superboom TNT");
        answers.put("wearable and grows", Text.translatable("block.minecraft.pumpkin").getString());
        answers.put("shiny and makes sparks", Text.translatable("item.minecraft.flint_and_steel").getString());
        answers.put("green and some dudes trade stuff for it", Text.translatable("item.minecraft.emerald").getString());
        answers.put("red and soft", Text.translatable("block.minecraft.red_wool").getString());
    }
}
