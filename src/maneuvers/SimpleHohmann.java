package maneuvers;

import dump.Astrophysics;
import dump.Burn;
import dump.Craft;
import dump.Vector3D;

public class SimpleHohmann extends Maneuver{
	public SimpleHohmann(Craft craft, double r) {
		//Puts the craft in a circular orbit of semi-major axis 'r'
		//Algorithm 36 from Fundamentals of Astrodynamics and Applications
		this.burns = new Burn[2];
		Vector3D rI = craft.position.clone().subtract(craft.parent.position);
		double rIMag = rI.magnitude();
		double aTrans = (rIMag + r)/2;
		double grav = craft.parent.mass * Astrophysics.G;
		double vI = Math.sqrt(grav/rIMag);
		double vF = Math.sqrt(grav/r);
		double vTransA = Math.sqrt((2*grav/rIMag)-(grav/aTrans));
		double vTransB = Math.sqrt((2*grav/r)-(grav/aTrans));
		double deltaVA = vTransA - vI;
		double deltaVB = vF - vTransB;
		this.deltaV = Math.abs(deltaVA) + Math.abs(deltaVB);
		double tTrans = Math.PI * Math.sqrt((aTrans*aTrans*aTrans)/grav);
		
		Vector3D prograde = craft.velocity.clone().subtract(craft.parent.velocity).normalize();
		
		Circularize c = new Circularize(craft);
		this.burns[0] = new Burn(prograde.clone().multiply(deltaVA).add(c.burns[0].deltaV), 0);
		this.burns[1] = new Burn(prograde.clone().multiply(-deltaVB), tTrans);
	}
}
