package cn.byronlab.weather.domain.result

data class DomainError(
    val type: Type,
    val message: String? = null,
    val cause: Throwable? = null,
) {
    enum class Type {
        INVALID_INPUT,
        MISSING_CURRENT_CITY,
        NOT_FOUND,
        NETWORK,
        STORAGE,
        LOCATION_PERMISSION_REQUIRED,
        LOCATION_UNAVAILABLE,
        UNKNOWN,
    }

    companion object {
        fun invalidInput(message: String): DomainError =
            DomainError(Type.INVALID_INPUT, message)

        fun missingCurrentCity(): DomainError =
            DomainError(Type.MISSING_CURRENT_CITY, "Current city is not configured.")

        fun unknown(cause: Throwable?): DomainError =
            DomainError(Type.UNKNOWN, cause?.message ?: "Unknown error.", cause)
    }
}
