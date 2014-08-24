import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVLoader {

	// creates a list of bodies from a csv file. All items in the list inherit
	// from body

	List<Body> bodies = new ArrayList<Body>();

	public CSVLoader(String csvFile) {
		Astrophysics vallado = new Astrophysics();
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		int n = 0;
		try {
			br = new BufferedReader(new FileReader(csvFile));
			while (((line = br.readLine()) != null)) {
				String[] column = line.split(cvsSplitBy);
				if (n > 0) { // skip header row
					String type = column[15];
					Body newBody = new Body();
					switch (type) {
					case "Star":
						newBody = new Star();
						break;
					case "Planet":
						newBody = new Planet();
						break;
					case "Dwarf Planet":
						newBody = new Planet();
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
					newBody.setSemiMajorAxis(Double.parseDouble(column[8]));
					newBody.setSize(newBody.radius);
					newBody.type = column[15];
					if (Integer.parseInt(column[2]) != -1) {
						newBody.parent = bodies
								.get(Integer.parseInt(column[2]));
						Vector3D[] state = vallado.toRV(
								Double.parseDouble(column[8]),
								Double.parseDouble(column[9]),
								Double.parseDouble(column[10]),
								Double.parseDouble(column[11]),
								Double.parseDouble(column[12]),
								Double.parseDouble(column[13]),
								newBody.parent.mass * Body.G);
						newBody.position = state[0]
								.add(newBody.parent.position);
						newBody.velocity = state[1]
								.add(newBody.parent.velocity);
					}
					//newBody.initTrail(); // Must initialize the trail after
											// finding the position so the trail
											// won't originate from the sun
					bodies.add(newBody);
					n++;
				} else {
					n++;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
