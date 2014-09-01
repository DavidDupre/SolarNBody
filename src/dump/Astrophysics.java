package dump;
import java.util.HashMap;

public class Astrophysics {
	public static /*D O*/ double G = 6.67384E-11;
	
	public static Vector3D dVToCircularize(double r, Vector3D v, double grav) {
		// Returns the delta-v needed to circularize an orbit given position,
		// velocity, and gravity of the primary
		double vCircMag = Math.sqrt(grav / r);
		Vector3D vCircVector = v.clone().normalize().multiply(vCircMag);
		Vector3D deltaV = vCircVector.subtract(v);
		return deltaV;
	}

	public static Vector3D[] toRV(double a, double e, double i, double node,
			double peri, double v, Body parent, boolean useDegrees) {
		// Inputs: semi-major axis, eccentricity, inclination, longitude of
		// ascending node,
		// argument of periapsis, true anomaly, gravitational constant of the
		// primary (central) body

		double grav = parent.mass * G;

		double p = a * (1 - e * e); // calculate semi-parameter

		if (useDegrees) {
			i = Math.toRadians(i); // convert angles to radians
			node = Math.toRadians(node);
			peri = Math.toRadians(peri);
			v = Math.toRadians(v);
		}

		// store trig variables to optimize
		double cosI = Math.cos(i);
		double sinI = Math.sin(i);
		double cosNode = Math.cos(node);
		double sinNode = Math.sin(node);
		double cosPeri = Math.cos(peri);
		double sinPeri = Math.sin(peri);
		double cosV = Math.cos(v);
		double sinV = Math.sin(v);

		Matrix rPQW = new Matrix(1, 3);
		rPQW.set(0, 0, (p * cosV) / (1 + e * cosV));
		rPQW.set(0, 1, (p * sinV) / (1 + e * cosV));
		rPQW.set(0, 2, 0);

		Matrix vPQW = new Matrix(1, 3);
		vPQW.set(0, 0, -(Math.sqrt(grav / p) * sinV));
		vPQW.set(0, 1, Math.sqrt(grav / p) * (e + cosV));
		vPQW.set(0, 2, 0);

		Matrix trans = new Matrix(3, 3);
		trans.set(0, 0, cosNode * cosPeri - sinNode * sinPeri * cosI);
		trans.set(0, 1, sinNode * cosPeri + cosNode * sinPeri * cosI);
		trans.set(0, 2, sinPeri * sinI);
		trans.set(1, 0, -cosNode * sinPeri - sinNode * cosPeri * cosI);
		trans.set(1, 1, -sinNode * sinPeri + cosNode * cosPeri * cosI);
		trans.set(1, 2, cosPeri * sinI);
		trans.set(2, 0, sinNode * sinI);
		trans.set(2, 1, -cosNode * sinI);
		trans.set(2, 2, cosI);

		Matrix rIJK = Matrix.multiply(trans, rPQW);
		Matrix vIJK = Matrix.multiply(trans, vPQW);

		Vector3D[] state = new Vector3D[2];
		state[0] = new Vector3D(rIJK.get(0, 0), rIJK.get(1, 0), rIJK.get(2, 0));
		state[1] = new Vector3D(vIJK.get(0, 0), vIJK.get(1, 0), vIJK.get(2, 0));
		return state;
	}

	public static HashMap<String, Double> toOrbitalElements(Vector3D r, Vector3D v, Body primary) {
		// Find orbital elements given position and velocity. The whole body is
		// given as input to adjust for parent bodies. Angles in radians.

		double grav = primary.mass * G;

		double rMag = r.magnitude();
		double vMag = v.magnitude();
		double rDotV = Vector3D.dotProduct(r, v);
		double twoPi = 2 * Math.PI;

		// Find specific angular momentum
		Vector3D h = Vector3D.crossProduct(r, v);

		// Find the node vector
		Vector3D k = new Vector3D(0, 0, 1);
		Vector3D n = Vector3D.crossProduct(k, h);
		double nMag = n.magnitude();

		Vector3D e = ((r.clone().multiply(vMag * vMag - (grav / rMag)))
				.subtract(v.clone().multiply(rDotV))).multiply(1 / grav);
		double eMag = e.magnitude();

		double sguig = vMag * vMag / 2 - grav / rMag;

		// Not true if e = 1
		double a = -grav / (2 * sguig);
		double p = a * (1 - eMag * eMag);

		double i = Math.acos(h.z / h.magnitude());

		double node = Math.acos(n.x / nMag);
		if (n.y < 0) {
			node = twoPi - node;
		}

		double peri = Math.acos(Vector3D.dotProduct(n, e) / (nMag * eMag));
		if (e.z < 0) {
			peri = twoPi - peri;
		}

		double anomaly = Math.acos(Vector3D.dotProduct(e, r) / (eMag * rMag));
		if (rDotV < 0) {
			anomaly = twoPi - anomaly;
		}

		HashMap<String, Double> orb = new HashMap<String, Double>();
		orb.put("p", p);
		orb.put("a", a);
		orb.put("e", eMag);
		orb.put("i", i);
		orb.put("node", node);
		orb.put("peri", peri);
		orb.put("v", anomaly);

		return orb;
	}
}
