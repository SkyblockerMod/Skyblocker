package de.hysky.skyblocker.utils.render.state;

import java.util.List;

public record OutlinedConnectedRenderState(List<BlockSide> sides, float[] colourComponents, float alpha, float lineWidth) {
}
