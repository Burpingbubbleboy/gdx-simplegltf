package net.bupy.gltf;

/**
 * Data container class that represents a single material when parsing a .GLTF file.<br>
 * Note that this .GLTF loader does not support PBR! Some nonessential fields may be omitted from this class.
 */
public final class GLTFMaterial {

    /// Name of the material
    private final String name;
    /// Determines if the material should do backface culling or not
    private final boolean doubleSided;
    /// Base color of the material. Used as a fallback if no texture is defined.
    private final float[] baseColorFactor;
    /// Index that points to a corresponding .GLTF texture
    /// Note that this is set to -1 by default to cover the case a material doesn't have a texture
    private final int baseColorTextureIndex;
    /// Index that points to a corresponding TEXCOORD_0 attribute.
    private final int texCoord;

    public GLTFMaterial(String name, boolean doubleSided, float[] baseColorFactor, int baseColorTextureIndex, int texCoord) {
        this.name = name;
        this.doubleSided = doubleSided;
        this.baseColorFactor = baseColorFactor;
        this.baseColorTextureIndex = baseColorTextureIndex;
        this.texCoord = texCoord;
    }

    public String name() {
        return name;
    }

    public boolean doubleSided() {
        return doubleSided;
    }

    public float[] baseColorFactor() {
        return baseColorFactor;
    }

    public int baseColorTextureIndex() {
        return baseColorTextureIndex;
    }

    public int texCoord() {
        return texCoord;
    }

    /**
     * Static nested class used for constructing the immutable parent class.<br>
     * Can be reused to construct multiple objects.<br>
     * Relevant field documentation is in the parent class.
     */
    public static final class Builder {

        private String name;
        private boolean doubleSided;
        private float[] baseColorFactor;
        private int baseColorTextureIndex = -1;
        private int texCoord;

        public GLTFMaterial build() {
            return new GLTFMaterial(name, doubleSided, baseColorFactor, baseColorTextureIndex, texCoord);
        }

        public void reset() {
            name = null;
            doubleSided = false;
            baseColorFactor = null;
            baseColorTextureIndex = -1;
            texCoord = 0;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDoubleSided(boolean doubleSided) {
            this.doubleSided = doubleSided;
            return this;
        }

        public Builder setBaseColorFactor(float[] baseColorFactor) {
            this.baseColorFactor = baseColorFactor;
            return this;
        }

        public Builder setBaseColorTextureIndex(int baseColorTextureIndex) {
            this.baseColorTextureIndex = baseColorTextureIndex;
            return this;
        }

        public Builder setTexCoord(int texCoord) {
            this.texCoord = texCoord;
            return this;
        }
    }
}
