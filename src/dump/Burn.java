package dump;

public class Burn {
	public Vector3D deltaV;
	public double tOF; //time of flight
	
	public Burn(Vector3D deltaV, double tOF) {
		this.deltaV = deltaV;
		this.tOF = tOF;
	}
	
	public void set(Vector3D deltaV, double tOF) {
		this.deltaV = deltaV;
		this.tOF = tOF;
	}
}
