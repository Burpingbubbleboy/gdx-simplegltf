package net.bupy.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import net.bupy.gltf.GLTFLoader;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {

    private static final String MODEL_PATH = "monkeys.gltf";

    private ModelBatch modelBatch;
    private Model loadedModel;
    private ModelInstance loadedModelInstance;
    private Model skyBoxModel;
    private ModelInstance skyBoxModelInstance;
    private FitViewport viewport;
    private AssetManager assetManager;
    private EditorController controller;
    private Environment environment;

    @Override
    public void create() {
        modelBatch = new ModelBatch();
        assetManager = new AssetManager();
        controller = new EditorController();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        skyBoxModel = createSkyBoxModel();
        skyBoxModelInstance = new ModelInstance(skyBoxModel);

        environment = new Environment();
        environment.set(ColorAttribute.createAmbientLight(0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 1f, -0.8f, -0.2f));

        assetManager.setLoader(Model.class, ".gltf", new GLTFLoader());
        assetManager.load(MODEL_PATH, Model.class);
        /*
        assetManager.load(MODEL_PATH, Model.class, new GLTFLoader.GLTFLoaderParameters()
            .setTextureSampler(GLTFLoader.GLTFLoaderParameters.TextureSampler.LINEAR)
            .setAlphaBlending(true));
         */
        assetManager.finishLoading();

        loadedModel = assetManager.get(MODEL_PATH);
        loadedModelInstance = new ModelInstance(loadedModel);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.05f, 0.05f, 0.05f, 1f, true);

        controller.update(Gdx.graphics.getDeltaTime());

        modelBatch.begin(controller.getCamera());
        modelBatch.render(loadedModelInstance, environment);
        modelBatch.render(skyBoxModelInstance);
        modelBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
    }

    private Model createSkyBoxModel() {
        final float size = -375f;
        ModelBuilder builder = new ModelBuilder();
        return builder.createBox(size, size, size,
            new Material(ColorAttribute.createDiffuse(0.5f, 0.5f, 0.8f, 1f)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
    }

}
