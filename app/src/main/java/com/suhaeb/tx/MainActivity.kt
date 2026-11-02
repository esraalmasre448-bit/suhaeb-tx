package com.suhaeb.tx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
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
    var selected by remember { mutableIntStateOf(0) }
    MaterialTheme(colorScheme = darkColorScheme(
        background = Ink, surface = Panel, primary = Green,
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
                }
            ) { padding ->
                when (selected) {
                    0 -> ChatsHome(Modifier.padding(padding))
                    1 -> EmptyFeatureScreen("المجموعات", "ستظهر مجموعاتك هنا بعد تفعيل الحساب والخدمة السحابية")
                    else -> SettingsHome(Modifier.padding(padding))
                }
            }
        }
    }
}

@Composable
private fun ChatsHome(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().background(Ink).padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(30.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("SUHAEB", color = Paper, fontSize = 27.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Text("CHAT  •  دردش بطريقتك", color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Box(Modifier.size(48.dp).background(Green, CircleShape), contentAlignment = Alignment.Center) {
                Text("S", color = Ink, fontWeight = FontWeight.Black, fontSize = 25.sp)
            }
        }
        Spacer(Modifier.height(26.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Panel,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Green.copy(alpha = .22f))
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("أهلاً بك في SUHAEB CHAT", color = Paper, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("مساحة تواصل مستقلة بهويتك، وحساباتك ومحادثاتك الخاصة.", color = Muted, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        Box(
            Modifier.size(92.dp).background(Panel, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Chat, contentDescription = null, tint = Green, modifier = Modifier.size(42.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text("لسه ما عندك محادثات", color = Paper, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "بعد ربط Firebase وتفعيل التسجيل، رح تقدر تبدأ محادثات حقيقية وتتزامن رسائلك عبر السحابة.",
            color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.weight(1f))
        Text(
            "نسخة تأسيسية — لا توجد رسائل تجريبية أو حسابات وهمية.",
            color = Muted, fontSize = 10.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
        )
    }
}

@Composable
private fun SettingsHome(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().background(Ink).padding(22.dp)) {
        Text("الإعدادات", color = Paper, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(18.dp))
        listOf(
            "الحساب والملف الشخصي" to "تسجيل الدخول والاسم والصورة واسم المستخدم",
            "الخصوصية والأمان" to "إعدادات الظهور والقراءة وقفل المحادثات",
            "الإشعارات" to "تنبيهات الرسائل والمجموعات",
            "اللغة والمظهر" to "العربية والإنجليزية والثيمات",
            "حول التطبيق" to "SUHAEB CHAT"
        ).forEach { (title, subtitle) ->
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                color = Panel, shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(title, color = Paper, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(subtitle, color = Muted, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("هذه الأقسام ستُفعّل تدريجياً بعد إعداد خدمات الحساب وقاعدة البيانات.", color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun EmptyFeatureScreen(title: String, subtitle: String) {
    Column(
        Modifier.fillMaxSize().background(Ink).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Groups, contentDescription = null, tint = Green, modifier = Modifier.size(54.dp))
        Spacer(Modifier.height(18.dp))
        Text(title, color = Paper, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = Muted, textAlign = TextAlign.Center)
    }
}
