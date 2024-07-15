package com.example.pulsmesserv2.utils

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TypefaceSpan
import android.view.MenuItem
import androidx.core.content.res.ResourcesCompat

fun applyFontToMenuItem(context: Context, menuItem: MenuItem, fontResId: Int) {
    val typeface = ResourcesCompat.getFont(context, fontResId)
    typeface?.let {
        val title = menuItem.title.toString()
        val spannableString = SpannableString(title)
        spannableString.setSpan(TypefaceSpan(it), 0, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        menuItem.title = spannableString
    }
}