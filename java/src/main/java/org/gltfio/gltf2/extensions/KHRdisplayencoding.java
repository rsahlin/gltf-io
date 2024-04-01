package org.gltfio.gltf2.extensions;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;
import org.gltfio.lib.ErrorMessage;

import com.google.gson.annotations.SerializedName;

public class KHRdisplayencoding extends JSONExtension {

    public static final OOTF HDR_OOTF = new OOTF(59.5208f, 2.4f);
    public static final OOTF SDR_OOTF = new OOTF(46.42f, 2.4f);
    // Default to REC.709 Yr,Yg,Yb values
    private final float[] colorPrimaries = new float[] { 0.333f, 0.600f, 0.066f };

    public enum DisplayEncodeExtensionSetting implements ExtensionSetting {
        DISPLAYENCODE("DISPLAYENCODE");

        public final String macroName;

        DisplayEncodeExtensionSetting(String macro) {
            macroName = macro;
        }

        @Override
        public String getMacroName() {
            return macroName;
        }
    }

    public static final class OOTF {

        private static final String RANGE_EXTENSION = "rangeExtension";
        private static final String GAMMA = "gamma";

        private OOTF(float ext, float g) {
            rangeExtension = ext;
            gamma = g;
        }

        @SerializedName(RANGE_EXTENSION)
        private float rangeExtension;
        @SerializedName(GAMMA)
        private float gamma;

        public float getRangeExtension() {
            return rangeExtension;
        }

        public float getGamma() {
            return gamma;
        }

    }

    @Override
    public List<String> getExtensionName() {
        return ExtensionTypes.KHR_displayencoding.names;
    }

    @Override
    public ExtensionSetting[] getSettings() {
        return new DisplayEncodeExtensionSetting[] { DisplayEncodeExtensionSetting.DISPLAYENCODE };
    }

    /**
     * Returns the Yr,Yg,Yb color primaries
     * 
     * @return
     */
    public final float[] getColorPrimaries() {
        return colorPrimaries;
    }

    /**
     * Sets the color primaries to use in the displayencoding
     * 
     * @param primaries
     */
    public final void setColorPrimaries(@NonNull float[] primaries) {
        if (primaries == null || primaries.length < colorPrimaries.length) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", invalid primaries");
        }
        System.arraycopy(primaries, 0, colorPrimaries, 0, colorPrimaries.length);
    }

    @Override
    public ExtensionTypes getExtensionType() {
        return ExtensionTypes.KHR_displayencoding;
    }
}
