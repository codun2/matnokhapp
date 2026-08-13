@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.matnokh.tajer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.matnokh.tajer.R
import com.matnokh.tajer.net.Net
import com.matnokh.tajer.net.SectionBody
import com.matnokh.tajer.net.SectionDto
import com.matnokh.tajer.net.call
import kotlinx.coroutines.launch

@Composable
fun SectionsScreen(onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit, canWrite: Boolean = true) {
    val scope = rememberCoroutineScope()
    var sections by remember { mutableStateOf<List<SectionDto>?>(null) }
    var name by remember { mutableStateOf("") }
    var emoIdx by remember { mutableStateOf(0) }
    var icons by remember { mutableStateOf<List<String>>(emptyList()) }

    suspend fun load() { call({ Net.api.sections() }, toast)?.let { sections = it.sections }; call({ Net.api.sectionIcons() }, toast)?.let { icons = it.icons } }
    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("أقسام المتجر", onBack, onMenu)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                    OcTitle(R.drawable.ic_plus, "إضافة قسم جديد")
                    FieldLabel("اسم القسم", required = true)
                    FinField(name, { name = it }, "مثال: ألبان وأجبان")
                    FieldLabel("أيقونة القسم")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        if (icons.isEmpty()) T("جارٍ تحميل أيقونات التصنيف…", 11, androidx.compose.ui.text.font.FontWeight.Medium, C.muted)
                        else icons.take(12).forEachIndexed { i, e -> IconPick(e, emoIdx == i) { emoIdx = i } }
                    }
                    Spacer(Modifier.height(14.dp))
                    WideButton("أضف القسم", R.drawable.ic_plus) {
                        if (!canWrite) { toast("عذراً، يرجى إكمال عملية الدفع أو التواصل مع مدير التطبيق"); return@WideButton }
                        val n = name.trim()
                        if (n.isEmpty()) { toast("اكتب اسم القسم"); return@WideButton }
                        scope.launch {
                            call({ Net.api.addSection(SectionBody(n, icons.getOrElse(emoIdx) { "🏷️" })) }, toast)?.let {
                                name = ""; toast(it.message ?: "أُضيف القسم ✓"); load()
                            }
                        }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    T("الأقسام الحالية", 16, FontWeight.ExtraBold, C.head, Modifier.weight(1f))
                    sections?.let { StatusPill("${it.size}", PillKind.Live) }
                }
            }
            val list = sections
            if (list == null) item { Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) } }
            else items(list) { s ->
                ListRow(
                    leading = { SectionIcon(s.icon) },
                    title = s.name,
                    subtitle = "${s.products_count} منتجاً في هذا القسم",
                    trailing = {
                        Box(Modifier.size(34.dp).clip(RoundedCornerShape(12.dp)).background(C.redBg)
                            .clickable { scope.launch { call({ Net.api.deleteSection(s.id) }, toast)?.let { toast(it.message ?: "حُذف"); load() } } },
                            contentAlignment = Alignment.Center) { Ic(R.drawable.ic_x, 15.dp, C.redText) }
                    },
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun IconPick(icon: String, selected: Boolean, onClick: () -> Unit) {
    if (icon.startsWith("http")) {
        Box(Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(if (selected) C.pillLive else C.card2).border(2.dp, if (selected) C.green else C.line, RoundedCornerShape(14.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
            AsyncImage(model = icon, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)))
        }
    } else Emo(icon, selected, onClick)
}

@androidx.compose.runtime.Composable
private fun SectionIcon(icon: String?) {
    if (icon != null && icon.startsWith("http")) Box(Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(C.card2), contentAlignment = Alignment.Center) {
        AsyncImage(model = icon, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
    } else EmojiBox(icon ?: "🏷️", 44.dp, 15.dp, 20)
}
