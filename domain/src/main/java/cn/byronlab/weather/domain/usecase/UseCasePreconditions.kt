package cn.byronlab.weather.domain.usecase

internal object UseCasePreconditions {

    fun isBlank(value: String?): Boolean = value.isNullOrBlank()

    fun trim(value: String?): String = value?.trim().orEmpty()
}
