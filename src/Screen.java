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

	double centerDistance = 1000f, yaw = 60, pitch = -135; // initial camera
															// position
	int focusID = 0; // The ID of the body to center the camera on
	int prevFocus = 0;
	List<Body> bodies;
		
	public Screen(List<Body> bodies) throws LWJGLException, InterruptedException {
		this.bodies = bodies;
		
		// Initialize display
		double width = 1600 * .5;
		double height = 900 * .5;
		Display.setDisplayMode(new DisplayMode((int) width, (int) height));
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
		isLit = true;
	}
	
	//Lighting variables
	private boolean isLit = false;
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
			focus(bodies.get(focusID));
			for (Body b : bodies) {
				drawSphere(b);
				GL11.glDisable(GL11.GL_LIGHTING);
				if (b.hasTrail) {
					b.trail.renderTrail(b.color);
				}
			}
			physicsLock.unlock();
	
			pollInput();
	
			Display.update();
			Display.sync(60);
		}
	}

	public void drawSphere(Body body) {
		GL11.glPushMatrix();
		GL11.glTranslated(body.position.x, body.position.y, body.position.z);
		GL11.glColor3f(body.color[0], body.color[1], body.color[2]);
		
		if (isLit){
			if (body.parent != null){
				GL11.glEnable(GL11.GL_LIGHTING);
				lightPosition = BufferUtils.createFloatBuffer(4);
				lightPosition.put((float)(-body.position.x)).put((float)(-body.position.y)).put((float)(-body.position.z)).put(0.0f).flip();
				GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, lightPosition);	
			}
			else{
				GL11.glDisable(GL11.GL_LIGHTING);
			}
		}
		
		body.image.draw((float) body.getSize(), 16, 16);
		GL11.glDisable(GL11.GL_LIGHTING);
		
		if (body.drawBigSphere){
			//draw not-to-scale translucent sphere
			GL11.glColor4f(body.color[0], body.color[1], body.color[2], .5f);
			body.image.draw((float) (100000*centerDistance), 16, 16);
		} else {
			GL11.glBegin(GL11.GL_POINTS);
			GL11.glVertex3d(0, 0, 0);
			GL11.glEnd();
		}
		
		if (body.drawConic){
			if (body.parent != null){
				//Draw the orbit path
				HashMap<String, Double> orb = Astrophysics.toOrbitalElements(body);
				double v = orb.get("v"); //get the current true anomaly so the ellipse passes through the body and looks good
				Conic conic = new Conic(orb);
				GL11.glBegin(GL11.GL_LINE_STRIP);
				GL11.glColor3f(body.color[0], body.color[1], body.color[2]);
				double e = orb.get("e");
				if (e > 1) {
					if (body.parent.parent != null){
						//Move to your grandparents' if you run away from home
						body.parent = body.parent.parent;
						orb = Astrophysics.toOrbitalElements(body);
					} else {
						//Draw a hyperbola excluding the ugly bits
						for (int i=0; i < 50; i++){ //Draw forwards
							Vector3D r = conic.getPosition(i*Math.PI/49 + v);
							r.subtract(body.position.clone().subtract(body.parent.position));
							if (r.magnitude() > 1e12){
								break;
							}
							GL11.glVertex3d(r.x, r.y, r.z);
						}
						GL11.glEnd();
						GL11.glBegin(GL11.GL_LINE_STRIP);
						for (int i=0; i < 50; i++){ //Draw backwards
							Vector3D r = conic.getPosition(-i*Math.PI/49 + v);
							r.subtract(body.position.clone().subtract(body.parent.position));
							if (r.magnitude() > 1e12){
								break;
							}
							GL11.glVertex3d(r.x, r.y, r.z);
						}
					}
				} else {
					//Draw an elliptical orbit
					for (int i=0; i < 100; i++){
						Vector3D r = conic.getPosition(i*Math.PI/49 + v);
						r.subtract(body.position.clone().subtract(body.parent.position));
						GL11.glVertex3d(r.x, r.y, r.z);
					}
				}
			}
		} 
		GL11.glEnd();
		GL11.glPopMatrix();
	}

	public void focus(Body body) {
		if (prevFocus!=focusID){
			//Keeps the zoom level in proportion to the size of the focus body. Not sure if I like this.
			//centerDistance *= bodies.get(focusID).radius/bodies.get(prevFocus).radius;
			prevFocus = focusID;
		}
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
