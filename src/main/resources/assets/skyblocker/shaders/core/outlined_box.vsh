#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

uniform samplerBuffer OutlinedBoxData;

in vec3 Position;
in vec3 Normal;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;

const float VIEW_SHRINK = 1.0 - (1.0 / 256.0);
const mat4 VIEW_SCALE = mat4(
	VIEW_SHRINK, 0.0, 0.0, 0.0,
	0.0, VIEW_SHRINK, 0.0, 0.0,
	0.0, 0.0, VIEW_SHRINK, 0.0,
	0.0, 0.0, 0.0, 1.0
);

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
	// 2: (maxY, maxZ, colour, lineWidth)
	int base = gl_InstanceID * 2;

	vec4 data1 = texelFetch(OutlinedBoxData, base);
	vec4 data2 = texelFetch(OutlinedBoxData, base + 1);

	vec3 boxMin = data1.xyz;
	vec3 boxMax = vec3(data1.w, data2.xy);
	uint colour = floatBitsToUint(data2.z);
	float lineWidth = data2.w;

	// Since this is drawn with a unit cube, the Position's components will be
	// between 0-1 meaning that mix will simply choose either min or max where necessary.
	// mix(min, max, 0.0) -> min
	// mix(min, max, 1.0) -> max
	vec3 worldPos = mix(boxMin, boxMax, Position);

	// Normal lines shader code below

	vec4 linePosStart = ProjMat * VIEW_SCALE * ModelViewMat * vec4(worldPos, 1.0);
	vec4 linePosEnd = ProjMat * VIEW_SCALE * ModelViewMat * vec4(worldPos + Normal, 1.0);

	vec3 ndc1 = linePosStart.xyz / linePosStart.w;
	vec3 ndc2 = linePosEnd.xyz / linePosEnd.w;

	vec2 lineScreenDirection = normalize((ndc2.xy - ndc1.xy) * ScreenSize);
	vec2 lineOffset = vec2(-lineScreenDirection.y, lineScreenDirection.x) * lineWidth / ScreenSize;

	if (lineOffset.x < 0.0) {
		lineOffset *= -1.0;
	}

	if (gl_VertexID % 2 == 0) {
		gl_Position = vec4((ndc1 + vec3(lineOffset, 0.0)) * linePosStart.w, linePosStart.w);
	} else {
		gl_Position = vec4((ndc1 - vec3(lineOffset, 0.0)) * linePosStart.w, linePosStart.w);
	}

	sphericalVertexDistance = fog_spherical_distance(worldPos);
	cylindricalVertexDistance = fog_cylindrical_distance(worldPos);
	vertexColor = unpackColour(colour);
}
