package de.hysky.skyblocker.utils.render.primitive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vulkan.VulkanDevice;

import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhases;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BeaconRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import de.hysky.skyblocker.mixins.accessors.BlockEntityRenderStateAccessor;
import de.hysky.skyblocker.mixins.accessors.GpuDeviceAccessor;
import de.hysky.skyblocker.utils.BlockPosSet;
import de.hysky.skyblocker.utils.render.FrustumUtils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.state.BlockHologramRenderState;
import de.hysky.skyblocker.utils.render.state.BlockSide;
import de.hysky.skyblocker.utils.render.state.CursorLineRenderState;
import de.hysky.skyblocker.utils.render.state.CylinderRenderState;
import de.hysky.skyblocker.utils.render.state.FilledBoxRenderState;
import de.hysky.skyblocker.utils.render.state.FilledCircleRenderState;
import de.hysky.skyblocker.utils.render.state.LinesRenderState;
import de.hysky.skyblocker.utils.render.state.OutlinedBoxRenderState;
import de.hysky.skyblocker.utils.render.state.OutlinedCircleRenderState;
import de.hysky.skyblocker.utils.render.state.OutlinedConnectedRenderState;
import de.hysky.skyblocker.utils.render.state.QuadRenderState;
import de.hysky.skyblocker.utils.render.state.SphereRenderState;
import de.hysky.skyblocker.utils.render.state.TextRenderState;
import de.hysky.skyblocker.utils.render.state.TexturedQuadRenderState;

public final class PrimitiveCollectorImpl implements PrimitiveCollector {
	private static final Minecraft MINECRAFT = Minecraft.getInstance();
	private static final int MAX_OVERWORLD_BUILD_HEIGHT = 319;
	private final boolean isVulkan;
	private final LevelRenderState worldState;
	private final Frustum frustum;
	private final List<VanillaSubmittable<?>> vanillaSubmittables = new ArrayList<>();
	private final List<FilledBoxRenderState> filledBoxStates = new ArrayList<>();
	private final List<FilledBoxRenderState> filledBoxThroughWallsStates = new ArrayList<>();
	private final List<OutlinedBoxRenderState> outlinedBoxStates = new ArrayList<>();
	private final List<OutlinedBoxRenderState> outlinedBoxThroughWallsStates = new ArrayList<>();
	private final List<OutlinedConnectedRenderState> outlinedConnectedStates = new ArrayList<>();
	private final List<OutlinedConnectedRenderState> outlinedConnectedThroughWallsStates = new ArrayList<>();
	private final List<LinesRenderState> linesStates = new ArrayList<>();
	private final List<LinesRenderState> linesThroughWallsStates = new ArrayList<>();
	private final List<CursorLineRenderState> cursorLineStates = new ArrayList<>();
	private final List<QuadRenderState> quadStates = new ArrayList<>();
	private final List<QuadRenderState> quadThroughWallsStates = new ArrayList<>();
	private final List<TexturedQuadRenderState> texturedQuadStates = new ArrayList<>();
	private final List<TexturedQuadRenderState> texturedQuadThroughWallsStates = new ArrayList<>();
	private final List<BlockHologramRenderState> blockHologramStates = new ArrayList<>();
	private final List<TextRenderState> textStates = new ArrayList<>();
	private final List<TextRenderState> textThroughWallsStates = new ArrayList<>();
	private final List<CylinderRenderState> cylinderStates = new ArrayList<>();
	private final List<SphereRenderState> sphereStates = new ArrayList<>();
	private final List<FilledCircleRenderState> filledCircleStates = new ArrayList<>();
	private final List<OutlinedCircleRenderState> outlinedCircleStates = new ArrayList<>();
	private boolean frozen = false;

	public PrimitiveCollectorImpl(LevelRenderState worldState, Frustum frustum) {
		this.isVulkan = ((GpuDeviceAccessor) RenderSystem.getDevice()).getBackend() instanceof VulkanDevice;
		this.worldState = worldState;
		this.frustum = frustum;
	}

	@Override
	public <S> void submitVanilla(S state, VanillaRenderer<S> renderer) {
		ensureNotFrozen();

		this.vanillaSubmittables.add(new VanillaSubmittable<>(state, renderer));
	}

	@Override
	public void submitFilledBoxWithBeaconBeam(BlockPos pos, float[] colourComponents, float alpha, boolean throughWalls) {
		submitFilledBox(pos, colourComponents, alpha, throughWalls);
		submitBeaconBeam(pos, colourComponents);
	}

	@Override
	public void submitFilledBoxWithBeaconBeam(AABB box, float[] colourComponents, float alpha, boolean throughWalls) {
		submitFilledBox(box, colourComponents, alpha, throughWalls);
		submitBeaconBeam(new BlockPos((int) box.minX, (int) box.minY, (int) box.minZ), colourComponents);
	}

	@Override
	public void submitFilledBox(BlockPos pos, float[] colourComponents, float alpha, boolean throughWalls) {
		submitFilledBox(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, colourComponents, alpha, throughWalls);
	}

	@Override
	public void submitFilledBox(Vec3 pos, Vec3 dimensions, float[] colourComponents, float alpha, boolean throughWalls) {
		submitFilledBox(pos.x, pos.y, pos.z, pos.x + dimensions.x, pos.y + dimensions.y, pos.z + dimensions.z, colourComponents, alpha, throughWalls);
	}

	@Override
	public void submitFilledBox(AABB box, float[] colourComponents, float alpha, boolean throughWalls) {
		submitFilledBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, colourComponents, alpha, throughWalls);
	}

	private void submitFilledBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float[] colourComponents, float alpha, boolean throughWalls) {
		ensureNotFrozen();

		// Ensure the box is in view
		if (!FrustumUtils.isVisible(this.frustum, minX, minY, minZ, maxX, maxY, maxZ)) {
			return;
		}

		FilledBoxRenderState state = new FilledBoxRenderState(minX, minY, minZ, maxX, maxY, maxZ, colourComponents, alpha);

		if (throughWalls) {
			this.filledBoxThroughWallsStates.add(state);
		} else {
			this.filledBoxStates.add(state);
		}
	}

	private void submitBeaconBeam(BlockPos pos, float[] colourComponents) {
		ensureNotFrozen();

		// Ensure the beacon is in view
		if (!FrustumUtils.isVisible(this.frustum, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, MAX_OVERWORLD_BUILD_HEIGHT, pos.getZ() + 1)) {
			return;
		}

		int colour = ARGB.colorFromFloat(1f, colourComponents[0], colourComponents[1], colourComponents[2]);
		float length = (float) RenderHelper.getCamera().position().subtract(Vec3.atCenterOf(pos)).horizontalDistance();
		BeaconRenderState state = new BeaconRenderState();
		state.blockPos = pos;
		((BlockEntityRenderStateAccessor) state).setBlockState(Blocks.BEACON.defaultBlockState());
		state.blockEntityType = BlockEntityTypes.BEACON;
		state.lightCoords = LightCoordsUtil.FULL_BRIGHT;
		state.breakProgress = null;
		state.animationTime = MINECRAFT.level != null ? Math.floorMod(MINECRAFT.level.getGameTime(), 40) + MINECRAFT.getDeltaTracker().getGameTimeDeltaPartialTick(true) : 0f;
		state.sections.add(new BeaconRenderState.Section(colour, MAX_OVERWORLD_BUILD_HEIGHT));
		state.beamRadiusScale = MINECRAFT.player != null && MINECRAFT.player.isScoping() ? 1.0f : Math.max(1.0f, length / 96.0f);

		this.worldState.blockEntityRenderStates.add(state);
	}

	@Override
	public void submitOutlinedBox(BlockPos pos, float[] colourComponents, float lineWidth, boolean throughWalls) {
		submitOutlinedBox(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, colourComponents, 1f, lineWidth, throughWalls);
	}

	@Override
	public void submitOutlinedBox(AABB box, float[] colourComponents, float lineWidth, boolean throughWalls) {
		submitOutlinedBox(box, colourComponents, 1f, lineWidth, throughWalls);
	}

	@Override
	public void submitOutlinedBox(AABB box, float[] colourComponents, float alpha, float lineWidth, boolean throughWalls) {
		submitOutlinedBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, colourComponents, alpha, lineWidth, throughWalls);
	}

	private void submitOutlinedBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float[] colourComponents, float alpha, float lineWidth, boolean throughWalls) {
		ensureNotFrozen();

		// Ensure the box is in view
		if (!FrustumUtils.isVisible(this.frustum, minX, minY, minZ, maxX, maxY, maxZ)) {
			return;
		}

		OutlinedBoxRenderState state = new OutlinedBoxRenderState(minX, minY, minZ, maxX, maxY, maxZ, colourComponents, alpha, lineWidth);

		if (throughWalls) {
			this.outlinedBoxThroughWallsStates.add(state);
		} else {
			this.outlinedBoxStates.add(state);
		}
	}

	@Override
	public void submitOutlinedConnected(BlockPosSet blocks, float[] colourComponents, float lineWidth, boolean throughWalls) {
		submitOutlinedConnected(blocks, colourComponents, 1f, lineWidth, throughWalls);
	}

	@Override
	public void submitOutlinedConnected(BlockPosSet blocks, float[] colourComponents, float alpha, float lineWidth, boolean throughWalls) {
		/* TODO, iterate over blocks to find block sides with no neighbor (including
			diagonal neighbors, so 6 neighbor blocks to check for each side) and store
			in a List<BlockSide> (or any better format) to then submit to the feature render */
	}

	private void submitOutlinedConnected(List<BlockSide> sides, float[] colourComponents, float alpha, float lineWidth, boolean throughWalls) {
		ensureNotFrozen();

		// Ensure the box is in view
		double minX = Collections.min(sides.stream().map(BlockSide::x).toList());
		double minY = Collections.min(sides.stream().map(BlockSide::y).toList());
		double minZ = Collections.min(sides.stream().map(BlockSide::z).toList());
		double maxX = Collections.max(sides.stream().map(BlockSide::x).toList());
		double maxY = Collections.max(sides.stream().map(BlockSide::y).toList());
		double maxZ = Collections.max(sides.stream().map(BlockSide::z).toList());
		if (!FrustumUtils.isVisible(this.frustum, minX, minY, minZ, maxX, maxY, maxZ)) {
			return;
		}

		OutlinedConnectedRenderState state = new OutlinedConnectedRenderState(sides, colourComponents, alpha, lineWidth);

		if (throughWalls) {
			this.outlinedConnectedThroughWallsStates.add(state);
		} else {
			this.outlinedConnectedStates.add(state);
		}
	}

	@Override
	public void submitLinesFromPoints(Vec3[] points, float[] colourComponents, float alpha, float lineWidth, boolean throughWalls) {
		ensureNotFrozen();

		LinesRenderState state = new LinesRenderState(points, colourComponents, alpha, lineWidth);

		if (throughWalls) {
			this.linesThroughWallsStates.add(state);
		} else {
			this.linesStates.add(state);
		}
	}

	@Override
	public void submitLineFromCursor(Vec3 point, float[] colourComponents, float alpha, float lineWidth) {
		ensureNotFrozen();

		CursorLineRenderState state = new CursorLineRenderState(point, colourComponents, alpha, lineWidth);
		this.cursorLineStates.add(state);
	}

	@Override
	public void submitQuad(Vec3[] points, float[] colourComponents, float alpha, boolean throughWalls) {
		ensureNotFrozen();

		QuadRenderState state = new QuadRenderState(points, colourComponents, alpha);

		if (throughWalls) {
			this.quadThroughWallsStates.add(state);
		} else {
			this.quadStates.add(state);
		}
	}

	@Override
	public void submitTexturedQuad(Vec3 pos, float width, float height, float textureWidth, float textureHeight, Vec3 renderOffset, Identifier texture, float[] shaderColour, float alpha, boolean throughWalls) {
		ensureNotFrozen();

		TexturedQuadRenderState state = new TexturedQuadRenderState(pos, width, height, textureWidth, textureHeight, renderOffset, texture, shaderColour, alpha);

		if (throughWalls) {
			this.texturedQuadThroughWallsStates.add(state);
		} else {
			this.texturedQuadStates.add(state);
		}
	}

	@Override
	public void submitBlockHologram(BlockPos pos, BlockState state, float alpha) {
		ensureNotFrozen();

		if (!FrustumUtils.isVisible(this.frustum, pos)) {
			return;
		}

		BlockHologramRenderState renderState = new BlockHologramRenderState(pos, state, alpha);
		this.blockHologramStates.add(renderState);
	}

	@Override
	public void submitText(Component text, Vec3 pos, boolean throughWalls) {
		submitText(text, pos, 1, throughWalls);
	}

	@Override
	public void submitText(Component text, Vec3 pos, float scale, boolean throughWalls) {
		submitText(text, pos, scale, 0, throughWalls);
	}

	@Override
	public void submitText(Component text, Vec3 pos, float scale, float yOffset, boolean throughWalls) {
		submitText(text.getVisualOrderText(), pos, scale, yOffset, throughWalls);
	}

	private void submitText(FormattedCharSequence text, Vec3 pos, float scale, float yOffset, boolean throughWalls) {
		ensureNotFrozen();

		Font textRenderer = MINECRAFT.font;
		float xOffset = -textRenderer.width(text) / 2f;
		Font.PreparedText glyphs = textRenderer.prepareText(text, xOffset, yOffset, CommonColors.WHITE, false, false, 0);

		TextRenderState state = new TextRenderState(glyphs, pos, scale * 0.025f, yOffset);

		if (throughWalls) {
			this.textThroughWallsStates.add(state);
		} else {
			this.textStates.add(state);
		}
	}

	@Override
	public void submitCylinder(Vec3 centre, float radius, float height, int segments, int colour) {
		ensureNotFrozen();

		CylinderRenderState state = new CylinderRenderState(centre, radius, height, segments, colour);
		this.cylinderStates.add(state);
	}

	@Override
	public void submitSphere(Vec3 centre, float radius, int segments, int rings, int colour) {
		ensureNotFrozen();

		SphereRenderState state = new SphereRenderState(centre, radius, segments, rings, colour);
		this.sphereStates.add(state);
	}

	@Override
	public void submitFilledCircle(Vec3 centre, float radius, int segments, int colour) {
		ensureNotFrozen();

		FilledCircleRenderState state = new FilledCircleRenderState(centre, radius, segments, colour);
		this.filledCircleStates.add(state);
	}

	@Override
	public void submitOutlinedCircle(Vec3 centre, float radius, float thickness, int segments, int colour) {
		ensureNotFrozen();

		OutlinedCircleRenderState state = new OutlinedCircleRenderState(centre, radius, thickness, segments, colour);
		this.outlinedCircleStates.add(state);
	}

	public void endCollection() {
		this.frozen = true;
	}

	/**
	 * Instances of this class are used only once, and primitives should not be submitted once the collection phase has ended.
	 */
	private void ensureNotFrozen() {
		if (this.frozen) {
			throw new IllegalStateException("Cannot submit primitives once the collection phase has ended!");
		}
	}

	@SuppressWarnings("unchecked")
	public void dispatchSubmits(LevelRenderState levelState, SubmitNodeCollector submitNodeCollector) {
		if (!this.frozen) {
			throw new IllegalStateException("Cannot dispatch submits until the collection phase has ended!");
		}

		CameraRenderState camera = levelState.cameraRenderState;

		if (!this.vanillaSubmittables.isEmpty()) {
			for (VanillaSubmittable<?> submittable : this.vanillaSubmittables) {
				((VanillaRenderer<Object>) submittable.renderer).submitVanilla(submittable.state(), levelState, submitNodeCollector);
			}
		}

		if (!this.filledBoxStates.isEmpty()) {
			if (this.isVulkan) {
				submitNodeCollector.submitCustom(SubmitRenderPhases.AFTER_TERRAIN, new FilledBoxInstancedFeatureRenderer.Submit(this.filledBoxStates, camera, false));
			} else {
				submitNodeCollector.submitCustom(SubmitRenderPhases.AFTER_TERRAIN, new FilledBoxFeatureRenderer.Submit(this.filledBoxStates, camera, false));
			}
		}

		if (!this.filledBoxThroughWallsStates.isEmpty()) {
			if (this.isVulkan) {
				submitNodeCollector.submitCustom(SubmitRenderPhases.ALWAYS_ON_TOP, new FilledBoxInstancedFeatureRenderer.Submit(this.filledBoxThroughWallsStates, camera, true));
			} else {
				submitNodeCollector.submitCustom(SubmitRenderPhases.ALWAYS_ON_TOP, new FilledBoxFeatureRenderer.Submit(this.filledBoxThroughWallsStates, camera, true));
			}
		}

		if (!this.outlinedBoxStates.isEmpty()) {
			if (this.isVulkan) {
				submitNodeCollector.submitCustom(SubmitRenderPhases.AFTER_TERRAIN, new OutlinedBoxInstancedFeatureRenderer.Submit(this.outlinedBoxStates, camera, false));
			} else {
				submitNodeCollector.submitCustom(SubmitRenderPhases.AFTER_TERRAIN, new OutlinedBoxFeatureRenderer.Submit(this.outlinedBoxStates, camera, false));
			}
		}

		if (!this.outlinedBoxThroughWallsStates.isEmpty()) {
			if (this.isVulkan) {
				submitNodeCollector.submitCustom(SubmitRenderPhases.ALWAYS_ON_TOP, new OutlinedBoxInstancedFeatureRenderer.Submit(this.outlinedBoxThroughWallsStates, camera, true));
			} else {
				submitNodeCollector.submitCustom(SubmitRenderPhases.ALWAYS_ON_TOP, new OutlinedBoxFeatureRenderer.Submit(this.outlinedBoxThroughWallsStates, camera, true));
			}
		}

		if (!this.linesStates.isEmpty()) {
			submitNodeCollector.submitCustom(SubmitRenderPhases.AFTER_TERRAIN, new LinesFeatureRenderer.Submit(this.linesStates, camera, false));
		}

		if (!this.linesThroughWallsStates.isEmpty()) {
			submitNodeCollector.submitCustom(SubmitRenderPhases.ALWAYS_ON_TOP, new LinesFeatureRenderer.Submit(this.linesThroughWallsStates, camera, true));
		}

		if (!this.cursorLineStates.isEmpty()) {
			submitNodeCollector.submitCustom(SubmitRenderPhases.ALWAYS_ON_TOP, new CursorLineFeatureRenderer.Submit(this.cursorLineStates, camera));
		}

		if (!this.quadStates.isEmpty()) {
			submitNodeCollector.submitCustom(SubmitRenderPhases.AFTER_TERRAIN, new QuadFeatureRenderer.Submit(this.quadStates, camera, false));
		}

		if (!this.quadThroughWallsStates.isEmpty()) {
			submitNodeCollector.submitCustom(SubmitRenderPhases.ALWAYS_ON_TOP, new QuadFeatureRenderer.Submit(this.quadThroughWallsStates, camera, true));
		}

		if (!this.texturedQuadStates.isEmpty()) {
			submitNodeCollector.submitCustom(SubmitRenderPhases.AFTER_TERRAIN, new TexturedQuadFeatureRenderer.Submit(this.texturedQuadStates, camera, false));
		}

		if (!this.texturedQuadThroughWallsStates.isEmpty()) {
			submitNodeCollector.submitCustom(SubmitRenderPhases.ALWAYS_ON_TOP, new TexturedQuadFeatureRenderer.Submit(this.texturedQuadThroughWallsStates, camera, true));
		}

		if (!this.blockHologramStates.isEmpty()) {
			submitNodeCollector.submitCustom(SubmitRenderPhases.AFTER_TERRAIN, new BlockHologramFeatureRenderer.Submit(this.blockHologramStates, camera));
		}

		if (!this.textStates.isEmpty()) {
			submitNodeCollector.submitCustom(SubmitRenderPhases.TEXTS, new TextFeatureRenderer.Submit(this.textStates, camera, false));
		}

		if (!this.textThroughWallsStates.isEmpty()) {
			submitNodeCollector.submitCustom(SubmitRenderPhases.ALWAYS_ON_TOP, new TextFeatureRenderer.Submit(this.textThroughWallsStates, camera, true));
		}

		if (!this.cylinderStates.isEmpty()) {
			submitNodeCollector.submitCustom(SubmitRenderPhases.AFTER_TERRAIN, new CylinderFeatureRenderer.Submit(this.cylinderStates, camera));
		}

		if (!this.sphereStates.isEmpty()) {
			submitNodeCollector.submitCustom(SubmitRenderPhases.AFTER_TERRAIN, new SphereFeatureRenderer.Submit(this.sphereStates, camera));
		}

		if (!this.filledCircleStates.isEmpty()) {
			submitNodeCollector.submitCustom(SubmitRenderPhases.AFTER_TERRAIN, new FilledCircleFeatureRenderer.Submit(this.filledCircleStates, camera));
		}

		if (!this.outlinedCircleStates.isEmpty()) {
			submitNodeCollector.submitCustom(SubmitRenderPhases.AFTER_TERRAIN, new OutlinedCircleFeatureRenderer.Submit(this.outlinedCircleStates, camera));
		}
	}

	private record VanillaSubmittable<S>(S state, VanillaRenderer<S> renderer) {}
}
