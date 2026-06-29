package cn.byronlab.weather.domain.result;

import java.util.Objects;

public final class DomainError {

    public enum Type {
        INVALID_INPUT,
        MISSING_CURRENT_CITY,
        NOT_FOUND,
        NETWORK,
        STORAGE,
        UNKNOWN
    }

    private final Type type;
    private final String message;
    private final Throwable cause;

    public DomainError(Type type, String message) {
        this(type, message, null);
    }

    public DomainError(Type type, String message, Throwable cause) {
        this.type = type == null ? Type.UNKNOWN : type;
        this.message = message;
        this.cause = cause;
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getCause() {
        return cause;
    }

    public static DomainError invalidInput(String message) {
        return new DomainError(Type.INVALID_INPUT, message);
    }

    public static DomainError missingCurrentCity() {
        return new DomainError(Type.MISSING_CURRENT_CITY, "Current city is not configured.");
    }

    public static DomainError unknown(Throwable cause) {
        String message = cause == null ? "Unknown error." : cause.getMessage();
        return new DomainError(Type.UNKNOWN, message, cause);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DomainError)) {
            return false;
        }
        DomainError that = (DomainError) o;
        return type == that.type
                && Objects.equals(message, that.message)
                && Objects.equals(cause, that.cause);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, message, cause);
    }
}
