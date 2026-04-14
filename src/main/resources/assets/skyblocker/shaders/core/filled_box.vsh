#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

uniform samplerBuffer BoxData;

in vec3 Position;

out vec4 vertexColor;

// FIXME Switch to unpackUnorm4x8 in the future
vec4 unpackColour(uint colour) {
	return vec4(
		float(colour & 0xFFu),
		float((colour >> 8u) & 0xFFu),
		float((colour >> 16u) & 0xFFu),
		float((colour >> 24u) & 0xFFu)
		) / 255.0;
}

void main() {
	// Two texels are used per instance
	// 1: (minX, minY, minZ, maxX)
	// 2: (maxY, maxZ, colour, unused)
	int base = gl_InstanceID * 2;

	vec4 data1 = texelFetch(BoxData, base);
	vec4 data2 = texelFetch(BoxData, base + 1);

	vec3 boxMin = data1.xyz;
	vec3 boxMax = vec3(data1.w, data2.xy);
	uint colour = floatBitsToUint(data2.z);

	// Since this is drawn with a unit cube, the Position's components will be
	// between 0-1 meaning that mix will simply choose either min or max where necessary.
	// mix(min, max, 0.0) -> min
	// mix(min, max, 1.0) -> max
	vec3 worldPos = mix(boxMin, boxMax, Position);

	gl_Position = ProjMat * ModelViewMat * vec4(worldPos, 1.0);
	vertexColor = unpackColour(colour);
}
