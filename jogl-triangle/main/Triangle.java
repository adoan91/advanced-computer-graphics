package main;

import java.nio.*;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.GLContext;
import com.jogamp.common.nio.Buffers;

import com.jogamp.opengl.util.*;
import graphicslib3D.GLSLUtils.*;
import graphicslib3D.*;

import java.awt.*;
import java.awt.event.*;

import java.lang.Math.*;

public class Triangle extends JFrame implements GLEventListener, KeyListener, MouseWheelListener
{	private GLCanvas myCanvas;
   private int rendering_program;
   private int vao[] = new int[1];
   private GLSLUtils util = new GLSLUtils();
   
   private int cFlag = 0; // color change on/off flag
   private float cf = 0.0f; // color flag for the color flag within shader
   
   private float inc = 0.01f; // speed for vertical movement
   private float x = 0.0f;
   private float y = 0.0f;
   
   private JPanel btnPanel;
   private JButton btnCircle;
   private JButton btnVert;
   
   private int flag = 0; // flag for motion of triangle 0 = still, 1 = vertical, 2 = circle
   private int sizeFlag = 0; // size change flag when = 1 means size needs to change
   
   private float size; // track the size incase it gets too big or too small
   
   private int theta; // angle for circle movement

   public Triangle()
   {	setTitle("CSC155 a1");
      setSize(500, 500);
      setLayout(new BorderLayout());
      
      theta = 45; // starting at 45 degrees
      
      btnPanel = new JPanel();
      btnPanel.setLayout(new FlowLayout());
      btnCircle = new JButton("Circle");
      btnVert = new JButton("Vertical");
      btnPanel.add(btnCircle);
      btnPanel.add(btnVert);
      
      btnCircle.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e){
                  flag = 2;
                  myCanvas.requestFocus(); // keep glcanvas focused
               }
            });
        
      btnVert.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent e){
                  flag = 1;
                  myCanvas.requestFocus(); // keep glcanvas focused
               }
            });
        
      myCanvas = new GLCanvas();
      myCanvas.addGLEventListener(this);
      myCanvas.setFocusable(true);
      myCanvas.addKeyListener(this);
      myCanvas.addMouseWheelListener(this);
   	
      getContentPane().add(myCanvas, BorderLayout.CENTER);
      getContentPane().add(btnPanel, BorderLayout.NORTH);
      setVisible(true);
      
      FPSAnimator animator = new FPSAnimator(myCanvas, 30);
      animator.start();
      myCanvas.requestFocus();
   }
   
   public void mouseWheelMoved(MouseWheelEvent e) {
      if(e.getWheelRotation() == 1) {
         size = size + 0.05f;
      } 
      else if(e.getWheelRotation() == -1) {
         size = size - 0.05f;
      }
      if(size < 0.05f) {
         size = 0.05f;
      }
      if(size > 1.0f) {
         size = 1.0f;
      }
      
      sizeFlag = 1;
      
      myCanvas.requestFocus(); // keep glcanvas focused
   }
   
   public void keyPressed(KeyEvent e) { // unused method from interface
      //System.out.println("keyPressed");
   }
   
   public void keyTyped(KeyEvent e) { // unused method from interface
      //System.out.println("keyTyped");
   }
   
   public void keyReleased(KeyEvent e) {
      if(e.getKeyCode()== KeyEvent.VK_K) {
         if( cFlag == 0) {
            cFlag = 1;
         } else {
            // do nothing
         }
      }
   }
   
   public void vertical(GL4 gl) {
   
      y += inc;   
      if (y > 1.0f) inc = -0.01f;
      if (y < -1.0f) inc = 0.01f;
      
      int offset_loc = gl.glGetUniformLocation(rendering_program, "iny");
      gl.glProgramUniform1f(rendering_program, offset_loc, y);
   
      gl.glDrawArrays(GL_TRIANGLES,0,3);
   
   }
   
   public void circle(GL4 gl) { 
   
      theta ++;//= (int) Math.toRadians(10);
      x = (float)(Math.cos(theta)*0.5);
      y = (float)(Math.sin(theta)*0.5);
            
      int offset_locc = gl.glGetUniformLocation(rendering_program, "inx");
      gl.glProgramUniform1f(rendering_program, offset_locc, x);
      
      int offset_loc = gl.glGetUniformLocation(rendering_program, "iny");
      gl.glProgramUniform1f(rendering_program, offset_loc, y);
   
      gl.glDrawArrays(GL_TRIANGLES,0,3);
   
   }
   
   public void changeColor(GL4 gl) {
         if(cf == 0.0f) {
            cf = 1.0f;
         } else {
            cf = 0.0f;
         }
   
         int offset_locc = gl.glGetUniformLocation(rendering_program, "cf");
         gl.glProgramUniform1f(rendering_program, offset_locc, cf);
      
         gl.glDrawArrays(GL_TRIANGLES,0,3);
   }

   public void display(GLAutoDrawable drawable)
   {	GL4 gl = (GL4) GLContext.getCurrentGL();
      gl.glUseProgram(rendering_program);
   
      float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f };
      FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
      gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
      
      if (cFlag == 1) { // change color from key press
         changeColor(gl);
         cFlag = 0;
      }
   
      if (sizeFlag == 1) { // change size
         int offset_locc = gl.glGetUniformLocation(rendering_program, "ins");
         gl.glProgramUniform1f(rendering_program, offset_locc, size);
      
         gl.glDrawArrays(GL_TRIANGLES,0,3);
         
         sizeFlag = 0;
      }
   
      if (flag == 0) { // triangle motion
         gl.glDrawArrays(GL_TRIANGLES,0,3);
      } 
      else if (flag == 1) {
         vertical(gl);
      } 
      else if (flag == 2) {
         circle(gl);
      }
   	
   }

   public void init(GLAutoDrawable drawable)
   {	GL4 gl = (GL4) GLContext.getCurrentGL();
      rendering_program = createShaderProgram();
      gl.glGenVertexArrays(vao.length, vao, 0);
      gl.glBindVertexArray(vao[0]);
   }

   private int createShaderProgram()
   {	GL4 gl = (GL4) GLContext.getCurrentGL();
   
      // printing JOGL and OpenGL versions
      System.out.println( "JOGL Version: " + Package.getPackage("com.jogamp.opengl").getImplementationVersion() );
      System.out.println("OpenGL Version: " + gl.glGetString(GL.GL_VERSION));
   
      int[] vertCompiled = new int[1];
		int[] fragCompiled = new int[1];
		int[] linked = new int[1];
   
      String vshaderSource[] = util.readShaderSource("a1/vert.shader");
      String fshaderSource[] = util.readShaderSource("a1/frag.shader");
      int lengths[];
   
      int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
      gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0);
      gl.glCompileShader(vShader);
      
      util.checkOpenGLError();  // can use returned boolean
		gl.glGetShaderiv(vShader, GL_COMPILE_STATUS, vertCompiled, 0);
		if (vertCompiled[0] == 1)
		{	System.out.println("vertex compilation success");
		} else
		{	System.out.println("vertex compilation failed");
			util.printShaderLog(vShader);
		}
      
      int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
      gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);
      gl.glCompileShader(fShader);
      
      util.checkOpenGLError();  // can use returned boolean
		gl.glGetShaderiv(fShader, GL_COMPILE_STATUS, fragCompiled, 0);
		if (fragCompiled[0] == 1)
		{	System.out.println("fragment compilation success");
		} else
		{	System.out.println("fragment compilation failed");
			util.printShaderLog(fShader);
		}
   
      int vfprogram = gl.glCreateProgram();
      gl.glAttachShader(vfprogram, vShader);
      gl.glAttachShader(vfprogram, fShader);
      
      gl.glLinkProgram(vfprogram);
      util.checkOpenGLError();
		gl.glGetProgramiv(vfprogram, GL_LINK_STATUS, linked, 0);
		if (linked[0] == 1)
		{	System.out.println("linking succeeded");
		} else
		{	System.out.println("linking failed");
			util.printProgramLog(vfprogram);
		}
      
      return vfprogram;
   }

   //public static void main(String[] args) { new Triangle(); }
   public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
   public void dispose(GLAutoDrawable drawable) {}
}