@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.moyarossiya.tv.ui

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.moyarossiya.tv.BuildConfig
import com.moyarossiya.tv.R
import com.moyarossiya.tv.ui.theme.MoyaGold
import com.moyarossiya.tv.ui.theme.MoyaMuted
import com.moyarossiya.tv.ui.theme.MoyaRed
import com.moyarossiya.tv.ui.theme.MoyaText
import okhttp3.OkHttpClient
import okhttp3.JavaNetCookieJar
import java.net.CookieManager
import java.net.CookiePolicy

@Composable
fun rememberLivePlayer(): ExoPlayer {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val player = remember {
        val cookieManager = CookieManager(null, CookiePolicy.ACCEPT_ALL)
        val client = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .build()
        val dataSourceFactory = OkHttpDataSource.Factory(client)
            .setUserAgent("MoyaRossiyaTV/0.1 AndroidTV")
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(BuildConfig.STREAM_URL))
                playWhenReady = true
                prepare()
            }
    }

    DisposableEffect(lifecycleOwner, player) {
        var resumeOnStart = true
        var liveRecoveryAttempts = 0
        val recoveryListener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) liveRecoveryAttempts = 0
            }

            override fun onPlayerError(playbackError: PlaybackException) {
                val staleLiveSession = generateSequence(playbackError as Throwable?) { it.cause }
                    .filterIsInstance<androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException>()
                    .any { it.responseCode == 401 || it.responseCode == 404 }
                val recoverable = playbackError.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW ||
                    staleLiveSession
                if (recoverable && liveRecoveryAttempts < 2) {
                    liveRecoveryAttempts++
                    player.seekToDefaultPosition()
                    player.prepare()
                }
            }
        }
        player.addListener(recoveryListener)
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (resumeOnStart) player.playWhenReady = true
                }
                Lifecycle.Event.ON_STOP -> {
                    resumeOnStart = player.playWhenReady
                    player.pause()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.removeListener(recoveryListener)
            player.release()
        }
    }
    return player
}

@Composable
fun LiveScreen(
    state: ProgramUiState,
    player: ExoPlayer,
    fullScreen: Boolean,
    onFullScreenChange: (Boolean) -> Unit,
    compact: Boolean = false,
) {
    if (fullScreen) {
        FullScreenLive(
            state = state,
            player = player,
            onClose = { onFullScreenChange(false) },
        )
    } else {
        EmbeddedLive(
            state = state,
            player = player,
            compact = compact,
            onFullScreen = { onFullScreenChange(true) },
        )
    }
}

@Composable
private fun EmbeddedLive(
    state: ProgramUiState,
    player: ExoPlayer,
    compact: Boolean,
    onFullScreen: () -> Unit,
) {
    val playerFocus = remember { FocusRequester() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (compact) Modifier.verticalScroll(rememberScrollState()) else Modifier)
            .padding(
                horizontal = if (compact) 16.dp else 32.dp,
                vertical = if (compact) 12.dp else 24.dp,
            ),
    ) {
        val playerModifier = if (compact) {
            Modifier.fillMaxWidth().aspectRatio(16f / 9f)
        } else {
            Modifier.weight(1f).fillMaxWidth()
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel(
                text = androidx.compose.ui.res.stringResource(
                    if (compact) R.string.live_now_short else R.string.live_now_on_channel,
                ),
                modifier = Modifier.weight(1f),
            )
            LiveBadge()
        }
        Spacer(modifier = Modifier.height(if (compact) 10.dp else 14.dp))
        LivePlayerSurface(
            player = player,
            onActivate = onFullScreen,
            modifier = playerModifier
                .focusProperties { left = liveNavigationFocus }
                .focusRequester(playerFocus),
        )
        Spacer(modifier = Modifier.height(if (compact) 12.dp else 16.dp))
        NowNext(state, compact)
        Spacer(modifier = Modifier.height(12.dp))
        PlaybackButtons(
            player = player,
            onFullScreen = onFullScreen,
        )
        androidx.compose.runtime.LaunchedEffect(Unit) {
            playerFocus.requestFocus()
        }
    }
}

@Composable
private fun FullScreenLive(
    state: ProgramUiState,
    player: ExoPlayer,
    onClose: () -> Unit,
) {
    val playerFocus = remember { FocusRequester() }
    var controlsVisible by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(playerFocus)
            .onKeyEvent { event ->
                if (
                    event.type == KeyEventType.KeyDown &&
                    event.key in setOf(Key.DirectionCenter, Key.Enter, Key.NumPadEnter, Key.ButtonA)
                ) {
                    controlsVisible = !controlsVisible
                    true
                } else {
                    false
                }
            }
            .focusable(),
    ) {
        LivePlayerSurface(
            player = player,
            onActivate = { controlsVisible = !controlsVisible },
            modifier = Modifier.fillMaxSize(),
        )
        if (controlsVisible) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent),
                        ),
                    )
                    .statusBarsPadding()
                    .padding(32.dp)
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Moya Rossiya TV",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    color = MoyaText,
                )
                LiveBadge()
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                        ),
                    )
                    .navigationBarsPadding()
                    .padding(32.dp)
                    .align(Alignment.BottomCenter),
            ) {
                val defaultTitle = androidx.compose.ui.res.stringResource(R.string.live_default_title)
                Text(
                    text = state.nowTitle.ifEmpty { defaultTitle },
                    color = MoyaText,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (state.nextTitle.isNotEmpty()) {
                    MutedText(
                        androidx.compose.ui.res.stringResource(
                            R.string.live_next_with_title,
                            state.nextTitle,
                        ),
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    PlayPauseButton(player)
                    TvButton(androidx.compose.ui.res.stringResource(R.string.back_to_overview), onClose)
                }
            }
        }
    }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        playerFocus.requestFocus()
    }
    androidx.compose.runtime.LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            kotlinx.coroutines.delay(5_000)
            controlsVisible = false
            playerFocus.requestFocus()
        }
    }
}

@Composable
private fun LivePlayerSurface(
    player: ExoPlayer,
    onActivate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var error by remember(player) {
        mutableStateOf(if (player.playerError == null) null else R.string.stream_error)
    }
    var buffering by remember(player) {
        mutableStateOf(player.playbackState == Player.STATE_BUFFERING)
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                buffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY || buffering) error = null
            }

            override fun onPlayerError(playbackError: PlaybackException) {
                buffering = false
                error = R.string.stream_error
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.Black)
            .tvAction(onActivate),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setBackgroundColor(AndroidColor.BLACK)
                    this.player = player
                }
            },
            update = { view ->
                view.player = player
                view.setOnClickListener { onActivate() }
            },
            modifier = Modifier.fillMaxSize(),
        )
        if (buffering && error == null) {
            Surface(color = Color.Black.copy(alpha = 0.65f), shape = RoundedCornerShape(12.dp)) {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.loading_stream),
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp),
                    color = MoyaText,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        error?.let { messageRes ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.86f))
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(messageRes),
                    color = MoyaText,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(16.dp))
                TvButton(
                    text = androidx.compose.ui.res.stringResource(R.string.retry),
                    onClick = {
                        error = null
                        buffering = true
                        player.seekToDefaultPosition()
                        player.prepare()
                        player.play()
                    },
                    emphasized = true,
                )
            }
        }
    }
}

@Composable
private fun NowNext(state: ProgramUiState, compact: Boolean) {
    if (compact) {
        Column(modifier = Modifier.fillMaxWidth()) {
            NowText(state)
            if (state.nextTitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                NextText(state)
            }
        }
    } else {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1.25f)) { NowText(state) }
            if (state.nextTitle.isNotEmpty()) {
                Spacer(modifier = Modifier.width(24.dp))
                Column(modifier = Modifier.weight(1f)) { NextText(state) }
            }
        }
    }
}

@Composable
private fun NowText(state: ProgramUiState) {
    val defaultNow = androidx.compose.ui.res.stringResource(R.string.live_default_now)
    Text(
        text = androidx.compose.ui.res.stringResource(R.string.live_now),
        color = MoyaRed,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
    )
    Text(
        text = state.nowTitle.ifEmpty { defaultNow },
        color = MoyaText,
        style = MaterialTheme.typography.titleMedium,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun NextText(state: ProgramUiState) {
    Text(
        text = androidx.compose.ui.res.stringResource(R.string.live_next),
        color = MoyaGold,
        style = MaterialTheme.typography.labelLarge,
    )
    Text(
        text = state.nextTitle,
        color = MoyaMuted,
        style = MaterialTheme.typography.bodyLarge,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun PlaybackButtons(player: ExoPlayer, onFullScreen: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        PlayPauseButton(player)
        TvButton(androidx.compose.ui.res.stringResource(R.string.fullscreen), onFullScreen, emphasized = true)
    }
}

@Composable
private fun PlayPauseButton(player: ExoPlayer) {
    var playing by remember(player) { mutableStateOf(player.isPlaying) }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playing = isPlaying
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }
    TvButton(
        text = androidx.compose.ui.res.stringResource(if (playing) R.string.pause else R.string.resume),
        onClick = { player.playWhenReady = !player.playWhenReady },
    )
}
