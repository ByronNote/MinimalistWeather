package cn.byronlab.weather.domain.result

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainResultTest {

    @Test
    fun successCarriesTypedData() {
        val result = DomainResult.success(listOf("北京", "上海"))

        assertTrue(result is DomainResult.Success)
        assertEquals(listOf("北京", "上海"), (result as DomainResult.Success).data)
    }

    @Test
    fun failureCarriesRequiredDomainError() {
        val error = DomainError.invalidInput("cityId must not be blank.")

        val result = DomainResult.failure<String>(error)

        assertTrue(result is DomainResult.Failure)
        assertSame(error, (result as DomainResult.Failure).error)
    }
}
