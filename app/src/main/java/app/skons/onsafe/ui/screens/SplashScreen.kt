package app.skons.onsafe.ui.screens

import android.Manifest
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import app.skons.onsafe.R
import app.skons.onsafe.data.OnSafePreferences
import app.skons.onsafe.ui.theme.AppColors
import app.skons.onsafe.ui.theme.LocalDarkTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SplashScreen(navController: NavHostController) {
    val isDark = LocalDarkTheme.current
    val bg = if (isDark) AppColors.BgDark else AppColors.AppBarYellow

    var showPermDialog by remember { mutableStateOf(false) }
    var dialogDismissed by remember { mutableStateOf(false) }
    var permissionsHandled by remember { mutableStateOf(false) }

    val allPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS,
        ),
        onPermissionsResult = { permissionsHandled = true },
    )

    LaunchedEffect(Unit) {
        navController.context.let { ctx ->
            val prefs = OnSafePreferences.appPrefs(ctx)
            val setupComplete = prefs.getBoolean("onsafe-first-setup-complete", false)
            if (!setupComplete) {
                showPermDialog = true
                while (!dialogDismissed) delay(50)
                allPermissions.launchMultiplePermissionRequest()
                while (!permissionsHandled) delay(100)
                prefs.edit()
                    .putBoolean("onsafe-location-notice-permanent", true)
                    .putBoolean("onsafe-first-setup-complete", true)
                    .apply()
            } else {
                delay(500)
            }
        }
        navController.navigate("home") { popUpTo("splash") { inclusive = true } }
    }

    Box(Modifier.fillMaxSize().background(bg), contentAlignment = Alignment.Center) {
        val iconRes = if (isDark) R.drawable.icon_transparent else R.drawable.icon
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(180.dp),
            contentScale = ContentScale.Fit,
        )
    }

    if (showPermDialog) {
        val textC = if (isDark) AppColors.TextDark else AppColors.TextLight
        val subC = if (isDark) AppColors.SubDark else AppColors.SubLight
        val cardBg = if (isDark) AppColors.CardDark else AppColors.CardLight
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        ) {
            Surface(shape = RoundedCornerShape(16.dp), color = cardBg) {
                Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 20.dp)) {
                    Text("권한 및 저장 안내", fontSize = 17.sp, fontWeight = FontWeight.W800, color = textC)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "권한 거부 시에도 앱을 사용할 수 있습니다. 데이터는 기기에만 저장되며 외부로 전송되지 않습니다.",
                        fontSize = 13.sp, color = subC, lineHeight = 20.sp,
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "위치 : 119 문자 신고, 보고 양식(주소)\n사진 : 보고 양식 첨부\n연락처 : 연락처 가져오기",
                        fontSize = 13.sp, color = textC, lineHeight = 21.sp,
                    )
                    Spacer(Modifier.height(20.dp))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "확인", fontSize = 15.sp, fontWeight = FontWeight.W800, color = textC,
                            modifier = Modifier.clickable { showPermDialog = false; dialogDismissed = true },
                        )
                    }
                }
            }
        }
    }
}
