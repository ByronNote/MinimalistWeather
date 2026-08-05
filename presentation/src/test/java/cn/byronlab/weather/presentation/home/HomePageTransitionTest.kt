package cn.byronlab.weather.presentation.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomePageTransitionTest {

    @Test
    fun `location picker opens vertically instead of moving forward`() {
        assertEquals(
            HomePageTransition.LocationPickerOpen,
            homePageTransition(HomePage.Home, HomePage.Cities),
        )
        assertEquals(
            HomePageTransition.LocationPickerClose,
            homePageTransition(HomePage.Cities, HomePage.Home),
        )
    }

    @Test
    fun `entering other deeper pages moves forward`() {
        assertEquals(
            HomePageTransition.Forward,
            homePageTransition(HomePage.Cities, HomePage.Search),
        )
    }

    @Test
    fun `returning to a parent page moves backward`() {
        assertEquals(
            HomePageTransition.Backward,
            homePageTransition(HomePage.HotCities, HomePage.Cities),
        )
        assertEquals(
            HomePageTransition.Backward,
            homePageTransition(HomePage.Settings, HomePage.Home),
        )
    }

    @Test
    fun `switching pages at the same depth crossfades`() {
        assertEquals(
            HomePageTransition.Crossfade,
            homePageTransition(HomePage.Cities, HomePage.Settings),
        )
    }

    @Test
    fun `only the weather home page uses light status bar icons`() {
        assertEquals(false, HomePage.Home.usesDarkStatusBarIcons())
        assertEquals(true, HomePage.Cities.usesDarkStatusBarIcons())
        assertEquals(true, HomePage.HotCities.usesDarkStatusBarIcons())
        assertEquals(true, HomePage.Search.usesDarkStatusBarIcons())
        assertEquals(true, HomePage.Settings.usesDarkStatusBarIcons())
    }
}
