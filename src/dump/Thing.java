package dump;
import org.lwjgl.util.glu.Sphere;


public class Thing {
	public int id;
	public String name;
	public double semiMajorAxis;
	public double period;

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
	public boolean drawConic = false;
	
	public Vector3D getRelativePosition() {
		if (parent == null) {
			return position;
		} else {
			return position.clone().subtract(parent.position);
		}
	}
	
	public Vector3D getRelativeVelocity() {
		if (parent == null) {
			return velocity;
		} else {
			return velocity.clone().subtract(parent.velocity);
		}
	}
	
	public void setSemiMajorAxis(double a){
		this.semiMajorAxis = a;
		if (parent != null){
			this.period = (4*Math.PI*Math.PI*Math.pow(semiMajorAxis, 3))/(parent.mass*Astrophysics.G);
		}
	}
	
	public void initTrail(){
		int freq = (int)(period/3.1558E7); //frequency scaled to Earth
		this.trail = new Trail(this, 100, freq);
	}
}
