package net.bupy.gltfloading.gltfloader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Exists to load .GLTF model files.<br>
 * Early version! Only supports loading untextured meshes with no animation!<br>
 */
public class GLTFLoader extends AsynchronousAssetLoader<Model, GLTFLoader.GLTFLoaderParameters> {

    public GLTFLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, GLTFLoaderParameters parameter) {
    }

    /// Highly inefficient at the moment; setting everything up for simplicity at the moment.
    @Override
    public Model loadSync(AssetManager manager, String fileName, FileHandle file, GLTFLoaderParameters parameter) {

        // get the contents of the .GLTF file.
        // .GLTF files are in the .JSON format
        JsonReader reader = new JsonReader();
        JsonValue gltfContents = reader.parse(Gdx.files.internal("test.gltf"));

        // get all the nodes in the .GLTF file, including translation, rotation and scale
        GLTFNode[] gltfNodes = new GLTFNode[0];
        for (JsonValue value : gltfContents) {
            if (value.name.equals("nodes")) {
                gltfNodes = getNodes(value);
                for (GLTFNode node : gltfNodes) {
                    System.out.println(node);
                }
            }
        }

        //byte[] binBytes = Gdx.files.internal("tree.bin").readBytes();

        ModelBuilder modelBuilder = new ModelBuilder();
        MeshPartBuilder meshPartBuilder;
        modelBuilder.begin();
        for (GLTFNode gltfNode : gltfNodes) {
            meshPartBuilder = modelBuilder.part(gltfNode.name, GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(1f, 1f, 0f, 1f)));
            meshPartBuilder.box(new Matrix4().set(gltfNode.translation, gltfNode.rotation, gltfNode.scale));
        }

        return modelBuilder.end();

    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, GLTFLoaderParameters parameter) {
        // doesn't need any dependencies :)
        return null;
    }

    /**
     * Generates an array of GLTFNode data classes.
     * @param nodesRoot JSON Root. Should be named "nodes," if you're parsing correctly.
     * @return The array of GLTFNodes.
     */
    private GLTFNode[] getNodes(JsonValue nodesRoot) {
        ArrayList<GLTFNode> gltfNodes = new ArrayList<>(4);
        for (JsonValue value : nodesRoot) { // looping through each node

            GLTFNode node = new GLTFNode();
            for (JsonValue fieldValue : value) { // looping through each node's fields (mesh, name, translation...)
                switch (fieldValue.name()) {
                    case "mesh":
                        node.mesh = fieldValue.asInt();
                        break;
                    case "name":
                        node.name = fieldValue.asString();
                        break;
                    case "rotation":
                        // .GLTF gives the quaternion fields as doubles, but libGDX works in floats.
                        float[] quatFields = fieldValue.asFloatArray();
                        node.rotation.set(new Quaternion(quatFields[0], quatFields[1], quatFields[2], quatFields[3]));
                        break;
                    case "scale":
                        float[] scaleFields = fieldValue.asFloatArray();
                        node.scale.set(new Vector3(scaleFields[0], scaleFields[1], scaleFields[2]));
                        break;
                    case "translation":
                        float[] transFields = fieldValue.asFloatArray();
                        node.translation.set(new Vector3(transFields[0], transFields[1], transFields[2]));
                        break;
                }
            }
            gltfNodes.add(node);
        }
        return gltfNodes.toArray(GLTFNode[]::new);
    }

    public static class GLTFLoaderParameters extends AssetLoaderParameters<Model> { }
}

