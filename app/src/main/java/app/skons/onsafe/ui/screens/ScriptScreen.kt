package app.skons.onsafe.ui.screens

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.skons.onsafe.ui.components.ActionSheetOption
import app.skons.onsafe.ui.components.BottomActionSheet
import app.skons.onsafe.ui.components.DetailAppBar
import app.skons.onsafe.ui.navigateMain
import app.skons.onsafe.ui.theme.AppColors
import app.skons.onsafe.ui.theme.LocalDarkTheme
import app.skons.onsafe.ui.theme.appThemeColors
import app.skons.onsafe.viewmodel.ContactViewModel
import app.skons.onsafe.viewmodel.LocationStatus
import app.skons.onsafe.viewmodel.LocationViewModel
import app.skons.onsafe.viewmodel.ScriptViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@Composable
fun ScriptScreen(
    contactViewModel: ContactViewModel,
    locationViewModel: LocationViewModel,
    scriptViewModel: ScriptViewModel,
    onBack: () -> Unit,
    onMenuClick: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    val ctx = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isDark = LocalDarkTheme.current
    val c = appThemeColors()

    val appData by contactViewModel.data.collectAsStateWithLifecycle()
    val locState by locationViewModel.state.collectAsStateWithLifecycle()
    val state by scriptViewModel.state.collectAsStateWithLifecycle()

    val dashedColor = if (isDark) AppColors.BorderDark.copy(alpha = 0.8f) else c.border
    val chipBg = if (isDark) AppColors.BorderDark else Color(0xFFEEEEEE)
    val underlineC = if (isDark) Color(0xFFFF6666) else AppColors.Red

    val photos = remember { mutableStateListOf<Uri?>(null, null) }
    var showPhotoSheet by remember { mutableStateOf(false) }
    var pendingIndex by remember { mutableStateOf(-1) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && pendingIndex >= 0) {
            photos[pendingIndex] = pendingCameraUri
        }
        pendingIndex = -1
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && pendingIndex >= 0) photos[pendingIndex] = uri
        pendingIndex = -1
    }

    LaunchedEffect(Unit) {
        scriptViewModel.initDefaults(appData.myInfo.company, appData.myInfo.name)
        locationViewModel.fetch()
        while (true) { delay(60_000); locationViewModel.fetch() }
    }

    val refreshRot = remember { Animatable(0f) }
    LaunchedEffect(locState.fetching) {
        if (locState.fetching) {
            refreshRot.snapTo(0f)
            while (true) {
                refreshRot.animateTo(360f, tween(700, easing = LinearEasing))
                refreshRot.snapTo(0f)
            }
        } else {
            refreshRot.stop()
        }
    }

    fun buildScript(): String {
        val lines = mutableListOf(
            "소속: ${state.company}", "보고자: ${state.reporter}", "발생 시각: ${state.time}",
            "발생 장소: ${state.location}", "작업 내용: ${state.workName}",
            "피해자: ${state.victim}", "사고 내용: ${state.incident}", "현재 상태: ${state.status}",
        )
        if (locState.locationEnabled && locState.status == LocationStatus.Ready && locState.address.isNotEmpty()) {
            lines.add("위치: ${locState.address}")
        }
        return lines.joinToString("\n")
    }

    fun shareText() {
        ctx.startActivity(Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, buildScript()) },
            null,
        ))
    }

    fun sharePhotos() {
        val uris = photos.filterNotNull()
        if (uris.isEmpty()) return
        val intent = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = "image/*"; putExtra(Intent.EXTRA_STREAM, uris[0])
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/*"; putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        ctx.startActivity(Intent.createChooser(intent, null))
    }

    Scaffold(
        topBar = {
            DetailAppBar(
                title = "보고 양식",
                currentRoute = "script",
                onBack = onBack,
                onMenuClick = onMenuClick,
                onNavigate = onNavigate,
            )
        },
        containerColor = c.bg,
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { focusManager.clearFocus() },
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp)
                    .padding(top = 10.dp, bottom = 6.dp),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .border(1.dp, c.border, RoundedCornerShape(16.dp))
                        .background(c.cardBg, RoundedCornerShape(16.dp))
                        .padding(horizontal = 18.dp)
                        .padding(top = 14.dp, bottom = 16.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("보고 양식", fontSize = 13.sp, fontWeight = FontWeight.W700, color = c.sub, letterSpacing = 0.3.sp)
                        Row(
                            Modifier
                                .background(chipBg, RoundedCornerShape(8.dp))
                                .clickable {
                                    scriptViewModel.reset(appData.myInfo.company, appData.myInfo.name)
                                    photos[0] = null; photos[1] = null
                                    locationViewModel.fetch()
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Outlined.Refresh, null, tint = c.sub, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("초기화", fontSize = 11.sp, color = c.sub, fontWeight = FontWeight.W600)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Divider(color = c.border, thickness = 1.dp)

                    ScriptField("소속", state.company, "소속", underlineC, c.hint, c.text, c.sub) {
                        scriptViewModel.update { s -> s.copy(company = it) }
                    }
                    ScriptField("보고자", state.reporter, "이름", underlineC, c.hint, c.text, c.sub) {
                        scriptViewModel.update { s -> s.copy(reporter = it) }
                    }
                    ScriptField("발생 시각", state.time, "시간", underlineC, c.hint, c.text, c.sub) {
                        scriptViewModel.update { s -> s.copy(time = it) }
                    }
                    ScriptField("발생 장소", state.location, "국소명", underlineC, c.hint, c.text, c.sub) {
                        scriptViewModel.update { s -> s.copy(location = it) }
                    }
                    ScriptField("작업 내용", state.workName, "작업명", underlineC, c.hint, c.text, c.sub) {
                        scriptViewModel.update { s -> s.copy(workName = it) }
                    }
                    ScriptField("피해자", state.victim, "피해자", underlineC, c.hint, c.text, c.sub) {
                        scriptViewModel.update { s -> s.copy(victim = it) }
                    }
                    ScriptField("사고 내용", state.incident, "사고 내용", underlineC, c.hint, c.text, c.sub) {
                        scriptViewModel.update { s -> s.copy(incident = it) }
                    }
                    ScriptField("현재 상태", state.status, "현재 상태", underlineC, c.hint, c.text, c.sub) {
                        scriptViewModel.update { s -> s.copy(status = it) }
                    }

                    Divider(color = c.border, thickness = 1.dp, modifier = Modifier.padding(vertical = 5.dp))

                    val (addrText, addrColor) = when {
                        !locState.locationEnabled -> "위치 수집 꺼짐" to c.sub
                        locState.status == LocationStatus.Loading ->
                            (locState.address.ifEmpty { "위치 수집중..." }) to c.sub
                        locState.status == LocationStatus.Denied ->
                            "위치 권한 허용안함" to (if (isDark) AppColors.RedSoftDark else AppColors.Red)
                        locState.status == LocationStatus.ServiceDisabled ->
                            "기기 위치 서비스 꺼짐" to (if (isDark) AppColors.RedSoftDark else AppColors.Red)
                        locState.status == LocationStatus.Failed -> "위치 신호 없음" to c.sub
                        else -> locState.address to c.sub
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = locState.locationEnabled,
                            onCheckedChange = { locationViewModel.setLocationEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = c.blue,
                                checkedTrackColor = c.blue.copy(alpha = 0.3f),
                            ),
                            modifier = Modifier
                                .layout { measurable, constraints ->
                                    val placeable = measurable.measure(constraints)
                                    val w = (placeable.width * 0.65f).toInt()
                                    val h = (placeable.height * 0.65f).toInt()
                                    layout(w, h) {
                                        placeable.placeRelative(
                                            -(placeable.width - w) / 2,
                                            -(placeable.height - h) / 2,
                                        )
                                    }
                                }
                                .scale(0.65f),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "위치", fontSize = 12.sp,
                            color = if (locState.locationEnabled) c.blue else c.sub,
                            fontWeight = FontWeight.W600,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(addrText, fontSize = 12.sp, color = addrColor, modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Outlined.Refresh, null, tint = c.sub,
                            modifier = Modifier.size(16.dp).rotate(refreshRot.value)
                                .clickable { locationViewModel.fetch() },
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(Modifier.fillMaxWidth()) {
                        PhotoSlot(
                            modifier = Modifier.weight(1f),
                            uri = photos.getOrNull(0), dashedColor = dashedColor, subC = c.sub,
                            onTap = { pendingIndex = 0; showPhotoSheet = true },
                            onRemove = { photos[0] = null },
                        )
                        Spacer(Modifier.width(10.dp))
                        PhotoSlot(
                            modifier = Modifier.weight(1f),
                            uri = photos.getOrNull(1), dashedColor = dashedColor, subC = c.sub,
                            onTap = { pendingIndex = 1; showPhotoSheet = true },
                            onRemove = { photos[1] = null },
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Divider(color = c.border, thickness = 1.dp)
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Button(
                    onClick = ::shareText,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) AppColors.BorderDark else Color(0xFFDDDDDD),
                        contentColor = c.text,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                ) {
                    Icon(Icons.Outlined.Article, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("텍스트 공유", fontSize = 15.sp, fontWeight = FontWeight.W700)
                }
                Spacer(Modifier.width(10.dp))
                val hasPhotos = photos.any { it != null }
                Button(
                    onClick = ::sharePhotos,
                    enabled = hasPhotos,
                    modifier = Modifier.weight(1f).alpha(if (hasPhotos) 1f else 0.35f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = c.blue, contentColor = Color.White,
                        disabledContainerColor = c.blue, disabledContentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                ) {
                    Icon(Icons.Outlined.Photo, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("사진 공유", fontSize = 15.sp, fontWeight = FontWeight.W700)
                }
            }
        }
    }

    if (showPhotoSheet) {
        BottomActionSheet(
            title = "사진 추가",
            options = listOf(
                ActionSheetOption(Icons.Outlined.CameraAlt, "사진 촬영", 0),
                ActionSheetOption(Icons.Outlined.PhotoLibrary, "갤러리 선택", 1),
            ),
            onSelect = { choice ->
                showPhotoSheet = false
                if (choice == 0) {
                    try {
                        val cv = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, "onsafe_${System.currentTimeMillis()}.jpg")
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        }
                        val mediaUri = ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
                        if (mediaUri != null) {
                            pendingCameraUri = mediaUri
                            val camIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                                putExtra(MediaStore.EXTRA_OUTPUT, mediaUri)
                            }
                            cameraLauncher.launch(camIntent)
                        } else {
                            pendingIndex = -1
                        }
                    } catch (_: android.content.ActivityNotFoundException) {
                        android.widget.Toast.makeText(ctx, "카메라 앱을 찾을 수 없습니다", android.widget.Toast.LENGTH_SHORT).show()
                        pendingIndex = -1
                    } catch (_: SecurityException) {
                        android.widget.Toast.makeText(ctx, "카메라 권한이 필요합니다", android.widget.Toast.LENGTH_SHORT).show()
                        pendingIndex = -1
                    }
                } else {
                    galleryLauncher.launch("image/*")
                }
            },
            onDismiss = { showPhotoSheet = false; pendingIndex = -1 },
        )
    }
}

@Composable
private fun ScriptField(
    label: String,
    value: String,
    hint: String,
    underlineC: Color,
    hintC: Color,
    textC: Color,
    subC: Color,
    onValueChange: (String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp),
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.W600, color = subC, modifier = Modifier.width(60.dp))
        Spacer(Modifier.width(12.dp))
        Box(
            Modifier
                .weight(1f)
                .drawBehind {
                    drawLine(
                        color = underlineC,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.5.dp.toPx(),
                    )
                }
                .padding(vertical = 4.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.W600, color = textC),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) Text(hint, color = hintC, fontSize = 15.sp, fontWeight = FontWeight.W600)
                    innerTextField()
                },
            )
        }
    }
}

@Composable
private fun PhotoSlot(
    modifier: Modifier = Modifier,
    uri: Uri?,
    dashedColor: Color,
    subC: Color,
    onTap: () -> Unit,
    onRemove: () -> Unit,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onTap),
    ) {
        Canvas(Modifier.matchParentSize()) {
            val paint = Paint().apply {
                color = dashedColor
                style = PaintingStyle.Stroke
                strokeWidth = 1.5.dp.toPx()
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f), 0f)
            }
            val path = Path().apply {
                addRoundRect(RoundRect(
                    left = 0f, top = 0f, right = size.width, bottom = size.height,
                    cornerRadius = CornerRadius(10.dp.toPx()),
                ))
            }
            drawIntoCanvas { it.drawPath(path, paint) }
        }

        if (uri != null) {
            AsyncImage(
                model = uri, contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(Color.Black.copy(alpha = 0.54f), RoundedCornerShape(16.dp))
                    .clickable(onClick = onRemove)
                    .padding(5.dp),
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        } else {
            Column(
                Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Outlined.CameraAlt, null, tint = subC, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(4.dp))
                Text("사진", fontSize = 12.sp, color = subC)
            }
        }
    }
}
