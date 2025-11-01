package de.hysky.skyblocker.utils.render.primitive;

import java.util.ArrayList;
import java.util.List;

import de.hysky.skyblocker.utils.render.FrustumUtils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.state.BlockHologramRenderState;
import de.hysky.skyblocker.utils.render.state.CursorLineRenderState;
import de.hysky.skyblocker.utils.render.state.CylinderRenderState;
import de.hysky.skyblocker.utils.render.state.FilledBoxRenderState;
import de.hysky.skyblocker.utils.render.state.FilledCircleRenderState;
import de.hysky.skyblocker.utils.render.state.LinesRenderState;
import de.hysky.skyblocker.utils.render.state.OutlinedBoxRenderState;
import de.hysky.skyblocker.utils.render.state.OutlinedCircleRenderState;
import de.hysky.skyblocker.utils.render.state.QuadRenderState;
import de.hysky.skyblocker.utils.render.state.SphereRenderState;
import de.hysky.skyblocker.utils.render.state.TextRenderState;
import de.hysky.skyblocker.utils.render.state.TexturedQuadRenderState;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.block.entity.state.BeaconBlockEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;

public final class PrimitiveCollectorImpl implements PrimitiveCollector {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final int MAX_OVERWORLD_BUILD_HEIGHT = 319;
	private final WorldRenderState worldState;
	private final Frustum frustum;
	private List<FilledBoxRenderState> filledBoxStates = null;
	private List<OutlinedBoxRenderState> outlinedBoxStates = null;
	private List<LinesRenderState> linesStates = null;
	private List<CursorLineRenderState> cursorLineStates = null;
	private List<QuadRenderState> quadStates = null;
	private List<TexturedQuadRenderState> texturedQuadStates = null;
	private List<BlockHologramRenderState> blockHologramStates = null;
	private List<TextRenderState> textStates = null;
	private List<CylinderRenderState> cylinderStates = null;
	private List<FilledCircleRenderState> filledCircleStates = null;
	private List<SphereRenderState> sphereStates = null;
	private List<OutlinedCircleRenderState> outlinedCircleStates = null;
	private boolean frozen = false;

	public PrimitiveCollectorImpl(WorldRenderState worldState, Frustum frustum) {
		this.worldState = worldState;
		this.frustum = frustum;
	}

	@Override
	public void submitFilledBoxWithBeaconBeam(BlockPos pos, float[] colourComponents, float alpha, boolean throughWalls) {
		submitFilledBox(pos, colourComponents, alpha, throughWalls);
		submitBeaconBeam(pos, colourComponents);
	}

	@Override
	public void submitFilledBox(BlockPos pos, float[] colourComponents, float alpha, boolean throughWalls) {
		submitFilledBox(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, colourComponents, alpha, throughWalls);
	}

	@Override
	public void submitFilledBox(Vec3d pos, Vec3d dimensions, float[] colourComponents, float alpha, boolean throughWalls) {
		submitFilledBox(pos.x, pos.y, pos.z, pos.x + dimensions.x, pos.y + dimensions.y, pos.z + dimensions.z, colourComponents, alpha, throughWalls);
	}

	@Override
	public void submitFilledBox(Box box, float[] colourComponents, float alpha, boolean throughWalls) {
		submitFilledBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, colourComponents, alpha, throughWalls);
	}

	private void submitFilledBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float[] colourComponents, float alpha, boolean throughWalls) {
		ensureNotFrozen();

		// Ensure the box is in view
		if (!FrustumUtils.isVisible(this.frustum, minX, minY, minZ, maxX, maxY, maxZ)) {
			return;
		}

		if (this.filledBoxStates == null) {
			this.filledBoxStates = new ArrayList<>();
		}

		FilledBoxRenderState state = new FilledBoxRenderState();
		state.minX = minX;
		state.minY = minY;
		state.minZ = minZ;
		state.maxX = maxX;
		state.maxY = maxY;
		state.maxZ = maxZ;
		state.colourComponents = colourComponents;
		state.alpha = alpha;
		state.throughWalls = throughWalls;

		this.filledBoxStates.add(state);
	}

	private void submitBeaconBeam(BlockPos pos, float[] colourComponents) {
		ensureNotFrozen();

		// Ensure the beacon is in view
		if (!FrustumUtils.isVisible(this.frustum, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, MAX_OVERWORLD_BUILD_HEIGHT, pos.getZ() + 1)) {
			return;
		}

		int colour = ColorHelper.fromFloats(1f, colourComponents[0], colourComponents[1], colourComponents[2]);
		float length = (float) RenderHelper.getCamera().getPos().subtract(pos.toCenterPos()).horizontalLength();
		BeaconBlockEntityRenderState state = new BeaconBlockEntityRenderState();
		state.pos = pos;
		state.blockState = Blocks.BEACON.getDefaultState();
		state.type = BlockEntityType.BEACON;
		state.lightmapCoordinates = LightmapTextureManager.MAX_LIGHT_COORDINATE;
		state.crumblingOverlay = null;
		state.beamRotationDegrees = CLIENT.world != null ? Math.floorMod(CLIENT.world.getTime(), 40) + CLIENT.getRenderTickCounter().getTickProgress(true) : 0f;
		state.beamSegments.add(new BeaconBlockEntityRenderState.BeamSegment(colour, MAX_OVERWORLD_BUILD_HEIGHT));
		state.beamScale = CLIENT.player != null && CLIENT.player.isUsingSpyglass() ? 1.0F : Math.max(1.0F, length / 96.0F);

		this.worldState.blockEntityRenderStates.add(state);
	}

	@Override
	public void submitOutlinedBox(BlockPos pos, float[] colourComponents, float lineWidth, boolean throughWalls) {
		submitOutlinedBox(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, colourComponents, 1f, lineWidth, throughWalls);
	}

	@Override
	public void submitOutlinedBox(Box box, float[] colourComponents, float lineWidth, boolean throughWalls) {
		submitOutlinedBox(box, colourComponents, 1f, lineWidth, throughWalls);
	}

	@Override
	public void submitOutlinedBox(Box box, float[] colourComponents, float alpha, float lineWidth, boolean throughWalls) {
		submitOutlinedBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, colourComponents, alpha, lineWidth, throughWalls);
	}

	private void submitOutlinedBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float[] colourComponents, float alpha, float lineWidth, boolean throughWalls) {
		ensureNotFrozen();

		// Ensure the box is in view
		if (!FrustumUtils.isVisible(this.frustum, minX, minY, minZ, maxX, maxY, maxZ)) {
			return;
		}

		if (this.outlinedBoxStates == null) {
			this.outlinedBoxStates = new ArrayList<>();
		}

		OutlinedBoxRenderState state = new OutlinedBoxRenderState();
		state.minX = minX;
		state.minY = minY;
		state.minZ = minZ;
		state.maxX = maxX;
		state.maxY = maxY;
		state.maxZ = maxZ;
		state.colourComponents = colourComponents;
		state.alpha = alpha;
		state.lineWidth = lineWidth;
		state.throughWalls = throughWalls;

		this.outlinedBoxStates.add(state);
	}

	@Override
	public void submitLinesFromPoints(Vec3d[] points, float[] colourComponents, float alpha, float lineWidth, boolean throughWalls) {
		ensureNotFrozen();

		if (this.linesStates == null) {
			this.linesStates = new ArrayList<>();
		}

		LinesRenderState state = new LinesRenderState();
		state.points = points;
		state.colourComponents = colourComponents;
		state.alpha = alpha;
		state.lineWidth = lineWidth;
		state.throughWalls = throughWalls;

		this.linesStates.add(state);
	}

	@Override
	public void submitLineFromCursor(Vec3d point, float[] colourComponents, float alpha, float lineWidth) {
		ensureNotFrozen();

		if (this.cursorLineStates == null) {
			this.cursorLineStates = new ArrayList<>();
		}

		CursorLineRenderState state = new CursorLineRenderState();
		state.point = point;
		state.colourComponents = colourComponents;
		state.alpha = alpha;
		state.lineWidth = lineWidth;

		this.cursorLineStates.add(state);
	}

	@Override
	public void submitQuad(Vec3d[] points, float[] colourComponents, float alpha, boolean throughWalls) {
		ensureNotFrozen();

		if (this.quadStates == null) {
			this.quadStates = new ArrayList<>();
		}

		QuadRenderState state = new QuadRenderState();
		state.points = points;
		state.colourComponents = colourComponents;
		state.alpha = alpha;
		state.throughWalls = throughWalls;

		this.quadStates.add(state);
	}

	@Override
	public void submitTexturedQuad(Vec3d pos, float width, float height, float textureWidth, float textureHeight, Vec3d renderOffset, Identifier texture, float[] shaderColour, float alpha, boolean throughWalls) {
		ensureNotFrozen();

		if (this.texturedQuadStates == null) {
			this.texturedQuadStates = new ArrayList<>();
		}

		TexturedQuadRenderState state = new TexturedQuadRenderState();
		state.pos = pos;
		state.width = width;
		state.height = height;
		state.textureWidth = textureWidth;
		state.textureHeight = textureHeight;
		state.renderOffset = renderOffset;
		state.texture = texture;
		state.shaderColour = shaderColour;
		state.alpha = alpha;
		state.throughWalls = throughWalls;

		this.texturedQuadStates.add(state);
	}

	@Override
	public void submitBlockHologram(BlockPos pos, BlockState state) {
		ensureNotFrozen();

		if (!FrustumUtils.isVisible(this.frustum, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)) {
			return;
		}

		if (this.blockHologramStates == null) {
			this.blockHologramStates = new ArrayList<>();
		}

		BlockHologramRenderState renderState = new BlockHologramRenderState();
		renderState.pos = pos;
		renderState.state = state;

		this.blockHologramStates.add(renderState);
	}

	@Override
	public void submitText(Text text, Vec3d pos, boolean throughWalls) {
		submitText(text, pos, 1, throughWalls);
	}

	@Override
	public void submitText(Text text, Vec3d pos, float scale, boolean throughWalls) {
		submitText(text, pos, scale, 0, throughWalls);
	}

	@Override
	public void submitText(Text text, Vec3d pos, float scale, float yOffset, boolean throughWalls) {
		submitText(text.asOrderedText(), pos, scale, yOffset, throughWalls);
	}

	private void submitText(OrderedText text, Vec3d pos, float scale, float yOffset, boolean throughWalls) {
		ensureNotFrozen();

		if (this.textStates == null) {
			this.textStates = new ArrayList<>();
		}

		TextRenderer textRenderer = CLIENT.textRenderer;
		float xOffset = -textRenderer.getWidth(text) / 2f;
		TextRenderer.GlyphDrawable glyphs = textRenderer.prepare(text, xOffset, yOffset, Colors.WHITE, false, 0);

		TextRenderState state = new TextRenderState();
		state.glyphs = glyphs;
		state.pos = pos;
		state.scale = scale * 0.025f;
		state.yOffset = yOffset;
		state.throughWalls = throughWalls;

		this.textStates.add(state);
	}

	@Override
	public void submitCylinder(Vec3d centre, float radius, float height, int segments, int colour) {
		ensureNotFrozen();

		if (this.cylinderStates == null) {
			this.cylinderStates = new ArrayList<>();
		}

		CylinderRenderState state = new CylinderRenderState();
		state.centre = centre;
		state.radius = radius;
		state.height = height;
		state.segments = segments;
		state.colour = colour;

		this.cylinderStates.add(state);
	}

	@Override
	public void submitFilledCircle(Vec3d centre, float radius, int segments, int colour) {
		ensureNotFrozen();

		if (this.filledCircleStates == null) {
			this.filledCircleStates = new ArrayList<>();
		}

		FilledCircleRenderState state = new FilledCircleRenderState();
		state.centre = centre;
		state.radius = radius;
		state.segments = segments;
		state.colour = colour;

		this.filledCircleStates.add(state);
	}

	@Override
	public void submitSphere(Vec3d centre, float radius, int segments, int rings, int colour) {
		ensureNotFrozen();

		if (this.sphereStates == null) {
			this.sphereStates = new ArrayList<>();
		}

		SphereRenderState state = new SphereRenderState();
		state.centre = centre;
		state.radius = radius;
		state.segments = segments;
		state.rings = rings;
		state.colour = colour;

		this.sphereStates.add(state);
	}

	@Override
	public void submitOutlinedCircle(Vec3d centre, float radius, float thickness, int segments, int colour) {
		ensureNotFrozen();

		if (this.outlinedCircleStates == null) {
			this.outlinedCircleStates = new ArrayList<>();
		}

		OutlinedCircleRenderState state = new OutlinedCircleRenderState();
		state.centre = centre;
		state.radius = radius;
		state.thickness = thickness;
		state.segments = segments;
		state.colour = colour;

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

	public void dispatchPrimitivesToRenderers(CameraRenderState cameraState) {
		if (!this.frozen) {
			throw new IllegalStateException("Cannot dispatch primitives until the collection phase has ended!");
		}

		if (this.filledBoxStates != null) {
			for (FilledBoxRenderState state : this.filledBoxStates) {
				FilledBoxRenderer.INSTANCE.submitPrimitives(state, cameraState);
			}
		}

		if (this.outlinedBoxStates != null) {
			for (OutlinedBoxRenderState state : this.outlinedBoxStates) {
				OutlinedBoxRenderer.INSTANCE.submitPrimitives(state, cameraState);
			}
		}

		if (this.linesStates != null) {
			for (LinesRenderState state : this.linesStates) {
				LinesRenderer.INSTANCE.submitPrimitives(state, cameraState);
			}
		}

		if (this.cursorLineStates != null) {
			for (CursorLineRenderState state : this.cursorLineStates) {
				CursorLineRenderer.INSTANCE.submitPrimitives(state, cameraState);
			}
		}

		if (this.quadStates != null) {
			for (QuadRenderState state : this.quadStates) {
				QuadRenderer.INSTANCE.submitPrimitives(state, cameraState);
			}
		}

		if (this.texturedQuadStates != null) {
			for (TexturedQuadRenderState state : this.texturedQuadStates) {
				TexturedQuadRenderer.INSTANCE.submitPrimitives(state, cameraState);
			}
		}

		if (this.blockHologramStates != null) {
			for (BlockHologramRenderState state : this.blockHologramStates) {
				BlockHologramRenderer.INSTANCE.submitPrimitives(state, cameraState);
			}
		}

		if (this.textStates != null) {
			for (TextRenderState state : this.textStates) {
				TextPrimitiveRenderer.INSTANCE.submitPrimitives(state, cameraState);
			}
		}

		if (this.cylinderStates != null) {
			for (CylinderRenderState state : this.cylinderStates) {
				CylinderRenderer.INSTANCE.submitPrimitives(state, cameraState);
			}
		}

		if (this.filledCircleStates != null) {
			for (FilledCircleRenderState state : this.filledCircleStates) {
				FilledCircleRenderer.INSTANCE.submitPrimitives(state, cameraState);
			}
		}

		if (this.sphereStates != null) {
			for (SphereRenderState state : this.sphereStates) {
				SphereRenderer.INSTANCE.submitPrimitives(state, cameraState);
			}
		}

		if (this.outlinedCircleStates != null) {
			for (OutlinedCircleRenderState state : this.outlinedCircleStates) {
				OutlinedCircleRenderer.INSTANCE.submitPrimitives(state, cameraState);
			}
		}
	}
}
