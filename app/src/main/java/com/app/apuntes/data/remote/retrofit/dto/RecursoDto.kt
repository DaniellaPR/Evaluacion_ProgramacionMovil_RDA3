package com.app.apuntes.data.remote.retrofit.dto

import com.google.gson.annotations.SerializedName

data class WikipediaSearchResponse(
    @SerializedName("query") val query: WikipediaQueryDto? = null
)

data class WikipediaQueryDto(
    @SerializedName("pages") val pages: Map<String, WikipediaPageDto>? = null
)

data class WikipediaPageDto(
    @SerializedName("pageid") val pageid: Long = 0,
    @SerializedName("title")  val title: String = "",
    @SerializedName("extract") val extract: String? = null
)
