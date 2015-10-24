#version 120

attribute vec3 vertexPos;
attribute vec3 vertexNorm;
attribute vec2 texCoords;

uniform vec4 lightDiff;
uniform vec3 lightPos;
uniform vec4 lightAmb;

varying vec4 lighting_out;
varying vec2 texture_out;

void main(void) {
    vec3 v, normal, lightDir;
    vec4 ambient =
        gl_LightModel.ambient *
        gl_FrontMaterial.ambient +
        lightAmb *
        gl_FrontMaterial.ambient;
    float NdotL;
    // transform the normal into eye space and normalize
    normal = normalize(gl_NormalMatrix * vertexNorm);
    //transform co-ords into eye space
    v = vec3(gl_ModelViewMatrix * vec4(vertexPos,1));
    // normalize the light's direction
    lightDir = normalize(lightPos - v);
    NdotL = max(dot(normal, lightDir), 0.0);

    /* Compute the diffuse term */
    lighting_out = NdotL * gl_FrontMaterial.diffuse *
    lightDiff + ambient;
    gl_Position = gl_ModelViewProjectionMatrix * vec4(vertexPos,1);
    texture_out = texCoords;
}


