package com.example.memorizy.data.source.network

import com.example.memorizy.data.source.network.dto.AuthRequest
import com.example.memorizy.data.source.network.dto.AuthResponse
import com.example.memorizy.data.source.network.dto.CardDto
import com.example.memorizy.data.source.network.dto.StudySetDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Интерфейс Retrofit
// Endpoints для операций с сервером

interface MemorizyApiService{

    // AuthController
    @POST("api/auth/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    // StudySetController
    @GET("api/sets")
    suspend fun getAllSets(@Header("Authorization") token: String): List<StudySetDto>

    @POST("api/sets")
    suspend fun createSet(
        @Header("Authorization") token: String,
        @Body dto: StudySetDto
    ): StudySetDto

    @PUT("api/sets/{id}")
    suspend fun updateSet(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body dto: StudySetDto
    ): StudySetDto

    @DELETE("api/sets/{id}")
    suspend fun deleteSet(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    )

    // CardController
    @GET("api/cards/by-set/{setId}")
    suspend fun getCardsBySet(
        @Header("Authorization") token: String,
        @Path("setId") setId: Long
    ): List<CardDto>

    @POST("api/cards")
    suspend fun createCard(
        @Header("Authorization") token: String,
        @Body dto: CardDto
    ): CardDto

    @PUT("api/cards/{id}")
    suspend fun updateCard(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body dto: CardDto
    ): CardDto

    @DELETE("api/cards/{id}")
    suspend fun deleteCard(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    )
}