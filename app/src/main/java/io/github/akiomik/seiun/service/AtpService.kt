package io.github.akiomik.seiun.service

import com.slack.eithernet.ApiResult
import com.slack.eithernet.DecodeErrorBody
import io.github.akiomik.seiun.model.AtpError
import io.github.akiomik.seiun.model.app.bsky.actor.Profile
import io.github.akiomik.seiun.model.app.bsky.blob.UploadBlobOutput
import io.github.akiomik.seiun.model.app.bsky.feed.Post
import io.github.akiomik.seiun.model.app.bsky.feed.Repost
import io.github.akiomik.seiun.model.app.bsky.feed.SetVoteInput
import io.github.akiomik.seiun.model.app.bsky.feed.SetVoteOutput
import io.github.akiomik.seiun.model.app.bsky.feed.Timeline
import io.github.akiomik.seiun.model.com.atproto.account.AccountCreateInput
import io.github.akiomik.seiun.model.com.atproto.account.AccountCreateOutput
import io.github.akiomik.seiun.model.com.atproto.repo.CreateRecordInput
import io.github.akiomik.seiun.model.com.atproto.repo.CreateRecordOutput
import io.github.akiomik.seiun.model.com.atproto.repo.DeleteRecordInput
import io.github.akiomik.seiun.model.com.atproto.session.SessionCreateInput
import io.github.akiomik.seiun.model.com.atproto.session.SessionCreateOutput
import io.github.akiomik.seiun.model.com.atproto.session.SessionRefreshOutput
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AtpService {
    @DecodeErrorBody
    @POST("com.atproto.account.create")
    suspend fun createAccount(
        @Body body: AccountCreateInput
    ): ApiResult<AccountCreateOutput, AtpError>

    @DecodeErrorBody
    @POST("com.atproto.session.create")
    suspend fun createSession(
        @Body body: SessionCreateInput
    ): ApiResult<SessionCreateOutput, AtpError>

    @DecodeErrorBody
    @POST("com.atproto.session.refresh")
    suspend fun refreshSession(
        @Header("Authorization") authorization: String
    ): ApiResult<SessionRefreshOutput, AtpError>

    @DecodeErrorBody
    @GET("app.bsky.actor.getProfile")
    suspend fun getProfile(
        @Header("Authorization") authorization: String,
        @Query("actor") actor: String
    ): ApiResult<Profile, AtpError>

    @DecodeErrorBody
    @GET("app.bsky.feed.getTimeline")
    suspend fun getTimeline(
        @Header("Authorization") authorization: String,
        @Query("algorithm") algorithm: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("before") before: String? = null
    ): ApiResult<Timeline, AtpError>

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
    ) // TODO: Handle empty response with EitherNet

    @DecodeErrorBody
    @POST("app.bsky.feed.setVote")
    suspend fun setVote(
        @Header("Authorization") authorization: String,
        @Body body: SetVoteInput
    ): ApiResult<SetVoteOutput, AtpError>

    @DecodeErrorBody
    @GET("app.bsky.notification.list")
    suspend fun listNotifications(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int? = null,
        @Query("before") before: String? = null
    ): ApiResult<io.github.akiomik.seiun.model.app.bsky.notification.NotificationList, AtpError>

    @DecodeErrorBody
    @POST("com.atproto.blob.upload")
    suspend fun uploadBlob(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String,
        @Body body: RequestBody
    ): ApiResult<UploadBlobOutput, AtpError>
}
