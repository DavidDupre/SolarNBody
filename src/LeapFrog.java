import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.lwjgl.Sys;

public class LeapFrog extends Thread {
	private double timeScale;
	private List<Body> bodies;
	private ReentrantLock physicsLock;

	private Thread thread;

	public LeapFrog(double multiplier, List<Body> bodies,
			ReentrantLock physicsLock) {
		this.timeScale = multiplier;
		this.bodies = bodies;
		this.physicsLock = physicsLock;
	}

	private double getSimpleMass(Body body1, Body body2) {
		double mass = 0;
		Body ancestor = body1.parent;
		while (ancestor != null) {
			if (ancestor == body2) {
				return body2.mass;
			}
			ancestor = ancestor.parent;
		}
		if (body1.parent == body2.parent
					// Sibling case: bundle niblings with siblings
				|| body1 == body2.parent
					// Child case: bundle grandchildren with child
				|| body1.parent.parent == body2.parent) {
					// Aunt/Uncle case: bundle cousins with aunt/uncle
			return body2.systemMass;
		}
		return mass;
	}

	private Vector3D getAcceleration(Body body1) {
		// Uses system mass instead of accounting for moons
		Vector3D aTotal = new Vector3D();
		for (int i = 0; i < bodies.size(); i++) {
			Body body2 = bodies.get(i);
			if (body1 == body2 || body1.mass == 0) {
				break;
			}

			double mass = getSimpleMass(body1, body2);
			if (mass == 0) {
				break;
			}

			Vector3D diff = body2.position.clone().subtract(body1.position);
			double mag = diff.magnitude();
			Vector3D a = diff.multiply(mass / Math.pow(mag, 3));
			aTotal.add(a);
		}
		return aTotal.multiply(Body.G);
	}

	public void run() {
		Vector3D[] a = new Vector3D[bodies.size()];
		for (int i = 0; i < bodies.size(); i++) { // Necessary to preload
													// accelerations for
													// leapfrog integration.
			a[i] = getAcceleration(bodies.get(i));
		}
		double t = timeScale;
		while (true) {
			physicsLock.lock();
			for (int k = 0; k < bodies.size(); k++) {
				Body b = bodies.get(k);
				Vector3D vHalf = b.velocity.clone().add(a[k].multiply(t / 2d));
				b.position.add(vHalf.clone().multiply(t));
				a[k] = getAcceleration(b);
				b.velocity = vHalf.add(a[k].clone().multiply(t / 2.0));
			}
			physicsLock.unlock();
			for (int i=0; i<bodies.size(); i++){
				Body b = bodies.get(i);
				if (b.hasTrail) {
					b.trail.makeTrail();
				}
			}
		}
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.setName("Leapfrog");
			thread.start();
		}
	}
}
