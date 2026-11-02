package com.suhaeb.tx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF090A0D)
private val Panel = Color(0xFF15171D)
private val Accent = Color(0xFFE51B2B)
private val Muted = Color(0xFF9296A1)
private val Paper = Color(0xFFF5F5F7)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SuhaebTxApp() }
    }
}

@Composable
private fun SuhaebTxApp() {
    var selected by remember { mutableIntStateOf(0) }
    MaterialTheme(colorScheme = darkColorScheme(
        background = Ink, surface = Panel, primary = Accent,
        onBackground = Paper, onSurface = Paper
    )) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                containerColor = Ink,
                bottomBar = {
                    NavigationBar(containerColor = Panel, contentColor = Paper) {
                        NavigationBarItem(selected == 0, { selected = 0 }, icon = { Icon(Icons.Default.Chat, null) }, label = { Text("الدردشات") })
                        NavigationBarItem(selected == 1, { selected = 1 }, icon = { Icon(Icons.Default.Groups, null) }, label = { Text("المجموعات") })
                        NavigationBarItem(selected == 2, { selected = 2 }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("الإعدادات") })
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { selected = 0 }, containerColor = Accent, contentColor = Color.White) {
                        Icon(Icons.Default.Add, contentDescription = "محادثة جديدة")
                    }
                }
            ) { padding ->
                when (selected) {
                    0 -> ChatsScreen(Modifier.padding(padding))
                    1 -> PlaceholderScreen("المجموعات", "ستظهر مجموعاتك هنا بعد إعداد الخدمة")
                    else -> PlaceholderScreen("الإعدادات", "الملف الشخصي والخصوصية وإعدادات الحساب")
                }
            }
        }
    }
}

@Composable
private fun ChatsScreen(modifier: Modifier = Modifier) {
    val sampleChats = listOf(
        ChatRow("سارة", "أهلاً! كيفك؟", "10:42", "س", true),
        ChatRow("أحمد", "وصلت الصورة، شكراً", "09:18", "أ", false),
        ChatRow("فريق SUHAEB", "تم إنشاء المجموعة", "أمس", "S", false),
        ChatRow("محمد", "مكالمة صوتية فائتة", "أمس", "م", false)
    )
    Column(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("SUHAEB", color = Paper, fontSize = 25.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Text("TX  •  تواصل بطريقتك", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Box(Modifier.size(42.dp).background(Accent, CircleShape), contentAlignment = Alignment.Center) {
                Text("S", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
            }
        }
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = "", onValueChange = {}, readOnly = true,
            placeholder = { Text("ابحث عن محادثة", color = Muted) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Muted) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Panel, focusedContainerColor = Panel,
                unfocusedBorderColor = Panel, focusedBorderColor = Accent
            )
        )
        Spacer(Modifier.height(26.dp))
        Text("رسائلك", color = Paper, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Text("مساحة آمنة لمحادثاتك", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sampleChats) { chat ->
                Row(
                    Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp))
                        .clickable { }.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(52.dp).background(if (chat.online) Accent.copy(alpha = .22f) else Color(0xFF292C35), CircleShape), contentAlignment = Alignment.Center) {
                        Text(chat.initial, color = Paper, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(chat.name, color = Paper, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(chat.message, color = Muted, fontSize = 13.sp)
                    }
                    Text(chat.time, color = Muted, fontSize = 11.sp)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Text("واجهة أولية — المحادثات المعروضة تجريبية وليست رسائل حقيقية بعد.",
            color = Muted, fontSize = 11.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))
    }
}

private data class ChatRow(val name: String, val message: String, val time: String, val initial: String, val online: Boolean)

@Composable
private fun PlaceholderScreen(title: String, subtitle: String) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = Paper, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = Muted, textAlign = TextAlign.Center)
    }
}
