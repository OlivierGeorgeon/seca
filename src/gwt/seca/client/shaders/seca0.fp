#ifdef GL_ES
precision highp float;
#endif
varying vec2 vTextureCoord;
varying vec3 vLightWeighting;
varying vec4 vColor;

uniform bool uUseTextures;
uniform bool uUseUniformColor;

uniform sampler2D uSampler;
uniform vec4 uColor;

void main(void) {
  vec4 fragmentColor;

  if (uUseUniformColor) {
    fragmentColor = vec4(uColor.rgb * vLightWeighting, uColor.a);
  } else if (uUseTextures) {
    vec4 textureColor = texture2D(uSampler, vec2(vTextureCoord.s, 1.0 - vTextureCoord.t));
    fragmentColor = vec4(textureColor.rgb * vLightWeighting, 1.0);
  } else {
    fragmentColor = vec4(vColor.rgb * vLightWeighting, vColor.a);
  }
  gl_FragColor = fragmentColor;
}
