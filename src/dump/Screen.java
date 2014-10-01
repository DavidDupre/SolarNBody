package dump;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

public class Screen {

	public static final boolean isLit = true;
	
	double centerDistance = 50f, yaw = 60, pitch = -180; // initial camera
															// position
	int focusID = 3; // The ID of the body to center the camera on
	int prevFocus = 0;
	List<Body> bodies;
	List<Craft> ships;
		
	public Screen(List<Body> bodies, List<Craft> ships) throws LWJGLException, InterruptedException {
		this.bodies = bodies;
		this.ships = ships;
		
		// Initialize display
		double width = 1600 * .5;
		double height = 900 * .5;
		Display.setDisplayMode(new DisplayMode((int) width, (int) height));
		Display.setTitle("N-body sim");
		Display.create();
		GL11.glClearColor(0f, 0f, 0f, 0f); // the blackness of space
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		double aspect = height / width;
		GL11.glLoadIdentity();
		GL11.glFrustum(-horizontalTan, horizontalTan, aspect * horizontalTan,
				aspect * -horizontalTan, 1, 10000000);
			
		lightItUp();
	}
	
	private void lightItUp() {
		initLightArrays();
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, matSpecular);
		GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, 50.0f);	
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, lightPosition);	
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, whiteLight);
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, whiteLight);	
		GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, lModelAmbient);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_LIGHT0);	
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT_AND_DIFFUSE);
	}
	
	//Lighting variables
	private FloatBuffer matSpecular;
	private FloatBuffer lightPosition;
	private FloatBuffer whiteLight; 
	private FloatBuffer lModelAmbient;
	
	private void initLightArrays() {
		matSpecular = BufferUtils.createFloatBuffer(4);
		matSpecular.put(1.0f).put(1.0f).put(1.0f).put(1.0f).flip();
		
		lightPosition = BufferUtils.createFloatBuffer(4);
		lightPosition.put(1.0f).put(1.0f).put(1.0f).put(0.0f).flip();
		
		whiteLight = BufferUtils.createFloatBuffer(4);
		whiteLight.put(1.0f).put(1.0f).put(1.0f).put(1.0f).flip();
		
		lModelAmbient = BufferUtils.createFloatBuffer(4);
		lModelAmbient.put(0.5f).put(0.5f).put(0.5f).put(1.0f).flip();
	}
	

	private double horizontalTan = Math.tan(Math.toRadians(25));
	private boolean quit = false;

	public void run(ReentrantLock physicsLock) {
		while (!Display.isCloseRequested() && !quit){
			// update 1 frame of display
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();
			GL11.glTranslated(0, 0, -centerDistance);
			GL11.glRotated(pitch, 1, 0, 0);
			GL11.glRotated(yaw, 0, 0, 1);
			GL11.glScaled(1E-7, 1E-7, 1E-7);	
	
			
			physicsLock.lock(); // keeps physics from updating at the same time
			focus();
			for (Body b : bodies) {
				b.draw(centerDistance);
			}
			for (Craft c : ships) {
				c.draw(centerDistance);
			}
			physicsLock.unlock();
	
			pollInput();
	
			Display.update();
			Display.sync(60);
		}
	}

	public void focus() {
		if (prevFocus!=focusID){
			//Keeps the zoom level in proportion to the size of the focus body. Not sure if I like this.
			//centerDistance *= bodies.get(focusID).radius/bodies.get(prevFocus).radius;
			prevFocus = focusID;
		}
		Body body = bodies.get(focusID);
		GL11.glTranslated(-body.position.x, -body.position.y, -body.position.z);
	}
	
	private void setFocus(int i){
		prevFocus = focusID;
		focusID = i;
	}
	
	private void toggleTrail() {
		for (int i=0; i < bodies.size(); i++){
			bodies.get(i).hasTrail = !bodies.get(i).hasTrail;
		}
	}
	
	private void boostFocus() {
		bodies.get(focusID).velocity.multiply(1.1);
	}

	public void pollInput() {
		// Handles keyboard and mouse input
		if (Mouse.isButtonDown(1)) {
			// Right-click to adjust pitch, yaw
			int mouseDX = Mouse.getDX();
			int mouseDY = Mouse.getDY();

			if ((mouseDY > 0 && pitch < 0) || (mouseDY < 0 && pitch > -180)) {
				pitch += mouseDY / 1.5;
			}
			yaw += mouseDX / 2;
		}

		double zoom = Mouse.getDWheel() * (Math.abs(centerDistance) / 1000);
		centerDistance -= zoom;

		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
					quit = true;
				}
			} else {
				switch (Keyboard.getEventKey()) {
				case Keyboard.KEY_1:
					setFocus(1);
					break;
				case Keyboard.KEY_2:
					setFocus(2);
					break;
				case Keyboard.KEY_3:
					setFocus(3);
					break;
				case Keyboard.KEY_4:
					setFocus(4);
					break;
				case Keyboard.KEY_5:
					setFocus(5);
					break;
				case Keyboard.KEY_6:
					setFocus(6);
					break;
				case Keyboard.KEY_7:
					setFocus(7);
					break;
				case Keyboard.KEY_8:
					setFocus(8);
					break;
				case Keyboard.KEY_9:
					setFocus(9);
					break;
				case Keyboard.KEY_0:
					setFocus(0);
					break;
				case Keyboard.KEY_T:
					toggleTrail();
					break;
				case Keyboard.KEY_W:
					boostFocus();
					break;
				}
			}
		}
	}

}
