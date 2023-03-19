package io.github.akiomik.seiun.repository

import android.util.Log
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.api.RequestHelper
import io.github.akiomik.seiun.datasources.PostFeedCacheDataSource
import io.github.akiomik.seiun.model.app.bsky.actor.Profile
import io.github.akiomik.seiun.model.app.bsky.blob.UploadBlobOutput
import io.github.akiomik.seiun.model.app.bsky.feed.AuthorFeed
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
import io.github.akiomik.seiun.model.type.Image
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*

class PostFeedRepository(private val authRepository: AuthRepository) : ApplicationRepository() {
    val feedPosts: Flow<List<FeedViewPost>> = PostFeedCacheDataSource.feed

    suspend fun getTimeline(before: String? = null): Timeline {
        Log.d(SeiunApplication.TAG, "Get timeline: before = $before")

        val timeline = RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().getTimeline("Bearer ${it.accessJwt}", before = before)
        }

        timeline.feed.forEach { PostFeedCacheDataSource.putFeedPost(it) }

        return timeline
    }

    suspend fun getAuthorFeed(author: Profile, before: String? = null): AuthorFeed {
        Log.d(SeiunApplication.TAG, "Get author feed: before = $before")

        val feed = RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().getAuthorFeed(
                "Bearer ${it.accessJwt}",
                author = author.handle,
                before = before
            )
        }

        feed.feed.forEach { PostFeedCacheDataSource.putFeedPost(it) }

        return feed
    }

    suspend fun upvote(feedPost: FeedViewPost): SetVoteOutput {
        val subject = feedPost.post.toStrongRef()
        Log.d(SeiunApplication.TAG, "Upvote post: uri = ${subject.uri}, cid = ${subject.cid}")

        val body = SetVoteInput(subject = subject, direction = VoteDirection.up)
        val res = RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().setVote(authorization = "Bearer ${it.accessJwt}", body = body)
        }

        PostFeedCacheDataSource.putFeedPost(feedPost.copy(post = feedPost.post.upvoted(res.upvote!!)))

        return res
    }

    suspend fun cancelVote(feedPost: FeedViewPost): SetVoteOutput {
        val subject = feedPost.post.toStrongRef()
        Log.d(SeiunApplication.TAG, "Cancel vote post: uri = ${subject.uri}, cid = ${subject.cid}")

        val body = SetVoteInput(subject = subject, direction = VoteDirection.none)
        val res = RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().setVote(authorization = "Bearer ${it.accessJwt}", body = body)
        }

        PostFeedCacheDataSource.putFeedPost(feedPost.copy(post = feedPost.post.upvoteCanceled()))

        return res
    }

    suspend fun repost(feedPost: FeedViewPost): CreateRecordOutput {
        val subject = feedPost.post.toStrongRef()
        Log.d(SeiunApplication.TAG, "Repost: uri = ${subject.uri}, cid = ${subject.cid}")

        val res = RequestHelper.executeWithRetry(authRepository) {
            val record = Repost(subject = subject, Date())
            val body = CreateRecordInput(
                did = it.did,
                record = record,
                collection = "app.bsky.feed.repost"
            )
            getAtpClient().repost(authorization = "Bearer ${it.accessJwt}", body = body)
        }

        PostFeedCacheDataSource.putFeedPost(feedPost.copy(post = feedPost.post.reposted(res.uri)))

        return res
    }

    suspend fun cancelRepost(feedPost: FeedViewPost) {
        val uri = feedPost.post.uri
        Log.d(SeiunApplication.TAG, "Cancel repost: $uri")

        val rkey = uri.split('/').last()

        val res = RequestHelper.executeWithRetry(authRepository) {
            val body =
                DeleteRecordInput(did = it.did, rkey = rkey, collection = "app.bsky.feed.repost")
            getAtpClient().deleteRecord("Bearer ${it.accessJwt}", body = body)
        }

        PostFeedCacheDataSource.putFeedPost(feedPost.copy(post = feedPost.post.repostCanceled()))

        return res
    }

    suspend fun createPost(
        content: String,
        imageCid: String?,
        imageMimeType: String?
    ): CreateRecordOutput {
        Log.d(SeiunApplication.TAG, "Create a post: content = $content")

        val embed = if (imageCid != null && imageMimeType != null) {
            val image = io.github.akiomik.seiun.model.app.bsky.embed.Image(
                image = Image(
                    imageCid,
                    imageMimeType
                ),
                alt = ""
            )
            ImagesOrExternal(images = listOf(image), type = "app.bsky.embed.images")
        } else {
            null
        }

        val record = Post(text = content, createdAt = Date(), embed = embed)

        val res = RequestHelper.executeWithRetry(authRepository) {
            val body =
                CreateRecordInput(did = it.did, record = record, collection = "app.bsky.feed.post")
            getAtpClient().createPost(authorization = "Bearer ${it.accessJwt}", body = body)
        }

        // TODO: Add created record to FeedCacheDataSource

        return res
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
                io.github.akiomik.seiun.model.app.bsky.embed.Image(
                    image = Image(
                        imageCid,
                        imageMimeType
                    ),
                    alt = "app.bsky.feed.post"
                )
            ImagesOrExternal(images = listOf(image), type = "app.bsky.embed.images")
        } else {
            null
        }
        val record = Post(text = content, createdAt = Date(), reply = to, embed = embed)

        val res = RequestHelper.executeWithRetry(authRepository) {
            val body =
                CreateRecordInput(did = it.did, record = record, collection = "app.bsky.feed.post")
            getAtpClient().createPost(authorization = "Bearer ${it.accessJwt}", body = body)
        }

        // TODO: Add created record to FeedCacheDataSource

        return res
    }

    suspend fun deletePost(feedViewPost: FeedViewPost) {
        Log.d(SeiunApplication.TAG, "Delete post: uri = ${feedViewPost.post.uri}")

        val rkey = feedViewPost.post.uri.split('/').last()

        val res = RequestHelper.executeWithRetry(authRepository) {
            val body =
                DeleteRecordInput(did = it.did, collection = "app.bsky.feed.post", rkey = rkey)
            getAtpClient().deleteRecord("Bearer ${it.accessJwt}", body)
        }

        PostFeedCacheDataSource.removeFeedPost(feedViewPost.id())

        return res
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
