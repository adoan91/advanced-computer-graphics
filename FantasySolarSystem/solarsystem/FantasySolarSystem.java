package solarsystem;

import solarsystem.Camera;
import solarsystem.WeirdPyramid;

import graphicslib3D.*;
import graphicslib3D.shape.*;
import graphicslib3D.GLSLUtils.*;
import java.io.*;
import java.nio.*;
import javax.swing.*;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.Graphics2D;

import java.awt.*;
import java.awt.event.*;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GLContext;


import com.jogamp.opengl.util.texture.*;

public class FantasySolarSystem extends JFrame implements GLEventListener, KeyListener
{	private GLCanvas myCanvas;
   private int rendering_program;
   private int vao[] = new int[1];
   private int vbo[] = new int[8];
   private GLSLUtils util;
   
   // textures
   private Texture joglRedTexture; // for X axis
   private int redTexture;
   private Texture joglGreenTexture; // for Y axis
   private int greenTexture;
   private Texture joglBlueTexture; // for Z axis
   private int blueTexture;
   private Texture joglCenterTexture; // for center planet
   private int centerTexture;
   private Texture joglPlanet1Texture; // for first planet from the center
   private int planet1Texture;
   private Texture joglPlanet2Texture; // for second planet from the center
   private int planet2Texture;
   private Texture joglMoon1Texture; // for moon of first planet
   private int moon1Texture;
   private Texture joglMoon2Texture; // for moon of second planet
   private int moon2Texture;
   
   private WeirdPyramid weirdPyramid = new WeirdPyramid(); // designed by me
   private Sphere mySphere = new Sphere(24);
	
   private	MatrixStack mvStack = new MatrixStack(20); // only one MatrixStack instance, max 20 matrices
   
   private int worldAxesFlag; // key space
   
   private Camera camera; // camera object
   
   public FantasySolarSystem()
   {	setTitle("Fantasy Solar System");
      setSize(600, 600);
      util = new GLSLUtils();
      myCanvas = new GLCanvas();
      myCanvas.addGLEventListener(this);
      myCanvas.setFocusable(true);
      myCanvas.addKeyListener(this);
      camera = new Camera(2.0f, 2.0f, 20.0f); // make camera object
      getContentPane().add(myCanvas);
      this.setVisible(true);
      // JOGL FPSanimator
      FPSAnimator animator = new FPSAnimator(myCanvas, 50);
      animator.start();
      myCanvas.requestFocus();
   }
   
   public void keyPressed(KeyEvent e) {} // unused method from interface
   
   public void keyTyped(KeyEvent e) {} // unused method from interface
   
   // handle various keyboard commands
   public void keyReleased(KeyEvent e) {
   
      switch (e.getKeyCode()) {
         case KeyEvent.VK_W:
            camera.forward(); // key w
            break;
         case KeyEvent.VK_S:
            camera.backward(); // key s
            break;
         case KeyEvent.VK_A:
            camera.strafeLeft(); // key a
            break;
         case KeyEvent.VK_D:
            camera.strafeRight(); // key d
            break;
         case KeyEvent.VK_E: 
            camera.down(); // key e
            break;
         case KeyEvent.VK_Q:
            camera.up(); // key q
            break;
         case KeyEvent.VK_LEFT:
            camera.panLeft(); // key left arrow
            break;
         case KeyEvent.VK_RIGHT:
            camera.panRight(); // key right arrow
            break;
         case KeyEvent.VK_UP:
            camera.pitchUp(); // key up arrow
            break;
         case KeyEvent.VK_DOWN:
            camera.pitchDown(); // key down arrow
            break;
         case KeyEvent.VK_SPACE: // toggle the visibility of the world axes
            if(worldAxesFlag == 0) { // key spacebar
               worldAxesFlag = 1;
            }
            else if (worldAxesFlag == 1) {
               worldAxesFlag = 0;
            }
            break;
         default:
            break;
      }
   }
   
   // a single set of colored axes showing the world XYZ axis locations
   public void drawWorldAxes(GL4 gl, int mv_loc) {
      if (worldAxesFlag == 1) {
         mvStack.pushMatrix();
         gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]); // X axis
         gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(0);
         
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
         gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(1);
         gl.glActiveTexture(GL_TEXTURE0);
         gl.glBindTexture(GL_TEXTURE_2D, redTexture);
         gl.glEnable(GL_CULL_FACE);
         gl.glFrontFace(GL_CCW);
         gl.glEnable(GL_DEPTH_TEST);
         gl.glDepthFunc(GL_LEQUAL);
         
         gl.glDrawArrays(GL_LINES, 0, 2);
         mvStack.popMatrix();
         
         mvStack.pushMatrix();
         gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]); // Y axis
         gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(0);
         
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
         gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(1);
         gl.glActiveTexture(GL_TEXTURE0);
         gl.glBindTexture(GL_TEXTURE_2D, greenTexture);
         gl.glEnable(GL_CULL_FACE);
         gl.glFrontFace(GL_CCW);
         gl.glEnable(GL_DEPTH_TEST);
         gl.glDepthFunc(GL_LEQUAL);
         
         gl.glDrawArrays(GL_LINES, 0, 2);
         mvStack.popMatrix();
         
         mvStack.pushMatrix();
         gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]); // Z axis
         gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(0);
         
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
         gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
         gl.glEnableVertexAttribArray(1);
         gl.glActiveTexture(GL_TEXTURE0);
         gl.glBindTexture(GL_TEXTURE_2D, blueTexture);
         gl.glEnable(GL_CULL_FACE);
         gl.glFrontFace(GL_CCW);
         gl.glEnable(GL_DEPTH_TEST);
         gl.glDepthFunc(GL_LEQUAL);
         
         gl.glDrawArrays(GL_LINES, 0, 2);
         mvStack.popMatrix();
      } else {}
   }

   public void display(GLAutoDrawable drawable) {
      GL4 gl = (GL4) GLContext.getCurrentGL();
   
      gl.glClear(GL_DEPTH_BUFFER_BIT);
      
      float bkg[] = {0.0f, 0.0f, 0.0f, 1.0f };
      FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
      gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
      
      gl.glUseProgram(rendering_program);
   
      int mv_loc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
      int proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");
   
      float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
      Matrix3D pMat = perspective(60.0f, aspect, 0.1f, 1000.0f);
      
      double amt = (double)(System.currentTimeMillis()) / 1000.0;
         
   	// building viewing matrices for controlling a camera
      camera.pushCameraMatrix(mvStack);
      
      // building matrix transformations and sending them as uniform variables
      gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);

      drawWorldAxes(gl, mv_loc);

   	// ----------------------  sphere == sun  
      mvStack.pushMatrix(); // push sun
      mvStack.translate(0, 0, 0); // sun is located at the world origin
   	mvStack.pushMatrix();
      // rate of rotation = (System.currentTimeMillis()) / 25.0
      mvStack.rotate((System.currentTimeMillis()) / 25.0, 0.0, 1.0, 0.0); // rotate on Y axis
      mvStack.scale(2.0, 2.0, 2.0);
      gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
      gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(0);
      
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
      gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(1);
      gl.glActiveTexture(GL_TEXTURE0);
      gl.glBindTexture(GL_TEXTURE_2D, centerTexture);
      
      gl.glEnable(GL_CULL_FACE);
      gl.glFrontFace(GL_CCW);
      gl.glEnable(GL_DEPTH_TEST);
      gl.glDepthFunc(GL_LEQUAL);
      
      int numVerts = mySphere.getIndices().length;
      gl.glDrawArrays(GL_TRIANGLES, 0, numVerts);
      mvStack.popMatrix(); // pop sun
   	
   	//-----------------------  WeirdPyramid planet 1
      mvStack.pushMatrix();
      // orbital speed = amt
      mvStack.translate(Math.sin(amt) * 4.0f, 0.0f, Math.cos(amt) * 4.0f);
      mvStack.pushMatrix();
      // rate of rotation = (System.currentTimeMillis()) / 20.0
      mvStack.rotate((System.currentTimeMillis()) / 20.0, 1.0, 0.0, 0.0); // rotate on X axis
      gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
      gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(0);
      
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
      gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(1);
      gl.glActiveTexture(GL_TEXTURE0);
      gl.glBindTexture(GL_TEXTURE_2D, planet1Texture);

      gl.glDrawArrays(GL_TRIANGLES, 0, 30);
      mvStack.popMatrix();
      
      //-----------------------  WeirdPyramid moon 1
      mvStack.pushMatrix();
      // orbital speed = amt * 1.2
      mvStack.translate(0.0f, Math.sin(amt * 1.2) * 2.0f, Math.cos(amt * 1.2) * 2.0f);
      mvStack.pushMatrix();
      // rate of rotation = (System.currentTimeMillis()) / 10.0
      mvStack.rotate((System.currentTimeMillis()) / 10.0, 1.0, 0.0, 0.0); // rotate on X axis
      mvStack.scale(0.75, 0.75, 0.75); // scaled down
      gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
      gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(0);
      
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
      gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(1);
      gl.glActiveTexture(GL_TEXTURE0);
      gl.glBindTexture(GL_TEXTURE_2D, moon1Texture);

      gl.glDrawArrays(GL_TRIANGLES, 0, 30);
      mvStack.popMatrix(); 
      mvStack.popMatrix(); 
      mvStack.popMatrix(); 
      mvStack.popMatrix();
      
      //----------------- WeirdPyramid planet 2
      mvStack.pushMatrix();
      // orbital speed = amt * 1.4
      mvStack.translate(Math.sin(amt * 1.4) * 8.0f, 0.0f, Math.cos(amt * 1.4) * 8.0f);
      mvStack.pushMatrix();
      // rate of rotation = (System.currentTimeMillis())/15.0
      mvStack.rotate((System.currentTimeMillis()) / 15.0, 0.0, 0.0, 1.0); // rotate on Z axis
      gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
      gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(0);
      
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
      gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(1);
      gl.glActiveTexture(GL_TEXTURE0);
      gl.glBindTexture(GL_TEXTURE_2D, planet2Texture);

      
      gl.glDrawArrays(GL_TRIANGLES, 0, 30);
      mvStack.popMatrix();
   
   	//----------------------- sphere moon 2
      mvStack.pushMatrix();
      // orbital speed = amt * 1.6
      mvStack.translate(0.0f, Math.sin(amt * 1.6) * 2.0f, Math.cos(amt * 1.6) * 2.0f);
      // rate of rotation = (System.currentTimeMillis()) / 12.0
      mvStack.rotate((System.currentTimeMillis()) / 12.0, 0.0, 0.0, 1.0); // rotate on Z axis
      mvStack.scale(0.75, 0.75, 0.75);
      gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
      
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
      gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(0);

      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
      gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
      gl.glEnableVertexAttribArray(1);
      gl.glActiveTexture(GL_TEXTURE0);
      gl.glBindTexture(GL_TEXTURE_2D, moon2Texture);


      gl.glDrawArrays(GL_TRIANGLES, 0, numVerts); 
      
      // pop all off
      mvStack.popMatrix();  
      mvStack.popMatrix();
      mvStack.popMatrix();
   }

   public void init(GLAutoDrawable drawable) {	
      GL4 gl = (GL4) GLContext.getCurrentGL();
   
      rendering_program = createShaderProgram();
      setupTextures();
      setupVertices();
   }
   
   public void setupTextures() {
   
      // using texture images
      joglCenterTexture = loadTexture("sunmap.jpg"); // for "sun" center planet
      centerTexture = joglCenterTexture.getTextureObject();
      
      joglPlanet1Texture = loadTexture("brick1.jpg");
      planet1Texture = joglPlanet1Texture.getTextureObject();
      
      joglMoon1Texture = loadTexture("fishy.jpg"); // I made this one
      moon1Texture = joglMoon1Texture.getTextureObject();
      
      joglPlanet2Texture = loadTexture("floor_nmap.jpg");
      planet2Texture = joglPlanet2Texture.getTextureObject();
      
      joglMoon2Texture = loadTexture("earthmap1k.jpg");
      moon2Texture = joglMoon2Texture.getTextureObject();
      
      joglRedTexture = loadTexture("red.jpg"); // X axis
      redTexture = joglRedTexture.getTextureObject();
      
      joglGreenTexture = loadTexture("green.jpg"); // Y axis
      greenTexture = joglGreenTexture.getTextureObject();
      
      joglBlueTexture = loadTexture("blue.jpg"); // Z axis
      blueTexture = joglBlueTexture.getTextureObject();
   
   }

   private void setupVertices()
   {	GL4 gl = (GL4) GLContext.getCurrentGL();
   
      Vertex3D[] vertices = mySphere.getVertices();
      int[] indices = mySphere.getIndices();
   	
      float[] pvalues = new float[indices.length * 3];
      float[] tvalues = new float[indices.length * 2];
      float[] nvalues = new float[indices.length * 3];
   	
      for (int i = 0; i < indices.length; i++)
      {	pvalues[i * 3] = (float) (vertices[indices[i]]).getX();
         pvalues[i * 3 + 1] = (float) (vertices[indices[i]]).getY();
         pvalues[i * 3 + 2] = (float) (vertices[indices[i]]).getZ();
         tvalues[i * 2] = (float) (vertices[indices[i]]).getS();
         tvalues[i * 2 + 1] = (float) (vertices[indices[i]]).getT();
         nvalues[i * 3] = (float) (vertices[indices[i]]).getNormalX();
         nvalues[i * 3 + 1] = (float)(vertices[indices[i]]).getNormalY();
         nvalues[i * 3 + 2] = (float) (vertices[indices[i]]).getNormalZ();
      }
         
      float[] xAxis = 
         { -100.0f, 0.0f, 0.0f, 
           100.0f, 0.0f, 0.0f 
         };
   	float[] yAxis = 
         { 0.0f, -100.0f, 0.0f, 
           0.0f, 100.0f, 0.0f 
         };
      float[] zAxis = 
         { 0.0f, 0.0f, -100.0f, 
           0.0f, 0.0f, 100.0f 
         };
      
      float[] weirdPyramid_positions = weirdPyramid.getVertices();
      
      float[] weirdPyramidTexture_coordinates = weirdPyramid.getTextureCoordinates();
      
      gl.glGenVertexArrays(vao.length, vao, 0);
      gl.glBindVertexArray(vao[0]);
      gl.glGenBuffers(vbo.length, vbo, 0);
   
      // using buffers to send vertex models and texture coordinates through
      // vertex attributes
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
      FloatBuffer pyrBuf = Buffers.newDirectFloatBuffer(weirdPyramid_positions); //pyramid_positions
      gl.glBufferData(GL_ARRAY_BUFFER, pyrBuf.limit() * 4, pyrBuf, GL_STATIC_DRAW);
   	
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
      FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
      gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL_STATIC_DRAW);
      
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
      FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
      gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL_STATIC_DRAW);
      
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
      FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
      gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit() * 4,norBuf, GL_STATIC_DRAW);
   
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
      FloatBuffer xAxisBuf = Buffers.newDirectFloatBuffer(xAxis);
      gl.glBufferData(GL_ARRAY_BUFFER, xAxisBuf.limit() * 4, xAxisBuf, GL_STATIC_DRAW);
   
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
      FloatBuffer yAxisBuf = Buffers.newDirectFloatBuffer(yAxis);
      gl.glBufferData(GL_ARRAY_BUFFER, yAxisBuf.limit() * 4, yAxisBuf, GL_STATIC_DRAW);
      
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
      FloatBuffer zAxisBuf = Buffers.newDirectFloatBuffer(zAxis);
      gl.glBufferData(GL_ARRAY_BUFFER, zAxisBuf.limit() * 4, zAxisBuf, GL_STATIC_DRAW);
   
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
      FloatBuffer tex2Buf = Buffers.newDirectFloatBuffer(weirdPyramidTexture_coordinates); //texture_coordinates
      gl.glBufferData(GL_ARRAY_BUFFER, tex2Buf.limit() * 4, tex2Buf, GL_STATIC_DRAW);

   }

   private Matrix3D perspective(float fovy, float aspect, float n, float f)
   {	float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
      float A = q / aspect;
      float B = (n + f) / (n - f);
      float C = (2.0f * n * f) / (n - f);
      Matrix3D r = new Matrix3D();
      r.setElementAt(0, 0, A);
      r.setElementAt(1, 1, q);
      r.setElementAt(2, 2, B);
      r.setElementAt(3, 2, -1.0f);
      r.setElementAt(2, 3, C);
      r.setElementAt(3, 3, 0.0f);
      return r;
   }

   public static void main(String[] args) { 
      //new FantasySolarSystem(); 
   }
   public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
   public void dispose(GLAutoDrawable drawable) {}

   private int createShaderProgram()
   {	GL4 gl = (GL4) GLContext.getCurrentGL();
   
      // printing JOGL, OpenGL and Java versions
      System.out.println( "JOGL Version: " + Package.getPackage("com.jogamp.opengl").getImplementationVersion() );
      System.out.println("OpenGL Version: " + gl.glGetString(GL.GL_VERSION));
      System.out.println("Java Version: " + System.getProperty("java.version"));
   
      int[] vertCompiled = new int[1];
		int[] fragCompiled = new int[1];
		int[] linked = new int[1];
   
      String vshaderSource[] = util.readShaderSource("solarsystem/vert.shader");
      String fshaderSource[] = util.readShaderSource("solarsystem/frag.shader");
      int lengths[];
   
      int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
      gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0);
      gl.glCompileShader(vShader);
      
      util.checkOpenGLError();  // can use returned boolean
		gl.glGetShaderiv(vShader, GL_COMPILE_STATUS, vertCompiled, 0);
		if (vertCompiled[0] == 1) {
         System.out.println("vertex compilation success");
		} else {	
         System.out.println("vertex compilation failed");
			util.printShaderLog(vShader);
		}
      
      int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
      gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);
      gl.glCompileShader(fShader);
   
      util.checkOpenGLError();  // can use returned boolean
		gl.glGetShaderiv(fShader, GL_COMPILE_STATUS, fragCompiled, 0);
		if (fragCompiled[0] == 1) {
         System.out.println("fragment compilation success");
		} else {	
         System.out.println("fragment compilation failed");
			util.printShaderLog(fShader);
		}
   
      int vfprogram = gl.glCreateProgram();
      gl.glAttachShader(vfprogram, vShader);
      gl.glAttachShader(vfprogram, fShader);
      
      gl.glLinkProgram(vfprogram);
      util.checkOpenGLError();
		gl.glGetProgramiv(vfprogram, GL_LINK_STATUS, linked, 0);
		if (linked[0] == 1) {
         System.out.println("linking succeeded");
		} else {	
         System.out.println("linking failed");
			util.printProgramLog(vfprogram);
		}
      
      return vfprogram;
   }
   
   public Texture loadTexture(String textureFileName)
   {	Texture tex = null;
      try { tex = TextureIO.newTexture(new File(textureFileName), false); }
      catch (Exception e) { e.printStackTrace(); }
      return tex;
   }
}