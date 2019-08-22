# java-image-converter

Class for ***FAST*** color space conversion.

_Actually only RGB to Ypbpr has been implemented._

[Bicubic Interpolation will be added soon.]

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

Once done that you only have to call the method :
```java
    ImageConverter.rgbConvert(new File("path/to/file"), YourClassName::convertRGB2Custom);
```


For ***JBlas documentation*** and api [have a look here](http://www.jblas.org/javadoc/index.html).

## Dependecies 

- JBlas 1.2.4 : for fast linear algebra.
- Java >= 8
