package com.chase.interview.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.chase.interview.R
import com.chase.interview.data.repository.ResultStatus
import com.chase.interview.model.WeatherResponse
import com.chase.interview.viewmodel.WeatherViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherScreen() {
    //I would carry viewmodel creation into navigation compose for prevent leak.
    val viewModel: WeatherViewModel = hiltViewModel()
    val weatherState by viewModel.weatherState.collectAsState()
    var weather by remember { mutableStateOf<WeatherResponse?>(null) }
    var backgroundColor by remember { mutableStateOf(listOf(Color.Transparent,Color.Transparent)) }

    Box(modifier = Modifier.fillMaxSize()){
        when (weatherState) {
            is ResultStatus.Loading -> {
                CircularProgressIndicator()
            }

            is ResultStatus.Success -> {
                val result = (weatherState as ResultStatus.Success<WeatherResponse?>).data
                weather = result
                weather?.let {
                    backgroundColor = getBackgroundColorForWeather(it.weather.firstOrNull()?.id ?: 800)
                }
            }

            is ResultStatus.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error loading weather", color = Color.Red)
                }
            }

            else -> {

            }
        }
    }

    //If I have a time, I would create a proper splash screen and handle permission from there.
    RequestLocationPermission(
        onGranted = {
            // Once permission is granted, request the weather
            viewModel.getWeatherByLocation()
        },
        onDenied = {
            // If I have a time ,I would handle permission denied case
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(backgroundColor)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SearchBar(viewModel,onSearch = { city -> viewModel.getWeatherByCity(city) })

        weather?.let {
           WeatherCard(
               location = it.name,
               temperature = it.main.temp.toInt().toString(),
               typeIcon = "https://openweathermap.org/img/wn/${it.weather.firstOrNull()?.icon}@2x.png",
               description = it.weather.firstOrNull()?.description ?: ""
           )
        }
    }
}

@Composable
fun SearchBar(viewModel: WeatherViewModel, onSearch: (String) -> Unit) {
    var searchText by remember { mutableStateOf("") }

    TextField(
        value = searchText,
        onValueChange = { searchText = it },
        label = { Text(stringResource(id = R.string.enter_city_name)) },
        modifier = Modifier.fillMaxWidth()
    )
    Button(onClick = {
        viewModel.saveSearchedCity(searchText)
        onSearch(searchText)
    }) {
        Text(stringResource(id = R.string.search))
    }
}

@Composable
fun getBackgroundColorForWeather(weatherId: Int): List<Color> {
    return when (weatherId) {
        in 200..232 -> listOf(colorResource(id = R.color.white), colorResource(id = R.color.black)) // Thunderstorm
        in 300..321 -> listOf(colorResource(id = R.color.lightBlue), colorResource(id = R.color.darkBlue)) // Drizzle
        in 500..531 -> listOf(colorResource(id = R.color.lightBlue), colorResource(id = R.color.darkBlue))  // Rain
        in 600..622 -> listOf(colorResource(id = R.color.white), colorResource(id = R.color.lightGray))  // Snow
        in 701..781 -> listOf(colorResource(id = R.color.lightGray), colorResource(id = R.color.darkGray))  // Atmosphere (Mist, Smoke, etc.)
        800 -> listOf(colorResource(id = R.color.lightOrange), colorResource(id = R.color.darkOrange))  // Clear sky
        in 801..804 -> listOf(colorResource(id = R.color.lightGray), colorResource(id = R.color.darkGray))  // Clouds
        else -> listOf(colorResource(id = R.color.white), colorResource(id = R.color.black)) // Default
    }
}

@Composable
fun WeatherIcon(iconUrl: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(iconUrl)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED) // Ensure disk caching is enabled
            .memoryCachePolicy(CachePolicy.ENABLED) // Ensure memory caching is enabled
            .build(),
        contentDescription = "Weather Icon",
        modifier = Modifier.size(120.dp),
        placeholder = painterResource(id = R.mipmap.ic_launcher_foreground), // Optional placeholder
        contentScale = ContentScale.Crop
    )
}

@Composable
fun WeatherCard(
    location: String,
    temperature: String,
    typeIcon : String,
    description : String
) {
    Box(
        modifier = Modifier.padding(top = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = location, fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            WeatherIcon(iconUrl = typeIcon)

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "$temperature°C", fontSize = 48.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = description, fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Light)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
private fun Prev_WeatherScreen() {
    WeatherScreen()
}