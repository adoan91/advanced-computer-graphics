#version 430

uniform float inx,iny,ins,cf;

out vec4 vc;
out float f;

void main(void)
{ f = cf;
  if (gl_VertexID == 0) { 
	gl_Position = vec4( 0.25+inx+ins,-0.25+iny-ins, 0.0, 1.0);
	vc = vec4(1.0, 0.0, 0.0, 1.0);
  }
  else if (gl_VertexID == 1) {
	gl_Position = vec4(-0.25+inx-ins,-0.25+iny-ins, 0.0, 1.0);
	vc = vec4(0.0, 1.0, 0.0, 1.0);
  }
  else {
	gl_Position = vec4( 0.25+inx+ins, 0.25+iny+ins, 0.0, 1.0);
	vc = vec4(0.0, 0.0, 1.0, 1.0);
  }
}