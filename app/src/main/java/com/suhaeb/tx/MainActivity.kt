package com.suhaeb.tx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val Ink = Color(0xFF050806)
private val Panel = Color(0xFF111A14)
private val Green = Color(0xFF16F05A)
private val Muted = Color(0xFF98A69C)
private val Paper = Color(0xFFF4FFF6)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SuhaebChatApp() }
    }
}

private data class ChatItem(
    val id: String,
    val peerUid: String,
    val lastMessage: String,
    val updatedAt: Long
)

private data class ChatMessage(
    val id: String,
    val senderId: String,
    val text: String
)

@Composable
private fun SuhaebChatApp() {
    val context = LocalContext.current
    val configured = remember { FirebaseApp.getApps(context).isNotEmpty() }
    var user by remember { mutableStateOf(if (configured) FirebaseAuth.getInstance().currentUser else null) }
    var selected by remember { mutableIntStateOf(0) }
    MaterialTheme(colorScheme = darkColorScheme(background = Ink, surface = Panel, primary = Green, onBackground = Paper, onSurface = Paper)) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            when {
                !configured -> FirebaseSetupScreen()
                user == null -> AuthScreen(onAuthenticated = { user = FirebaseAuth.getInstance().currentUser })
                else -> MainArea(
                    uid = user!!.uid,
                    onSignOut = {
                        FirebaseAuth.getInstance().signOut()
                        user = null
                        selected = 0
                    }
                )
            }
        }
    }
}

@Composable
private fun FirebaseSetupScreen() {
    Column(Modifier.fillMaxSize().background(Ink).padding(26.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        BrandHeader()
        Spacer(Modifier.height(28.dp))
        Text("ربط Firebase مطلوب", color = Paper, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text("أضف ملف google-services.json الخاص بمشروع SUHAEB CHAT داخل مجلد app ثم أعد البناء. لا نستخدم مفاتيح أو بيانات مشروع وهمية.", color = Muted, textAlign = TextAlign.Center)
    }
}

@Composable
private fun AuthScreen(onAuthenticated: () -> Unit) {
    var create by remember { mutableStateOf(false) }
    var reset by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().background(Ink).verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(34.dp))
        BrandHeader()
        Spacer(Modifier.height(34.dp))
        Text(if (reset) "استعادة كلمة المرور" else if (create) "إنشاء حساب جديد" else "تسجيل الدخول", color = Paper, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        if (create && !reset) {
            AuthField(name, { name = it }, "الاسم الظاهر")
            Spacer(Modifier.height(10.dp))
        }
        AuthField(email, { email = it }, "البريد الإلكتروني", KeyboardType.Email)
        if (!reset) {
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(password, { password = it }, modifier = Modifier.fillMaxWidth(), label = { Text("كلمة المرور") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), colors = authFieldColors())
        }
        Spacer(Modifier.height(18.dp))
        Button(
            enabled = !busy && email.isNotBlank() && (reset || password.length >= 6) && (!create || name.isNotBlank()),
            onClick = {
                busy = true
                message = ""
                scope.launch {
                    try {
                        val auth = FirebaseAuth.getInstance()
                        when {
                            reset -> {
                                auth.sendPasswordResetEmail(email.trim()).await()
                                message = "إذا كان البريد مسجلاً، ستصلك رسالة استعادة كلمة المرور."
                            }
                            create -> {
                                val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
                                result.user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build())?.await()
                                result.user?.sendEmailVerification()?.await()
                                auth.signOut()
                                message = "أنشأنا الحساب وأرسلنا رابط التحقق. افتحه ثم سجّل الدخول."
                                create = false
                            }
                            else -> {
                                val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
                                val account = result.user
                                account?.reload()?.await()
                                if (account?.isEmailVerified != true) {
                                    auth.signOut()
                                    message = "تحقق من بريدك الإلكتروني أولاً، ثم أعد تسجيل الدخول."
                                } else onAuthenticated()
                            }
                        }
                    } catch (e: Exception) {
                        if (e is FirebaseAuthUserCollisionException) {
                            // The address already belongs to an account: guide the user to sign in.
                            create = false
                            reset = false
                            message = "هذا البريد مسجل مسبقاً. انتقلنا إلى تسجيل الدخول؛ أدخل كلمة المرور، أو اضغط «نسيت كلمة المرور؟» إذا نسيتها."
                        } else {
                            message = friendlyError(e)
                        }
                    }
                    busy = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)
        ) {
            if (busy) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Ink, strokeWidth = 2.dp)
            else Text(if (reset) "إرسال رابط الاستعادة" else if (create) "إنشاء الحساب" else "دخول", fontWeight = FontWeight.Bold)
        }
        if (message.isNotBlank()) {
            Spacer(Modifier.height(14.dp))
            Text(message, color = Muted, textAlign = TextAlign.Center, fontSize = 13.sp)
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { reset = !reset; create = false; message = "" }) { Text(if (reset) "العودة لتسجيل الدخول" else "نسيت كلمة المرور؟", color = Green) }
        if (!reset) TextButton(onClick = { create = !create; message = "" }) { Text(if (create) "لديك حساب؟ سجّل الدخول" else "إنشاء حساب جديد", color = Muted) }
    }
}

@Composable
private fun AuthField(value: String, onValueChange: (String) -> Unit, label: String, keyboard: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(value, onValueChange, modifier = Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = keyboard), colors = authFieldColors())
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Paper, unfocusedTextColor = Paper,
    focusedBorderColor = Green, unfocusedBorderColor = Muted,
    focusedLabelColor = Green, unfocusedLabelColor = Muted,
    cursorColor = Green
)

private fun friendlyError(error: Exception): String {
    val raw = error.message.orEmpty().lowercase()
    return when {
        "network" in raw -> "تحقق من اتصال الإنترنت وحاول مجدداً."
        "email address is already" in raw -> "هذا البريد مسجل مسبقاً."
        "password is invalid" in raw || "no user record" in raw || "invalid credential" in raw -> "البريد أو كلمة المرور غير صحيحة."
        "badly formatted" in raw -> "صيغة البريد الإلكتروني غير صحيحة."
        "weak password" in raw -> "كلمة المرور ضعيفة؛ استخدم 6 أحرف على الأقل."
        "permission" in raw -> "صلاحيات قاعدة البيانات لا تسمح بهذه العملية. يلزم نشر قواعد SUHAEB CHAT بعد مراجعتها."
        else -> "تعذر إكمال العملية. تحقق من اتصالك وإعداد Firebase."
    }
}

@Composable
private fun BrandHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("SUHAEB", color = Paper, fontSize = 30.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Text("CHAT  •  دردش بطريقتك", color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(14.dp))
        Box(Modifier.size(52.dp).background(Green, CircleShape), contentAlignment = Alignment.Center) {
            Text("S", color = Ink, fontWeight = FontWeight.Black, fontSize = 28.sp)
        }
    }
}

@Composable
private fun MainArea(uid: String, onSignOut: () -> Unit) {
    var profileReady by remember(uid) { mutableStateOf<Boolean?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var openedChat by remember { mutableStateOf<Pair<String, String>?>(null) }
    LaunchedEffect(uid) {
        profileReady = try {
            val profile = FirebaseFirestore.getInstance().collection("users").document(uid).get().await()
            profile.exists() && !profile.getString("username").isNullOrBlank()
        } catch (_: Exception) {
            false
        }
    }
    when (profileReady) {
        null -> Box(Modifier.fillMaxSize().background(Ink), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Green) }
        false -> ProfileSetup(uid = uid, onComplete = { profileReady = true }, onSignOut = onSignOut)
        true -> {
            if (openedChat != null) {
                ConversationScreen(
                    chatId = openedChat!!.first,
                    peerUid = openedChat!!.second,
                    myUid = uid,
                    onBack = { openedChat = null }
                )
            } else {
                Scaffold(
                    containerColor = Ink,
                    bottomBar = {
                        NavigationBar(containerColor = Panel, contentColor = Paper) {
                            NavigationBarItem(selectedTab == 0, { selectedTab = 0 }, icon = { Icon(Icons.Default.Chat, null) }, label = { Text("الدردشات") })
                            NavigationBarItem(selectedTab == 1, { selectedTab = 1 }, icon = { Icon(Icons.Default.Groups, null) }, label = { Text("المجموعات") })
                            NavigationBarItem(selectedTab == 2, { selectedTab = 2 }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("الإعدادات") })
                        }
                    }
                ) { padding ->
                    when (selectedTab) {
                        0 -> ChatsHome(Modifier.padding(padding), uid, onOpenChat = { id, peer -> openedChat = id to peer })
                        1 -> FeatureScreen("المجموعات", "المجموعات قيد التطوير؛ لم نعرض مجموعات وهمية.")
                        else -> SettingsHome(Modifier.padding(padding), onSignOut)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSetup(uid: String, onComplete: () -> Unit, onSignOut: () -> Unit) {
    var displayName by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser?.displayName.orEmpty()) }
    var username by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().background(Ink).padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        BrandHeader()
        Spacer(Modifier.height(28.dp))
        Text("جهّز ملفك الشخصي", color = Paper, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("اختر اسماً ظاهراً ومعرّفاً يستطيع أصدقاؤك البحث عنه.", color = Muted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(18.dp))
        AuthField(displayName, { displayName = it }, "الاسم الظاهر")
        Spacer(Modifier.height(10.dp))
        AuthField(username, { username = it.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' } }, "المعرّف — مثل suhaeb_1")
        Text("من 3 إلى 20 حرفاً أو رقماً أو _", color = Muted, fontSize = 11.sp, modifier = Modifier.fillMaxWidth())
        if (error.isNotBlank()) Text(error, color = Color(0xFFFF7777), textAlign = TextAlign.Center)
        Spacer(Modifier.height(18.dp))
        Button(
            enabled = !busy && displayName.isNotBlank() && username.matches(Regex("[a-z0-9_]{3,20}")),
            onClick = {
                busy = true
                error = ""
                scope.launch {
                    try {
                        val db = FirebaseFirestore.getInstance()
                        val userRef = db.collection("users").document(uid)
                        val handleRef = db.collection("usernames").document(username)
                        val existing = handleRef.get().await()
                        if (existing.exists()) {
                            error = "هذا المعرّف مستخدم. اختر معرّفاً آخر."
                        } else {
                            val batch = db.batch()
                            val profileSnapshot = userRef.get().await()
                            val profileData = mapOf(
                                "uid" to uid,
                                "displayName" to displayName.trim(),
                                "username" to username,
                                "createdAt" to (profileSnapshot.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                            if (profileSnapshot.exists()) batch.update(userRef, profileData)
                            else batch.set(userRef, profileData)
                            batch.set(handleRef, mapOf(
                                "uid" to uid,
                                "displayName" to displayName.trim(),
                                "username" to username,
                                "createdAt" to FieldValue.serverTimestamp()
                            ))
                            batch.commit().await()
                            onComplete()
                        }
                    } catch (e: Exception) {
                        error = friendlyError(e)
                    }
                    busy = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)
        ) {
            if (busy) CircularProgressIndicator(color = Ink, modifier = Modifier.size(18.dp))
            else Text("حفظ الملف الشخصي")
        }
        TextButton(onClick = onSignOut) { Text("تسجيل الخروج", color = Muted) }
    }
}

@Composable
private fun ChatsHome(modifier: Modifier, uid: String, onOpenChat: (String, String) -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }
    val chats = remember { mutableStateListOf<ChatItem>() }
    var handle by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    DisposableEffect(uid) {
        val listener: ListenerRegistration = db.collection("chats")
            .whereArrayContains("memberIds", uid)
            .addSnapshotListener { snapshot, exception ->
                loading = false
                if (exception != null) error = "تعذر تحميل الدردشات: تحقق من قواعد Firestore."
                else {
                    chats.clear()
                    snapshot?.documents?.forEach { doc ->
                        val members = doc.get("memberIds") as? List<*> ?: emptyList<Any>()
                        val peer = members.filterIsInstance<String>().firstOrNull { it != uid } ?: return@forEach
                        val stamp = doc.getTimestamp("updatedAt")?.toDate()?.time ?: 0L
                        chats.add(ChatItem(doc.id, peer, doc.getString("lastMessageText").orEmpty(), stamp))
                    }
                    chats.sortByDescending { it.updatedAt }
                    error = ""
                }
            }
        onDispose { listener.remove() }
    }

    Column(modifier.fillMaxSize().background(Ink).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(24.dp))
        BrandHeader()
        Spacer(Modifier.height(22.dp))
        Text("ابدأ محادثة", color = Paper, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(handle, { handle = it.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' } }, modifier = Modifier.weight(1f), singleLine = true, label = { Text("معرّف المستخدم") }, colors = authFieldColors())
            Button(
                enabled = !busy && handle.matches(Regex("[a-z0-9_]{3,20}")),
                onClick = {
                    busy = true
                    error = ""
                    scope.launch {
                        try {
                            val profile = db.collection("usernames").document(handle).get().await()
                            if (!profile.exists()) error = "لم نعثر على هذا المعرّف."
                            else {
                                val peerUid = profile.getString("uid").orEmpty()
                                if (peerUid == uid) error = "هذا معرّف حسابك."
                                else {
                                    val first = minOf(uid, peerUid)
                                    val second = maxOf(uid, peerUid)
                                    val chatId = "${first}_${second}"
                                    val chatRef = db.collection("chats").document(chatId)
                                    val current = chatRef.get().await()
                                    if (!current.exists()) {
                                        val now = FieldValue.serverTimestamp()
                                        chatRef.set(mapOf(
                                            "memberIds" to listOf(first, second),
                                            "createdAt" to now,
                                            "updatedAt" to now,
                                            "lastMessageText" to "",
                                            "lastMessageAt" to now,
                                            "lastMessageSenderId" to "",
                                            "lastMessageId" to ""
                                        )).await()
                                    } else {
                                        val members = current.get("memberIds") as? List<*>
                                        if (members == null || !members.contains(uid) || !members.contains(peerUid)) {
                                            error = "تعذر فتح المحادثة بسبب عدم تطابق العضوية."
                                        }
                                    }
                                    if (error.isBlank()) onOpenChat(chatId, peerUid)
                                }
                            }
                        } catch (e: Exception) {
                            error = friendlyError(e)
                        }
                        busy = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)
            ) {
                if (busy) CircularProgressIndicator(color = Ink, modifier = Modifier.size(18.dp))
                else Text("فتح")
            }
        }
        if (error.isNotBlank()) Text(error, color = Color(0xFFFF7777), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(22.dp))
        Text("محادثاتك", color = Paper, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        when {
            loading -> Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Green) }
            chats.isEmpty() -> Text("ما عندك محادثات بعد. اكتب معرّف صديقك بالأعلى لبدء أول محادثة.", color = Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 42.dp))
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
                items(chats, key = { it.id }) { chat ->
                    Surface(
                        onClick = { onOpenChat(chat.id, chat.peerUid) },
                        modifier = Modifier.fillMaxWidth(),
                        color = Panel,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).background(Green.copy(alpha = 0.16f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = Green)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("محادثة مع ${chat.peerUid.take(8)}", color = Paper, fontWeight = FontWeight.Bold)
                                Text(chat.lastMessage.ifBlank { "ابدأ بإرسال أول رسالة" }, color = Muted, maxLines = 1, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationScreen(chatId: String, peerUid: String, myUid: String, onBack: () -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }
    val messages = remember(chatId) { mutableStateListOf<ChatMessage>() }
    var draft by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    DisposableEffect(chatId) {
        val listener = db.collection("chats").document(chatId).collection("messages")
            .orderBy("createdAt")
            .limit(100)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) error = "تعذر تحميل الرسائل. تحقق من قواعد Firestore."
                else {
                    messages.clear()
                    snapshot?.documents?.forEach { doc ->
                        messages.add(ChatMessage(doc.id, doc.getString("senderId").orEmpty(), doc.getString("text").orEmpty()))
                    }
                    error = ""
                }
            }
        onDispose { listener.remove() }
    }

    Column(Modifier.fillMaxSize().background(Ink)) {
        Row(Modifier.fillMaxWidth().background(Panel).padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Green) }
            Column {
                Text("محادثة خاصة", color = Paper, fontWeight = FontWeight.Bold)
                Text("معرّف الطرف الآخر: ${peerUid.take(12)}", color = Muted, fontSize = 10.sp)
            }
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp), reverseLayout = false, contentPadding = PaddingValues(vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages, key = { it.id }) { message ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.senderId == myUid) Arrangement.Start else Arrangement.End) {
                    Surface(
                        color = if (message.senderId == myUid) Color(0xFF164B29) else Panel,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Text(message.text, color = Paper, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                    }
                }
            }
        }
        if (error.isNotBlank()) Text(error, color = Color(0xFFFF7777), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 14.dp))
        Row(Modifier.fillMaxWidth().background(Panel).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                draft,
                { if (it.length <= 4000) draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("اكتب رسالة...") },
                maxLines = 4,
                colors = authFieldColors()
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                enabled = draft.isNotBlank() && !sending,
                onClick = {
                    val text = draft.trim()
                    if (text.isNotEmpty()) {
                        sending = true
                        scope.launch {
                            try {
                                val chatRef = db.collection("chats").document(chatId)
                                val messageRef = chatRef.collection("messages").document()
                                val batch = db.batch()
                                batch.set(messageRef, mapOf(
                                    "senderId" to myUid,
                                    "text" to text,
                                    "createdAt" to FieldValue.serverTimestamp()
                                ))
                                batch.update(chatRef, mapOf(
                                    "lastMessageText" to text,
                                    "lastMessageAt" to FieldValue.serverTimestamp(),
                                    "lastMessageSenderId" to myUid,
                                    "lastMessageId" to messageRef.id,
                                    "updatedAt" to FieldValue.serverTimestamp()
                                ))
                                batch.commit().await()
                                draft = ""
                                error = ""
                            } catch (e: Exception) {
                                error = friendlyError(e)
                            }
                            sending = false
                        }
                    }
                }
            ) {
                if (sending) CircularProgressIndicator(color = Green, modifier = Modifier.size(20.dp))
                else Icon(Icons.Default.Send, contentDescription = "إرسال", tint = Green)
            }
        }
    }
}

@Composable
private fun SettingsHome(modifier: Modifier = Modifier, onSignOut: () -> Unit) {
    Column(modifier.fillMaxSize().background(Ink).padding(22.dp)) {
        Text("الإعدادات", color = Paper, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(18.dp))
        listOf(
            "الحساب والملف الشخصي" to "ملفك ومعرّفك محفوظان في SUHAEB CHAT",
            "الخصوصية والأمان" to "المحادثات متاحة لأعضائها فقط وفق قواعد Firestore",
            "الإشعارات" to "تنبيهات الرسائل ستضاف في مرحلة لاحقة",
            "اللغة والمظهر" to "العربية — أسود وأخضر"
        ).forEach { (title, subtitle) ->
            Surface(Modifier.fillMaxWidth().padding(bottom = 10.dp), color = Panel, shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(title, color = Paper, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(subtitle, color = Muted, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) { Text("تسجيل الخروج", color = Green) }
    }
}

@Composable
private fun FeatureScreen(title: String, subtitle: String) {
    Column(Modifier.fillMaxSize().background(Ink).padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Groups, contentDescription = null, tint = Green, modifier = Modifier.size(54.dp))
        Spacer(Modifier.height(18.dp))
        Text(title, color = Paper, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = Muted, textAlign = TextAlign.Center)
    }
}
