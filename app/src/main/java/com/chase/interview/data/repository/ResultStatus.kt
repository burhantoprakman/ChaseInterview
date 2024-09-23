package com.chase.interview.data.repository

/*
 *Different states for response status
*/
sealed class ResultStatus<out T> {
    data class Success<out T>(val data: T) : ResultStatus<T>()
    data class Error(val message: String) : ResultStatus<Nothing>()
    data object Loading : ResultStatus<Nothing>()
}