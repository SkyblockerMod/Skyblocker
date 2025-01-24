package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(InGameHud.class)
public interface InGameHudInvoker {

    @Invoker("renderScoreboardSidebar")
    void skyblocker$renderSidebar(DrawContext context, ScoreboardObjective objective);
}
