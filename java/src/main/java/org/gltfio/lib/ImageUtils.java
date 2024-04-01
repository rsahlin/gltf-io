package org.gltfio.lib;

/**
 * Singleton helper class for buffer based images - no external dependencies
 */
public class ImageUtils {

    private static ImageUtils imageUtils;

    private ImageUtils() {
    }

    public static ImageUtils getInstance() {
        if (imageUtils == null) {
            imageUtils = new ImageUtils();
        }
        return imageUtils;
    }

    /**
     * Converts 8 bit BGRA format to BGR by copying bytes to destination array.
     * 
     * @param source Source array in BGRA format
     * @param destination Destination array BGR pixels
     * @param width
     * @param height
     */
    public void convertBGRAToBGR(byte[] source, byte[] destination, int width, int height) {

        int destIndex = 0;
        int sourceIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                destination[destIndex++] = source[sourceIndex++];
                destination[destIndex++] = source[sourceIndex++];
                destination[destIndex++] = source[sourceIndex++];
                sourceIndex++;
            }
        }
    }

}
