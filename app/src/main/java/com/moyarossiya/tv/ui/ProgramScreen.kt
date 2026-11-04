package com.moyarossiya.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moyarossiya.tv.R
import com.moyarossiya.tv.data.ScheduleEntry
import com.moyarossiya.tv.ui.theme.MoyaCard
import com.moyarossiya.tv.ui.theme.MoyaCardLight
import com.moyarossiya.tv.ui.theme.MoyaGold
import com.moyarossiya.tv.ui.theme.MoyaMuted
import com.moyarossiya.tv.ui.theme.MoyaNavy
import com.moyarossiya.tv.ui.theme.MoyaRed
import com.moyarossiya.tv.ui.theme.MoyaText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

@Composable
fun ProgramScreen(
    state: ProgramUiState,
    selectedEntry: ScheduleEntry?,
    onEntrySelected: (ScheduleEntry?) -> Unit,
    onRetry: () -> Unit,
    onWatchLive: () -> Unit,
    compact: Boolean = false,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedEntry == null) {
            ProgramList(
                state = state,
                compact = compact,
                onEntrySelected = onEntrySelected,
                onRetry = onRetry,
            )
        } else {
            ScheduleDetail(
                entry = selectedEntry,
                compact = compact,
                onWatchLive = onWatchLive,
                onClose = { onEntrySelected(null) },
            )
        }
    }
}

@Composable
private fun ProgramList(
    state: ProgramUiState,
    compact: Boolean,
    onEntrySelected: (ScheduleEntry) -> Unit,
    onRetry: () -> Unit,
) {
    val listState = rememberLazyListState()
    val currentFocus = remember { FocusRequester() }
    var initialScrollDone by rememberSaveable { mutableStateOf(false) }
    val now = System.currentTimeMillis() / 1000
    val currentIndex = state.entries.indexOfFirst { now >= it.start && now < it.ende }
    val firstRelevant = if (currentIndex >= 0) {
        currentIndex
    } else {
        state.entries.indexOfFirst { it.ende > now }.coerceAtLeast(0)
    }
    val heading: @Composable () -> Unit = {
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.program_today),
            color = MoyaText,
            style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
        )
        MutedText(
            text = if (state.scheduleDate.isEmpty()) {
                androidx.compose.ui.res.stringResource(R.string.schedule_time_zone)
            } else {
                androidx.compose.ui.res.stringResource(R.string.schedule_date_time_zone, state.scheduleDate)
            },
        )
    }

    LaunchedEffect(firstRelevant, state.entries.size) {
        if (!initialScrollDone && state.entries.isNotEmpty()) {
            listState.scrollToItem(max(0, firstRelevant - 1))
            if (currentIndex >= 0) currentFocus.requestFocus()
            initialScrollDone = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = if (compact) 16.dp else 32.dp,
                vertical = if (compact) 12.dp else 24.dp,
            ),
    ) {
        if (compact) {
            heading()
            Spacer(modifier = Modifier.height(10.dp))
            TvButton(androidx.compose.ui.res.stringResource(R.string.refresh), onRetry)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) { heading() }
                TvButton(androidx.compose.ui.res.stringResource(R.string.refresh), onRetry)
            }
        }
        Spacer(modifier = Modifier.height(if (compact) 12.dp else 18.dp))
        state.error?.let { error ->
            val messageRes = when (error) {
                ProgramError.LIVE_INFO -> R.string.error_live_info
                ProgramError.SCHEDULE -> R.string.schedule_unavailable
            }
            Text(
                text = androidx.compose.ui.res.stringResource(messageRes),
                color = MoyaGold,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        when {
            state.isLoading && state.entries.isEmpty() -> {
                EmptyProgram(androidx.compose.ui.res.stringResource(R.string.schedule_loading), null)
            }
            state.entries.isEmpty() -> {
                EmptyProgram(androidx.compose.ui.res.stringResource(R.string.schedule_unavailable), onRetry)
            }
            else -> {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    itemsIndexed(
                        items = state.entries,
                        key = { _, entry -> "${entry.start}-${entry.titel}" },
                    ) { _, entry ->
                        val isCurrent = now >= entry.start && now < entry.ende
                        val isPast = entry.ende <= now
                        ScheduleRow(
                            entry = entry,
                            isCurrent = isCurrent,
                            isPast = isPast,
                            compact = compact,
                            onClick = { onEntrySelected(entry) },
                            modifier = if (isCurrent) Modifier.focusRequester(currentFocus) else Modifier,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleRow(
    entry: ScheduleEntry,
    isCurrent: Boolean,
    isPast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    var focused by remember { mutableStateOf(false) }
    val background = when {
        focused -> MoyaCardLight
        isCurrent -> MoyaGold.copy(alpha = 0.16f)
        else -> MoyaCard.copy(alpha = 0.8f)
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isPast && !focused) 0.48f else 1f)
            .onFocusChanged { focused = it.isFocused }
            .tvAction(onClick),
        color = background,
        shape = RoundedCornerShape(14.dp),
    ) {
        if (compact) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${formatTime(entry.start)} – ${formatTime(entry.ende)}",
                        modifier = Modifier.weight(1f),
                        color = if (isCurrent) MoyaGold else MoyaMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (isCurrent) LiveBadge()
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = entry.titel,
                    color = MoyaText,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${formatTime(entry.start)} – ${formatTime(entry.ende)}",
                    modifier = Modifier.width(176.dp),
                    color = if (isCurrent) MoyaGold else MoyaMuted,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = entry.titel,
                    modifier = Modifier.weight(1f),
                    color = MoyaText,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (isCurrent) {
                    Spacer(modifier = Modifier.width(16.dp))
                    LiveBadge()
                }
            }
        }
    }
}

@Composable
private fun ScheduleDetail(
    entry: ScheduleEntry,
    onWatchLive: () -> Unit,
    onClose: () -> Unit,
    compact: Boolean,
) {
    val buttonFocus = remember { FocusRequester() }
    val now = System.currentTimeMillis() / 1000
    val statusRes = when {
        now >= entry.start && now < entry.ende -> R.string.schedule_status_current
        entry.ende <= now -> R.string.schedule_status_past
        else -> R.string.schedule_status_upcoming
    }
    val duration = max(1L, (entry.ende - entry.start + 59) / 60)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MoyaNavy)
            .padding(
                horizontal = if (compact) 20.dp else 64.dp,
                vertical = if (compact) 20.dp else 44.dp,
            ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionLabel(androidx.compose.ui.res.stringResource(R.string.schedule_info))
            Spacer(modifier = Modifier.height(if (compact) 16.dp else 24.dp))
            Text(
                text = entry.titel,
                color = MoyaText,
                style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(if (compact) 16.dp else 22.dp))
            Text(
                text = androidx.compose.ui.res.stringResource(
                    R.string.schedule_time_duration,
                    formatTime(entry.start),
                    formatTime(entry.ende),
                    duration,
                ),
                color = MoyaGold,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            MutedText(androidx.compose.ui.res.stringResource(statusRes))
            Spacer(modifier = Modifier.height(if (compact) 24.dp else 36.dp))
            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TvButton(
                        text = androidx.compose.ui.res.stringResource(R.string.watch_live),
                        onClick = onWatchLive,
                        modifier = Modifier.fillMaxWidth().focusRequester(buttonFocus),
                        emphasized = true,
                    )
                    TvButton(
                        text = androidx.compose.ui.res.stringResource(R.string.back_to_program),
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TvButton(
                        text = androidx.compose.ui.res.stringResource(R.string.watch_live),
                        onClick = onWatchLive,
                        modifier = Modifier.focusRequester(buttonFocus),
                        emphasized = true,
                    )
                    TvButton(androidx.compose.ui.res.stringResource(R.string.back_to_program), onClose)
                }
            }
        }
        LaunchedEffect(Unit) { buttonFocus.requestFocus() }
    }
}

@Composable
private fun EmptyProgram(message: String, onRetry: (() -> Unit)?) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = message, color = MoyaMuted, style = MaterialTheme.typography.bodyLarge)
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(18.dp))
            TvButton(androidx.compose.ui.res.stringResource(R.string.retry), onRetry, emphasized = true)
        }
    }
}

private fun formatTime(epochSeconds: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Berlin")
    }.format(Date(epochSeconds * 1000))
}
