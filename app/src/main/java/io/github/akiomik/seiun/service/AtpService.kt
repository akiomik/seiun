package io.github.akiomik.seiun.service

import com.slack.eithernet.ApiResult
import com.slack.eithernet.DecodeErrorBody
import io.github.akiomik.seiun.model.*
import retrofit2.Call

import retrofit2.http.*

interface AtpService {
    @DecodeErrorBody
    @POST("com.atproto.session.create")
    suspend fun login(
        @Body body: LoginParam,
    ): ApiResult<Session, AtpError>

    @DecodeErrorBody
    @POST("com.atproto.session.refresh")
    suspend fun refreshSession(
        @Header("Authorization") authorization: String,
    ): ApiResult<Session, AtpError>

    @DecodeErrorBody
    @GET("com.atproto.repo.listRecords")
    suspend fun listRecords(
        @Query("user") user: String,
        @Query("collection") collection: String,
        @Query("limit") limit: Int? = null,
        @Query("before") before: String? = null,
        @Query("after") after: String? = null,
        @Query("reverse") reverse: Boolean? = null
    ): ApiResult<ListRecords, AtpError>

    @DecodeErrorBody
    @GET("app.bsky.feed.getTimeline")
    suspend fun getTimeline(
        @Header("Authorization") authorization: String,
        @Query("algorithm") algorithm: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("before") before: String? = null,
    ): ApiResult<Timeline, AtpError>

    @DecodeErrorBody
    @POST("com.atproto.repo.createRecord")
    suspend fun createPost(
        @Header("Authorization") authorization: String,
        @Body body: CreatePostParam
    ): ApiResult<CreatePostResponse, AtpError>
}