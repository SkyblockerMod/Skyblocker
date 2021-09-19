package me.xmrvizzy.skyblocker.skyblock.dungeon;

import me.xmrvizzy.skyblocker.chat.ChatListener;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ThreeWeirdos extends ChatListener {
    public ThreeWeirdos() {
        super("^§e\\[NPC] §c([A-Z][a-z]+)§f: (?:The reward is(?: not in my chest!|n't in any of our chests\\.)|My chest (?:doesn't have the reward\\. We are all telling the truth\\.|has the reward and I'm telling the truth!)|At least one of them is lying, and the reward is not in §c§c[A-Z][a-z]+'s §rchest\\!|Both of them are telling the truth\\. Also, §c§c[A-Z][a-z]+ §rhas the reward in their chest\\!)$");
    }

    @Override
    public boolean isEnabled() {
        return SkyblockerConfig.get().locations.dungeons.solveThreeWeirdos;
    }

    @Override
    public boolean onMessage(String[] groups) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.world != null;
        assert client.player != null;
        client.world.getEntitiesByClass(
                ArmorStandEntity.class,
                client.player.getBoundingBox().expand(3),
                entity -> {
                    Text customName = entity.getCustomName();
                    if (customName != null && customName.getString().equals(groups[1])) {
                        return true;
                    }
                    return false;
                }
        ).forEach(
                entity -> entity.setCustomName(Text.of(Formatting.GREEN + groups[1]))
        );
        return false;
    }
}
