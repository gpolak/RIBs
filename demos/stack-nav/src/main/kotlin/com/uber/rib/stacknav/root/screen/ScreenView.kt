/*
 * Copyright (C) 2025. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.uber.rib.stacknav.root.screen

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Full-screen view for a single stack entry. Includes a top navigation bar with an explicit back
 * button (hidden on the root screen), the screen number in the centre, and a push button.
 */
class ScreenView(context: Context, screenNumber: Int, maxScreens: Int) : FrameLayout(context) {

  var onNextClicked: (() -> Unit)? = null
  var onBackClicked: (() -> Unit)? = null

  private val screenColors =
    listOf(
      Color.parseColor("#1565C0"), // deep blue
      Color.parseColor("#2E7D32"), // deep green
      Color.parseColor("#E65100"), // deep orange
      Color.parseColor("#6A1B9A"), // deep purple
      Color.parseColor("#B71C1C"), // deep red
    )

  init {
    val bg = screenColors[(screenNumber - 1) % screenColors.size]
    setBackgroundColor(bg)

    val dp = { n: Int ->
      TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, n.toFloat(), resources.displayMetrics)
        .toInt()
    }

    // ── Top navigation bar ──────────────────────────────────────────────────
    val navBar =
      LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setBackgroundColor(Color.parseColor("#22000000"))
        setPadding(dp(4), dp(4), dp(16), dp(4))
      }

    // Back button: a TextView styled as a tappable nav item
    val backBtn =
      TextView(context).apply {
        text = "‹  Back"
        setTextColor(Color.WHITE)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        typeface = Typeface.DEFAULT_BOLD
        setPadding(dp(12), dp(8), dp(16), dp(8))
        visibility = if (screenNumber > 1) VISIBLE else INVISIBLE
        isClickable = true
        isFocusable = true
        setOnClickListener { onBackClicked?.invoke() }
        background = null
      }

    val navTitle =
      TextView(context).apply {
        text = "Screen $screenNumber"
        setTextColor(Color.WHITE)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
      }

    // Invisible placeholder mirrors the back button width so the title stays centred
    val navEndSpacer =
      TextView(context).apply {
        text = "‹  Back"
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        setPadding(dp(12), dp(8), dp(16), dp(8))
        visibility = INVISIBLE
      }

    navBar.addView(backBtn)
    navBar.addView(navTitle)
    navBar.addView(navEndSpacer)

    // ── Body content ────────────────────────────────────────────────────────
    val bigNumber =
      TextView(context).apply {
        text = "$screenNumber"
        setTextColor(Color.WHITE)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 96f)
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
      }

    val depth =
      TextView(context).apply {
        text = "Depth $screenNumber of $maxScreens"
        setTextColor(Color.parseColor("#CCFFFFFF"))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        gravity = Gravity.CENTER
        setPadding(0, dp(4), 0, dp(40))
      }

    val nextButton =
      Button(context).apply {
        text =
          if (screenNumber < maxScreens) "Push Screen ${screenNumber + 1}  →" else "Stack is full"
        isEnabled = screenNumber < maxScreens
        setTextColor(Color.WHITE)
        setBackgroundColor(Color.parseColor("#33FFFFFF"))
        setPadding(dp(32), dp(12), dp(32), dp(12))
        setOnClickListener { onNextClicked?.invoke() }
      }

    val body =
      LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        addView(bigNumber)
        addView(depth)
        addView(nextButton)
      }

    // ── Root layout: nav bar pinned to top, body fills the rest ─────────────
    val root =
      LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        addView(
          navBar,
          LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
          ),
        )
        addView(body, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
      }

    addView(root, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

    // Push nav-bar content below the status bar on edge-to-edge displays (Android 15+).
    // The background colour already extends behind the status bar, which looks intentional.
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
      val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
      navBar.setPadding(dp(4), statusBarHeight + dp(4), dp(16), dp(4))
      insets
    }
  }

  // Insets are dispatched once at window-attach time. Screens pushed later onto the stack
  // are attached after that initial dispatch, so we re-request here to guarantee delivery.
  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    ViewCompat.requestApplyInsets(this)
  }
}
