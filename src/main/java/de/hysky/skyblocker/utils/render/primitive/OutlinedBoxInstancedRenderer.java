package de.hysky.skyblocker.utils.render.primitive;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.BufferBuilder;

import de.hysky.skyblocker.utils.render.OutlinedBoxDataUniform;
import de.hysky.skyblocker.utils.render.Renderer;
import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.OutlinedBoxRenderState;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public final class OutlinedBoxInstancedRenderer implements AutoCloseable {
	public static final OutlinedBoxInstancedRenderer INSTANCE = new OutlinedBoxInstancedRenderer();
	private static final AABB UNIT_BOX = new AABB(BlockPos.ZERO);
	private final OutlinedBoxDataUniform normalOutlinedBoxData = new OutlinedBoxDataUniform();
	private final OutlinedBoxDataUniform throughWallsOutlinedBoxData = new OutlinedBoxDataUniform();

	private OutlinedBoxInstancedRenderer() {}

	public void submitPrimitives(List<OutlinedBoxRenderState> states, CameraRenderState cameraState) {
		// Initialize to the highest possible capacity to avoid any resizing overhead
		List<OutlinedBoxRenderState> normalStates = new ArrayList<>(states.size());
		List<OutlinedBoxRenderState> throughWallsStates = new ArrayList<>(states.size());

		for (OutlinedBoxRenderState state : states) {
			if (state.throughWalls) {
				throughWallsStates.add(state);
			} else {
				normalStates.add(state);
			}
		}

		if (!normalStates.isEmpty()) {
			Renderer.UniformBinding normalUniform = new Renderer.UniformBinding("OutlinedBoxData", this.normalOutlinedBoxData.update(normalStates, cameraState));
			BufferBuilder normalBuffer = Renderer.getBuffer(SkyblockerRenderPipelines.OUTLINED_BOX_INSTANCED, TextureSetup.noTexture(), 1f, normalStates.size(), normalUniform);
			buildUnitBox((float) UNIT_BOX.minX, (float) UNIT_BOX.minY, (float) UNIT_BOX.minZ, (float) UNIT_BOX.maxX, (float) UNIT_BOX.maxY, (float) UNIT_BOX.maxZ, normalBuffer);
		}

		if (!throughWallsStates.isEmpty()) {
			Renderer.UniformBinding throughWallsUniform = new Renderer.UniformBinding("OutlinedBoxData", this.throughWallsOutlinedBoxData.update(throughWallsStates, cameraState));
			BufferBuilder throughWallsBuffer = Renderer.getBuffer(SkyblockerRenderPipelines.OUTLINED_BOX_THROUGH_WALLS_INSTANCED, TextureSetup.noTexture(), 1f, throughWallsStates.size(), throughWallsUniform);
			buildUnitBox((float) UNIT_BOX.minX, (float) UNIT_BOX.minY, (float) UNIT_BOX.minZ, (float) UNIT_BOX.maxX, (float) UNIT_BOX.maxY, (float) UNIT_BOX.maxZ, throughWallsBuffer);
		}
	}

	private static void buildUnitBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, BufferBuilder buffer) {
		buffer.addVertex(minX, minY, minZ).setNormal(1.0f, 0.0f, 0.0f);
		buffer.addVertex(maxX, minY, minZ).setNormal(1.0f, 0.0f, 0.0f);
		buffer.addVertex(minX, minY, minZ).setNormal(0.0f, 1.0f, 0.0f);
		buffer.addVertex(minX, maxY, minZ).setNormal(0.0f, 1.0f, 0.0f);
		buffer.addVertex(minX, minY, minZ).setNormal(0.0f, 0.0f, 1.0f);
		buffer.addVertex(minX, minY, maxZ).setNormal(0.0f, 0.0f, 1.0f);
		buffer.addVertex(maxX, minY, minZ).setNormal(0.0f, 1.0f, 0.0f);
		buffer.addVertex(maxX, maxY, minZ).setNormal(0.0f, 1.0f, 0.0f);
		buffer.addVertex(maxX, maxY, minZ).setNormal(-1.0f, 0.0f, 0.0f);
		buffer.addVertex(minX, maxY, minZ).setNormal(-1.0f, 0.0f, 0.0f);
		buffer.addVertex(minX, maxY, minZ).setNormal(0.0f, 0.0f, 1.0f);
		buffer.addVertex(minX, maxY, maxZ).setNormal(0.0f, 0.0f, 1.0f);
		buffer.addVertex(minX, maxY, maxZ).setNormal(0.0f, -1.0f, 0.0f);
		buffer.addVertex(minX, minY, maxZ).setNormal(0.0f, -1.0f, 0.0f);
		buffer.addVertex(minX, minY, maxZ).setNormal(1.0f, 0.0f, 0.0f);
		buffer.addVertex(maxX, minY, maxZ).setNormal(1.0f, 0.0f, 0.0f);
		buffer.addVertex(maxX, minY, maxZ).setNormal(0.0f, 0.0f, -1.0f);
		buffer.addVertex(maxX, minY, minZ).setNormal(0.0f, 0.0f, -1.0f);
		buffer.addVertex(minX, maxY, maxZ).setNormal(1.0f, 0.0f, 0.0f);
		buffer.addVertex(maxX, maxY, maxZ).setNormal(1.0f, 0.0f, 0.0f);
		buffer.addVertex(maxX, minY, maxZ).setNormal(0.0f, 1.0f, 0.0f);
		buffer.addVertex(maxX, maxY, maxZ).setNormal(0.0f, 1.0f, 0.0f);
		buffer.addVertex(maxX, maxY, minZ).setNormal(0.0f, 0.0f, 1.0f);
		buffer.addVertex(maxX, maxY, maxZ).setNormal(0.0f, 0.0f, 1.0f);
	}

	@Override
	public void close() {
		this.normalOutlinedBoxData.close();
		this.throughWallsOutlinedBoxData.close();
	}
}
