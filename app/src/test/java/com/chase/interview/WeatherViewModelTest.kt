package com.chase.interview

import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chase.interview.data.repository.ResultStatus
import com.chase.interview.data.repository.WeatherRepository
import com.chase.interview.model.MainInfo
import com.chase.interview.model.WeatherInfo
import com.chase.interview.model.WeatherResponse
import com.chase.interview.util.DataStoreManager
import com.chase.interview.util.LocationProvider
import com.chase.interview.viewmodel.WeatherViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var weatherRepository: WeatherRepository

    @Mock
    private lateinit var locationProvider: LocationProvider

    @Mock
    private lateinit var dataStoreManager: DataStoreManager

    private lateinit var weatherViewModel: WeatherViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockitoAnnotations.openMocks(this)

        dataStoreManager = mock(DataStoreManager::class.java)
        `when`(dataStoreManager.lastSearchedCity).thenReturn(flowOf("New York"))
        weatherRepository = mock(WeatherRepository::class.java)
        locationProvider = mock(LocationProvider::class.java)

        // Initialize ViewModel
        weatherViewModel = WeatherViewModel(weatherRepository, locationProvider, dataStoreManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
    }

    @Test
    fun `getWeatherByCity should emit success when repository returns data`() = runTest {
        // Given
        val weatherResponse = WeatherResponse(name = "Allen",
            MainInfo(299.0), weather = listOf(WeatherInfo(500,"Test","10n"))) // Sample response
        val cityName = "New York"
        `when`(weatherRepository.getWeatherByCity(cityName)).thenReturn(flow {
            emit(weatherResponse)
        })

        // When
        weatherViewModel.getWeatherByCity(cityName)

        // Simulate success state by collecting flow
        advanceUntilIdle()
        assertTrue(weatherViewModel.weatherState.value is ResultStatus.Success)
        val successState = weatherViewModel.weatherState.value as ResultStatus.Success<*>
        assertEquals(successState.data, weatherResponse)

        // Verify the repository call was made
        verify(weatherRepository).getWeatherByCity(cityName)
    }

    @Test
    fun `getWeatherByCity should emit error when repository throws exception`() = runTest {
        // Given
        val cityName = "London"
        val errorMessage ="Failed to fetch weather data: Test"
        `when`(weatherRepository.getWeatherByCity(cityName)).thenReturn(flow {
            throw Exception("Test")
        })

        // When
        weatherViewModel.getWeatherByCity(cityName)

        // Then
        advanceUntilIdle()

        val state = weatherViewModel.weatherState.value
        assertTrue(state is ResultStatus.Error)
        val errorState = state as ResultStatus.Error
        assertEquals(errorMessage, errorState.message)

        // Verify the repository call was made
        verify(weatherRepository).getWeatherByCity(cityName)
    }

    @Test
    fun `getWeatherByLocation should call getWeatherByCoordinates when location is available`() = runTest {
        // Given
        val weatherResponse = WeatherResponse(name = "Allen",
            MainInfo(299.0), weather = listOf(WeatherInfo(500,"Test","10n")))
        val location = mock(Location::class.java)
        `when`(location.latitude).thenReturn(40.7128)
        `when`(location.longitude).thenReturn(-74.0060)
        `when`(locationProvider.getLastKnownLocation()).thenReturn(location)

        `when`(weatherRepository.getWeatherByCoordinates(40.7128, -74.0060)).thenReturn(flow {
            emit(weatherResponse)
        })

        // When
        weatherViewModel.getWeatherByLocation()

        // Then
        advanceUntilIdle()
        val state = weatherViewModel.weatherState.value
        assertTrue(state is ResultStatus.Success)
        val successState = state as ResultStatus.Success<*>
        assertEquals(weatherResponse, successState.data)

        // Verify that locationProvider and weatherRepository were called
        verify(locationProvider).getLastKnownLocation()
        verify(weatherRepository).getWeatherByCoordinates(40.7128, -74.0060)
    }

    @Test
    fun `saveSearchedCity should call DataStoreManager to save city`() = runTest {
        // Given
        val cityName = "Los Angeles"

        // When
        weatherViewModel.saveSearchedCity(cityName)

        // Then
        advanceUntilIdle()
        verify(dataStoreManager).saveLastSearchedCity(cityName)
    }
}
