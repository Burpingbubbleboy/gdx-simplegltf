package net.bupy.gltf;

/**
 * Data container class that represents a single Buffer when parsing a .GLTF file.<br>
 * Fields are named in according to the .GLTF 2.0 reference.
 */
public final class GLTFBuffer {

    /// Provides the length of the buffer, in bytes.
    private final int byteLength;
    /// Provides the URI of the .BIN file to read from.
    private final String uri;

    public GLTFBuffer(int byteLength, String uri) {
        this.byteLength = byteLength;
        this.uri = uri;
    }

    public int byteLength() {
        return byteLength;
    }

    public String uri() {
        return uri;
    }

    @Override
    public String toString() {
        return "GLTFBuffer{" +
            "byteLength=" + byteLength +
            ", uri='" + uri + '\'' +
            '}';
    }

    /**
     * Static nested class used for constructing the immutable parent class.<br>
     * Can be reused to construct multiple objects.<br>
     * Relevant field documentation is in the parent class.
     */
    public static final class Builder {

        private int byteLength;
        private String uri;

        public GLTFBuffer build() {
            return new GLTFBuffer(byteLength, uri);
        }

        public void reset() {
            byteLength = 0;
            uri = null;
        }

        public Builder setUri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder setByteLength(int byteLength) {
            this.byteLength = byteLength;
            return this;
        }
    }
}
