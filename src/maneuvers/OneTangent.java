package maneuvers;

import dump.Astrophysics;
import dump.Craft;

public class OneTangent extends Maneuver{ //TODO finish this
	//Algorithm 38 from Fundamentals of Astrodynamics and Applications
	public OneTangent(Craft craft, double rFinal, double transPoint) {
		//Faster but less efficient than a Hohmann transfer
		//Decrease transPoint for a shorter transfer time
		double grav = craft.parent.mass * Astrophysics.G;
		double rInitial = craft.position.clone().subtract(craft.parent.position).magnitude();
		double invR = rInitial/rFinal;
		boolean isPeriapsis = rInitial < rFinal;
		double bigE0 = (isPeriapsis? 0 : Math.PI);
		double eTrans = (invR - 1)/(Math.cos(transPoint) + (isPeriapsis? -invR : invR));
		double aTrans = rInitial / (1 + (isPeriapsis? -eTrans : eTrans));
		
		double vInitial = Math.sqrt(grav/rInitial);
		double vFinal = Math.sqrt(grav/rFinal);
		double vTransA = Math.sqrt((2*grav/rInitial) - (grav/aTrans));
		double vTransB = Math.sqrt((2*grav/rFinal) - (grav/aTrans));
		
		double deltaVA = vTransA - vInitial;
		
		//Calculate flight-path angle
		double thetaTransB = Math.atan2(eTrans*Math.sin(transPoint), 1 + eTrans*Math.cos(transPoint));
		
		double deltaVB = Math.sqrt(vTransB*vTransB + vFinal*vFinal - 2*vTransB*vFinal*Math.cos(thetaTransB));
		this.deltaV = Math.abs(deltaVA) + Math.abs(deltaVB);
		
		double bigE = Math.acos((eTrans + Math.cos(transPoint))/(1 + eTrans*Math.cos(transPoint)));
		
		double tTrans = Math.sqrt(aTrans*aTrans*aTrans/grav);
	}
}
