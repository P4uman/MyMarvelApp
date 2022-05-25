package com.example.mymarvelapp.network.interactor

import com.example.mymarvelapp.BuildConfig
import com.example.mymarvelapp.network.entity.CharacterDataListEntity
import com.example.mymarvelapp.MarvelAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FetchCharactersInteractor {

    private val retrofit: Retrofit
    private val marvelAPI: MarvelAPI

    init {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        marvelAPI = retrofit.create(MarvelAPI::class.java)
    }

    // hashData parameter is md5 of timeStamp + privateKey + publicKey
    suspend fun fetchCharacters(
        timeStamp: String,
        hashData: String,
        onSuccess: (CharacterDataListEntity) -> Unit,
        onComplete: () -> Unit,
        onFailure: () -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val response = marvelAPI.getCharacters(
                    timeStamp = timeStamp,
                    apiKey = BuildConfig.PUBLIC_KEY,
                    hash = hashData
                )
                if (response.isSuccessful && response.body()?.data != null) {
                    onSuccess.invoke(response.body()?.data!!)
                } else {
                    onFailure.invoke()
                }
            } catch (t: Throwable) {
                onFailure.invoke()
                t.printStackTrace()
            } finally {
                onComplete.invoke()
            }
        }
    }
}