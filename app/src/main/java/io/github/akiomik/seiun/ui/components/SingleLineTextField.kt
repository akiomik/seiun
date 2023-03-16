package io.github.akiomik.seiun.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun SingleLineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    label: @Composable () -> Unit = {},
    placeholder: @Composable () -> Unit = {},
    prefix: @Composable () -> Unit = {},
    suffix: @Composable () -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    TextField(
        value = value,
        onValueChange = { onValueChange(it.replace("\n", "")) },
        label = label,
        placeholder = placeholder,
        keyboardOptions = keyboardOptions,
        modifier = modifier,
        prefix = prefix,
        suffix = suffix,
        visualTransformation = visualTransformation,
        maxLines = 1,
        singleLine = true
    )
}
