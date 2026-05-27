package com.calcwidget.app

import android.content.Context
import android.content.SharedPreferences
import kotlin.math.abs

/**
 * Calculator engine with SharedPreferences-backed state,
 * shared between the widget and the main Activity.
 */
object CalculatorEngine {

    private const val PREFS = "calc_state"
    private const val KEY_DISPLAY = "display"
    private const val KEY_PREV    = "previous"
    private const val KEY_OP      = "operator"
    private const val KEY_WAITING = "waiting"

    // ── State accessors ──────────────────────────────────────────────────────

    fun getDisplay(ctx: Context)  : String  = prefs(ctx).getString(KEY_DISPLAY, "0") ?: "0"
    fun getOperator(ctx: Context) : String  = prefs(ctx).getString(KEY_OP, "")      ?: ""
    private fun getPrev(ctx: Context) : String  = prefs(ctx).getString(KEY_PREV, "")  ?: ""
    private fun isWaiting(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_WAITING, false)

    // ── Button handler ───────────────────────────────────────────────────────

    fun handleButton(ctx: Context, btn: String) {
        when (btn) {
            "C"   -> clear(ctx)
            "+/-" -> toggleSign(ctx)
            "%"   -> percent(ctx)
            "."   -> inputDecimal(ctx)
            "="   -> calcResult(ctx)
            "+", "-", "×", "÷" -> performOp(ctx, btn)
            else  -> inputDigit(ctx, btn)   // "0".."9"
        }
    }

    // ── Operations ───────────────────────────────────────────────────────────

    private fun clear(ctx: Context) = save(ctx, "0", "", "", false)

    private fun toggleSign(ctx: Context) {
        val d = getDisplay(ctx).toDoubleOrNull() ?: return
        save(ctx, fmt(-d), getPrev(ctx), getOperator(ctx), isWaiting(ctx))
    }

    private fun percent(ctx: Context) {
        val d = getDisplay(ctx).toDoubleOrNull() ?: return
        save(ctx, fmt(d / 100.0), getPrev(ctx), getOperator(ctx), isWaiting(ctx))
    }

    private fun inputDecimal(ctx: Context) {
        if (isWaiting(ctx)) {
            save(ctx, "0.", "", "", false)
            return
        }
        val display = getDisplay(ctx)
        if (!display.contains('.')) {
            save(ctx, "$display.", getPrev(ctx), getOperator(ctx), false)
        }
    }

    private fun inputDigit(ctx: Context, digit: String) {
        val display = getDisplay(ctx)
        val newDisplay = if (isWaiting(ctx)) digit
                         else if (display == "0") digit
                         else display + digit
        save(ctx, newDisplay, getPrev(ctx), getOperator(ctx), false)
    }

    private fun performOp(ctx: Context, nextOp: String) {
        val display  = getDisplay(ctx)
        val prev     = getPrev(ctx)
        val operator = getOperator(ctx)
        val input    = display.toDoubleOrNull() ?: 0.0

        val newPrev = if (prev.isEmpty()) {
            display
        } else if (operator.isNotEmpty()) {
            fmt(calculate(prev.toDoubleOrNull() ?: 0.0, input, operator))
        } else {
            prev
        }

        val newDisplay = if (prev.isNotEmpty() && operator.isNotEmpty())
            fmt(calculate(prev.toDoubleOrNull() ?: 0.0, input, operator))
        else display

        save(ctx, newDisplay, newPrev, nextOp, true)
    }

    private fun calcResult(ctx: Context) {
        val display  = getDisplay(ctx)
        val prev     = getPrev(ctx)
        val operator = getOperator(ctx)
        if (operator.isEmpty() || prev.isEmpty()) return
        val result = calculate(prev.toDoubleOrNull() ?: 0.0,
                               display.toDoubleOrNull() ?: 0.0, operator)
        save(ctx, fmt(result), "", "", true)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun calculate(a: Double, b: Double, op: String): Double {
        val r = when (op) {
            "+" -> a + b
            "-" -> a - b
            "×" -> a * b
            "÷" -> if (b != 0.0) a / b else Double.NaN
            else -> b
        }
        // Clamp floating-point noise
        return if (r.isNaN() || r.isInfinite()) r
               else "%.12g".format(r).toDouble()
    }

    /** Format a Double for display: strip trailing zeros, cap length. */
    fun fmt(d: Double): String {
        if (d.isNaN()) return "Error"
        if (d.isInfinite()) return if (d > 0) "∞" else "-∞"
        val s = "%.10g".format(d)
        return if (s.contains('.')) s.trimEnd('0').trimEnd('.') else s
    }

    private fun save(ctx: Context, display: String, prev: String,
                     op: String, waiting: Boolean) {
        prefs(ctx).edit()
            .putString(KEY_DISPLAY, display)
            .putString(KEY_PREV,    prev)
            .putString(KEY_OP,      op)
            .putBoolean(KEY_WAITING, waiting)
            .apply()
    }

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
