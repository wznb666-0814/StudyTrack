package com.repea.studytrack

import android.graphics.ImageDecoder
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.repea.studytrack.repository.ForegroundMode
import com.repea.studytrack.ui.components.BottomNavigationBar
import com.repea.studytrack.ui.components.LiquidBackground
import com.repea.studytrack.ui.components.ProvideLiquidGlass
import com.repea.studytrack.ui.components.LiquidGlassParams
import com.repea.studytrack.ui.navigation.NavGraph
import com.repea.studytrack.ui.theme.StudyTrackTheme
import com.repea.studytrack.viewmodel.MainViewModel
import com.repea.studytrack.viewmodel.UserPreferencesViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false) // Ensure edge-to-edge
        setContent {
            AppRoot()
        }
    }
}

@Composable
fun AppRoot(
    prefsViewModel: UserPreferencesViewModel = hiltViewModel()
) {
    val prefs by prefsViewModel.preferences.collectAsState()
    val wallpaperAnalysis by rememberWallpaperAnalysis(
        wallpaperUri = prefs.wallpaperUri,
        defaultWallpaperResId = R.drawable.bizhi
    )
    val contentOnDarkBackground = when (prefs.foregroundMode) {
        ForegroundMode.AUTO -> wallpaperAnalysis.isDark
        ForegroundMode.LIGHT -> true
        ForegroundMode.DARK -> false
    }

    StudyTrackTheme(
        contentOnDarkBackground = contentOnDarkBackground,
        primaryOverride = wallpaperAnalysis.complementaryPrimary
    ) {
        MainScreen(userPrefs = prefs)
    }
}

@Composable
private fun rememberWallpaperAnalysis(
    wallpaperUri: String?,
    defaultWallpaperResId: Int
): androidx.compose.runtime.State<WallpaperAnalysis> {
    val context = LocalContext.current
    return produceState(
        initialValue = WallpaperAnalysis(null, null),
        key1 = wallpaperUri,
        key2 = defaultWallpaperResId
    ) {
        value = runCatching {
            val bitmap = wallpaperUri?.takeIf { it.isNotBlank() }?.let { uriString ->
                val uri = Uri.parse(uriString)
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.setTargetSize(96, 96)
                }
            } ?: run {
                val source = ImageDecoder.createSource(context.resources, defaultWallpaperResId)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.setTargetSize(96, 96)
                }
            }

            // 采样整张壁纸，并额外加权中心区域（通常文字与图标集中在这里）
            val lums = ArrayList<Double>(600)
            val centerLums = ArrayList<Double>(200)
            var sumR = 0L
            var sumG = 0L
            var sumB = 0L
            var count = 0
            val w = bitmap.width
            val h = bitmap.height
            val stepX = (w / 24).coerceAtLeast(1)
            val stepY = (h / 24).coerceAtLeast(1)
            val centerLeft = w * 0.25
            val centerRight = w * 0.75
            val centerTop = h * 0.2
            val centerBottom = h * 0.8

            var y = 0
            while (y < h) {
                var x = 0
                while (x < w) {
                    val c = bitmap.getPixel(x, y)
                    val r = (c shr 16) and 0xFF
                    val g = (c shr 8) and 0xFF
                    val b = c and 0xFF
                    sumR += r
                    sumG += g
                    sumB += b
                    val lum = (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0
                    lums.add(lum)

                    if (x in centerLeft.toInt()..centerRight.toInt() &&
                        y in centerTop.toInt()..centerBottom.toInt()
                    ) {
                        centerLums.add(lum)
                    }

                    count++
                    x += stepX
                }
                y += stepY
            }

            if (lums.isEmpty() || count == 0) return@runCatching WallpaperAnalysis(null, null)

            val avgR = (sumR / count).toInt().coerceIn(0, 255)
            val avgG = (sumG / count).toInt().coerceIn(0, 255)
            val avgB = (sumB / count).toInt().coerceIn(0, 255)

            lums.sort()
            val medianLum = lums[lums.size / 2]
            val avgLum = (0.2126 * avgR + 0.7152 * avgG + 0.0722 * avgB) / 255.0
            val darkRatio = lums.count { it < 0.45 } / lums.size.toDouble()

            val centerLum = centerLums.takeIf { it.size >= 8 }?.sorted()?.let { center ->
                center[center.size / 2]
            }

            // 以中心亮度为主，整图亮度为辅，决定是否需要“反色”前景
            val effectiveLum = centerLum ?: medianLum
            val isDark = when {
                darkRatio > 0.7 -> true
                darkRatio < 0.3 -> false
                effectiveLum < 0.5 && avgLum < 0.6 -> true
                effectiveLum > 0.6 && avgLum > 0.55 -> false
                else -> effectiveLum < 0.55
            }

            val hsv = FloatArray(3)
            AndroidColor.RGBToHSV(avgR, avgG, avgB, hsv)
            val hue = (hsv[0] + 180f) % 360f
            val satBase = hsv[1].coerceIn(0.2f, 0.9f)
            val sat = (0.35f + satBase * 0.35f).coerceIn(0.35f, 0.7f)
            val value = if (isDark) 0.78f else 0.9f
            val complementary = Color(AndroidColor.HSVToColor(floatArrayOf(hue, sat, value)))

            WallpaperAnalysis(isDark, complementary)
        }.getOrDefault(WallpaperAnalysis(null, null))
    }
}

data class WallpaperAnalysis(
    val isDark: Boolean?,
    val complementaryPrimary: Color?
)

@Composable
fun MainScreen(
    userPrefs: com.repea.studytrack.repository.UserPreferencesState,
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val isFirstLaunch by viewModel.isFirstLaunch.collectAsState(initial = true)

    // 在部分模拟器 / 驱动上，LiquidGlass 的 LayerBackdrop 可能导致 RenderThread SIGSEGV，
    // 这里对模拟器做兼容处理：模拟器上强制关闭 backdrop，真机仍按用户设置启用。
    val isProbablyEmulator =
        Build.FINGERPRINT.startsWith("generic", ignoreCase = true) ||
            Build.FINGERPRINT.lowercase().contains("emulator") ||
            Build.MODEL.contains("google_sdk", ignoreCase = true) ||
            Build.MODEL.contains("sdk_gphone", ignoreCase = true) ||
            Build.MODEL.contains("Emulator", ignoreCase = true) ||
            Build.MODEL.contains("Android SDK built for x86", ignoreCase = true) ||
            Build.HARDWARE.contains("goldfish", ignoreCase = true) ||
            Build.HARDWARE.contains("ranchu", ignoreCase = true) ||
            (Build.BRAND.startsWith("generic", ignoreCase = true) &&
                Build.DEVICE.startsWith("generic", ignoreCase = true))

    val enableBackdrop = userPrefs.liquidGlassEnabled && !isProbablyEmulator

    val backdrop = if (enableBackdrop) rememberLayerBackdrop() else null

    val glassParams = LiquidGlassParams(
        enabled = userPrefs.liquidGlassEnabled,
        blurRadiusDp = userPrefs.glassBlurRadiusDp,
        refractionHeightDp = userPrefs.glassRefractionHeightDp,
        refractionAmountDp = userPrefs.glassRefractionAmountDp,
        tintAlpha = userPrefs.glassTintAlpha,
        borderAlpha = userPrefs.glassBorderAlpha,
        vibrancyEnabled = userPrefs.glassVibrancyEnabled,
        chromaticAberration = userPrefs.glassChromaticAberration
    )

    ProvideLiquidGlass(backdrop = backdrop, params = glassParams) {
        LiquidBackground(
            backdrop = backdrop,
            wallpaperUri = userPrefs.wallpaperUri,
            defaultWallpaperResId = R.drawable.bizhi
        ) {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) },
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .statusBarsPadding()
                ) {
                    NavGraph(
                        navController = navController,
                        startDestination = if (isFirstLaunch) "welcome" else "home",
                        onOnboardingComplete = { viewModel.completeOnboarding() }
                    )
                }
            }
        }
    }
}
