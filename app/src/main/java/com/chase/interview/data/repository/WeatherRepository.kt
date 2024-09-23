package com.chase.interview.data.repository

import com.chase.interview.constant.AppConstant.API_KEY
import com.chase.interview.constant.AppConstant.METRIC
import com.chase.interview.data.api.WeatherApi
import com.chase.interview.model.WeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val api: WeatherApi
) {
    suspend fun getWeatherByCity(city: String): Flow<WeatherResponse> = flow {
        val response = api.getWeatherByCity(city, API_KEY,METRIC)
        emit(response)
    }

    suspend fun getWeatherByCoordinates(
        lat: Double,
        lon: Double
    ): Flow<WeatherResponse> = flow {
        val response = api.getWeatherByCoordinates(lat, lon, API_KEY,METRIC)
        emit(response)
    }
}