package main;

import graphicslib3D.*;

public class Camera {

   private Matrix3D location = new Matrix3D();
   private Matrix3D rotation = new Matrix3D();
   private Vector3D U = new Vector3D(1.0f, 0.0f, 0.0f, 0.0f);
   private Vector3D V = new Vector3D(0.0f, 1.0f, 0.0f, 0.0f);
   private Vector3D N = new Vector3D(0.0f, 0.0f, 1.0f, 0.0f);
   private Vector3D r_3 = new Vector3D(0.0f, 0.0f, 0.0f, 1.0f);
   
	public Camera(double cameraX, double cameraY, double cameraZ) {
   
		location.setElementAt(0, 3, -cameraX);
		location.setElementAt(1, 3, -cameraY);
		location.setElementAt(2, 3, -cameraZ);
		rotation.setRow(0, U);
		rotation.setRow(1, V);
		rotation.setRow(2, N);
		rotation.setRow(3, r_3);
	}

   // positive-N direction
   public void forward() {
		Vector3D out = new Vector3D(  
         (rotation.getRow(2)).getX() + (location.getCol(3)).getX(), 
         (rotation.getRow(2)).getY() + (location.getCol(3)).getY(), 
         (rotation.getRow(2)).getZ() + (location.getCol(3)).getZ(), 
         1.0f);
		location.setCol(3, out);
   }
   
   // negative-N direction
   public void backward() {
		Vector3D out = new Vector3D(  
         (location.getCol(3)).getX() - (rotation.getRow(2)).getX(), 
         (location.getCol(3)).getY() - (rotation.getRow(2)).getY(), 
         (location.getCol(3)).getZ() - (rotation.getRow(2)).getZ(), 
         1.0f);
		location.setCol(3, out);
   }

   // negative-U direction
   public void strafeLeft() {
		Vector3D out = new Vector3D(  
         (location.getCol(3)).getX() + (rotation.getRow(0)).getX(), 
         (location.getCol(3)).getY() + (rotation.getRow(0)).getY(), 
         (location.getCol(3)).getZ() + (rotation.getRow(0)).getZ(), 
         1.0f);
		location.setCol(3, out);
   }
   
   // positive-U direction
   public void strafeRight() {
		Vector3D out = new Vector3D(  
         (location.getCol(3)).getX() - (rotation.getRow(0)).getX(), 
         (location.getCol(3)).getY() - (rotation.getRow(0)).getY(), 
         (location.getCol(3)).getZ() - (rotation.getRow(0)).getZ(), 
         1.0f);
		location.setCol(3, out);
   }
   
   // negative-V direction
   public void down() {
		Vector3D out = new Vector3D(  
         (location.getCol(3)).getX() - (rotation.getRow(1)).getX(), 
         (location.getCol(3)).getY() - (rotation.getRow(1)).getY(), 
         (location.getCol(3)).getZ() - (rotation.getRow(1)).getZ(), 
         1.0f);
		location.setCol(3, out);
   }
   
   // positive-V direction
   public void up() {
		Vector3D out = new Vector3D(  
         (location.getCol(3)).getX() + (rotation.getRow(1)).getX(), 
         (location.getCol(3)).getY() + (rotation.getRow(1)).getY(), 
         (location.getCol(3)).getZ() + (rotation.getRow(1)).getZ(), 
         1.0f);
		location.setCol(3, out);
   }

   // rotate left around V axis   
   public void panLeft() {
      rotation.rotate(-5.0, rotation.getRow(1)); 
   }
   
   // rotate right around V axis
   public void panRight() {
      rotation.rotate(5.0, rotation.getRow(1)); 
   }
   
   // rotate up around U axis
   public void pitchUp() {
      rotation.rotate(-5.0, rotation.getRow(0)); 
   }
   
   // rotate down around U axis
   public void pitchDown() {
      rotation.rotate(5.0, rotation.getRow(0)); 
   }
      
   // send back view matrix
   public Matrix3D pushCameraMatrix(/*MatrixStack mvStack*/) {
      Matrix3D temp = new Matrix3D();
      temp.setToIdentity();
		temp.concatenate(rotation);
		temp.concatenate(location);
      
      return temp;
      
	}

}