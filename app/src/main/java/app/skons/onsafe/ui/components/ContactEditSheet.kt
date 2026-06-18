package app.skons.onsafe.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.skons.onsafe.data.ContactModel
import app.skons.onsafe.data.MyInfo
import app.skons.onsafe.ui.theme.AppColors

private fun formatPhone(raw: String): String {
    val d = raw.replace(Regex("\\D"), "").take(11)
    return when {
        d.length <= 3 -> d
        d.length <= 7 -> "${d.substring(0, 3)}-${d.substring(3)}"
        else -> "${d.substring(0, 3)}-${d.substring(3, 7)}-${d.substring(7)}"
    }
}

data class EditSheetFields(
    val role: String = "",
    val name: String = "",
    val phone: String = "",
    val company: String = "",
)

@Composable
fun ContactEditSheetContent(
    contact: ContactModel? = null,
    isMyInfo: Boolean = false,
    myInfo: MyInfo? = null,
    initialName: String? = null,
    initialPhone: String? = null,
    isDark: Boolean,
    onSave: (EditSheetFields) -> Unit,
    onDismiss: () -> Unit,
) {
    val textC = if (isDark) AppColors.TextDark else AppColors.TextLight
    val subC = if (isDark) AppColors.SubDark else AppColors.SubLight
    val borderC = if (isDark) AppColors.BorderDark else AppColors.BorderLight
    val blueAccent = if (isDark) AppColors.BlueDark else AppColors.Blue
    val hintC = if (isDark) AppColors.HintDark else AppColors.HintLight
    val fillBg = if (isDark) AppColors.BgDark else AppColors.BgLight

    var role by remember { mutableStateOf(if (isMyInfo) "" else (contact?.role ?: "")) }
    var name by remember { mutableStateOf(if (isMyInfo) (myInfo?.name ?: "") else (initialName ?: contact?.name ?: "")) }
    var phone by remember { mutableStateOf(if (isMyInfo) "" else (initialPhone ?: contact?.phone ?: "")) }
    var company by remember { mutableStateOf(if (isMyInfo) (myInfo?.company ?: "") else "") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = blueAccent, unfocusedBorderColor = borderC,
        focusedTextColor = textC, unfocusedTextColor = textC,
        unfocusedContainerColor = fillBg, focusedContainerColor = fillBg,
        unfocusedPlaceholderColor = hintC, focusedPlaceholderColor = hintC,
    )

    val title = when {
        isMyInfo -> "내 정보 수정"
        contact == null -> "연락처 추가"
        else -> "${contact.role} 수정"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .imePadding()
            .navigationBarsPadding()
            .padding(bottom = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.W700, color = textC, modifier = Modifier.weight(1f))
            TextButton(onClick = { role = ""; name = ""; phone = ""; company = "" }) {
                Text("초기화", fontSize = 13.sp, color = subC, fontWeight = FontWeight.W600)
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = null, tint = subC)
            }
        }
        Spacer(Modifier.height(12.dp))

        if (isMyInfo) {
            Text("소속", fontSize = 12.sp, color = subC, fontWeight = FontWeight.W600)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = company, onValueChange = { company = it },
                placeholder = { Text("소속 (회사/부서명)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors, singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            Text("이름", fontSize = 12.sp, color = subC, fontWeight = FontWeight.W600)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                placeholder = { Text("이름") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors, singleLine = true,
            )
        } else {
            if (contact == null || contact.deletable) {
                Text("직책", fontSize = 12.sp, color = subC, fontWeight = FontWeight.W600)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = role, onValueChange = { role = it },
                    placeholder = { Text("직책") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = fieldColors, singleLine = true,
                )
                Spacer(Modifier.height(12.dp))
            }
            Text("이름", fontSize = 12.sp, color = subC, fontWeight = FontWeight.W600)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                placeholder = { Text("이름") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors, singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            Text("연락처", fontSize = 12.sp, color = subC, fontWeight = FontWeight.W600)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { new ->
                    val formatted = formatPhone(new)
                    if (formatted != phone) phone = formatted
                },
                placeholder = { Text("010-0000-0000") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
            )
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                onSave(EditSheetFields(role = role, name = name, phone = phone, company = company))
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = blueAccent),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Text("저장", fontSize = 16.sp, fontWeight = FontWeight.W700)
        }
    }
}
