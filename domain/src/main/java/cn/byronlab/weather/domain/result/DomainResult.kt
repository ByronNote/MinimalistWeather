package cn.byronlab.weather.domain.result

sealed interface DomainResult<out T> {

    data class Success<out T>(val data: T) : DomainResult<T>

    data class Failure(val error: DomainError) : DomainResult<Nothing>

    companion object {
        fun <T> success(data: T): DomainResult<T> = Success(data)

        fun <T> failure(error: DomainError): DomainResult<T> = Failure(error)
    }
}
