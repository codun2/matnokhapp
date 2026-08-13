package com.matnokh.customer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import com.google.maps.android.compose.TileOverlay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

/* مزوّد بلاطات OpenStreetMap — يضمن ظهور الشوارع والأماكن فوق الخريطة دائماً */
private val tileClient = OkHttpClient()

class OsmTileProvider : TileProvider {
    override fun getTile(x: Int, y: Int, zoom: Int): Tile {
        if (zoom < 0 || zoom > 19) return TileProvider.NO_TILE
        return try {
            val req = Request.Builder()
                .url("https://tile.openstreetmap.org/$zoom/$x/$y.png")
                .header("User-Agent", "MatnokhCustomer/1.0 (Android; contact app)")
                .build()
            tileClient.newCall(req).execute().use { r ->
                val b = r.body?.bytes()
                if (!r.isSuccessful || b == null) TileProvider.NO_TILE else Tile(256, 256, b)
            }
        } catch (e: Exception) {
            TileProvider.NO_TILE
        }
    }
}

/* يوضع داخل محتوى GoogleMap ليرسم بلاطات الشوارع */
@Composable
fun OsmTiles() {
    // بلاطات جوجل مفعّلة الآن — أُزيلت طبقة OSM لأنها كانت تسبّب بطء/تعليق تحريك الخريطة
}

/* غلاف السحب-للتحديث: يظهر سبينر عند السحب للأسفل ويعيد تحميل محتوى الواجهة */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Refreshable(onRefresh: suspend () -> Unit, content: @Composable () -> Unit) {
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val state = rememberPullRefreshState(refreshing, {
        scope.launch { refreshing = true; runCatching { onRefresh() }; refreshing = false }
    })
    Box(Modifier.fillMaxSize().pullRefresh(state)) {
        content()
        PullRefreshIndicator(
            refreshing, state,
            Modifier.align(Alignment.TopCenter).statusBarsPadding(),
            backgroundColor = C.card, contentColor = C.greenD,
        )
    }
}

/* مؤشّر تحديث عام: أي شاشة تراقبه تعيد تحميل بياناتها عند السحب */
object RefreshBus { var tick by mutableStateOf(0) }
