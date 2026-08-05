package cn.byronlab.weather.data.openmeteo

internal class OpenMeteoCityMapper {

    fun map(response: OpenMeteoGeocodingResponse): List<OpenMeteoCity> {
        return response.results.mapNotNull { city ->
            val id = city.id?.toString() ?: return@mapNotNull null
            val name = city.name?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val latitude = city.latitude ?: return@mapNotNull null
            val longitude = city.longitude ?: return@mapNotNull null
            OpenMeteoCity(
                geonameId = id,
                name = name,
                nameEn = name,
                country = city.country.orEmpty(),
                admin1 = city.admin1.orEmpty(),
                latitude = latitude,
                longitude = longitude,
                timezone = city.timezone.orEmpty().ifBlank { "auto" },
                population = city.population ?: 0,
            )
        }
    }
}
