package io.github.akiomik.seiun.repository

import android.util.Log
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.api.RequestHelper
import io.github.akiomik.seiun.model.app.bsky.actor.ProfileView
import io.github.akiomik.seiun.model.app.bsky.graph.Follow
import io.github.akiomik.seiun.model.app.bsky.graph.Followers
import io.github.akiomik.seiun.model.app.bsky.graph.Follows
import io.github.akiomik.seiun.model.app.bsky.graph.MuteActorInput
import io.github.akiomik.seiun.model.app.bsky.graph.UnmuteActorInput
import io.github.akiomik.seiun.model.com.atproto.repo.CreateRecordInput
import io.github.akiomik.seiun.model.com.atproto.repo.CreateRecordOutput
import io.github.akiomik.seiun.model.com.atproto.repo.DeleteRecordInput
import io.github.akiomik.seiun.utilities.UriConverter
import java.util.*

class UserRepository(private val authRepository: AuthRepository) : ApplicationRepository() {
    suspend fun getProfile(): ProfileView {
        Log.d(SeiunApplication.TAG, "Get profile")

        return RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().getProfile("Bearer ${it.accessJwt}", it.did)
        }
    }

    suspend fun getProfileOf(did: String): ProfileView {
        Log.d(SeiunApplication.TAG, "Get profile of $did")

        return RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().getProfile("Bearer ${it.accessJwt}", did)
        }
    }

//    suspend fun updateProfile(profile: Profile): UpdateProfileOutput {
//        Log.d(SeiunApplication.TAG, "Update profile")
//
//        val body = UpdateProfileInput(
//            displayName = profile.displayName,
//            description = profile.description,
//            avatar = profile.avatar,
//            banner = profile.banner
//        )
//        return RequestHelper.executeWithRetry(authRepository) {
//            getAtpClient().updateProfile("Bearer ${it.accessJwt}", body)
//        }
//    }

    suspend fun getFollows(did: String, cursor: String? = null): Follows {
        Log.d(SeiunApplication.TAG, "Get follows: did = $did, cursor = $cursor")

        return RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().getFollows(
                authorization = "Bearer ${it.accessJwt}",
                user = did,
                cursor = cursor
            )
        }
    }

    suspend fun getFollowers(did: String, cursor: String? = null): Followers {
        Log.d(SeiunApplication.TAG, "Get followers: did = $did, cursor = $cursor")

        return RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().getFollowers(
                authorization = "Bearer ${it.accessJwt}",
                user = did,
                cursor = cursor
            )
        }
    }

    suspend fun follow(did: String): CreateRecordOutput {
        Log.d(SeiunApplication.TAG, "Follow $did")

        return RequestHelper.executeWithRetry(authRepository) {
            val body = CreateRecordInput(
                did = it.did,
                record = Follow(subject = did, Date()),
                collection = "app.bsky.graph.follow"
            )
            getAtpClient().follow("Bearer ${it.accessJwt}", body)
        }
    }

    suspend fun unfollow(uri: String) {
        Log.d(SeiunApplication.TAG, "Unfollow $uri")

        return RequestHelper.executeWithRetry(authRepository) {
            val body = DeleteRecordInput(
                did = it.did,
                collection = "app.bsky.graph.follow",
                rkey = UriConverter.toRkey(uri)
            )
            getAtpClient().unfollow("Bearer ${it.accessJwt}", body)
        }
    }

    suspend fun mute(did: String) {
        Log.d(SeiunApplication.TAG, "Mute user: $did")
        val body = MuteActorInput(actor = did)

        return RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().muteActor("Bearer ${it.accessJwt}", body = body)
        }
    }

    suspend fun unmute(did: String) {
        Log.d(SeiunApplication.TAG, "Unmute user: $did")

        val body = UnmuteActorInput(actor = did)
        return RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().unmuteActor("Bearer ${it.accessJwt}", body = body)
        }
    }
}
