import org.lwjgl.LWJGLException;

public class Simulator {
	public static void main(String[] args) throws LWJGLException, InterruptedException{
		Simulation solarSystem = new Simulation("/Users/David Dupre/Documents/solar system elements mixed.csv", 3000);
		solarSystem.simulate();
	}
}
