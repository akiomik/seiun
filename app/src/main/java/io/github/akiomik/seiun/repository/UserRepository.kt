package io.github.akiomik.seiun.repository

import android.util.Log
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.api.RequestHelper
import io.github.akiomik.seiun.model.app.bsky.actor.ProfileDetail
import io.github.akiomik.seiun.model.app.bsky.actor.Ref
import io.github.akiomik.seiun.model.app.bsky.graph.Follow
import io.github.akiomik.seiun.model.app.bsky.graph.MuteInput
import io.github.akiomik.seiun.model.app.bsky.graph.UnmuteInput
import io.github.akiomik.seiun.model.app.bsky.system.DeclRef
import io.github.akiomik.seiun.model.com.atproto.repo.CreateRecordInput
import io.github.akiomik.seiun.model.com.atproto.repo.CreateRecordOutput
import io.github.akiomik.seiun.model.com.atproto.repo.DeleteRecordInput
import io.github.akiomik.seiun.utilities.UriConverter
import java.util.*

class UserRepository(private val authRepository: AuthRepository) : ApplicationRepository() {
    suspend fun getProfile(): ProfileDetail {
        Log.d(SeiunApplication.TAG, "Get profile")

        return RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().getProfile("Bearer ${it.accessJwt}", it.did)
        }
    }

    suspend fun getProfileOf(did: String): ProfileDetail {
        Log.d(SeiunApplication.TAG, "Get profile of $did")

        return RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().getProfile("Bearer ${it.accessJwt}", did)
        }
    }

    suspend fun follow(did: String, declRef: DeclRef): CreateRecordOutput {
        Log.d(SeiunApplication.TAG, "Follow $did")

        val subject = Ref(did = did, declarationCid = declRef.cid)
        return RequestHelper.executeWithRetry(authRepository) {
            val body = CreateRecordInput(
                did = it.did,
                record = Follow(subject = subject, Date()),
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
        val body = MuteInput(user = did)

        return RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().mute("Bearer ${it.accessJwt}", body = body)
        }
    }

    suspend fun unmute(did: String) {
        Log.d(SeiunApplication.TAG, "Unmute user: $did")

        val body = UnmuteInput(user = did)
        return RequestHelper.executeWithRetry(authRepository) {
            getAtpClient().unmute("Bearer ${it.accessJwt}", body = body)
        }
    }
}
