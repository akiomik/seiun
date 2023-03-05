package io.github.akiomik.seiun

import android.util.Log
import com.slack.eithernet.ApiResult
import com.slack.eithernet.response
import io.github.akiomik.seiun.model.*
import io.github.akiomik.seiun.service.AtpService
import io.github.akiomik.seiun.service.UnauthorizedException
import java.time.Instant

class TimelineRepository(private val atpService: AtpService) {
    suspend fun getTimeline(session: Session, before: String? = null): Timeline {
        Log.d("Seiun", "Get timeline: before = $before")

        return when (val result = atpService.getTimeline("Bearer ${session.accessJwt}", before = before)) {
            is ApiResult.Success -> result.value
            is ApiResult.Failure -> when(result) {
                is ApiResult.Failure.HttpFailure -> {
                    if (result.code == 400 || result.code == 401) {
                        throw UnauthorizedException("Unauthorized: ${result.code} (${result.error})")
                    } else {
                        throw IllegalStateException("HttpError: ${result.code} (${result.error})")
                    }
                }
                else -> throw IllegalStateException("ApiResult.Failure: ${result.response()}")
            }
        }
    }

    suspend fun upvote(session: Session, subject: StrongRef) {
        Log.d("Seiun", "Upvote post: uri = ${subject.uri}, cid = ${subject.cid}")

        val body = SetVoteParam(subject = subject, direction = VoteDirection.up)
        when (val result = atpService.upvote(authorization = "Bearer ${session.accessJwt}", body = body)) {
            is ApiResult.Success -> {}
            is ApiResult.Failure -> when(result) {
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

    suspend fun cancelVote(session: Session, subject: StrongRef) {
        Log.d("Seiun", "Cancel vote post: uri = ${subject.uri}, cid = ${subject.cid}")

        val body = SetVoteParam(subject = subject, direction = VoteDirection.none)
        when (val result = atpService.upvote(authorization = "Bearer ${session.accessJwt}", body = body)) {
            is ApiResult.Success -> {}
            is ApiResult.Failure -> when(result) {
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

    suspend fun createPost(session: Session, content: String) {
        Log.d("Seiun", "Create a post: content = $content")

        val createdAt = Instant.now().toString()
        val record = FeedPostRecord(text = content, createdAt = createdAt)
        val body = CreatePostParam(did = session.did, record = record)

        when (val result = atpService.createPost(authorization = "Bearer ${session.accessJwt}", body = body)) {
            is ApiResult.Success -> {}
            is ApiResult.Failure -> when(result) {
                is ApiResult.Failure.HttpFailure -> {
                    if (result.code == 400 || result.code == 401) {
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