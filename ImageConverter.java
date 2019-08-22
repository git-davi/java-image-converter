package imageconverter;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.jblas.FloatMatrix;

public class ImageConverter {

	public final static int R = 0, G = 1, B = 2;
	public final static int Y = 0, Pb = 1, Pr = 2;
	public final static int N_CHANNELS = 3;
	
	private static BufferedImage readImage(File rgbImageFile) {
		BufferedImage RGBImage = null;
		
		try {
			RGBImage = ImageIO.read(rgbImageFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return RGBImage;
	}
	private static void fillWithChannel(List<Float> tmpBuffer, final int channel, final DataBuffer imageDataBuffer) {
		for (int i = channel; i < imageDataBuffer.getSize(); i += N_CHANNELS) {
			tmpBuffer.add((float)imageDataBuffer.getElem(i));
		}
	}
	private static void fillArray(float[] tmpArray, List<Float> tmpBuffer) {
		for (int i = 0; i < tmpArray.length; i++) {
			tmpArray[i] = tmpBuffer.get(i);
		}
	}
	private static FloatMatrix createMatrix(int width, int height, List<Float> tmpBuffer) {
		FloatMatrix matrix;
		float[] tmpArray = new float[tmpBuffer.size()];
		
		fillArray(tmpArray, tmpBuffer);
		matrix = new FloatMatrix(height, width, tmpArray);
		
		return matrix;
	}
	private static FloatMatrix[] createRGBMatrices(BufferedImage rgbImage) {
		List<Float> tmpBuffer = new LinkedList<Float>();
		FloatMatrix[] rgbImagePixels = new FloatMatrix[N_CHANNELS];
		
		fillWithChannel(tmpBuffer, R, rgbImage.getData().getDataBuffer());
		rgbImagePixels[R] = createMatrix(rgbImage.getWidth(), rgbImage.getHeight(), tmpBuffer);
		tmpBuffer.clear();
		fillWithChannel(tmpBuffer, G, rgbImage.getData().getDataBuffer());
		rgbImagePixels[G] = createMatrix(rgbImage.getWidth(), rgbImage.getHeight(), tmpBuffer);
		tmpBuffer.clear();
		fillWithChannel(tmpBuffer, B, rgbImage.getData().getDataBuffer());
		rgbImagePixels[B] = createMatrix(rgbImage.getWidth(), rgbImage.getHeight(), tmpBuffer);
		tmpBuffer.clear();
		
		return rgbImagePixels;
	}
	private static FloatMatrix[] convertRGB2YPbPr(FloatMatrix[] rgbImagePixels) {
		FloatMatrix[] ypbprImagePixels = new FloatMatrix[N_CHANNELS];
		
		// scale matrix
		for (int i = 0; i < N_CHANNELS; i++)  {
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
	public static float[][][] rgb2ypbpr(File rgbImageFile) {
		BufferedImage rgbImage = null;
		BufferedImage ypbprImage = null;

		FloatMatrix[] rgbImagePixels;
		FloatMatrix[] ypbprImagePixels;
		
		rgbImage = readImage(rgbImageFile);
		
		rgbImagePixels = createRGBMatrices(rgbImage);
		ypbprImagePixels = convertRGB2YPbPr(rgbImagePixels);
		
		// bicubic interpolation of pbpr - x2 scale
		
		//return the matrices in an array form
		
		return null;
	}
	public static void writeArrayToImage(float[][][] byteImage) {
		// convert the array into a buffered image and writes the result on file
	}
	public static float[][][][] toTensorflowInputShape(float[][][]) {
	
	}
	*/
}
