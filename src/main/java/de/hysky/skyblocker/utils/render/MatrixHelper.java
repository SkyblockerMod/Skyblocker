package de.hysky.skyblocker.utils.render;

import org.joml.Matrix4f;

import net.minecraft.client.util.math.MatrixStack;

/**
 * Matrix helper methods
 */
public interface MatrixHelper {

	/**
	 * Copies the {@code matrix} into a new {@link Matrix4f}. This is necessary otherwise
	 * any transformations applied will affect other uses of the same matrix.
	 */
	static Matrix4f copyOf(Matrix4f matrix) {
		return new Matrix4f(matrix);
	}

	/**
	 * Creates a blank {@link MatrixStack} and sets it's position matrix to the supplied
	 * {@code positionMatrix}.
	 */
	static MatrixStack toStack(Matrix4f positionMatrix) {
		MatrixStack matrices = new MatrixStack();
		matrices.peek().getPositionMatrix().set(positionMatrix);

		return matrices;
	}
}
