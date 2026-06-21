package app.skons.onsafe.ui.components

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.layout
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.skons.onsafe.data.ContactModel
import app.skons.onsafe.data.MyInfo
import app.skons.onsafe.ui.theme.AppColors
import app.skons.onsafe.ui.theme.LocalDarkTheme
import app.skons.onsafe.ui.theme.appThemeColors
import app.skons.onsafe.viewmodel.ContactViewModel
import app.skons.onsafe.viewmodel.LocationStatus
import app.skons.onsafe.viewmodel.LocationViewModel
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawer(
    contactViewModel: ContactViewModel,
    locationViewModel: LocationViewModel,
    onDismiss: () -> Unit,
) {
    BackHandler { onDismiss() }

    val ctx = LocalContext.current
    val appData by contactViewModel.data.collectAsStateWithLifecycle()
    val locState by locationViewModel.state.collectAsStateWithLifecycle()

    val c = appThemeColors()
    val isDark = LocalDarkTheme.current

    var showMyInfoEdit by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var editContact by remember { mutableStateOf<ContactModel?>(null) }
    var prefillName by remember { mutableStateOf("") }
    var prefillPhone by remember { mutableStateOf("") }
    var showActionSheetFor by remember { mutableStateOf<ContactModel?>(null) }
    var showAddActionSheet by remember { mutableStateOf(false) }
    var deleteConfirmContact by remember { mutableStateOf<ContactModel?>(null) }
    var pendingPickContact by remember { mutableStateOf<ContactModel?>(null) }
    var pendingPickIsNew by remember { mutableStateOf(false) }

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
        contactPickerLauncher.launch(null)
    }

    fun openDirectEdit(forContact: ContactModel?, isNew: Boolean) {
        prefillName = ""
        prefillPhone = ""
        editContact = if (isNew) null else forContact
        showEditSheet = true
    }

    val refreshRotation = remember { Animatable(0f) }
    LaunchedEffect(locState.fetching) {
        if (locState.fetching) {
            refreshRotation.snapTo(0f)
            while (true) {
                refreshRotation.animateTo(360f, tween(700, easing = LinearEasing))
                refreshRotation.snapTo(0f)
            }
        } else {
            refreshRotation.stop()
        }
    }

    val contacts = appData.contacts
    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val headerCount = 3
        val fIdx = from.index - headerCount
        val tIdx = to.index - headerCount
        if (fIdx >= 0 && tIdx >= 0 && fIdx < contacts.size && tIdx < contacts.size) {
            contactViewModel.reorderContacts(fIdx, tIdx)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.54f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onDismiss() },
        )

        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.88f)
                .align(Alignment.CenterStart),
            color = c.bg,
            shadowElevation = 10.dp,
        ) {
            Column(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(AppColors.AppBarYellow)
                        .statusBarsPadding()
                        .height(48.dp),
                ) {
                    Text(
                        "메뉴", fontSize = 20.sp, fontWeight = FontWeight.Bold,
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

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.weight(1f),
                ) {
                    item(key = "location") {
                        Column(
                            Modifier
                                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
                                .border(1.dp, c.border, RoundedCornerShape(10.dp))
                                .background(c.cardBg, RoundedCornerShape(10.dp)),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(c.badgeBg, RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp))
                                    .drawBehind {
                                        drawLine(c.border, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                                    }
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
                                    "위치", fontSize = 12.sp, fontWeight = FontWeight.W800, color = c.text,
                                    letterSpacing = 0.1.sp,
                                    modifier = Modifier.weight(1f),
                                )
                                Switch(
                                    checked = locState.locationEnabled,
                                    onCheckedChange = { locationViewModel.setLocationEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = c.blue,
                                        checkedTrackColor = c.blue.copy(alpha = 0.3f),
                                    ),
                                    modifier = Modifier
                                        .layout { measurable, constraints ->
                                            val placeable = measurable.measure(
                                                constraints.copy(minHeight = 0, maxHeight = Int.MAX_VALUE)
                                            )
                                            layout(placeable.width, 0) {
                                                placeable.placeRelative(0, -(placeable.height / 2))
                                            }
                                        }
                                        .scale(0.65f),
                                )
                            }
                            Column(Modifier.padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 12.dp)) {
                                Text("위치 사용 : 119 문자 신고 · 보고 양식", fontSize = 11.sp, color = c.sub)
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
                                        else -> c.sub
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.Refresh,
                                            contentDescription = null,
                                            tint = c.sub,
                                            modifier = Modifier
                                                .size(15.dp)
                                                .rotate(refreshRotation.value)
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

                    item(key = "myInfo") {
                        val myInfo = appData.myInfo
                        val hasInfo = myInfo.company.isNotEmpty() || myInfo.name.isNotEmpty()
                        Column(
                            Modifier
                                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                                .border(1.dp, c.border, RoundedCornerShape(10.dp))
                                .background(c.cardBg, RoundedCornerShape(10.dp)),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(c.badgeBg, RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp))
                                    .drawBehind {
                                        drawLine(c.border, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                                    }
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
                                    "내 정보", fontSize = 12.sp, fontWeight = FontWeight.W800, color = c.text,
                                    letterSpacing = 0.1.sp,
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
                                                Text("소속  ", fontSize = 12.sp, color = c.sub)
                                                Text(myInfo.company, fontSize = 15.sp, fontWeight = FontWeight.W600, color = c.text)
                                            }
                                        }
                                        if (myInfo.name.isNotEmpty()) {
                                            Spacer(Modifier.height(2.dp))
                                            Row {
                                                Text("이름  ", fontSize = 12.sp, color = c.sub)
                                                Text(myInfo.name, fontSize = 15.sp, fontWeight = FontWeight.W600, color = c.text)
                                            }
                                        }
                                    } else {
                                        Text("미입력", fontSize = 15.sp, color = c.hint)
                                    }
                                }
                                Box(
                                    Modifier
                                        .background(c.badgeBg, RoundedCornerShape(7.dp))
                                        .clickable { showMyInfoEdit = true }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                ) {
                                    Text("수정", fontSize = 12.sp, fontWeight = FontWeight.W700, color = c.badgeTx)
                                }
                            }
                        }
                    }

                    item(key = "dividerTop") {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 14.dp, start = 12.dp, end = 12.dp)
                                .height(1.dp)
                                .background(c.border),
                        )
                    }

                    items(contacts, key = { it.id }) { contact ->
                        ReorderableItem(reorderState, contact.id) { isDragging ->
                            val elevation = if (isDragging) 4.dp else 0.dp
                            Surface(
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 12.dp, bottom = 6.dp)
                                    .border(1.dp, c.border, RoundedCornerShape(10.dp)),
                                color = c.cardBg,
                                shape = RoundedCornerShape(10.dp),
                                shadowElevation = elevation,
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
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
                                        Text(contact.role, fontSize = 15.sp, color = c.sub, lineHeight = 20.sp)
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            if (hasName) contact.name else "이름 미입력",
                                            fontSize = 17.sp, fontWeight = FontWeight.W700,
                                            color = if (hasName) c.text else c.hint,
                                        )
                                        Spacer(Modifier.height(3.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Outlined.PhoneInTalk, contentDescription = null,
                                                tint = if (hasPhone) c.blue else c.hint,
                                                modifier = Modifier.size(with(LocalDensity.current) { 13.sp.toDp() }),
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                if (hasPhone) fmtPhone(contact.phone) else "번호 없음",
                                                fontSize = 13.sp,
                                                fontWeight = if (hasPhone) FontWeight.W600 else FontWeight.Normal,
                                                color = if (hasPhone) c.blue else c.hint,
                                            )
                                        }
                                    }
                                    if (contact.deletable) {
                                        Icon(
                                            Icons.Outlined.Delete, contentDescription = "삭제",
                                            tint = if (isDark) Color(0xFFFF8080) else Color(0xFFCC3333),
                                            modifier = Modifier
                                                .padding(start = 4.dp, top = 8.dp, bottom = 8.dp)
                                                .size(17.dp)
                                                .clickable { deleteConfirmContact = contact },
                                        )
                                    }
                                    Icon(
                                        Icons.Default.Edit, contentDescription = "수정",
                                        tint = c.sub,
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp, vertical = 8.dp)
                                            .size(17.dp)
                                            .clickable { showActionSheetFor = contact },
                                    )
                                    Box(
                                        Modifier
                                            .fillMaxHeight()
                                            .width(38.dp)
                                            .drawBehind {
                                                drawLine(
                                                    color = c.border,
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
                                            tint = c.hint, modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item(key = "bottomPad") { Spacer(Modifier.height(8.dp)) }
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(c.bg)
                        .navigationBarsPadding()
                        .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 12.dp),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, c.border, RoundedCornerShape(10.dp))
                            .clickable { showAddActionSheet = true }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Add, contentDescription = null, tint = c.sub, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("연락처 추가", color = c.sub, fontWeight = FontWeight.W600)
                        }
                    }
                }
            }
        }
    }

    if (showMyInfoEdit) {
        BottomSheetContainer(onDismiss = { showMyInfoEdit = false }) {
            ContactEditSheetContent(
                isMyInfo = true,
                myInfo = appData.myInfo,
                onSave = { fields ->
                    contactViewModel.updateMyInfo(
                        appData.myInfo.copy(name = fields.name, company = fields.company),
                    )
                },
                onDismiss = { showMyInfoEdit = false },
            )
        }
    }

    if (showEditSheet) {
        BottomSheetContainer(onDismiss = { showEditSheet = false }) {
            ContactEditSheetContent(
                contact = editContact,
                initialName = prefillName.takeIf { it.isNotEmpty() },
                initialPhone = prefillPhone.takeIf { it.isNotEmpty() },
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
                            role = fields.role.ifEmpty { "추가 연락처" },
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

    deleteConfirmContact?.let { contact ->
        BottomActionSheet(
            title = "${contact.role} 삭제",
            options = listOf(
                ActionSheetOption(Icons.Outlined.Delete, "삭제", 0),
                ActionSheetOption(Icons.Default.Close, "취소", 1),
            ),
            onSelect = { choice ->
                deleteConfirmContact = null
                if (choice == 0) contactViewModel.removeContact(contact.id)
            },
            onDismiss = { deleteConfirmContact = null },
        )
    }

    showActionSheetFor?.let { contact ->
        BottomActionSheet(
            title = "${contact.role} 수정",
            options = listOf(
                ActionSheetOption(Icons.Outlined.Contacts, "연락처 가져오기", 1),
                ActionSheetOption(Icons.Outlined.EditNote, "직접 입력", 0),
            ),
            onSelect = { choice ->
                showActionSheetFor = null
                if (choice == 0) openDirectEdit(contact, false)
                else launchContactPick(contact, false)
            },
            onDismiss = { showActionSheetFor = null },
        )
    }

    if (showAddActionSheet) {
        BottomActionSheet(
            title = "연락처 추가",
            options = listOf(
                ActionSheetOption(Icons.Outlined.Contacts, "연락처 가져오기", 1),
                ActionSheetOption(Icons.Outlined.EditNote, "직접 입력", 0),
            ),
            onSelect = { choice ->
                showAddActionSheet = false
                if (choice == 0) openDirectEdit(null, true)
                else launchContactPick(null, true)
            },
            onDismiss = { showAddActionSheet = false },
        )
    }
}
