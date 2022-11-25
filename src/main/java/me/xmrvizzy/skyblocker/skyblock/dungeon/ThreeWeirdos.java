package me.xmrvizzy.skyblocker.skyblock.dungeon;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.chat.ChatFilterResult;
import me.xmrvizzy.skyblocker.chat.ChatPatternListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;

public class ThreeWeirdos extends ChatPatternListener {
    public ThreeWeirdos() {
        super("^§e\\[NPC] §c([A-Z][a-z]+)§f: (?:The reward is(?: not in my chest!|n't in any of our chests\\.)|My chest (?:doesn't have the reward\\. We are all telling the truth\\.|has the reward and I'm telling the truth!)|At least one of them is lying, and the reward is not in §c§c[A-Z][a-z]+'s §rchest\\!|Both of them are telling the truth\\. Also, §c§c[A-Z][a-z]+ §rhas the reward in their chest\\!)$");
    }

    @Override
    public ChatFilterResult state() {
        return SkyblockerMod.getInstance().CONFIG.dungeons.solveThreeWeirdos() ? null : ChatFilterResult.PASS;
    }

    @Override
    public boolean onMatch(Text message, Matcher matcher) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return false;
        client.world.getEntitiesByClass(
                ArmorStandEntity.class,
                client.player.getBoundingBox().expand(3),
                entity -> {
                    Text customName = entity.getCustomName();
                    return customName != null && customName.getString().equals(matcher.group(1));
                }
        ).forEach(
                entity -> entity.setCustomName(Text.of(Formatting.GREEN + matcher.group(1)))
        );
        return false;
    }
}
