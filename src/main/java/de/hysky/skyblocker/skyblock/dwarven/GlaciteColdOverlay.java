package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

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
        ClientReceiveMessageEvents.ALLOW_GAME.register(GlaciteColdOverlay::coldReset);
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, POWDER_SNOW_OUTLINE, (context, tickCounter) -> render(context));
    }

    private static boolean coldReset(Text text, boolean b) {
        if (!Utils.isInDwarvenMines() || b) {
            return true;
        }
        String message = text.getString();
        if (message.equals("The warmth of the campfire reduced your ❄ Cold to 0!")) {
            cold = 0;
            resetTime = System.currentTimeMillis();
        }

        return true;
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

    /**
     * @see InGameHud#renderOverlay as this is a carbon copy of it
     */
    private static void renderOverlay(DrawContext context, Identifier texture, float opacity) {
		int white = ColorHelper.getWhite(opacity);
		context.drawTexture(
			RenderPipelines.GUI_TEXTURED,
			texture,
			0,
			0,
			0.0F,
			0.0F,
			context.getScaledWindowWidth(),
			context.getScaledWindowHeight(),
			context.getScaledWindowWidth(),
			context.getScaledWindowHeight(),
			white
		);
    }

    public static void render(DrawContext context) {
        if (Utils.isInDwarvenMines() && SkyblockerConfigManager.get().mining.glacite.coldOverlay) {
            renderOverlay(context, POWDER_SNOW_OUTLINE, cold / 100f);
        }
    }
}
