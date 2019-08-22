# java-image-converter

Class for ***FAST*** conversion from RGB to Ypbpr color space.
Custom conversions can be added easily.

Bicubic Interpolation will be added soon.

> **Consistency of conversion hasn't been tested** 

## Custom color space conversions

Other conversion can be added simply by creating a custom method inside your class :
```java
public static FloatMatrix[] convertRGB2Custom(FloatMatrix[] rgbImagePixels) {
    /*
    *
    code here
    *
    */
} 
```
Where the returned FloatMatrix[] is the converted image of shape {HEIGHT, WIDTH, 3}.
For JBlas matrix operation and api [have a look here](http://www.jblas.org/javadoc/index.html).

Once done that you only have to call the method :
```java
    ImageConverter.rgbConvert(new File("path/to/file"), YourClassName::convertRGB2Custom);
```


## Dependecies 

- JBlas 1.2.4 : for fast linear algebra.
- Java >= 8