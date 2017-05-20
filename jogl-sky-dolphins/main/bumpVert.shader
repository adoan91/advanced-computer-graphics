#version 430

layout (location=0) in vec3 vertPos;
layout (location=1) in vec3 vertNormal;

out vec3 vNormal, vLightDir, vVertPos, vHalfVec; 
out vec3 originalVertex;
out vec4 shadow_coord;

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

mat4 buildTranslate(float x, float y, float z);

void main(void)
{	
	float i = gl_InstanceID; // + tf;
	
	float a = sin(2.0 * i) * 10.0;
	float b = sin(3.0 * i) * -20.0;
	float c = sin(4.0 * i) * 10.0;

	mat4 localTrans = buildTranslate(a, b, c);
	
	//output the vertex position to the rasterizer for interpolation
	vVertPos = (mv_matrix * vec4(vertPos,1.0)).xyz;
        
	//get a vector from the vertex to the light and output it to the rasterizer for interpolation
	vLightDir = light.position - vVertPos;

	//get a vertex normal vector in eye space and output it to the rasterizer for interpolation
	vNormal = (normalMat * vec4(vertNormal,1.0)).xyz;
	
	// calculate the half vector (L+V)
	vHalfVec = (vLightDir-vVertPos).xyz;
	
	shadow_coord = shadowMVP * vec4(vertPos,1.0);
	
	gl_Position = proj_matrix * mv_matrix * localTrans * vec4(vertPos,1.0);

	originalVertex = vertPos;
}

mat4 buildTranslate(float x, float y, float z) {
	mat4 trans = mat4(	1.0, 0.0, 0.0, 0.0,
						0.0, 1.0, 0.0, 0.0,
						0.0, 0.0, 1.0, 0.0,
						x, y, z, 1.0);
	return trans;
}
