package fr.layer4.dek;

public class DekException extends RuntimeException {

    public DekException(String message) {
        super(message);
    }

    public DekException(String message, Throwable cause) {
        super(message, cause);
    }

    public DekException(Throwable cause) {
        super(cause);
    }

    protected DekException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
