package com.example.catpaw.services

import com.example.catpaw.models.*
import retrofit2.Call

import retrofit2.http.*

interface AtpService {
    @POST("com.atproto.session.create")
    fun login(
        @Body body: LoginParam,
    ): Call<Session>

    @GET("com.atproto.repo.listRecords")
    fun listRecords(
        @Query("user") user: String,
        @Query("collection") collection: String,
        @Query("limit") limit: Int? = null,
        @Query("before") before: String? = null,
        @Query("after") after: String? = null,
        @Query("reverse") reverse: Boolean? = null
    ): Call<ListRecords>

    @GET("app.bsky.feed.getTimeline")
    fun getTimeline(
        @Header("Authorization") authorization: String,
        @Query("algorithm") algorithm: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("before") before: String? = null,
    ): Call<Timeline>
}