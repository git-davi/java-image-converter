# java-image-converter

Class for ***FAST*** color space conversion.\
**I founded hard to get a rgb to ypbpr color conversion on the web so I decided to implement one.**\
\
Useful to translate an image to a tensor. The output will be a tensor ```float[][][]``` of shape {width, heigth, channels}\
\
Also bicubic interpolation has been added.\
\
Implemented color space conversions :
>_**RGB to YCbCr**_\
>_**RGB to YPbPr**_



## Custom color space conversions

Other conversiosn can be added simply by creating a custom method inside your class :
```java
public static FloatMatrix[] convertRGB2Custom(FloatMatrix[] rgbImagePixels) {
    /*
    *
    code here
    *
    */
} 
```

Once done that you'll only have to call the method :
```java
ImageConverter.rgbConvert(new File("path/to/file"), YourClassName::convertRGB2Custom);
```

For ***JBlas documentation*** and api [have a look here](http://www.jblas.org/javadoc/index.html).


## Resize images with bicubic interpolation

Just call the static method :
```java
BicubicInterp.resizeImage(new File("<path to your image>"), 2.0);
```
The second argument is the scaling factor of the image.\
\
Your image will be saved into img folder.


## Dependecies 

- JBlas 1.2.4 : for fast linear algebra.
- Java >= 8
