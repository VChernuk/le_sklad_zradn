package com.kophe.le_sklad_zradn.util.copy

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import java.lang.ref.WeakReference

interface CopyUtil {

    fun copy(text: String?)

}

class DefaultCopyUtil(val context: Context) : CopyUtil {
    private val weakContext = WeakReference(context)

    override fun copy(text: String?) {
        if (text == null) return
        val context = weakContext.get() ?: return
        val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        clipboard.setPrimaryClip(ClipData.newPlainText(text, text))
        Toast.makeText(context, "Copied!", LENGTH_LONG).show()
    }

}
