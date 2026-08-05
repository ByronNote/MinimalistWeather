package cn.byronlab.weather.data.openmeteo

import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class OpenMeteoClientTest {

    @Test
    fun getForecast_requestsRealHourlyWeatherFields() {
        var requestedUrl: HttpUrl? = null
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                requestedUrl = chain.request().url
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("{}".toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()

        OpenMeteoClient(okHttpClient).getForecast(OpenMeteoCityCatalog.defaultCity)

        assertEquals(
            "temperature_2m,weather_code,is_day",
            requestedUrl?.queryParameter("hourly"),
        )
        assertEquals("auto", requestedUrl?.queryParameter("timezone"))
        assertEquals("7", requestedUrl?.queryParameter("forecast_days"))
    }
}
