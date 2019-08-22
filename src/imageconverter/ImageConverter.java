package imageconverter;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.jblas.FloatMatrix;


public class ImageConverter {
	
	public final static int R = 0, G = 1, B = 2;
	public final static int Y = 0, Pb = 1, Pr = 2;
	public final static int RGB_CHANNELS = 3;
	public final static int YPBPR_CHANNELS = 3;
	
	
	/*
	 * This method takes the rgb file and load 
	 * in memory the associated BufferedImage
	 */
	private static BufferedImage readImage(File rgbImageFile) {
		BufferedImage RGBImage = null;
		
		try {
			RGBImage = ImageIO.read(rgbImageFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return RGBImage;
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
		
		// scale matrix
		for (int i = 0; i < RGB_CHANNELS; i++)  {
			rgbImagePixels[i] = rgbImagePixels[i].div(255.0f);
		}
		
		// Y = 0.2126 R + 0.7152 G + 0.0722 B
		ypbprImagePixels[Y] = rgbImagePixels[R].mul(0.2126f).add(rgbImagePixels[G].mul(0.7152f)).add(rgbImagePixels[B].mul(0.0722f));
		
		// PB carries the difference between blue and luma (B − Y)
		ypbprImagePixels[Pb] = rgbImagePixels[B].sub(ypbprImagePixels[Y]);
		
		// PR carries the difference between red and luma (R − Y)
		ypbprImagePixels[Pr] = rgbImagePixels[R].sub(ypbprImagePixels[Y]);
				
		return ypbprImagePixels;
	}


	/*
	 * This provides the standard routine for color conversion
	 */
	public static BufferedImage rgbConvertRoutine(	File rgbImageFile, 
											Function<FloatMatrix[], FloatMatrix[]> colorSpaceConversion){
		BufferedImage rgbImage = null;
		BufferedImage ypbprImage = null;

		FloatMatrix[] rgbImagePixels;
		FloatMatrix[] ypbprImagePixels;
		
		rgbImage = readImage(rgbImageFile);
		
		if (rgbImage.getType() != BufferedImage.TYPE_3BYTE_BGR) {
			System.out.println("Not able to convert this image");
			System.exit(1);
		}
		
		rgbImagePixels = createRGBMatricesFast(rgbImage);
		ypbprImagePixels = colorSpaceConversion.apply(rgbImagePixels);
		
		// write ypbprimage on file
		
		
		// fill ypbprBuffer ?
		
		
		return ypbprImage;
	}	
	
	// need bicubic interpolation (pbpr - x2 scale)
	
	/*
	 * Method for making flat an Array.
	 */
	public static Stream<Object> flatten(Object[] array) {
	    return Arrays.stream(array).flatMap(o -> o instanceof Object[]? flatten((Object[])o): Stream.of(o));
	}
}