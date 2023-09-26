package com.ugikpoenya.deepai.api

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface ApiService {
    @POST
    fun generateImages(@Url url: String, @Header("api-key") apiKey: String?, @Body request: MultipartBody?): Call<BodyResponse>
}