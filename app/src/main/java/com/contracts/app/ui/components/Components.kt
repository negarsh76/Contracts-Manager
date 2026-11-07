package com.contracts.app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.contracts.app.data.model.ColorSlot
import com.contracts.app.data.model.Contract
import com.contracts.app.data.model.ContractStatus
import com.contracts.app.ui.theme.*

@Composable
fun statusColor(s: ContractStatus) = when (s) {
    ContractStatus.ACTIVE   -> StatusActive
    ContractStatus.WARNING  -> StatusWarning
    ContractStatus.CRITICAL -> StatusCritical
    ContractStatus.EXPIRED  -> StatusExpired
    ContractStatus.UNKNOWN  -> appColors().textMuted
}

@Composable
fun statusBg(s: ContractStatus) = when (s) {
    ContractStatus.ACTIVE   -> StatusActiveBg
    ContractStatus.WARNING  -> StatusWarningBg
    ContractStatus.CRITICAL -> StatusCriticalBg
    ContractStatus.EXPIRED  -> StatusExpiredBg
    ContractStatus.UNKNOWN  -> Color(0x11888888)
}

@Composable
fun PulseDot(status: ContractStatus, size: Dp = 9.dp) {
    val color = statusColor(status)
    val shouldPulse = status == ContractStatus.CRITICAL || status == ContractStatus.WARNING
    val anim = rememberInfiniteTransition(label = "pulse")
    val scale by anim.animateFloat(0.85f, 1.2f,
        infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale")
    Box(
        Modifier.size(size)
            .graphicsLayer { if (shouldPulse) { scaleX = scale; scaleY = scale } }
            .clip(CircleShape).background(color)
    )
}

@Composable
fun DonutChart(counts: Map<ContractStatus, Int>, modifier: Modifier = Modifier) {
    val total = counts.values.sum().toFloat().coerceAtLeast(1f)
    val statuses = listOf(ContractStatus.ACTIVE, ContractStatus.WARNING, ContractStatus.CRITICAL, ContractStatus.EXPIRED)
    val colors = statuses.map { s -> when (s) {
        ContractStatus.ACTIVE   -> StatusActive
        ContractStatus.WARNING  -> StatusWarning
        ContractStatus.CRITICAL -> StatusCritical
        else                    -> StatusExpired
    }}
    val anim = remember { Animatable(0f) }
    LaunchedEffect(counts) { anim.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }

    Canvas(modifier.size(120.dp)) {
        var start = -90f
        val stroke = size.minDimension * 0.18f
        val inset = stroke / 2f
        val rect = androidx.compose.ui.geometry.Rect(inset, inset, size.width - inset, size.height - inset)
        statuses.forEachIndexed { i, s ->
            val sweep = (counts[s] ?: 0) / total * 360f * anim.value
            if (sweep > 0f) {
                drawArc(colors[i], start, sweep - 2f, false,
                    rect.topLeft, rect.size, style = Stroke(stroke, cap = StrokeCap.Round))
                start += sweep
            }
        }
    }
}

@Composable
fun StatusChip(status: ContractStatus, count: Int, selected: Boolean, onClick: () -> Unit) {
    val c = appColors()
    val color = statusColor(status)
    val bgAnim by animateColorAsState(if (selected) color.copy(0.18f) else Color.Transparent, tween(180), label = "bg")
    val borderAnim by animateColorAsState(if (selected) color else color.copy(0.3f), tween(180), label = "border")
    Row(
        Modifier.clip(RoundedCornerShape(20.dp)).background(bgAnim)
            .border(1.dp, borderAnim, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PulseDot(status, 7.dp)
        Text(status.label, color = if (selected) color else c.textSecondary, fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        if (count > 0) Box(Modifier.clip(CircleShape).background(color.copy(0.2f)).padding(horizontal = 6.dp, vertical = 1.dp)) {
            Text(count.toString(), color = color, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun ContractCard(contract: Contract, onClick: () -> Unit, onStar: () -> Unit) {
    val c = appColors()
    val color = statusColor(contract.status)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors   = CardDefaults.cardColors(containerColor = c.surface),
        shape    = RoundedCornerShape(18.dp),
        border   = BorderStroke(1.dp, color.copy(0.22f))
    ) {
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.width(4.dp).fillMaxHeight()
                .background(Brush.verticalGradient(listOf(color, color.copy(0.3f)))))
            Column(Modifier.weight(1f).padding(14.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(contract.title, color = c.textPrimary, fontSize = 14.sp,
                            fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(3.dp))
                        Text(contract.company, color = c.textSecondary, fontSize = 11.sp,
                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.clip(RoundedCornerShape(10.dp))
                            .background(statusBg(contract.status))
                            .padding(horizontal = 10.dp, vertical = 4.dp)) {
                            Text(
                                if (contract.daysRemaining < 0) "${-contract.daysRemaining}روز گذشت"
                                else "${contract.daysRemaining} روز",
                                color = color, fontSize = 10.sp, fontWeight = FontWeight.Black
                            )
                        }
                        IconButton(onClick = onStar, modifier = Modifier.size(28.dp)) {
                            Icon(if (contract.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                                null, tint = if (contract.isStarred) Color(0xFFFFD700) else c.textMuted,
                                modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    InfoPill("شروع", contract.startDateJalali)
                    InfoPill("پایان", contract.endDateJalali)
                    if (contract.durationMonths > 0) InfoPill("مدت", "${contract.durationMonths}م")
                }
                if (contract.durationMonths > 0 && contract.daysRemaining >= 0) {
                    Spacer(Modifier.height(8.dp))
                    val prog = 1f - (contract.daysRemaining / (contract.durationMonths * 30f)).coerceIn(0f, 1f)
                    val progAnim by animateFloatAsState(prog, tween(600), label = "prog")
                    Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(color.copy(0.15f))) {
                        Box(Modifier.fillMaxWidth(progAnim).fillMaxHeight().clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(color.copy(0.6f), color))))
                    }
                }
            }
        }
    }
}

@Composable
fun InfoPill(label: String, value: String) {
    val c = appColors()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = c.textMuted, fontSize = 9.sp)
        Text(value.ifEmpty { "—" }, color = c.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

private val ColorPresets = listOf(
    Color(0xFFFFFFFF), Color(0xFFF5F6F8), Color(0xFF1A1A1A), Color(0xFF000000),
    Color(0xFF2962FF), Color(0xFF00BCD4), Color(0xFF4CAF50), Color(0xFFFFC107),
    Color(0xFFFF6B35), Color(0xFFFF6666), Color(0xFF8B5CF6), Color(0xFFA9A9A9),
)

/** دیالوگ انتخاب رنگ برای یک اسلات مشخص از تم سفارشی — با کد Hex و چند رنگ آماده. */
@Composable
fun ColorPickerDialog(slot: ColorSlot, initial: Color, onDismiss: () -> Unit, onConfirm: (Color) -> Unit) {
    val c = appColors()
    var hex by remember(slot) {
        mutableStateOf("#" + Integer.toHexString(initial.toArgb()).takeLast(6).uppercase())
    }
    var preview by remember(slot) { mutableStateOf(initial) }
    var isValid by remember { mutableStateOf(true) }

    fun applyHex(v: String) {
        hex = v
        runCatching {
            val clean = if (v.startsWith("#")) v else "#$v"
            preview = Color(android.graphics.Color.parseColor(clean))
            isValid = true
        }.onFailure { isValid = false }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surfaceElevated,
        title = {
            Column {
                Text(slot.label, color = c.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(slot.hint, color = c.textMuted, fontSize = 11.sp)
            }
        },
        text = {
            Column {
                Box(
                    Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(12.dp))
                        .background(preview).border(1.dp, c.outline, RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = hex, onValueChange = ::applyHex,
                    label = { Text("کد رنگ (Hex)", color = c.textMuted) },
                    isError = !isValid, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = c.primary, unfocusedBorderColor = c.outline,
                        focusedTextColor = c.textPrimary, unfocusedTextColor = c.textPrimary,
                        cursorColor = c.primary)
                )
                Spacer(Modifier.height(14.dp))
                Text("رنگ‌های آماده", color = c.textMuted, fontSize = 11.sp)
                Spacer(Modifier.height(8.dp))
                ColorPresets.chunked(6).forEach { row ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { preset ->
                            Box(
                                Modifier.size(30.dp).clip(CircleShape).background(preset)
                                    .border(if (preview == preset) 2.dp else 1.dp,
                                        if (preview == preset) c.primary else c.outline, CircleShape)
                                    .clickable { applyHex("#" + Integer.toHexString(preset.toArgb()).takeLast(6)) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (isValid) onConfirm(preview) }) { Text("ذخیره", color = c.primary) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف", color = c.textMuted) } }
    )
}

/** نمایش لوگوی سفارشی اپ از روی مسیر فایل ذخیره‌شده روی دیسک؛ اگه لوگو نبود چیزی نشون نمی‌ده. */
@Composable
fun LogoImage(path: String?, modifier: Modifier = Modifier) {
    if (path.isNullOrBlank()) return
    val bitmap = remember(path) { runCatching { BitmapFactory.decodeFile(path)?.asImageBitmap() }.getOrNull() }
    bitmap?.let {
        Image(it, contentDescription = "لوگو", modifier = modifier.clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop)
    }
}
