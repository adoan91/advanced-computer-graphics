#version 430

in vec3 vNormal, vVertPos;
out vec4 fragColor;

layout (binding=1) uniform samplerCube samp;
uniform mat4 mv_matrix; 
uniform mat4 proj_matrix;
uniform mat4 normalMat;

void main(void)
{	
	vec3 r = reflect(normalize(-vVertPos), normalize(vNormal));
				
	fragColor = (texture(samp, r));
						
}
