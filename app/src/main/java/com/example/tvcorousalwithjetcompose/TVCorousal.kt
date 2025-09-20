@file:OptIn(ExperimentalTvMaterial3Api::class)
package com.example.tvcorousalwithjetcompose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.ExperimentalTvMaterial3Api
import kotlinx.coroutines.delay
import androidx.media3.common.Player as Media3Player
import androidx.tv.material3.Card as TvCard
import androidx.tv.material3.CardDefaults as TvCardDefaults
import androidx.tv.material3.Icon as TvIcon
import androidx.tv.material3.MaterialTheme as TvMaterialTheme
import androidx.tv.material3.Surface as TvSurface
import androidx.tv.material3.SurfaceDefaults as TvSurfaceDefaults
import androidx.tv.material3.Text as TvText

// Define Orange color for selected items
val Orange = Color(0xFFFFA500)

// Data classes for carousel items
sealed class CarouselItem {
    data class ImageItem(
        val imageRes: Int,
        val duration: Long = 5000L // 5 seconds
    ) : CarouselItem()

    data class VideoItem(
        val videoResId: Int, // Use Int resource ID
        val duration: Long = 0L // Will be set by video player
    ) : CarouselItem()
}

// Bottom navigation item
data class BottomNavItem(
    val name: String,
    val icon: ImageVector
)

@Composable
fun TVCarouselApp() {
    var selectedBottomIndex by remember { mutableIntStateOf(0) }

    // Sample carousel data - Ensure you have sample_video1 and sample_video2 in res/raw
    val carouselItems = remember {
        listOf(
            CarouselItem.ImageItem(R.mipmap.image_1),
            CarouselItem.VideoItem(R.raw.sample_video1), // Replace with your actual raw resource
            CarouselItem.ImageItem(R.mipmap.image_2),
            CarouselItem.VideoItem(R.raw.sample_video2), // Replace with your actual raw resource
      /*      CarouselItem.ImageItem(R.mipmap.image_1),
            // Option 1: Online video (recommended for demo)
            CarouselItem.VideoItem("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
            CarouselItem.ImageItem(R.mipmap.image_2),
            // Option 2: Another online video
            CarouselItem.VideoItem("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
            // Option 3: Raw resource (if you have local videos)
            // CarouselItem.VideoItem("android.resource://com.example.tvcarousel/${R.raw.sample_video}"),
            // Option 4: Asset folder video
            // CarouselItem.VideoItem("file:///android_asset/videos/local_video.mp4"),
       */ )
    }

    val bottomNavItems = listOf(
        BottomNavItem("Home", Icons.Default.Home),
        BottomNavItem("Settings", Icons.Default.Settings)
    )

    TvMaterialTheme { // Use TV Material Theme
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TvMaterialTheme.colorScheme.surface) // Use theme color
        ) {
            when (selectedBottomIndex) {
                0 -> HomeScreen(carouselItems)
                1 -> SettingsScreen()
            }

            BottomNavigationBar(
                items = bottomNavItems,
                selectedIndex = selectedBottomIndex,
                onItemSelected = { selectedBottomIndex = it },
                modifier = Modifier.align(Alignment.BottomCenter) // Aligns the whole nav bar
            )
        }
    }
}

@Composable
fun HomeScreen(carouselItems: List<CarouselItem>) {
    Box(modifier = Modifier.fillMaxSize()) {
        CarouselWithIndicators(
            items = carouselItems,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun SettingsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvMaterialTheme.colorScheme.surface), // Use theme color
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TvText(
                text = "Settings",
                style = TvMaterialTheme.typography.headlineMedium,
            )
            TvText(
                text = "Configure your TV app preferences",
                style = TvMaterialTheme.typography.bodyLarge,
                color = TvMaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CarouselWithIndicators(
    items: List<CarouselItem>,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var videoProgress by remember { mutableFloatStateOf(0f) }
    var isVideoPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(currentIndex, items) {
        val currentItem = items.getOrNull(currentIndex)
        if (currentItem is CarouselItem.ImageItem) {
            delay(currentItem.duration)
            if (items.isNotEmpty()) { // Ensure items is not empty before modulo
                currentIndex = (currentIndex + 1) % items.size
            }
        }
    }

    Box(modifier = modifier) {
        when (val currentItem = items.getOrNull(currentIndex)) {
            is CarouselItem.ImageItem -> {
                ImageSlide(
                    imageRes = currentItem.imageRes,
                    modifier = Modifier.fillMaxSize()
                )
            }
            is CarouselItem.VideoItem -> {
                VideoSlide(
                    videoResId = currentItem.videoResId,
                    modifier = Modifier.fillMaxSize(),
                    onProgressUpdate = { progress, _ ->
                        videoProgress = progress
                    },
                    onVideoCompleted = {
                        if (items.isNotEmpty()) { // Ensure items is not empty
                            currentIndex = (currentIndex + 1) % items.size
                            videoProgress = 0f
                        }
                    },
                    onPlayingStateChanged = { isPlaying ->
                        isVideoPlaying = isPlaying
                    }
                )
            }
            null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    TvText(
                        text = "No content available",
                        style = TvMaterialTheme.typography.headlineMedium
                    )
                }
            }
        }

        if (items.isNotEmpty()) { // Only show indicators if there are items
            CarouselIndicators(
                items = items,
                currentIndex = currentIndex,
                videoProgress = videoProgress,
                isVideoPlaying = isVideoPlaying,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(32.dp)
                    .zIndex(10f)
            )
        }
    }
}

@Composable
fun ImageSlide(
    imageRes: Int,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = "Carousel image",
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

@Composable
fun VideoSlide(
    videoResId: Int,
    modifier: Modifier = Modifier,
    onProgressUpdate: (Float, Long) -> Unit = { _, _ -> },
    onVideoCompleted: () -> Unit = {},
    onPlayingStateChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    LaunchedEffect(videoResId) {
        exoPlayer?.release()
        val packageName = context.packageName
        val videoUri = "android.resource://$packageName/$videoResId".toUri()
        val player = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            playWhenReady = true
        }
        exoPlayer = player

        val listener = object : Media3Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Media3Player.STATE_ENDED) {
                    onVideoCompleted()
                }
                onPlayingStateChanged(player.isPlaying)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                onPlayingStateChanged(isPlaying)
            }
        }
        player.addListener(listener)

        var isProgressLoopActive = true
        while (isProgressLoopActive) { // Loop while player is active or being set up
            if (player.playbackState == Media3Player.STATE_IDLE || player.playbackState == Media3Player.STATE_BUFFERING){
                 delay(100)
                 continue
            }
            if (!player.isPlaying && player.playbackState != Media3Player.STATE_READY) {
                isProgressLoopActive = false // Exit loop if not playing and not ready (e.g. error or ended without event)
                continue
            }
            val currentPosition = player.currentPosition
            val duration = player.duration
            if (duration > 0) {
                onProgressUpdate(currentPosition.toFloat() / duration, duration)
            }
             if (!player.isPlaying && player.playbackState == Media3Player.STATE_ENDED) {
                isProgressLoopActive = false // Ensure loop exits if ended event was missed
            }
            delay(100) // Update interval
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                this.player = exoPlayer
            }
        },
        update = { view ->
            view.player = exoPlayer
        },
        modifier = modifier
    )
}

@Composable
fun CarouselIndicators(
    items: List<CarouselItem>,
    currentIndex: Int,
    videoProgress: Float,
    isVideoPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            when (item) {
                is CarouselItem.ImageItem -> {
                    ImageIndicatorDot(
                        isActive = index == currentIndex,
                        modifier = Modifier.size(12.dp)
                    )
                }
                is CarouselItem.VideoItem -> {
                    VideoIndicatorDot(
                        isActive = index == currentIndex,
                        progress = if (index == currentIndex) videoProgress else 0f,
                        isPlaying = index == currentIndex && isVideoPlaying,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ImageIndicatorDot(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.3f,
        animationSpec = tween(300),
        label = "ImageIndicatorAlpha"
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = animatedAlpha))
    )
}

@Composable
fun VideoIndicatorDot(
    isActive: Boolean,
    progress: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.3f,
        animationSpec = tween(300),
        label = "VideoIndicatorAlpha"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color.White.copy(alpha = animatedAlpha * 0.3f))
        )

        if (isActive && isPlaying) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = Color.White.copy(alpha = animatedAlpha),
                strokeWidth = 2.dp,
                trackColor = Color.Transparent
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize(0.6f)
                .clip(CircleShape)
                .background(
                    if (isActive) Color.Red.copy(alpha = animatedAlpha)
                    else Color.White.copy(alpha = animatedAlpha)
                )
        )

        if (isActive) {
            val icon = if (isPlaying) "▶" else "⏸"
            TvText(
                text = icon,
                color = Color.White,
                style = TvMaterialTheme.typography.labelSmall
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun BottomNavigationBar(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier // This modifier from TVCarouselApp is Modifier.align(Alignment.BottomCenter)
) {
    TvSurface(
        modifier = modifier // Applied Modifier.align(Alignment.BottomCenter) first
            .padding(16.dp), // Then padding is applied for the content area within the surface
        colors = TvSurfaceDefaults.colors(
            containerColor = Color.Gray.copy(alpha = 0.5f) // Transparent gray background
        ),
        shape = RoundedCornerShape(12.dp) // Rounded corners for the surface itself
    ) {
        Row(
            // Row will wrap content by default if no size modifiers are present
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                BottomNavCard(
                    item = item,
                    isSelected = index == selectedIndex,
                    onClick = { onItemSelected(index) }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun BottomNavCard(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (isSelected) Orange else Color.White

    TvCard(
        onClick = onClick,
        modifier = Modifier.padding(0.dp), // Card itself does not need extra padding beyond what Row provides
        colors = TvCardDefaults.colors(
            containerColor = Color.Transparent, // Card background is transparent
            // contentColor can be set here if TvCard respects it for all children,
            // but explicitly setting on TvIcon and TvText is safer.
        ),
        shape = TvCardDefaults.shape(shape = RoundedCornerShape(8.dp)) // Standard TV card shape, can be less rounded
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), // Padding inside each card
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TvIcon(
                imageVector = item.icon,
                contentDescription = item.name,
                tint = contentColor, // Use the determined content color
                modifier = Modifier.size(24.dp)
            )
            TvText(
                text = item.name,
                color = contentColor, // Use the determined content color
                style = TvMaterialTheme.typography.labelMedium
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1920dp,height=1080dp,dpi=240")
@Composable
fun TVCarouselAppPreview() {
    TvMaterialTheme { // Use TV Material Theme for Preview
        TVCarouselApp()
    }
}
