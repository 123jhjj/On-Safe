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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.skons.onsafe.data.ContactModel
import app.skons.onsafe.data.MyInfo
import app.skons.onsafe.ui.theme.AppColors
import app.skons.onsafe.ui.theme.appThemeColors

private fun formatPhone(raw: String): String {
    val d = raw.replace(Regex("\\D"), "")
    return when {
        d.matches(Regex("01[016789]\\d*")) -> {
            val s = d.take(11)
            when {
                s.length <= 3 -> s
                s.length <= 7 -> "${s.substring(0, 3)}-${s.substring(3)}"
                else          -> "${s.substring(0, 3)}-${s.substring(3, 7)}-${s.substring(7)}"
            }
        }
        d.startsWith("02") -> {
            val s = d.take(10)
            when {
                s.length <= 2 -> s
                s.length <= 5 -> "${s.substring(0, 2)}-${s.substring(2)}"
                s.length <= 9 -> "${s.substring(0, 2)}-${s.substring(2, 5)}-${s.substring(5)}"
                else          -> "${s.substring(0, 2)}-${s.substring(2, 6)}-${s.substring(6)}"
            }
        }
        d.startsWith("0") -> {
            val s = d.take(11)
            when {
                s.length <= 3  -> s
                s.length <= 6  -> "${s.substring(0, 3)}-${s.substring(3)}"
                s.length <= 10 -> "${s.substring(0, 3)}-${s.substring(3, 6)}-${s.substring(6)}"
                else           -> "${s.substring(0, 3)}-${s.substring(3, 7)}-${s.substring(7)}"
            }
        }
        d.matches(Regex("1[5-9]\\d*")) -> {
            val s = d.take(8)
            if (s.length <= 4) s else "${s.substring(0, 4)}-${s.substring(4)}"
        }
        else -> d
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
    onSave: (EditSheetFields) -> Unit,
    onDismiss: () -> Unit,
) {
    val ctx = LocalContext.current
    val c = appThemeColors()

    var role by remember { mutableStateOf(if (isMyInfo) "" else (contact?.role?.takeIf { it != "추가 연락처" } ?: "")) }
    var name by remember { mutableStateOf(if (isMyInfo) (myInfo?.name ?: "") else (initialName ?: contact?.name ?: "")) }
    var phone by remember { mutableStateOf(TextFieldValue(if (isMyInfo) "" else formatPhone(initialPhone ?: contact?.phone ?: ""))) }
    var company by remember { mutableStateOf(if (isMyInfo) (myInfo?.company ?: "") else "") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = c.blue, unfocusedBorderColor = c.border,
        focusedTextColor = c.text, unfocusedTextColor = c.text,
        unfocusedContainerColor = c.bg, focusedContainerColor = c.bg,
        unfocusedPlaceholderColor = c.hint, focusedPlaceholderColor = c.hint,
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
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.W700, color = c.text, modifier = Modifier.weight(1f))
            TextButton(onClick = { role = ""; name = ""; phone = TextFieldValue(""); company = "" }) {
                Text("초기화", fontSize = 13.sp, color = c.sub, fontWeight = FontWeight.W600)
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = null, tint = c.sub)
            }
        }
        Spacer(Modifier.height(12.dp))

        if (isMyInfo) {
            Text("소속", fontSize = 12.sp, color = c.sub, fontWeight = FontWeight.W600)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = company, onValueChange = { company = it },
                placeholder = { Text("소속 (회사/부서명)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors, singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            Text("이름", fontSize = 12.sp, color = c.sub, fontWeight = FontWeight.W600)
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
                Text("직책", fontSize = 12.sp, color = c.sub, fontWeight = FontWeight.W700)
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
            Text("이름", fontSize = 12.sp, color = c.sub, fontWeight = FontWeight.W600)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                placeholder = { Text("이름") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors, singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            Text("연락처", fontSize = 12.sp, color = c.sub, fontWeight = FontWeight.W600)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { tfv ->
                    val cursorPos = tfv.selection.end
                    val digitsBeforeCursor = tfv.text.substring(0, cursorPos).count { it.isDigit() }
                    val formatted = formatPhone(tfv.text)
                    var digitCount = 0
                    var newCursor = formatted.length
                    for (i in formatted.indices) {
                        if (formatted[i].isDigit()) digitCount++
                        if (digitCount == digitsBeforeCursor) { newCursor = i + 1; break }
                    }
                    phone = TextFieldValue(formatted, TextRange(newCursor))
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
                onSave(EditSheetFields(role = role, name = name, phone = phone.text, company = company))
                android.widget.Toast.makeText(ctx, "저장 완료", android.widget.Toast.LENGTH_SHORT).show()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = c.blue),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Text("저장", fontSize = 16.sp, fontWeight = FontWeight.W700)
        }
    }
}
