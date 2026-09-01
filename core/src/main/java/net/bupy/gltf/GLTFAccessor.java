package net.bupy.gltf;

import java.util.Arrays;

/**
 * Data container class that represents a single Accessor when parsing a .GLTF file.<br>
 * Fields are named in according to the .GLTF 2.0 reference.
 */
public final class GLTFAccessor {

    /// Points to a bufferView index.
    private final int bufferView;
    /// Provides an additional offset, in number of bytes, that the bufferView should use.
    private final int byteOffset;
    /// Declares the primitive type that {@code type} uses. (e.g. u_short, int, long...)
    private final int componentType;
    /// Number of elements referenced by this accessor.
    private final int count;
    /// Maximum range of all element values.
    private final float[] max;
    /// Minimum range of all element values.
    private final float[] min;
    /// Declares the object type. (e.g. Vec3, Mat4, Scalar...)
    private final String type;

    public GLTFAccessor(int bufferView, int byteOffset, int componentType, int count, float[] max, float[] min, String type) {
        this.bufferView = bufferView;
        this.byteOffset = byteOffset;
        this.componentType = componentType;
        this.count = count;
        this.max = max;
        this.min = min;
        this.type = type;
    }

    public int bufferView() {
        return bufferView;
    }

    public int byteOffset() {
        return byteOffset;
    }

    public int componentType() {
        return componentType;
    }

    public int count() {
        return count;
    }

    public float[] max() {
        return max;
    }

    public float[] min() {
        return min;
    }

    public String type() {
        return type;
    }

    @Override
    public String toString() {
        return "GLTFAccessor{" +
            "bufferView=" + bufferView +
            ", byteOffset=" + byteOffset +
            ", componentType=" + componentType +
            ", count=" + count +
            ", max=" + Arrays.toString(max) +
            ", min=" + Arrays.toString(min) +
            ", type='" + type + '\'' +
            '}';
    }

    /**
     * Static nested class used for constructing the immutable parent class.<br>
     * Can be reused to construct multiple objects.<br>
     * Relevant field documentation is in the parent class.
     */
    public static final class Builder {

        private int bufferView;
        private int byteOffset;
        private int componentType;
        private int count;
        private float[] max;
        private float[] min;
        private String type;

        public GLTFAccessor build() {
            return new GLTFAccessor(bufferView, byteOffset, componentType, count, max, min, type);
        }

        public void reset() {
            bufferView = 0;
            byteOffset = 0;
            componentType = 0;
            count = 0;
            max = null;
            min = null;
            type = null;
        }

        public Builder setBufferView(int bufferView) {
            this.bufferView = bufferView;
            return this;
        }

        public Builder setByteOffset(int byteOffset) {
            this.byteOffset = byteOffset;
            return this;
        }

        public Builder setComponentType(int componentType) {
            this.componentType = componentType;
            return this;
        }

        public Builder setCount(int count) {
            this.count = count;
            return this;
        }

        public Builder setMax(float[] max) {
            this.max = max;
            return this;
        }

        public Builder setMin(float[] min) {
            this.min = min;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }
    }
}
