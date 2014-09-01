package dump;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.lwjgl.Sys;

public class RungeKutta extends Thread {
	double rSquared;
	private double timeScale;
	private List<Body> bodies;
	private ReentrantLock physicsLock;

	private Thread thread;

	public RungeKutta(double multiplier, List<Body> bodies, ReentrantLock physicsLock) {
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
	
	private Vector3D getAcceleration(Body body1, Vector3D position){
		Vector3D aTotal = new Vector3D();
		for (int i=0; i < bodies.size(); i++) {
			Body body2 = bodies.get(i);
			if (body1 == body2) {
				break;
			}
			rSquared = position.clone().distSquared(body2.position);
			if (rSquared == 0 || body1.mass == 0) {
				break;
			}
			Vector3D a = Vector3D.normalize(
					position.clone().subtract(body2.position)).multiply(
					6.67384E-11 * body2.mass / -rSquared);
			aTotal.add(a);
		}
		return aTotal;
	}

	Vector3D[] k1 = new Vector3D[2];
	Vector3D[] v1 = new Vector3D[2];
	Vector3D[] k2 = new Vector3D[2];
	Vector3D[] v2 = new Vector3D[2];
	Vector3D[] k3 = new Vector3D[2];
	Vector3D[] v3 = new Vector3D[2];
	Vector3D[] k4 = new Vector3D[2];
	
	private void rungeKutta(double h) {
		Vector3D kAverage0 = new Vector3D();
		Vector3D kAverage1 = new Vector3D();
		for (int i=0; i < bodies.size(); i++) {
			k1[0] = bodies.get(i).velocity;
			k1[1] = getAcceleration(bodies.get(i), bodies.get(i).position);
			
			v1[0] = bodies.get(i).position.clone().add(k1[0].multiply(h/2));
			v1[1] = bodies.get(i).velocity.clone().add(k1[1].multiply(h/2));
			
			k2[0] = v1[1];
			k2[1] = getAcceleration(bodies.get(i), v1[0]);
			
			v2[0] = bodies.get(i).position.clone().add(k2[0].multiply(h/2));
			v2[1] = bodies.get(i).velocity.clone().add(k2[1].multiply(h/2)); 

			k3[0] = v2[1];
			k3[1] = getAcceleration(bodies.get(i), v2[0]);
			
			v3[0] = bodies.get(i).position.clone().add(k3[0].multiply(h));
			v3[1] = bodies.get(i).velocity.clone().add(k3[1].multiply(h)); 
			
			k4[0] = v3[1];
			k4[1] = getAcceleration(bodies.get(i), v3[0]);
			
			kAverage0 = k1[0].add(k2[0].multiply(2).add(k3[0].multiply(2).add(k4[0]))).multiply(h/6);
			kAverage1 = k1[1].add(k2[1].multiply(2).add(k3[1].multiply(2).add(k4[1]))).multiply(h/6);
		}
		
		for (int i=0; i < bodies.size(); i++) {
			Body b = bodies.get(i);
			b.position.add(kAverage0);
			b.velocity.add(kAverage1);
			if (b.hasTrail){
				b.trail.makeTrail();
			}
		}
	}
	
	double delta;
	
	public void run() {
		double t = timeScale;
		int i = 0;
		while (true) { // please don't slow me down if I'm going too fast	
			physicsLock.lock(); // keeps screen from trying to update at the same time
			rungeKutta(t);
			physicsLock.unlock();
			if (i>1000){
				System.out.println(bodies.get(0).position);
				i = 0;
			}
			i++;
		}
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}
}
