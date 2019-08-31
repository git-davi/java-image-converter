package imageconverter;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.nio.FloatBuffer;
import java.util.function.Function;

import org.jblas.FloatMatrix;


public class ImageConverter {
	
	public final static int R = 0, G = 1, B = 2;
	public final static int Y = 0, Pb = 1, Pr = 2;
	public final static int RGB_CHANNELS = 3;
	public final static int YPBPR_CHANNELS = 3;
	
	public final static int Cb = 1, Cr = 2;
	public final static int YCBCR_CHANNELS = 3;
	
	
	private static void normalizeMatricesInPlace(FloatMatrix[] matrices, float value) {
		for (int i = 0; i < matrices.length; i++) 
			matrices[i].divi(value);
	}
	
	
	/*
	 * This method takes translate the image into a 3-Dimensional tensor.
	 * Returned with the FloatMatrix array. Shape (width, height, 3)
	 */
	private static FloatMatrix[] createRGBMatricesFast(BufferedImage rgbImage) {
		FloatMatrix[] rgbImagePixels = new FloatMatrix[RGB_CHANNELS];
		FloatBuffer tmpBuffer;
		
		for (int i = 0; i < RGB_CHANNELS; i++) {
			tmpBuffer = FloatBuffer.allocate(rgbImage.getHeight() * rgbImage.getWidth());
			fillWithChannelFast(tmpBuffer, i, rgbImage.getData().getDataBuffer());
			rgbImagePixels[i] = createMatrixFast(rgbImage.getHeight(), rgbImage.getWidth(), tmpBuffer);
		}
		
		return rgbImagePixels;
	}
	

	/*
	 * This method takes the imageDataBuffer and fills the FloatBuffer
	 * with the value of each channel, by skipping three byte each cycle
	 */
	private static void fillWithChannelFast(FloatBuffer tmpBuffer, final int channel, final DataBuffer imageDataBuffer) {
		for (int i = channel; i < imageDataBuffer.getSize(); i += RGB_CHANNELS) {
			tmpBuffer.put((float)imageDataBuffer.getElem(i));
		}
	}
	

	/*
	 * This is the fast implementation for loading a matrix of size
	 * rows and width columns from a Float Buffer.
	 */
	private static FloatMatrix createMatrixFast(int rows, int columns, FloatBuffer tmpBuffer) {
		FloatMatrix matrix;
		
		matrix = new FloatMatrix(rows, columns, tmpBuffer.array());
		
		return matrix;
	}
		
	
	
	/*
	 * My implementation for RGB to YPbPr conversion. 
	 * Making use of linear algebra (JBlas).
	 * For more info : https://en.wikipedia.org/wiki/YPbPr
	 */
	public static FloatMatrix[] convertRGB2YPbPr(FloatMatrix[] rgbImagePixels) {
		FloatMatrix[] ypbprImagePixels = new FloatMatrix[YPBPR_CHANNELS];
		
		normalizeMatricesInPlace(rgbImagePixels, 255);
		
		// Y = 0.2126 R + 0.7152 G + 0.0722 B
		ypbprImagePixels[Y] = rgbImagePixels[R].mul(0.2126f).add(rgbImagePixels[G].mul(0.7152f)).add(rgbImagePixels[B].mul(0.0722f));
		
		// PB carries the difference between blue and luma (B − Y)
		ypbprImagePixels[Pb] = rgbImagePixels[B].sub(ypbprImagePixels[Y]);
		
		// PR carries the difference between red and luma (R − Y)
		ypbprImagePixels[Pr] = rgbImagePixels[R].sub(ypbprImagePixels[Y]);
				
		return ypbprImagePixels;
	}

	
	public static FloatMatrix[] convertRGB2YCbCr(FloatMatrix[] rgbImagePixels) {
		FloatMatrix[] ypbprImagePixels = new FloatMatrix[YCBCR_CHANNELS];
		
		normalizeMatricesInPlace(rgbImagePixels, 256);
		
		ypbprImagePixels[Y] = rgbImagePixels[R].mul(65.738f)
								.add(rgbImagePixels[G].mul(129.057f))
								.add(rgbImagePixels[B].mul(25.064f))
								.add(16f);
		
		ypbprImagePixels[Cb] = rgbImagePixels[R].mul(-37.945f)
								.add(rgbImagePixels[G].mul(-74.494f))
								.add(rgbImagePixels[B].mul(112.439f))
								.add(128f);
		
		ypbprImagePixels[Cr] = rgbImagePixels[R].mul(112.439f)
								.add(rgbImagePixels[G].mul(-96.154f))
								.add(rgbImagePixels[B].mul(-18.285f))
								.add(128f);
				
		return ypbprImagePixels;
	}
	
	/*
	 * This provides the standard routine for color conversion
	 * Returns the pixel in a tensor shape [width][height][channels]
	 */
	public static float[][][] rgbConvertRoutine(File rgbImageFile, 
											Function<FloatMatrix[], FloatMatrix[]> colorSpaceConversion){
		BufferedImage rgbImage = null;
		
		FloatMatrix[] rgbImagePixels;
		FloatMatrix[] ypbprImagePixels;
		
		rgbImage = BicubicInterp.readImage(rgbImageFile);
		
		if (rgbImage.getType() != BufferedImage.TYPE_3BYTE_BGR) {
			System.out.println("Not able to convert this image");
			System.exit(1);
		}
		
		rgbImagePixels = createRGBMatricesFast(rgbImage);
		ypbprImagePixels = colorSpaceConversion.apply(rgbImagePixels);
		
		return ArrayOp.matrixToTensor(ypbprImagePixels);
	}	
	
}
