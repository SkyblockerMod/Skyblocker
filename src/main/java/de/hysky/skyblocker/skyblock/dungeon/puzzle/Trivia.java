package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.waypoint.FairySouls;
import de.hysky.skyblocker.utils.SkyblockTime;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import java.util.*;
import java.util.regex.Matcher;

public class Trivia extends ChatPatternListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, String[]> answers;
    private List<String> solutions = Collections.emptyList();

    public Trivia() {
        //FIXME I think its worth replacing this with something less fragile and is capable of handing multiple lines
        //perhaps manual incremental reading based off the start of a question
        super("^ +(?:([A-Za-z,' ]*\\?)| ([ⓐⓑⓒ]) ([a-zA-Z0-9 ]+))$");
    }

    @Override
    public ChatFilterResult state() {
        return SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveTrivia ? ChatFilterResult.FILTER : ChatFilterResult.PASS;
    }

    @Override
    public boolean onMatch(Text message, Matcher matcher) {
        String riddle = matcher.group(3);
        if (riddle != null) {
            if (!solutions.contains(riddle)) {
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null)
                    Utils.sendMessageToBypassEvents(Text.of("     " + Formatting.GOLD + matcher.group(2) + Formatting.RED + " " + riddle));
                return player != null;
            }
        } else updateSolutions(matcher.group(0));
        return false;
    }

    private void updateSolutions(String question) {
        try {
            String trimmedQuestion = question.trim();
            if (trimmedQuestion.equals("What SkyBlock year is it?")) {
                int year = SkyblockTime.skyblockYear.get();
                solutions = Collections.singletonList("Year " + year);
            } else {
                String[] questionAnswers = answers.get(trimmedQuestion);
                if (questionAnswers != null) solutions = Arrays.asList(questionAnswers);
            }
        } catch (Exception e) { //Hopefully the solver doesn't go south
            LOGGER.error("[Skyblocker] Failed to update the Trivia puzzle answers!", e);
        }
    }

    static {
        answers = Collections.synchronizedMap(new HashMap<>());
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
        answers.put("What is the status of Maxor, Storm, Goldor, and Necron?", new String[]{"The Wither Lords"});
        answers.put("Which brother is on the Spider's Den?", new String[]{"Rick"});
        answers.put("What is the name of Rick's brother?", new String[]{"Pat"});
        //Full Question: "What is the name of the vendor in the Hub who sells stained glass?"
        //The solver cannot handle multiple lines right now and just sees "glass?" as the question
        answers.put("glass?", new String[]{"Wool Weaver"});
        answers.put("What is the name of the person that upgrades pets?", new String[]{"Kat"});
        answers.put("What is the name of the lady of the Nether?", new String[]{"Elle"});
        answers.put("Which villager in the Village gives you a Rogue Sword?", new String[]{"Jamie"});
        answers.put("How many unique minions are there?", new String[]{"59 Minions"});
        answers.put("Which of these enemies does not spawn in the Spider's Den?", new String[]{"Zombie Spider", "Cave Spider", "Wither Skeleton", "Dashing Spooder", "Broodfather", "Night Spider"});
        answers.put("Which of these monsters only spawns at night?", new String[]{"Zombie Villager", "Ghast"});
        answers.put("Which of these is not a dragon in The End?", new String[]{"Zoomer Dragon", "Weak Dragon", "Stonk Dragon", "Holy Dragon", "Boomer Dragon", "Booger Dragon", "Older Dragon", "Elder Dragon", "Stable Dragon", "Professor Dragon"});
        FairySouls.runAsyncAfterFairySoulsLoad(() -> {
            answers.put("How many total Fairy Souls are there?", getFairySoulsSizeString(null));
            answers.put("How many Fairy Souls are there in Spider's Den?", getFairySoulsSizeString("combat_1"));
            answers.put("How many Fairy Souls are there in The End?", getFairySoulsSizeString("combat_3"));
            answers.put("How many Fairy Souls are there in The Farming Islands?", getFairySoulsSizeString("farming_1"));
            answers.put("How many Fairy Souls are there in Crimson Isle?", getFairySoulsSizeString("crimson_isle"));
            answers.put("How many Fairy Souls are there in The Park?", getFairySoulsSizeString("foraging_1"));
            answers.put("How many Fairy Souls are there in Jerry's Workshop?", getFairySoulsSizeString("winter"));
            answers.put("How many Fairy Souls are there in Hub?", getFairySoulsSizeString("hub"));
            answers.put("How many Fairy Souls are there in The Hub?", getFairySoulsSizeString("hub"));
            answers.put("How many Fairy Souls are there in Deep Caverns?", getFairySoulsSizeString("mining_2"));
            answers.put("How many Fairy Souls are there in Gold Mine?", getFairySoulsSizeString("mining_1"));
            answers.put("How many Fairy Souls are there in Dungeon Hub?", getFairySoulsSizeString("dungeon_hub"));
        });
    }

    @NotNull
    private static String[] getFairySoulsSizeString(@Nullable String location) {
        return new String[]{"%d Fairy Souls".formatted(FairySouls.getFairySoulsSize(location))};
    }
}
