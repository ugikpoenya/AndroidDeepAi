package com.ugikpoenya.deepai

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.ugikpoenya.deepai.api.ApiClient
import com.ugikpoenya.deepai.api.ApiService
import com.ugikpoenya.deepai.api.BodyRequest
import com.ugikpoenya.deepai.api.BodyResponse
import com.ugikpoenya.deepai.api.ErrorResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class DeepAi(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(context.packageName, 0)
    var DEEPAI_API_KEY: String
        get() = prefs.getString("DEEPAI_API_KEY", "").toString()
        set(value) = prefs.edit().putString("DEEPAI_API_KEY", value).apply()

    private fun capitalize(str: String): String {
        return str.trim().split("\\s+".toRegex()).joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }.trim()
    }

    fun getStyles(files: ArrayList<String>?): ArrayList<StyleModel> {
        val styleModelArrayList = ArrayList<StyleModel>()
        files?.forEach {
            val image = it
            var url = it.substring(it.lastIndexOf('/') + 1).lowercase()
            url = url.substring(url.lastIndexOf('_') + 1)
            url = url.substring(0, url.lastIndexOf('.'))
            url = url.replace("-thumb", "").trim()
            var title = url.replace("-", " ").replace("generator", "")
            title = capitalize(title)
            styleModelArrayList.add(StyleModel(title, image, url))
        }
        return styleModelArrayList
    }

    fun generateImages(url: String, bodyRequest: BodyRequest?, function: (response: BodyResponse?, error: ErrorResponse?) -> (Unit)) {
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        builder.addFormDataPart("text", bodyRequest?.text.toString())
        if (bodyRequest?.grid_size != null && bodyRequest.grid_size!!.isNotEmpty()) builder.addFormDataPart("grid_size", bodyRequest.grid_size.toString())
        if (bodyRequest?.image_generator_version != null && bodyRequest.image_generator_version!!.isNotEmpty()) builder.addFormDataPart("image_generator_version", bodyRequest?.image_generator_version.toString())

        if (bodyRequest?.image != null && bodyRequest.image!!.isNotEmpty()) {
            val image = File(bodyRequest.image.toString())
            builder.addFormDataPart("image", image.name, image.asRequestBody("multipart/form-data".toMediaTypeOrNull()))
        }

        if (bodyRequest?.image1 != null && bodyRequest.image1!!.isNotEmpty()) {
            val image1 = File(bodyRequest.image1.toString())
            builder.addFormDataPart("image1", image1.name, image1.asRequestBody("multipart/form-data".toMediaTypeOrNull()))
        }

        if (bodyRequest?.image2 != null && bodyRequest.image2!!.isNotEmpty()) {
            val image2 = File(bodyRequest.image2.toString())
            builder.addFormDataPart("image2", image2.name, image2.asRequestBody("multipart/form-data".toMediaTypeOrNull()))
        }

        val requestBody = builder.build()
        val apiService = ApiClient.client!!.create(ApiService::class.java)
        val call: Call<BodyResponse> = apiService.generateImages(url, DEEPAI_API_KEY, requestBody)
        call.enqueue(object : Callback<BodyResponse> {
            override fun onResponse(call: Call<BodyResponse>, response: Response<BodyResponse>) {
                if (response.isSuccessful) {
                    function(response.body(), null)
                } else {
                    val errorResponse = Gson().fromJson(response.errorBody()!!.charStream(), ErrorResponse::class.java)
                    Log.d("LOG", "errorBody " + errorResponse.error?.message)
                    function(null, errorResponse)
                }
            }

            override fun onFailure(call: Call<BodyResponse>, t: Throwable) {
                Log.d("LOG", "onFailure " + t.localizedMessage)
                function(null, null)
            }
        })
    }

}