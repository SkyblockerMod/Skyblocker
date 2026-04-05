#version 330

#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;

in vec4 vertexColor;

out vec4 fragColor;

// Single-pass Bilinear Box Blur
void main() {
	vec2 screenUV = gl_FragCoord.xy / ScreenSize;
	vec2 texelSize = 1.0 / ScreenSize;

	vec4 colourSum = vec4(0.0);
	float totalWeights = 0.0;

	// The vertex colour is the easiest way to pass data to the shader
	// Convert the radius to an int first because it must be an integer value
	int blurRadius = int(vertexColor.r * 255.0);
	// Convert the radius to a float so that we can use it in the loop
	float r = float(blurRadius);

	// Step by 2 to skip every other texel
	// The linear filter will blend the skipped texels for us which allows us to
	// eliminate samples while maintaining the same look
	for (float x = -r; x < r; x += 2.0) {
		for (float y = -r; y < r; y += 2.0) {
			// Offset by 0.5 texels to hit the centre between the pixels
			// This is where the linear filtering does the heavy lifting for us
			vec2 offset = vec2(x + 0.5, y + 0.5) * texelSize;

			colourSum += texture(Sampler0, screenUV + offset);
			totalWeights += 1.0;
		}
	}

	if (totalWeights > 0.0) {
		// Force alpha to 1.0 to prevent blending issues
		fragColor = vec4((colourSum / totalWeights).rgb, 1.0);
	} else {
		fragColor = texture(Sampler0, screenUV);
	}
}
