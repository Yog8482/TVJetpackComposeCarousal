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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
                modifier = Modifier.align(Alignment.BottomCenter)
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
    // videoDuration is implicitly handled by progress (0f to 1f)
    var isVideoPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(currentIndex, items) {
        val currentItem = items.getOrNull(currentIndex)
        if (currentItem is CarouselItem.ImageItem) {
            delay(currentItem.duration)
            currentIndex = (currentIndex + 1) % items.size
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
                    onProgressUpdate = { progress, _ -> // duration from player not strictly needed if progress is 0-1
                        videoProgress = progress
                    },
                    onVideoCompleted = {
                        currentIndex = (currentIndex + 1) % items.size
                        videoProgress = 0f
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

        // Progress tracking
        while (player.isPlaying) { // Check isPlaying for active progress
            val currentPosition = player.currentPosition
            val duration = player.duration
            if (duration > 0) {
                onProgressUpdate(currentPosition.toFloat() / duration, duration)
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
                this.player = exoPlayer // Assign the player instance
            }
        },
        update = { view -> // Update player in PlayerView if it changes
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
    // videoDuration: Long, // Removed as progress is 0-1
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
            CircularProgressIndicator( // Use M3 CircularProgressIndicator
                progress = { progress }, // Updated to lambda
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
            val icon = if (isPlaying) "▶" else "⏸" // Simple text for play/pause
            TvText(
                text = icon,
                color = Color.White,
                style = TvMaterialTheme.typography.labelSmall // Use TV Material Theme
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
    modifier: Modifier = Modifier
) {
    TvSurface( // Use TV Surface
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = TvSurfaceDefaults.colors(
            containerColor = Color.Transparent // Changed to full transparent
        ),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp) // Corrected: Direct Shape usage
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
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
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) TvMaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f) else Color.Transparent,
        animationSpec = tween(300), // Explicit type for tween
        label = "BottomNavCardBackground"
    )
    val contentColor = if (isSelected) TvMaterialTheme.colorScheme.onSurface else TvMaterialTheme.colorScheme.onSurfaceVariant


    TvCard( // Use TV Card
        onClick = onClick,
        modifier = Modifier.padding(4.dp),
        colors = TvCardDefaults.colors(containerColor = backgroundColor),
        shape = TvCardDefaults.shape(shape = RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TvIcon( // Use TV Icon
                imageVector = item.icon,
                contentDescription = item.name,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            TvText( // Use TV Text
                text = item.name,
                color = contentColor,
                style = TvMaterialTheme.typography.labelMedium // Use TV Material Theme
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
