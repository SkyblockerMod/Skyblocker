package de.hysky.skyblocker.utils.render.primitive;

import org.joml.Matrix4f;

import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.Renderer;
import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.CameraRenderState;
import de.hysky.skyblocker.utils.render.state.TexturedQuadRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.texture.TextureSetup;

public final class TexturedQuadRenderer implements PrimitiveRenderer<TexturedQuadRenderState> {
	protected static final TexturedQuadRenderer INSTANCE = new TexturedQuadRenderer();

	private TexturedQuadRenderer() {}

	@Override
	public void submitPrimitives(TexturedQuadRenderState state, CameraRenderState cameraState) {
		TextureSetup textureSetup = RenderHelper.singleTexture(MinecraftClient.getInstance().getTextureManager().getTexture(state.texture).getGlTextureView());
		BufferBuilder buffer = Renderer.getBuffer(state.throughWalls ? SkyblockerRenderPipelines.TEXTURE_THROUGH_WALLS : SkyblockerRenderPipelines.TEXTURE, textureSetup);
		Matrix4f positionMatrix = new Matrix4f()
				.translate((float) (state.pos.getX() - cameraState.pos.getX()), (float) (state.pos.getY() - cameraState.pos.getY()), (float) (state.pos.getZ() - cameraState.pos.getZ()))
				.rotate(cameraState.rotation);

		buffer.vertex(positionMatrix, (float) state.renderOffset.getX(), (float) state.renderOffset.getY(), (float) state.renderOffset.getZ()).texture(1, 1 - state.textureHeight).color(state.shaderColour[0], state.shaderColour[1], state.shaderColour[2], state.alpha);
		buffer.vertex(positionMatrix, (float) state.renderOffset.getX(), (float) state.renderOffset.getY() + state.height, (float) state.renderOffset.getZ()).texture(1, 1).color(state.shaderColour[0], state.shaderColour[1], state.shaderColour[2], state.alpha);
		buffer.vertex(positionMatrix, (float) state.renderOffset.getX() + state.width, (float) state.renderOffset.getY() + state.height, (float) state.renderOffset.getZ()).texture(1 - state.textureWidth, 1).color(state.shaderColour[0], state.shaderColour[1], state.shaderColour[2], state.alpha);
		buffer.vertex(positionMatrix, (float) state.renderOffset.getX() + state.width, (float) state.renderOffset.getY(), (float) state.renderOffset.getZ()).texture(1 - state.textureWidth, 1 - state.textureHeight).color(state.shaderColour[0], state.shaderColour[1], state.shaderColour[2], state.alpha);
	}
}
