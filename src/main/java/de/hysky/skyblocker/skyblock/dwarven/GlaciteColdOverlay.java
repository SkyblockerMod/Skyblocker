package de.hysky.skyblocker.skyblock.dwarven;

import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlaciteColdOverlay {
    private static final Identifier POWDER_SNOW_OUTLINE = Identifier.ofVanilla("textures/misc/powder_snow_outline.png");
    private static final Pattern COLD_PATTERN = Pattern.compile("Cold: -(\\d+)❄");
    private static int cold = 0;
    private static long resetTime = System.currentTimeMillis();

    @Init
    public static void init() {
        Scheduler.INSTANCE.scheduleCyclic(GlaciteColdOverlay::update, 20);
        ClientReceiveMessageEvents.GAME.register(GlaciteColdOverlay::coldReset);
    }

    private static void coldReset(Text text, boolean b) {
        if (!Utils.isInDwarvenMines() || b) {
            return;
        }
        String message = text.getString();
        if (message.equals("The warmth of the campfire reduced your ❄ Cold to 0!")) {
            cold = 0;
            resetTime = System.currentTimeMillis();
        }
    }

    private static void update() {
        if (!Utils.isInDwarvenMines() || System.currentTimeMillis() - resetTime < 3000 || !SkyblockerConfigManager.get().mining.glacite.coldOverlay) {
            cold = 0;
            return;
        }
        for (String line : Utils.STRING_SCOREBOARD) {
            Matcher coldMatcher = COLD_PATTERN.matcher(line);
            if (coldMatcher.matches()) {
                String value = coldMatcher.group(1);
                cold = Integer.parseInt(value);
                return;
            }
        }
        cold = 0;
    }

    private static void renderOverlay(DrawContext context, Identifier texture, float opacity) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        context.setShaderColor(1.0f, 1.0f, 1.0f, opacity);
        context.drawTexture(texture, 0, 0, -90, 0.0f, 0.0f, context.getScaledWindowWidth(), context.getScaledWindowHeight(), context.getScaledWindowWidth(), context.getScaledWindowHeight());
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void render(DrawContext context) {
        if (Utils.isInDwarvenMines() && SkyblockerConfigManager.get().mining.glacite.coldOverlay) {
            renderOverlay(context, POWDER_SNOW_OUTLINE, cold / 100f);
        }
    }
}
