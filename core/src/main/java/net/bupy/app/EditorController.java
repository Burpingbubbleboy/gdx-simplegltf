package net.bupy.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class EditorController {

    private enum MovementState { FREE, MOVING }

    private static final float MOVE_SPEED = 16f;
    private static final float MOUSE_SENSITIVITY = 0.5f;

    private final PerspectiveCamera camera;
    private final Quaternion rotation;

    private MovementState movementState = MovementState.FREE;
    private float cameraYaw = 135f;
    private float cameraPitch = 45f;

    public EditorController() {
        rotation = new Quaternion().idt().setEulerAngles(cameraYaw, cameraPitch, 0f);
        camera = new PerspectiveCamera(85, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(-4f, 4f, 4f);
        camera.near = 0.05f;
        camera.far = 400f;
        updateCameraTransforms();
    }

    public void update(float deltaTime) {
        movementState = Gdx.input.isButtonPressed(Input.Buttons.RIGHT) ? MovementState.MOVING : MovementState.FREE;
        if (movementState == MovementState.MOVING) {
            handleMovement(deltaTime);
            handleMouseLooking();
        }
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    private void handleMovement(float deltaTime) {
        Vector3 movementDirection = new Vector3();

        if (Gdx.input.isKeyPressed(Input.Keys.W))
            movementDirection.add(camera.direction);
        if (Gdx.input.isKeyPressed(Input.Keys.A))
            movementDirection.add(camera.direction.cpy().crs(Vector3.Y).scl(-1f));
        if (Gdx.input.isKeyPressed(Input.Keys.S))
            movementDirection.add(camera.direction.cpy().scl(-1f));
        if (Gdx.input.isKeyPressed(Input.Keys.D))
            movementDirection.add(camera.direction.cpy().crs(Vector3.Y));
        if (Gdx.input.isKeyPressed(Input.Keys.E))
            movementDirection.add(Vector3.Y);
        if (Gdx.input.isKeyPressed(Input.Keys.Q))
            movementDirection.add(Vector3.Y.cpy().scl(-1f));

        float moveSpeedMultiplier = 1f;

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))
            moveSpeedMultiplier = 0.33f;

        camera.translate(movementDirection.nor().scl(deltaTime * MOVE_SPEED * moveSpeedMultiplier));
        updateCameraTransforms();
    }

    private void handleMouseLooking() {
        cameraYaw -= Gdx.input.getDeltaX() * MOUSE_SENSITIVITY;
        cameraPitch += Gdx.input.getDeltaY() * MOUSE_SENSITIVITY;

        cameraPitch = Math.min(Math.max(cameraPitch, -89f), 89f);
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
