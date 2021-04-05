package me.xmrvizzy.skyblocker.skyblock.dungeon;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DungeonPuzzles {

    public static void threeWeirdos(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null && client.world == null) return;

        String[] solutions = {
                "The reward is not in my chest!",
                "At least one of them is lying, and the reward is not in",
                "My chest doesn't have the reward. We are all telling the truth.",
                "My chest has the reward and I'm telling the truth!",
                "The reward isn't in any of our chests.",
                "Both of them are telling the truth. Also,"
        };

        for (String s : solutions) {
            if (message.contains(s)) {
                String npc = message.substring(message.indexOf("]") + 4, message.indexOf(":") - 2);
                client.world.getEntitiesByClass(ArmorStandEntity.class, client.player.getBoundingBox().expand(3), entity -> {
                    if (entity.hasCustomName() && entity.getCustomName().getString().contains(npc))
                        return true;
                    return false;
                }).forEach(entity -> entity.setCustomName(Text.of(Formatting.GREEN + npc)));
            }
        }
    }
    public static String[] triviaAnswers = null;
    public static void trivia(String message, CallbackInfo ci){
        MinecraftClient client = MinecraftClient.getInstance();
        
      
        if (client.player == null && client.world == null) return;
        if (message.contains("What SkyBlock year is it?")) {
            double currentTime = System.currentTimeMillis() /1000L;

            double diff = Math.floor(currentTime - 1560276000);

            int year = (int) (diff / 446400 + 1);
            triviaAnswers = new String[]{"Year " + year};
        }else{
            Map<String, String[]> solutions = new HashMap<>();
            solutions.put("What is the status of The Watcher?", new String[]{"Stalker"});
            solutions.put("What is the status of Bonzo?", new String[]{"New Necromancer"});
            solutions.put("What is the status of Scarf?", new String[]{"Apprentice Necromancer"});
            solutions.put("What is the status of The Professor?", new String[]{"Professor"});
            solutions.put("What is the status of Thorn?", new String[]{"Shaman Necromancer"});
            solutions.put("What is the status of Livid?", new String[]{"Master Necromancer"});
            solutions.put("What is the status of Sadan?", new String[]{"Necromancer Lord"});
            solutions.put("What is the status of Maxor?", new String[]{"Young Wither"});
            solutions.put("What is the status of Goldor?", new String[]{"Wither Soldier"});
            solutions.put("What is the status of Storm?", new String[]{"Elementalist"});
            solutions.put("What is the status of Necron?", new String[]{"Wither Lord"});
            solutions.put("How many total Fairy Souls are there?", new String[]{"220 Fairy Souls"});
            solutions.put("How many Fairy Souls are there in Spider's Den?", new String[]{"19 Fairy Souls"});
            solutions.put("How many Fairy Souls are there in The End?", new String[]{"12 Fairy Souls"});
            solutions.put("How many Fairy Souls are there in The Barn?", new String[]{"7 Fairy Souls"});
            solutions.put("How many Fairy Souls are there in Mushroom Desert?", new String[]{"8 Fairy Souls"});
            solutions.put("How many Fairy Souls are there in Blazing Fortress?", new String[]{"19 Fairy Souls"});
            solutions.put("How many Fairy Souls are there in The Park?", new String[]{"11 Fairy Souls"});
            solutions.put("How many Fairy Souls are there in Jerry's Workshop?", new String[]{"5 Fairy Souls"});
            solutions.put("How many Fairy Souls are there in Hub?", new String[]{"79 Fairy Souls"});
            solutions.put("How many Fairy Souls are there in The Hub?", new String[]{"79 Fairy Souls"});
            solutions.put("How many Fairy Souls are there in Deep Caverns?", new String[]{"21 Fairy Souls"});
            solutions.put("How many Fairy Souls are there in Gold Mine?", new String[]{"12 Fairy Souls"});
            solutions.put("How many Fairy Souls are there in Dungeon Hub?", new String[]{"7 Fairy Souls"});
            solutions.put("Which brother is on the Spider's Den?", new String[]{"Rick"});
            solutions.put("What is the name of Rick's brother?", new String[]{"Pat"});
            solutions.put("What is the name of the Painter in the Hub?", new String[]{"Marco"});
            solutions.put("What is the name of the person that upgrades pets?", new String[]{"Kat"});
            solutions.put("What is the name of the lady of the Nether?", new String[]{"Elle"});
            solutions.put("Which villager in the Village gives you a Rogue Sword?", new String[]{"Jamie"});
            solutions.put("How many unique minions are there?", new String[]{"53 Minions"});
            solutions.put("Which of these enemies does not spawn in the Spider's Den?", new String[]{"Zombie Spider", "Cave Spider", "Wither Skeleton",
                    "Dashing Spooder", "Broodfather", "Night Spider"});
            solutions.put("Which of these monsters only spawns at night?", new String[]{"Zombie Villager", "Ghast"});
            solutions.put("Which of these is not a dragon in The End?", new String[]{"Zoomer Dragon", "Weak Dragon", "Stonk Dragon", "Holy Dragon", "Boomer Dragon",
                    "Booger Dragon", "Older Dragon", "Elder Dragon", "Stable Dragon", "Professor Dragon"});
            
            for (String question : solutions.keySet()) {
                if (message.contains(question)) {
                    triviaAnswers = solutions.get(question);
                    break;
                }
            }
        }

        if (triviaAnswers != null && (message.contains("ⓐ") || message.contains("ⓑ") || message.contains("ⓒ"))) {
            boolean isSolution = false;
            for (String solution : triviaAnswers) {
                if (message.contains(solution)) {
                    //client.player.sendMessage(new LiteralText(solution), false);
                    isSolution = true;
                    break;
                }
            }
            if (!isSolution) {
                char letter = message.charAt(5);
                String option = message.substring(6);
                ci.cancel();
                client.player.sendMessage(new LiteralText("     " + Formatting.GOLD + letter + Formatting.RED + option), false);
                return;
            }
        }
    }
}