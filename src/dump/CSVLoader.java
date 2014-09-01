package dump;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVLoader {

	// creates a list of bodies from a csv file. All items in the list inherit
	// from body
	
	public List<Body> bodies = new ArrayList<Body>();
	public List<Craft> ships = new ArrayList<Craft>();
	
	public CSVLoader(String bodyFile, String craftFile) {
		BufferedReader brBodies = null;
		BufferedReader brCraft = null;
		String line = "";
		String cvsSplitBy = ",";

		int n = 0;
		int p = 0;
		try {
			brBodies = new BufferedReader(new FileReader(bodyFile));
			while (((line = brBodies.readLine()) != null)) {
				String[] column = line.split(cvsSplitBy);
				if (n > 0) { // skip header row
					loadBody(column);
				}
				n++;
			}
			
			brCraft = new BufferedReader(new FileReader(craftFile));
			while (((line = brCraft.readLine()) != null)) {
				String[] column2 = line.split(cvsSplitBy);
				if (p > 0 && column2[p].length() > 0) { // skip header row
					loadCraft(column2);
				}
				p++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (brBodies != null) {
				try {
					brBodies.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (brCraft != null) {
				try {
					brCraft.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void loadBody(String[] column) {
		Body newBody = new Body();
		newBody.type = column[16];
		switch (newBody.type) {
		case "Star":
			newBody = new Star();
			break;
		case "Planet":
			newBody = new Planet(Double.parseDouble(column[14]));
			break;
		case "Dwarf Planet":
			newBody = new Planet(Double.parseDouble(column[14]));
			break;
		case "Moon":
			newBody = new Moon();
			break;
		case "Asteroid":
			newBody = new Asteroid();
			break;
		}
		newBody.id = Integer.parseInt(column[0]);
		newBody.name = column[1];
		newBody.mass = Double.parseDouble(column[3]);
		newBody.systemMass = Double.parseDouble(column[7]);
		newBody.radius = Double.parseDouble(column[4]);
		newBody.setSize(newBody.radius);
		if (Integer.parseInt(column[2]) != -1) {
			newBody.parent = bodies
					.get(Integer.parseInt(column[2]));
			double i = Double.parseDouble(column[10]);
			/*if (Integer.parseInt(column[15]) == 1){
				i += newBody.parent.obliquity;
			}*/
			Vector3D[] state = Astrophysics.toRV(
					Double.parseDouble(column[8]),
					Double.parseDouble(column[9]),
					i,
					Double.parseDouble(column[11]),
					Double.parseDouble(column[12]),
					Double.parseDouble(column[13]),
					newBody.parent,
					true);
			newBody.position = state[0]
					.add(newBody.parent.position);
			newBody.velocity = state[1]
					.add(newBody.parent.velocity);
		}
		newBody.setSemiMajorAxis(Double.parseDouble(column[8]));
		newBody.initTrail(); // Must initialize the trail after
								// finding the position so the trail
								// won't originate from the sun
		bodies.add(newBody);
	}
	
	private void loadCraft(String[] column) {
		Craft newCraft = new Craft();
		newCraft.id = Integer.parseInt(column[0]);
		newCraft.name = column[1];
		newCraft.parent = bodies
				.get(Integer.parseInt(column[2]));
		newCraft.setSemiMajorAxis(Double.parseDouble(column[3]));
		Vector3D[] state = Astrophysics.toRV(
				newCraft.semiMajorAxis,
				Double.parseDouble(column[4]),
				Double.parseDouble(column[5]),
				Double.parseDouble(column[6]),
				Double.parseDouble(column[7]),
				Double.parseDouble(column[8]),
				newCraft.parent,
				true);
		newCraft.position = state[0]
				.add(newCraft.parent.position);
		newCraft.velocity = state[1]
				.add(newCraft.parent.velocity);
		newCraft.initTrail(); // Must initialize the trail after
								// finding the position so the trail
								// won't originate from the sun
		ships.add(newCraft);
	}
}
