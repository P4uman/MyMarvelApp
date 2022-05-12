package com.example.mymarvelapp

import com.google.gson.annotations.SerializedName

data class CharacterListEntity(
    @SerializedName("code") val code: Int?,
    @SerializedName("data") val data: CharacterDataListEntity?
)

data class CharacterDataListEntity(
    @SerializedName("total") val total: Int?,
    @SerializedName("limit") val limit: Int?,
    @SerializedName("count") val count: Int?,
    @SerializedName("results") val characters: List<CharacterEntity>?
)

data class CharacterEntity(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,

)