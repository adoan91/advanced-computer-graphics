#version 430

//in vec4 varyingColor;
in vec2 tc;
layout (binding=0) uniform sampler2D samp;
out vec4 color;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;



void main(void)
{	//color = varyingColor;
	color = texture(samp, tc);
}
