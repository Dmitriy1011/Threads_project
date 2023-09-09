package ru.netology.nmedia.api

import com.google.firebase.BuildConfig
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.Auth.AppAuth
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {
    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999/api/slow/"
    }

    @Provides
    @Singleton
    fun provideLogging(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Singleton
    @Provides
    fun provideOkHttp( //здесь находится пул потоков
        appAuth: AppAuth
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        })
        .addInterceptor { chain ->
            appAuth.authStateFlow.value?.token?.let { token ->
                val newRequest = chain.request().newBuilder()
                    .addHeader(
                        "Authorization",
                        token
                    ) //Запросы на сервер связанные с изменениями должны быть с таким хедером
                    .build()
                return@addInterceptor chain.proceed(newRequest)
            }
            chain.proceed(chain.request())
        }
        .build()

    @Singleton
    @Provides
    fun provideRetrofit(
        client: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder()
                    .registerTypeAdapter(
                        LocalDateTime::class.java,
                        object : TypeAdapter<LocalDateTime>() {
                            override fun write(out: JsonWriter?, value: LocalDateTime?) {
                                value?.toEpochSecond(ZoneOffset.of(ZoneId.systemDefault().id))
                            }

                            override fun read(reader: JsonReader): LocalDateTime =
                                LocalDateTime.ofEpochSecond(
                                    reader.nextLong(),
                                    0,
                                    ZoneOffset.of(ZoneId.systemDefault().id)
                                )
                        })
                    .create()
            )
        )
        .client(client)
        .baseUrl(BASE_URL)
        .build()

    @Singleton
    @Provides
    fun provideApiService(
        retrofit: Retrofit
    ): PostApiService = retrofit.create<PostApiService>()
}