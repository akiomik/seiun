package io.github.akiomik.seiun

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.akiomik.seiun.ui.app.App

class SeiunActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val from = intent.getStringExtra("from")
        Log.d(SeiunApplication.TAG, "from = $from")

        setContent {
            App(from)
        }
    }
}
