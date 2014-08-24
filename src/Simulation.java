import java.util.concurrent.locks.ReentrantLock;

import org.lwjgl.LWJGLException;

public class Simulation {
	private CSVLoader data;
	private Screen graphics;
	private LeapFrog physicsEngine;
	private ReentrantLock physicsLock;

	public Simulation(String csvFile, double multiplier) throws LWJGLException, InterruptedException {
		data = new CSVLoader(csvFile);
		data.bodies.get(3).initTrail();
		graphics = new Screen(data.bodies);
		physicsLock = new ReentrantLock();
		physicsEngine = new LeapFrog(multiplier, data.bodies, physicsLock);
	}
	
	public void simulate(){
		physicsEngine.start();
		graphics.run(physicsLock);
		physicsEngine.stop();
		System.exit(0);
	}
}
