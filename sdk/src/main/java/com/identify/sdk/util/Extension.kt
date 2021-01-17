package com.identify.sdk.util

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun <T> Fragment.observe(data: LiveData<T>, block: (T) -> Unit) {
    data.observe(this, Observer(block))
}


fun Context.alert(isCancelable : Boolean, posBtnText : String?, negBtnText: String?, title : String?, message : String?, positiveClicked : (dialog : DialogInterface) -> Unit,negativeClicked : (dialog : DialogInterface) -> Unit, neutralClicked : (dialog : DialogInterface) -> Unit){
    val builder = AlertDialog.Builder(this)
    builder.setTitle(title)
    builder.setMessage(message)
    builder.setCancelable(isCancelable)
    builder.setPositiveButton(posBtnText) { dialog, _ ->
        positiveClicked(dialog)
    }
    builder.setNegativeButton(negBtnText) { dialog, _ ->
        negativeClicked(dialog)
    }
    builder.show()
}


fun MaterialAlertDialogBuilder.negativeButton(
    text: String = "No",
    handleClick: (dialogInterface: DialogInterface) -> Unit = { it.dismiss() }
) {
    this.setNegativeButton(text) { dialogInterface, _ -> handleClick(dialogInterface) }
}

fun MaterialAlertDialogBuilder.positiveButton(
    text: String = "Yes",
    handleClick: (dialogInterface: DialogInterface) -> Unit = { it.dismiss() }
) {
    this.setPositiveButton(text) { dialogInterface, _ -> handleClick(dialogInterface) }
}

fun MaterialAlertDialogBuilder.neutralButton(
    text: String = "OK",
    handleClick: (dialogInterface: DialogInterface) -> Unit = { it.dismiss() }
) {
    this.setNeutralButton(text) { dialogInterface, _ -> handleClick(dialogInterface) }
}