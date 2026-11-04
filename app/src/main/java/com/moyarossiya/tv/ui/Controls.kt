package com.moyarossiya.tv.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.moyarossiya.tv.ui.theme.MoyaCardLight
import com.moyarossiya.tv.ui.theme.MoyaGold
import com.moyarossiya.tv.ui.theme.MoyaMuted
import com.moyarossiya.tv.ui.theme.MoyaNavy
import com.moyarossiya.tv.ui.theme.MoyaRed
import com.moyarossiya.tv.ui.theme.MoyaText

private val activationKeys = setOf(
    Key.DirectionCenter,
    Key.Enter,
    Key.NumPadEnter,
    Key.ButtonA,
)

@Composable
fun Modifier.tvAction(onClick: () -> Unit): Modifier {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.025f else 1f, label = "focusScale")
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .border(
            width = if (focused) 3.dp else 1.dp,
            color = if (focused) MoyaGold else Color.White.copy(alpha = 0.12f),
            shape = RoundedCornerShape(14.dp),
        )
        .onFocusChanged { focused = it.isFocused }
        .onKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown && event.key in activationKeys) {
                onClick()
                true
            } else {
                false
            }
        }
        .pointerInput(onClick) {
            detectTapGestures { onClick() }
        }
        .semantics {
            onClick {
                onClick()
                true
            }
        }
        .focusable()
}

@Composable
fun TvButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    var focused by remember { mutableStateOf(false) }
    val background = when {
        focused -> MoyaGold
        emphasized -> MoyaRed
        else -> MoyaCardLight
    }
    Surface(
        modifier = modifier
            .onFocusChanged { focused = it.isFocused }
            .tvAction(onClick),
        color = background,
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 13.dp),
            color = if (focused) MoyaNavy else MoyaText,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun LiveBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MoyaRed,
        shape = RoundedCornerShape(50),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .background(Color.White, CircleShape),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "LIVE",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = MoyaGold,
        style = MaterialTheme.typography.labelLarge,
    )
}

@Composable
fun MutedText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = MoyaMuted,
        style = MaterialTheme.typography.bodyMedium,
    )
}
