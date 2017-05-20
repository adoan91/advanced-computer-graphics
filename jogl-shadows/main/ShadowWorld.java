package main;

import main.Camera;

import graphicslib3D.*;
import graphicslib3D.light.*;
import graphicslib3D.GLSLUtils.*;
import graphicslib3D.shape.*;

import java.nio.*;
import javax.swing.*;

import java.awt.event.*;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.common.nio.Buffers;

import com.jogamp.opengl.util.texture.*;
import java.io.*;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.Graphics2D;

public class ShadowWorld extends JFrame implements GLEventListener, KeyListener, MouseWheelListener, MouseListener
{	private GLCanvas myCanvas;
	private Material thisMaterial;
	private String[] vBlinn1ShaderSource, vBlinn2ShaderSource, fBlinn2ShaderSource;
	private int rendering_program1, rendering_program2;
	private int vao[] = new int[1];
	private int vbo[] = new int[15];
	private int mv_location, proj_location, vertexLoc, n_location;
	private float aspect;
	private GLSLUtils util = new GLSLUtils();
	
	// locationa
	private Point3D torusLoc = new Point3D(1.6, 0.0, -0.3);
	private Point3D pyrLoc = new Point3D(-1.0, 0.1, 0.3);
	private Point3D shuttleLoc = new Point3D(-2.5, 0.5, 0.5);
   private Point3D cameraLoc = new Point3D(-1.0, 3.0, 11.0);
	private Point3D lightLoc = new Point3D(-4.0f, 1.2f, 2.0f);
   private Point3D purpleDolphinLoc = new Point3D(0.0, 5.0, 0.0);
   private Point3D dolphinLoc = new Point3D(3.0, 0.0, -3.0);
	
	private Matrix3D m_matrix = new Matrix3D();
	private Matrix3D v_matrix = new Matrix3D();
	private Matrix3D mv_matrix = new Matrix3D();
	private Matrix3D proj_matrix = new Matrix3D();
	
	// light stuff
	private float [] globalAmbient = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
	private PositionalLight currentLight = new PositionalLight();
	
	// shadow stuff
	private int scSizeX, scSizeY;
	private int [] shadow_tex = new int[1];
	private int [] shadow_buffer = new int[1];
	private Matrix3D lightV_matrix = new Matrix3D();
	private Matrix3D lightP_matrix = new Matrix3D();
	private Matrix3D shadowMVP1 = new Matrix3D();
	private Matrix3D shadowMVP2 = new Matrix3D();
	private Matrix3D b = new Matrix3D();

	// model stuff
	private ImportedModel pyramid = new ImportedModel("pyr.obj");
   private ImportedModel shuttle = new ImportedModel("shuttle.obj");
   private ImportedModel dolphin = new ImportedModel("dolphinHighPoly.obj");
	private Torus myTorus = new Torus(0.6f, 0.4f, 48);
   private Sphere mySphere = new Sphere(48);
   
	private int numPyramidVertices, numTorusVertices;
   private int numShuttleVertices, numDolphinVertices;
	
   private Camera camera;
   private int firstTexture;
   private Texture joglFirstTexture;
   private int secondTexture;
   private Texture joglSecondTexture;
   
   private int redTexture;
   private Texture joglRedTexture;
   private int greenTexture;
   private Texture joglGreenTexture;
   private int blueTexture;
   private Texture joglBlueTexture;
   private int yellowTexture;
   private Texture joglYellowTexture;
   
   private float lightFlag;
   private int worldAxesFlag;
   
   float[] initialLightAmb = new float[3];
   float[] initialLightDiff = new float[3];
   float[] initialLightSpec = new float[3];
   
   private int prevMouseX;
   
	public ShadowWorld()
	{	setTitle("a3");
		setSize(800, 800);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
      myCanvas.addKeyListener(this);
      myCanvas.addMouseWheelListener(this);
      myCanvas.addMouseListener(this);
      camera = new Camera(cameraLoc.getX(),cameraLoc.getY(),cameraLoc.getZ());
		getContentPane().add(myCanvas);
		setVisible(true);
		FPSAnimator animator = new FPSAnimator(myCanvas, 30);
		animator.start();
      myCanvas.requestFocus();
	}
   
   
   
   public void mouseWheelMoved (MouseWheelEvent e) {
      if (lightFlag == 1) {
         lightLoc.setY(lightLoc.getY() + (-1 * e.getWheelRotation()));
      }
   }
   
   public void mouseClicked(MouseEvent e) {}
   
   public void mouseEntered(MouseEvent e) {}
   
   public void mouseExited(MouseEvent e) {}
   
   public void mousePressed(MouseEvent e) {
      prevMouseX = e.getX();
   }
   
   public void mouseReleased(MouseEvent e) { 
      if (lightFlag == 1) {
         if (e.getButton() == MouseEvent.BUTTON1) { // left click drag
            if (e.getX() > prevMouseX) {
               // dragged right
               // move light right (+X)
               lightLoc.setX(lightLoc.getX() + 1);
//                System.out.println("prevMouseX: " + prevMouseX + "\nReleaseMouseX: " + e.getX());
            }
            else if (e.getX() == prevMouseX) {
               // do nothing
            } else {
               // dragged left
               // move light left (-X)
               lightLoc.setX(lightLoc.getX() - 1);
//                System.out.println("prevMouseX: " + prevMouseX + "\nReleaseMouseX: " + e.getX());
            }
         }
         
         if (e.getButton() == MouseEvent.BUTTON3) { // right click drag
            if (e.getX() > prevMouseX) {
               // dragged right
               // move light forward  (+Z)
               lightLoc.setZ(lightLoc.getZ() + 1);
//                System.out.println("prevMouseX: " + prevMouseX + "\nReleaseMouseX: " + e.getX());
            }
            else if (e.getX() == prevMouseX) {
               // do nothing
            } else {
               // dragged left
               // move light back (-Z)
               lightLoc.setZ(lightLoc.getZ() - 1);
//                System.out.println("prevMouseX: " + prevMouseX + "\nReleaseMouseX: " + e.getX());
            }
         }
      }
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
         case KeyEvent.VK_M: // toggle positional light
            if(lightFlag == 0) { 
               lightFlag = 1;
               currentLight.setAmbient(initialLightAmb);
               currentLight.setDiffuse(initialLightDiff);
               currentLight.setSpecular(initialLightSpec);
            }
            else if (lightFlag == 1) {
               lightFlag = 0;
            }
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

	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
      
      if (lightFlag == 1) {
         currentLight.setPosition(lightLoc);
      } else {
         currentLight.setAmbient(new float[] {0.0f, 0.0f, 0.0f, 0.0f});
         currentLight.setDiffuse(new float[] {0.0f, 0.0f, 0.0f, 0.0f});
         currentLight.setSpecular(new float[] {0.0f, 0.0f, 0.0f, 0.0f});
      }
   
		
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		proj_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);
		
		float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);

		gl.glBindFramebuffer(GL_FRAMEBUFFER, shadow_buffer[0]);
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadow_tex[0], 0);
	
		gl.glDrawBuffer(GL_NONE);
		gl.glEnable(GL_DEPTH_TEST);

		gl.glEnable(GL_POLYGON_OFFSET_FILL);	// for reducing
		gl.glPolygonOffset(2.0f, 4.0f);			//  shadow artifacts

		passOne();
		
		gl.glDisable(GL_POLYGON_OFFSET_FILL);	// artifact reduction, continued
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
      gl.glGenerateMipmap(GL_TEXTURE_2D);
      
      if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")) { 
         float max[ ] = new float[1];  
         gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max, 0);  
         gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, max[0]); 
      }
	
		gl.glDrawBuffer(GL_FRONT);
		
		passTwo();
      
      
      
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passOne()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		gl.glUseProgram(rendering_program1);
		
		Point3D origin = new Point3D(0.0, 0.0, 0.0);
		Vector3D up = new Vector3D(0.0, 1.0, 0.0);
		lightV_matrix.setToIdentity();
		lightP_matrix.setToIdentity();
	
		lightV_matrix = lookAt(currentLight.getPosition(), origin, up);	// vector from light to origin
		lightP_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);
      int shadow_location = gl.glGetUniformLocation(rendering_program1, "shadowMVP");
   
		// draw the dolphin
		
		m_matrix.setToIdentity();
		m_matrix.translate(dolphinLoc.getX(),dolphinLoc.getY(),dolphinLoc.getZ());
      m_matrix.scale(5.0, 5.0, 5.0);		
      
      shadowMVP1.setToIdentity();
      shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
   
		shadowMVP1.concatenate(m_matrix);

		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
      
		// set up dolphin vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);	
	
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, dolphin.getNumVertices());

		// ---- draw the pyramid
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(pyrLoc.getX(),pyrLoc.getY(),pyrLoc.getZ());
		m_matrix.rotateX(30.0);
		m_matrix.rotateY(40.0);

		shadowMVP1.setToIdentity();
      shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
   
		shadowMVP1.concatenate(m_matrix);

		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

      // gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, pyramid.getNumVertices());
      
      // ---- draw the shuttle
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(shuttleLoc.getX(),shuttleLoc.getY(),shuttleLoc.getZ());
		m_matrix.rotateX(30.0);
		m_matrix.rotateY(40.0);

		shadowMVP1.setToIdentity();

		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
      shadowMVP1.concatenate(m_matrix);

		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

      // gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, shuttle.getNumVertices());
      
      
      // ---- draw the dolphin
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(purpleDolphinLoc.getX(),purpleDolphinLoc.getY(),purpleDolphinLoc.getZ());
      m_matrix.scale(1.5, 1.5, 1.5);
		

		
      shadowMVP1.setToIdentity();

		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);

		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

      // gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
      gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, dolphin.getNumVertices());
      
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passTwo()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	   gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glUseProgram(rendering_program2);
		
      float [] jadeAmb = new float[] {0.135f, 0.2225f, 0.1575f, 0.135f };
      float [] jadeDif = new float[] {0.54f, 0.89f, 0.63f, 0.135f}; 
      float [] jadeSpec = new float[] {0.3162f, 0.3162f, 0.3162f, 1.35f};
      
		mv_location = gl.glGetUniformLocation(rendering_program2, "mv_matrix");
		proj_location = gl.glGetUniformLocation(rendering_program2, "proj_matrix");
		n_location = gl.glGetUniformLocation(rendering_program2, "normalMat");
		int shadow_location = gl.glGetUniformLocation(rendering_program2,  "shadowMVP");
		
      //  build the VIEW matrix
		v_matrix.setToIdentity();
		//v_matrix.translate(-cameraLoc.getX(),-cameraLoc.getY(),-cameraLoc.getZ());
      v_matrix = camera.pushCameraMatrix();
      
      // draw the shuttle
		gl.glDisableVertexAttribArray(2);
		thisMaterial = graphicslib3D.Material.GOLD;		
		installLights(rendering_program2, v_matrix);
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(shuttleLoc.getX(),shuttleLoc.getY(),shuttleLoc.getZ());
		m_matrix.rotateX(30.0);
		m_matrix.rotateY(40.0);

		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
	   shadowMVP2.concatenate(lightP_matrix);
	   shadowMVP2.concatenate(lightV_matrix);
      shadowMVP2.concatenate(m_matrix);

		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

      //texture
      // gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
// 		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
// 		gl.glEnableVertexAttribArray(2);
//       
//       gl.glActiveTexture(GL_TEXTURE1);
// 		gl.glBindTexture(GL_TEXTURE_2D, redTexture);
//       gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
//       gl.glGenerateMipmap(GL_TEXTURE_2D);
// 
//       if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")) { 
//          float max[ ] = new float[1];  
//          gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max, 0);  
//          gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, max[0]); 
//       }

		// set up normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

      //gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, shuttle.getNumVertices());
            
      // ---- draw the dolphin
      
		thisMaterial = new Material(); // made a purple material
      thisMaterial.setAmbient(new float[] {0.335f, 0.4225f, 0.5575f, 0.135f});
      thisMaterial.setDiffuse(new float[] {0.74f, 0.29f, 0.73f, 0.135f});
      thisMaterial.setSpecular(new float[] {0.4162f, 0.9162f, 0.1162f, 1.35f});
      thisMaterial.setShininess(12.8f);		

		installLights(rendering_program2, v_matrix);

		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(purpleDolphinLoc.getX(),purpleDolphinLoc.getY(),purpleDolphinLoc.getZ());
      m_matrix.scale(1.5, 1.5, 1.5);

		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);

	   shadowMVP2.concatenate(lightP_matrix);
	   shadowMVP2.concatenate(lightV_matrix);
      shadowMVP2.concatenate(m_matrix);

		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
		
      // vertices
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
          
      //texture
//       gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
// 		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
// 		gl.glEnableVertexAttribArray(2);
//       
//       gl.glActiveTexture(GL_TEXTURE1);
// 		gl.glBindTexture(GL_TEXTURE_2D, secondTexture);
//       gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
//       gl.glGenerateMipmap(GL_TEXTURE_2D);
//       
//       if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")) { 
//          float max[ ] = new float[1];  
//          gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max, 0);  
//          gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, max[0]); 
//       }
      
      // normals
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
      
      //gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
      gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, dolphin.getNumVertices());
      
      // draw the dolphin
      
      //  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(dolphinLoc.getX(),dolphinLoc.getY(),dolphinLoc.getZ());
      m_matrix.scale(5.0, 5.0, 5.0);
      
		thisMaterial = new Material(); // light jade material		
		thisMaterial.setAmbient(jadeAmb);
      thisMaterial.setDiffuse(jadeDif);
      thisMaterial.setSpecular(jadeSpec);
      thisMaterial.setShininess(12.8f);

		installLights(rendering_program2, v_matrix);
		
		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
      shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
	   shadowMVP2.concatenate(lightP_matrix);
	   shadowMVP2.concatenate(lightV_matrix);
      shadowMVP2.concatenate(m_matrix);
      
      //  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
		
		
		// set up dolphin vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

      //texture
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
      
      gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, secondTexture);
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
      gl.glGenerateMipmap(GL_TEXTURE_2D);
      
      if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")) { 
         float max[ ] = new float[1];  
         gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max, 0);  
         gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, max[0]); 
      }

		// set up dolphin normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);	
	
		// gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	   		
      gl.glDrawArrays(GL_TRIANGLES, 0, dolphin.getNumVertices());
      
      if (lightFlag == 1) {
         
         // dot
         m_matrix.setToIdentity();
   		m_matrix.translate(lightLoc.getX(),lightLoc.getY(),lightLoc.getZ());
         m_matrix.scale(0.1, 0.1, 0.1);
         
         //  build the MODEL-VIEW matrix
   		mv_matrix.setToIdentity();
   		mv_matrix.concatenate(v_matrix);
   		mv_matrix.concatenate(m_matrix);
   		
   		shadowMVP2.setToIdentity();
   		shadowMVP2.concatenate(b);
   		shadowMVP2.concatenate(lightP_matrix);
   		shadowMVP2.concatenate(lightV_matrix);
   		shadowMVP2.concatenate(m_matrix);
   		
   		//  put the MV and PROJ matrices into the corresponding uniforms
   		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
   		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
   		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
   		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
   		
   		// set up light vertices buffer
   		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
   		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
   		gl.glEnableVertexAttribArray(0);
         
         //texture
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
   		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
   		gl.glEnableVertexAttribArray(2);
         
         gl.glActiveTexture(GL_TEXTURE1);
   		gl.glBindTexture(GL_TEXTURE_2D, yellowTexture);
         gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
         gl.glGenerateMipmap(GL_TEXTURE_2D);
         
         if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")) { 
            float max[ ] = new float[1];  
            gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max, 0);  
            gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, max[0]); 
         }
   
   		// set up light normals buffer
   		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
   		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
   		gl.glEnableVertexAttribArray(1);	
   	
   		//gl.glClear(GL_DEPTH_BUFFER_BIT);
   		gl.glEnable(GL_CULL_FACE);
   		gl.glFrontFace(GL_CCW);
   		gl.glEnable(GL_DEPTH_TEST);
   		gl.glDepthFunc(GL_LEQUAL);
   	   		
         gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
      }

      if (worldAxesFlag == 1) {
         // draw the x axis
   		//  build the MODEL matrix
   		m_matrix.setToIdentity();
   		m_matrix.translate(0.0, 0.0, 0.0);

   		//  build the MODEL-VIEW matrix
   		mv_matrix.setToIdentity();
   		mv_matrix.concatenate(v_matrix);
   		mv_matrix.concatenate(m_matrix);
   		
   		shadowMVP2.setToIdentity();
   		shadowMVP2.concatenate(b);
      		
		   shadowMVP2.concatenate(lightP_matrix);
		   shadowMVP2.concatenate(lightV_matrix);
         shadowMVP2.concatenate(m_matrix);
   
   		//  put the MV and PROJ matrices into the corresponding uniforms
   		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
   		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
   		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
   		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
   		
   		// set up vertices buffer
   		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
   		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
   		gl.glEnableVertexAttribArray(0);
         
         // test texture
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
   		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
   		gl.glEnableVertexAttribArray(2);
         
         gl.glActiveTexture(GL_TEXTURE1);
   		gl.glBindTexture(GL_TEXTURE_2D, redTexture);
         gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
         gl.glGenerateMipmap(GL_TEXTURE_2D);
         
         if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")) { 
            float max[ ] = new float[1];  
            gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max, 0);  
            gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, max[0]); 
         }
        
         //gl.glClear(GL_DEPTH_BUFFER_BIT);
   		gl.glEnable(GL_CULL_FACE);
   		gl.glFrontFace(GL_CCW);
   		gl.glEnable(GL_DEPTH_TEST);
   		gl.glDepthFunc(GL_LEQUAL);
   
   		gl.glDrawArrays(GL_LINES, 0, 2);
         
         // draw the y axis
   		
   		//  build the MODEL matrix
   		m_matrix.setToIdentity();
   		m_matrix.translate(0.0, 0.0, 0.0);
   
   		//  build the MODEL-VIEW matrix
   		mv_matrix.setToIdentity();
   		mv_matrix.concatenate(v_matrix);
   		mv_matrix.concatenate(m_matrix);
   		
   		shadowMVP2.setToIdentity();
   		shadowMVP2.concatenate(b);
      		
		   shadowMVP2.concatenate(lightP_matrix);
		   shadowMVP2.concatenate(lightV_matrix);
         shadowMVP2.concatenate(m_matrix);
   
   		//  put the MV and PROJ matrices into the corresponding uniforms
   		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
   		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
   		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
   		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
   		
   		// set up vertices buffer
   		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
   		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
   		gl.glEnableVertexAttribArray(0);
         
         // test texture
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
   		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
   		gl.glEnableVertexAttribArray(2);
         
         gl.glActiveTexture(GL_TEXTURE1);
   		gl.glBindTexture(GL_TEXTURE_2D, greenTexture);
         gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
         gl.glGenerateMipmap(GL_TEXTURE_2D);
         
         if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")) { 
            float max[ ] = new float[1];  
            gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max, 0);  
            gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, max[0]); 
         }
        
         //gl.glClear(GL_DEPTH_BUFFER_BIT);
   		gl.glEnable(GL_CULL_FACE);
   		gl.glFrontFace(GL_CCW);
   		gl.glEnable(GL_DEPTH_TEST);
   		gl.glDepthFunc(GL_LEQUAL);
   
   		gl.glDrawArrays(GL_LINES, 0, 2);
         
         // draw the z axis
   		//  build the MODEL matrix
   		m_matrix.setToIdentity();
   		m_matrix.translate(0.0, 0.0, 0.0);
   
   		//  build the MODEL-VIEW matrix
   		mv_matrix.setToIdentity();
   		mv_matrix.concatenate(v_matrix);
   		mv_matrix.concatenate(m_matrix);
   		
   		shadowMVP2.setToIdentity();
   		shadowMVP2.concatenate(b);
      		
		   shadowMVP2.concatenate(lightP_matrix);
		   shadowMVP2.concatenate(lightV_matrix);
         shadowMVP2.concatenate(m_matrix);
   
   		//  put the MV and PROJ matrices into the corresponding uniforms
   		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
   		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
   		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
   		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
   		
   		// set up vertices buffer
   		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
   		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
   		gl.glEnableVertexAttribArray(0);
         
         // test texture
         gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
   		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
   		gl.glEnableVertexAttribArray(2);
         
         gl.glActiveTexture(GL_TEXTURE1);
   		gl.glBindTexture(GL_TEXTURE_2D, blueTexture);
         gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
         gl.glGenerateMipmap(GL_TEXTURE_2D);
         
         if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")) { 
            float max[ ] = new float[1];  
            gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max, 0);  
            gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, max[0]); 
         }
        
         //gl.glClear(GL_DEPTH_BUFFER_BIT);
   		gl.glEnable(GL_CULL_FACE);
   		gl.glFrontFace(GL_CCW);
   		gl.glEnable(GL_DEPTH_TEST);
   		gl.glDepthFunc(GL_LEQUAL);
   
   		gl.glDrawArrays(GL_LINES, 0, 2);
      }
		
      // draw the pyramid
		thisMaterial = graphicslib3D.Material.BRONZE;		
		installLights(rendering_program2, v_matrix);
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(pyrLoc.getX(),pyrLoc.getY(),pyrLoc.getZ());
		m_matrix.rotateX(30.0);
		m_matrix.rotateY(40.0);

		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);

		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
	   shadowMVP2.concatenate(lightP_matrix);
	   shadowMVP2.concatenate(lightV_matrix);

      shadowMVP2.concatenate(m_matrix);

		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
      
      // test texture
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
      
      gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, firstTexture);
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
      gl.glGenerateMipmap(GL_TEXTURE_2D);
      
      if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")) { 
         float max[ ] = new float[1];  
         gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max, 0);  
         gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, max[0]); 
      }

		// set up normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

      // gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, pyramid.getNumVertices());
	}

	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		createShaderPrograms();
		setupVertices();
		setupShadowBuffers();

      lightFlag = 1.0f;
      worldAxesFlag = 1;
      
      initialLightAmb = currentLight.getAmbient();
      initialLightDiff = currentLight.getDiffuse();
      initialLightSpec = currentLight.getSpecular();
      
		b.setElementAt(0,0,0.5);b.setElementAt(0,1,0.0);b.setElementAt(0,2,0.0);b.setElementAt(0,3,0.5f);
		b.setElementAt(1,0,0.0);b.setElementAt(1,1,0.5);b.setElementAt(1,2,0.0);b.setElementAt(1,3,0.5f);
		b.setElementAt(2,0,0.0);b.setElementAt(2,1,0.0);b.setElementAt(2,2,0.5);b.setElementAt(2,3,0.5f);
		b.setElementAt(3,0,0.0);b.setElementAt(3,1,0.0);b.setElementAt(3,2,0.0);b.setElementAt(3,3,1.0f);
		
		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	
   /*
      joglFirstTexture = loadTexture("fishy.jpg");
      firstTexture = joglFirstTexture.getTextureObject();
      joglSecondTexture = loadTexture("bkgd1.jpg");
      secondTexture = joglSecondTexture.getTextureObject();
      
      joglRedTexture = loadTexture("red.jpg"); // X axis
      redTexture = joglRedTexture.getTextureObject();
      
      joglGreenTexture = loadTexture("green.jpg"); // Y axis
      greenTexture = joglGreenTexture.getTextureObject();
      
      joglBlueTexture = loadTexture("blue.jpg"); // Z axis
      blueTexture = joglBlueTexture.getTextureObject();
      
      joglYellowTexture = loadTexture("yellow.jpg"); // for camera
      yellowTexture = joglYellowTexture.getTextureObject();
     */
      
      // mipmapping and anistropic filtering
      firstTexture = loadTexture("fishy.jpg");
      secondTexture = loadTexture("bkgd1.jpg");
      redTexture = loadTexture("red.jpg");
      greenTexture = loadTexture("green.jpg");
      blueTexture = loadTexture("blue.jpg");
      yellowTexture = loadTexture("yellow.jpg");      
   

   }
	
	public void setupShadowBuffers()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		scSizeX = myCanvas.getWidth();
		scSizeY = myCanvas.getHeight();
	
		gl.glGenFramebuffers(1, shadow_buffer, 0);
	
		gl.glGenTextures(1, shadow_tex, 0);
		gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
						scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glGenerateMipmap(GL_TEXTURE_2D);
      
      if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")) { 
         float max[ ] = new float[1];  
         gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max, 0);  
         gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, max[0]); 
      }
      
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
	}

// -----------------------------
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		setupShadowBuffers();
	}

	private void setupVertices()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
      
      //world axes
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
	
		// pyramid definition
		Vertex3D[] pyramid_vertices = pyramid.getVertices();
		numPyramidVertices = pyramid.getNumVertices();

		float[] pyramid_vertex_positions = new float[numPyramidVertices*3];
		float[] pyramid_normals = new float[numPyramidVertices*3];
       
      float[] pyramid_texture_coordinates = new float[numPyramidVertices*2];

		for (int i=0; i<numPyramidVertices; i++)
		{	pyramid_vertex_positions[i*3]   = (float) (pyramid_vertices[i]).getX();			
			pyramid_vertex_positions[i*3+1] = (float) (pyramid_vertices[i]).getY();
			pyramid_vertex_positions[i*3+2] = (float) (pyramid_vertices[i]).getZ();
			pyramid_texture_coordinates[i*2] = (float) (pyramid_vertices[i]).getS();
         pyramid_texture_coordinates[i*2+1] = (float) (pyramid_vertices[i]).getT();
			pyramid_normals[i*3]   = (float) (pyramid_vertices[i]).getNormalX();
			pyramid_normals[i*3+1] = (float) (pyramid_vertices[i]).getNormalY();
			pyramid_normals[i*3+2] = (float) (pyramid_vertices[i]).getNormalZ();
		}
      
      // dolphin definition
		Vertex3D[] dolphin_vertices = dolphin.getVertices();
		numDolphinVertices = dolphin.getNumVertices();

		float[] dolphin_vertex_positions = new float[numDolphinVertices*3];
		float[] dolphin_normals = new float[numDolphinVertices*3];
      
      float[] dolphin_texture_coordinates = new float[numDolphinVertices*2];
		    
		for (int i=0; i<numDolphinVertices; i++)
		{	dolphin_vertex_positions[i*3]   = (float) (dolphin_vertices[i]).getX();			
			dolphin_vertex_positions[i*3+1] = (float) (dolphin_vertices[i]).getY();
			dolphin_vertex_positions[i*3+2] = (float) (dolphin_vertices[i]).getZ();
			dolphin_texture_coordinates[i*2] = (float) (dolphin_vertices[i]).getS();
         dolphin_texture_coordinates[i*2+1] = (float) (dolphin_vertices[i]).getT();
			dolphin_normals[i*3]   = (float) (dolphin_vertices[i]).getNormalX();
			dolphin_normals[i*3+1] = (float) (dolphin_vertices[i]).getNormalY();
			dolphin_normals[i*3+2] = (float) (dolphin_vertices[i]).getNormalZ();
		}
     
      // shuttle definition
		Vertex3D[] shuttle_vertices = shuttle.getVertices();
		numShuttleVertices = shuttle.getNumVertices();

		float[] shuttle_vertex_positions = new float[numShuttleVertices*3];
		float[] shuttle_normals = new float[numShuttleVertices*3];
      float[] shuttle_texture_coordinates = new float[shuttle_vertices.length*2];

		for (int i=0; i<numShuttleVertices; i++)
		{	shuttle_vertex_positions[i*3]   = (float) (shuttle_vertices[i]).getX();			
			shuttle_vertex_positions[i*3+1] = (float) (shuttle_vertices[i]).getY();
			shuttle_vertex_positions[i*3+2] = (float) (shuttle_vertices[i]).getZ();
			shuttle_texture_coordinates[i*2] = (float) (shuttle_vertices[i]).getS();
         shuttle_texture_coordinates[i*2+1] = (float) (shuttle_vertices[i]).getT();
			shuttle_normals[i*3]   = (float) (shuttle_vertices[i]).getNormalX();
			shuttle_normals[i*3+1] = (float) (shuttle_vertices[i]).getNormalY();
			shuttle_normals[i*3+2] = (float) (shuttle_vertices[i]).getNormalZ();
		}
      
      // sphere definition
      Vertex3D[] vertices = mySphere.getVertices();
		int[] indices = mySphere.getIndices();
		
		float[] pvalues = new float[indices.length*3];
		float[] tvalues = new float[indices.length*2];
		float[] nvalues = new float[indices.length*3];
		
		for (int i=0; i<indices.length; i++)
		{	pvalues[i*3] = (float) (vertices[indices[i]]).getX();
			pvalues[i*3+1] = (float) (vertices[indices[i]]).getY();
			pvalues[i*3+2] = (float) (vertices[indices[i]]).getZ();
			tvalues[i*2] = (float) (vertices[indices[i]]).getS();
			tvalues[i*2+1] = (float) (vertices[indices[i]]).getT();
			nvalues[i*3] = (float) (vertices[indices[i]]).getNormalX();
			nvalues[i*3+1]= (float)(vertices[indices[i]]).getNormalY();
			nvalues[i*3+2]=(float) (vertices[indices[i]]).getNormalZ();
		}
      

		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);

		gl.glGenBuffers(15, vbo, 0);

		//  put the dolphin vertices into the first buffer,
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer dolphinVertBuf = Buffers.newDirectFloatBuffer(dolphin_vertex_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, dolphinVertBuf.limit()*4, dolphinVertBuf, GL_STATIC_DRAW);
		
		//  load the pyramid vertices into the second buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer pyrVertBuf = Buffers.newDirectFloatBuffer(pyramid_vertex_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrVertBuf.limit()*4, pyrVertBuf, GL_STATIC_DRAW);
		
		// load the dolphin normal coordinates into the third buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer dolphinNorBuf = Buffers.newDirectFloatBuffer(dolphin_normals);
		gl.glBufferData(GL_ARRAY_BUFFER, dolphinNorBuf.limit()*4, dolphinNorBuf, GL_STATIC_DRAW);
		
		// load the pyramid normal coordinates into the fourth buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer pyrNorBuf = Buffers.newDirectFloatBuffer(pyramid_normals);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrNorBuf.limit()*4, pyrNorBuf, GL_STATIC_DRAW);
      
      //  load the shuttle vertices into the second buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		FloatBuffer shuttleVertBuf = Buffers.newDirectFloatBuffer(shuttle_vertex_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, shuttleVertBuf.limit()*4, shuttleVertBuf, GL_STATIC_DRAW);
		
		// load the shuttle normal coordinates into the third buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		FloatBuffer shuttleNorBuf = Buffers.newDirectFloatBuffer(shuttle_normals);
		gl.glBufferData(GL_ARRAY_BUFFER, shuttleNorBuf.limit()*4, shuttleNorBuf, GL_STATIC_DRAW);
	
      
      // pyramid texture
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		FloatBuffer pyrTexBuf = Buffers.newDirectFloatBuffer(pyramid_texture_coordinates);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrTexBuf.limit()*4, pyrTexBuf, GL_STATIC_DRAW);
      
      // sphere texture
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		FloatBuffer sphereTexBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, sphereTexBuf.limit()*4, sphereTexBuf, GL_STATIC_DRAW);
      
      // sphere vertices
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		FloatBuffer sphereVertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, sphereVertBuf.limit()*4, sphereVertBuf, GL_STATIC_DRAW);
      
      // sphere normals
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		FloatBuffer sphereNorBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, sphereNorBuf.limit()*4, sphereNorBuf, GL_STATIC_DRAW);
      
      // x axis
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
      FloatBuffer xAxisBuf = Buffers.newDirectFloatBuffer(xAxis);
      gl.glBufferData(GL_ARRAY_BUFFER, xAxisBuf.limit() * 4, xAxisBuf, GL_STATIC_DRAW);
   
      // y axis
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
      FloatBuffer yAxisBuf = Buffers.newDirectFloatBuffer(yAxis);
      gl.glBufferData(GL_ARRAY_BUFFER, yAxisBuf.limit() * 4, yAxisBuf, GL_STATIC_DRAW);
      
      // z axis
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
      FloatBuffer zAxisBuf = Buffers.newDirectFloatBuffer(zAxis);
      gl.glBufferData(GL_ARRAY_BUFFER, zAxisBuf.limit() * 4, zAxisBuf, GL_STATIC_DRAW);
   
      // dolphin texture coords
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
		FloatBuffer dolphinTexBuf = Buffers.newDirectFloatBuffer(dolphin_texture_coordinates);
		gl.glBufferData(GL_ARRAY_BUFFER, dolphinTexBuf.limit()*4, dolphinTexBuf, GL_STATIC_DRAW);
      
      //shuttle texture coords
      gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
		FloatBuffer shuttleTexBuf = Buffers.newDirectFloatBuffer(shuttle_texture_coordinates);
		gl.glBufferData(GL_ARRAY_BUFFER, shuttleTexBuf.limit()*4, shuttleTexBuf, GL_STATIC_DRAW);
   }
	
	private void installLights(int rendering_program, Matrix3D v_matrix)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		Material currentMaterial = thisMaterial;
		
		Point3D lightP = currentLight.getPosition();
		Point3D lightPv = lightP.mult(v_matrix);
		
		float [] currLightPos = new float[] { (float) lightPv.getX(),
			(float) lightPv.getY(),
			(float) lightPv.getZ() };

		// get the location of the global ambient light field in the shader
		int globalAmbLoc = gl.glGetUniformLocation(rendering_program, "globalAmbient");
	
		// set the current globalAmbient settings
		gl.glProgramUniform4fv(rendering_program, globalAmbLoc, 1, globalAmbient, 0);

		// get the locations of the light and material fields in the shader
		int ambLoc = gl.glGetUniformLocation(rendering_program, "light.ambient");
		int diffLoc = gl.glGetUniformLocation(rendering_program, "light.diffuse");
		int specLoc = gl.glGetUniformLocation(rendering_program, "light.specular");
		int posLoc = gl.glGetUniformLocation(rendering_program, "light.position");

		int MambLoc = gl.glGetUniformLocation(rendering_program, "material.ambient");
		int MdiffLoc = gl.glGetUniformLocation(rendering_program, "material.diffuse");
		int MspecLoc = gl.glGetUniformLocation(rendering_program, "material.specular");
		int MshiLoc = gl.glGetUniformLocation(rendering_program, "material.shininess");

		// set the uniform light and material values in the shader
		gl.glProgramUniform4fv(rendering_program, ambLoc, 1, currentLight.getAmbient(), 0);
		gl.glProgramUniform4fv(rendering_program, diffLoc, 1, currentLight.getDiffuse(), 0);
		gl.glProgramUniform4fv(rendering_program, specLoc, 1, currentLight.getSpecular(), 0);
		gl.glProgramUniform3fv(rendering_program, posLoc, 1, currLightPos, 0);
	
		gl.glProgramUniform4fv(rendering_program, MambLoc, 1, currentMaterial.getAmbient(), 0);
		gl.glProgramUniform4fv(rendering_program, MdiffLoc, 1, currentMaterial.getDiffuse(), 0);
		gl.glProgramUniform4fv(rendering_program, MspecLoc, 1, currentMaterial.getSpecular(), 0);
		gl.glProgramUniform1f(rendering_program, MshiLoc, currentMaterial.getShininess());
	}

	public static void main(String[] args) { 
      //new ShadowWorld(); 
   }

	@Override
	public void dispose(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) drawable.getGL();
		gl.glDeleteVertexArrays(1, vao, 0);
	}

//-----------------
	private void createShaderPrograms()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] vertCompiled = new int[1];
		int[] fragCompiled = new int[1];

		vBlinn1ShaderSource = util.readShaderSource("main/blinnVert1.shader");
		vBlinn2ShaderSource = util.readShaderSource("main/blinnVert2.shader");
		fBlinn2ShaderSource = util.readShaderSource("main/blinnFrag2.shader");
      
		int vertexShader1 = gl.glCreateShader(GL_VERTEX_SHADER);
		int vertexShader2 = gl.glCreateShader(GL_VERTEX_SHADER);
		int fragmentShader2 = gl.glCreateShader(GL_FRAGMENT_SHADER);
      
		gl.glShaderSource(vertexShader1, vBlinn1ShaderSource.length, vBlinn1ShaderSource, null, 0);
		gl.glShaderSource(vertexShader2, vBlinn2ShaderSource.length, vBlinn2ShaderSource, null, 0);
		gl.glShaderSource(fragmentShader2, fBlinn2ShaderSource.length, fBlinn2ShaderSource, null, 0);

		gl.glCompileShader(vertexShader1);
		gl.glCompileShader(vertexShader2);
		gl.glCompileShader(fragmentShader2);
      
		rendering_program1 = gl.glCreateProgram();
		rendering_program2 = gl.glCreateProgram();
      
		gl.glAttachShader(rendering_program1, vertexShader1);
		gl.glAttachShader(rendering_program2, vertexShader2);
		gl.glAttachShader(rendering_program2, fragmentShader2);
      
		gl.glLinkProgram(rendering_program1);
		gl.glLinkProgram(rendering_program2);
   }

//------------------
	private Matrix3D perspective(float fovy, float aspect, float n, float f)
	{	float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
		float A = q / aspect;
		float B = (n + f) / (n - f);
		float C = (2.0f * n * f) / (n - f);
		Matrix3D r = new Matrix3D();
		r.setElementAt(0,0,A);
		r.setElementAt(1,1,q);
		r.setElementAt(2,2,B);
		r.setElementAt(3,2,-1.0f);
		r.setElementAt(2,3,C);
		r.setElementAt(3,3,0.0f);
		return r;
	}

	private Matrix3D lookAt(Point3D eye, Point3D target, Vector3D y)
	{	Vector3D eyeV = new Vector3D(eye);
		Vector3D targetV = new Vector3D(target);
		Vector3D fwd = (targetV.minus(eyeV)).normalize();
		Vector3D side = (fwd.cross(y)).normalize();
		Vector3D up = (side.cross(fwd)).normalize();
		Matrix3D look = new Matrix3D();
		look.setElementAt(0,0, side.getX());
		look.setElementAt(1,0, up.getX());
		look.setElementAt(2,0, -fwd.getX());
		look.setElementAt(3,0, 0.0f);
		look.setElementAt(0,1, side.getY());
		look.setElementAt(1,1, up.getY());
		look.setElementAt(2,1, -fwd.getY());
		look.setElementAt(3,1, 0.0f);
		look.setElementAt(0,2, side.getZ());
		look.setElementAt(1,2, up.getZ());
		look.setElementAt(2,2, -fwd.getZ());
		look.setElementAt(3,2, 0.0f);
		look.setElementAt(0,3, side.dot(eyeV.mult(-1)));
		look.setElementAt(1,3, up.dot(eyeV.mult(-1)));
		look.setElementAt(2,3, (fwd.mult(-1)).dot(eyeV.mult(-1)));
		look.setElementAt(3,3, 1.0f);
		return(look);
	}
   
   /*
   public Texture loadTexture(String textureFileName)
	{	Texture tex = null;
		try { tex = TextureIO.newTexture(new File(textureFileName), false); }
		catch (Exception e) { e.printStackTrace(); }
		return tex;
	}
   */
   // mipmapping and anistropic filtering -----------------------------------
   private int loadTexture(String textureFileName)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		BufferedImage textureImage = getBufferedImage(textureFileName);
		byte[ ] imgRGBA = getRGBAPixelData(textureImage);
		ByteBuffer rgbaBuffer = Buffers.newDirectByteBuffer(imgRGBA);
		
		int[ ] textureIDs = new int[1];				// array to hold generated texture IDs
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = textureIDs[0];				// ID for the 0th texture object
		gl.glBindTexture(GL_TEXTURE_2D, textureID);	// specifies the currently active 2D texture
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA,	// MIPMAP Level, number of color components
			textureImage.getWidth(), textureImage.getHeight(), 0,	// image size, border (ignored)
			GL_RGBA, GL_UNSIGNED_BYTE,				// pixel format and data type
			rgbaBuffer);							// buffer holding texture data
		
		// here we also demonstrate building a mipmap and using anisotropic filtering
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glGenerateMipmap(GL.GL_TEXTURE_2D);
		
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic"))
		{	float anisoset[] = new float[1];
			gl.glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, anisoset, 0);
			gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisoset[0]);
		}
		
		return textureID;
	}
	
	private BufferedImage getBufferedImage(String fileName)
	{	BufferedImage img;
		try { img = ImageIO.read(new File(fileName)); }
		catch (IOException e)
		{	System.err.println("Error reading '" + fileName + '"'); throw new RuntimeException(e); }
		return img;
	}
	
	private byte[ ] getRGBAPixelData(BufferedImage img)
	{	byte[ ] imgRGBA;
		int height = img.getHeight(null);
		int width = img.getWidth(null);
		
		WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, 4, null);
		ComponentColorModel colorModel = new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_sRGB),
			new int[ ] { 8, 8, 8, 8 }, true, false, // bits, has Alpha, isAlphaPreMultiplied
			ComponentColorModel.TRANSLUCENT, 	// transparency
			DataBuffer.TYPE_BYTE); 			// data transfer type
		BufferedImage newImage = new BufferedImage(colorModel, raster, false, null);
		
		// use an affine transform to “flip” the image to conform to OpenGL orientation.
		// In Java the origin is at the upper left of the window.
		// In OpenGL the origin is at the lower left of the canvas.
		AffineTransform gt = new AffineTransform();
		gt.translate(0, height);
		gt.scale(1, -1d);
		
		Graphics2D g = newImage.createGraphics();
		g.transform(gt);
		g.drawImage(img, null, null);
		g.dispose();
		
		DataBufferByte dataBuf = (DataBufferByte) raster.getDataBuffer();
		imgRGBA = dataBuf.getData();
		return imgRGBA;
	}
   // mipmapping and anistropic filtering -----------------------------------

}