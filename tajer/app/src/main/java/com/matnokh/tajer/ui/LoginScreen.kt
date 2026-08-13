@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.matnokh.tajer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.matnokh.tajer.R
import com.matnokh.tajer.net.LoginBody
import com.matnokh.tajer.net.Net
import com.matnokh.tajer.net.Session
import com.matnokh.tajer.net.errorMessage
import kotlinx.coroutines.launch

private val METHOD_LABELS = mapOf(
    "phone_password" to "هاتف",
    "email_password" to "بريد",
    "phone_otp" to "رمز SMS",
)

@Composable
fun LoginScreen(onLoggedIn: () -> Unit, onRegister: () -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var methods by remember { mutableStateOf(listOf("phone_password")) }
    var method by remember { mutableStateOf("phone_password") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        runCatching { Net.api.loginMethods() }.onSuccess {
            methods = it.methods.filter { m -> m != "phone_otp" }.ifEmpty { listOf("phone_password") }
            method = methods.first()
        }
    }

    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFF3F6F2), C.bg)))
            .verticalScroll(rememberScrollState()).safeDrawingPadding().padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        Box(Modifier.size(84.dp).clip(RoundedCornerShape(26.dp)).background(Grad.green), contentAlignment = Alignment.Center) {
            Ic(R.drawable.ic_shop, 44.dp, Color.White)
        }
        Spacer(Modifier.height(18.dp))
        T("تسجيل الدخول", 26, FontWeight.Black, C.head)
        Spacer(Modifier.height(4.dp))
        T("ادخل إلى متجرك على مطنوخ", 13, FontWeight.Medium, C.muted)
        Spacer(Modifier.height(24.dp))

        // اختيار الطريقة (إن كانت أكثر من واحدة)
        if (methods.size > 1) {
            Row(
                Modifier.fillMaxWidth().clip(CircleShape).background(C.card).padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                methods.forEach { m ->
                    val on = method == m
                    Box(
                        Modifier.weight(1f).clip(CircleShape).then(if (on) Modifier.background(Grad.green) else Modifier)
                            .clickable { method = m; otpSent = false }.padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) { T(METHOD_LABELS[m] ?: m, 11, FontWeight.ExtraBold, if (on) Color.White else C.muted) }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        OCard(Modifier.fillMaxWidth()) {
            when (method) {
                "email_password" -> {
                    FieldLabel("البريد الإلكتروني")
                    FinField(email, { email = it }, "you@example.com", keyboard = KeyboardType.Email)
                    FieldLabel("كلمة المرور")
                    FinField(password, { password = it }, "••••••", keyboard = KeyboardType.Password)
                }
                "phone_otp" -> {
                    FieldLabel("رقم الهاتف")
                    FinField(phone, { phone = it }, "05xxxxxxxx", keyboard = KeyboardType.Phone)
                    if (otpSent) {
                        FieldLabel("رمز التحقق")
                        FinField(code, { code = it }, "____", keyboard = KeyboardType.Number)
                    }
                }
                else -> {
                    FieldLabel("رقم الهاتف")
                    FinField(phone, { phone = it }, "05xxxxxxxx", keyboard = KeyboardType.Phone)
                    FieldLabel("كلمة المرور")
                    FinField(password, { password = it }, "••••••", keyboard = KeyboardType.Password)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        val primaryLabel = if (method == "phone_otp" && !otpSent) "إرسال الرمز" else "دخول"
        Box(Modifier.fillMaxWidth()) {
            WideButton(if (loading) "…" else primaryLabel, if (loading) null else R.drawable.ic_back) {
                if (loading) return@WideButton
                scope.launch {
                    loading = true
                    try {
                        if (method == "phone_otp" && !otpSent) {
                            val r = Net.api.requestOtp(mapOf("phone" to phone))
                            otpSent = true
                            toast(r.dev_code?.let { "رمز التطوير: $it" } ?: (r.message ?: "أُرسل الرمز"))
                        } else {
                            val body = when (method) {
                                "email_password" -> LoginBody("email_password", email = email, password = password)
                                "phone_otp" -> LoginBody("phone_otp", phone = phone, code = code)
                                else -> LoginBody("phone_password", phone = phone, password = password)
                            }
                            val r = Net.api.login(body)
                            if (r.token != null) { Session.save(r); onLoggedIn() }
                            else toast(r.message ?: "تعذّر الدخول")
                        }
                    } catch (e: retrofit2.HttpException) {
                        toast(errorMessage(e) ?: "بيانات غير صحيحة")
                    } catch (e: Exception) {
                        toast("تعذّر الاتصال بالخادم")
                    } finally { loading = false }
                }
            }
            if (loading) CircularProgressIndicator(Modifier.align(Alignment.CenterEnd).padding(end = 20.dp).size(20.dp), color = Color.White, strokeWidth = 2.dp)
        }

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            T("ليس لديك متجر؟ ", 12, FontWeight.Medium, C.muted)
            T("سجّل متجرك", 12, FontWeight.ExtraBold, C.greenD, Modifier.clickable(onClick = onRegister))
        }
        Spacer(Modifier.height(24.dp))
    }
}
