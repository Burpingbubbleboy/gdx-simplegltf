package net.bupy.gltfloading;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class EditorController {

    private enum MouseState { FREE, LOOKING, MOVING }

    private static final float MOVE_SPEED = 16f;

    private final PerspectiveCamera camera;
    private final Quaternion rotation;

    private MouseState mouseState = MouseState.FREE;
    private float cameraYaw = -135f;
    private float cameraPitch = 45f;

    public EditorController() {
        rotation = new Quaternion().idt().setEulerAngles(cameraYaw, cameraPitch, 0f);
        camera = new PerspectiveCamera(85, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(4f, 4f, 4f);
        camera.near = 0.05f;
        camera.far = 20f;
        updateCameraTransforms();
    }

    public void update(float deltaTime) {
        mouseState = Gdx.input.isButtonPressed(Input.Buttons.RIGHT) ? MouseState.LOOKING : MouseState.FREE;
        mouseState = Gdx.input.isButtonPressed(Input.Buttons.LEFT) ? MouseState.MOVING : mouseState;
        switch (mouseState) {
            case MOVING:
                handleMouseMoving(deltaTime);
                break;
            case LOOKING:
                handleMouseLooking();
                break;
        }
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    private void handleMouseMoving(float deltaTime) {
        camera.translate(camera.direction.cpy().scl(deltaTime * MOVE_SPEED));
        updateCameraTransforms();
    }

    private void handleMouseLooking() {
        cameraYaw -= Gdx.input.getDeltaX();
        cameraPitch += Gdx.input.getDeltaY();

        cameraPitch = Math.min(Math.max(cameraPitch, -90f), 90f);
        cameraYaw = cameraYaw % 360f;

        updateCameraTransforms();
    }

    private void updateCameraTransforms() {
        rotation.idt();
        rotation.setEulerAngles(cameraYaw, cameraPitch, 0f);

        camera.direction.set(0f, 0f, 1f);
        camera.up.set(0f, 1f, 0f);
        camera.rotate(rotation);
        camera.update();
    }
}
