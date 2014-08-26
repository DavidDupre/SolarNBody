import org.lwjgl.opengl.GL11;

public class Trail {
	private Vector3D[] path;
	private Body body;
	private int freq;

	public Trail(Body body, int length, int freq) {
		// Initialize a path
		this.path = new Vector3D[length];
		this.body = body;
		this.freq = freq;
		for (int i = 0; i < path.length; i++) {
			path[i] = body.position.clone();
			if (body.parent != null) {
				path[i].subtract(body.parent.position);
			}
		}
	}

	public void renderTrail(float[] color) {
		if (body.hasTrail){
			GL11.glBegin(GL11.GL_LINE_STRIP);
			GL11.glVertex3d(body.position.x, body.position.y, body.position.z);
			for (int i = 0; i < path.length; i++) {
				GL11.glColor4f(color[0], color[1], color[2],
						(float) Math.pow((1.0 - ((double) i / path.length)), .5));
				if (body.parent == null) {
					GL11.glVertex3d(path[i].x, path[i].y, path[i].z);
				} else {
					GL11.glVertex3d(path[i].x + body.parent.position.x, path[i].y
							+ body.parent.position.y, path[i].z
							+ body.parent.position.z);
				}
			}
			GL11.glEnd();
		}
	}

	private double dont = 0;

	public void makeTrail() {
		if (body.hasTrail){
			if (dont > freq) {
				for (int i = path.length - 1; i > 0; i--) {
					path[i] = path[i - 1];
				}
				path[0] = body.position.clone();
				if (body.parent != null) {
					path[0].subtract(body.parent.position);
				}
				dont = 0;
			}
			dont += freq;
		}
	}
}
