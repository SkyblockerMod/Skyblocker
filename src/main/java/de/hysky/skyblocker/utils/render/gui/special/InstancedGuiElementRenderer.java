package de.hysky.skyblocker.utils.render.gui.special;

import de.hysky.skyblocker.utils.render.gui.state.InstancedGuiElementRenderState;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.render.VertexConsumerProvider;

/**
 * Specialization of a {@code SpecialGuiElementRenderer} that allows for rendering multiple instances of the same element with
 * different parameters.
 */
public abstract class InstancedGuiElementRenderer<T extends InstancedGuiElementRenderState> extends SpecialGuiElementRenderer<T> {
	private boolean usedThisFrame;

	protected InstancedGuiElementRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
		super(vertexConsumers);
	}

	public final boolean usedThisFrame() {
		return this.usedThisFrame;
	}

	public final void resetUsedThisFrame() {
		this.usedThisFrame = false;
	}

	@Override
	public void renderElement(T specialGuiElementRenderState, GuiRenderState guiRenderState) {
		super.renderElement(specialGuiElementRenderState, guiRenderState);
		this.usedThisFrame = true;
	}
}
