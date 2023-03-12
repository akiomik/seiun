package io.github.akiomik.seiun.repository

import android.util.Log
import com.slack.eithernet.ApiResult
import com.slack.eithernet.response
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.ISession
import io.github.akiomik.seiun.model.app.bsky.blob.UploadBlobOutput
import io.github.akiomik.seiun.model.app.bsky.embed.Image
import io.github.akiomik.seiun.model.app.bsky.feed.FeedViewPost
import io.github.akiomik.seiun.model.app.bsky.feed.ImagesOrExternal
import io.github.akiomik.seiun.model.app.bsky.feed.Post
import io.github.akiomik.seiun.model.app.bsky.feed.PostReplyRef
import io.github.akiomik.seiun.model.app.bsky.feed.Repost
import io.github.akiomik.seiun.model.app.bsky.feed.SetVoteInput
import io.github.akiomik.seiun.model.app.bsky.feed.SetVoteOutput
import io.github.akiomik.seiun.model.app.bsky.feed.Timeline
import io.github.akiomik.seiun.model.app.bsky.feed.VoteDirection
import io.github.akiomik.seiun.model.app.bsky.report.RepoRefOrRecordRef
import io.github.akiomik.seiun.model.app.bsky.report.ReportCreateInput
import io.github.akiomik.seiun.model.app.bsky.report.ReportCreateOutput
import io.github.akiomik.seiun.model.com.atproto.repo.CreateRecordInput
import io.github.akiomik.seiun.model.com.atproto.repo.CreateRecordOutput
import io.github.akiomik.seiun.model.com.atproto.repo.DeleteRecordInput
import io.github.akiomik.seiun.model.com.atproto.repo.StrongRef
import io.github.akiomik.seiun.service.AtpService
import io.github.akiomik.seiun.service.UnauthorizedException
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.time.Instant
import io.github.akiomik.seiun.model.type.Image as ImageType

class TimelineRepository(private val atpService: AtpService) {
    suspend fun getTimeline(session: ISession, before: String? = null): Timeline {
        Log.d(SeiunApplication.TAG, "Get timeline: before = $before")

        return when (
            val result =
                atpService.getTimeline("Bearer ${session.accessJwt}", before = before)
        ) {
            is ApiResult.Success -> result.value
            is ApiResult.Failure -> when (result) {
                is ApiResult.Failure.HttpFailure -> {
                    if (result.code == 401) {
                        throw UnauthorizedException("Unauthorized: ${result.code} (${result.error})")
                    } else {
                        throw IllegalStateException("HttpError: ${result.code} (${result.error})")
                    }
                }
                else -> throw IllegalStateException("ApiResult.Failure: ${result.response()}")
            }
        }
    }

    suspend fun upvote(session: ISession, subject: StrongRef): SetVoteOutput {
        Log.d(SeiunApplication.TAG, "Upvote post: uri = ${subject.uri}, cid = ${subject.cid}")

        val body = SetVoteInput(subject = subject, direction = VoteDirection.up)
        when (
            val result =
                atpService.setVote(authorization = "Bearer ${session.accessJwt}", body = body)
        ) {
            is ApiResult.Success -> return result.value
            is ApiResult.Failure -> when (result) {
                is ApiResult.Failure.HttpFailure -> {
                    if (result.code == 401) {
                        throw UnauthorizedException("Unauthorized: ${result.code} (${result.error})")
                    } else {
                        throw IllegalStateException("HttpError: ${result.code} (${result.error})")
                    }
                }
                else -> throw IllegalStateException("ApiResult.Failure: ${result.response()}")
            }
        }
    }

    suspend fun cancelVote(session: ISession, subject: StrongRef) {
        Log.d(SeiunApplication.TAG, "Cancel vote post: uri = ${subject.uri}, cid = ${subject.cid}")

        val body = SetVoteInput(subject = subject, direction = VoteDirection.none)
        when (
            val result =
                atpService.setVote(authorization = "Bearer ${session.accessJwt}", body = body)
        ) {
            is ApiResult.Success -> {}
            is ApiResult.Failure -> when (result) {
                is ApiResult.Failure.HttpFailure -> {
                    if (result.code == 401) {
                        throw UnauthorizedException("Unauthorized: ${result.code} (${result.error})")
                    } else {
                        throw IllegalStateException("HttpError: ${result.code} (${result.error})")
                    }
                }
                else -> throw IllegalStateException("ApiResult.Failure: ${result.response()}")
            }
        }
    }

    suspend fun repost(session: ISession, subject: StrongRef): CreateRecordOutput {
        Log.d(SeiunApplication.TAG, "Cancel repost: uri = ${subject.uri}, cid = ${subject.cid}")

        val createdAt = Instant.now().toString()
        val record = Repost(subject = subject, createdAt)
        val body = CreateRecordInput(
            did = session.did,
            record = record,
            collection = "app.bsky.feed.repost"
        )

        when (
            val result =
                atpService.repost(authorization = "Bearer ${session.accessJwt}", body = body)
        ) {
            is ApiResult.Success -> return result.value
            is ApiResult.Failure -> when (result) {
                is ApiResult.Failure.HttpFailure -> {
                    if (result.code == 401) {
                        throw UnauthorizedException("Unauthorized: ${result.code} (${result.error})")
                    } else {
                        throw IllegalStateException("HttpError: ${result.code} (${result.error})")
                    }
                }
                else -> throw IllegalStateException("ApiResult.Failure: ${result.response()}")
            }
        }
    }

    suspend fun cancelRepost(session: ISession, uri: String) {
        Log.d(SeiunApplication.TAG, "Cancel repost: $uri")

        val rkey = uri.split('/').last()
        val body =
            DeleteRecordInput(did = session.did, rkey = rkey, collection = "app.bsky.feed.repost")

        when (val res = atpService.deleteRecord("Bearer ${session.accessJwt}", body = body)) {
            is ApiResult.Success -> {}
            is ApiResult.Failure -> when (res) {
                is ApiResult.Failure.HttpFailure -> {
                    if (res.code == 401) {
                        throw UnauthorizedException(res.error?.message.orEmpty())
                    } else {
                        throw IllegalStateException(res.error?.message.orEmpty())
                    }
                }
                else -> throw IllegalStateException(res.toString())
            }
        }
    }

    suspend fun createPost(
        session: ISession,
        content: String,
        imageCid: String?,
        imageMimeType: String?
    ) {
        Log.d(SeiunApplication.TAG, "Create a post: content = $content")

        val createdAt = Instant.now().toString()
        val embed = if (imageCid != null && imageMimeType != null) {
            val image = Image(image = ImageType(imageCid, imageMimeType), alt = "")
            ImagesOrExternal(images = listOf(image), type = "app.bsky.embed.images")
        } else {
            null
        }
        val record = Post(text = content, createdAt = createdAt, embed = embed)
        val body =
            CreateRecordInput(did = session.did, record = record, collection = "app.bsky.feed.post")

        when (
            val result =
                atpService.createPost(authorization = "Bearer ${session.accessJwt}", body = body)
        ) {
            is ApiResult.Success -> {}
            is ApiResult.Failure -> when (result) {
                is ApiResult.Failure.HttpFailure -> {
                    if (result.code == 401) {
                        throw UnauthorizedException("Unauthorized: ${result.code} (${result.error})")
                    } else {
                        throw IllegalStateException("HttpError: ${result.code} (${result.error})")
                    }
                }
                else -> throw IllegalStateException("ApiResult.Failure: ${result.response()}")
            }
        }
    }

    suspend fun createReply(
        session: ISession,
        content: String,
        to: PostReplyRef,
        imageCid: String?,
        imageMimeType: String?
    ) {
        Log.d(SeiunApplication.TAG, "Create a reply: content = $content, to = $to")

        val createdAt = Instant.now().toString()
        val embed = if (imageCid != null && imageMimeType != null) {
            val image =
                Image(image = ImageType(imageCid, imageMimeType), alt = "app.bsky.feed.post")
            ImagesOrExternal(images = listOf(image), type = "app.bsky.embed.images")
        } else {
            null
        }
        val record = Post(text = content, createdAt = createdAt, reply = to, embed = embed)
        val body = CreateRecordInput(did = session.did, record = record, collection = "")

        when (
            val result =
                atpService.createPost(authorization = "Bearer ${session.accessJwt}", body = body)
        ) {
            is ApiResult.Success -> {}
            is ApiResult.Failure -> when (result) {
                is ApiResult.Failure.HttpFailure -> {
                    if (result.code == 401) {
                        throw UnauthorizedException("Unauthorized: ${result.code} (${result.error})")
                    } else {
                        throw IllegalStateException("HttpError: ${result.code} (${result.error})")
                    }
                }
                else -> throw IllegalStateException("ApiResult.Failure: ${result.response()}")
            }
        }
    }

    suspend fun deletePost(session: ISession, feedViewPost: FeedViewPost) {
        Log.d(SeiunApplication.TAG, "Delete post: uri = ${feedViewPost.post.uri}")

        val rkey = feedViewPost.post.uri.split('/').last()
        val body =
            DeleteRecordInput(did = session.did, collection = "app.bsky.feed.post", rkey = rkey)
        try {
            atpService.deleteRecord("Bearer ${session.accessJwt}", body)
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw UnauthorizedException("Unauthorized: ${e.code()} (${e.message()})")
            } else {
                throw IllegalStateException("HttpError: ${e.code()} (${e.message()})")
            }
        }
    }

    suspend fun uploadImage(
        session: ISession,
        image: ByteArray,
        mimeType: String
    ): UploadBlobOutput {
        Log.d(SeiunApplication.TAG, "Upload image: mimeType = $mimeType")

        when (
            val result =
                atpService.uploadBlob(
                    authorization = "Bearer ${session.accessJwt}",
                    contentType = mimeType,
                    body = image.toRequestBody()
                )
        ) {
            is ApiResult.Success -> return result.value
            is ApiResult.Failure -> when (result) {
                is ApiResult.Failure.HttpFailure -> {
                    if (result.code == 401) {
                        throw UnauthorizedException("Unauthorized: ${result.code} (${result.error})")
                    } else {
                        throw IllegalStateException("HttpError: ${result.code} (${result.error})")
                    }
                }
                else -> throw IllegalStateException("ApiResult.Failure: ${result.response()}")
            }
        }
    }

    suspend fun reportPost(
        session: ISession,
        feedViewPost: FeedViewPost,
        reasonType: String,
        reason: String? = null
    ): ReportCreateOutput {
        Log.d(
            SeiunApplication.TAG,
            "Report post: cid = ${feedViewPost.post.cid}, reasonType = $reasonType"
        )

        val body = ReportCreateInput(
            subject = RepoRefOrRecordRef(
                type = "com.atproto.repo.recordRef",
                uri = feedViewPost.post.uri,
                cid = feedViewPost.post.cid
            ),
            reasonType = reasonType,
            reason = reason
        )
        when (
            val result =
                atpService.createReport(authorization = "Bearer ${session.accessJwt}", body = body)
        ) {
            is ApiResult.Success -> return result.value
            is ApiResult.Failure -> when (result) {
                is ApiResult.Failure.HttpFailure -> {
                    if (result.code == 401) {
                        throw UnauthorizedException("Unauthorized: ${result.code} (${result.error})")
                    } else {
                        throw IllegalStateException("HttpError: ${result.code} (${result.error})")
                    }
                }
                else -> throw IllegalStateException("ApiResult.Failure: ${result.response()}")
            }
        }
    }
}
