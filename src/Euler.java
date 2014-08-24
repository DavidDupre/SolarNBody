import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.lwjgl.Sys;

public class Euler extends Thread {
	double rSquared;
	private double timeScale;
	private List<Body> bodies;
	private ReentrantLock physicsLock;

	private Thread thread;

	public Euler(double multiplier, List<Body> bodies, ReentrantLock physicsLock) {
		this.timeScale = multiplier;
		this.bodies = bodies;
		this.physicsLock = physicsLock;
	}

	private static long getTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}

	private static long lastFrame = getTime();

	private static double getDelta() {
		long time = getTime();
		long delta = (time - lastFrame);
		lastFrame = time;
		return delta;
	}
	
	double delta;
	
	private Vector3D getForceFrom(Body body1, Body body2){
		//returns the force exerted on body1 by body2
		if (body1 == body2) {
			return new Vector3D();
		}
		rSquared = body1.position.clone().distSquared(body2.position);
		if (rSquared == 0 || body1.mass == 0) {
			return new Vector3D();
		}
		Vector3D f = Vector3D.normalize(
				body1.position.clone().subtract(body2.position)).multiply(
				Body.G * body1.mass * body2.mass / -rSquared);
		return f;
	}
	
	private void addAccelerationsQuickly(){
		//First finds the forces between bodies, then calculates accelerations
		//while reusing forces. Calculates half as many forces as the slow
		//method calculates accelerations
		
		//Build a list of forces
		Vector3D a1;
		Vector3D a2;
		Vector3D force;
		for (int i=0; i < bodies.size(); i++) {
			for (int j=i; j < bodies.size(); j++){
				force = getForceFrom(bodies.get(i), bodies.get(j));
				a1 = force.clone().multiply(1/bodies.get(i).mass);
				a2 = force.clone().multiply(-1/bodies.get(j).mass);
				bodies.get(i).acceleration.add(a1);
				bodies.get(j).acceleration.add(a2);
			}
		}
	}
	
	public void run() {
		while (true) { // please don't slow me down if I'm going too fast	
			addAccelerationsQuickly();
			physicsLock.lock(); // keeps screen from trying to update at the same time
			delta = getDelta(); // Find time since last tick
			for (int k = 0; k < bodies.size(); k++) {
				// Transfer the accelerations to velocities and positions; reset
				// acceleration to 0
				double t = timeScale; //delta * timeScale;
				Body b = bodies.get(k);
				Vector3D a = b.acceleration.clone();
				b.velocity.add(a.multiply(t));
				b.position.add(b.velocity.clone().multiply(t));
				b.acceleration.set(0, 0, 0); // reset so new forces can be added
				if (b.hasTrail){
					b.trail.makeTrail();
				}
			}
			physicsLock.unlock();
		}
	}
	
	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}
}
