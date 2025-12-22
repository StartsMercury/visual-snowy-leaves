#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:chunksection.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:smooth_lighting.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;
in int VslIndex;

uniform sampler2D Sampler2;
layout(std140) uniform SnowProgress {
    float Progress;
};

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;

void main() {
    vec3 pos = Position + (ChunkPosition - CameraBlockPos) + CameraOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    sphericalVertexDistance = fog_spherical_distance(pos);
    cylindricalVertexDistance = fog_cylindrical_distance(pos);
    vertexColor = minecraft_sample_lightmap(Sampler2, UV2) * vec4(
        Color.a * (VslIndex != 1 ? Color.rgb : Progress + (1 - Progress) * Color.rgb),
        1.0
    );
    texCoord0 = UV0;
}
