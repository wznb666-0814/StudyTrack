package com.repea.studytrack.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repea.studytrack.repository.ForegroundMode
import com.repea.studytrack.ui.components.GlassCard
import com.repea.studytrack.viewmodel.UserPreferencesViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizationScreen(
    navController: NavController,
    viewModel: UserPreferencesViewModel = hiltViewModel()
) {
    val prefs by viewModel.preferences.collectAsState()
    val context = LocalContext.current
    var isAdjustingSlider by remember { mutableStateOf(false) }

    val pickWallpaperLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                viewModel.setWallpaperUri(uri.toString())
            }
        }
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("个性化", color = MaterialTheme.colorScheme.onSurface) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState(), enabled = !isAdjustingSlider),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("壁纸", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f), CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), CircleShape)
                                .clickable { pickWallpaperLauncher.launch(arrayOf("image/*")) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "选择壁纸", tint = MaterialTheme.colorScheme.onSurface)
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f), CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), CircleShape)
                                .clickable { viewModel.setWallpaperUri(null) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "清除壁纸", tint = MaterialTheme.colorScheme.onSurface)
                        }

                        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                            Text(
                                if (prefs.wallpaperUri.isNullOrBlank()) "未设置壁纸" else "已设置壁纸",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "选择后将作为全局背景",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("文字与图标颜色", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)

                    val options = listOf(
                        ForegroundMode.AUTO to ("自动" to "根据壁纸明暗自动反色"),
                        ForegroundMode.LIGHT to ("亮色" to "强制白色文字/图标"),
                        ForegroundMode.DARK to ("暗色" to "强制黑色文字/图标")
                    )

                    options.forEach { (mode, labelAndDesc) ->
                        val (label, desc) = labelAndDesc
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setForegroundMode(mode) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = prefs.foregroundMode == mode,
                                onClick = { viewModel.setForegroundMode(mode) }
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(label, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("液态玻璃参数", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    val glassEnabled = prefs.liquidGlassEnabled

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("启用液态玻璃", color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "关闭后可显著降低 GPU 压力",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = glassEnabled,
                            onCheckedChange = { viewModel.setLiquidGlassEnabled(it) }
                        )
                    }

                    var blurDp by remember(prefs.glassBlurRadiusDp) { mutableFloatStateOf(prefs.glassBlurRadiusDp) }
                    var blurSent by remember(prefs.glassBlurRadiusDp) { mutableFloatStateOf(prefs.glassBlurRadiusDp) }
                    Text("模糊强度：${blurDp.toInt()}dp", color = MaterialTheme.colorScheme.onSurface)
                    Slider(
                        value = blurDp,
                        onValueChange = {
                            isAdjustingSlider = true
                            blurDp = it
                            if (abs(blurDp - blurSent) >= 0.6f) {
                                blurSent = blurDp
                                viewModel.setGlassBlurRadiusDp(blurDp)
                            }
                        },
                        onValueChangeFinished = {
                            isAdjustingSlider = false
                            viewModel.setGlassBlurRadiusDp(blurDp)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = glassEnabled,
                        valueRange = 0f..40f
                    )

                    var refractionHeightDp by remember(prefs.glassRefractionHeightDp) { mutableFloatStateOf(prefs.glassRefractionHeightDp) }
                    var refractionHeightSent by remember(prefs.glassRefractionHeightDp) { mutableFloatStateOf(prefs.glassRefractionHeightDp) }
                    Text("折射高度：${refractionHeightDp.toInt()}dp", color = MaterialTheme.colorScheme.onSurface)
                    Slider(
                        value = refractionHeightDp,
                        onValueChange = {
                            isAdjustingSlider = true
                            refractionHeightDp = it
                            if (abs(refractionHeightDp - refractionHeightSent) >= 0.6f) {
                                refractionHeightSent = refractionHeightDp
                                viewModel.setGlassRefractionHeightDp(refractionHeightDp)
                            }
                        },
                        onValueChangeFinished = {
                            isAdjustingSlider = false
                            viewModel.setGlassRefractionHeightDp(refractionHeightDp)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = glassEnabled,
                        valueRange = 0f..40f
                    )

                    var refractionAmountDp by remember(prefs.glassRefractionAmountDp) { mutableFloatStateOf(prefs.glassRefractionAmountDp) }
                    var refractionAmountSent by remember(prefs.glassRefractionAmountDp) { mutableFloatStateOf(prefs.glassRefractionAmountDp) }
                    Text("折射幅度：${refractionAmountDp.toInt()}dp", color = MaterialTheme.colorScheme.onSurface)
                    Slider(
                        value = refractionAmountDp,
                        onValueChange = {
                            isAdjustingSlider = true
                            refractionAmountDp = it
                            if (abs(refractionAmountDp - refractionAmountSent) >= 0.9f) {
                                refractionAmountSent = refractionAmountDp
                                viewModel.setGlassRefractionAmountDp(refractionAmountDp)
                            }
                        },
                        onValueChangeFinished = {
                            isAdjustingSlider = false
                            viewModel.setGlassRefractionAmountDp(refractionAmountDp)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = glassEnabled,
                        valueRange = 0f..60f
                    )

                    var tintAlpha by remember(prefs.glassTintAlpha) { mutableFloatStateOf(prefs.glassTintAlpha) }
                    var tintSent by remember(prefs.glassTintAlpha) { mutableFloatStateOf(prefs.glassTintAlpha) }
                    Text("玻璃染色：${(tintAlpha * 100).toInt()}%", color = MaterialTheme.colorScheme.onSurface)
                    Slider(
                        value = tintAlpha,
                        onValueChange = {
                            isAdjustingSlider = true
                            tintAlpha = it
                            if (abs(tintAlpha - tintSent) >= 0.02f) {
                                tintSent = tintAlpha
                                viewModel.setGlassTintAlpha(tintAlpha)
                            }
                        },
                        onValueChangeFinished = {
                            isAdjustingSlider = false
                            viewModel.setGlassTintAlpha(tintAlpha)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = glassEnabled,
                        valueRange = 0f..0.35f
                    )

                    var borderAlpha by remember(prefs.glassBorderAlpha) { mutableFloatStateOf(prefs.glassBorderAlpha) }
                    var borderSent by remember(prefs.glassBorderAlpha) { mutableFloatStateOf(prefs.glassBorderAlpha) }
                    Text("边框强度：${(borderAlpha * 100).toInt()}%", color = MaterialTheme.colorScheme.onSurface)
                    Slider(
                        value = borderAlpha,
                        onValueChange = {
                            isAdjustingSlider = true
                            borderAlpha = it
                            if (abs(borderAlpha - borderSent) >= 0.02f) {
                                borderSent = borderAlpha
                                viewModel.setGlassBorderAlpha(borderAlpha)
                            }
                        },
                        onValueChangeFinished = {
                            isAdjustingSlider = false
                            viewModel.setGlassBorderAlpha(borderAlpha)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = glassEnabled,
                        valueRange = 0f..0.45f
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Vibrancy", color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "增强玻璃下的色彩层次",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = prefs.glassVibrancyEnabled,
                            onCheckedChange = { viewModel.setGlassVibrancyEnabled(it) },
                            enabled = glassEnabled
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("色差折射", color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "更明显的液态折射边缘",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = prefs.glassChromaticAberration,
                            onCheckedChange = { viewModel.setGlassChromaticAberration(it) },
                            enabled = glassEnabled
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

