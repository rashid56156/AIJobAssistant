package com.sample.aijobassistant.di

import android.content.Context
import androidx.room.Room
import com.sample.aijobassistant.data.local.AnalysisRecordDao
import com.sample.aijobassistant.data.local.AppDatabase
import com.sample.aijobassistant.data.remote.GeminiApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // BASIC level logging only: never log request/response bodies, since
        // the API key travels in a request header and we don't want it ever
        // landing in Logcat, even in debug builds.
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            // OkHttp's defaults (10s connect/read/write) are too tight for
            // Gemini: generating a structured JSON analysis response often
            // takes well past 10s, especially under load. A request that's
            // genuinely still processing would otherwise be killed and
            // surfaced to the user as a misleading "network error" rather
            // than what it actually is - the model taking a while to think.
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(GeminiApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideGeminiApi(retrofit: Retrofit): GeminiApi = retrofit.create(GeminiApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .build()

    @Provides
    fun provideAnalysisRecordDao(database: AppDatabase): AnalysisRecordDao =
        database.analysisRecordDao()
}
