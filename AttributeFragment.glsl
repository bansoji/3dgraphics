#version 120

varying vec2 texture_out;
varying vec4 vectorPos_out;
varying vec4 vectorNorm_out;
varying vec4 lighting_out;
uniform sampler2D texture;
uniform vec4 materialColor;



void main (void) {
      //gl_FragColor = lighting_out * texture2D(texture,texture_out);
      gl_FragColor = lighting_out * materialColor;

}

