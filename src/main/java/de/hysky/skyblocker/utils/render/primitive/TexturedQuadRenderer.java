package de.hysky.skyblocker.utils.render.primitive;

import org.joml.Matrix4f;
import com.mojang.blaze3d.vertex.BufferBuilder;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.Renderer;
import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.TexturedQuadRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.CameraRenderState;

public final class TexturedQuadRenderer implements PrimitiveRenderer<TexturedQuadRenderState> {
	protected static final TexturedQuadRenderer INSTANCE = new TexturedQuadRenderer();

	private TexturedQuadRenderer() {}

	@Override
	public void submitPrimitives(TexturedQuadRenderState state, CameraRenderState cameraState) {
		TextureSetup textureSetup = RenderHelper.singleTexture(Minecraft.getInstance().getTextureManager().getTexture(state.texture).getTextureView());
		BufferBuilder buffer = Renderer.getBuffer(state.throughWalls ? SkyblockerRenderPipelines.TEXTURE_THROUGH_WALLS : SkyblockerRenderPipelines.TEXTURE, textureSetup);
		Matrix4f positionMatrix = new Matrix4f()
				.translate((float) (state.pos.x() - cameraState.pos.x()), (float) (state.pos.y() - cameraState.pos.y()), (float) (state.pos.z() - cameraState.pos.z()))
				.rotate(cameraState.orientation);

		buffer.addVertex(positionMatrix, (float) state.renderOffset.x(), (float) state.renderOffset.y(), (float) state.renderOffset.z()).setUv(1, 1 - state.textureHeight).setColor(state.shaderColour[0], state.shaderColour[1], state.shaderColour[2], state.alpha);
		buffer.addVertex(positionMatrix, (float) state.renderOffset.x(), (float) state.renderOffset.y() + state.height, (float) state.renderOffset.z()).setUv(1, 1).setColor(state.shaderColour[0], state.shaderColour[1], state.shaderColour[2], state.alpha);
		buffer.addVertex(positionMatrix, (float) state.renderOffset.x() + state.width, (float) state.renderOffset.y() + state.height, (float) state.renderOffset.z()).setUv(1 - state.textureWidth, 1).setColor(state.shaderColour[0], state.shaderColour[1], state.shaderColour[2], state.alpha);
		buffer.addVertex(positionMatrix, (float) state.renderOffset.x() + state.width, (float) state.renderOffset.y(), (float) state.renderOffset.z()).setUv(1 - state.textureWidth, 1 - state.textureHeight).setColor(state.shaderColour[0], state.shaderColour[1], state.shaderColour[2], state.alpha);
	}
}
