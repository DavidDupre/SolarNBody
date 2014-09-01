package dump;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

//From this site: http://digitalcommons.calpoly.edu/cgi/viewcontent.cgi?article=1096&context=aerosp

public class RungeKutta2 extends Thread {
	private double timeScale;
	private List<Body> bodies;
	private ReentrantLock physicsLock;

	private Thread thread;

	public RungeKutta2(double multiplier, List<Body> bodies, ReentrantLock physicsLock) {
		this.timeScale = multiplier;
		this.bodies = bodies;
		this.physicsLock = physicsLock;
	}
	
	private Vector3D getAcceleration(Body body1, Vector3D position){
		Vector3D aTotal = new Vector3D();
		for (int i=0; i < bodies.size(); i++) {
			Body body2 = bodies.get(i);
			if (body1 == body2) {
				break;
			}
			double rSquared = position.clone().distSquared(body2.position);
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
	
	private void rungeKutta(double h) {
		Vector3D[] kv1 = new Vector3D[bodies.size()];
		Vector3D[] kr1 = new Vector3D[bodies.size()];
		Vector3D[] kv2 = new Vector3D[bodies.size()];
		Vector3D[] kr2 = new Vector3D[bodies.size()];
		Vector3D[] kv3 = new Vector3D[bodies.size()];
		Vector3D[] kr3 = new Vector3D[bodies.size()];
		Vector3D[] kv4 = new Vector3D[bodies.size()];
		Vector3D[] kr4 = new Vector3D[bodies.size()];

		for (int i=0; i < bodies.size(); i++){
			Body b = bodies.get(i);
			kv1[i] = getAcceleration(b, b.position);
			kr1[i] = b.velocity;
			kv2[i] = getAcceleration(b, b.position.clone().add((kr1[i].clone()).multiply(h/2d)));
			kr2[i] = kv1[i].clone().multiply(h/2d);
			kv3[i] = getAcceleration(b, b.position.clone().add((kr2[i].clone()).multiply(h/2d)));
			kr3[i] = kv2[i].clone().multiply(h/2d);
			kv4[i] = getAcceleration(b, b.position.clone().add((kr3[i].clone()).multiply(h)));
			kr4[i] = kv3[i].clone().multiply(h);
		}
		physicsLock.lock();
		for (int i=0; i < bodies.size(); i++){
			Body b = bodies.get(i);
			b.position.add(((kv1[i].add((kv2[i].add(kv3[i])).multiply(2))).add(kv4[i])).multiply(h/6d));
			b.velocity.add(((kr1[i].add((kr2[i].add(kr3[i])).multiply(2))).add(kr4[i])).multiply(h/6d));
			if (b.hasTrail){
				b.trail.makeTrail();
			}
		}
		physicsLock.unlock();
	}
		
	public void run() {
		double t = 5; //timeScale;
		int i = 0;
		while (true) {
			rungeKutta(t);
			if (i>1000){
				System.out.println(bodies.get(3).position);
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
