package com.example.memorizy.di

import com.example.memorizy.data.source.network.MemorizyApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.Provides
import jakarta.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

// DI для Retrofit

@Module // Инструкция как создавать объекты
@InstallIn(SingletonComponent::class)
object NetworkDataModule {

    // URL сервера
    private const val BASE_URL = "http://192.168.1.73:8080/"

    // Учим создавать OkHttpClient - передатчик Http запросов
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {    // Смотреть логи
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    // Учим создавать MemorizyApiService
    @Provides
    @Singleton
    fun provideMemorizyApiService(client: OkHttpClient): MemorizyApiService {
        val contentType = "application/json".toMediaType()  // Общаемся используя JSON

        val json = Json {
            ignoreUnknownKeys = true    // Игнорируем неизвестные поля от сервера
            encodeDefaults = true   // Отправляем все поля, даже если равны default
        }

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            // Подключаем Kotlin Serialization
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(MemorizyApiService::class.java) // Генерация кода для HTTP запросов
    }
}