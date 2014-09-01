package maneuvers;
import dump.Astrophysics;
import dump.Body;
import dump.Burn;
import dump.Vector3D;


public class Hohmann extends Maneuver{	
	public Hohmann(Vector3D vT, Vector3D rT, Vector3D vI, Vector3D rI, Body primary) {
		this.burns = new Burn[2];
		double grav = primary.mass * Astrophysics.G;
		
		double rTMag = rT.magnitude();
		double rIMag = rI.magnitude();
				
		//Calculate delta-v to make orbits circular
		Vector3D circT = Astrophysics.dVToCircularize(rTMag, vT, grav);
		Vector3D circI = Astrophysics.dVToCircularize(rIMag, vI, grav);
		
		//Calculate semi-major axis of the transit orbit
		double aTrans = (rIMag + rTMag)/2;
		
		double vTransA = Math.sqrt((2*grav/rIMag)-(grav/aTrans));
		double vTransB = Math.sqrt((2*grav/rTMag)-(grav/aTrans));
		
		Vector3D vINorm = vI.clone().normalize();
		
		Vector3D deltaVA = (vINorm.clone().multiply(vTransA)).subtract(vI);
		Vector3D deltaVB = vINorm.clone().multiply(-(vT.magnitude() - vTransB));
				
		deltaVA.add(circI);
		deltaVB.subtract(circT);
		
		double tTrans = Math.PI * Math.sqrt((aTrans*aTrans*aTrans)/grav);
		
		this.burns[0] = new Burn(deltaVA, 0);
		this.burns[1] = new Burn(deltaVB, tTrans);
		System.out.println(deltaVB);
		this.deltaV = deltaVA.magnitude() + deltaVB.magnitude();
	}
}
