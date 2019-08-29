package imageconverter;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

public class BicubicInterp {
	
	/*
	 * This method takes the rgb file and load 
	 * in memory the associated BufferedImage
	 */
	public static BufferedImage readImage(File ImageFile) {
		BufferedImage image = null;
		
		try {
			image = ImageIO.read(ImageFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return image;
	}
	
	
	public static void writeImage(BufferedImage image, String oldName) {
		try {
			StringTokenizer tokenizer = new StringTokenizer(oldName, ".");
			File outFile = new File("img/" + tokenizer.nextToken() +"_bicubic_scaled.jpg");
			ImageIO.write(image, "jpg", outFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void resizeImage(File smallImageFile, double scale) {
		BufferedImage scaledImage = null;
		BufferedImage smallImage = readImage(smallImageFile);
		final int newWidth = smallImage.getWidth() * (int) scale;
		final int newHeight = smallImage.getHeight() * (int) scale;
		
		scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_3BYTE_BGR);
		
		final AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
		final AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
		scaledImage = ato.filter(smallImage, scaledImage);
		
		writeImage(scaledImage, smallImageFile.getName());
	}
}
