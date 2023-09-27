package com.test.movieapp.api
import MovieDetailResponse
import MovieGenresResponse
import MovieListResponse
import MovieReviewsResponse
import MovieVideosResponse

import io.reactivex.Observable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ApiService {

    companion object {
        private const val DOMAIN_MOVIE = "https://api.themoviedb.org"
        private const val API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI0NDk4NDI2MjQ1ZWNlNTAzZjgxMjBiMjViOWQwNDIwZSIsInN1YiI6IjViZGZlMmQxMGUwYTI2MDU4ZjAzNzY0NCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.a0w6SY7sW1jyzoVCkle6QW9bJ6a9NRhoTbhwaE_Ukto"

        fun create(): ApiService{
            val okHttpClientBuilder = OkHttpClient.Builder()

            var interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpClientBuilder.addInterceptor(interceptor)

            val authInterceptor = Interceptor { chain ->
                val originalRequest: Request = chain.request()
                val newRequest: Request = originalRequest.newBuilder()
                    .header("accept", "application/json")
                    .header("Authorization", "Bearer $API_KEY")
                    .build()
                chain.proceed(newRequest)
            }
            okHttpClientBuilder.addInterceptor(authInterceptor)

            val client = okHttpClientBuilder
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(DOMAIN_MOVIE)
                .client(client)
                .build()

            return retrofit.create(ApiService::class.java)
        }
    }

    @GET("3/discover/movie")
    fun getMovieList(@Query("page") id: Int, @Query("with_genres") genres: String) : Observable<MovieListResponse>

    @GET("3/movie/{movie_id}")
    fun getMovieDetail(@Path("movie_id") id: Int) : Observable<MovieDetailResponse>

    @GET("3/movie/{movie_id}/videos")
    fun getMovieVideos(@Path("movie_id") id: Int) : Observable<MovieVideosResponse>

    @GET("3/movie/{movie_id}/reviews")
    fun getMovieReviewList(@Path("movie_id") id: Int, @Query("page") page: Int) : Observable<MovieReviewsResponse>

    @GET("3/genre/movie/list")
    fun getGenres() : Observable<MovieGenresResponse>
}