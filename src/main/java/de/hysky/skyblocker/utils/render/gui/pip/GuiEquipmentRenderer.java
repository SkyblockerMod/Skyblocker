package de.hysky.skyblocker.utils.render.gui.pip;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.render.state.gui.GuiEquipmentRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.util.LightCoordsUtil;

public class GuiEquipmentRenderer<S> extends PictureInPictureRenderer<GuiEquipmentRenderState<S>> {

	private GuiEquipmentRenderer(PictureInPictureRendererRegistry.Context context) {
		super(context.bufferSource());
	}

	@Init
	public static void init() {
		PictureInPictureRendererRegistry.register(GuiEquipmentRenderer::new);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<GuiEquipmentRenderState<S>> getRenderStateClass() {
		return (Class<GuiEquipmentRenderState<S>>) (Object) GuiEquipmentRenderState.class;
	}

	@Override
	protected void renderToTexture(GuiEquipmentRenderState<S> state, PoseStack matrices) {
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
	protected float getTranslateY(int height, int guiScale) {
		return height / 2;
	}

	@Override
	protected String getTextureLabel() {
		return "skyblocker equipment renderer";
	}
}
