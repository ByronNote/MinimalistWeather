package cn.byronlab.weather.data.openmeteo

import java.util.Locale

internal object OpenMeteoCityCatalog {

    val defaultCity: OpenMeteoCity
        get() = popularCities.first()

    val defaultCityId: String
        get() = OpenMeteoCityCodec.encode(defaultCity)

    val popularCities: List<OpenMeteoCity> = listOf(
        OpenMeteoCity("1816670", "北京", "Beijing", "中国", "北京", 39.9042, 116.4074, "Asia/Shanghai", 21893095),
        OpenMeteoCity("1796236", "上海", "Shanghai", "中国", "上海", 31.2304, 121.4737, "Asia/Shanghai", 24870895),
        OpenMeteoCity("1809858", "广州", "Guangzhou", "中国", "广东", 23.1291, 113.2644, "Asia/Shanghai", 18676605),
        OpenMeteoCity("1795565", "深圳", "Shenzhen", "中国", "广东", 22.5431, 114.0579, "Asia/Shanghai", 17661900),
        OpenMeteoCity("1819729", "香港", "Hong Kong", "中国", "香港", 22.3193, 114.1694, "Asia/Hong_Kong", 7496981),
        OpenMeteoCity("1668341", "台北", "Taipei", "中国", "台湾", 25.0330, 121.5654, "Asia/Taipei", 2646204),
        OpenMeteoCity("1850147", "东京", "Tokyo", "日本", "东京都", 35.6762, 139.6503, "Asia/Tokyo", 13960000),
        OpenMeteoCity("1835848", "首尔", "Seoul", "韩国", "首尔", 37.5665, 126.9780, "Asia/Seoul", 9733509),
        OpenMeteoCity("1880252", "新加坡", "Singapore", "新加坡", "", 1.3521, 103.8198, "Asia/Singapore", 5638700),
        OpenMeteoCity("1609350", "曼谷", "Bangkok", "泰国", "曼谷", 13.7563, 100.5018, "Asia/Bangkok", 10539000),
        OpenMeteoCity("5128581", "纽约", "New York", "美国", "纽约州", 40.7128, -74.0060, "America/New_York", 8804190),
        OpenMeteoCity("5368361", "洛杉矶", "Los Angeles", "美国", "加利福尼亚州", 34.0522, -118.2437, "America/Los_Angeles", 3898747),
        OpenMeteoCity("5391959", "旧金山", "San Francisco", "美国", "加利福尼亚州", 37.7749, -122.4194, "America/Los_Angeles", 873965),
        OpenMeteoCity("2643743", "伦敦", "London", "英国", "英格兰", 51.5072, -0.1276, "Europe/London", 8982000),
        OpenMeteoCity("2988507", "巴黎", "Paris", "法国", "法兰西岛", 48.8566, 2.3522, "Europe/Paris", 2161000),
        OpenMeteoCity("2950159", "柏林", "Berlin", "德国", "柏林", 52.5200, 13.4050, "Europe/Berlin", 3664088),
        OpenMeteoCity("3169070", "罗马", "Rome", "意大利", "拉齐奥", 41.9028, 12.4964, "Europe/Rome", 2873000),
        OpenMeteoCity("3117735", "马德里", "Madrid", "西班牙", "马德里自治区", 40.4168, -3.7038, "Europe/Madrid", 3223000),
        OpenMeteoCity("2147714", "悉尼", "Sydney", "澳大利亚", "新南威尔士州", -33.8688, 151.2093, "Australia/Sydney", 5312000),
        OpenMeteoCity("2158177", "墨尔本", "Melbourne", "澳大利亚", "维多利亚州", -37.8136, 144.9631, "Australia/Melbourne", 5078000),
        OpenMeteoCity("6167865", "多伦多", "Toronto", "加拿大", "安大略省", 43.6532, -79.3832, "America/Toronto", 2794356),
        OpenMeteoCity("6173331", "温哥华", "Vancouver", "加拿大", "不列颠哥伦比亚省", 49.2827, -123.1207, "America/Vancouver", 662248),
        OpenMeteoCity("292223", "迪拜", "Dubai", "阿联酋", "迪拜", 25.2048, 55.2708, "Asia/Dubai", 3331420),
        OpenMeteoCity("745044", "伊斯坦布尔", "Istanbul", "土耳其", "伊斯坦布尔", 41.0082, 28.9784, "Europe/Istanbul", 15462452),
        OpenMeteoCity("524901", "莫斯科", "Moscow", "俄罗斯", "莫斯科", 55.7558, 37.6173, "Europe/Moscow", 12506468),
        OpenMeteoCity("360630", "开罗", "Cairo", "埃及", "开罗省", 30.0444, 31.2357, "Africa/Cairo", 10100000),
        OpenMeteoCity("993800", "约翰内斯堡", "Johannesburg", "南非", "豪登省", -26.2041, 28.0473, "Africa/Johannesburg", 5635127),
        OpenMeteoCity("3451190", "里约热内卢", "Rio de Janeiro", "巴西", "里约热内卢州", -22.9068, -43.1729, "America/Sao_Paulo", 6748000),
        OpenMeteoCity("3448439", "圣保罗", "Sao Paulo", "巴西", "圣保罗州", -23.5558, -46.6396, "America/Sao_Paulo", 12330000),
        OpenMeteoCity("3530597", "墨西哥城", "Mexico City", "墨西哥", "墨西哥城", 19.4326, -99.1332, "America/Mexico_City", 9209944),
    )

    fun resolve(cityId: String): OpenMeteoCity? {
        val decoded = OpenMeteoCityCodec.decode(cityId)
        if (decoded != null) {
            return decoded
        }
        return popularCities.firstOrNull { OpenMeteoCityCodec.encode(it) == cityId }
    }

    fun searchLocal(query: String): List<OpenMeteoCity> {
        val normalizedQuery = query.trim().lowercase(Locale.ROOT)
        if (normalizedQuery.isBlank()) {
            return popularCities
        }
        return popularCities.filter { city ->
            listOf(city.name, city.nameEn, city.country, city.admin1)
                .any { it.lowercase(Locale.ROOT).contains(normalizedQuery) }
        }
    }
}
