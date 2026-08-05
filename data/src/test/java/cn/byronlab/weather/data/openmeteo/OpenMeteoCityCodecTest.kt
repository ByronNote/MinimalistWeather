package cn.byronlab.weather.data.openmeteo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenMeteoCityCodecTest {

    @Test
    fun encodeDecode_roundTripPreservesCityFields() {
        val city = OpenMeteoCity(
            geonameId = "1816670",
            name = "北京",
            nameEn = "Beijing",
            country = "中国",
            admin1 = "北京",
            latitude = 39.9042,
            longitude = 116.4074,
            timezone = "Asia/Shanghai",
            population = 21893095,
        )

        val decoded = OpenMeteoCityCodec.decode(OpenMeteoCityCodec.encode(city))

        assertNotNull(decoded)
        assertEquals(city.geonameId, decoded?.geonameId)
        assertEquals(city.name, decoded?.name)
        assertEquals(city.nameEn, decoded?.nameEn)
        assertEquals(city.country, decoded?.country)
        assertEquals(city.admin1, decoded?.admin1)
        assertEquals(city.latitude, decoded?.latitude)
        assertEquals(city.longitude, decoded?.longitude)
        assertEquals(city.timezone, decoded?.timezone)
    }

    @Test
    fun decode_returnsNullForMalformedEncoding() {
        val decoded = OpenMeteoCityCodec.decode(
            "openmeteo|1816670|39.9042|116.4074|Asia%2FShanghai|%ZZ|Beijing|China|Beijing",
        )

        assertEquals(null, decoded)
    }

    @Test
    fun searchLocal_matchesChineseAndEnglishNames() {
        val chineseResult = OpenMeteoCityCatalog.searchLocal("北京")
        val englishResult = OpenMeteoCityCatalog.searchLocal("new york")

        assertTrue(chineseResult.any { it.name == "北京" })
        assertTrue(englishResult.any { it.nameEn == "New York" })
    }
}
