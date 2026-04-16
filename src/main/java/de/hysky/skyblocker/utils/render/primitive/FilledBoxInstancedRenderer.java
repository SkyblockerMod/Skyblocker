package de.hysky.skyblocker.utils.render.primitive;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.BufferBuilder;

import de.hysky.skyblocker.utils.render.BoxDataUniform;
import de.hysky.skyblocker.utils.render.Renderer;
import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.FilledBoxRenderState;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public final class FilledBoxInstancedRenderer {
	public static final FilledBoxInstancedRenderer INSTANCE = new FilledBoxInstancedRenderer();
	private static final AABB UNIT_BOX = new AABB(BlockPos.ZERO);
	private final BoxDataUniform normalBoxData = new BoxDataUniform();
	private final BoxDataUniform throughWallsBoxData = new BoxDataUniform();

	private FilledBoxInstancedRenderer() {}

	public void submitPrimitives(List<FilledBoxRenderState> states, CameraRenderState cameraState) {
		// Initialize to the highest possible capacity to avoid any resizing overhead
		List<FilledBoxRenderState> normalStates = new ArrayList<>(states.size());
		List<FilledBoxRenderState> throughWallsStates = new ArrayList<>(states.size());

		for (FilledBoxRenderState state : states) {
			if (state.throughWalls) {
				throughWallsStates.add(state);
			} else {
				normalStates.add(state);
			}
		}

		if (!normalStates.isEmpty()) {
			Renderer.UniformBinding normalUniform = new Renderer.UniformBinding("BoxData", this.normalBoxData.update(normalStates, cameraState));
			BufferBuilder normalBuffer = Renderer.getBuffer(SkyblockerRenderPipelines.FILLED_INSTANCED, TextureSetup.noTexture(), 1f, normalStates.size(), normalUniform);
			buildUnitBox((float) UNIT_BOX.minX, (float) UNIT_BOX.minY, (float) UNIT_BOX.minZ, (float) UNIT_BOX.maxX, (float) UNIT_BOX.maxY, (float) UNIT_BOX.maxZ, normalBuffer);
		}

		if (!throughWallsStates.isEmpty()) {
			Renderer.UniformBinding throughWallsUniform = new Renderer.UniformBinding("BoxData", this.throughWallsBoxData.update(throughWallsStates, cameraState));
			BufferBuilder throughWallsBuffer = Renderer.getBuffer(SkyblockerRenderPipelines.FILLED_THROUGH_WALLS_INSTANCED, TextureSetup.noTexture(), 1f, throughWallsStates.size(), throughWallsUniform);
			buildUnitBox((float) UNIT_BOX.minX, (float) UNIT_BOX.minY, (float) UNIT_BOX.minZ, (float) UNIT_BOX.maxX, (float) UNIT_BOX.maxY, (float) UNIT_BOX.maxZ, throughWallsBuffer);
		}
	}

	private static void buildUnitBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, BufferBuilder buffer) {
		// Front face
		buffer.addVertex(minX, minY, minZ);
		buffer.addVertex(maxX, minY, minZ);
		buffer.addVertex(maxX, maxY, minZ);
		buffer.addVertex(minX, maxY, minZ);

		// Back face
		buffer.addVertex(maxX, minY, maxZ);
		buffer.addVertex(minX, minY, maxZ);
		buffer.addVertex(minX, maxY, maxZ);
		buffer.addVertex(maxX, maxY, maxZ);

		// Left face
		buffer.addVertex(minX, minY, maxZ);
		buffer.addVertex(minX, minY, minZ);
		buffer.addVertex(minX, maxY, minZ);
		buffer.addVertex(minX, maxY, maxZ);

		// Right face
		buffer.addVertex(maxX, minY, minZ);
		buffer.addVertex(maxX, minY, maxZ);
		buffer.addVertex(maxX, maxY, maxZ);
		buffer.addVertex(maxX, maxY, minZ);

		// Top face
		buffer.addVertex(minX, maxY, minZ);
		buffer.addVertex(maxX, maxY, minZ);
		buffer.addVertex(maxX, maxY, maxZ);
		buffer.addVertex(minX, maxY, maxZ);

		// Bottom face
		buffer.addVertex(minX, minY, maxZ);
		buffer.addVertex(maxX, minY, maxZ);
		buffer.addVertex(maxX, minY, minZ);
		buffer.addVertex(minX, minY, minZ);
	}
}
