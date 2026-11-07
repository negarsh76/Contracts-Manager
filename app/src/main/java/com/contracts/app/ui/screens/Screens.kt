package com.contracts.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import com.contracts.app.data.model.*
import com.contracts.app.domain.ContractViewModel
import com.contracts.app.ui.components.*
import com.contracts.app.ui.theme.*
import com.contracts.app.utils.JalaliConverter
import java.io.File

// ─────────────────────────────────────────────────────────────────────────────
// DASHBOARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun DashboardScreen(vm: ContractViewModel, onGoToList: () -> Unit, onContractClick: (Int) -> Unit) {
    val state   by vm.state.collectAsState()
    val summary by vm.summary.collectAsState()
    val c = appColors()

    val urgent = state.contracts
        .filter { it.status == ContractStatus.CRITICAL || it.status == ContractStatus.WARNING }
        .sortedBy { it.daysRemaining }.take(5)

    val totalAmount = remember(state.contracts) {
        state.contracts.sumOf { it.amount.replace(",", "").replace("،", "").trim().toDoubleOrNull() ?: 0.0 }
    }
    val expiringThisMonth = remember(state.contracts) {
        state.contracts.count { it.daysRemaining in 0..30 }
    }
    val avgDuration = remember(state.contracts) {
        val withDuration = state.contracts.filter { it.durationMonths > 0 }
        if (withDuration.isEmpty()) 0 else withDuration.sumOf { it.durationMonths } / withDuration.size
    }
    val topCompany = remember(state.contracts) {
        state.contracts.groupingBy { it.company }.eachCount().filterKeys { it.isNotBlank() }
            .maxByOrNull { it.value }?.key
    }

    LazyColumn(
        Modifier.fillMaxSize().background(c.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Text("داشبورد مدیریت قراردادها", color = c.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("${state.contracts.size} قرارداد", color = c.primary, fontSize = 13.sp)
                }
                if (state.logoPath != null) LogoImage(state.logoPath, Modifier.size(46.dp))
            }
        }

        // Quick stats grid
        if (state.contracts.isNotEmpty()) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickStatCard("در حال انقضا (۳۰ روز)", expiringThisMonth.toString(), Icons.Default.Alarm, StatusWarning, Modifier.weight(1f))
                    QuickStatCard("میانگین مدت", if (avgDuration > 0) "$avgDuration ماه" else "—", Icons.Default.Timer, c.primary, Modifier.weight(1f))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickStatCard("جمع مبالغ", if (totalAmount > 0) formatAmount(totalAmount) else "—", Icons.Default.Payments, StatusActive, Modifier.weight(1f))
                    QuickStatCard("پرتکرارترین شرکت", topCompany?.take(14) ?: "—", Icons.Default.Business, StatusExpired, Modifier.weight(1f))
                }
            }
            val finishedCount = state.contracts.count { it.status == ContractStatus.EXPIRED }
            if (finishedCount > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { vm.setFilter(ContractStatus.EXPIRED); onGoToList() },
                        colors = CardDefaults.cardColors(containerColor = c.surface), shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, StatusExpired.copy(0.4f))
                    ) {
                        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(40.dp).clip(CircleShape).background(StatusExpired.copy(0.15f)),
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.TaskAlt, null, tint = StatusExpired, modifier = Modifier.size(20.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text("🏁 قراردادهای اتمام‌یافته", color = c.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("$finishedCount قرارداد به پایان رسیده — برای دیدن کامل بزن", color = c.textMuted, fontSize = 11.sp)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = c.textMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Donut + breakdown
        item {
            Card(colors = CardDefaults.cardColors(containerColor = c.surface), shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.padding(20.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Box(contentAlignment = Alignment.Center) {
                        DonutChart(summary)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.contracts.size.toString(), color = c.textPrimary,
                                fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Text("کل", color = c.textMuted, fontSize = 9.sp)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(ContractStatus.ACTIVE, ContractStatus.WARNING,
                            ContractStatus.CRITICAL, ContractStatus.EXPIRED).forEach { s ->
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = (summary[s] ?: 0) > 0) {
                                        vm.setFilter(s); onGoToList()
                                    }.padding(vertical = 3.dp, horizontal = 4.dp)) {
                                PulseDot(s, 8.dp)
                                Text(s.label, color = c.textSecondary, fontSize = 12.sp,
                                    modifier = Modifier.width(52.dp))
                                Text((summary[s] ?: 0).toString(),
                                    color = statusColor(s), fontSize = 16.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        // Timeline bar
        if (state.contracts.isNotEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = c.surface), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("توزیع وضعیت", color = c.textMuted, fontSize = 11.sp)
                        Spacer(Modifier.height(8.dp))
                        val total = state.contracts.size.toFloat().coerceAtLeast(1f)
                        Row(Modifier.fillMaxWidth().height(12.dp).clip(CircleShape)) {
                            listOf(ContractStatus.ACTIVE to StatusActive, ContractStatus.WARNING to StatusWarning,
                                ContractStatus.CRITICAL to StatusCritical, ContractStatus.EXPIRED to StatusExpired
                            ).forEach { (s, color) ->
                                val w = (summary[s] ?: 0) / total
                                if (w > 0f) Box(Modifier.weight(w).fillMaxHeight().background(color))
                            }
                        }
                    }
                }
            }
        }

        // Urgent
        if (urgent.isNotEmpty()) {
            item { Text("🚨 نیاز به توجه فوری", color = StatusCritical, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            items(urgent, key = { it.id }) { contract ->
                ContractCard(contract, { onContractClick(contract.id) }, { vm.toggleStar(contract.id) })
            }
            item {
                TextButton(onClick = onGoToList, Modifier.fillMaxWidth()) {
                    Text("مشاهده همه →", color = c.primary)
                }
            }
        }

        if (state.contracts.isEmpty() && !state.isLoading) {
            item {
                Column(Modifier.fillMaxWidth().padding(top = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("📊", fontSize = 64.sp)
                    Text("داشبورد خالیه", color = c.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("از تب قراردادها یه فایل اکسل بارگذاری کن",
                        color = c.textSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun QuickStatCard(label: String, value: String, icon: ImageVector, tint: Color, modifier: Modifier = Modifier) {
    val c = appColors()
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = c.surface), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
            Text(value, color = c.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Black,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, color = c.textMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun formatAmount(amount: Double): String {
    val rounded = amount.toLong()
    val formatted = "%,d".format(rounded)
    return "$formatted تومان"
}

// ─────────────────────────────────────────────────────────────────────────────
// CONTRACT LIST
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractListScreen(vm: ContractViewModel, onContractClick: (Int) -> Unit) {
    val state     by vm.state.collectAsState()
    val contracts by vm.filtered.collectAsState()
    val summary   by vm.summary.collectAsState()
    val c = appColors()
    val context = LocalContext.current
    var showSort by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { vm.loadFile(it) }
    }

    fun share(contract: Contract) {
        val text = "📋 ${contract.title}\n🏢 ${contract.company}\n📅 پایان: ${contract.endDateJalali}\n⏳ ${contract.status.label} – ${
            if (contract.daysRemaining < 0) "${-contract.daysRemaining} روز گذشته" else "${contract.daysRemaining} روز باقیمانده"
        }\nارسال از اپ مدیریت قراردادها"
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text)
        }, "اشتراک‌گذاری"))
    }

    Scaffold(
        containerColor = c.background,
        topBar = {
            Column(Modifier.background(c.surfaceElevated).statusBarsPadding().padding(16.dp, 10.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        Text("قراردادها", color = c.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Text("${contracts.size} نتیجه", color = c.primary, fontSize = 12.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { vm.setShowStarred(!state.showStarred) }) {
                            Icon(if (state.showStarred) Icons.Default.Star else Icons.Default.StarBorder,
                                null, tint = if (state.showStarred) Color(0xFFFFD700) else c.textSecondary)
                        }
                        IconButton(onClick = { showSort = true }) {
                            Icon(Icons.Default.Sort, null, tint = c.textSecondary)
                        }
                        IconButton(onClick = { picker.launch(arrayOf("*/*")) }) {
                            Icon(Icons.Default.FileUpload, null, tint = c.primary)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                TextField(
                    value = state.search, onValueChange = vm::setSearch,
                    placeholder = { Text("جستجوی هوشمند...", color = c.textMuted, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = c.textMuted) },
                    trailingIcon = { if (state.search.isNotEmpty()) IconButton(onClick = { vm.setSearch("") }) {
                        Icon(Icons.Default.Close, null, tint = c.textMuted) } },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = c.surface, unfocusedContainerColor = c.surface,
                        focusedTextColor = c.textPrimary, unfocusedTextColor = c.textPrimary,
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = c.primary),
                    shape = RoundedCornerShape(14.dp), singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(selected = state.statusFilter == null, onClick = { vm.setFilter(null) },
                            label = { Text("همه · ${state.contracts.size}", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = c.primary.copy(0.2f), selectedLabelColor = c.primary,
                                containerColor = c.surface, labelColor = c.textSecondary))
                    }
                    items(ContractStatus.entries.filter { it != ContractStatus.UNKNOWN }) { s ->
                        StatusChip(s, summary[s] ?: 0, state.statusFilter == s) {
                            vm.setFilter(if (state.statusFilter == s) null else s)
                        }
                    }
                }
            }
        }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when {
                state.isLoading -> Column(Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircularProgressIndicator(color = c.primary)
                    Text("در حال خواندن فایل...", color = c.textSecondary)
                }
                state.error != null -> Column(Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.ErrorOutline, null, tint = StatusCritical, modifier = Modifier.size(52.dp))
                    Text(state.error!!, color = StatusCritical, textAlign = TextAlign.Center)
                    Button(onClick = vm::clearError) { Text("بستن") }
                }
                !state.hasFile -> Column(Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("📄", fontSize = 72.sp)
                    Text("فایل اکسل ندارم!", color = c.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("فایل Contracts_Smart.xlsm رو انتخاب کن",
                        color = c.textSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
                    Button(onClick = { picker.launch(arrayOf("*/*")) },
                        colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
                        shape = RoundedCornerShape(14.dp), modifier = Modifier.height(50.dp)) {
                        Icon(Icons.Default.FileUpload, null); Spacer(Modifier.width(8.dp))
                        Text("انتخاب فایل اکسل", fontWeight = FontWeight.Bold)
                    }
                }
                contracts.isEmpty() -> Text("نتیجه‌ای پیدا نشد",
                    modifier = Modifier.align(Alignment.Center), color = c.textSecondary)
                else -> LazyColumn(contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(contracts, key = { it.id }) { contract ->
                        AnimatedVisibility(true, enter = fadeIn() + slideInVertically { it / 3 }) {
                            ContractCard(contract, { onContractClick(contract.id) }, { vm.toggleStar(contract.id) })
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showSort) ModalBottomSheet(onDismissRequest = { showSort = false }, containerColor = c.surfaceElevated) {
        Column(Modifier.padding(24.dp).navigationBarsPadding()) {
            Text("مرتب‌سازی", color = c.textPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Spacer(Modifier.height(16.dp))
            SortField.entries.forEach { f ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .clickable { vm.setSort(f); showSort = false }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(state.sortField == f, { vm.setSort(f); showSort = false },
                        colors = RadioButtonDefaults.colors(selectedColor = c.primary))
                    Text(f.label, color = c.textPrimary, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DETAIL
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractDetailScreen(id: Int, vm: ContractViewModel, onBack: () -> Unit) {
    val state    by vm.state.collectAsState()
    val contract = state.contracts.find { it.id == id }
    val c = appColors()
    val context  = LocalContext.current

    if (contract == null) {
        Box(Modifier.fillMaxSize().background(c.background)) {
            Text("پیدا نشد", Modifier.align(Alignment.Center), color = c.textSecondary)
        }
        return
    }

    val color = statusColor(contract.status)
    var noteText   by remember { mutableStateOf(contract.personalNote) }
    var editNote   by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = c.background,
        topBar = {
            TopAppBar(
                title = { Text("جزئیات", color = c.textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = c.textSecondary) } },
                actions = {
                    IconButton(onClick = { vm.toggleStar(id) }) {
                        Icon(if (contract.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                            null, tint = if (contract.isStarred) Color(0xFFFFD700) else c.textSecondary)
                    }
                    IconButton(onClick = {
                        val text = "📋 ${contract.title}\n🏢 ${contract.company}\n📅 ${contract.endDateJalali}\n⏳ ${contract.status.label}"
                        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, "اشتراک‌گذاری"))
                    }) { Icon(Icons.Default.Share, null, tint = c.primary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.surfaceElevated)
            )
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // Hero card
            Card(colors = CardDefaults.cardColors(containerColor = statusBg(contract.status)),
                shape = RoundedCornerShape(22.dp), border = BorderStroke(1.dp, color.copy(0.45f))) {
                Column(Modifier.padding(22.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(contract.title, color = c.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Text(contract.company, color = c.textSecondary, fontSize = 13.sp)
                            if (contract.contractNumber.isNotBlank())
                                Text("شماره: ${contract.contractNumber}", color = c.textMuted, fontSize = 11.sp)
                        }
                        Box(Modifier.clip(RoundedCornerShape(14.dp)).background(color.copy(0.2f))
                            .padding(horizontal = 14.dp, vertical = 8.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                PulseDot(contract.status, 10.dp); Spacer(Modifier.height(4.dp))
                                Text(contract.status.label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider(color = color.copy(0.2f))
                    Spacer(Modifier.height(20.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.Bottom) {
                        Text(if (contract.daysRemaining < 0) "${-contract.daysRemaining}" else "${contract.daysRemaining}",
                            color = color, fontSize = 56.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("روز", color = c.textSecondary, fontSize = 16.sp)
                            Text(if (contract.daysRemaining < 0) "گذشته" else "باقیمانده", color = c.textMuted, fontSize = 12.sp)
                        }
                    }
                    if (contract.durationMonths > 0 && contract.daysRemaining >= 0) {
                        Spacer(Modifier.height(16.dp))
                        val prog = 1f - (contract.daysRemaining / (contract.durationMonths * 30f)).coerceIn(0f, 1f)
                        Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(color.copy(0.15f))) {
                            Box(Modifier.fillMaxWidth(prog).fillMaxHeight().clip(CircleShape)
                                .background(Brush.horizontalGradient(listOf(color.copy(0.6f), color))))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("${(prog*100).toInt()}% از مدت گذشته", color = c.textMuted, fontSize = 10.sp,
                            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    }
                }
            }

            // Quick actions
            SectionCard("⚡ اقدامات سریع") {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = contract.managerEmail.isNotBlank()) {
                        val subject = "قرارداد: ${contract.title}"
                        val body = "شرکت: ${contract.company}\nشماره قرارداد: ${contract.contractNumber}\n" +
                            "تاریخ پایان: ${contract.endDateJalali}\nوضعیت: ${contract.status.label}"
                        val recipient = contract.managerEmail
                        val gmailIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "message/rfc822"
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
                            putExtra(Intent.EXTRA_SUBJECT, subject)
                            putExtra(Intent.EXTRA_TEXT, body)
                            setPackage("com.google.android.gm")
                        }
                        runCatching { context.startActivity(gmailIntent) }.onFailure {
                            val fallback = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
                                putExtra(Intent.EXTRA_SUBJECT, subject)
                                putExtra(Intent.EXTRA_TEXT, body)
                            }
                            context.startActivity(Intent.createChooser(fallback, "ارسال ایمیل"))
                        }
                    }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Email, null, tint = c.primary, modifier = Modifier.size(20.dp))
                    Column(Modifier.weight(1f)) {
                        Text("ایمیل به مدیر", color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(
                            if (contract.managerEmail.isBlank()) "برای این قرارداد ایمیل مدیر تو اکسل ثبت نشده"
                            else contract.managerEmail,
                            color = c.textMuted, fontSize = 10.sp)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = c.textMuted, modifier = Modifier.size(16.dp))
                }
                HorizontalDivider(color = c.outline.copy(0.4f))
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .clickable {
                        val g = JalaliConverter.toGregorian(contract.endDateJalali)
                        val calIntent = Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            putExtra(CalendarContract.Events.TITLE, "پایان قرارداد: ${contract.title}")
                            putExtra(CalendarContract.Events.DESCRIPTION, "شرکت: ${contract.company}")
                            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                            if (g != null) {
                                val cal = java.util.Calendar.getInstance()
                                cal.set(g.first, g.second - 1, g.third, 0, 0, 0)
                                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.timeInMillis)
                                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.timeInMillis)
                            }
                            if (state.myEmail.isNotBlank()) {
                                putExtra(Intent.EXTRA_EMAIL, state.myEmail)
                            }
                        }
                        runCatching { context.startActivity(calIntent) }
                    }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.EventAvailable, null, tint = c.primary, modifier = Modifier.size(20.dp))
                    Column(Modifier.weight(1f)) {
                        Text("افزودن سررسید به تقویم گوشی", color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("یادآوری تاریخ پایان در تقویم اندروید", color = c.textMuted, fontSize = 10.sp)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = c.textMuted, modifier = Modifier.size(16.dp))
                }
            }

            // Contact point
            if (contract.contactName.isNotBlank() || contract.contactRole.isNotBlank() ||
                contract.contactPhone.isNotBlank() || contract.managerEmail.isNotBlank()) {
                SectionCard("👤 نقطه تماس") {
                    if (contract.contactName.isNotBlank()) DRow(Icons.Default.Person, "نام", contract.contactName)
                    if (contract.contactRole.isNotBlank()) DRow(Icons.Default.Badge, "سمت", contract.contactRole)
                    if (contract.contactPhone.isNotBlank()) {
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contract.contactPhone}"))
                                runCatching { context.startActivity(dial) }
                            }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Call, null, tint = c.primary, modifier = Modifier.size(18.dp))
                            Column(Modifier.weight(1f)) {
                                Text("تلفن", color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(contract.contactPhone, color = c.textMuted, fontSize = 11.sp)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = c.textMuted, modifier = Modifier.size(14.dp))
                        }
                    }
                    if (contract.managerEmail.isNotBlank()) DRow(Icons.Default.Email, "ایمیل", contract.managerEmail)
                }
            }

            // Dates
            SectionCard("📅 تاریخ‌ها") {
                DRow(Icons.Default.CalendarToday,  "شروع شمسی",   contract.startDateJalali)
                DRow(Icons.Default.Event,          "پایان شمسی",  contract.endDateJalali)
                DRow(Icons.Default.DateRange,      "شروع میلادی", contract.startDateGreg)
                DRow(Icons.Default.DateRange,      "پایان میلادی",contract.endDateGreg)
                if (contract.durationMonths > 0) DRow(Icons.Default.Timer, "مدت", "${contract.durationMonths} ماه")
            }

            if (contract.amount.isNotBlank() || contract.paymentMethod.isNotBlank()) {
                SectionCard("💰 مالی") {
                    DRow(Icons.Default.Payments, "مبلغ", contract.amount + if (contract.amount.isNotBlank()) " تومان" else "")
                    DRow(Icons.Default.Payment, "روش پرداخت", contract.paymentMethod)
                }
            }

            SectionCard("📋 اطلاعات") {
                DRow(Icons.Default.Category, "نوع", contract.contractType)
                DRow(Icons.Default.Link, "لینک فایل", contract.fileLink)
            }

            if (contract.notes.isNotBlank()) {
                SectionCard("📝 توضیحات") {
                    Text(contract.notes, color = c.textSecondary, fontSize = 13.sp, lineHeight = 21.sp)
                }
            }

            // Personal note
            Card(colors = CardDefaults.cardColors(containerColor = c.surface), shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, c.primary.copy(0.3f))) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("✏️ یادداشت من", color = c.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = {
                            if (editNote) { vm.saveNote(id, noteText); editNote = false } else editNote = true
                        }) { Text(if (editNote) "ذخیره" else "ویرایش", color = c.primary) }
                    }
                    if (editNote) {
                        OutlinedTextField(noteText, { noteText = it }, Modifier.fillMaxWidth(),
                            placeholder = { Text("یادداشت شخصی...", color = c.textMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = c.primary, unfocusedBorderColor = c.outline,
                                focusedTextColor = c.textPrimary, unfocusedTextColor = c.textPrimary,
                                cursorColor = c.primary), minLines = 3)
                    } else {
                        Text(noteText.ifBlank { "یادداشتی ندارم — بزن «ویرایش»" },
                            color = if (noteText.isBlank()) c.textMuted else c.textSecondary,
                            fontSize = 13.sp, lineHeight = 21.sp)
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    val c = appColors()
    Card(colors = CardDefaults.cardColors(containerColor = c.surface), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = c.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp)); content()
        }
    }
}

@Composable
fun DRow(icon: ImageVector, label: String, value: String) {
    if (value.isBlank() || value == "—") return
    val c = appColors()
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            Icon(icon, null, tint = c.textMuted, modifier = Modifier.size(15.dp))
            Text(label, color = c.textMuted, fontSize = 12.sp)
        }
        Text(value, color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CALENDAR
// ─────────────────────────────────────────────────────────────────────────────
private val MONTHS = listOf("فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور",
    "مهر","آبان","آذر","دی","بهمن","اسفند")
private val DAYS_SHORT = listOf("ش","ی","د","س","چ","پ","ج")

@Composable
fun CalendarScreen(vm: ContractViewModel, onContractClick: (Int) -> Unit) {
    val state by vm.state.collectAsState()
    val c = appColors()
    var year  by remember { mutableIntStateOf(1404) }
    var month by remember { mutableIntStateOf(3) }
    var highlightDay by remember { mutableStateOf<Int?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val monthContracts = remember(state.contracts, year, month) {
        state.contracts.filter { ct ->
            val p = ct.endDateJalali.trim().split("/")
            p.size == 3 && p[0].toIntOrNull() == year && p[1].toIntOrNull() == month
        }
    }
    val dayContracts = remember(monthContracts) {
        monthContracts.groupBy { ct -> ct.endDateJalali.trim().split("/").getOrNull(2)?.toIntOrNull() ?: 0 }
    }
    val daysInMonth = if (month <= 6) 31 else if (month <= 11) 30 else 29
    val firstOffset = remember(year, month) { calcFirstOffset(year, month) }

    Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(16.dp, 14.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            IconButton(onClick = { if (month == 1) { month = 12; year-- } else month-- }) {
                Icon(Icons.Default.ChevronRight, null, tint = c.primary) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(MONTHS[month - 1], color = c.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(year.toString(), color = c.primary, fontSize = 13.sp)
            }
            IconButton(onClick = { if (month == 12) { month = 1; year++ } else month++ }) {
                Icon(Icons.Default.ChevronLeft, null, tint = c.primary) }
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                colors = CardDefaults.cardColors(containerColor = c.surface), shape = RoundedCornerShape(14.dp)
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Search, null, tint = c.textMuted, modifier = Modifier.size(18.dp))
                    Text(
                        highlightDay?.let { "$year/$month/$it" } ?: "جستجوی یه تاریخ خاص در تقویم",
                        color = if (highlightDay != null) c.textPrimary else c.textMuted, fontSize = 13.sp,
                        modifier = Modifier.weight(1f))
                    if (highlightDay != null) {
                        IconButton(onClick = { highlightDay = null }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, null, tint = c.textMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        if (showDatePicker) {
            JalaliDatePickerDialog(
                initialYear = year, initialMonth = month, initialDay = highlightDay ?: 1,
                onDismiss = { showDatePicker = false },
                onConfirm = { y, m, d -> year = y; month = m; highlightDay = d; showDatePicker = false }
            )
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            DAYS_SHORT.forEach { d ->
                Text(d, color = c.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(6.dp))

        val rows = (firstOffset + daysInMonth + 6) / 7
        Column(Modifier.padding(horizontal = 8.dp)) {
            repeat(rows) { row ->
                Row(Modifier.fillMaxWidth()) {
                    repeat(7) { col ->
                        val cell = row * 7 + col
                        val day  = cell - firstOffset + 1
                        val cts  = if (day in 1..daysInMonth) dayContracts[day] ?: emptyList() else emptyList()
                        Box(Modifier.weight(1f).aspectRatio(1f).padding(2.dp)) {
                            if (day in 1..daysInMonth) {
                                val isHighlighted = day == highlightDay
                                Box(Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                                    .background(if (cts.isNotEmpty()) statusBg(cts.minByOrNull { it.status.ordinal }!!.status) else Color.Transparent)
                                    .then(if (isHighlighted) Modifier.border(2.dp, c.primary, RoundedCornerShape(8.dp)) else Modifier)
                                    .then(if (cts.isNotEmpty()) Modifier.clickable { if (cts.size == 1) onContractClick(cts[0].id) } else Modifier),
                                    contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(day.toString(),
                                            color = if (cts.isNotEmpty()) c.textPrimary else c.textMuted,
                                            fontSize = 12.sp, fontWeight = if (cts.isNotEmpty()) FontWeight.Bold else FontWeight.Normal)
                                        if (cts.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            cts.take(3).forEach { ct ->
                                                Box(Modifier.size(4.dp).clip(CircleShape).background(statusColor(ct.status)))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = c.outline, modifier = Modifier.padding(top = 12.dp))

        LazyColumn(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (monthContracts.isEmpty()) {
                item { Text("هیچ قراردادی در این ماه پایان نمی‌یابد",
                    color = c.textMuted, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
            } else {
                item { Text("${monthContracts.size} قرارداد در ${MONTHS[month-1]}",
                    color = c.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                items(monthContracts.sortedBy { it.daysRemaining }) { ct ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(c.surface).clickable { onContractClick(ct.id) }.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.width(3.dp).height(28.dp).clip(CircleShape).background(statusColor(ct.status)))
                            Column {
                                Text(ct.title, color = c.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(ct.company, color = c.textSecondary, fontSize = 10.sp)
                            }
                        }
                        Text(ct.endDateJalali.split("/").lastOrNull() ?: "",
                            color = statusColor(ct.status), fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun JalaliDatePickerDialog(
    initialYear: Int, initialMonth: Int, initialDay: Int,
    onDismiss: () -> Unit, onConfirm: (Int, Int, Int) -> Unit
) {
    val c = appColors()
    var year by remember { mutableIntStateOf(initialYear) }
    var month by remember { mutableIntStateOf(initialMonth) }
    var day by remember { mutableIntStateOf(initialDay) }
    val daysInMonth = if (month <= 6) 31 else if (month <= 11) 30 else 29
    LaunchedEffect(daysInMonth) { if (day > daysInMonth) day = daysInMonth }

    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = c.surfaceElevated), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("انتخاب تاریخ", color = c.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WheelPicker("روز", (1..daysInMonth).map { it.toString() }, day - 1,
                        { day = it + 1 }, Modifier.weight(1f))
                    WheelPicker("ماه", MONTHS, month - 1, { month = it + 1 }, Modifier.weight(1.4f))
                    WheelPicker("سال", (1395..1410).map { it.toString() }, year - 1395,
                        { year = 1395 + it }, Modifier.weight(1f))
                }
                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("انصراف", color = c.textMuted)
                    }
                    Button(onClick = { onConfirm(year, month, day) }, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
                        shape = RoundedCornerShape(12.dp)) {
                        Text("برو به این تاریخ")
                    }
                }
            }
        }
    }
}

/** یه انتخابگر ساده و لمسی (بدون تایپ) — لیست عمودی قابل اسکرول با آیتم انتخاب‌شده در وسط. */
@Composable
private fun WheelPicker(label: String, items: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val c = appColors()
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = c.textMuted, fontSize = 10.sp)
        Spacer(Modifier.height(4.dp))
        Box(Modifier.height(160.dp).fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(c.surface)) {
            val listState = rememberLazyListState()
            LaunchedEffect(selectedIndex) { listState.scrollToItem(selectedIndex) }
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(items.size) { idx ->
                    val selected = idx == selectedIndex
                    Box(Modifier.fillMaxWidth().height(40.dp).clickable { onSelect(idx) },
                        contentAlignment = Alignment.Center) {
                        Text(items[idx], color = if (selected) c.primary else c.textSecondary,
                            fontSize = if (selected) 15.sp else 13.sp,
                            fontWeight = if (selected) FontWeight.Black else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

private fun calcFirstOffset(jYear: Int, jMonth: Int): Int {
    val jy = jYear - 979
    var jDN = 365L * jy + (jy / 33) * 8 + ((jy % 33 + 3) / 4)
    for (i in 0 until jMonth - 1) jDN += if (i < 6) 31L else 30L
    jDN += 1
    val gDN = jDN + 79
    var gY = 1600L + 400L * (gDN / 146097); var rem = gDN % 146097; var leap = 1L
    if (rem >= 36525) { rem -= 1; gY += 100L * (rem / 36524); rem %= 36524; if (rem >= 365) rem += 1 else leap = 0 }
    gY += 4L * (rem / 1461); rem %= 1461
    if (rem >= 366) { leap = 0; rem -= 1; gY += rem / 365; rem %= 365 }
    val gMD = longArrayOf(31, 28+leap, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    var mi = 0; var r2 = rem
    while (mi < 11 && r2 >= gMD[mi]) { r2 -= gMD[mi]; mi++ }
    val gD = (r2 + 1).toInt(); val gM = mi + 1; val gYi = gY.toInt()
    val a = (14 - gM) / 12; val yy = gYi - a; val mm = gM + 12 * a - 2
    val dow = (gD + yy + yy/4 - yy/100 + yy/400 + (31 * mm) / 12) % 7
    return (dow + 1) % 7
}

// ─────────────────────────────────────────────────────────────────────────────
// SETTINGS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SettingsScreen(vm: ContractViewModel) {
    val state by vm.state.collectAsState()
    val c = appColors()
    val context = LocalContext.current
    var editingSlot by remember { mutableStateOf<ColorSlot?>(null) }
    var activeDialog by remember { mutableStateOf<SettingsDialog?>(null) }
    var myEmailText by remember(state.myEmail) { mutableStateOf(state.myEmail) }

    val logoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val result = runCatching {
                val dest = File(context.filesDir, "app_logo_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    dest.outputStream().use { output -> input.copyTo(output) }
                } ?: error("فایل عکس باز نشد")
                // پاک کردن لوگوی قبلی برای جلوگیری از تجمع فایل
                state.logoPath?.let { old -> runCatching { File(old).delete() } }
                vm.setLogoPath(dest.absolutePath)
            }
            result.onFailure {
                android.widget.Toast.makeText(context, "خطا در ذخیره‌ی لوگو: ${it.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(Modifier.fillMaxSize().background(c.background).verticalScroll(rememberScrollState())
        .statusBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Text("تنظیمات", color = c.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)

        SettingsRow(Icons.Default.Image, "لوگوی اپلیکیشن",
            if (state.logoPath != null) "لوگو تنظیم شده" else "لوگو تنظیم نشده",
            trailing = { Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(c.surfaceElevated),
                contentAlignment = Alignment.Center) {
                if (state.logoPath != null) LogoImage(state.logoPath, Modifier.fillMaxSize())
                else Icon(Icons.Default.Image, null, tint = c.textMuted, modifier = Modifier.size(18.dp))
            } }
        ) { activeDialog = SettingsDialog.LOGO }

        SettingsRow(Icons.Default.Palette, "تم و رنگ‌ها", "${state.themeId.emoji} ${state.themeId.displayName}") {
            activeDialog = SettingsDialog.THEME
        }

        SettingsRow(Icons.Default.Notifications, "یادآوری‌ها",
            if (state.notifyThresholds.isEmpty()) "غیرفعال"
            else state.notifyThresholds.sorted().joinToString(" و ") { "$it روز" }
        ) { activeDialog = SettingsDialog.NOTIFY }

        SettingsRow(Icons.Default.Email, "ایمیل من",
            state.myEmail.ifBlank { "تنظیم نشده" }
        ) { activeDialog = SettingsDialog.MY_EMAIL }

        SettingsRow(Icons.Default.Info, "درباره‌ی اپ", "نسخه ۱.۰.۰") { activeDialog = SettingsDialog.ABOUT }

        Spacer(Modifier.height(80.dp))
    }

    when (activeDialog) {
        SettingsDialog.LOGO -> SettingsDialogScaffold("لوگوی اپلیکیشن", { activeDialog = null }) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(72.dp).clip(RoundedCornerShape(16.dp)).background(c.surfaceElevated)
                    .border(1.dp, c.outline, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    if (state.logoPath != null) LogoImage(state.logoPath, Modifier.fillMaxSize())
                    else Icon(Icons.Default.Image, null, tint = c.textMuted)
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        logoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }, colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
                        shape = RoundedCornerShape(10.dp)) {
                        Text(if (state.logoPath == null) "انتخاب لوگو" else "تغییر لوگو", fontSize = 12.sp)
                    }
                    if (state.logoPath != null) {
                        TextButton(onClick = { vm.setLogoPath(null) }) {
                            Text("حذف لوگو", color = StatusCritical, fontSize = 12.sp)
                        }
                    }
                }
            }
            Text("این لوگو تو داشبورد نمایش داده می‌شه. اگه با انتخاب عکس چیزی تغییر نکرد، دوباره امتحان کن یا از یه عکس دیگه استفاده کن.",
                color = c.textMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 10.dp))
        }

        SettingsDialog.THEME -> SettingsDialogScaffold("تم و رنگ‌ها", { activeDialog = null }) {
            Text("تم آماده انتخاب کن یا رنگ‌ها رو دقیق شخصی‌سازی کن", color = c.textMuted, fontSize = 11.sp)
            Spacer(Modifier.height(12.dp))
            AppThemeId.entries.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { tid ->
                        val tc = if (tid == AppThemeId.CUSTOM) (state.customColors ?: themeColors[tid]!!) else themeColors[tid]!!
                        val selected = state.themeId == tid
                        Card(modifier = Modifier.weight(1f).clickable { vm.setTheme(tid) },
                            colors = CardDefaults.cardColors(containerColor = tc.background),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(if (selected) 2.dp else 1.dp,
                                if (selected) tc.primary else tc.outline)) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(tc.primary, StatusCritical, StatusWarning, StatusActive).forEach { col ->
                                        Box(Modifier.size(10.dp).clip(CircleShape).background(col))
                                    }
                                }
                                Text("${tid.emoji} ${tid.displayName}", color = tc.textPrimary, fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
            }
            HorizontalDivider(color = c.outline.copy(0.4f), modifier = Modifier.padding(vertical = 8.dp))
            Text("شخصی‌سازی دقیق رنگ‌ها", color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("با اولین تغییر، تم روی «سفارشی من» می‌ره.", color = c.textMuted, fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 6.dp))
            val activeCustom = state.customColors ?: themeColors[AppThemeId.LIGHT]!!
            ColorSlot.entries.forEach { slot ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .clickable { editingSlot = slot }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(slot.label, color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(slot.hint, color = c.textMuted, fontSize = 10.sp)
                    }
                    Box(Modifier.size(28.dp).clip(CircleShape).background(activeCustom.get(slot))
                        .border(1.dp, c.outline, CircleShape))
                }
                HorizontalDivider(color = c.outline.copy(0.4f))
            }
        }

        SettingsDialog.NOTIFY -> SettingsDialogScaffold("یادآوری‌ها", { activeDialog = null }) {
            Text("چند روز قبل از پایان قرارداد نوتیف بگیری؟ چندتا رو می‌تونی هم‌زمان انتخاب کنی. حتی اگه اپ بسته باشه، نوتیف روزانه ارسال می‌شه.",
                color = c.textMuted, fontSize = 11.sp)
            Spacer(Modifier.height(14.dp))
            val options = listOf(3, 7, 15, 30, 60, 90)
            options.chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { d ->
                        val selected = d in state.notifyThresholds
                        Card(modifier = Modifier.weight(1f).clickable {
                            val next = if (selected) state.notifyThresholds - d else state.notifyThresholds + d
                            vm.setNotifyThresholds(next)
                        }, colors = CardDefaults.cardColors(containerColor = if (selected) c.primary else c.surfaceElevated),
                            shape = RoundedCornerShape(12.dp)) {
                            Text("$d روز", color = if (selected) c.onPrimary else c.textSecondary, fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp))
                        }
                    }
                }
            }
        }

        SettingsDialog.MY_EMAIL -> SettingsDialogScaffold("ایمیل من", { activeDialog = null }) {
            Text("این ایمیل، ایمیل شخصی خودته (مثلاً برای اقدامات مربوط به تقویم). ایمیل مدیر برای هر قرارداد جدا از فایل اکسل خونده می‌شه.",
                color = c.textMuted, fontSize = 11.sp)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = myEmailText,
                onValueChange = { myEmailText = it; vm.setMyEmail(it) },
                placeholder = { Text("you@email.com", color = c.textMuted) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = c.primary, unfocusedBorderColor = c.outline,
                    focusedTextColor = c.textPrimary, unfocusedTextColor = c.textPrimary,
                    cursorColor = c.primary)
            )
        }

        SettingsDialog.ABOUT -> SettingsDialogScaffold("درباره‌ی اپ", { activeDialog = null }) {
            listOf("نسخه" to "1.0.0", "پلتفرم" to "Android", "تقویم" to "شمسی").forEach { (l, v) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), Arrangement.SpaceBetween) {
                    Text(l, color = c.textMuted, fontSize = 12.sp)
                    Text(v, color = c.textSecondary, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(c.primary.copy(0.22f), c.primary.copy(0.06f))))
                    .border(1.dp, c.primary.copy(0.35f), RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("✨", fontSize = 22.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("طراحی و توسعه", color = c.textMuted, fontSize = 11.sp)
                    Text("نگار شجاعی 🙂", color = c.textPrimary, fontSize = 19.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(4.dp))
                    Text("ساخته‌شده با ❤️ برای مدیریت بهتر قراردادها", color = c.textSecondary, fontSize = 11.sp,
                        textAlign = TextAlign.Center)
                }
            }
        }
        null -> {}
    }

    editingSlot?.let { slot ->
        val activeCustom = state.customColors ?: themeColors[AppThemeId.LIGHT]!!
        ColorPickerDialog(
            slot = slot,
            initial = activeCustom.get(slot),
            onDismiss = { editingSlot = null },
            onConfirm = { color -> vm.setCustomColor(slot, color); editingSlot = null }
        )
    }
}

private enum class SettingsDialog { LOGO, THEME, NOTIFY, MY_EMAIL, ABOUT }

@Composable
private fun SettingsRow(
    icon: ImageVector, title: String, subtitle: String,
    trailing: (@Composable () -> Unit)? = null, onClick: () -> Unit
) {
    val c = appColors()
    Card(colors = CardDefaults.cardColors(containerColor = c.surface), shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = c.primary, modifier = Modifier.size(20.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = c.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(subtitle, color = c.textMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            trailing?.invoke()
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = c.textMuted, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun SettingsDialogScaffold(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val c = appColors()
    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = c.surfaceElevated), shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp).heightIn(max = 560.dp).verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text(title, color = c.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = c.textMuted) }
                }
                Spacer(Modifier.height(10.dp))
                content()
            }
        }
    }
}
