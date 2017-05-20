package solarsystem;

public class WeirdPyramid {

   private float[] weirdPyramid_positions =
   	{	-1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f,    //front
   		-1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f,    //front
         1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,    //right
         1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,      // smaller right
   		1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,  //back
   		-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,  //left
         -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f,    // smaller left
         -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f,      // top
   		-1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, //LF
   		1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f  //RR
   	};
   // mapped by on paper by hand
   private float[] weirdPyramidTexture_coordinates =
   	{	0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,    // front
   		1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,    // front
         0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,    // right
   		0.0f, 0.0f, 0.5f, 1.0f, 0.0f, 1.0f,    // smaller right
         0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,    // back
   		0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,    // left
         0.0f, 0.0f, 0.5f, 1.0f, 0.0f, 1.0f,    // smaller left
         0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,    // top
         0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,    //lf
         0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f     //rr
   	};


   public WeirdPyramid() {
   
   }
   
   public float[] getVertices() {
      return weirdPyramid_positions;
   }
   
   public float[] getTextureCoordinates() {
      return weirdPyramidTexture_coordinates;
   }

}