package io.github.akiomik.seiun.ui.components

import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.util.LinkifyCompat
import io.github.akiomik.seiun.R

// https://stackoverflow.com/a/68670583/1918609
@Composable
fun LinkableText(text: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val customLinkifyTextView = remember { TextView(context) }

    AndroidView(modifier = modifier, factory = { customLinkifyTextView }) { textView ->
        textView.text = text
        LinkifyCompat.addLinks(
            textView,
            Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS
        )
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.setTextAppearance(R.style.linkable_text)
    }
}
