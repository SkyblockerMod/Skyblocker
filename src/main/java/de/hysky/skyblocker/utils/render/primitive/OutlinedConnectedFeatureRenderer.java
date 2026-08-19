package de.hysky.skyblocker.utils.render.primitive;

import java.util.List;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.BlockSide;
import de.hysky.skyblocker.utils.render.state.OutlinedConnectedRenderState;

public class OutlinedConnectedFeatureRenderer extends PrimitiveFeatureRenderer<OutlinedConnectedFeatureRenderer.Submit> {
	public static final FeatureRendererType<OutlinedConnectedFeatureRenderer.Submit> TYPE = FeatureRendererType.create("Skyblocker Outlined Connected");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<OutlinedConnectedFeatureRenderer.Submit> submits) {
		for (Submit submit : submits) {
			Matrix4f positionMatrix = new Matrix4f()
					.translate((float) -submit.camera().pos.x, (float) -submit.camera().pos.y, (float) -submit.camera().pos.z);

			for (OutlinedConnectedRenderState state : submit.states()) {
				VertexConsumer builder = this.getVertexBuilder(submit.throughWalls() ? SkyblockerRenderPipelines.LINES_THROUGH_WALLS : RenderPipelines.LINES);
				float red = state.colourComponents()[0];
				float green = state.colourComponents()[1];
				float blue = state.colourComponents()[2];
				float alpha = state.alpha();
				for (BlockSide side : state.sides()) {
					// TODO!
				}
			}
		}
	}

	public record Submit(List<OutlinedConnectedRenderState> states, CameraRenderState camera, boolean throughWalls) implements SubmitNode {
		@Override
		public FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
