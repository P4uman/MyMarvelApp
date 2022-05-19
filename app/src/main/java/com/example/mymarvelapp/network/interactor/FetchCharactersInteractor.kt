package com.example.mymarvelapp.network.interactor

import com.example.mymarvelapp.CharacterDataListEntity
import com.example.mymarvelapp.MarvelAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val URL = "https://gateway.marvel.com"
private const val PUBLIC_KEY = "56648a248c17e14c8b3cf59d293b99b8"
private const val PRIVATE_KEY = "b5860460de0f24c1191329c107b7f5c3109a1bb2"

class FetchCharactersInteractor {

    private val retrofit: Retrofit
    private val marvelAPI: MarvelAPI

    init {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        retrofit = Retrofit.Builder()
            .baseUrl(URL)
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
                    apiKey = PUBLIC_KEY,
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