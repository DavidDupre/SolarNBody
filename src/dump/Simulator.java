package dump;
import org.lwjgl.LWJGLException;

public class Simulator {
	public static void main(String[] args) throws LWJGLException, InterruptedException{
		String bodyFile = "res/solar system elements mixed.csv";
		String craftFile = "res/craft.csv";
		double timeStep = 1;
		Simulation solarSystem = new Simulation(bodyFile, craftFile, timeStep);
		solarSystem.simulate();
	}
}
