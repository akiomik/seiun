package io.github.akiomik.seiun.repository

import android.util.Log
import io.github.akiomik.seiun.SeiunApplication
import io.github.akiomik.seiun.model.ISession
import io.github.akiomik.seiun.model.app.bsky.actor.Profile
import io.github.akiomik.seiun.model.app.bsky.graph.MuteInput
import io.github.akiomik.seiun.model.app.bsky.graph.UnmuteInput

class UserRepository : ApplicationRepository() {
    suspend fun getProfile(session: ISession): Profile {
        Log.d(SeiunApplication.TAG, "Get profile")

        return handleRequest {
            getAtpClient().getProfile("Bearer ${session.accessJwt}", session.did)
        }
    }

    suspend fun mute(session: ISession, did: String) {
        Log.d(SeiunApplication.TAG, "Mute user: $did")
        val body = MuteInput(user = did)

        handleRequest {
            getAtpClient().mute("Bearer ${session.accessJwt}", body = body)
        }
    }

    suspend fun unmute(session: ISession, did: String) {
        Log.d(SeiunApplication.TAG, "Unmute user: $did")

        val body = UnmuteInput(user = did)
        handleRequest {
            getAtpClient().unmute("Bearer ${session.accessJwt}", body = body)
        }
    }
}
