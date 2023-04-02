package io.github.akiomik.seiun.api

import com.slack.eithernet.ApiResult
import com.slack.eithernet.ApiResultCallAdapterFactory
import com.slack.eithernet.DecodeErrorBody
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.github.akiomik.seiun.model.AtpError
import io.github.akiomik.seiun.model.app.bsky.actor.ProfileView
import io.github.akiomik.seiun.model.app.bsky.feed.AuthorFeed
import io.github.akiomik.seiun.model.app.bsky.feed.Like
import io.github.akiomik.seiun.model.app.bsky.feed.Post
import io.github.akiomik.seiun.model.app.bsky.feed.Repost
import io.github.akiomik.seiun.model.app.bsky.feed.Timeline
import io.github.akiomik.seiun.model.app.bsky.graph.Follow
import io.github.akiomik.seiun.model.app.bsky.graph.Followers
import io.github.akiomik.seiun.model.app.bsky.graph.Follows
import io.github.akiomik.seiun.model.app.bsky.graph.MuteActorInput
import io.github.akiomik.seiun.model.app.bsky.graph.UnmuteActorInput
import io.github.akiomik.seiun.model.app.bsky.notification.UpdateNotificationSeenInput
import io.github.akiomik.seiun.model.com.atproto.moderation.CreateReportInput
import io.github.akiomik.seiun.model.com.atproto.moderation.CreateReportOutput
import io.github.akiomik.seiun.model.com.atproto.repo.CreateRecordInput
import io.github.akiomik.seiun.model.com.atproto.repo.CreateRecordOutput
import io.github.akiomik.seiun.model.com.atproto.repo.DeleteRecordInput
import io.github.akiomik.seiun.model.com.atproto.repo.UploadBlobOutput
import io.github.akiomik.seiun.model.com.atproto.server.CreateAccountInput
import io.github.akiomik.seiun.model.com.atproto.server.CreateAccountOutput
import io.github.akiomik.seiun.model.com.atproto.server.CreateSessionInput
import io.github.akiomik.seiun.model.com.atproto.server.CreateSessionOutput
import io.github.akiomik.seiun.model.com.atproto.server.RefreshSessionOutput
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.*

interface AtpService {
    companion object {
        private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .build()
        private val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        private val client = OkHttpClient.Builder().addInterceptor(logging).build()

        fun create(serviceProvider: String): AtpService {
            return Retrofit.Builder()
                .baseUrl("https://$serviceProvider/xrpc/")
                .client(client)
                .addConverterFactory(CustomApiResultConverterFactory)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(ApiResultCallAdapterFactory)
                .build()
                .create()
        }
    }

    @DecodeErrorBody
    @POST("com.atproto.server.createAccount")
    suspend fun createAccount(
        @Body body: CreateAccountInput
    ): ApiResult<CreateAccountOutput, AtpError>

    @DecodeErrorBody
    @POST("com.atproto.server.createSession")
    suspend fun createSession(
        @Body body: CreateSessionInput
    ): ApiResult<CreateSessionOutput, AtpError>

    @DecodeErrorBody
    @POST("com.atproto.server.refreshSession")
    suspend fun refreshSession(
        @Header("Authorization") authorization: String
    ): ApiResult<RefreshSessionOutput, AtpError>

    @DecodeErrorBody
    @GET("app.bsky.actor.getProfile")
    suspend fun getProfile(
        @Header("Authorization") authorization: String,
        @Query("actor") actor: String
    ): ApiResult<ProfileView, AtpError>

//    @DecodeErrorBody
//    @POST("com.atproto.repo.putRecord")
//    suspend fun updateProfile(
//        @Header("Authorization") authorization: String,
//        @Body body: UpdateProfileInput
//    ): ApiResult<UpdateProfileOutput, AtpError>

    @DecodeErrorBody
    @GET("app.bsky.feed.getTimeline")
    suspend fun getTimeline(
        @Header("Authorization") authorization: String,
        @Query("algorithm") algorithm: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ): ApiResult<Timeline, AtpError>

    @DecodeErrorBody
    @GET("app.bsky.feed.getAuthorFeed")
    suspend fun getAuthorFeed(
        @Header("Authorization") authorization: String,
        @Query("actor") actor: String,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ): ApiResult<AuthorFeed, AtpError>

    @DecodeErrorBody
    @POST("com.atproto.repo.createRecord")
    suspend fun createPost(
        @Header("Authorization") authorization: String,
        @Body body: CreateRecordInput<Post>
    ): ApiResult<CreateRecordOutput, AtpError>

    @DecodeErrorBody
    @POST("com.atproto.repo.createRecord")
    suspend fun repost(
        @Header("Authorization") authorization: String,
        @Body body: CreateRecordInput<Repost>
    ): ApiResult<CreateRecordOutput, AtpError>

    @DecodeErrorBody
    @POST("com.atproto.repo.deleteRecord")
    suspend fun deleteRecord(
        @Header("Authorization") authorization: String,
        @Body body: DeleteRecordInput
    ): ApiResult<Unit, AtpError>

    @DecodeErrorBody
    @POST("com.atproto.repo.createRecord")
    suspend fun like(
        @Header("Authorization") authorization: String,
        @Body body: CreateRecordInput<Like>
    ): ApiResult<CreateRecordOutput, AtpError>

    @DecodeErrorBody
    @GET("app.bsky.notification.listNotifications")
    suspend fun listNotifications(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ): ApiResult<io.github.akiomik.seiun.model.app.bsky.notification.Notifications, AtpError>

    @DecodeErrorBody
    @POST("app.bsky.notification.updateSeen")
    suspend fun updateNotificationSeen(
        @Header("Authorization") authorization: String,
        @Body() body: UpdateNotificationSeenInput
    ): ApiResult<Unit, AtpError>

    @DecodeErrorBody
    @POST("com.atproto.repo.uploadBlob")
    suspend fun uploadBlob(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String,
        @Body body: RequestBody
    ): ApiResult<UploadBlobOutput, AtpError>

    @DecodeErrorBody
    @POST("com.atproto.moderation.createReport")
    suspend fun createReport(
        @Header("Authorization") authorization: String,
        @Body body: CreateReportInput
    ): ApiResult<CreateReportOutput, AtpError>

    @DecodeErrorBody
    @GET("app.bsky.graph.getFollows")
    suspend fun getFollows(
        @Header("Authorization") authorization: String,
        @Query("actor") actor: String,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ): ApiResult<Follows, AtpError>

    @DecodeErrorBody
    @GET("app.bsky.graph.getFollowers")
    suspend fun getFollowers(
        @Header("Authorization") authorization: String,
        @Query("actor") actor: String,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ): ApiResult<Followers, AtpError>

    @DecodeErrorBody
    @POST("com.atproto.repo.createRecord")
    suspend fun follow(
        @Header("Authorization") authorization: String,
        @Body body: CreateRecordInput<Follow>
    ): ApiResult<CreateRecordOutput, AtpError>

    @DecodeErrorBody
    @POST("com.atproto.repo.deleteRecord")
    suspend fun unfollow(
        @Header("Authorization") authorization: String,
        @Body body: DeleteRecordInput
    ): ApiResult<Unit, AtpError>

    @DecodeErrorBody
    @POST("app.bsky.graph.muteActor")
    suspend fun muteActor(
        @Header("Authorization") authorization: String,
        @Body body: MuteActorInput
    ): ApiResult<Unit, AtpError>

    @DecodeErrorBody
    @POST("app.bsky.graph.unmuteActor")
    suspend fun unmuteActor(
        @Header("Authorization") authorization: String,
        @Body body: UnmuteActorInput
    ): ApiResult<Unit, AtpError>
}
