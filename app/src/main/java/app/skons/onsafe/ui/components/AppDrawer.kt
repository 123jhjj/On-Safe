package app.skons.onsafe.ui.components

import android.Manifest
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.PhoneInTalk
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.skons.onsafe.data.ContactModel
import app.skons.onsafe.data.MyInfo
import app.skons.onsafe.ui.theme.AppColors
import app.skons.onsafe.viewmodel.ContactViewModel
import app.skons.onsafe.viewmodel.LocationStatus
import app.skons.onsafe.viewmodel.LocationViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private val BadgeBgLight = Color(0xFFFFF8E0)
private val BadgeBgDark = Color(0xFF2A2A3D)
private val BadgeTxLight = Color(0xFF7A5C00)
private val BadgeTxDark = Color(0xFFFFD966)

private fun fmtPhone(raw: String): String {
    val d = raw.replace(Regex("\\D"), "")
    return when (d.length) {
        11 -> "${d.substring(0, 3)}-${d.substring(3, 7)}-${d.substring(7)}"
        10 -> "${d.substring(0, 3)}-${d.substring(3, 6)}-${d.substring(6)}"
        else -> raw
    }
}

private fun getContactFromUri(context: Context, uri: Uri): Pair<String, String> {
    var name = ""
    var phone = ""
    context.contentResolver.query(
        uri,
        arrayOf(ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID),
        null, null, null,
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            if (nameIdx >= 0) name = cursor.getString(nameIdx) ?: ""
            if (idIdx >= 0) {
                val id = cursor.getString(idIdx)
                context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                    arrayOf(id), null,
                )?.use { pc ->
                    if (pc.moveToFirst()) {
                        val pIdx = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (pIdx >= 0) phone = pc.getString(pIdx) ?: ""
                    }
                }
            }
        }
    }
    return name to phone
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppDrawer(
    contactViewModel: ContactViewModel,
    locationViewModel: LocationViewModel,
    isDark: Boolean,
    onDismiss: () -> Unit,
) {
    val ctx = LocalContext.current
    val appData by contactViewModel.data.collectAsStateWithLifecycle()
    val locState by locationViewModel.state.collectAsStateWithLifecycle()

    val textC = if (isDark) AppColors.TextDark else AppColors.TextLight
    val subC = if (isDark) AppColors.SubDark else AppColors.SubLight
    val hintC = if (isDark) AppColors.HintDark else AppColors.HintLight
    val borderC = if (isDark) AppColors.BorderDark else AppColors.BorderLight
    val cardBg = if (isDark) AppColors.CardDark else AppColors.CardLight
    val bg = if (isDark) AppColors.BgDark else AppColors.CardLight
    val badgeBg = if (isDark) BadgeBgDark else BadgeBgLight
    val badgeTx = if (isDark) BadgeTxDark else BadgeTxLight

    // Sheet states
    var showMyInfoEdit by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var editContact by remember { mutableStateOf<ContactModel?>(null) }
    var prefillName by remember { mutableStateOf("") }
    var prefillPhone by remember { mutableStateOf("") }
    var showActionSheetFor by remember { mutableStateOf<ContactModel?>(null) }
    var showAddActionSheet by remember { mutableStateOf(false) }
    var pendingPickContact by remember { mutableStateOf<ContactModel?>(null) }
    var pendingPickIsNew by remember { mutableStateOf(false) }

    val contactsPermission = rememberPermissionState(Manifest.permission.READ_CONTACTS)

    val contactPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickContact(),
    ) { uri ->
        if (uri != null) {
            val (n, p) = getContactFromUri(ctx, uri)
            prefillName = n
            prefillPhone = p
            editContact = if (pendingPickIsNew) null else pendingPickContact
            showEditSheet = true
        }
        pendingPickContact = null
        pendingPickIsNew = false
    }

    fun launchContactPick(forContact: ContactModel?, isNew: Boolean) {
        pendingPickContact = forContact
        pendingPickIsNew = isNew
        if (contactsPermission.status.isGranted) {
            contactPickerLauncher.launch()
        } else {
            contactsPermission.launchPermissionRequest()
        }
    }

    fun openDirectEdit(forContact: ContactModel?, isNew: Boolean) {
        prefillName = ""
        prefillPhone = ""
        editContact = if (isNew) null else forContact
        showEditSheet = true
    }

    // Rotation for location refresh icon
    val infiniteTransition = rememberInfiniteTransition(label = "refresh")
    val rawRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing)),
        label = "rotation",
    )
    val refreshRotation = if (locState.fetching) rawRotation else 0f

    // Reorderable list
    val contacts = appData.contacts
    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val headerCount = 2
        val fIdx = from.index - headerCount
        val tIdx = to.index - headerCount
        if (fIdx >= 0 && tIdx >= 0 && fIdx < contacts.size && tIdx < contacts.size) {
            contactViewModel.reorderContacts(fIdx, tIdx)
        }
    }

    Box(Modifier.fillMaxSize()) {
        // Scrim
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.54f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onDismiss() },
        )

        // Drawer panel
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.88f)
                .align(Alignment.CenterStart),
            color = bg,
            shadowElevation = 10.dp,
        ) {
            Column(Modifier.fillMaxSize()) {
                // Yellow header
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(AppColors.AppBarYellow)
                        .statusBarsPadding()
                        .height(56.dp),
                ) {
                    Text(
                        "메뉴", fontSize = 20.sp, fontWeight = FontWeight.W800,
                        color = AppColors.AppBarFg,
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 18.dp),
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd),
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "닫기", tint = AppColors.AppBarFg)
                    }
                }

                // Scrollable content
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.weight(1f).navigationBarsPadding(),
                ) {
                    // Location card
                    item(key = "location") {
                        // Location section
                        Column(
                            Modifier
                                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
                                .border(1.dp, borderC, RoundedCornerShape(10.dp))
                                .background(cardBg, RoundedCornerShape(10.dp)),
                        ) {
                            // Card header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(badgeBg, RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp))
                                    .border(
                                        width = 0.dp,
                                        color = Color.Transparent,
                                        shape = RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp),
                                    )
                                    .padding(start = 12.dp, end = 4.dp, top = 9.dp, bottom = 9.dp),
                            ) {
                                Box(
                                    Modifier
                                        .width(4.dp)
                                        .height(16.dp)
                                        .background(AppColors.AppBarYellow, RoundedCornerShape(2.dp)),
                                )
                                Spacer(Modifier.width(9.dp))
                                Text(
                                    "위치", fontSize = 12.sp, fontWeight = FontWeight.W800, color = textC,
                                    modifier = Modifier.weight(1f),
                                )
                                Switch(
                                    checked = locState.locationEnabled,
                                    onCheckedChange = { locationViewModel.setLocationEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = if (isDark) AppColors.BlueDark else AppColors.Blue,
                                        checkedTrackColor = (if (isDark) AppColors.BlueDark else AppColors.Blue).copy(alpha = 0.3f),
                                    ),
                                    modifier = Modifier.height(26.dp),
                                )
                            }
                            Column(Modifier.padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 12.dp)) {
                                Text(
                                    "위치 사용 : 119 문자 신고 · 보고 양식",
                                    fontSize = 11.sp, color = subC,
                                )
                                Spacer(Modifier.height(6.dp))
                                if (locState.locationEnabled) {
                                    val addrText = when (locState.status) {
                                        LocationStatus.Loading -> if (locState.address.isNotEmpty()) locState.address else "위치 수집중..."
                                        LocationStatus.Denied -> "위치 권한 허용 안 됨"
                                        LocationStatus.ServiceDisabled -> "기기 위치 서비스 꺼짐"
                                        LocationStatus.Failed -> "위치 신호 없음"
                                        LocationStatus.Ready -> locState.address
                                    }
                                    val addrColor = when (locState.status) {
                                        LocationStatus.Denied, LocationStatus.ServiceDisabled ->
                                            if (isDark) AppColors.RedSoftDark else AppColors.Red
                                        else -> subC
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.Refresh,
                                            contentDescription = null,
                                            tint = subC,
                                            modifier = Modifier
                                                .size(15.dp)
                                                .rotate(refreshRotation)
                                                .clickable { locationViewModel.fetch() },
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            addrText.ifEmpty { "위치 수집중..." },
                                            fontSize = 13.sp, color = addrColor, lineHeight = 18.sp,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                } else {
                                    Text(
                                        "위치 수집 꺼짐", fontSize = 13.sp, fontWeight = FontWeight.W600,
                                        color = if (isDark) AppColors.RedSoftDark else AppColors.Red,
                                    )
                                }
                            }
                        }
                    }

                    // My info card
                    item(key = "myInfo") {
                        val myInfo = appData.myInfo
                        val hasInfo = myInfo.company.isNotEmpty() || myInfo.name.isNotEmpty()
                        Column(
                            Modifier
                                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                                .border(1.dp, borderC, RoundedCornerShape(10.dp))
                                .background(cardBg, RoundedCornerShape(10.dp)),
                        ) {
                            // Header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(badgeBg, RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp))
                                    .padding(start = 12.dp, end = 4.dp, top = 9.dp, bottom = 9.dp),
                            ) {
                                Box(
                                    Modifier
                                        .width(4.dp)
                                        .height(16.dp)
                                        .background(AppColors.AppBarYellow, RoundedCornerShape(2.dp)),
                                )
                                Spacer(Modifier.width(9.dp))
                                Text(
                                    "내 정보", fontSize = 12.sp, fontWeight = FontWeight.W800, color = textC,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            ) {
                                Column(Modifier.weight(1f)) {
                                    if (hasInfo) {
                                        if (myInfo.company.isNotEmpty()) {
                                            Row {
                                                Text("소속  ", fontSize = 12.sp, color = subC, fontWeight = FontWeight.W500)
                                                Text(myInfo.company, fontSize = 15.sp, fontWeight = FontWeight.W600, color = textC)
                                            }
                                        }
                                        if (myInfo.name.isNotEmpty()) {
                                            Spacer(Modifier.height(2.dp))
                                            Row {
                                                Text("이름  ", fontSize = 12.sp, color = subC, fontWeight = FontWeight.W500)
                                                Text(myInfo.name, fontSize = 15.sp, fontWeight = FontWeight.W600, color = textC)
                                            }
                                        }
                                    } else {
                                        Text("미입력", fontSize = 15.sp, color = hintC)
                                    }
                                }
                                Box(
                                    Modifier
                                        .background(badgeBg, RoundedCornerShape(7.dp))
                                        .clickable { showMyInfoEdit = true }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                ) {
                                    Text("수정", fontSize = 12.sp, fontWeight = FontWeight.W700, color = badgeTx)
                                }
                            }
                        }
                    }

                    // Contacts (reorderable)
                    items(contacts, key = { it.id }) { contact ->
                        ReorderableItem(reorderState, contact.id) { isDragging ->
                            val elevation = if (isDragging) 4.dp else 0.dp
                            Surface(
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 12.dp, bottom = 6.dp)
                                    .border(1.dp, borderC, RoundedCornerShape(10.dp)),
                                color = cardBg,
                                shape = RoundedCornerShape(10.dp),
                                shadowElevation = elevation,
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    // Content
                                    Column(
                                        Modifier
                                            .weight(1f)
                                            .padding(start = 12.dp, top = 9.dp, bottom = 9.dp, end = 4.dp)
                                            .clickable {
                                                val phone = contact.phone.replace(Regex("\\D"), "")
                                                if (phone.isNotEmpty()) {
                                                    val intent = android.content.Intent(
                                                        android.content.Intent.ACTION_DIAL,
                                                        Uri.parse("tel:$phone"),
                                                    )
                                                    ctx.startActivity(intent)
                                                }
                                            },
                                    ) {
                                        val hasName = contact.name.isNotEmpty()
                                        val hasPhone = contact.phone.isNotEmpty()
                                        Text(contact.role, fontSize = 15.sp, color = subC, lineHeight = 20.sp)
                                        Spacer(Modifier.height(1.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                if (hasName) contact.name else "이름 미입력",
                                                fontSize = 17.sp, fontWeight = FontWeight.W700,
                                                color = if (hasName) textC else hintC,
                                                modifier = Modifier.weight(1f),
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Icon(
                                                Icons.Outlined.PhoneInTalk, contentDescription = null,
                                                tint = if (hasPhone) badgeTx else hintC,
                                                modifier = Modifier.size(13.dp),
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                if (hasPhone) fmtPhone(contact.phone) else "번호 없음",
                                                fontSize = 13.sp,
                                                fontWeight = if (hasPhone) FontWeight.W600 else FontWeight.W400,
                                                color = if (hasPhone) badgeTx else hintC,
                                            )
                                        }
                                    }
                                    // Edit button
                                    Icon(
                                        Icons.Default.Edit, contentDescription = "수정",
                                        tint = subC,
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp, vertical = 8.dp)
                                            .size(17.dp)
                                            .clickable {
                                                showActionSheetFor = contact
                                            },
                                    )
                                    // Delete (deletable only)
                                    if (contact.deletable) {
                                        Icon(
                                            Icons.Outlined.Delete, contentDescription = "삭제",
                                            tint = if (isDark) Color(0xFFFF8080) else Color(0xFFCC3333),
                                            modifier = Modifier
                                                .padding(end = 4.dp, top = 8.dp, bottom = 8.dp)
                                                .size(17.dp)
                                                .clickable { contactViewModel.removeContact(contact.id) },
                                        )
                                    }
                                    // Drag handle
                                    Box(
                                        Modifier
                                            .fillMaxHeight()
                                            .width(38.dp)
                                            .drawBehind {
                                                drawLine(
                                                    color = borderC,
                                                    start = Offset(0f, 0f),
                                                    end = Offset(0f, size.height),
                                                    strokeWidth = 0.8.dp.toPx(),
                                                )
                                            }
                                            .draggableHandle(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            Icons.Default.DragHandle, contentDescription = null,
                                            tint = hintC, modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Add contact button
                    item(key = "addBtn") {
                        Box(
                            Modifier
                                .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 24.dp)
                                .fillMaxWidth()
                                .border(1.5.dp, borderC, RoundedCornerShape(10.dp))
                                .clickable { showAddActionSheet = true }
                                .padding(vertical = 13.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Add, contentDescription = null, tint = subC, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("연락처 추가", color = subC, fontWeight = FontWeight.W600)
                            }
                        }
                    }
                }
            }
        }
    }

    // My info edit sheet
    if (showMyInfoEdit) {
        BottomSheetContainer(isDark = isDark, onDismiss = { showMyInfoEdit = false }) {
            ContactEditSheetContent(
                isMyInfo = true,
                myInfo = appData.myInfo,
                isDark = isDark,
                onSave = { fields ->
                    contactViewModel.updateMyInfo(
                        appData.myInfo.copy(name = fields.name, company = fields.company),
                    )
                },
                onDismiss = { showMyInfoEdit = false },
            )
        }
    }

    // Contact edit sheet (edit existing or add new)
    if (showEditSheet) {
        BottomSheetContainer(isDark = isDark, onDismiss = { showEditSheet = false }) {
            ContactEditSheetContent(
                contact = editContact,
                initialName = prefillName.takeIf { it.isNotEmpty() },
                initialPhone = prefillPhone.takeIf { it.isNotEmpty() },
                isDark = isDark,
                onSave = { fields ->
                    val target = editContact
                    if (target == null) {
                        contactViewModel.addContact(
                            role = fields.role.ifEmpty { "추가 연락처" },
                            name = fields.name,
                            phone = fields.phone,
                        )
                    } else if (target.deletable) {
                        contactViewModel.updateExtraContact(
                            id = target.id,
                            role = fields.role,
                            name = fields.name,
                            phone = fields.phone,
                        )
                    } else {
                        contactViewModel.updateContact(
                            id = target.id,
                            name = fields.name,
                            phone = fields.phone,
                        )
                    }
                },
                onDismiss = { showEditSheet = false },
            )
        }
    }

    // Action sheet for existing contact (직접 입력 / 연락처 가져오기)
    showActionSheetFor?.let { contact ->
        BottomActionSheet(
            title = "${contact.role} 수정",
            options = listOf(
                ActionSheetOption(Icons.Outlined.EditNote, "직접 입력", 0),
                ActionSheetOption(Icons.Outlined.Contacts, "연락처 가져오기", 1),
            ),
            isDark = isDark,
            onSelect = { choice ->
                showActionSheetFor = null
                if (choice == 0) openDirectEdit(contact, false)
                else launchContactPick(contact, false)
            },
            onDismiss = { showActionSheetFor = null },
        )
    }

    // Action sheet for add new contact
    if (showAddActionSheet) {
        BottomActionSheet(
            title = "연락처 추가",
            options = listOf(
                ActionSheetOption(Icons.Outlined.EditNote, "직접 입력", 0),
                ActionSheetOption(Icons.Outlined.Contacts, "연락처 가져오기", 1),
            ),
            isDark = isDark,
            onSelect = { choice ->
                showAddActionSheet = false
                if (choice == 0) openDirectEdit(null, true)
                else launchContactPick(null, true)
            },
            onDismiss = { showAddActionSheet = false },
        )
    }
}
