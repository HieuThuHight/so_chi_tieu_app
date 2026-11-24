package com.example.glassfinance.ui.components

import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight

import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassfinance.ui.theme.FinanceTheme
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Constraints
import kotlin.math.roundToInt

/** Độ đặc của thẻ kính, lấy từ Cài đặt (0.3 = rất trong, 0.95 = gần như đặc). */
val LocalGlassOpacity = compositionLocalOf { 0.86f }

/** Độ đặc của thanh điều hướng dưới cùng, chỉnh riêng trong Cài đặt. */
val LocalNavBarOpacity = compositionLocalOf { 0.06f }

/** Backdrop dùng chung: chỉ ghi lại nền trang, không ghi lại các mặt kính. */
val LocalLiquidBackdrop = compositionLocalOf<Backdrop?> { null }

/**
 * Hiệu ứng "nhún" khi bấm: thu nhỏ nhẹ lúc nhấn xuống rồi bật về bằng spring.
 * Dùng trong graphicsLayer: `Modifier.graphicsLayer { scaleX = s.value; scaleY = s.value }`
 * (đọc State trong khối layer nên không làm recompose cả cây).
 */
@Composable
fun rememberPressScale(source: InteractionSource, pressedScale: Float = 0.96f): State<Float> {
    val pressed by source.collectIsPressedAsState()
    return animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pressScale"
    )
}

/**
 * Nền phía sau toàn app: gradient + vài quầng màu mềm trôi chậm.
 * Kính chỉ "trông như kính" khi có thứ gì đó phía sau để nhìn xuyên qua – chính các quầng màu này
 * làm thanh "Độ trong suốt của kính" ở Cài đặt có tác dụng nhìn thấy được.
 */
@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = FinanceTheme.colors
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val backdrop = rememberLayerBackdrop()

    val transition = rememberInfiniteTransition(label = "glassOrbs")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    val mint = if (dark) Color(0xFF2FA37A) else Color(0xFF6FD3AE)
    val sand = if (dark) Color(0xFFB98243) else Color(0xFFF5C77E)
    val sky = if (dark) Color(0xFF3F7E9E) else Color(0xFF8FC3EA)
    val lilac = if (dark) Color(0xFF6C5AA8) else Color(0xFFC2B0EE)
    val strength = if (dark) 0.42f else 0.62f

    // Quan trọng: backdrop chỉ ghi nền ở Box thứ nhất. Nội dung và các mặt kính
    // được vẽ ở Box anh em thứ hai để tránh vòng lặp shader.
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .layerBackdrop(backdrop)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(colors.backgroundTop, colors.backgroundBottom)
                    )
                )
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    fun orb(cx: Float, cy: Float, r: Float, c: Color) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(c.copy(alpha = strength), Color.Transparent),
                                center = Offset(cx, cy),
                                radius = r
                            ),
                            radius = r,
                            center = Offset(cx, cy)
                        )
                    }
                    orb(w * (0.10f + 0.16f * drift), h * 0.10f, w * 0.80f, mint)
                    orb(w * (1.00f - 0.18f * drift), h * (0.34f + 0.05f * drift), w * 0.70f, sand)
                    orb(w * (0.05f + 0.10f * drift), h * (0.66f - 0.06f * drift), w * 0.85f, sky)
                    orb(w * (0.95f - 0.12f * drift), h * 0.94f, w * 0.75f, lilac)
                }
        )

        androidx.compose.runtime.CompositionLocalProvider(
            LocalLiquidBackdrop provides backdrop
        ) {
            Box(modifier = Modifier.fillMaxSize(), content = content)
        }
    }
}

/**
 * Thẻ liquid glass: nền hơi đục (độ đặc theo Cài đặt) + vệt sáng chéo như phản chiếu +
 * viền sáng bên trong (sáng ở góc trên trái, tối dần xuống dưới phải) + viền ngoài mảnh.
 * Truyền [onClick] để thẻ bấm được (có hiệu ứng nhún).
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.surface.luminance() < 0.5f
    val opacity = LocalGlassOpacity.current.coerceIn(0.30f, 0.94f)
    val backdrop = LocalLiquidBackdrop.current
    val edge = scheme.outlineVariant
    val border = BorderStroke(
        1.dp,
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = if (dark) 0.55f else 0.90f),
                edge.copy(alpha = 0.24f)
            )
        )
    )
    val hi = Color.White.copy(alpha = if (dark) 0.34f else 0.78f)
    val lo = Color.White.copy(alpha = if (dark) 0.05f else 0.18f)
    val sheen = Brush.linearGradient(
        colorStops = arrayOf(
            0f to Color.White.copy(alpha = if (dark) 0.12f else 0.42f),
            0.42f to Color.Transparent,
            1f to Color.Transparent
        )
    )

    val glassSurface = if (backdrop != null) {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                vibrancy()
                blur(12.dp.toPx())
                lens(14.dp.toPx(), 28.dp.toPx())
            },
            highlight = { Highlight.Default },
            onDrawSurface = {
                drawRect(scheme.surface.copy(alpha = opacity * 0.52f))
            }
        )
    } else {
        Modifier.background(scheme.surface.copy(alpha = opacity), shape)
    }

    val glassInner = Modifier
        .background(sheen)
        .drawWithContent {
            drawContent()
            val stroke = 1.35.dp.toPx()
            drawRoundRect(
                brush = Brush.linearGradient(listOf(hi, lo, lo.copy(alpha = lo.alpha * 1.5f))),
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(size.width - stroke, size.height - stroke),
                cornerRadius = CornerRadius(24.dp.toPx() - stroke / 2),
                style = Stroke(width = stroke)
            )
        }

    if (onClick != null) {
        val source = remember { MutableInteractionSource() }
        val scale = rememberPressScale(source, 0.97f)
        Surface(
            onClick = onClick,
            modifier = modifier
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
                .then(glassSurface),
            shape = shape,
            color = Color.Transparent,
            contentColor = scheme.onSurface,
            border = border,
            interactionSource = source
        ) {
            Column(modifier = glassInner, content = content)
        }
    } else {
        Surface(
            modifier = modifier.then(glassSurface),
            shape = shape,
            color = Color.Transparent,
            contentColor = scheme.onSurface,
            border = border
        ) {
            Column(modifier = glassInner, content = content)
        }
    }
}

/** Kích thước thanh điều hướng, dùng chung để Scaffold chừa đúng chỗ cho thanh nổi. */
val GlassNavBarHeight = 76.dp

val GlassNavBarVerticalPadding = 10.dp

/**
 * Thanh điều hướng liquid glass kiểu Apple Music (thanh dài, bo góc, lơ lửng phía trên nội dung):
 *  - Thanh dài hình chữ nhật bo góc, căn giữa, nổi lên trên nội dung màn hình.
 *  - Nếu [selected] nằm ngoài danh sách (ví dụ đang ở Cài đặt) thì viên chọn ẩn đi.
 *  - Viên chọn tab là liquid glass (có khúc xạ riêng, ánh màu chủ đạo), trượt bằng spring.
 *  - Nhấn xuống: viên chọn phồng lên (press-to-bulge); đặt tay rồi kéo ngang: viên chạy theo ngón tay,
 *    chỉ khi nhấc tay mới chọn tab và chuyển màn hình.
 * [items] là cặp (biểu tượng, tên). Tap thường và kéo đều do một pointerInput duy nhất xử lý.
 */
@Composable
fun GlassNavBar(
    items: List<Pair<String, String>>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = LocalLiquidBackdrop.current
) {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.surface.luminance() < 0.5f
    val count = items.size.coerceAtLeast(1)

    // Vị trí ngón tay (px, tính trong thanh) khi đang chạm; null khi thả tay.
    var fingerX by remember { mutableStateOf<Float?>(null) }
    var barWidthPx by remember { mutableFloatStateOf(0f) }
    val pressed = fingerX != null

    val hasSelection = selected in 0 until count
    val pillAlpha by animateFloatAsState(
        targetValue = if (hasSelection) 1f else 0f,
        animationSpec = tween(220),
        label = "navPillAlpha"
    )
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentSelected by rememberUpdatedState(selected)

    // Vị trí viên chọn theo đơn vị "ô" (0..count-1): theo ngón tay khi kéo, về ô đã chọn khi thả.
    val cellPx = if (barWidthPx > 0f) barWidthPx / count else 0f
    val target = fingerX?.let { x ->
        if (cellPx > 0f) (x / cellPx - 0.5f).coerceIn(0f, (count - 1).toFloat()) else selected.toFloat()
    } ?: selected.coerceIn(0, count - 1).toFloat()
    val pos by animateFloatAsState(
        targetValue = target,
        animationSpec = if (pressed) {
            spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMedium)
        } else {
            spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessLow)
        },
        label = "navPos"
    )
    // Phồng khi nhấn, bật nảy khi thả.
    val bulge by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
        label = "navBulge"
    )

    val accent = scheme.primary
    // Hiệu ứng nền cần GPU/Android mới: blur từ API 31, khúc xạ (shader) từ API 33.
    // Máy cũ hơn thì dùng nền mờ đục để chữ phía sau không lọt qua thanh.
    val sdk = android.os.Build.VERSION.SDK_INT
    val glassFx = sdk >= 31
    // Viên thuốc hoàn toàn như Apple Music (bo tròn hết cỡ hai đầu).
    val barShape = RoundedCornerShape(percent = 50)
    val glass = LocalNavBarOpacity.current.coerceIn(0.0f, 0.90f)
    val rimHi = Color.White.copy(alpha = if (dark) 0.28f else 0.85f)
    val rimLo = Color.White.copy(alpha = if (dark) 0.06f else 0.25f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = GlassNavBarVerticalPadding),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .height(GlassNavBarHeight)
                .onSizeChanged { barWidthPx = it.width.toFloat() }
                // Một pointerInput lo cả chạm lẫn kéo: nhấn xuống là phồng, kéo thì viên chạy theo ngón tay,
                // và CHỈ khi nhấc tay mới chọn tab (điều hướng sang màn hình tương ứng).
                .pointerInput(count) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val cell = size.width.toFloat() / count
                        fun cellAt(x: Float) = (x / cell).toInt().coerceIn(0, count - 1)

                        var lastX = down.position.x
                        fingerX = lastX
                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            lastX = change.position.x
                            fingerX = lastX
                            if (change.positionChange().x != 0f) change.consume()
                        } while (change.pressed)

                        // Nhấc tay: chốt ô dưới ngón tay rồi mới điều hướng.
                        val target = cellAt(lastX)
                        fingerX = null
                        if (target != currentSelected) currentOnSelect(target)
                    }
                }
        ) {
            // Khung kính viên thuốc ôm sát các tab (cắt đúng hình, không lộ vùng chữ nhật).
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .then(
                        if (backdrop != null && glassFx) {
                            Modifier.drawBackdrop(
                                backdrop = backdrop,
                                shape = { barShape },
                                effects = {
                                    // Kính mờ kiểu Apple Music: làm rực màu phía sau rồi blur mạnh,
                                    // khúc xạ rất nhẹ ở mép để thanh không trông như tấm nhựa phẳng.
                                    vibrancy()
                                    blur(2.dp.toPx())
                                },
                                highlight = { Highlight.Default },
                                onDrawSurface = {
                                    // Lớp phủ trắng/đen mờ: độ đặc theo thanh trượt "Độ mờ thanh điều hướng" trong Cài đặt.
                                    drawRect(scheme.surface.copy(alpha = glass))
                                    drawRect(Color.White.copy(alpha = if (dark) 0.02f else 0.04f))
                                }
                            )
                        } else {
                            Modifier.background(scheme.surface.copy(alpha = (glass + 0.5f).coerceAtMost(0.92f)), barShape)
                        }
                    )
                    .clip(barShape)
                    .border(1.dp, Brush.linearGradient(listOf(rimHi, rimLo)), barShape)
            )

            // Viên kính chọn tab (liquid glass: khúc xạ + blur + viền sáng). Phồng tràn ra ngoài khi nhấn.
            val pillShape = RoundedCornerShape(percent = 50)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = pillAlpha }
                    .layout { measurable, constraints ->
                        val barW = constraints.maxWidth
                        val barH = constraints.maxHeight
                        val cell = barW / count.toFloat()
                        val inset = 6.dp.toPx()
                        val stretch = 0.22f * minOf(1f, kotlin.math.abs(currentSelected.coerceIn(0, count - 1) - pos))
                        val w = ((cell - inset * 2) * (1f + 0.14f * bulge + stretch)).roundToInt()
                        val h = ((barH - inset * 2) * (1f + 0.12f * bulge - 0.35f * stretch)).roundToInt()
                        val placeable = measurable.measure(Constraints.fixed(w, h))
                        layout(barW, barH) {
                            val cx = (pos + 0.5f) * cell
                            placeable.place(
                                (cx - w / 2f).roundToInt(),
                                ((barH - h) / 2f).roundToInt()
                            )
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (backdrop != null && glassFx) {
                                Modifier.drawBackdrop(
                                    backdrop = backdrop,
                                    shape = { pillShape },
                                    effects = {
                                        vibrancy()
                                        blur(8.dp.toPx())
                                        if (sdk >= 33) lens(14.dp.toPx(), 28.dp.toPx())
                                    },
                                    highlight = { Highlight.Default },
                                    onDrawSurface = {
                                        // Ánh màu chủ đạo + một lớp sáng nhẹ, đậm lên khi nhấn.
                                        drawRect(accent.copy(alpha = (if (dark) 0.30f else 0.20f) + 0.08f * bulge))
                                        drawRect(Color.White.copy(alpha = if (dark) 0.06f else 0.20f))
                                    }
                                )
                            } else {
                                Modifier.background(accent.copy(alpha = if (dark) 0.30f else 0.18f), pillShape)
                            }
                        )
                        .clip(pillShape)
                        .drawWithContent {
                            drawContent()
                            if (bulge > 0.01f) {
                                val r = size.maxDimension * 0.75f
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = (if (dark) 0.16f else 0.34f) * bulge),
                                            Color.Transparent
                                        ),
                                        center = center,
                                        radius = r
                                    ),
                                    radius = r,
                                    center = center
                                )
                            }
                        }
                        .border(
                            1.dp,
                            Brush.linearGradient(listOf(Color.White.copy(alpha = if (dark) 0.45f else 0.9f), Color.Transparent)),
                            pillShape
                        )
                )
            }

            // Biểu tượng + chữ, màu chuyển mượt theo khoảng cách tới viên chọn.
            Row(
                modifier = Modifier.matchParentSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, (icon, title) ->
                    val closeness = (1f - kotlin.math.abs(pos - index)).coerceIn(0f, 1f) * pillAlpha
                    val tint = lerp(scheme.onSurface.copy(alpha = 0.88f), accent, closeness)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = icon,
                            fontSize = 24.sp,
                            color = tint,
                            modifier = Modifier.graphicsLayer {
                                val lift = 1f + 0.16f * closeness + 0.06f * bulge * closeness
                                scaleX = lift
                                scaleY = lift
                            }
                        )
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (closeness > 0.5f) FontWeight.Bold else FontWeight.SemiBold,
                            color = tint,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/** Chỗ trống vô hình có đúng chiều cao thanh điều hướng, để Scaffold chừa chỗ cho thanh nổi. */
@Composable
fun GlassNavBarSpacer(modifier: Modifier = Modifier) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(GlassNavBarHeight + GlassNavBarVerticalPadding * 2)
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun ScreenHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyState(
    emoji: String,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 34.sp)
            }
            Spacer(Modifier.height(14.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}