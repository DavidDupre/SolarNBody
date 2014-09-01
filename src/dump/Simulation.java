package dump;
import java.util.concurrent.locks.ReentrantLock;

import maneuvers.Hohmann;
import maneuvers.Maneuver;
import maneuvers.SimpleHohmann;

import org.lwjgl.LWJGLException;

public class Simulation {
	private Screen graphics;
	private LeapFrog physicsEngine;
	private ReentrantLock physicsLock;

	public Simulation(String bodyFile, String craftFile, double timeStep) throws LWJGLException,
			InterruptedException {
		CSVLoader data = new CSVLoader(bodyFile, craftFile);
		
		Body earth = data.bodies.get(3);
		earth.drawConic = true;
		Body moon = data.bodies.get(10);
		moon.drawConic = true;
		Craft arkyd = data.ships.get(0);
		arkyd.drawConic = true;
		arkyd.drawBigSphere = true;
					
		graphics = new Screen(data.bodies, data.ships);
		physicsLock = new ReentrantLock();

		Maneuver maneuver = new SimpleHohmann(arkyd, 60000000);
		physicsEngine = new LeapFrog(timeStep, data.bodies, data.ships, physicsLock);
		physicsEngine.maneuver = maneuver;
	}

	public void simulate() {
		physicsEngine.start();
		graphics.run(physicsLock);
		physicsEngine.stop();
		System.exit(0);
	}
}
