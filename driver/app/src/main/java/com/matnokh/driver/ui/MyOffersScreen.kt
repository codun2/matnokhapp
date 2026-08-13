package com.matnokh.driver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.driver.R

@Composable
fun MyOffersScreen(onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    var jobs by remember { mutableStateOf<List<Job>?>(null) }
    LaunchedEffect(Unit) { jobs = repoMyOffers(toast) }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("عروضي المقدَّمة", onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(6.dp))
            val list = jobs
            when {
                list == null -> Box(Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }
                list.isEmpty() -> Box(Modifier.fillMaxWidth().padding(top = 70.dp), contentAlignment = Alignment.Center) { T("لا توجد عروض مقدَّمة بانتظار اختيار الزبون", 13, FontWeight.Medium, C.muted) }
                else -> list.forEach { j -> OfferCard(j) }
            }
            Spacer(Modifier.height(90.dp))
        }
    }
}

@Composable
private fun OfferCard(job: Job) {
    Column(Modifier.padding(horizontal = 22.dp).padding(bottom = 12.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).padding(15.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GradBadge(job.iconId, jobGradients[job.gradient])
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) { T(job.svc, 13, FontWeight.Bold, C.head, maxLines = 1); T("#${job.id} · ${job.cust} · ${job.km} كم", 10, FontWeight.Normal, C.muted, maxLines = 1) }
            StatusPill("بانتظار اختيار الزبون", PillKind.Wait)
        }
        Spacer(Modifier.height(11.dp)); RouteBox(job.from, job.to); Spacer(Modifier.height(11.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Ic(R.drawable.ic_cash, 15.dp, C.greenD); Spacer(Modifier.width(6.dp))
            T("عرضك المقدَّم: ﷼${job.price}", 12, FontWeight.ExtraBold, C.greenD)
        }
    }
}
