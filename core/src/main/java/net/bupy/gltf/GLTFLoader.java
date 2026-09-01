package net.bupy.gltf;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import static net.bupy.gltf.GLTFMesh.GLTFPrimitive;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Exists to load .GLTF model files.<br>
 * Very early version! Only supports loading flat textured meshes with no animation!<br>
 * Only supports reading from ONE URI! THIS MEANS ONLY ONE .BIN FILE PER .GLTF!<br>
 * Does not include a check for >32k vertices! Large models may import with some issues! Break up your models if issues arise!<br>
 * Use {@code GLTFLoaderParameters} to configure how a .GLTF file should be loaded!
 */
public class GLTFLoader extends AsynchronousAssetLoader<Model, GLTFLoader.GLTFLoaderParameters> {

    /**
     * Creates a new {@code GLTFLoader} with the specified resolver.
     * @param resolver The specified resolver
     */
    public GLTFLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    /**
     * Creates a new {@code GLTFLoader} with an {@code InternalFileHandleResolver}.
     */
    public GLTFLoader() {
        super(new InternalFileHandleResolver());
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, GLTFLoaderParameters parameter) {
    }

    /// Highly inefficient at the moment; setting everything up for simplicity.
    @Override
    public Model loadSync(AssetManager manager, String fileName, FileHandle file, GLTFLoaderParameters parameter) {

        if (parameter == null) {
            parameter = new GLTFLoaderParameters()
                .setAlphaBlending(false)
                .setTextureSampler(GLTFLoaderParameters.TextureSampler.LINEAR)
                .setTextureWrap(Texture.TextureWrap.Repeat);
        }

        // get the contents of the .GLTF file.
        // .GLTF files are in the .JSON format
        JsonReader reader = new JsonReader();
        JsonValue gltfContents = reader.parse(file);

        // get all the nodes in the .GLTF file, including translation, rotation and scale
        GLTFNode[] gltfNodes = new GLTFNode[0];
        for (JsonValue value : gltfContents) {
            if (value.name.equals("nodes")) {
                gltfNodes = getNodes(value);
            }
        }

        // get all the meshes in the .GLTF file
        GLTFMesh[] gltfMeshes = new GLTFMesh[0];
        for (JsonValue value : gltfContents) {
            if (value.name.equals("meshes")) {
                gltfMeshes = getMeshes(value);
            }
        }

        // get all the accessors in the .GLTF file
        GLTFAccessor[] gltfAccessors = new GLTFAccessor[0];
        for (JsonValue value : gltfContents) {
            if (value.name.equals("accessors")) {
                gltfAccessors = getAccessors(value);
            }
        }

        // get all the buffer views in the .GLTF file
        GLTFBufferView[] gltfBufferViews = new GLTFBufferView[0];
        for (JsonValue value : gltfContents) {
            if (value.name.equals("bufferViews")) {
                gltfBufferViews = getBufferViews(value);
            }
        }

        // get all the buffers in the .GLTF file
        GLTFBuffer[] gltfBuffers = new GLTFBuffer[0];
        for (JsonValue value : gltfContents) {
            if (value.name.equals("buffers")) {
                gltfBuffers = getBuffers(value);
            }
        }

        // get all the materials in the .GLTF file
        GLTFMaterial[] gltfMaterials = new GLTFMaterial[0];
        for (JsonValue value : gltfContents) {
            if (value.name.equals("materials")) {
                gltfMaterials = getMaterials(value);
            }
        }

        // get all the textures in the .GLTF file
        GLTFTexture[] gltfTextures = new GLTFTexture[0];
        for (JsonValue value : gltfContents) {
            if (value.name.equals("textures")) {
                gltfTextures = getTextures(value);
            }
        }

        GLTFImage[] gltfImages = new GLTFImage[0];
        for (JsonValue value : gltfContents) {
            if (value.name.equals("images")) {
                gltfImages = getImages(value);
            }
        }

        GLTFSampler[] gltfSamplers = new GLTFSampler[0];
        for (JsonValue value : gltfContents) {
            if (value.name.equals("samplers")) {
                gltfSamplers = getSamplers(value);
            }
        }

        // slapped this check on to avoid confusion when the loader crashes when loading nothing
        if (gltfMeshes.length == 0 || gltfNodes.length == 0) {
            throw new GLTFException("No meshes or nodes to load! Ensure the .GLTF file was exported with nodes and meshes!");
        }

        // alright, almost to the binary parsing, we just need the materials now
        // if you're reading this and are still somehow unaware,
        // this loader DOES NOT SUPPORT PBR! IT WILL ONLY USE FLAT MATERIALS!
        // TODO: add support for samplers
        ArrayList<Material> materials = new ArrayList<>(gltfMaterials.length);
        for (GLTFMaterial gltfMaterial : gltfMaterials) {

            ArrayList<Attribute> materialAttributes = new ArrayList<>(3);

            // okay, lets disable backface culling if "doubleSided" in the .GLTF says so
            if (gltfMaterial.doubleSided()) {
                materialAttributes.add(IntAttribute.createCullFace(0));
            }

            // if there's a material present, but it doesn't have a texture
            if (gltfMaterial.baseColorTextureIndex() == -1) {
                float[] baseColor = gltfMaterial.baseColorFactor();
                ColorAttribute diffuseAttribute = ColorAttribute.createDiffuse(baseColor[0], baseColor[1], baseColor[2], baseColor[3]);
                materialAttributes.add(diffuseAttribute);
                materials.add(new Material(materialAttributes.toArray(Attribute[]::new)));
                continue;
            }

            // ok great lets set up the texture filtering options, assuming we have a texture for this material
            GLTFTexture gltfTexture = gltfTextures[gltfMaterial.baseColorTextureIndex()];
            GLTFImage gltfImage = gltfImages[gltfTexture.source()];

            Texture.TextureFilter minFilter;
            Texture.TextureFilter magFilter;
            boolean useMipMaps = false;
            switch (parameter.sampler()) {
                case GLTF:
                    // TODO: FALLBACK TO LINEAR FOR NOW
                case LINEAR:
                    minFilter = Texture.TextureFilter.Linear;
                    magFilter = Texture.TextureFilter.Linear;
                    break;
                case NEAREST:
                    minFilter = Texture.TextureFilter.Nearest;
                    magFilter = Texture.TextureFilter.Nearest;
                    break;
                case
                    LINEAR_MIPMAP: // dunno if this works... Texture.TextureFilter.MipMapLinearLinear causes min Nearest filtering??? huh????
                    minFilter = Texture.TextureFilter.Linear;
                    magFilter = Texture.TextureFilter.Linear;
                    useMipMaps = true;
                    break;
                default:
                    minFilter = Texture.TextureFilter.Linear;
                    magFilter = Texture.TextureFilter.Linear;
                    break;
            }
            Texture texture = new Texture(Gdx.files.internal(convertURIToPath(gltfImage.uri(), file)), useMipMaps);
            texture.setFilter(minFilter, magFilter);
            materialAttributes.add(TextureAttribute.createDiffuse(texture));

            // okay, let's set the texture wrap according to whatever the params say
            texture.setWrap(parameter.textureWrap, parameter.textureWrap);

            // alright! let's set up alpha blending if the params say there's alpha
            if (parameter.useAlpha()) {
                materialAttributes.add(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
                materialAttributes.add(new FloatAttribute(FloatAttribute.AlphaTest, 0.5f));
            }

            materials.add(new Material(materialAttributes.toArray(Attribute[]::new)));
        }

        // wooo parsing binary time!
        // this part of the process looks scary, so if you're reading this code from the future,
        // please remember to look up the official Khronos GLTF 2.0 reference!
        byte[] rawBinBytes = Gdx.files.internal(convertURIToPath(gltfBuffers[0].uri(), file)).readBytes();

        // each element represents one mesh
        ArrayList<MeshPartWithMaterial[]> totalMeshParts = new ArrayList<>(3);

        for (GLTFNode node : gltfNodes) {

            GLTFMesh mesh = gltfMeshes[node.mesh];

            // each GLTFPrimitive is roughly equal to one meshPart
            MeshPartWithMaterial[] meshParts = new MeshPartWithMaterial[mesh.gltfPrimitives().length];
            int meshPartIndex = 0;

            for (GLTFPrimitive primitive : mesh.gltfPrimitives()) {

                float[] positions;
                float[] normals;
                float[] texCoord0s;
                short[] indices;

                /*
                 * do the position type shit
                 */

                {
                    GLTFAccessor positionAccessor = gltfAccessors[primitive.POSITION()];
                    GLTFBufferView positionBufferView = gltfBufferViews[positionAccessor.bufferView()];

                    int totalByteOffset = positionAccessor.byteOffset() + positionBufferView.byteOffset();
                    int totalByteLength = positionBufferView.byteLength();
                    int currentVertexPosIndex = 0;
                    positions = new float[positionAccessor.count() * 3]; // Stores raw VEC3 -- count() = floats ;; 3 = VEC3
                    for (int i = totalByteOffset; i < totalByteOffset + totalByteLength; i += 4) {

                        // a float is made up of four bytes. this byte[] will only ever have a length of four.
                        byte[] rawFloat = new byte[]{rawBinBytes[i], rawBinBytes[i + 1], rawBinBytes[i + 2], rawBinBytes[i + 3]};
                        // .GLTF is a little indian
                        float actualFloat = ByteBuffer.wrap(rawFloat).order(ByteOrder.LITTLE_ENDIAN).getFloat();

                        positions[currentVertexPosIndex] = actualFloat;
                        currentVertexPosIndex++;
                    }
                }

                /*
                 * do the normal type shit
                 * note that postions and normals, when read by libGDX, should be in a single float[] array that has the layout of:
                 * {posX, posY, posZ, norX, norY, norZ, ...}
                 * currently kept separate for readability and separation of concerns -- this float[] array of normals will be joined later
                 * during mesh construction
                 */

                {
                    GLTFAccessor normalAccessor = gltfAccessors[primitive.NORMAL()];
                    GLTFBufferView normalBufferView = gltfBufferViews[normalAccessor.bufferView()];

                    int totalByteOffset = normalAccessor.byteOffset() + normalBufferView.byteOffset();
                    int totalByteLength = normalBufferView.byteLength();
                    int currentVertexPosIndex = 0;
                    // normals are given per-face, not per-vertex
                    normals = new float[normalAccessor.count() * 3]; // Stores raw VEC3 -- count() = floats ;; 3 = VEC3
                    for (int i = totalByteOffset; i < totalByteOffset + totalByteLength; i += 4) {

                        // a float is made up of four bytes. this byte[] will only ever have a length of four.
                        byte[] rawFloat = new byte[]{rawBinBytes[i], rawBinBytes[i + 1], rawBinBytes[i + 2], rawBinBytes[i + 3]};
                        // .GLTF is a little indian
                        float actualFloat = ByteBuffer.wrap(rawFloat).order(ByteOrder.LITTLE_ENDIAN).getFloat();

                        normals[currentVertexPosIndex] = actualFloat;
                        currentVertexPosIndex++;
                    }
                }

                /*
                 * do the texcoord_0 type shit
                 */

                {
                    GLTFAccessor texCoordAccessor = gltfAccessors[primitive.TEXCOORD_0()];
                    GLTFBufferView texCoordBufferView = gltfBufferViews[texCoordAccessor.bufferView()];

                    int totalByteOffset = texCoordAccessor.byteOffset() + texCoordBufferView.byteOffset();
                    int totalByteLength = texCoordBufferView.byteLength();
                    int currentTexCoordIndex = 0;
                    texCoord0s = new float[texCoordAccessor.count() * 2]; // Stores raw VEC2 -- count() = floats ;; 2 = VEC2
                    for (int i = totalByteOffset; i < totalByteOffset + totalByteLength; i += 4) {

                        // a float is made up of four bytes. this byte[] will only ever have a length of four.
                        byte[] rawFloat = new byte[]{rawBinBytes[i], rawBinBytes[i + 1], rawBinBytes[i + 2], rawBinBytes[i + 3]};
                        // .GLTF is a little indian
                        float actualFloat = ByteBuffer.wrap(rawFloat).order(ByteOrder.LITTLE_ENDIAN).getFloat();

                        texCoord0s[currentTexCoordIndex] = actualFloat;
                        currentTexCoordIndex++;
                    }
                }

                /*
                 * do the indices type shit
                 */

                {
                    GLTFAccessor indexAccessor = gltfAccessors[primitive.indices()];
                    GLTFBufferView indexBufferView = gltfBufferViews[indexAccessor.bufferView()];

                    int totalByteOffset = indexAccessor.byteOffset() + indexBufferView.byteOffset();
                    int totalByteLength = indexBufferView.byteLength();
                    int currentIndexPos = 0;
                    indices = new short[indexAccessor.count()]; // garbage name, sorry
                    for (int i = totalByteOffset; i < totalByteOffset + totalByteLength; i += 2) {

                        // a short is made up of two bytes. this will only ever have a length of two.
                        byte[] rawShort = new byte[]{rawBinBytes[i], rawBinBytes[i + 1]};
                        // .GLTF is little endian
                        short actualShort = ByteBuffer.wrap(rawShort).order(ByteOrder.LITTLE_ENDIAN).getShort();

                        indices[currentIndexPos] = actualShort;
                        currentIndexPos++;
                    }
                }

                /*
                 * construct the meshpart
                 */

                float[] vertices = interleaveArrays(positions, normals, texCoord0s);

                Mesh mesh1 = new Mesh(true, vertices.length / 8, indices.length,
                    new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                    new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
                    new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
                mesh1.setVertices(vertices);
                mesh1.setIndices(indices);

                MeshPartWithMaterial part = new MeshPartWithMaterial();
                part.id = "" + meshPartIndex;
                part.offset = 0;
                part.size = indices.length;
                part.mesh = mesh1;
                part.primitiveType = GL20.GL_TRIANGLES;
                part.materialIndex = primitive.material();

                meshParts[meshPartIndex] = part;
                meshPartIndex++;

            } // close GLTFPrimitive loop

            totalMeshParts.add(meshParts);
        } // close GLTFMesh loop



        // okay you have all the meshes, and they look amazing
        // go ahead and build them into one complete model
        // note that I'm unsure of how libGDX handles batching, so this might not be the most performant way to do things

        ModelBuilder builder1 = new ModelBuilder();
        builder1.begin();

        for (int i = 0; i < totalMeshParts.size(); i++) {
            for (int k = 0; k < totalMeshParts.get(i).length; k++) {
                MeshPartWithMaterial selectedPart = totalMeshParts.get(i)[k];

                // yuck... having to find the right material for the right MeshPart... eugh...

                Node node = builder1.node();
                node.id = gltfNodes[i].name;
                node.translation.set(gltfNodes[i].translation);
                node.rotation.set(gltfNodes[i].rotation);
                node.scale.set(gltfNodes[i].scale);
                builder1.part(selectedPart, materials.get(selectedPart.materialIndex));
            }
        }

        return builder1.end();
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, GLTFLoaderParameters parameter) {
        // doesn't need any dependencies :)
        return null;
    }

    /**
     * Merges the given arrays together under a specific layout.<br>
     * Specifically coded for positions (len = 3),<br>
     * normals (len = 3), and texCoords (len = 2).<br>
     * In that order.
     */
    private float[] interleaveArrays(float[] positions, float[] normals, float[] texCoord0s) {
        float[] interleaved = new float[positions.length + normals.length + texCoord0s.length];

        for (int currentMeshIndex = 0; currentMeshIndex < positions.length / 3; currentMeshIndex++) {

            for (int i = 0; i < positions.length / 3; i++) {
                int source = i * 3;
                int source2 = i * 2;
                int dest = i * 8;

                interleaved[dest] = positions[source];
                interleaved[dest + 1] = positions[source + 1];
                interleaved[dest + 2] = positions[source + 2];
                interleaved[dest + 3] = normals[source];
                interleaved[dest + 4] = normals[source + 1];
                interleaved[dest + 5] = normals[source + 2];
                interleaved[dest + 6] = texCoord0s[source2];
                interleaved[dest + 7] = texCoord0s[source2 + 1];
            }
        }

        return interleaved;
    }

    /**
     * Generates an array of GLTFSampler data classes.
     * @param samplersRoot JSON Root. Should be named "textures," if you're parsing correctly.
     * @return The array of GLTFSampler
     */
    private GLTFSampler[] getSamplers(JsonValue samplersRoot) {
        ArrayList<GLTFSampler> gltfTextures = new ArrayList<>(1);
        GLTFSampler.Builder builder = new GLTFSampler.Builder();

        for (JsonValue value : samplersRoot) { // looping through each texture
            for (JsonValue fieldValue : value) {
                switch (fieldValue.name()) {
                    case "magFilter":
                        builder.setMagFilter(fieldValue.asInt());
                        break;
                    case "minFilter":
                        builder.setMinFilter(fieldValue.asInt());
                        break;
                }
            }

            gltfTextures.add(builder.build());
            builder.reset();
        }

        return gltfTextures.toArray(GLTFSampler[]::new);
    }

    /**
     * Generates an array of GLTFImage data classes.
     * @param imagesRoot JSON Root. Should be named "textures," if you're parsing correctly.
     * @return The array of GLTFImage
     */
    private GLTFImage[] getImages(JsonValue imagesRoot) {
        ArrayList<GLTFImage> gltfTextures = new ArrayList<>(1);
        GLTFImage.Builder builder = new GLTFImage.Builder();

        for (JsonValue value : imagesRoot) { // looping through each texture
            for (JsonValue fieldValue : value) {
                switch (fieldValue.name()) {
                    case "uri":
                        builder.setUri(fieldValue.asString());
                        break;
                }
            }

            gltfTextures.add(builder.build());
            builder.reset();
        }

        return gltfTextures.toArray(GLTFImage[]::new);
    }

    /**
     * Generates an array of GLTFTexture data classes.
     * @param texturesRoot JSON Root. Should be named "textures," if you're parsing correctly.
     * @return The array of GLTFTextures
     */
    private GLTFTexture[] getTextures(JsonValue texturesRoot) {
        ArrayList<GLTFTexture> gltfTextures = new ArrayList<>(1);
        GLTFTexture.Builder builder = new GLTFTexture.Builder();

        for (JsonValue value : texturesRoot) { // looping through each texture
            for (JsonValue fieldValue : value) {
                switch (fieldValue.name()) {
                    case "sampler":
                        builder.setSampler(fieldValue.asInt());
                        break;
                    case "source":
                        builder.setSource(fieldValue.asInt());
                        break;
                }
            }

            gltfTextures.add(builder.build());
            builder.reset();
        }

        return gltfTextures.toArray(GLTFTexture[]::new);
    }

    /**
     * Generates an array of GLTFMaterial data classes.
     * @param materialsRoot JSON Root. Should be named "materials," if you're parsing correctly.
     * @return The array of GLTFMaterials
     */
    private GLTFMaterial[] getMaterials(JsonValue materialsRoot) {
        ArrayList<GLTFMaterial> gltfMaterials = new ArrayList<>(1);
        GLTFMaterial.Builder builder = new GLTFMaterial.Builder();

        for (JsonValue value : materialsRoot) { // looping through each material
            for (JsonValue fieldValue : value) {

                switch (fieldValue.name()) {
                    case "name":
                        builder.setName(fieldValue.asString());
                        break;
                    case "doubleSided":
                        builder.setDoubleSided(fieldValue.asBoolean());
                        break;
                    case "pbrMetallicRoughness": // just how .GLTF is set up, weird name, I know...
                        if (fieldValue.has("baseColorTexture")) {
                            JsonValue baseColorTexture = fieldValue.get("baseColorTexture");
                            builder.setBaseColorTextureIndex(baseColorTexture.get("index").asInt());
                            if (baseColorTexture.has("texCoord"))
                                builder.setTexCoord(baseColorTexture.get("texCoord").asInt());
                        } if (fieldValue.has("baseColorFactor")) {
                            builder.setBaseColorFactor(fieldValue.get("baseColorFactor").asFloatArray());
                    }
                        break;
                }
            }

            gltfMaterials.add(builder.build());
            builder.reset();
        }

        return gltfMaterials.toArray(GLTFMaterial[]::new);
    }

    /**
     * Generates an array of GLTFBuffer data classes.
     * @param buffersRoot JSON Root. Should be named "buffers," if you're parsing correctly.
     * @return The array of GLTFBuffers
     */

    // note that this function was written to look like every other "get" method in this class
    // however, it should only ever return an array of just one element -- one massive buffer
    // this was done to make future updates (where multiple buffers are supported) easier
    private GLTFBuffer[] getBuffers(JsonValue buffersRoot) {
        ArrayList<GLTFBuffer> gltfBuffers = new ArrayList<>(1);
        GLTFBuffer.Builder builder = new GLTFBuffer.Builder();

        for (JsonValue value : buffersRoot) { // looping through each buffer... 99% of the time this is just one massive buffer
            for (JsonValue fieldValue : value) {
                switch (fieldValue.name()) {
                    case "byteLength":
                        builder.setByteLength(fieldValue.asInt());
                        break;
                    case "uri":
                        builder.setUri(fieldValue.asString());
                        break;
                }
            }

            gltfBuffers.add(builder.build());
            builder.reset();
        }

        // extra check to ensure that only one buffer is getting returned
        // this check may be removed in later versions, but for now only one .BIN file is supported per .GTLF file
        if (gltfBuffers.size() > 1) {
            throw new RuntimeException("Warning! Detected more than one buffer! This is unsupported!");
        }

        return gltfBuffers.toArray(GLTFBuffer[]::new);
    }

    /**
     * Generates an array of GLTFBufferView data classes.
     * @param bufferViewsRoot JSON Root. Should be named "bufferViews," if you're parsing correctly.
     * @return The array of GLTFBufferViews
     */
    private GLTFBufferView[] getBufferViews(JsonValue bufferViewsRoot) {
        ArrayList<GLTFBufferView> gltfBufferViews = new ArrayList<>(4);
        GLTFBufferView.Builder builder = new GLTFBufferView.Builder();

        for (JsonValue value : bufferViewsRoot) { // looping through each bufferView
            for (JsonValue fieldValue : value) {
                switch (fieldValue.name()) {
                    case "buffer":
                        builder.setBuffer(fieldValue.asInt());
                        break;
                    case "byteLength":
                        builder.setByteLength(fieldValue.asInt());
                        break;
                    case "byteOffset":
                        builder.setByteOffset(fieldValue.asInt());
                        break;
                }
            }

            gltfBufferViews.add(builder.build());
            builder.reset();
        }

        return gltfBufferViews.toArray(GLTFBufferView[]::new);
    }

    /**
     * Generates an array of GLTFAccessor data classes.
     * @param accessorsRoot JSON Root. Should be named "accessors," if you're parsing correctly.
     * @return The array of GLTFAccessors
     */
    private GLTFAccessor[] getAccessors(JsonValue accessorsRoot) {
        ArrayList<GLTFAccessor> gltfAccessors = new ArrayList<>(4);
        GLTFAccessor.Builder builder = new GLTFAccessor.Builder();

        for (JsonValue value : accessorsRoot) { // looping through each accessor
            for (JsonValue fieldValue : value) {
                switch (fieldValue.name()) {
                    case "bufferView":
                        builder.setBufferView(fieldValue.asInt());
                        break;
                    case "byteOffset":
                        builder.setByteOffset(fieldValue.asInt());
                        break;
                    case "componentType":
                        builder.setComponentType(fieldValue.asInt());
                        break;
                    case "count":
                        builder.setCount(fieldValue.asInt());
                        break;
                    case "max":
                        builder.setMax(fieldValue.asFloatArray());
                        break;
                    case "min":
                        builder.setMin(fieldValue.asFloatArray());
                        break;
                    case "type":
                        builder.setType(fieldValue.asString());
                        break;
                }
            }

            gltfAccessors.add(builder.build());
            builder.reset();
        }

        return gltfAccessors.toArray(GLTFAccessor[]::new);
    }

    /**
     * Generates an array of GLTFMesh data classes.
     * @param meshesRoot JSON Root. Should be named "meshes," if you're parsing correctly.
     * @return The array of GLTFMeshes
     */

    // I would like to personally and deeply apologize for the severe nesting of the code here.
    // If you dare read this code, I can only wish you the best of luck.
    private GLTFMesh[] getMeshes(JsonValue meshesRoot) {
        ArrayList<GLTFMesh> gltfMeshes = new ArrayList<>(4);
        GLTFMesh.Builder builder = new GLTFMesh.Builder();

        for (JsonValue value : meshesRoot) { // looping through each mesh
            for (JsonValue fieldValue : value) {
                switch (fieldValue.name()) {
                    case "name":
                        builder.setName(fieldValue.asString());
                        break;
                    case "primitives":

                        for (JsonValue primitiveValue : fieldValue) { // for each primitive,
                            builder.primitive();

                            for (JsonValue primitiveFieldValue : primitiveValue) { // for each field in the primitive,

                                switch (primitiveFieldValue.name()) {
                                    case "attributes":
                                        for (JsonValue attributeFieldValue : primitiveFieldValue) { // for each field in the attributes array,

                                            switch (attributeFieldValue.name()) {
                                                case "POSITION":
                                                    builder.setPrimitivePOSITION(attributeFieldValue.asInt());
                                                    break;
                                                case "NORMAL":
                                                    builder.setPrimitiveNORMAL(attributeFieldValue.asInt());
                                                    break;
                                                case "TEXCOORD_0":
                                                    builder.setPrimitiveTEXCOORD_0(attributeFieldValue.asInt());
                                                    break;
                                            }
                                        }
                                        break;
                                    case "indices":
                                        builder.setPrimitiveIndices(primitiveFieldValue.asInt());
                                        break;
                                    case "material":
                                        builder.setPrimitiveMaterial(primitiveFieldValue.asInt());
                                        break;
                                }
                            }
                        }

                }
            }

            gltfMeshes.add(builder.build());
            builder.reset();
        }
        return gltfMeshes.toArray(GLTFMesh[]::new);
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

    /**
     * In GLTF, URIs are not always paths. This method performs a simple conversion to convert a regular URI<br>
     * into a path that {@code Gdx.files.internal()} can understand.
     * @param uri The raw URI.
     * @param file The {@code FileHandle} that the whole .GLTF was loaded with.
     * @return The converted path.
     */
    private String convertURIToPath(String uri, FileHandle file) {
        return file.parent().child(uri).path();
    }

    public static class GLTFLoaderParameters extends AssetLoaderParameters<Model> {

        public enum TextureSampler { GLTF, NEAREST, LINEAR, LINEAR_MIPMAP }

        private TextureSampler sampler = TextureSampler.GLTF;
        private Texture.TextureWrap textureWrap = Texture.TextureWrap.Repeat;
        private boolean useAlpha = false;

        /**
         * Overrides the texture sampling defined by the .GLTF file.<br>
         * Set to {@code TextureSampler.GLTF} to allow the .GLTF file to decide the sampling used.<br>
         * {@code TextureSampler.GLTF} is used by default
         * @param sampler Texture sampling to be used
         * @return This object for method chaining
         */
        public GLTFLoaderParameters setTextureSampler(TextureSampler sampler) {
            this.sampler = sampler;
            return this;
        }

        public TextureSampler sampler() {
            return sampler;
        }

        /**
         * Sets the texture wrap to be used on the material.<br>
         * @param textureWrap The wrapping method to be used.
         * @return This object for method chaining
         */
        public GLTFLoaderParameters setTextureWrap(Texture.TextureWrap textureWrap) {
            this.textureWrap = textureWrap;
            return this;
        }

        public Texture.TextureWrap textureWrap() {
            return textureWrap;
        }

        /**
         * Enables/Disables alpha blending on material.<br>
         * The .GLTF loader does not currently determine this automatically for you.<br>
         * {@code false} = no alpha blending. {@code true} = alpha blending is enabled.<br>
         * {@code false} is used by default.
         * @param useAlpha Enable/Disable alpha blending
         * @return This object for method chaining
         */
        public GLTFLoaderParameters setAlphaBlending(boolean useAlpha) {
            this.useAlpha = useAlpha;
            return this;
        }

        public boolean useAlpha() {
            return useAlpha;
        }
    }

    private static final class MeshPartWithMaterial extends MeshPart {

        int materialIndex = -1;
    }
}

