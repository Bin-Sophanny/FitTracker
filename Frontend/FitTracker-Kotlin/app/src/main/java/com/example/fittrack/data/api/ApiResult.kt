package com.example.fittrack.data.api

/**
 * API Response wrapper to handle success/error states
 */
sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val exception: Exception? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

/**
 * Extension function to safely handle API responses
 */
suspend fun <T> safeApiCall(
    apiCall: suspend () -> retrofit2.Response<T>
): ApiResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            response.body()?.let {
                ApiResult.Success(it)
            } ?: ApiResult.Error("Empty response body")
        } else {
            ApiResult.Error("API Error: ${response.code()} - ${response.message()}")
        }
    } catch (e: Exception) {
        ApiResult.Error("Network error: ${e.localizedMessage}", e)
    }
}

