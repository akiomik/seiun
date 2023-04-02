package io.github.akiomik.seiun.repository

import android.util.Log
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.api.RequestHelper
import io.github.akiomik.seiun.datasources.PostFeedCacheDataSource
import io.github.akiomik.seiun.model.app.bsky.actor.ProfileView
import io.github.akiomik.seiun.model.app.bsky.feed.AuthorFeed
import io.github.akiomik.seiun.model.app.bsky.feed.FeedViewPost
import io.github.akiomik.seiun.model.app.bsky.feed.ImagesOrExternalOrRecordOrRecordOrRecordWithMedia
import io.github.akiomik.seiun.model.app.bsky.feed.Like
import io.github.akiomik.seiun.model.app.bsky.feed.Post
import io.github.akiomik.seiun.model.app.bsky.feed.PostReplyRef
import io.github.akiomik.seiun.model.app.bsky.feed.Repost
import io.github.akiomik.seiun.model.app.bsky.feed.Timeline
import io.github.akiomik.seiun.model.com.atproto.moderation.CreateReportInput
import io.github.akiomik.seiun.model.com.atproto.moderation.CreateReportOutput
import io.github.akiomik.seiun.model.com.atproto.moderation.RepoRefOrStrongRef
import io.github.akiomik.seiun.model.com.atproto.repo.CreateRecordInput
import io.github.akiomik.seiun.model.com.atproto.repo.CreateRecordOutput
import io.github.akiomik.seiun.model.com.atproto.repo.DeleteRecordInput
import io.github.akiomik.seiun.model.com.atproto.repo.UploadBlobOutput
import io.github.akiomik.seiun.model.type.Blob
import io.github.akiomik.seiun.utilities.UriConverter
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*

class PostFeedRepository(private val authRepository: AuthRepository) : ApplicationRepository() {
    val feedPosts: Flow<List<FeedViewPost>> = PostFeedCacheDataSource.feed

    suspend fun getTimeline(cursor: String? = null): Timeline {
        Log.d(SeiunApplication.TAG, "Get timeline: cursor = $cursor")

        val timeline = RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().getTimeline("Bearer ${it.accessJwt}", cursor = cursor)
        }

        timeline.feed.forEach { PostFeedCacheDataSource.putFeedPost(it) }

        return timeline
    }

    suspend fun getAuthorFeed(author: ProfileView, cursor: String? = null): AuthorFeed {
        Log.d(SeiunApplication.TAG, "Get author feed: cursor = $cursor")

        val feed = RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().getAuthorFeed(
                "Bearer ${it.accessJwt}",
                actor = author.handle,
                cursor = cursor
            )
        }

        feed.feed.forEach { PostFeedCacheDataSource.putFeedPost(it) }

        return feed
    }

    suspend fun like(feedPost: FeedViewPost): CreateRecordOutput {
        val subject = feedPost.post.toStrongRef()
        Log.d(SeiunApplication.TAG, "Like: uri = ${subject.uri}, cid = ${subject.cid}")

        val res = RequestHelper.executeWithRetry(authRepository) {
            val body = CreateRecordInput(
                did = it.did,
                record = Like(subject = subject, createdAt = Date()),
                collection = "app.bsky.feed.like"
            )
            getAtpClient().like(authorization = "Bearer ${it.accessJwt}", body = body)
        }

        PostFeedCacheDataSource.putFeedPost(feedPost.copy(post = feedPost.post.liked(res.uri)))

        return res
    }

    suspend fun cancelLike(feedPost: FeedViewPost) {
        val uri = feedPost.post.viewer?.like ?: return
        Log.d(SeiunApplication.TAG, "Cancel like: uri = $uri")

        val rkey = UriConverter.toRkey(uri)
        RequestHelper.executeWithRetry(authRepository) {
            val body = DeleteRecordInput(
                did = it.did,
                collection = "app.bsky.feed.like",
                rkey = rkey
            )
            getAtpClient().deleteRecord(authorization = "Bearer ${it.accessJwt}", body = body)
        }

        PostFeedCacheDataSource.putFeedPost(feedPost.copy(post = feedPost.post.likeCanceled()))
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
        val uri = feedPost.post.viewer?.repost ?: return

        Log.d(SeiunApplication.TAG, "Cancel repost: $uri")

        val rkey = UriConverter.toRkey(uri)
        RequestHelper.executeWithRetry(authRepository) {
            val body =
                DeleteRecordInput(did = it.did, rkey = rkey, collection = "app.bsky.feed.repost")
            getAtpClient().deleteRecord("Bearer ${it.accessJwt}", body = body)
        }

        PostFeedCacheDataSource.putFeedPost(feedPost.copy(post = feedPost.post.repostCanceled()))
    }

    suspend fun createPost(content: String, imageBlob: Blob? = null): CreateRecordOutput {
        Log.d(SeiunApplication.TAG, "Create a post: content = $content")

        val embed = if (imageBlob != null) {
            val image = io.github.akiomik.seiun.model.app.bsky.embed.ImagesImage(
                image = imageBlob,
                alt = ""
            )
            ImagesOrExternalOrRecordOrRecordOrRecordWithMedia(images = listOf(image), type = "app.bsky.embed.images")
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
        imageBlob: Blob? = null
    ): CreateRecordOutput {
        Log.d(SeiunApplication.TAG, "Create a reply: content = $content, to = $to")

        val embed = if (imageBlob != null) {
            val image =
                io.github.akiomik.seiun.model.app.bsky.embed.ImagesImage(
                    image = imageBlob,
                    alt = "app.bsky.feed.post"
                )
            ImagesOrExternalOrRecordOrRecordOrRecordWithMedia(images = listOf(image), type = "app.bsky.embed.images")
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

        val rkey = UriConverter.toRkey(feedViewPost.post.uri)
        RequestHelper.executeWithRetry(authRepository) {
            val body =
                DeleteRecordInput(did = it.did, collection = "app.bsky.feed.post", rkey = rkey)
            getAtpClient().deleteRecord("Bearer ${it.accessJwt}", body)
        }

        PostFeedCacheDataSource.removeFeedPost(feedViewPost.id())
    }

    suspend fun uploadBlob(blob: ByteArray, mimeType: String): UploadBlobOutput {
        Log.d(SeiunApplication.TAG, "Upload blob: mimeType = $mimeType")

        return RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().uploadBlob(
                authorization = "Bearer ${it.accessJwt}",
                contentType = mimeType,
                body = blob.toRequestBody()
            )
        }
    }

    suspend fun reportPost(
        feedViewPost: FeedViewPost,
        reasonType: String,
        reason: String? = null
    ): CreateReportOutput {
        Log.d(
            SeiunApplication.TAG,
            "Report post: cid = ${feedViewPost.post.cid}, reasonType = $reasonType"
        )

        val body = CreateReportInput(
            subject = RepoRefOrStrongRef(
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
