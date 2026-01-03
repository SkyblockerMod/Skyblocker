package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Gui.class)
public interface GuiInvoker {

	@Accessor
	@Nullable
	Component getOverlayMessageString();

	@Invoker("displayScoreboardSidebar")
	void skyblocker$renderSidebar(GuiGraphics context, Objective objective);
}
