package app.skons.onsafe.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.skons.onsafe.ui.theme.AppColors

data class ActionSheetOption<T>(val icon: ImageVector, val label: String, val value: T)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> BottomActionSheet(
    title: String,
    options: List<ActionSheetOption<T>>,
    isDark: Boolean,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val textC = if (isDark) AppColors.TextDark else AppColors.TextLight
    val subC = if (isDark) AppColors.SubDark else AppColors.SubLight
    val borderC = if (isDark) AppColors.BorderDark else AppColors.BorderLight

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) AppColors.CardDark else AppColors.CardLight,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title, fontSize = 16.sp, fontWeight = FontWeight.W700, color = textC,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = subC)
                }
            }
            Spacer(Modifier.height(12.dp))
            options.forEachIndexed { i, opt ->
                if (i > 0) Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, borderC, RoundedCornerShape(10.dp))
                        .clickable { onSelect(opt.value) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Icon(opt.icon, contentDescription = null, tint = subC)
                    Spacer(Modifier.width(12.dp))
                    Text(opt.label, fontSize = 15.sp, fontWeight = FontWeight.W600, color = textC)
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetContainer(
    isDark: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) AppColors.CardDark else AppColors.CardLight,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        content()
    }
}
