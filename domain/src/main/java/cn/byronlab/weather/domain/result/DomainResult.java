package cn.byronlab.weather.domain.result;

public final class DomainResult<T> {

    private final T data;
    private final DomainError error;

    private DomainResult(T data, DomainError error) {
        this.data = data;
        this.error = error;
    }

    public static <T> DomainResult<T> success(T data) {
        return new DomainResult<>(data, null);
    }

    public static <T> DomainResult<T> failure(DomainError error) {
        if (error == null) {
            throw new IllegalArgumentException("error == null");
        }
        return new DomainResult<>(null, error);
    }

    public boolean isSuccess() {
        return error == null;
    }

    public boolean isFailure() {
        return error != null;
    }

    public T getData() {
        return data;
    }

    public DomainError getError() {
        return error;
    }
}
