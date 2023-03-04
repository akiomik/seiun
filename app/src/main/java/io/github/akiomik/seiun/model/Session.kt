package com.example.catpaw.models

data class Session(val accessJwt: String, val refreshJwt: String, val handle: String, val did: String)
