package com.suhaeb.tx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
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

@Composable
private fun SuhaebChatApp() {
    val context = LocalContext.current
    val configured = remember { FirebaseApp.getApps(context).isNotEmpty() }
    var user by remember { mutableStateOf(if (configured) FirebaseAuth.getInstance().currentUser else null) }
    var selected by remember { mutableIntStateOf(0) }
    MaterialTheme(colorScheme = darkColorScheme(background = Ink, surface = Panel, primary = Green, onBackground = Paper, onSurface = Paper)) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            if (!configured) {
                FirebaseSetupScreen()
            } else if (user == null) {
                AuthScreen(onAuthenticated = { user = FirebaseAuth.getInstance().currentUser })
            } else {
                Scaffold(
                    containerColor = Ink,
                    bottomBar = {
                        NavigationBar(containerColor = Panel, contentColor = Paper) {
                            NavigationBarItem(selected == 0, { selected = 0 }, icon = { Icon(Icons.Default.Chat, null) }, label = { Text("الدردشات") })
                            NavigationBarItem(selected == 1, { selected = 1 }, icon = { Icon(Icons.Default.Groups, null) }, label = { Text("المجموعات") })
                            NavigationBarItem(selected == 2, { selected = 2 }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("الإعدادات") })
                        }
                    }
                ) { padding ->
                    when (selected) {
                        0 -> ChatsHome(Modifier.padding(padding), user!!.uid)
                        1 -> EmptyFeatureScreen("المجموعات", "سيظهر هنا إنشاء المجموعات وإدارتها بعد تفعيل المرحلة التالية")
                        else -> SettingsHome(Modifier.padding(padding), onSignOut = {
                            FirebaseAuth.getInstance().signOut()
                            user = null
                        })
                    }
                }
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
        Text("أضف ملف google-services.json الخاص بمشروع SUHAEB CHAT داخل مجلد app ثم أعد البناء. لم نضع مفاتيح أو بيانات مشروع وهمية داخل التطبيق.", color = Muted, textAlign = TextAlign.Center)
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
    Column(Modifier.fillMaxSize().background(Ink).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(34.dp))
        BrandHeader()
        Spacer(Modifier.height(34.dp))
        Text(if (reset) "استعادة كلمة المرور" else if (create) "إنشاء حساب جديد" else "تسجيل الدخول", color = Paper, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        if (create && !reset) {
            AuthField(name, { name = it }, "الاسم الظاهر")
            Spacer(Modifier.height(10.dp))
        }
        AuthField(email, { email = it }, "البريد الإلكتروني")
        if (!reset) {
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(password, { password = it }, modifier = Modifier.fillMaxWidth(), label = { Text("كلمة المرور") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), colors = authFieldColors())
        }
        Spacer(Modifier.height(18.dp))
        Button(
            enabled = !busy && email.isNotBlank() && (reset || password.length >= 6) && (!create || name.isNotBlank()),
            onClick = {
                busy = true; message = ""
                scope.launch {
                    try {
                        val auth = FirebaseAuth.getInstance()
                        when {
                            reset -> { auth.sendPasswordResetEmail(email.trim()).await(); message = "إذا كان البريد مسجلاً، ستصلك رسالة استعادة كلمة المرور." }
                            create -> {
                                val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
                                result.user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build())?.await()
                                result.user?.sendEmailVerification()?.await()
                                auth.signOut()
                                message = "أنشأنا الحساب وأرسلنا رابط التحقق إلى بريدك. افتح الرابط ثم سجّل الدخول."
                                create = false
                            }
                            else -> {
                                val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
                                val account = result.user
                                account?.reload()?.await()
                                if (account?.isEmailVerified != true) {
                                    auth.signOut()
                                    message = "تحقق من بريدك الإلكتروني أولاً، ثم أعد تسجيل الدخول."
                                } else {
                                    val userDoc = FirebaseFirestore.getInstance().collection("users").document(account.uid)
                                    val existingProfile = userDoc.get().await()
                                    val profileData = mutableMapOf<String, Any>(
                                        "uid" to account.uid,
                                        "displayName" to (account.displayName ?: account.email.orEmpty().substringBefore("@")),
                                        "updatedAt" to FieldValue.serverTimestamp()
                                    )
                                    if (!existingProfile.exists()) {
                                        profileData["createdAt"] = FieldValue.serverTimestamp()
                                        userDoc.set(profileData).await()
                                    } else {
                                        userDoc.update("updatedAt", FieldValue.serverTimestamp()).await()
                                    }
                                    onAuthenticated()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        message = friendlyAuthError(e)
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
            Text(message, color = if (message.contains("أرسلنا") || message.contains("ستصلك")) Green else Muted, textAlign = TextAlign.Center, fontSize = 13.sp)
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { reset = !reset; create = false; message = "" }) { Text(if (reset) "العودة لتسجيل الدخول" else "نسيت كلمة المرور؟", color = Green) }
        if (!reset) TextButton(onClick = { create = !create; message = "" }) { Text(if (create) "لديك حساب؟ سجّل الدخول" else "إنشاء حساب جديد", color = Muted) }
    }
}

@Composable
private fun AuthField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(value, onValueChange, modifier = Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true, colors = authFieldColors())
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Paper, unfocusedTextColor = Paper,
    focusedBorderColor = Green, unfocusedBorderColor = Muted,
    focusedLabelColor = Green, unfocusedLabelColor = Muted,
    cursorColor = Green
)

private fun friendlyAuthError(error: Exception): String {
    val raw = error.message.orEmpty().lowercase()
    return when {
        "network" in raw -> "تحقق من اتصال الإنترنت وحاول مجدداً."
        "email address is already" in raw -> "هذا البريد مسجل مسبقاً."
        "password is invalid" in raw || "no user record" in raw || "invalid credential" in raw -> "البريد أو كلمة المرور غير صحيحة."
        "badly formatted" in raw -> "صيغة البريد الإلكتروني غير صحيحة."
        "weak password" in raw -> "كلمة المرور ضعيفة؛ استخدم 6 أحرف على الأقل."
        else -> "تعذر إكمال العملية. تحقق من إعداد Firebase وحاول مجدداً."
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
private fun ChatsHome(modifier: Modifier = Modifier, uid: String) {
    var chatCount by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    DisposableEffect(uid) {
        val registration = FirebaseFirestore.getInstance().collection("chats")
            .whereArrayContains("memberIds", uid)
            .addSnapshotListener { snapshot, exception ->
                loading = false
                if (exception != null) error = "تعذر تحميل المحادثات. تحقق من قواعد Firestore."
                else { chatCount = snapshot?.size() ?: 0; error = "" }
            }
        onDispose { registration.remove() }
    }
    Column(modifier.fillMaxSize().background(Ink).padding(horizontal = 22.dp)) {
        Spacer(Modifier.height(30.dp))
        BrandHeader()
        Spacer(Modifier.height(24.dp))
        Surface(Modifier.fillMaxWidth(), color = Panel, shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(18.dp)) {
                Text("محادثاتك", color = Paper, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("الحساب: $uid", color = Muted, fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(20.dp))
        when {
            loading -> CircularProgressIndicator(color = Green, modifier = Modifier.align(Alignment.CenterHorizontally))
            error.isNotBlank() -> Text(error, color = Muted)
            chatCount == 0 -> Text("ما عندك محادثات حالياً. ابدأ بإضافة جهة اتصال بعد إكمال ميزة البحث عن المستخدمين.", color = Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 70.dp))
            else -> Text("عدد محادثاتك: $chatCount", color = Green, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SettingsHome(modifier: Modifier = Modifier, onSignOut: () -> Unit) {
    Column(modifier.fillMaxSize().background(Ink).padding(22.dp)) {
        Text("الإعدادات", color = Paper, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(18.dp))
        listOf("الحساب والملف الشخصي" to "حسابك موثق بالبريد الإلكتروني", "الخصوصية والأمان" to "سيتم تفعيل إعدادات الخصوصية تدريجياً", "الإشعارات" to "تنبيهات الرسائل والمجموعات", "اللغة والمظهر" to "العربية — أسود وأخضر").forEach { (title, subtitle) ->
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
private fun EmptyFeatureScreen(title: String, subtitle: String) {
    Column(Modifier.fillMaxSize().background(Ink).padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Groups, contentDescription = null, tint = Green, modifier = Modifier.size(54.dp))
        Spacer(Modifier.height(18.dp))
        Text(title, color = Paper, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = Muted, textAlign = TextAlign.Center)
    }
}
