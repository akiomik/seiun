package io.github.akiomik.seiun.repository

import android.util.Log
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
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*
import io.github.akiomik.seiun.model.type.Image as ImageType

class TimelineRepository() : ApplicationRepository() {
    suspend fun getTimeline(session: ISession, before: String? = null): Timeline {
        Log.d(SeiunApplication.TAG, "Get timeline: before = $before")

        return handleRequest {
            getAtpClient().getTimeline("Bearer ${session.accessJwt}", before = before)
        }
    }

    suspend fun upvote(session: ISession, subject: StrongRef): SetVoteOutput {
        Log.d(SeiunApplication.TAG, "Upvote post: uri = ${subject.uri}, cid = ${subject.cid}")

        val body = SetVoteInput(subject = subject, direction = VoteDirection.up)
        return handleRequest {
            getAtpClient().setVote(authorization = "Bearer ${session.accessJwt}", body = body)
        }
    }

    suspend fun cancelVote(session: ISession, subject: StrongRef): SetVoteOutput {
        Log.d(SeiunApplication.TAG, "Cancel vote post: uri = ${subject.uri}, cid = ${subject.cid}")

        val body = SetVoteInput(subject = subject, direction = VoteDirection.none)
        return handleRequest {
            getAtpClient().setVote(authorization = "Bearer ${session.accessJwt}", body = body)
        }
    }

    suspend fun repost(session: ISession, subject: StrongRef): CreateRecordOutput {
        Log.d(SeiunApplication.TAG, "Repost: uri = ${subject.uri}, cid = ${subject.cid}")

        val record = Repost(subject = subject, Date())
        val body = CreateRecordInput(
            did = session.did,
            record = record,
            collection = "app.bsky.feed.repost"
        )

        return handleRequest {
            getAtpClient().repost(authorization = "Bearer ${session.accessJwt}", body = body)
        }
    }

    suspend fun cancelRepost(session: ISession, uri: String) {
        Log.d(SeiunApplication.TAG, "Cancel repost: $uri")

        val rkey = uri.split('/').last()
        val body =
            DeleteRecordInput(did = session.did, rkey = rkey, collection = "app.bsky.feed.repost")

        handleRequest {
            getAtpClient().deleteRecord("Bearer ${session.accessJwt}", body = body)
        }
    }

    suspend fun createPost(
        session: ISession,
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
        val body =
            CreateRecordInput(did = session.did, record = record, collection = "app.bsky.feed.post")

        return handleRequest {
            getAtpClient().createPost(authorization = "Bearer ${session.accessJwt}", body = body)
        }
    }

    suspend fun createReply(
        session: ISession,
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
        val body = CreateRecordInput(did = session.did, record = record, collection = "")

        return handleRequest {
            getAtpClient().createPost(authorization = "Bearer ${session.accessJwt}", body = body)
        }
    }

    suspend fun deletePost(session: ISession, feedViewPost: FeedViewPost) {
        Log.d(SeiunApplication.TAG, "Delete post: uri = ${feedViewPost.post.uri}")

        val rkey = feedViewPost.post.uri.split('/').last()
        val body =
            DeleteRecordInput(did = session.did, collection = "app.bsky.feed.post", rkey = rkey)

        handleRequest {
            getAtpClient().deleteRecord("Bearer ${session.accessJwt}", body)
        }
    }

    suspend fun uploadImage(
        session: ISession,
        image: ByteArray,
        mimeType: String
    ): UploadBlobOutput {
        Log.d(SeiunApplication.TAG, "Upload image: mimeType = $mimeType")

        return handleRequest {
            getAtpClient().uploadBlob(
                authorization = "Bearer ${session.accessJwt}",
                contentType = mimeType,
                body = image.toRequestBody()
            )
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

        return handleRequest {
            getAtpClient().createReport(authorization = "Bearer ${session.accessJwt}", body = body)
        }
    }
}
