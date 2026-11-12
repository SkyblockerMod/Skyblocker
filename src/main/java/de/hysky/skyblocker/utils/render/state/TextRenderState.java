package de.hysky.skyblocker.utils.render.state;

import org.joml.Matrix4fc;

import net.minecraft.util.FormattedCharSequence;

public record TextRenderState(FormattedCharSequence text, Matrix4fc matrix, float xOffset, float yOffset) {
}
