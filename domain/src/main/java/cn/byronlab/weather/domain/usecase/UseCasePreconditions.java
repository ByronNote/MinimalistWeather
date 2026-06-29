package cn.byronlab.weather.domain.usecase;

final class UseCasePreconditions {

    private UseCasePreconditions() {
    }

    static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
