package de.hysky.skyblocker.utils.render.gui.special;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.render.gui.state.EquipmentGuiElementRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.command.RenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public class EquipmentGuiElementRenderer<S> extends SpecialGuiElementRenderer<EquipmentGuiElementRenderState<S>> {

	private EquipmentGuiElementRenderer(SpecialGuiElementRegistry.Context context) {
		super(context.vertexConsumers());
	}

	@Init
	public static void init() {
		SpecialGuiElementRegistry.register(EquipmentGuiElementRenderer::new);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class getElementClass() {
		return EquipmentGuiElementRenderState.class;
	}

	@Override
	protected void render(EquipmentGuiElementRenderState<S> state, MatrixStack matrices) {
		MinecraftClient client = MinecraftClient.getInstance();

		matrices.push();
		matrices.translate(0, state.offset() / state.scale(), 0);
		matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(-5));
		matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(state.rotation()));

		client.gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ENTITY_IN_UI);
		RenderDispatcher renderDispatcher = MinecraftClient.getInstance().gameRenderer.getEntityRenderDispatcher();
		OrderedRenderCommandQueueImpl orderedRenderCommandQueueImpl = renderDispatcher.getQueue();
		state.equipmentRenderer().render(
				state.layerType(),
				state.assetKey(),
				state.model(),
				state.state(),
				state.stack(),
				matrices,
				orderedRenderCommandQueueImpl,
				LightmapTextureManager.MAX_LIGHT_COORDINATE,
				0
		);

		renderDispatcher.render();
		matrices.pop();
	}

	@Override
	protected float getYOffset(int height, int windowScaleFactor) {
		return height / 2;
	}

	@Override
	protected String getName() {
		return "skyblocker equipment renderer";
	}
}
