package de.hysky.skyblocker.utils.render.gui.special;

import de.hysky.skyblocker.utils.render.gui.state.EquipmentGuiElementRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public class EquipmentGuiElementRenderer extends InstancedGuiElementRenderer<EquipmentGuiElementRenderState> {

	public EquipmentGuiElementRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
		super(vertexConsumers);
	}

	@Override
	public Class<EquipmentGuiElementRenderState> getElementClass() {
		return EquipmentGuiElementRenderState.class;
	}

	@Override
	protected void render(EquipmentGuiElementRenderState state, MatrixStack matrices) {
		MinecraftClient client = MinecraftClient.getInstance();

		matrices.push();
		matrices.translate(0, state.offset() / state.scale(), 0);
		matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(-5));
		matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(state.rotation()));

		client.gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ENTITY_IN_UI);
		state.equipmentRenderer().render(
				state.layerType(),
				state.assetKey(),
				state.model(),
				state.stack(),
				matrices,
				this.vertexConsumers,
				LightmapTextureManager.MAX_LIGHT_COORDINATE
		);

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
