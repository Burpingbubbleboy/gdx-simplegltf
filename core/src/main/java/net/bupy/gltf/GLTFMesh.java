package net.bupy.gltf;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Data container class that represents a single Mesh when parsing a .GLTF file.<br>
 * Fields are named in according to the .GLTF 2.0 reference.<br>
 * Note that this class contains attributes information! There is no dedicated attributes class!<br>
 * Note that this class does not support multiple primitives for one mesh!
 */
public final class GLTFMesh {

    /// Name of the mesh -- this is NOT what you'd see named in Blender in Scene View, you'd be looking at the Node name.
    private final String name;
    /// Underlying primitives array
    private final GLTFPrimitive[] gltfPrimitives;

    public GLTFMesh(String name, GLTFPrimitive[] gltfPrimitives) {
        this.name = name;
        this.gltfPrimitives = gltfPrimitives;
    }

    public String name() {
        return name;
    }

    public GLTFPrimitive[] gltfPrimitives() {
        return gltfPrimitives;
    }

    @Override
    public String toString() {
        return "GLTFMesh{" +
            "name='" + name + '\'' +
            ", gltfPrimitives=" + Arrays.toString(gltfPrimitives) +
            '}';
    }

    /**
     * Static nested Data container class used for holding information of the primitives.<br>
     * Fields are named in according to the .GLTF 2.0 reference.<br>
     * Made a nested class due to the tight coupling of information.<br>
     */
    public static final class GLTFPrimitive {
        /// POSITION attribute. Points to an index.
        private int POSITION;
        /// NORMAL attribute. Points to an index.
        private int NORMAL;
        /// TEXCOORD_0 attribute. Points to an index.
        private int TEXCOORD_0;
        /// Vertex indices. Points to an index.
        private int indices;
        /// Material this primitive should use. Points to an index.<br>
        /// If a material isn't defined in the primitive, it'll default to using index -1 for now.<br>
        /// Support for setting a custom "missing material" will be added later...<br>
        private int material;

        public int POSITION() {
            return POSITION;
        }

        public int NORMAL() {
            return NORMAL;
        }

        public int TEXCOORD_0() {
            return TEXCOORD_0;
        }

        public int indices() {
            return indices;
        }

        public int material() {
            return material;
        }

        @Override
        public String toString() {
            return "GLTFPrimitive{" +
                "POSITION=" + POSITION +
                ", NORMAL=" + NORMAL +
                ", TEXCOORD_0=" + TEXCOORD_0 +
                ", indices=" + indices +
                ", material=" + material +
                '}';
        }

        // No nested builder class -- deemed unnecessary
    }

    /**
     * Static nested class used for constructing the immutable parent class.<br>
     * Can be reused to construct multiple objects.<br>
     * Relevant field documentation is in the parent class.
     */
    public static final class Builder {

        private final ArrayList<GLTFPrimitive> gltfPrimitives = new ArrayList<>();
        private int currentPrimitiveIndex = -1;
        private String name;

        public GLTFMesh build() {
            return new GLTFMesh(name, gltfPrimitives.toArray(GLTFPrimitive[]::new));
        }

        public void reset() {
            name = null;
            currentPrimitiveIndex = -1;
            gltfPrimitives.clear();
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Creates a new internal {@code GLTFPrimitive} object.<br>
         * Subsequent usages to various setter methods will then operate on this internal object.
         * @return This object for method chaining
         */
        public Builder primitive() {
            gltfPrimitives.add(new GLTFPrimitive());
            currentPrimitiveIndex++;
            return this;
        }

        public Builder setPrimitivePOSITION(int POSITION) {
            gltfPrimitives.get(currentPrimitiveIndex).POSITION = POSITION;
            return this;
        }

        public Builder setPrimitiveNORMAL(int NORMAL) {
            gltfPrimitives.get(currentPrimitiveIndex).NORMAL = NORMAL;
            return this;
        }

        public Builder setPrimitiveTEXCOORD_0(int TEXCOORD_0) {
            gltfPrimitives.get(currentPrimitiveIndex).TEXCOORD_0 = TEXCOORD_0;
            return this;
        }

        public Builder setPrimitiveIndices(int indices) {
            gltfPrimitives.get(currentPrimitiveIndex).indices = indices;
            return this;
        }

        public Builder setPrimitiveMaterial(int material) {
            gltfPrimitives.get(currentPrimitiveIndex).material = material;
            return this;
        }
    }
}
