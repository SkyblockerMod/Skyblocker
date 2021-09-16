package me.xmrvizzy.skyblocker.skyblock.dungeon;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ThreeWeirdos {
    public static void process(String message) {
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
}
