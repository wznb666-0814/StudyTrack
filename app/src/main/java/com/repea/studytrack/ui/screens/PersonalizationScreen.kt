package com.repea.studytrack.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repea.studytrack.repository.AppThemeStyle
import com.repea.studytrack.ui.components.GlassCard
import com.repea.studytrack.ui.components.StudySectionHeader
import com.repea.studytrack.ui.components.StudySettingRow
import com.repea.studytrack.viewmodel.UserPreferencesViewModel
import coil.compose.AsyncImage
import kotlin.math.abs

@Composable
fun PersonalizationScreen(
    navController: NavController,
    viewModel: UserPreferencesViewModel = hiltViewModel()
) {
    val prefs by viewModel.preferences.collectAsState()
    val context = LocalContext.current
    var isAdjustingSlider by remember { mutableStateOf(false) }
    val wallpaperPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.setCustomWallpaperUri(uri.toString())
        }
    }

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState(), enabled = !isAdjustingSlider)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "外观细节",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (prefs.themeStyle == AppThemeStyle.LIQUID_GLASS) {
                        "自定义壁纸并微调液态玻璃的模糊、染色与折射参数"
                    } else {
                        "纯白主题下仅保留简洁外观，液态玻璃专属设置将暂停生效"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (prefs.themeStyle == AppThemeStyle.LIQUID_GLASS) "液态玻璃主题已启用" else "当前处于纯白主题",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (prefs.themeStyle == AppThemeStyle.LIQUID_GLASS) {
                            if (prefs.customWallpaperUri.isNullOrBlank()) {
                                "你可以添加自定义壁纸，并实时调整卡片模糊、折射与透明边框效果。"
                            } else {
                                "当前已启用自定义壁纸，下方参数会实时影响壁纸上的液态玻璃表现。"
                            }
                        } else {
                            "当前页面不会再调整液态玻璃参数。切回液态玻璃主题后，已保存的自定义壁纸会自动恢复。"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), contentPadding = 14.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StudySectionHeader(title = "自定义壁纸")
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
                    ) {
                        if (prefs.customWallpaperUri.isNullOrBlank()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "还没有设置壁纸",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (prefs.themeStyle == AppThemeStyle.LIQUID_GLASS) {
                                        "选择设备中的图片后，将在液态玻璃主题下作为全局壁纸显示。"
                                    } else {
                                        "纯白主题下不显示壁纸。切换到液态玻璃主题后才可启用自定义壁纸。"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .size(188.dp)
                                    .clip(RoundedCornerShape(18.dp))
                            ) {
                                AsyncImage(
                                    model = prefs.customWallpaperUri,
                                    contentDescription = "壁纸预览",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(12.dp),
                                    shape = RoundedCornerShape(999.dp),
                                    color = Color.White.copy(alpha = 0.82f)
                                ) {
                                    Text(
                                        text = if (prefs.themeStyle == AppThemeStyle.LIQUID_GLASS) "当前壁纸已启用" else "壁纸已保存，切回液态玻璃后启用",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { wallpaperPicker.launch(arrayOf("image/*")) },
                            enabled = prefs.themeStyle == AppThemeStyle.LIQUID_GLASS,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null
                            )
                            Text("选择图片")
                        }
                        Button(
                            onClick = { viewModel.clearCustomWallpaper() },
                            enabled = !prefs.customWallpaperUri.isNullOrBlank(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null
                            )
                            Text("清除壁纸")
                        }
                    }

                    if (prefs.themeStyle != AppThemeStyle.LIQUID_GLASS) {
                        Text(
                            text = "当前是纯白主题，液态玻璃参数与壁纸效果已暂停显示。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (prefs.themeStyle == AppThemeStyle.LIQUID_GLASS) {
                GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), contentPadding = 14.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StudySectionHeader(title = "液态玻璃参数")

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
                            valueRange = 0f..0.45f
                        )

                        StudySettingRow(
                            icon = Icons.Default.WbSunny,
                            title = "增强色彩层次",
                            subtitle = "提升玻璃后的色彩通透感",
                            trailing = {
                                Switch(
                                    checked = prefs.glassVibrancyEnabled,
                                    onCheckedChange = { viewModel.setGlassVibrancyEnabled(it) }
                                )
                            }
                        )

                        StudySettingRow(
                            icon = Icons.Default.WbSunny,
                            title = "色差折射",
                            subtitle = "更明显的液态折射边缘",
                            trailing = {
                                Switch(
                                    checked = prefs.glassChromaticAberration,
                                    onCheckedChange = { viewModel.setGlassChromaticAberration(it) }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
