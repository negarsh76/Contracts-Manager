package com.contracts.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.contracts.app.MainActivity
import com.contracts.app.R
import com.contracts.app.data.model.ContractStatus
import com.contracts.app.data.repository.ExcelRepository
import com.contracts.app.domain.SettingsStore
import java.util.Calendar
import java.util.concurrent.TimeUnit

const val CHANNEL_ID = "contracts_ch"

fun createChannel(ctx: Context) {
    val ch = NotificationChannel(CHANNEL_ID, "سررسید قراردادها", NotificationManager.IMPORTANCE_HIGH).apply {
        enableVibration(true)
    }
    (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
}

class DeadlineWorker(private val ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val contracts = ExcelRepository(ctx).loadCached()
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        fun notify(id: Int, title: String, body: String) {
            val pi = PendingIntent.getActivity(ctx, id,
                Intent(ctx, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK },
                PendingIntent.FLAG_IMMUTABLE)
            nm.notify(id, NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title).setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi).setAutoCancel(true).build())
        }

        val thresholds = SettingsStore.loadNotifyThresholds(ctx)
        thresholds.sorted().forEach { threshold ->
            val matching = contracts.filter { it.daysRemaining == threshold.toLong() }
            if (matching.isNotEmpty()) {
                val extra = if (matching.size > 3) " و ${matching.size - 3} مورد دیگر" else ""
                notify(200 + threshold, "⏰ $threshold روز تا سررسید – ${matching.size} قرارداد",
                    matching.take(3).joinToString("، ") { it.title.take(18) } + extra)
            }
        }
        contracts.filter { it.status == ContractStatus.EXPIRED }.let { list ->
            if (list.isNotEmpty()) notify(3, "⚫ منقضی – ${list.size} قرارداد", "${list.size} قرارداد منقضی")
        }
        return Result.success()
    }

    companion object {
        fun schedule(ctx: Context) {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
            }
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork("deadline_check",
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<DeadlineWorker>(1, TimeUnit.DAYS)
                    .setInitialDelay(target.timeInMillis - now.timeInMillis, TimeUnit.MILLISECONDS)
                    .addTag("deadline_check").build())
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) DeadlineWorker.schedule(ctx)
    }
}
