
package org.gltfio.lib;

/**
 * Simple quaternion methods
 * Quaternion is in the form X,Y,Z,W
 * 
 *
 */
public class Quaternion {

    private Quaternion() {
    }

    public static final void clear(float[] quaternion) {
        quaternion[0] = 0;
        quaternion[1] = 0;
        quaternion[2] = 0;
        quaternion[3] = 1;
    }

    /**
     * Sets the quaternion to the x axis rotation
     * 
     * Q = [ sin(angle/2), 0, 0, cos(angle/2)
     * 
     * @param angle Y axis rotation
     * @param quaternaion
     */
    public static final void setXAxisRotation(float angle, float[] quaternion) {
        float a = angle / 2;
        quaternion[0] = (float) Math.sin(a);
        quaternion[1] = 0;
        quaternion[2] = 0;
        quaternion[3] = (float) Math.cos(a);
    }

    /**
     * Sets the quaternion to the y axis rotation
     * 
     * Q = [ 0, sin(angle/2), 0, cos(angle/2)
     * 
     * @param angle Y axis rotation
     * @param quaternaion
     */
    public static final void setYAxisRotation(float angle, float[] quaternion) {
        float a = angle / 2;
        quaternion[0] = 0;
        quaternion[1] = (float) Math.sin(a);
        quaternion[2] = 0;
        quaternion[3] = (float) Math.cos(a);
    }

    /**
     * Sets the quaternion to the z axis rotation
     * 
     * Q = [ 0, 0, sin(angle/2), cos(angle/2)
     * 
     * @param angle Y axis rotation
     * @param quaternaion
     */
    public static final void setZAxisRotation(float angle, float[] quaternion) {
        float a = angle / 2;
        quaternion[0] = 0;
        quaternion[1] = 0;
        quaternion[2] = (float) Math.sin(a);
        quaternion[3] = (float) Math.cos(a);
    }

    public static final void setXYZAxisRotation(float x, float y, float z, float[] quaternion) {
        float cosZ = (float) Math.cos(z * 0.5);
        float sinZ = (float) Math.sin(z * 0.5);
        float cosY = (float) Math.cos(y * 0.5);
        float sinY = (float) Math.sin(y * 0.5);
        float cosX = (float) Math.cos(x * 0.5);
        float sinX = (float) Math.sin(x * 0.5);

        quaternion[0] = sinX * cosY * cosZ - cosX * sinY * sinZ;
        quaternion[1] = cosX * sinY * cosZ + sinX * cosY * sinZ;
        quaternion[2] = cosX * cosY * sinZ - sinX * sinY * cosZ;
        quaternion[3] = cosX * cosY * cosZ + sinX * sinY * sinZ;
    }

    /**
     * Multiplies quaternion1 with quaternion2 storing the result in result.
     * 
     * @param quaternion1
     * @param quaternion2
     * @param result May be quaternion1 or quaternion2
     * @return The result
     */
    public static final float[] mul(float[] quaternion1, float[] quaternion2, float[] result) {
        float x = quaternion1[0] * quaternion2[3] + quaternion1[1] * quaternion2[2] - quaternion1[2] * quaternion2[1]
                + quaternion1[3] * quaternion2[0];
        float y = -quaternion1[0] * quaternion2[2] + quaternion1[1] * quaternion2[3] + quaternion1[2] * quaternion2[0]
                + quaternion1[3] * quaternion2[1];
        float z = quaternion1[0] * quaternion2[1] - quaternion1[1] * quaternion2[0] + quaternion1[2] * quaternion2[3]
                + quaternion1[3] * quaternion2[2];
        float w = -quaternion1[0] * quaternion2[0] - quaternion1[1] * quaternion2[1] - quaternion1[2] * quaternion2[2]
                + quaternion1[3] * quaternion2[3];
        result[0] = x;
        result[1] = y;
        result[2] = z;
        result[3] = w;
        return result;
    }

    /**
     * Multiplies quaternion1 with quaternion2 storing the result in destination
     * 
     * @param quaternion1
     * @param quaternion2
     * @param destination
     */
    public static final void mul(float[] quaternion1, float[] quaternion2, Transform destination) {
        float x = quaternion1[0] * quaternion2[3] + quaternion1[1] * quaternion2[2] - quaternion1[2] * quaternion2[1]
                + quaternion1[3] * quaternion2[0];
        float y = -quaternion1[0] * quaternion2[2] + quaternion1[1] * quaternion2[3] + quaternion1[2] * quaternion2[0]
                + quaternion1[3] * quaternion2[1];
        float z = quaternion1[0] * quaternion2[1] - quaternion1[1] * quaternion2[0] + quaternion1[2] * quaternion2[3]
                + quaternion1[3] * quaternion2[2];
        float w = -quaternion1[0] * quaternion2[0] - quaternion1[1] * quaternion2[1] - quaternion1[2] * quaternion2[2]
                + quaternion1[3] * quaternion2[3];
        destination.setRotation(x, y, z, w);
    }

}
