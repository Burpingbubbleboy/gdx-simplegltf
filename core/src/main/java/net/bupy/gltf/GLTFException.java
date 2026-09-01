package net.bupy.gltf;

public class GLTFException extends RuntimeException {

    public GLTFException() {
    }

    public GLTFException(String message) {
        super(message);
    }

    public GLTFException(String message, Throwable cause) {
        super(message, cause);
    }

    public GLTFException(Throwable cause) {
        super(cause);
    }

    public GLTFException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
