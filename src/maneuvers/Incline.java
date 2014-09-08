package maneuvers;

import java.util.HashMap;

import dump.Astrophysics;
import dump.Burn;
import dump.Craft;
import dump.Vector3D;

public class Incline extends Maneuver {
	// Algorithm 39 from Fundamentals of Astrodynamics and Applications
	public Incline(Craft craft, double deltaI) {
		this.burns = new Burn[1];
		Vector3D r = craft.position.clone().subtract(craft.parent.position);
		Vector3D v = craft.velocity.clone().subtract(craft.parent.velocity);
		HashMap<String, Double> orb = Astrophysics.toOrbitalElements(r, v,
				craft.parent);

		Vector3D[] newOrbitState = Astrophysics.toRV(orb.get("a"), orb.get("e"),
				orb.get("i") + deltaI, orb.get("node"), orb.get("peri"),
				orb.get("v"), craft.parent, false);
		
		Vector3D deltaVI = v.clone().subtract(newOrbitState[1]);
		
		this.deltaV = deltaVI.magnitude();
		System.out.println(v.magnitude());
		System.out.println(newOrbitState[1].magnitude());
		this.burns[0] = new Burn(deltaVI, 0);
	}
}
