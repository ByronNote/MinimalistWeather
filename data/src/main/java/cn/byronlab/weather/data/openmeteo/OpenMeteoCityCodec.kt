package cn.byronlab.weather.data.openmeteo

import cn.byronlab.weather.domain.model.City
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Locale

internal object OpenMeteoCityCodec {

    private const val PREFIX = "openmeteo"
    private const val ENCODING = "UTF-8"

    fun encode(city: OpenMeteoCity): String {
        return listOf(
            PREFIX,
            city.geonameId,
            coordinate(city.latitude),
            coordinate(city.longitude),
            city.timezone,
            city.name,
            city.nameEn,
            city.country,
            city.admin1,
        ).joinToString(separator = "|", transform = ::encodePart)
    }

    fun decode(cityId: String?): OpenMeteoCity? {
        if (cityId.isNullOrBlank()) {
            return null
        }
        return try {
            decodeValue(cityId)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun decodeValue(cityId: String): OpenMeteoCity? {
        val parts = cityId.split("|")
        if (parts.size < 9 || decodePart(parts[0]) != PREFIX) {
            return null
        }
        val latitude = decodePart(parts[2]).toDoubleOrNull() ?: return null
        val longitude = decodePart(parts[3]).toDoubleOrNull() ?: return null
        return OpenMeteoCity(
            geonameId = decodePart(parts[1]),
            latitude = latitude,
            longitude = longitude,
            timezone = decodePart(parts[4]).ifBlank { "auto" },
            name = decodePart(parts[5]),
            nameEn = decodePart(parts[6]),
            country = decodePart(parts[7]),
            admin1 = decodePart(parts[8]),
        )
    }

    fun toDomainCity(city: OpenMeteoCity): City {
        val parent = listOf(city.admin1, city.country)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(" · ")
        return City(
            cityId = encode(city),
            name = city.name,
            nameEn = city.nameEn,
            root = city.country,
            parent = parent,
            longitude = coordinate(city.longitude),
            latitude = coordinate(city.latitude),
            uniqueId = uniqueId(city),
        )
    }

    fun uniqueId(cityId: String): String {
        return decode(cityId)?.let(::uniqueId) ?: "legacy:${cityId.trim()}"
    }

    fun uniqueId(city: OpenMeteoCity): String {
        val name = normalizePlacePart(city.name).ifBlank {
            normalizePlacePart(city.nameEn)
        }
        val country = normalizeIdentityPart(city.country)
        val admin = normalizePlacePart(city.admin1).takeUnless { it == name }.orEmpty()
        if (name.isNotBlank()) {
            return "place:$country:$admin:$name"
        }
        if (city.geonameId.isNotBlank() && city.geonameId != DEVICE_LOCATION_ID) {
            return "geoname:${city.geonameId.trim()}"
        }
        return "coordinates:${coordinate(city.latitude)}:${coordinate(city.longitude)}"
    }

    private fun encodePart(value: String): String {
        return URLEncoder.encode(value, ENCODING)
    }

    private fun decodePart(value: String): String {
        return URLDecoder.decode(value, ENCODING)
    }

    private fun coordinate(value: Double): String {
        return String.format(Locale.US, "%.4f", value)
    }

    private fun normalizePlacePart(value: String): String {
        var normalized = normalizeIdentityPart(value)
        ADMINISTRATIVE_SUFFIXES.firstOrNull(normalized::endsWith)?.let { suffix ->
            normalized = normalized.removeSuffix(suffix)
        }
        return normalized
    }

    private fun normalizeIdentityPart(value: String): String {
        return value
            .trim()
            .lowercase(Locale.ROOT)
            .replace(IDENTITY_SEPARATOR_REGEX, "")
    }

    private val IDENTITY_SEPARATOR_REGEX = Regex("[\\s·•,，.。'’_\\-/]+")
    private val ADMINISTRATIVE_SUFFIXES = listOf(
        "特别行政区",
        "维吾尔自治区",
        "壮族自治区",
        "回族自治区",
        "自治区",
        "自治州",
        "地区",
        "城市",
        "city",
        "省",
        "州",
        "市",
        "县",
        "区",
        "盟",
    )
    private const val DEVICE_LOCATION_ID = "device-location"
}
