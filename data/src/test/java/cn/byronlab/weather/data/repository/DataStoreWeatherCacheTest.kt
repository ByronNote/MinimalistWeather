package cn.byronlab.weather.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreWeatherCacheTest {

    private val dispatcher = StandardTestDispatcher()

    @Test
    fun writeAndReadReturnsPayloadForMatchingCity() = runTest(dispatcher) {
        val cache = DataStoreWeatherCache(FakePreferencesDataStore(), dispatcher)

        cache.write(
            cityId = "beijing",
            forecastPayload = "forecast",
            airQualityPayload = "air-quality",
            cachedAtMillis = 123L,
        )

        assertEquals(
            CachedWeatherPayload("beijing", "forecast", "air-quality", 123L),
            cache.read("beijing"),
        )
    }

    @Test
    fun cacheKeepsCitiesIndependentAndCanClearOneCity() = runTest(dispatcher) {
        val cache = DataStoreWeatherCache(FakePreferencesDataStore(), dispatcher)
        cache.write("beijing", "forecast", "air-quality", 123L)
        cache.write("london", "london-forecast", "london-air-quality", 456L)

        assertEquals(
            CachedWeatherPayload("beijing", "forecast", "air-quality", 123L),
            cache.read("beijing"),
        )
        assertEquals(
            CachedWeatherPayload("london", "london-forecast", "london-air-quality", 456L),
            cache.read("london"),
        )

        cache.clear("beijing")

        assertNull(cache.read("beijing"))
        assertEquals(
            CachedWeatherPayload("london", "london-forecast", "london-air-quality", 456L),
            cache.read("london"),
        )
    }

    private class FakePreferencesDataStore : DataStore<Preferences> {
        private val state = MutableStateFlow(emptyPreferences())

        override val data: Flow<Preferences> = state

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            return transform(state.value).also { state.value = it }
        }
    }
}
