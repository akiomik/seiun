package io.github.akiomik.seiun.repository

import android.util.Log
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.api.RequestHelper
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
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*
import io.github.akiomik.seiun.model.type.Image as ImageType

class TimelineRepository(private val authRepository: AuthRepository) : ApplicationRepository() {
    suspend fun getTimeline(before: String? = null): Timeline {
        Log.d(SeiunApplication.TAG, "Get timeline: before = $before")

        return RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().getTimeline("Bearer ${it.accessJwt}", before = before)
        }
    }

    suspend fun upvote(subject: StrongRef): SetVoteOutput {
        Log.d(SeiunApplication.TAG, "Upvote post: uri = ${subject.uri}, cid = ${subject.cid}")

        val body = SetVoteInput(subject = subject, direction = VoteDirection.up)
        return RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().setVote(authorization = "Bearer ${it.accessJwt}", body = body)
        }
    }

    suspend fun cancelVote(subject: StrongRef): SetVoteOutput {
        Log.d(SeiunApplication.TAG, "Cancel vote post: uri = ${subject.uri}, cid = ${subject.cid}")

        val body = SetVoteInput(subject = subject, direction = VoteDirection.none)
        return RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().setVote(authorization = "Bearer ${it.accessJwt}", body = body)
        }
    }

    suspend fun repost(subject: StrongRef): CreateRecordOutput {
        Log.d(SeiunApplication.TAG, "Repost: uri = ${subject.uri}, cid = ${subject.cid}")

        return RequestHelper.executeWithRetry(authRepository) {
            val record = Repost(subject = subject, Date())
            val body = CreateRecordInput(
                did = it.did,
                record = record,
                collection = "app.bsky.feed.repost"
            )
            getAtpClient().repost(authorization = "Bearer ${it.accessJwt}", body = body)
        }
    }

    suspend fun cancelRepost(uri: String) {
        Log.d(SeiunApplication.TAG, "Cancel repost: $uri")

        val rkey = uri.split('/').last()

        return RequestHelper.executeWithRetry(authRepository) {
            val body =
                DeleteRecordInput(did = it.did, rkey = rkey, collection = "app.bsky.feed.repost")
            getAtpClient().deleteRecord("Bearer ${it.accessJwt}", body = body)
        }
    }

    suspend fun createPost(
        content: String,
        imageCid: String?,
        imageMimeType: String?
    ): CreateRecordOutput {
        Log.d(SeiunApplication.TAG, "Create a post: content = $content")

        val embed = if (imageCid != null && imageMimeType != null) {
            val image = Image(image = ImageType(imageCid, imageMimeType), alt = "")
            ImagesOrExternal(images = listOf(image), type = "app.bsky.embed.images")
        } else {
            null
        }

        val record = Post(text = content, createdAt = Date(), embed = embed)

        return RequestHelper.executeWithRetry(authRepository) {
            val body =
                CreateRecordInput(did = it.did, record = record, collection = "app.bsky.feed.post")
            getAtpClient().createPost(authorization = "Bearer ${it.accessJwt}", body = body)
        }
    }

    suspend fun createReply(
        content: String,
        to: PostReplyRef,
        imageCid: String?,
        imageMimeType: String?
    ): CreateRecordOutput {
        Log.d(SeiunApplication.TAG, "Create a reply: content = $content, to = $to")

        val embed = if (imageCid != null && imageMimeType != null) {
            val image =
                Image(image = ImageType(imageCid, imageMimeType), alt = "app.bsky.feed.post")
            ImagesOrExternal(images = listOf(image), type = "app.bsky.embed.images")
        } else {
            null
        }
        val record = Post(text = content, createdAt = Date(), reply = to, embed = embed)

        return RequestHelper.executeWithRetry(authRepository) {
            val body = CreateRecordInput(did = it.did, record = record, collection = "")
            getAtpClient().createPost(authorization = "Bearer ${it.accessJwt}", body = body)
        }
    }

    suspend fun deletePost(feedViewPost: FeedViewPost) {
        Log.d(SeiunApplication.TAG, "Delete post: uri = ${feedViewPost.post.uri}")

        val rkey = feedViewPost.post.uri.split('/').last()

        return RequestHelper.executeWithRetry(authRepository) {
            val body =
                DeleteRecordInput(did = it.did, collection = "app.bsky.feed.post", rkey = rkey)
            getAtpClient().deleteRecord("Bearer ${it.accessJwt}", body)
        }
    }

    suspend fun uploadImage(image: ByteArray, mimeType: String): UploadBlobOutput {
        Log.d(SeiunApplication.TAG, "Upload image: mimeType = $mimeType")

        return RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().uploadBlob(
                authorization = "Bearer ${it.accessJwt}",
                contentType = mimeType,
                body = image.toRequestBody()
            )
        }
    }

    suspend fun reportPost(
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

        return RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().createReport(authorization = "Bearer ${it.accessJwt}", body = body)
        }
    }
}
