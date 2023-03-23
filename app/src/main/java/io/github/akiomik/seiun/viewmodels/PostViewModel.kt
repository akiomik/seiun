package io.github.akiomik.seiun.viewmodels

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.app.bsky.feed.FeedViewPost
import io.github.akiomik.seiun.model.app.bsky.feed.PostReplyRef
import java.io.ByteArrayOutputStream

class PostViewModel : ApplicationViewModel() {
    private val userRepository = SeiunApplication.instance!!.userRepository
    private val postFeedRepository = SeiunApplication.instance!!.postFeedRepository

    fun upvote(
        feedPost: FeedViewPost,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        wrapError(
            run = { postFeedRepository.like(feedPost) },
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    fun cancelVote(
        feedPost: FeedViewPost,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        wrapError(
            run = { postFeedRepository.cancelLike(feedPost) },
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    fun repost(
        feedPost: FeedViewPost,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        wrapError(
            run = { postFeedRepository.repost(feedPost) },
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    fun cancelRepost(
        feedPost: FeedViewPost,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        wrapError(
            run = { postFeedRepository.cancelRepost(feedPost) },
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    fun createPost(
        content: String,
        image: ByteArray? = null,
        mimeType: String? = null,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        wrapError(run = {
            val output = if (image != null && mimeType != null) {
                postFeedRepository.uploadImage(image, mimeType)
            } else {
                null
            }
            postFeedRepository.createPost(content, output?.blob?.cid, mimeType)
            // TODO: Add id to _feedPostIds
        }, onSuccess = { onSuccess() }, onError = onError)
    }

    fun createReply(
        content: String,
        feedViewPost: FeedViewPost,
        image: ByteArray? = null,
        mimeType: String? = null,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        wrapError(run = {
            val to = if (feedViewPost.reply == null) {
                val ref = feedViewPost.post.toStrongRef()
                PostReplyRef(root = ref, parent = ref)
            } else {
                val root = feedViewPost.reply.root.toStrongRef()
                val parent = feedViewPost.post.toStrongRef()
                PostReplyRef(root = root, parent = parent)
            }

            val output = if (image != null && mimeType != null) {
                postFeedRepository.uploadImage(image, mimeType)
            } else {
                null
            }
            postFeedRepository.createReply(content, to, output?.blob?.cid, mimeType)
            // TODO: Add id to _feedPostIds
        }, onSuccess = { onSuccess() }, onError = onError)
    }

    fun deletePost(viewPost: FeedViewPost, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        wrapError(
            run = { postFeedRepository.deletePost(viewPost) },
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    fun reportPost(
        viewPost: FeedViewPost,
        reasonType: String,
        reason: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        wrapError(
            run = { postFeedRepository.reportPost(viewPost, reasonType, reason) },
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    fun mute(did: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        wrapError(
            run = { userRepository.mute(did) },
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    fun unmute(did: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        wrapError(
            run = { userRepository.unmute(did) },
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    private fun resizeImage(original: Bitmap): Bitmap {
        // NOTE: app.bsky.embed.images supports images up to 2000x2000px and 1MB
        val maxWidth = 2000
        val maxHeight = 2000

        if (original.width <= maxWidth && original.height <= maxHeight) {
            return original
        }

        val (width, height) = if (original.width >= original.height) {
            Pair(maxWidth, original.height * maxWidth / original.width)
        } else {
            Pair(original.width * maxHeight / original.height, maxHeight)
        }

        return Bitmap.createScaledBitmap(original, width, height, true)
    }

    fun convertToUploadableImage(source: ImageDecoder.Source): ByteArray {
        val bitmap = resizeImage(ImageDecoder.decodeBitmap(source))
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }
}
