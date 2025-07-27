package de.hysky.skyblocker.utils.render.gui.state;

import de.hysky.skyblocker.utils.render.gui.special.InstancedGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.VertexConsumerProvider;

public interface InstancedGuiElementRenderState extends SpecialGuiElementRenderState {
	InstancedGuiElementRenderer<? extends InstancedGuiElementRenderState> newRenderer(VertexConsumerProvider.Immediate vertexConsumers);
}
