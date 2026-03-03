package de.hysky.skyblocker.utils.render;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;

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
	 * Creates a blank {@link PoseStack} and sets it's position matrix to the supplied
	 * {@code positionMatrix}.
	 */
	static PoseStack toStack(Matrix4f positionMatrix) {
		PoseStack matrices = new PoseStack();
		matrices.last().pose().set(positionMatrix);

		return matrices;
	}
}
