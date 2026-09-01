package net.bupy.gltf;

/**
 * Data container class that represents a single Texture when parsing a .GLTF file.<br>
 * Fields are named in according to the .GLTF 2.0 reference.
 */
public final class GLTFTexture {

    /// Index that points to a sampler (GL_LINEAR, GL_NEAREST, GL_LINEAR_MIPMAP...)
    private final int sampler;
    /// Index that points to a source (an image, basically)
    private final int source;

    public GLTFTexture(int sampler, int source) {
        this.sampler = sampler;
        this.source = source;
    }

    public int sampler() {
        return sampler;
    }

    public int source() {
        return source;
    }

    /**
     * Static nested class used for constructing the immutable parent class.<br>
     * Can be reused to construct multiple objects.<br>
     * Relevant field documentation is in the parent class.
     */
    public static final class Builder {

        private int sampler;
        private int source;

        public GLTFTexture build() {
            return new GLTFTexture(sampler, source);
        }

        public void reset() {
            sampler = 0;
            source = 0;
        }

        public Builder setSampler(int sampler) {
            this.sampler = sampler;
            return this;
        }

        public Builder setSource(int source) {
            this.source = source;
            return this;
        }
    }
}
