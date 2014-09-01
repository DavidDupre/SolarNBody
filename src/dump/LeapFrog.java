package dump;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import maneuvers.Maneuver;

import org.lwjgl.Sys;

public class LeapFrog extends Thread {
	private double timeStep;
	private List<Body> bodies;
	private List<Craft> ships;
	private ReentrantLock physicsLock;
	private Timer timer;

	public Maneuver maneuver;

	private Thread thread;

	public LeapFrog(double multiplier, List<Body> bodies, List<Craft> ships,
			ReentrantLock physicsLock) {
		this.timeStep = multiplier;
		this.bodies = bodies;
		this.ships = ships;
		this.physicsLock = physicsLock;
		this.timer = new Timer();
		this.timer.start();
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
		) {

			return body2.systemMass;
		}
		if (body1.parent != null) {
			if (body1.parent.parent == body2.parent) {
				// Aunt/Uncle case: bundle cousins with aunt/uncle
				return body2.systemMass;
			}
		}
		return mass;
	}

	private Vector3D getAcceleration(Body body1) {
		// Uses system mass instead of accounting for moons
		Vector3D aTotal = new Vector3D();
		for (int i = 0; i < bodies.size(); i++) {
			Body body2 = bodies.get(i);
			if (body1 == body2) {
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
		return aTotal.multiply(Astrophysics.G);
	}

	private Vector3D getAccelerationForCraft(Craft craft) {
		// Uses mass of all bodies
		Vector3D aTotal = new Vector3D();
		for (int i = 0; i < bodies.size(); i++) {
			Body body = bodies.get(i);

			if (body.mass == 0) {
				break;
			}

			Vector3D diff = body.position.clone().subtract(craft.position);
			double mag = diff.magnitude();
			Vector3D a = diff.multiply(body.mass / Math.pow(mag, 3));
			aTotal.add(a);
		}
		return aTotal.multiply(Astrophysics.G);
	}

	private Vector3D[] a;
	private Vector3D[] aCraft;
	private double missionTime;

	public void run() {
		a = new Vector3D[bodies.size()];
		for (int i = 0; i < bodies.size(); i++) { // Necessary to preload
													// accelerations for
													// leapfrog integration.
			a[i] = getAcceleration(bodies.get(i));
		}
		aCraft = new Vector3D[ships.size()];
		for (int p = 0; p < ships.size(); p++) {
			aCraft[p] = getAccelerationForCraft(ships.get(p));
		}

		missionTime = 0;
		for (int j = 0; j < maneuver.burns.length; j++) {
			System.out.println("Waiting for burn " + j);
			double stopTime = maneuver.burns[j].tOF - timeStep;
			while (missionTime < stopTime) {
				simulate(timeStep);
			}
			// So that the burn occurs exactly when it should, simulate the
			// remainder of time before burning
			simulate(maneuver.burns[j].tOF - stopTime);
			ships.get(0).velocity.add(maneuver.burns[j].deltaV);
			System.out.println("Finished burn " + j);
		}
		while (true) {
			simulate(timeStep);
		}
	}

	private void simulate(double t) {
		physicsLock.lock();
		// Do physics for bodies
		for (int k = 0; k < bodies.size(); k++) {
			Body b = bodies.get(k);
			Vector3D vHalf = b.velocity.clone().add(a[k].multiply(t / 2d));
			b.position.add(vHalf.clone().multiply(t));
			a[k] = getAcceleration(b);
			b.velocity = vHalf.add(a[k].clone().multiply(t / 2.0));
		}

		// Do physics for craft
		for (int i = 0; i < ships.size(); i++) {
			Craft c = ships.get(i); // shippuden
			Vector3D vHalf = c.velocity.clone().add(aCraft[i].multiply(t / 2d));
			c.position.add(vHalf.clone().multiply(t));
			aCraft[i] = getAccelerationForCraft(c);
			c.velocity = vHalf.add(aCraft[i].clone().multiply(t / 2.0));
		}

		physicsLock.unlock();
		timer.elapsedTime += t;
		missionTime += t;
		for (int i = 0; i < bodies.size(); i++) {
			Body b = bodies.get(i);
			if (b.hasTrail) {
				b.trail.makeTrail();
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
