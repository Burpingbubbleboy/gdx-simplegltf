package net.bupy.gltfloading.gltfloader;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/**
 * Data container class that represents a single Node when parsing a .GLTF file.<br>
 * Fields are named in according to the .GLTF 2.0 reference
 */
public class GLTFNode {

    public final Quaternion rotation;
    public final Vector3 translation;
    public final Vector3 scale;
    /// Name of the node -- this would be the name you see directly in Blender
    public String name;
    /// Points to an index
    public int mesh;

    public GLTFNode() {
        rotation = new Quaternion();
        translation = new Vector3();
        scale = new Vector3(1f, 1f, 1f);
    }

    @Override
    public String toString() {
        return "GLTFNode{" +
            "rotation=" + rotation +
            ", translation=" + translation +
            ", scale=" + scale +
            ", name='" + name + '\'' +
            ", mesh=" + mesh +
            '}';
    }
}
