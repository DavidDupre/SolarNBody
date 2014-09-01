package dump;
import org.lwjgl.LWJGLException;

public class Simulator {
	public static void main(String[] args) throws LWJGLException, InterruptedException{
		String bodyFile = "/Users/David Dupre/Documents/solar system elements mixed.csv";
		String craftFile = "/Users/David Dupre/Documents/craft.csv";
		double timeStep = 1;
		Simulation solarSystem = new Simulation(bodyFile, craftFile, timeStep);
		solarSystem.simulate();
	}
}
