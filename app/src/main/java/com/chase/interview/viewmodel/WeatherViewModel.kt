package com.chase.interview.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chase.interview.data.repository.ResultStatus
import com.chase.interview.data.repository.WeatherRepository
import com.chase.interview.model.WeatherResponse
import com.chase.interview.util.DataStoreManager
import com.chase.interview.util.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationProvider: LocationProvider,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _weatherState = MutableStateFlow<ResultStatus<WeatherResponse?>?>(null)
    val weatherState: StateFlow<ResultStatus<WeatherResponse?>?> = _weatherState

    //Cache last searched city information to avoid recomposition or configuration changes
    private val _lastSearchedCity = dataStoreManager.lastSearchedCity.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )


    fun getWeatherByCity(city: String) {

        viewModelScope.launch {
            try {
                _weatherState.value = ResultStatus.Loading
                val result = weatherRepository.getWeatherByCity(city).first()
                _weatherState.value = ResultStatus.Success(result)

                saveSearchedCity(city)
            } catch (e: Exception) {
                handleWeatherError(e)
            }

        }
    }

    fun getWeatherByLocation() {
        viewModelScope.launch {
            try {
                _weatherState.value = ResultStatus.Loading
                val location = locationProvider.getLastKnownLocation()
                if (location?.latitude != null) {
                    val result = weatherRepository.getWeatherByCoordinates(
                        location.latitude,
                        location.longitude
                    ).first()
                    _weatherState.value = ResultStatus.Success(result)

                } else {
                    _lastSearchedCity.value?.let { getWeatherByCity(it) }
                }

            } catch (e: Exception) {
                handleWeatherError(e)
            }
        }
    }


    fun saveSearchedCity(city: String) {
        viewModelScope.launch {
            dataStoreManager.saveLastSearchedCity(city) // Save to DataStore
        }
    }

    private fun handleWeatherError(e: Exception) {
        _weatherState.value = ResultStatus.Error("Failed to fetch weather data: ${e.message}")
    }

}