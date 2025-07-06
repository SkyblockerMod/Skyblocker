package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(InGameHud.class)
public interface InGameHudInvoker {

	@Accessor
	@Nullable
	Text getOverlayMessage();

    @Invoker("renderScoreboardSidebar")
    void skyblocker$renderSidebar(DrawContext context, ScoreboardObjective objective);
}
