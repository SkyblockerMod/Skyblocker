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
import de.hysky.skyblocker.utils.render.state.OutlinedBoxRenderState;

public class OutlinedBoxFeatureRenderer extends PrimitiveFeatureRenderer<OutlinedBoxFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("Skyblocker Outlined Box");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		for (Submit submit : submits) {
			Matrix4f positionMatrix = new Matrix4f()
					.translate((float) -submit.camera().pos.x, (float) -submit.camera().pos.y, (float) -submit.camera().pos.z);

			for (OutlinedBoxRenderState state : submit.states()) {
				VertexConsumer builder = this.getVertexBuilder(submit.throughWalls() ? SkyblockerRenderPipelines.LINES_THROUGH_WALLS : RenderPipelines.LINES);
				float minX = (float) state.minX();
				float minY = (float) state.minY();
				float minZ = (float) state.minZ();
				float maxX = (float) state.maxX();
				float maxY = (float) state.maxY();
				float maxZ = (float) state.maxZ();
				float red = state.colourComponents()[0];
				float green = state.colourComponents()[1];
				float blue = state.colourComponents()[2];
				float alpha = state.alpha();

				builder.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(-1.0f, 0.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(-1.0f, 0.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(0.0f, -1.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(0.0f, -1.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(0.0f, 0.0f, -1.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha).setNormal(0.0f, 0.0f, -1.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(state.lineWidth());
				builder.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(state.lineWidth());
			}
		}
	}

	public record Submit(List<OutlinedBoxRenderState> states, CameraRenderState camera, boolean throughWalls) implements SubmitNode {
		@Override
		public FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
