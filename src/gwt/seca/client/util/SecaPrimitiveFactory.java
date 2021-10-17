package gwt.seca.client.util;

import gwt.g3d.client.primitive.MeshData;
import gwt.g3d.client.primitive.PrimitiveFactory;

public class SecaPrimitiveFactory extends PrimitiveFactory {

	/**
	 * Makes a 1 unit radius hemisphere centered at the origin and taking place in the half-space z>=0. The number of slices
	 * is the number of divisions along the equator and stacks is the number of
	 * divisions along any meridian.
	 * 
	 * @param stacks
	 * @param slices
	 */
	public static MeshData makeHemisphere(int stacks, int slices) {
		// Create all the vertex data
		int size = (stacks + 1) * (slices + 1);

		// Slices have 20 vertices each
		float[] vertices = new float[3 * size];
		float[] normals = new float[3 * size];
		float[] texCoords = new float[2 * size];
		int[] triangles = new int[6 * slices * stacks];
		
		// Create the intermediate vertices
		for (int ctr1 = 0, pos = 0; ctr1 <= stacks; ctr1++) {
			// The cosine and sine of the azimuth
			double azimuth = ctr1 * Math.PI / stacks;
			float cHeight1 = cos(azimuth);
			float sHeight1 = sin(azimuth);

			// Must duplicate the vertices on the wrapped edge to get the
			// textures to come out right
			for (int ctr2 = 0; ctr2 <= slices; ctr2++) {

				// The cosine and sine of the zenith
				double zenith = ctr2 * Math.PI / (slices-1);
				if (ctr2 == slices)
					zenith = 0;
				float cAcross1 = cos(zenith);
				float sAcross1 = sin(zenith);

				int index = pos * 3;
				
				float x = cAcross1 * sHeight1;
				float y = cHeight1;
				float z = sAcross1 * sHeight1;
				vertices[index] = x;
				vertices[index + 1] = y;
				vertices[index + 2] = z;

				float l = 1.0f / (float) Math.sqrt(x * x + y * y + z * z);

				normals[index] = x * l;
				normals[index + 1] = y * l;
				normals[index + 2] = z * l;
				
				index = pos * 2;
				texCoords[index] = 1.0f - (ctr2 / (float) slices);
				texCoords[index + 1] = ctr1 / (float) stacks;

				pos++;
			}
		}
		
		// Sets up the indices.
		for (int j = 0, index = 0; j < stacks; j++) {
			for (int i = 0; i < slices; i++) {
				int topL = j * (slices + 1) + i;
				int topR = topL + 1;
				int botL = ((j + 1) * (slices + 1) + i);
				int botR = botL + 1;

				triangles[index++] = topL;
				triangles[index++] = topR;
				triangles[index++] = botL;
				triangles[index++] = botL;
				triangles[index++] = topR;
				triangles[index++] = botR;
			}
		}
		return new MeshData(vertices, triangles, normals, texCoords);
	}
	
	/**
	 * Quick casting version of cosine
	 * 
	 * @param d
	 * @return
	 */
	private static float cos(double d) {
		return (float) Math.cos(d);
	}

	/**
	 * Quick casting version of sine
	 * 
	 * @param d
	 * @return
	 */
	private static float sin(double d) {
		return (float) Math.sin(d);
	}
}
