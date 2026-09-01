package net.bupy.gltf;

/**
 * Data container class that represents a single Image when parsing a .GLTF file.<br>
 * Fields are named in according to the .GLTF 2.0 reference.
 */
public final class GLTFImage {

    /// Represents the URI for the image. This is usually a file path.
    private final String uri;

    public GLTFImage(String uri) {
        this.uri = uri;
    }

    public String uri() {
        return uri;
    }

    /**
     * Static nested class used for constructing the immutable parent class.<br>
     * Can be reused to construct multiple objects.<br>
     * Relevant field documentation is in the parent class.
     */
    public static final class Builder {

        private String uri;

        public GLTFImage build() {
            return new GLTFImage(uri);
        }

        public void reset() {
            uri = null;
        }

        public Builder setUri(String uri) {
            this.uri = uri;
            return this;
        }
    }
}

