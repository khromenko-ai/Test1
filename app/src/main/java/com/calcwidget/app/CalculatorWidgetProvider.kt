package com.calcwidget.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.widget.RemoteViews

class CalculatorWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_BTN = "com.calcwidget.app.BUTTON_CLICK"
        const val EXTRA_BTN  = "button_value"

        private val BUTTONS = listOf(
            "C", "+/-", "%", "÷",
            "7", "8",   "9", "×",
            "4", "5",   "6", "-",
            "1", "2",   "3", "+",
            "0", "0",   ".", "="   // "0" appears twice → wide key handled in XML
        )

        private val ACTION_BTNS = setOf("÷", "×", "-", "+", "=")

        /** Push a full widget refresh to all instances. */
        fun refresh(ctx: Context) {
            val mgr    = AppWidgetManager.getInstance(ctx)
            val ids    = mgr.getAppWidgetIds(ComponentName(ctx, CalculatorWidgetProvider::class.java))
            val intent = Intent(ctx, CalculatorWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            ctx.sendBroadcast(intent)
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateWidget(ctx, mgr, it) }
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        super.onReceive(ctx, intent)
        if (intent.action == ACTION_BTN) {
            val btn = intent.getStringExtra(EXTRA_BTN) ?: return
            CalculatorEngine.handleButton(ctx, btn)
            refresh(ctx)
        }
    }

    // ── Widget builder ────────────────────────────────────────────────────────

    private fun updateWidget(ctx: Context, mgr: AppWidgetManager, id: Int) {
        val theme   = ThemeManager.current(ctx)
        val display = CalculatorEngine.getDisplay(ctx)
        val op      = CalculatorEngine.getOperator(ctx)

        val views = RemoteViews(ctx.packageName, R.layout.widget_calculator)

        // Display text
        views.setTextViewText(R.id.tv_display, display)
        views.setTextColor(R.id.tv_display, theme.displayText)
        views.setTextViewText(R.id.tv_operator, op)
        views.setTextColor(R.id.tv_operator, theme.displayText)

        // Background bitmaps (RemoteViews can't use GradientDrawable directly)
        views.setImageViewBitmap(R.id.img_widget_bg,
            roundedRect(theme.widgetBg, theme.cornerDp.dp(ctx)))
        views.setImageViewBitmap(R.id.img_display_bg,
            roundedRect(theme.displayBg, (theme.cornerDp * 0.8f).toInt().dp(ctx)))

        // Wire every button
        val btnIds = listOf(
            R.id.btn_c,     R.id.btn_sign,  R.id.btn_pct,   R.id.btn_div,
            R.id.btn_7,     R.id.btn_8,     R.id.btn_9,     R.id.btn_mul,
            R.id.btn_4,     R.id.btn_5,     R.id.btn_6,     R.id.btn_sub,
            R.id.btn_1,     R.id.btn_2,     R.id.btn_3,     R.id.btn_add,
            R.id.btn_zero,  R.id.btn_dot,   R.id.btn_eq
        )
        val btnValues = listOf(
            "C", "+/-", "%", "÷",
            "7", "8",   "9", "×",
            "4", "5",   "6", "-",
            "1", "2",   "3", "+",
            "0", ".",   "="
        )

        btnIds.zip(btnValues).forEach { (viewId, value) ->
            val isAction = value in ACTION_BTNS
            val bg   = if (isAction) theme.actionBg else theme.btnBg
            val text = if (isAction) theme.actionText else theme.btnText
            val r    = theme.cornerDp.dp(ctx).toFloat()
            views.setImageViewBitmap(viewId, buttonBitmap(bg, r))
            views.setTextColor(viewId, text)                         // API 31+? safe fallback below
            views.setOnClickPendingIntent(viewId, btnPending(ctx, value))
        }

        mgr.updateAppWidget(id, views)
    }

    // ── Drawing helpers ────────────────────────────────────────────────────────

    /** Solid rounded-rect bitmap used as a view background. */
    private fun roundedRect(color: Int, cornerPx: Int, w: Int = 4, h: Int = 4): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint  = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        canvas.drawRoundRect(0f, 0f, w.toFloat(), h.toFloat(),
            cornerPx.toFloat(), cornerPx.toFloat(), paint)
        return bmp
    }

    /** Small bitmap used as button background (stretched by RemoteViews). */
    private fun buttonBitmap(color: Int, cornerPx: Float): Bitmap {
        val size = 48
        val bmp  = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c    = Canvas(bmp)
        val p    = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        c.drawRoundRect(0f, 0f, size.toFloat(), size.toFloat(), cornerPx, cornerPx, p)
        return bmp
    }

    private fun btnPending(ctx: Context, value: String): PendingIntent {
        val intent = Intent(ctx, CalculatorWidgetProvider::class.java).apply {
            action = ACTION_BTN
            putExtra(EXTRA_BTN, value)
        }
        val flags = if (Build.VERSION.SDK_INT >= 23)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT
        // Use unique request codes to avoid collisions
        val rc = value.hashCode() and 0xFFFF
        return PendingIntent.getBroadcast(ctx, rc, intent, flags)
    }

    private fun Int.dp(ctx: Context) =
        (this * ctx.resources.displayMetrics.density + 0.5f).toInt()
}
