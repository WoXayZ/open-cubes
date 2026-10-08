#version 330

uniform sampler2D Sampler0;

out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / vec2(textureSize(Sampler0, 0));
    fragColor = vec4(texture(Sampler0, uv).rgb, 1.0);
}
