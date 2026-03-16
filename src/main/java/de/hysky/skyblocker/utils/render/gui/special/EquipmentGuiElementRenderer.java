package de.hysky.skyblocker.utils.render.gui.special;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.render.gui.state.EquipmentGuiElementRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.util.LightCoordsUtil;

public class EquipmentGuiElementRenderer<S> extends PictureInPictureRenderer<EquipmentGuiElementRenderState<S>> {

	private EquipmentGuiElementRenderer(PictureInPictureRendererRegistry.Context context) {
		super(context.bufferSource());
	}

	@Init
	public static void init() {
		PictureInPictureRendererRegistry.register(EquipmentGuiElementRenderer::new);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<EquipmentGuiElementRenderState<S>> getRenderStateClass() {
		return (Class<EquipmentGuiElementRenderState<S>>) (Object) EquipmentGuiElementRenderState.class;
	}

	@Override
	protected void renderToTexture(EquipmentGuiElementRenderState<S> state, PoseStack matrices) {
		Minecraft client = Minecraft.getInstance();

		matrices.pushPose();
		matrices.translate(0, state.offset() / state.scale(), 0);
		matrices.mulPose(Axis.XN.rotationDegrees(-5));
		matrices.mulPose(Axis.YN.rotationDegrees(state.rotation()));

		client.gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
		FeatureRenderDispatcher renderDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
		SubmitNodeStorage orderedRenderCommandQueueImpl = renderDispatcher.getSubmitNodeStorage();
		state.equipmentRenderer().renderLayers(
				state.layerType(),
				state.assetKey(),
				state.model(),
				state.state(),
				state.stack(),
				matrices,
				orderedRenderCommandQueueImpl,
				LightCoordsUtil.FULL_BRIGHT,
				0
		);

		renderDispatcher.renderAllFeatures();
		matrices.popPose();
	}

	@Override
	protected float getTranslateY(int height, int windowScaleFactor) {
		return height / 2;
	}

	@Override
	protected String getTextureLabel() {
		return "skyblocker equipment renderer";
	}
}
