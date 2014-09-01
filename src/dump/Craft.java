package dump;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;


public class Craft extends Thing{
	public Craft() {
		
	}
	
	public void draw(double centerDistance) {
		GL11.glPushMatrix();
		GL11.glTranslated(position.x, position.y, position.z);
		GL11.glColor3f(color[0], color[1], color[2]);
		
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
