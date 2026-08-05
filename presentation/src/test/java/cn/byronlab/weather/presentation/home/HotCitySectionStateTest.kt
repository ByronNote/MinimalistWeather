package cn.byronlab.weather.presentation.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HotCitySectionStateTest {

    @Test
    fun `featured city cards use the requested cities and order`() {
        assertEquals(
            listOf("北京", "上海", "伦敦", "巴黎", "柏林", "纽约", "洛杉矶", "东京", "首尔"),
            featuredCityNames(),
        )
    }

    @Test
    fun `every built in popular city has a landmark thumbnail`() {
        assertEquals(
            setOf(
                "北京", "上海", "广州", "深圳", "香港", "台北", "东京", "首尔", "新加坡", "曼谷",
                "纽约", "洛杉矶", "旧金山", "伦敦", "巴黎", "柏林", "罗马", "马德里", "悉尼", "墨尔本",
                "多伦多", "温哥华", "迪拜", "伊斯坦布尔", "莫斯科", "开罗", "约翰内斯堡", "里约热内卢",
                "圣保罗", "墨西哥城",
            ),
            hotCityThumbnailNames(),
        )
    }

    @Test
    fun `all continents are expanded by default`() {
        assertEquals(
            setOf("亚洲", "欧洲", "北美洲", "南美洲", "大洋洲", "非洲"),
            defaultExpandedHotCitySections(),
        )
    }

    @Test
    fun `sections can expand and collapse independently`() {
        val withEuropeCollapsed = toggleHotCitySection(
            expandedSections = defaultExpandedHotCitySections(),
            sectionTitle = "欧洲",
        )

        assertTrue("亚洲" in withEuropeCollapsed)
        assertFalse("欧洲" in withEuropeCollapsed)

        val withAsiaCollapsed = toggleHotCitySection(
            expandedSections = withEuropeCollapsed,
            sectionTitle = "亚洲",
        )

        assertFalse("亚洲" in withAsiaCollapsed)
        assertFalse("欧洲" in withAsiaCollapsed)
    }
}
