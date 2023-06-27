package me.xmrvizzy.skyblocker.skyblock.dungeon;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import me.xmrvizzy.skyblocker.chat.ChatFilterResult;
import me.xmrvizzy.skyblocker.chat.ChatPatternListener;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Trivia extends ChatPatternListener {
    private static final Map<String, String[]> answers;
    private List<String> solutions = Collections.emptyList();

    public Trivia() {
        super("^ +(?:([A-Za-z,' ]*\\?)|§6 ([ⓐⓑⓒ]) §a([a-zA-Z0-9 ]+))$");
    }

    @Override
    public ChatFilterResult state() {
        return SkyblockerConfig.get().locations.dungeons.solveTrivia ? ChatFilterResult.FILTER : ChatFilterResult.PASS;
    }

    @Override
    public boolean onMatch(Text message, Matcher matcher) {
        String riddle = matcher.group(3);
        if (riddle != null) {
            if (!solutions.contains(riddle)) {
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null)
                    MinecraftClient.getInstance().player.sendMessage(Text.of("     " + Formatting.GOLD + matcher.group(2) + Formatting.RED + " " + riddle), false);
                return player != null;
            }
        } else
            updateSolutions(matcher.group(0));
        return false;
    }

    private void updateSolutions(String question) {
        String trimmedQuestion = question.trim();
        if (trimmedQuestion.equals("What SkyBlock year is it?")) {
            long currentTime = System.currentTimeMillis() / 1000L;
            long diff = currentTime - 1560276000;
            int year = (int) (diff / 446400 + 1);
            solutions = Collections.singletonList("Year " + year);
        } else {
            solutions = Arrays.asList(answers.get(trimmedQuestion));
        }
    }

    static {
        answers = new HashMap<>();
        answers.put("What is the status of The Watcher?", new String[]{"Stalker"});
        answers.put("What is the status of Bonzo?", new String[]{"New Necromancer"});
        answers.put("What is the status of Scarf?", new String[]{"Apprentice Necromancer"});
        answers.put("What is the status of The Professor?", new String[]{"Professor"});
        answers.put("What is the status of Thorn?", new String[]{"Shaman Necromancer"});
        answers.put("What is the status of Livid?", new String[]{"Master Necromancer"});
        answers.put("What is the status of Sadan?", new String[]{"Necromancer Lord"});
        answers.put("What is the status of Maxor?", new String[]{"The Wither Lords"});
        answers.put("What is the status of Goldor?", new String[]{"The Wither Lords"});
        answers.put("What is the status of Storm?", new String[]{"The Wither Lords"});
        answers.put("What is the status of Necron?", new String[]{"The Wither Lords"});
        answers.put("What is the status of Maxor, Storm, Goldor and Necron?", new String[]{"The Wither Lords"});
        answers.put("How many total Fairy Souls are there?", new String[]{"242 Fairy Souls"});
        answers.put("How many Fairy Souls are there in Spider's Den?", new String[]{"19 Fairy Souls"});
        answers.put("How many Fairy Souls are there in The End?", new String[]{"12 Fairy Souls"});
        answers.put("How many Fairy Souls are there in The Farming Islands?", new String[]{"20 Fairy Souls"});
        answers.put("How many Fairy Souls are there in Crimson Isle?", new String[]{"29 Fairy Souls"});
        answers.put("How many Fairy Souls are there in The Park?", new String[]{"11 Fairy Souls"});
        answers.put("How many Fairy Souls are there in Jerry's Workshop?", new String[]{"5 Fairy Souls"});
        answers.put("How many Fairy Souls are there in Hub?", new String[]{"79 Fairy Souls"});
        answers.put("How many Fairy Souls are there in The Hub?", new String[]{"79 Fairy Souls"});
        answers.put("How many Fairy Souls are there in Deep Caverns?", new String[]{"21 Fairy Souls"});
        answers.put("How many Fairy Souls are there in Gold Mine?", new String[]{"12 Fairy Souls"});
        answers.put("How many Fairy Souls are there in Dungeon Hub?", new String[]{"7 Fairy Souls"});
        answers.put("Which brother is on the Spider's Den?", new String[]{"Rick"});
        answers.put("What is the name of Rick's brother?", new String[]{"Pat"});
        answers.put("What is the name of the Painter in the Hub?", new String[]{"Marco"});
        answers.put("What is the name of the person that upgrades pets?", new String[]{"Kat"});
        answers.put("What is the name of the lady of the Nether?", new String[]{"Elle"});
        answers.put("Which villager in the Village gives you a Rogue Sword?", new String[]{"Jamie"});
        answers.put("How many unique minions are there?", new String[]{"59 Minions"});
        answers.put("Which of these enemies does not spawn in the Spider's Den?", new String[]{"Zombie Spider", "Cave Spider", "Wither Skeleton",
                "Dashing Spooder", "Broodfather", "Night Spider"});
        answers.put("Which of these monsters only spawns at night?", new String[]{"Zombie Villager", "Ghast"});
        answers.put("Which of these is not a dragon in The End?", new String[]{"Zoomer Dragon", "Weak Dragon", "Stonk Dragon", "Holy Dragon", "Boomer Dragon",
                "Booger Dragon", "Older Dragon", "Elder Dragon", "Stable Dragon", "Professor Dragon"});
    }
}
