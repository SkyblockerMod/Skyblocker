package de.hysky.skyblocker.utils.render.primitive;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface PrimitiveCollector {

	<S> void submitVanilla(S state, VanillaRenderer<S> renderer);

	void submitFilledBoxWithBeaconBeam(BlockPos pos, float[] colourComponents, float alpha, boolean throughWalls);

	void submitFilledBoxWithBeaconBeam(AABB box, float[] colourComponents, float alpha, boolean throughWalls);

	void submitFilledBox(BlockPos pos, float[] colourComponents, float alpha, boolean throughWalls);

	void submitFilledBox(Vec3 pos, Vec3 dimensions, float[] colourComponents, float alpha, boolean throughWalls);

	void submitFilledBox(AABB box, float[] colourComponents, float alpha, boolean throughWalls);

	void submitOutlinedBox(BlockPos pos, float[] colourComponents, float lineWidth, boolean throughWalls);

	void submitOutlinedBox(AABB box, float[] colourComponents, float lineWidth, boolean throughWalls);

	void submitOutlinedBox(AABB box, float[] colourComponents, float alpha, float lineWidth, boolean throughWalls);

	/**
	 * Submits lines to be drawn from point to point.<br><br>
	 * <p>
	 * Tip: To draw lines from the center of a block, offset the X, Y and Z each by 0.5
	 * <p>
	 * Note: This is super messed up when drawing long lines. Tried different normals and {@link DrawMode#LINES} but nothing worked.
	 *
	 * @param points    The points from which to draw lines between
	 * @param lineWidth The width of the lines
	 */
	void submitLinesFromPoints(Vec3[] points, float[] colourComponents, float alpha, float lineWidth, boolean throughWalls);

	void submitLineFromCursor(Vec3 point, float[] colourComponents, float alpha, float lineWidth);

	void submitQuad(Vec3[] points, float[] colourComponents, float alpha, boolean throughWalls);

	/**
	 * Submits a texture in world space facing the player (like a name tag).
	 *
	 * @param pos           world position
	 * @param width         rendered width
	 * @param height        rendered height
	 * @param textureWidth  amount of texture rendered width
	 * @param textureHeight amount of texture rendered height
	 * @param renderOffset  offset once it's been placed in the world facing the player
	 * @param texture       reference to texture to render
	 * @param shaderColor   colour to apply to the texture (use white if none)
	 */
	void submitTexturedQuad(Vec3 pos, float width, float height, float textureWidth, float textureHeight, Vec3 renderOffset, Identifier texture, float[] shaderColour, float alpha, boolean throughWalls);

	void submitBlockHologram(BlockPos pos, BlockState state);

	void submitText(Component text, Vec3 pos, boolean throughWalls);

	void submitText(Component text, Vec3 pos, float scale, boolean throughWalls);

	void submitText(Component text, Vec3 pos, float scale, float yOffset, boolean throughWalls);

	/**
	 * Submits a cylinder without the top or bottom faces.
	 *
	 * @param centre   The position that the circle will be centred around.
	 * @param radius   The radius of the cylinder.
	 * @param height   The total height of the cylinder with {@code pos} as the midpoint.
	 * @param segments The number of triangles used to approximate the circle.
	 */
	void submitCylinder(Vec3 centre, float radius, float height, int segments, int colour);

	/**
	 * Submits a circle filled in using the triangle fan draw mode.
	 *
	 * @param centre   The position that the circle will be centred around.
	 * @param radius   The radius of the circle.
	 * @param segments The number of triangles used to approximate the circle.
	 */
	void submitFilledCircle(Vec3 centre, float radius, int segments, int colour);

	/**
	 * Submits a circle sphere in using triangle strips.
	 *
	 * @param centre   The position that the circle will be centred around.
	 * @param radius   The radius of the sphere.
	 * @param rings    The number of rings to subdivide the sphere.
	 * @param segments The number of triangles used to approximate the circle.
	 */
	void submitSphere(Vec3 centre, float radius, int segments, int rings, int colour);

	/**
	 * Submits a circle outline in using quads
	 *
	 * @param centre    The position that the circle will be centred around.
	 * @param radius    The radius of the circle.
	 * @param thickness The thickness of the outline in blocks.
	 * @param segments  The number of triangles used to approximate the circle.
	 */
	void submitOutlinedCircle(Vec3 centre, float radius, float thickness, int segments, int colour);
}
