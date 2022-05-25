package com.example.mymarvelapp

import com.example.mymarvelapp.network.entity.CharacterListEntity
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MarvelAPI {
    @GET("/v1/public/characters")
    suspend fun getCharacters(
        @Query("ts") timeStamp: String,
        @Query("apikey") apiKey: String,
        @Query("hash") hash: String): Response<CharacterListEntity>
}