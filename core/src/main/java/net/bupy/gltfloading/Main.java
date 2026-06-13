package net.bupy.gltfloading;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.utils.ScreenUtils;
import net.bupy.gltfloading.gltfloader.GLTFLoader;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {

    private ModelBatch modelBatch;
    private Model loadedModel;
    private ModelInstance loadedModelInstance;
    private AssetManager assetManager;
    private EditorController controller;
    private Environment environment;

    @Override
    public void create() {
        modelBatch = new ModelBatch();
        assetManager = new AssetManager();

        controller = new EditorController();

        environment = new Environment();
        environment.set(ColorAttribute.createAmbientLight(0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        assetManager.setLoader(Model.class, ".gltf", new GLTFLoader(new InternalFileHandleResolver()));
        assetManager.load("tree.gltf", Model.class);
        assetManager.finishLoading();

        loadedModel = assetManager.get("tree.gltf");
        loadedModelInstance = new ModelInstance(loadedModel);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.05f, 0.05f, 0.05f, 1f, true);

        controller.update(Gdx.graphics.getDeltaTime());

        modelBatch.begin(controller.getCamera());
        modelBatch.render(loadedModelInstance, environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
    }
}
