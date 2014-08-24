public class Astrophysics {
	public Vector3D[] toRV(double a, double e, double i, double node, double peri, double v, double grav){
		//Inputs: semi-major axis, eccentricity, inclination, longitude of ascending node, 
		//argument of periapsis, true anomaly, gravitational constant of the primary (central) body
		
		double p = a * (1 - e*e); //calculate semi-parameter
		
		i = Math.toRadians(i); //convert angles to radians
		node = Math.toRadians(node);
		peri = Math.toRadians(peri);
		v = Math.toRadians(v);
		
		//store trig variables to optimize
		double cosI = Math.cos(i);
		double sinI = Math.sin(i);
		double cosNode = Math.cos(node);
		double sinNode = Math.sin(node);
		double cosPeri = Math.cos(peri);
		double sinPeri = Math.sin(peri);
		double cosV = Math.cos(v);
		double sinV = Math.sin(v);
		
		Matrix rPQW = new Matrix(1, 3);
		rPQW.set(0, 0, (p*cosV)/(1+e*cosV));
		rPQW.set(0, 1, (p*sinV)/(1+e*cosV));
		rPQW.set(0, 2, 0);
		
		Matrix vPQW = new Matrix(1, 3);
		vPQW.set(0, 0, -(Math.sqrt(grav/p)*sinV));
		vPQW.set(0, 1, Math.sqrt(grav/p)*(e+cosV));
		vPQW.set(0, 2, 0);
		
		Matrix trans = new Matrix(3, 3);
		trans.set(0, 0, cosNode*cosPeri - sinNode*sinPeri*cosI);
		trans.set(0, 1, sinNode*cosPeri + cosNode*sinPeri*cosI);
		trans.set(0, 2, sinPeri*sinI);
		trans.set(1, 0, -cosNode*sinPeri - sinNode*cosPeri*cosI);
		trans.set(1, 1, -sinNode*sinPeri + cosNode*cosPeri*cosI);
		trans.set(1, 2, cosPeri*sinI);
		trans.set(2, 0, sinNode*sinI);
		trans.set(2, 1, -cosNode*sinI);
		trans.set(2, 2, cosI);
		
		Matrix rIJK = Matrix.multiply(trans, rPQW);
		Matrix vIJK = Matrix.multiply(trans, vPQW);
		
		Vector3D[] state = new Vector3D[2];
		state[0] = new Vector3D(rIJK.get(0,0), rIJK.get(1,0), rIJK.get(2,0));
		state[1] = new Vector3D(vIJK.get(0,0), vIJK.get(1,0), vIJK.get(2,0));	
		return state;
	}
}
