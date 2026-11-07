package com.contracts.app.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.GlanceModifier

class ContractsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Text(text = "مدیریت قراردادها")
            }
        }
    }
}

class ContractsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = ContractsWidget()
}
