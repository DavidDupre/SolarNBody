package maneuvers;

import dump.Astrophysics;
import dump.Burn;
import dump.Craft;
import dump.Vector3D;

public class Circularize extends Maneuver{
	public Circularize(Craft craft) {
		//Circularize the craft's orbit to its current r
		this.burns = new Burn[1];
		double r = craft.position.clone().subtract(craft.parent.position).magnitude();
		Vector3D v = craft.velocity.clone().subtract(craft.parent.velocity);
		double grav = craft.parent.mass * Astrophysics.G;
		Vector3D deltaV = Astrophysics.dVToCircularize(r, v, grav);
		this.deltaV = deltaV.magnitude();
		this.burns[0] = new Burn(deltaV, 0);
	}
}
