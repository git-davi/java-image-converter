package imageconverter;

import java.nio.IntBuffer;
import java.nio.FloatBuffer;

import org.jblas.FloatMatrix;
import org.tensorflow.Tensor;

public class ArrayOp {
	
	
	public static IntBuffer clipScaleAndCast(FloatBuffer buf) {
		IntBuffer newBuf = IntBuffer.allocate(buf.capacity());
		float val;
		for (int i = 0; i < buf.capacity(); i++) {
			val = buf.get(i);
			val *= 255; // scale
			val = (val > 255) ? 255 : val;
			val = (val < 0) ? 0 : val;
			newBuf.put(i, (byte) val);
		}
		
		return newBuf;
	}
	
	public static float[][][] extractChannels(int[] channels, float[][][] data) {
		float[][][] extractedChannels = new float[data.length][data[0].length][channels.length];
		
		int exChannelCounter = 0;
		for (int channel : channels) {
			
			for(int i = 0; i < data.length; i++)
				for(int j = 0; j < data[0].length; j++) 
					extractedChannels[i][j][exChannelCounter] = data[i][j][channel];
					
			exChannelCounter++;
		}
		
		return extractedChannels;
	}
	
	/*
	 * Convert matrix array to tensor [rows][columns][channels]
	 */
	public static float[][][] matrixToTensor(FloatMatrix[] matrices) {
		float[][][] tensor = new float[matrices[0].rows]
									[matrices[0].columns]
									[matrices.length];
		
		
		for (int i = 0; i < matrices[0].rows; i++) 
			for (int j = 0; j < matrices[0].columns; j++) 
				for (int k = 0; k < matrices.length; k++)
					tensor[i][j][k] = matrices[k].get(i, j);
		
		return tensor;
	}
	
	/*
	 * Method for flatting an Array.
	 */
	
	/*
	private static Stream<Object> flatten(Object[] array) {
	    return Arrays.stream(array).flatMap(o -> o instanceof Object[]? flatten((Object[])o): Stream.of(o));
	}
	*/
	
	private static float[] flatten(float[][][] array) {
		float[] flatArray = new float[array.length * array[0].length * array[0][0].length];
		int counter = 0;
		
		for (int i = 0; i < array.length; i++)
			for (int j = 0; j < array[0].length; j++)
				for (int k = 0; k < array[0][0].length; k++) {
					flatArray[counter] = array[i][j][k]; 
					counter++;
				}
		
		return flatArray;
	}
	
	/*
	 * convert tensor array to to Tensor object
	 */
	public static Tensor<?> getTensorObject(long[] shape, float[][][] tensor) {
		return Tensor.create(shape, FloatBuffer.wrap(ArrayOp.flatten(tensor)));
	}
}
