package com.matnokh.driver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.driver.R
import com.matnokh.driver.net.LoginBody
import com.matnokh.driver.net.Net
import com.matnokh.driver.net.Session
import com.matnokh.driver.net.call
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoggedIn: () -> Unit, onRegister: () -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().background(C.bg).verticalScroll(rememberScrollState()).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(70.dp))
        Box(Modifier.size(96.dp).clip(RoundedCornerShape(28.dp)).background(Grad.green), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_van, 48.dp, Color.White) }
        Spacer(Modifier.height(18.dp))
        T("مطنوخ كابتن", 28, FontWeight.Black, C.head)
        Spacer(Modifier.height(4.dp)); T("سجّل الدخول لاستقبال الطلبات", 13, FontWeight.Medium, C.muted)
        Spacer(Modifier.height(30.dp))
        OCard(Modifier.fillMaxWidth()) {
            FieldLabel("رقم الجوال")
            FinField(phone, { phone = it }, "05xxxxxxxx", keyboard = KeyboardType.Phone)
            Spacer(Modifier.height(12.dp))
            FieldLabel("كلمة المرور")
            FinField(password, { password = it }, "••••••••", keyboard = KeyboardType.Password)
            Spacer(Modifier.height(18.dp))
            WideButton(if (loading) "جارٍ الدخول…" else "دخول", R.drawable.ic_nav) {
                if (loading) return@WideButton
                if (phone.isBlank() || password.isBlank()) { toast("أدخل رقم الجوال وكلمة المرور"); return@WideButton }
                loading = true
                scope.launch {
                    val resp = call({ Net.api.login(LoginBody("phone_password", phone = phone.trim(), password = password)) }, toast)
                    loading = false
                    if (resp?.token != null) { Session.save(resp); onLoggedIn() }
                    else if (resp?.message != null) toast(resp.message)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        T("سائق جديد؟ سجّل الآن", 13, FontWeight.Bold, C.greenD, Modifier.clickable(onClick = onRegister))
        Spacer(Modifier.height(30.dp))
    }
}
