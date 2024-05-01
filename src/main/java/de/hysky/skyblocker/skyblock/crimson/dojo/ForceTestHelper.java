package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class ForceTestHelper {

    private static final DecimalFormat FORMATTER = new DecimalFormat("0.0");

    private static final Map<ZombieEntity, Long> zombies = new HashMap<>();

    protected static void reset() {
        zombies.clear();
    }

    protected static boolean shouldGlow(String name) {
        if (name == null) return false;
        return name.contains("-");
    }

    protected static int getColor() {
        return Color.RED.getRGB();
    }

    protected static void onEntitySpawn(Entity entity) {
        if (entity instanceof ZombieEntity zombie) {
            zombies.put(zombie, System.currentTimeMillis() + 10100); //they last for 10100 millis ish so this is the time they despawn
        }
    }

    protected static void onEntityAttacked(Entity entity) {
        if (entity instanceof ZombieEntity zombie) {
            if (zombies.containsKey(zombie)) {
                zombies.put(zombie, System.currentTimeMillis() + 10100); //timer is reset when they are hit
            }
        }
    }

    protected static void onEntityDespawn(Entity entity) {
        if (entity instanceof ZombieEntity zombie) {
            zombies.remove(zombie);
        }
    }

    protected static void render(WorldRenderContext context) {
        //render times
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<ZombieEntity, Long> zombie : zombies.entrySet()) {
            float secondsTime = Math.max((zombie.getValue() - currentTime) / 1000f, 0);

            Text text;
            if (secondsTime > 1) {
                text = Text.literal(FORMATTER.format(secondsTime)).formatted(Formatting.GREEN);
            } else if (secondsTime > 0) {
                text = Text.literal(FORMATTER.format(secondsTime)).formatted(Formatting.YELLOW);
            } else {
                text = Text.literal("Soon").formatted(Formatting.RED); //todo translate
            }

            Vec3d lablePos = zombie.getKey().getEyePos();
            RenderHelper.renderText(context, text, lablePos, 1.5f, true);
        }
    }
}
