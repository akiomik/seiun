package com.example.catpaw.models

data class FeedPost(
    val uri: String,
    val cid: String,
    val author: ActorWithInfo,
    val record: FeedPostRecord,
    val replyCount: Integer,
    val repostCount: Integer,
    val upvoteCount: Integer,
    val downvoteCount: Integer,
    val indexedAt: String, // TODO: datetime
//    val viewer: ViewerState
)
