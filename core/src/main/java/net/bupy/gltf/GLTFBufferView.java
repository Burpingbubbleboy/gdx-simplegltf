package net.bupy.gltf;

/**
 * Data container class that represents a single BufferView when parsing a .GLTF file.<br>
 * Fields are named in according to the .GLTF 2.0 reference.<br>
 * Note that OpenGL Targets are intentionally unsupported!<br>
 */
public final class GLTFBufferView {

    /// Points to a buffer index. This is typically 0.
    private final int buffer;
    /// Provides the length of the bufferView, in number of bytes.
    private final int byteLength;
    /// Provides the base offset of the bufferView, in number of bytes.
    private final int byteOffset;

    public GLTFBufferView(int buffer, int byteLength, int byteOffset) {
        this.buffer = buffer;
        this.byteLength = byteLength;
        this.byteOffset = byteOffset;
    }

    public int buffer() {
        return buffer;
    }

    public int byteLength() {
        return byteLength;
    }

    public int byteOffset() {
        return byteOffset;
    }

    @Override
    public String toString() {
        return "GLTFBufferView{" +
            "buffer=" + buffer +
            ", byteLength=" + byteLength +
            ", byteOffset=" + byteOffset +
            '}';
    }

    /**
     * Static nested class used for constructing the immutable parent class.<br>
     * Can be reused to construct multiple objects.<br>
     * Relevant field documentation is in the parent class.
     */
    public static final class Builder {

        private int buffer;
        private int byteLength;
        private int byteOffset;

        public GLTFBufferView build() {
            return new GLTFBufferView(buffer, byteLength, byteOffset);
        }

        public void reset() {
            buffer = 0;
            byteLength = 0;
            byteOffset = 0;
        }

        public Builder setBuffer(int buffer) {
            this.buffer = buffer;
            return this;
        }

        public Builder setByteLength(int byteLength) {
            this.byteLength = byteLength;
            return this;
        }

        public Builder setByteOffset(int byteOffset) {
            this.byteOffset = byteOffset;
            return this;
        }
    }
}
