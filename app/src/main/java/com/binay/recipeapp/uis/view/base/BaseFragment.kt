package com.binay.recipeapp.uis.view.base

import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.binay.recipeapp.R
import com.google.android.material.snackbar.Snackbar

open class BaseFragment : Fragment() {

    fun showError(message: String?) {
        val errorMsgToDisplay = if (!message.isNullOrEmpty()) {
            message
        } else {
            getString(R.string.label_error_try_again)
        }
        Snackbar.make(
            requireView(),
            errorMsgToDisplay,
            Snackbar.LENGTH_SHORT
        ).setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.snack_bar_background))
            .setActionTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.snack_bar_text_color
                )
            )
            .show()
    }
}