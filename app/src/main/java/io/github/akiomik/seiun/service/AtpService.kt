package io.github.akiomik.seiun.service

import com.slack.eithernet.ApiResult
import com.slack.eithernet.DecodeErrorBody
import io.github.akiomik.seiun.model.*
import retrofit2.http.*

interface AtpService {
    @DecodeErrorBody
    @POST("com.atproto.account.create")
    suspend fun createAccount(
        @Body body: AccountCreateParam,
    ): ApiResult<Session, AtpError>

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
    ): ApiResult<CreateRecordResponse, AtpError>

    @DecodeErrorBody
    @POST("com.atproto.repo.createRecord")
    suspend fun repost(
        @Header("Authorization") authorization: String,
        @Body body: RepostParam
    ): ApiResult<CreateRecordResponse, AtpError>

    @DecodeErrorBody
    @POST("com.atproto.repo.deleteRecord")
    suspend fun deleteRecord(
        @Header("Authorization") authorization: String,
        @Body body: DeleteRecordParam
    ) // TODO: Handle empty response with EitherNet

    @DecodeErrorBody
    @POST("app.bsky.feed.setVote")
    suspend fun upvote(
        @Header("Authorization") authorization: String,
        @Body body: SetVoteParam
    ): ApiResult<SetVoteResponse, AtpError>

    @DecodeErrorBody
    @GET("app.bsky.notification.list")
    suspend fun listNotifications(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int? = null,
        @Query("before") before: String? = null,
    ): ApiResult<NotificationList, AtpError>
}