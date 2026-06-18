package com.app.apuntes.data.remote.retrofit

import com.app.apuntes.data.remote.retrofit.dto.WikipediaSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    /**
     * Busca artículos en Wikipedia en español usando MediaWiki Action API
     * y obtiene sus resúmenes en texto plano en una sola consulta.
     */
    @GET("api.php")
    suspend fun buscarArticulos(
        @Query("action")    action: String = "query",
        @Query("format")    format: String = "json",
        @Query("generator") generator: String = "search",
        @Query("gsrsearch") query: String,
        @Query("gsrlimit")  limit: Int = 3,
        @Query("prop")      prop: String = "extracts",
        @Query("exintro")   exintro: Int = 1,
        @Query("explaintext") explaintext: Int = 1,
        @Query("exsentences") exsentences: Int = 2
    ): Response<WikipediaSearchResponse>
}
