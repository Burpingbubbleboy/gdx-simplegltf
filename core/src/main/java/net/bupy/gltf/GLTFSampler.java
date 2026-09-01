package net.bupy.gltf;

/**
 * Data container class that represents a single Image when parsing a .GLTF file.<br>
 * Fields are named in according to the .GLTF 2.0 reference.
 */
public final class GLTFSampler {

    /// "What does the texture look like when it's up in your face?"
    private final int magFilter;
    /// "What does the texture look like when it's far away?"<br>
    /// Supports mipmapping constants
    private final int minFilter;

    public GLTFSampler(int magFilter, int minFilter) {
        this.magFilter = magFilter;
        this.minFilter = minFilter;
    }

    public int magFilter() {
        return magFilter;
    }

    public int minFilter() {
        return minFilter;
    }

    /**
     * Static nested class used for constructing the immutable parent class.<br>
     * Can be reused to construct multiple objects.<br>
     * Relevant field documentation is in the parent class.
     */
    public static final class Builder {

        private int magFilter;
        private int minFilter;

        public GLTFSampler build() {
            return new GLTFSampler(magFilter, minFilter);
        }

        public void reset() {
            magFilter = 0;
            minFilter = 0;
        }

        public Builder setMagFilter(int magFilter) {
            this.magFilter = magFilter;
            return this;
        }

        public Builder setMinFilter(int minFilter) {
            this.minFilter = minFilter;
            return this;
        }
    }
}

