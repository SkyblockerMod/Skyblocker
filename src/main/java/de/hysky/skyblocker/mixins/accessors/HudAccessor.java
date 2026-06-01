package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Hud.class)
public interface HudAccessor {

	@Accessor
	@Nullable Component getOverlayMessageString();

	@Invoker("displayScoreboardSidebar")
	void extractSidebar(GuiGraphicsExtractor graphics, Objective objective);
}
