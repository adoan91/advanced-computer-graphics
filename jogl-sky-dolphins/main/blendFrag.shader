#version 430

in vec3 vNormal, vLightDir, vVertPos, vHalfVec;
in vec4 shadow_coord;
out vec4 fragColor;

struct PositionalLight
{	vec4 ambient, diffuse, specular;
	vec3 position;
};

struct Material
{	vec4 ambient, diffuse, specular;
	float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix; 
uniform mat4 proj_matrix;
uniform mat4 normalMat;
uniform mat4 shadowMVP;
layout (binding=0) uniform sampler2DShadow shadowTex;

uniform float alpha;
uniform float flipNormal;

void main(void)
{	vec3 L = normalize(vLightDir);
	vec3 N = normalize(vNormal);
	vec3 V = normalize(-vVertPos);
	vec3 H = normalize(vHalfVec);
	
	vec3 amb = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz; 
	vec3 dif = light.diffuse.xyz * material.diffuse.xyz * max(dot(L,N), 0.0); 
	vec3 spec = material.specular.xyz * light.specular.xyz * pow(max(dot(H,N), 0.0f), material.shininess); 	
				
	fragColor = (vec4((amb + dif + spec), 1.0));
	 
	// the following is added for transparency
	fragColor = vec4(fragColor.xyz, alpha);

}
