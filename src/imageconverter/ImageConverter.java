package imageconverter;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.function.Function;

import org.jblas.FloatMatrix;


public class ImageConverter {
	
	public final static int B = 0, G = 1, R = 2;
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
			tmpBuffer.put((float) imageDataBuffer.getElem(i));
		}
	}
	

	/*
	 * This is the fast implementation for loading a matrix of size
	 * rows and width columns from a Float Buffer.
	 */
	private static FloatMatrix createMatrixFast(int rows, int columns, Buffer tmpBuffer) {
		FloatMatrix matrix;
		
		matrix = new FloatMatrix(rows, columns, (float[]) tmpBuffer.array());
		
		return matrix.transpose();
	}
		
	
	
	/*
	 * My implementation for RGB to YPbPr conversion. 
	 * Making use of linear algebra (JBlas).
	 * For more info : https://en.wikipedia.org/wiki/YPbPr
	 */
	public static FloatMatrix[] convertRGB2YPbPr(FloatMatrix[] rgbImagePixels) {
		FloatMatrix[] ypbprImagePixels = new FloatMatrix[YPBPR_CHANNELS];
		
		normalizeMatricesInPlace(rgbImagePixels, 255);
		
		/*
		 * [[ 0.299   , 0.587   , 0.114   ],
		 *	[-0.168736,-0.331264, 0.5     ],
		 *	[ 0.5     ,-0.418688,-0.081312]]
		 */
		
		ypbprImagePixels[Y] = rgbImagePixels[R].mul(0.299f)
						.add(rgbImagePixels[G].mul(0.587f))
						.add(rgbImagePixels[B].mul(0.114f));

		ypbprImagePixels[Pb] = rgbImagePixels[R].mul(-0.168736f)
						.add(rgbImagePixels[G].mul(-0.331264f))
						.add(rgbImagePixels[B].mul(0.5f));
		
		ypbprImagePixels[Pr] = rgbImagePixels[R].mul(0.5f)
						.add(rgbImagePixels[G].mul(-0.418688f))
						.add(rgbImagePixels[B].mul(-0.081312f));
				
		return ypbprImagePixels;
	}

	
	public static FloatMatrix[] convertRGB2YCbCr(FloatMatrix[] rgbImagePixels) {
		FloatMatrix[] ycbcrImagePixels = new FloatMatrix[YCBCR_CHANNELS];
		
		normalizeMatricesInPlace(rgbImagePixels, 255);
		
		/*
		 * [[    65.481,   128.553,    24.966],
         *	[   -37.797,   -74.203,   112.0  ],
		 *	[   112.0  ,   -93.786,   -18.214]])
		 */
		
		ycbcrImagePixels[Y] = rgbImagePixels[R].mul(65.481f)
								.add(rgbImagePixels[G].mul(128.553f))
								.add(rgbImagePixels[B].mul(24.966f))
								.add(16f);
		
		ycbcrImagePixels[Cb] = rgbImagePixels[R].mul(-37.797f)
								.add(rgbImagePixels[G].mul(-74.203f))
								.add(rgbImagePixels[B].mul(112.0f))
								.add(128f);
		
		ycbcrImagePixels[Cr] = rgbImagePixels[R].mul(112.0f)
								.add(rgbImagePixels[G].mul(-93.786f))
								.add(rgbImagePixels[B].mul(-18.214f))
								.add(128f);
				
		return ycbcrImagePixels;
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
