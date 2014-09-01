package maneuvers;
import dump.Burn;


public class Maneuver {
	public Burn[] burns;
	public double deltaV;
	
	public Maneuver() {
		this.burns = new Burn[2];
		this.deltaV = 0;
	}
}
