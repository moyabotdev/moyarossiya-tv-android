package com.moyarossiya.tv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moyarossiya.tv.R
import com.moyarossiya.tv.data.ScheduleEntry
import com.moyarossiya.tv.ui.theme.MoyaBlue
import com.moyarossiya.tv.ui.theme.MoyaCardLight
import com.moyarossiya.tv.ui.theme.MoyaGold
import com.moyarossiya.tv.ui.theme.MoyaMuted
import com.moyarossiya.tv.ui.theme.MoyaNavy
import com.moyarossiya.tv.ui.theme.MoyaRed
import com.moyarossiya.tv.ui.theme.MoyaText

private enum class TvSection(val titleRes: Int) {
    LIVE(R.string.nav_live),
    PROGRAM(R.string.nav_program),
    ABOUT(R.string.nav_about),
}

internal val liveNavigationFocus = androidx.compose.ui.focus.FocusRequester()

@Composable
fun MoyaTvApp(programViewModel: ProgramViewModel = viewModel()) {
    val state by programViewModel.uiState.collectAsStateWithLifecycle()
    val player = rememberLivePlayer()
    val activity = androidx.compose.ui.platform.LocalContext.current as? android.app.Activity
    var section by rememberSaveable { mutableStateOf(TvSection.LIVE) }
    var fullScreen by rememberSaveable { mutableStateOf(false) }
    var selectedEntry by remember { mutableStateOf<ScheduleEntry?>(null) }

    androidx.compose.runtime.LaunchedEffect(fullScreen) {
        activity?.requestedOrientation = if (fullScreen) {
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    BackHandler(enabled = fullScreen || selectedEntry != null || section != TvSection.LIVE) {
        when {
            fullScreen -> fullScreen = false
            selectedEntry != null -> selectedEntry = null
            else -> section = TvSection.LIVE
        }
    }

    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(MoyaNavy, Color(0xFF1D2A42), Color(0xFF2C3D5C)),
                ),
            )
            .then(if (fullScreen) Modifier else Modifier.safeDrawingPadding()),
    ) {
        val compact = maxWidth < 700.dp
        if (fullScreen) {
            LiveScreen(
                state = state,
                player = player,
                fullScreen = true,
                onFullScreenChange = { fullScreen = it },
            )
        } else {
            val page: @Composable () -> Unit = {
                when (section) {
                    TvSection.LIVE -> LiveScreen(
                        state = state,
                        player = player,
                        fullScreen = false,
                        compact = compact,
                        onFullScreenChange = { fullScreen = it },
                    )
                    TvSection.PROGRAM -> ProgramScreen(
                        state = state,
                        selectedEntry = selectedEntry,
                        compact = compact,
                        onEntrySelected = { selectedEntry = it },
                        onRetry = programViewModel::retry,
                        onWatchLive = {
                            selectedEntry = null
                            section = TvSection.LIVE
                        },
                    )
                    TvSection.ABOUT -> AboutScreen(compact = compact)
                }
            }
            if (compact) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Tricolor()
                    CompactHeader()
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) { page() }
                    CompactNavigation(
                        selected = section,
                        onSelect = {
                            section = it
                            selectedEntry = null
                        },
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Tricolor()
                    Row(modifier = Modifier.fillMaxSize()) {
                        Navigation(
                            selected = section,
                            onSelect = {
                                section = it
                                selectedEntry = null
                            },
                        )
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) { page() }
                    }
                }
            }
        }
    }
}

@Composable
private fun Tricolor() {
    Row(modifier = Modifier.fillMaxWidth().height(4.dp)) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.White))
        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(MoyaBlue))
        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(MoyaRed))
    }
}

@Composable
private fun CompactHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = androidx.compose.ui.res.stringResource(R.string.logo_description),
            modifier = Modifier.size(44.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "MOYA ROSSIYA TV",
            color = MoyaText,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun CompactNavigation(selected: TvSection, onSelect: (TvSection) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.22f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        TvSection.entries.forEach { item ->
            NavigationItem(
                text = androidx.compose.ui.res.stringResource(item.titleRes),
                active = item == selected,
                isLive = item == TvSection.LIVE,
                compact = true,
                onClick = { onSelect(item) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun Navigation(selected: TvSection, onSelect: (TvSection) -> Unit) {
    Column(
        modifier = Modifier
            .width(220.dp)
            .fillMaxHeight()
            .background(Color.Black.copy(alpha = 0.18f))
            .padding(horizontal = 20.dp, vertical = 26.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = androidx.compose.ui.res.stringResource(R.string.logo_description),
                modifier = Modifier.size(64.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "MOYA",
                    color = MoyaText,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "ROSSIYA TV",
                    color = MoyaGold,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        Spacer(modifier = Modifier.height(44.dp))
        TvSection.entries.forEach { item ->
            NavigationItem(
                text = androidx.compose.ui.res.stringResource(item.titleRes),
                active = item == selected,
                isLive = item == TvSection.LIVE,
                onClick = { onSelect(item) },
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "24/7 LIVE",
            color = MoyaMuted,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun NavigationItem(
    text: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    isLive: Boolean = false,
) {
    var focused by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isLive) Modifier.focusRequester(liveNavigationFocus) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .tvAction(onClick),
        color = when {
            focused -> MoyaGold
            active -> MoyaCardLight
            else -> Color.Transparent
        },
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compact) 4.dp else 18.dp,
                    vertical = if (compact) 10.dp else 15.dp,
                ),
            color = if (focused) MoyaNavy else if (active) MoyaGold else MoyaText,
            style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.labelLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
    }
}
