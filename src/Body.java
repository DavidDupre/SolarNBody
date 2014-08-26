import org.lwjgl.util.glu.Sphere;

public class Body {
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
	public boolean drawConic = false;
	
	public static /*D O*/ double G = 6.67384E-11;
	
	public void initTrail(){
		int freq = (int)(period/3.1558E7); //frequency scaled to Earth
		this.trail = new Trail(this, 100, freq);
	}
	
	public void setSemiMajorAxis(double a){
		this.semiMajorAxis = a;
		if (parent != null){
			this.period = (4*Math.PI*Math.PI*Math.pow(semiMajorAxis, 3))/(parent.mass*G);
		}
	}
	
	public void setSize(double size) {
		radius = size;
		this.sizeScale = (double) (size / Math.pow(mass, 0.33333));
	}
	
	public double getSize() {
		return (double) Math.pow(mass, 0.33333) * sizeScale;
	}
	
}
