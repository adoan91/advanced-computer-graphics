#version 430



out vec4 color;
in float f;
in vec4 vc;
void main(void)
{
	if (f == 0) {
		color = vec4(0.0, 1.0, 0.0, 1.0);
	} else {
		color = vc;
	}
}