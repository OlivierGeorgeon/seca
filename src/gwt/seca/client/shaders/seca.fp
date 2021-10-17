#ifdef GL_ES
precision highp float;
#endif
varying vec4 vPosition;
varying vec4 vTransformedNormal;
varying vec2 vTextureCoord;
varying vec4 vColor;
 
uniform bool uUseLighting;
uniform bool uUseTextures;
uniform bool uUseUniformColor;
 
uniform vec3 uAmbientColor;
uniform vec3 uPointLightingLocation;
uniform vec3 uPointLightingColor;
 
uniform sampler2D uSampler;
uniform vec4 uColor;
  
void main(void) {
  vec3 lightWeighting;
  if (!uUseLighting) {
    lightWeighting = vec3(1.0, 1.0, 1.0);
  } else {
    vec3 lightDirection = normalize(uPointLightingLocation - vPosition.xyz);
 
    float directionalLightWeighting = max(dot(normalize(vTransformedNormal.xyz), lightDirection), 0.0);
    lightWeighting = uAmbientColor + uPointLightingColor * directionalLightWeighting;
  }

  vec4 fragmentColor;
  if (uUseTextures) {
    fragmentColor = texture2D(uSampler, vec2(vTextureCoord.s, 1.0 - vTextureCoord.t));
  } else if (uUseUniformColor) {
    fragmentColor = uColor;
  } else {
    fragmentColor = vColor;
  }
  gl_FragColor = vec4(fragmentColor.rgb * lightWeighting, fragmentColor.a);
}