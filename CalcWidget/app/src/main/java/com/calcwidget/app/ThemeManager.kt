package com.calcwidget.app

import android.content.Context
import android.graphics.Color

data class CalcTheme(
    val widgetBg   : Int,
    val displayBg  : Int,
    val displayText: Int,
    val btnBg      : Int,
    val btnText    : Int,
    val actionBg   : Int,
    val actionText : Int,
    val cornerDp   : Int = 24
)

object ThemeManager {

    private const val PREFS = "calc_theme"
    private const val KEY   = "theme_name"

    val themes: Map<String, CalcTheme> = linkedMapOf(
        "Eco Default" to CalcTheme(
            widgetBg    = parseColor("#0B1105ED"),
            displayBg   = Color.TRANSPARENT,
            displayText = Color.WHITE,
            btnBg       = parseColor("#2AFFFFFF"),
            btnText     = Color.WHITE,
            actionBg    = parseColor("#9EAE6F"),
            actionText  = parseColor("#0B1105"),
        ),
        "Material Light" to CalcTheme(
            widgetBg    = parseColor("#F3F4F6"),
            displayBg   = Color.WHITE,
            displayText = parseColor("#111827"),
            btnBg       = parseColor("#E5E7EB"),
            btnText     = parseColor("#1F2937"),
            actionBg    = parseColor("#8B5CF6"),
            actionText  = Color.WHITE,
        ),
        "AMOLED Neon" to CalcTheme(
            widgetBg    = Color.BLACK,
            displayBg   = Color.BLACK,
            displayText = parseColor("#10B981"),
            btnBg       = parseColor("#111111"),
            btnText     = Color.WHITE,
            actionBg    = parseColor("#10B981"),
            actionText  = Color.BLACK,
            cornerDp    = 12,
        ),
        "Pastel Dream" to CalcTheme(
            widgetBg    = parseColor("#FDF4FF"),
            displayBg   = parseColor("#FCE7F3"),
            displayText = parseColor("#831843"),
            btnBg       = parseColor("#FBCFE8"),
            btnText     = parseColor("#9D174D"),
            actionBg    = parseColor("#EC4899"),
            actionText  = Color.WHITE,
            cornerDp    = 32,
        ),
        "Ocean Blue" to CalcTheme(
            widgetBg    = parseColor("#0F172A"),
            displayBg   = parseColor("#1E293B"),
            displayText = parseColor("#E0F2FE"),
            btnBg       = parseColor("#0F172A"),
            btnText     = parseColor("#BAE6FD"),
            actionBg    = parseColor("#0EA5E9"),
            actionText  = Color.WHITE,
            cornerDp    = 36,
        )
    )

    fun current(ctx: Context): CalcTheme {
        val name = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "Eco Default") ?: "Eco Default"
        return themes[name] ?: themes.values.first()
    }

    fun currentName(ctx: Context): String =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "Eco Default") ?: "Eco Default"

    fun save(ctx: Context, name: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, name).apply()
    }

    private fun parseColor(hex: String): Int {
        // Handle 8-digit hex (#AARRGGBB or #RRGGBBAA from CSS)
        return if (hex.length == 9) {
            // CSS format is #RRGGBBAA → Android wants AARRGGBB
            val r = hex.substring(1, 3)
            val g = hex.substring(3, 5)
            val b = hex.substring(5, 7)
            val a = hex.substring(7, 9)
            Color.parseColor("#$a$r$g$b")
        } else {
            Color.parseColor(hex)
        }
    }
}
