package dump;
import java.nio.FloatBuffer;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Sphere;

public class Body extends Thing{
	// Variables from csv file
	public int id = 0;
	public String name;
	public double mass = 1f;
	public double radius = 1;
	public double semiMajorAxis;
	public double systemMass;
	public double period;
	public String type = "Moon";
	public double obliquity = 0;

	public Vector3D velocity = new Vector3D();
	public Vector3D position = new Vector3D();
	public Vector3D acceleration = new Vector3D();
	public Body parent;
	public Sphere image = new Sphere();
	public double sizeScale = 1;
	public boolean hasTrail = false;
	public Trail trail;
	public float[] color = {1f,1f,1f};
	public boolean drawBigSphere = false;
	public boolean drawConic = true;
	
	public void setSize(double size) {
		radius = size;
		this.sizeScale = (double) (size / Math.pow(mass, 0.33333));
	}
	
	public double getSize() {
		return (double) Math.pow(mass, 0.33333) * sizeScale;
	}	
	
	private FloatBuffer lightPosition;
	
	public Body() {
		lightPosition = BufferUtils.createFloatBuffer(4);
		lightPosition.put(1.0f).put(1.0f).put(1.0f).put(0.0f).flip();
	}
	
	public void draw(double centerDistance) {
		GL11.glPushMatrix();
		GL11.glTranslated(position.x, position.y, position.z);
		GL11.glColor3f(color[0], color[1], color[2]);
		
		//Draw physical object
		if (Screen.isLit){
			if (parent != null){
				GL11.glEnable(GL11.GL_LIGHTING);
				lightPosition = BufferUtils.createFloatBuffer(4);
				lightPosition.put((float)(-position.x)).put((float)(-position.y)).put((float)(-position.z)).put(0.0f).flip();
				GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, lightPosition);	
			} else{
				GL11.glDisable(GL11.GL_LIGHTING);
			}
		}
		
		image.draw((float) getSize(), 16, 16);
		GL11.glDisable(GL11.GL_LIGHTING);
		
		
		//Draw conic section
		if (parent != null && drawConic) {
			Vector3D r = position.clone().subtract(parent.position); //getRelativePosition();
			Vector3D v = velocity.clone().subtract(parent.velocity);
			HashMap<String, Double> orb = Astrophysics.toOrbitalElements(r, v, parent);
			double anomaly = orb.get("v"); //get the current true anomaly so the ellipse passes through the body and looks good
			Conic conic = new Conic(orb);
			GL11.glBegin(GL11.GL_LINE_STRIP);		
			for (int i=0; i < 100; i++){
				Vector3D pos = conic.getPosition(i*Math.PI/49 + anomaly);
				pos.subtract(r);
				GL11.glVertex3d(pos.x, pos.y, pos.z);
			}
			GL11.glEnd();
		}
		
		
		//Draw a transparent sphere proportional to the zoom level (stays the same size on screen)
		if (drawBigSphere) {
			GL11.glColor4f(color[0], color[1], color[2], .5f);
			image.draw((float) (100000*centerDistance), 16, 16);
		} else { 
			GL11.glBegin(GL11.GL_POINTS);
			GL11.glVertex3d(0, 0, 0);
			GL11.glEnd();
		}
		
		
		if (hasTrail) {
			trail.renderTrail(color);
		}
		
		GL11.glEnd();
		GL11.glPopMatrix();
	}
}
