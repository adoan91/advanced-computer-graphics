#version 430

layout (location=0) in vec3 vertPos;
layout (location=1) in vec3 vertNormal;

out vec3 vNormal, vVertPos;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 normalMat;
layout (binding=1) uniform samplerCube samp;

void main(void)
{	//output the vertex position to the rasterizer for interpolation
	vVertPos = (mv_matrix * vec4(vertPos,1.0)).xyz;
        
	//get a vertex normal vector in eye space and output it to the rasterizer for interpolation
	vNormal = (normalMat * vec4(vertNormal,1.0)).xyz;
	
	gl_Position = proj_matrix * mv_matrix * vec4(vertPos,1.0);

}
