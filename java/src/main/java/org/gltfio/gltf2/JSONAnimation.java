package org.gltfio.gltf2;

import com.google.gson.annotations.SerializedName;

/**
 * channels animation.channel [1-*] An array of animation channels.
 * An animation channel combines an animation sampler with a target property being animated.
 * Different channels of the same animation MUST NOT have the same targets.
 * Yes
 * samplers animation.sampler [1-*] An array of animation samplers. An animation sampler combines timestamps with a
 * sequence of output values and defines an interpolation algorithm.
 * Yes
 * name string The user-defined name of this object.
 * No
 * extensions extension JSON object with extension-specific objects.
 * No
 * extras extras Application-specific data.
 * No
 */
public class JSONAnimation extends NamedValue implements RuntimeObject {

    private static final String CHANNELS = "channels";
    private static final String SAMPLERS = "samplers";

    /**
     * An animation channel combines an animation sampler with a target property being animated.
     * sampler integer The index of a sampler in this animation used to compute the value for the target.
     * Yes
     * target animation.channel.target The descriptor of the animated property.
     * Yes
     * extensions extension JSON object with extension-specific objects.
     * No
     * extras extras Application-specific data.
     * No
     */
    public static class JSONAnimationChannel {

        /**
         * 
         * The descriptor of the animated property.
         * node integer The index of the node to animate. When undefined, the animated object MAY be defined by an
         * extension.
         * No
         * path string The name of the node’s TRS property to animate,or the "weights" of the Morph Targets it
         * instantiates.
         * For the "translation" property, the values that are provided by the sampler are the translation along the X,
         * Y,
         * and Z axes. For the "rotation" property, the values are a quaternion in the order (x, y, z, w), where w is
         * the
         * scalar. For the "scale" property, the values are the scaling factors along the X, Y, and Z axes.
         * Yes
         * extensions extension JSON object with extension-specific objects.
         * No
         * extras extras Application-specific data.
         * No
         */
        public static class JSONAnimationTarget {

            public enum AnimationPath {
                translation(),
                rotation(),
                scale(),
                weights();
            }

            private static final String NODE = "node";
            private static final String PATH = "path";

            @SerializedName(NODE)
            private int node;
            @SerializedName(PATH)
            private AnimationPath path;

        }

        private static final String SAMPLER = "sampler";
        private static final String TARGET = "target";

        @SerializedName(SAMPLER)
        private int sampler;
        @SerializedName(TARGET)
        private JSONAnimationTarget target;

        @SerializedName(CHANNELS)
        private JSONAnimationChannel[] channels;
        @SerializedName(SAMPLERS)
        private JSONAnimationSampler[] samplers;

    }

    /**
     * 
     * An animation sampler combines timestamps with a sequence of output values and defines an interpolation algorithm.
     * 
     * input integer The index of an accessor containing keyframe timestamps.
     * Yes
     * interpolation string Interpolation algorithm. No, default: "LINEAR"
     * output integer The index of an accessor,
     * containing keyframe output values.
     * Yes
     * extensions extension JSON object with extension-specific objects.
     * No
     * extras extras Application-specific data.
     * No
     */
    public static class JSONAnimationSampler {

        public enum AnimationInterpolation {
            linear(),
            step(),
            cubicspline();
        }

        private static final String INPUT = "input";
        private static final String INTERPOLATION = "interpolation";
        private static final String OUTPUT = "output";

        @SerializedName(INPUT)
        private int input;
        @SerializedName(INTERPOLATION)
        private AnimationInterpolation interpolation;
        @SerializedName(OUTPUT)
        private int output;

    }

    @SerializedName(CHANNELS)
    private JSONAnimationChannel[] channels;
    @SerializedName(SAMPLERS)
    private JSONAnimationSampler[] samplers;

    @Override
    public void resolveTransientValues() {
        // TODO Auto-generated method stub

    }
}
