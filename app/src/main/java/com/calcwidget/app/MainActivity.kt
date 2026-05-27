package com.calcwidget.app

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.calcwidget.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        setupButtons()
        setupThemeSelector()
        refreshDisplay()
        applyTheme()
    }

    // ── Button wiring ─────────────────────────────────────────────────────────

    private fun setupButtons() {
        // Map each button view to its value
        val map = mapOf(
            b.btnC     to "C",   b.btnSign  to "+/-", b.btnPct   to "%",
            b.btnDiv   to "÷",   b.btn7     to "7",   b.btn8     to "8",
            b.btn9     to "9",   b.btnMul   to "×",   b.btn4     to "4",
            b.btn5     to "5",   b.btn6     to "6",   b.btnSub   to "-",
            b.btn1     to "1",   b.btn2     to "2",   b.btn3     to "3",
            b.btnAdd   to "+",   b.btnZero  to "0",   b.btnDot   to ".",
            b.btnEq    to "="
        )
        map.forEach { (view, value) ->
            view.setOnClickListener {
                animatePress(it)
                CalculatorEngine.handleButton(this, value)
                refreshDisplay()
                CalculatorWidgetProvider.refresh(this)   // sync widget
            }
        }
    }

    private fun setupThemeSelector() {
        b.btnTheme.setOnClickListener {
            b.themePanel.isVisible = !b.themePanel.isVisible
        }

        // Dynamically add a circle button per theme
        ThemeManager.themes.forEach { (name, theme) ->
            val btn = View(this).apply {
                layoutParams = ViewGroup.MarginLayoutParams(56.dp, 56.dp).apply {
                    marginEnd = 12.dp
                }
                background = circleDrawable(theme.actionBg, theme.widgetBg)
                setOnClickListener {
                    ThemeManager.save(this@MainActivity, name)
                    b.themePanel.isVisible = false
                    applyTheme()
                    CalculatorWidgetProvider.refresh(this@MainActivity)
                }
                contentDescription = name
            }
            b.themePicker.addView(btn)
        }
    }

    // ── UI refresh ────────────────────────────────────────────────────────────

    private fun refreshDisplay() {
        b.tvDisplay.text  = CalculatorEngine.getDisplay(this)
        b.tvOperator.text = CalculatorEngine.getOperator(this)
    }

    private fun applyTheme() {
        val t = ThemeManager.current(this)

        // Root background
        b.root.setBackgroundColor(t.widgetBg)

        // Display panel
        b.displayPanel.background = roundedDrawable(t.displayBg,
            (t.cornerDp * 0.8f).toInt().dp.toFloat())
        b.tvDisplay.setTextColor(t.displayText)
        b.tvOperator.setTextColor(adjustAlpha(t.displayText, 0.5f))

        // Buttons
        val allBtns = listOf(b.btnC, b.btnSign, b.btnPct,
            b.btn7, b.btn8, b.btn9, b.btn4, b.btn5, b.btn6,
            b.btn1, b.btn2, b.btn3, b.btnZero, b.btnDot)
        allBtns.forEach { btn ->
            btn.background = roundedDrawable(t.btnBg, t.cornerDp.dp.toFloat())
            btn.setTextColor(t.btnText)
        }

        val actionBtns = listOf(b.btnDiv, b.btnMul, b.btnSub, b.btnAdd, b.btnEq)
        actionBtns.forEach { btn ->
            btn.background = roundedDrawable(t.actionBg, t.cornerDp.dp.toFloat())
            btn.setTextColor(t.actionText)
        }

        // Theme panel bg
        b.themePanel.setBackgroundColor(adjustAlpha(t.widgetBg, 0.95f))
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun animatePress(v: View) {
        val scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1f, 0.90f, 1f)
        val scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f, 0.90f, 1f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 120
            start()
        }
    }

    private fun roundedDrawable(color: Int, radiusPx: Float) =
        android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setColor(color)
            cornerRadius = radiusPx
        }

    private fun circleDrawable(border: Int, fill: Int) =
        android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(fill)
            setStroke(4.dp, border)
        }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val a = (Color.alpha(color) * factor).toInt().coerceIn(0, 255)
        return Color.argb(a, Color.red(color), Color.green(color), Color.blue(color))
    }

    private val Int.dp get() = (this * resources.displayMetrics.density + 0.5f).toInt()
}
