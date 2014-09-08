package maneuvers;

import dump.Astrophysics;
import dump.Burn;
import dump.Craft;
import dump.Vector3D;

public class BiElliptic extends Maneuver{
	//Algorithm 37 from Fundamentals of Astrodynamics and Applications
	public BiElliptic(Craft craft, double rB, double rFinal) {
		this.burns = new Burn[3];
		double rInitial = craft.position.clone().subtract(craft.parent.position).magnitude();
		double grav = craft.parent.mass * Astrophysics.G;
		
		//Calculate semi-major axes of transfer orbits
		double aTrans1 = (rInitial + rB)/2;
		double aTrans2 = (rB + rFinal)/2;
		
		//Calculate velocities for transfers
		double vInitial = Math.sqrt(grav/rInitial);
		double vTrans1A = Math.sqrt((2*grav/rInitial)-(grav/aTrans1));
		double vTrans1B = Math.sqrt((2*grav/rB)-(grav/aTrans1));
		double vTrans2B = Math.sqrt((2*grav/rB)-(grav/aTrans2));
		double vTrans2C = Math.sqrt((2*grav/rFinal)-(grav/aTrans2));
		double vFinal = Math.sqrt(grav/rFinal);
		
		//Find difference to calculate delta-v
		double deltaVA = vTrans1A - vInitial;
		double deltaVB = vTrans2B - vTrans1B;
		double deltaVC = vFinal - vTrans2C;
		this.deltaV = Math.abs(deltaVA) + Math.abs(deltaVB) + Math.abs(deltaVC);
		
		//Calculate length of transit times
		double tTrans1 = Math.PI * Math.sqrt(aTrans1*aTrans1*aTrans1/grav);
		double tTrans2 = Math.PI * Math.sqrt(aTrans2*aTrans2*aTrans2/grav);
		
		//Convert delta-v to vectors
		Vector3D prograde = craft.velocity.clone().subtract(craft.parent.velocity).normalize();
		Circularize c = new Circularize(craft);
		this.burns[0] = new Burn(prograde.clone().multiply(deltaVA).add(c.burns[0].deltaV), 0);
		this.burns[1] = new Burn(prograde.clone().multiply(-deltaVB), tTrans1);
		this.burns[2] = new Burn(prograde.clone().multiply(deltaVC), tTrans1 + tTrans2);
	}
}
