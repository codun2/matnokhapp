package com.matnokh.customer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.matnokh.customer.R
import com.matnokh.customer.net.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoggedIn: () -> Unit, onRegister: () -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFF3F6F2), C.bg))).verticalScroll(rememberScrollState()).safeDrawingPadding().padding(horizontal = 28.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(24.dp))
        Box(Modifier.size(84.dp).clip(RoundedCornerShape(26.dp)).background(Grad.green), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_truck, 44.dp, Color.White) }
        Spacer(Modifier.height(18.dp)); T(tr("تسجيل الدخول", "Log in"), 26, FontWeight.Black, C.head); Spacer(Modifier.height(4.dp)); T(tr("مرحباً بك في مطنوخ", "Welcome to Matnokh"), 13, FontWeight.Medium, C.muted)
        Spacer(Modifier.height(24.dp))
        OCard(Modifier.fillMaxWidth()) {
            FieldLabel(tr("رقم الهاتف", "Phone number")); FinField(phone, { phone = it }, "05xxxxxxxx", keyboard = KeyboardType.Phone)
            FieldLabel(tr("كلمة المرور", "Password")); FinField(password, { password = it }, "••••••", keyboard = KeyboardType.Password)
        }
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth()) {
            WideButton(if (loading) "…" else tr("دخول", "Log in"), if (loading) null else R.drawable.ic_back) {
                if (loading) return@WideButton
                scope.launch {
                    loading = true
                    try { val r = Net.api.login(LoginBody(phone = phone.trim(), password = password)); if (r.token != null) { Session.token = r.token; Session.name = r.customer?.name; Session.phone = r.customer?.phone; Session.avatar = r.customer?.avatar; Session.radius = (r.customer?.search_radius_km ?: 0.0).toFloat(); onLoggedIn() } else toast(r.message ?: tr("تعذّر الدخول", "Login failed")) }
                    catch (e: retrofit2.HttpException) { toast(errorMessage(e) ?: tr("بيانات غير صحيحة", "Invalid details")) } catch (e: Exception) { toast(tr("تعذّر الاتصال بالخادم", "Couldn't reach the server")) } finally { loading = false }
                }
            }
            if (loading) CircularProgressIndicator(Modifier.align(Alignment.CenterEnd).padding(end = 20.dp).size(20.dp), color = Color.White, strokeWidth = 2.dp)
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) { T(tr("ليس لديك حساب؟ ", "Don't have an account? "), 12, FontWeight.Medium, C.muted); T(tr("سجّل الآن", "Register now"), 12, FontWeight.ExtraBold, C.greenD, Modifier.clickable(onClick = onRegister)) }
        Spacer(Modifier.height(10.dp)); T(tr("تصفّح بدون حساب", "Browse without an account"), 12, FontWeight.Bold, C.muted, Modifier.clickable(onClick = onLoggedIn))
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun RegisterScreen(onDone: () -> Unit, onBack: () -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }; var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFF3F6F2), C.bg))).verticalScroll(rememberScrollState()).safeDrawingPadding().padding(horizontal = 28.dp, vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { HeaderIcon(R.drawable.ic_back, onBack); Spacer(Modifier.width(10.dp)); T(tr("حساب جديد", "New account"), 18, FontWeight.ExtraBold, C.head) }
        Spacer(Modifier.height(18.dp))
        Box(Modifier.size(72.dp).clip(RoundedCornerShape(22.dp)).background(Grad.green), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_user, 38.dp, Color.White) }
        Spacer(Modifier.height(18.dp))
        OCard(Modifier.fillMaxWidth()) {
            FieldLabel(tr("الاسم", "Name"), required = true); FinField(name, { name = it }, tr("الاسم الكامل", "Full name"))
            FieldLabel(tr("رقم الهاتف", "Phone number"), required = true); FinField(phone, { phone = it }, "05xxxxxxxx", keyboard = KeyboardType.Phone)
            FieldLabel(tr("البريد (اختياري)", "Email (optional)")); FinField(email, { email = it }, "you@example.com", keyboard = KeyboardType.Email)
            FieldLabel(tr("كلمة المرور", "Password"), required = true); FinField(password, { password = it }, tr("6 أحرف فأكثر", "6+ characters"), keyboard = KeyboardType.Password)
        }
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth()) {
            WideButton(if (loading) "…" else tr("إنشاء الحساب", "Create account"), if (loading) null else R.drawable.ic_check) {
                if (loading) return@WideButton
                if (name.isBlank() || phone.isBlank() || password.length < 6) { toast(tr("أكمل الحقول (كلمة المرور 6 فأكثر)", "Complete the fields (password 6+ chars)")); return@WideButton }
                scope.launch {
                    loading = true
                    try { val r = Net.api.register(RegisterBody(name.trim(), phone.trim(), email.trim().ifBlank { null }, password)); if (r.token != null) { Session.token = r.token; Session.name = r.customer?.name; Session.phone = r.customer?.phone; Session.avatar = r.customer?.avatar; Session.radius = (r.customer?.search_radius_km ?: 0.0).toFloat(); onDone() } else toast(r.message ?: tr("تعذّر التسجيل", "Registration failed")) }
                    catch (e: retrofit2.HttpException) { toast(errorMessage(e) ?: tr("تعذّر التسجيل", "Registration failed")) } catch (e: Exception) { toast(tr("تعذّر الاتصال بالخادم", "Couldn't reach the server")) } finally { loading = false }
                }
            }
            if (loading) CircularProgressIndicator(Modifier.align(Alignment.CenterEnd).padding(end = 20.dp).size(20.dp), color = Color.White, strokeWidth = 2.dp)
        }
        Spacer(Modifier.height(24.dp))
    }
}
