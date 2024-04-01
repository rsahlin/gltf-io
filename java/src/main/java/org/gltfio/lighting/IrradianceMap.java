
package org.gltfio.lighting;

import org.gltfio.lib.ErrorMessage;

/**
 * Handles the irradiance coefficients for spherical harmonics indirect (diffused) lighting.
 *
 */
public class IrradianceMap {

    public static final int IRRADIANCE_COEFFICIENT_COUNT = 9;
    private static final int RGB = 3;

    public enum IRMAP {

        DEFAULT(IrradianceMap.DEFAULT_COEFFICIENTS),
        STUDIO_5(IrradianceMap.STUDIO_5),
        UNDERWATER_1(IrradianceMap.UNDERWATER_1),
        UNDERWATER_2(IrradianceMap.UNDERWATER_2),
        CATHEDRAL(IrradianceMap.CATHEDRAL),
        EUCALYPTUS_GROVE(IrradianceMap.EUCALYPTUS_GROVE);

        public final float[] coefficients;

        IRMAP(float[] coefficients) {
            this.coefficients = coefficients;
        }

        public static IRMAP get(String name) {
            for (IRMAP map : values()) {
                if (map.name().equalsIgnoreCase(name)) {
                    return map;
                }
            }
            return null;
        }

    }

    public static final float[] CATHEDRAL = new float[] {
            0.78908f, 0.43710f, 0.54161f,
            0.39499f, 0.34989f, 0.60488f,
            -0.33974f, -0.18236f, -0.26940f,
            -0.29213f, -0.05562f, -0.00944f,
            -0.11141f, -0.05090f, -0.12231f,
            -0.26240f, -0.22401f, -0.47479f,
            -0.15570f, -0.09471f, -0.14733f,
            0.56014f, 0.21444f, 0.13915f,
            0.21205f, -0.05432f, -0.30374f };
    public static final float[] EUCALYPTUS_GROVE = new float[] {
            0.38f, 0.43f, 0.45f,
            0.29f, 0.36f, 0.41f,
            0.04f, 0.03f, 0.01f,
            -.10f, -.10f, -.09f,
            -.06f, -.06f, -.04f,
            .01f, -.01f, -.05f,
            -.09f, -.13f, -.15f,
            -.06f, -.05f, -.04f,
            .02f, -.00f, -.05f
    };

    public static final float[] DEFAULT_COEFFICIENTS = new float[] {
            0.8966792f, 0.86376095f, 0.86376095f,
            0.6904614f, 0.6904614f, 0.61035883f,
            0.78916955f, 0.7829518f, 0.73402005f,
            -0.7644828f, -0.7644828f, -0.6609855f,
            -0.680011f, -0.680011f, -0.56443167f,
            -0.6072222f, -0.5962319f, -0.5465285f,
            -0.13119966f, -0.13119966f, -0.13119966f,
            0.7624938f, 0.7624938f, 0.7172956f,
            0.09687597f, 0.09687597f, 0.09687597f
    };
    public static final float[] STUDIO_5 = new float[] {
            1.7219219f, 1.7485379f, 1.7445517f,
            0.5640655f, 0.5475895f, 0.53966206f,
            -0.20790188f, -0.21537878f, -0.20072107f,
            -0.24612671f, -0.25956377f, -0.2682215f,
            0.020330442f, 0.027245346f, 0.026683763f,
            -0.09169499f, -0.10187498f, -0.11396345f,
            -0.0072228718f, -0.0043860166f, -0.011310946f,
            -0.031278726f, -0.023547454f, -0.02058177f,
            0.1176779f, 0.12499094f, 0.12803316f
    };

    public static final float[] UNDERWATER_1 = new float[] {
            0.54434407f, 2.6992254f, 2.5600224f,
            -0.23836443f, -0.19288594f, -0.06842944f,
            0.024546446f, -0.040716946f, -0.047600023f,
            -0.012051719f, 0.01691293f, 0.040194713f,
            -0.026325826f, -0.002021944f, 0.008261753f,
            0.054705713f, 0.018083664f, 0.011287488f,
            -0.22845553f, -0.092818655f, -0.033757787f,
            0.031527054f, 0.0055867904f, 4.0130067E-4f,
            -0.3510552f, -0.103205584f, -0.01911696f
    };

    public static final float[] UNDERWATER_2 = new float[] {
            0.48312187f, 1.7323166f, 2.141251f,
            0.5661614f, 0.95249605f, 0.8749701f,
            0.051005393f, -0.048130006f, -0.08650999f,
            -0.056946643f, 0.08133684f, 0.09059762f,
            -0.13718559f, -0.017340664f, -0.010224633f,
            0.05964786f, 0.042910337f, 0.058635056f,
            -0.2397378f, -0.24174817f, -0.33766463f,
            -0.09381779f, -0.2130417f, -0.3271612f,
            -0.3303902f, -0.14828853f, -0.10029267f
    };

    final float[] coefficients = new float[IRRADIANCE_COEFFICIENT_COUNT * RGB];

    public IrradianceMap() {

    }

    public IrradianceMap(float[][] coeffs, float intensity) {
        set(coeffs, intensity);
    }

    /**
     * Sets the coefficients to the default factored by the intensity
     * 
     * @param intensity
     */
    public void setDefault(float intensity) {
        set(DEFAULT_COEFFICIENTS, intensity);
    }

    /**
     * Sets the coefficients using intensity
     * 
     * @param coeffs
     * @param intensity
     */
    public void set(float[][] coeffs, float intensity) {
        if (coeffs == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        if (coeffs.length > 0) {
            if (coeffs.length * coeffs[0].length != this.coefficients.length) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Wrong number of coefficients");
            }
            int index = 0;
            for (float[] c : coeffs) {
                for (int i = 0; i < c.length; i++) {
                    coefficients[index++] = c[i] * intensity;
                }
            }
        }

    }

    /**
     * Sets the coefficients and intensity factor
     * 
     * @param coeffs
     * @param intensity
     */
    public void set(float[] coeffs, float intensity) {
        for (int i = 0; i < this.coefficients.length; i++) {
            coefficients[i] = coeffs[i] * intensity;
        }
    }

    /**
     * Returns the array of coefficients
     * 
     * @return
     */
    public float[] getCoefficients() {
        return coefficients;
    }

}
