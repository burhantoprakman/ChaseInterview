package com.chase.interview.di

import com.chase.interview.data.api.RetrofitBuilder
import com.chase.interview.data.api.WeatherApi
import com.chase.interview.data.repository.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        return RetrofitBuilder.retrofit
    }

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): WeatherApi {
        return retrofit.create(WeatherApi::class.java)
    }

    @Singleton
    @Provides
    fun provideRepository(weatherApi: WeatherApi): WeatherRepository{
        return WeatherRepository(weatherApi)
    }
}