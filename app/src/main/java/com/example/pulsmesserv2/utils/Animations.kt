package com.example.pulsmesserv2.utils

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.pulsmesserv2.R

class Animations(private val context: Context) {

    fun growAndShrink(circle: ConstraintLayout){
        val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
            circle,
            PropertyValuesHolder.ofFloat("scaleX", 1.1f),
            PropertyValuesHolder.ofFloat("scaleY", 1.1f)
        )
        scaleDown.duration = 2000
        scaleDown.repeatMode = ValueAnimator.REVERSE
        scaleDown.repeatCount = ValueAnimator.INFINITE
        scaleDown.start()
    }
    fun colorChange(circle: ConstraintLayout){
        val colorChange = ValueAnimator.ofArgb(
            context.getColor(R.color.foreground),
            context.getColor(R.color.disabled)
        )
        colorChange.addUpdateListener { animator ->
            val color = animator.animatedValue as Int
            circle.backgroundTintList = ColorStateList.valueOf(color)
        }
        colorChange.duration = 2000 // Dauer der Animation in Millisekunden
        colorChange.repeatMode = ValueAnimator.REVERSE
        colorChange.repeatCount = ValueAnimator.INFINITE
        colorChange.start()
    }
}