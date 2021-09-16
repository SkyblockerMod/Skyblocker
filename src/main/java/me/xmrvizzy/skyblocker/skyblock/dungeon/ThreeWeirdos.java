package me.xmrvizzy.skyblocker.skyblock.dungeon;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThreeWeirdos {

    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Pattern answer = Pattern.compile("^§e\\[NPC] §c([A-Z][a-z]+)§f: (?:The reward is(?: not in my chest!|n't in any of our chests\\.)|My chest (?:doesn't have the reward we are all telling the truth\\.|has the reward and I'm telling the truth!)|At least one of them is lying, and the reward is not in [A-Z][a-z]+'s chest\\.|Both of them are telling the truth\\. Also, [A-Z][a-z]+ has the reward in their chest\\.)$");

    public static void process(String message) {
        assert client.player != null;
        assert client.world != null;

        Matcher matcher = answer.matcher(message);
        if (!matcher.matches())
            return;

        String npcName = matcher.group(1);
        client.world.getEntitiesByClass(
            ArmorStandEntity.class,
            client.player.getBoundingBox().expand(3),
            entity -> entity.hasCustomName() && entity.getCustomName().getString().contains(npcName)
        ).forEach(
            entity -> entity.setCustomName(Text.of(Formatting.GREEN + npcName))
        );
    }
}
