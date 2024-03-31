package com.sahil.recipeapp.uis.view.base

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.sahil.recipeapp.R
import com.google.android.material.snackbar.Snackbar

open class BaseActivity : AppCompatActivity() {

    fun showError(view: View, message: String?) {
        val errorMsgToDisplay = if (!message.isNullOrEmpty()) {
            message
        } else {
            getString(R.string.label_error_try_again)
        }
        Snackbar.make(
            view,
            errorMsgToDisplay,
            Snackbar.LENGTH_SHORT
        ).setBackgroundTint(ContextCompat.getColor(this, R.color.snack_bar_background))
            .setActionTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.snack_bar_text_color
                )
            )
            .show()
    }
}