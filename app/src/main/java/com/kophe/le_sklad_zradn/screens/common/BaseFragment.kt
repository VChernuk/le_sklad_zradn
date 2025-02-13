package com.kophe.le_sklad_zradn.screens.common

import android.app.AlertDialog.BUTTON_NEGATIVE
import android.app.AlertDialog.BUTTON_POSITIVE
import android.content.Intent
import android.content.res.Configuration
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog.Builder
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.kophe.leskladlib.repository.common.TaskStatus.StatusFailed
import com.kophe.leskladlib.repository.common.TaskStatus.StatusInProgress
import java.io.File
import java.lang.ref.WeakReference

abstract class BaseFragment<T : ViewDataBinding> : Fragment() {

    protected open val titleRes: Int? = null
    protected lateinit var weakBinding: WeakReference<T>
    protected val binding
        get() = weakBinding.get()
    protected val baseActivity
        get() = activity as? BaseActivity

    protected fun hideKeyboard() = baseActivity?.hideKeyboard(view)

    protected fun hideActionBar() = baseActivity?.hideActionBar()

    protected fun showActionBar() = baseActivity?.showActionBar()

    override fun onResume() {
        super.onResume()
        setCustomTitle()
    }

    protected fun showDefaultErrorMessage() =
        showMessage(getString(com.kophe.leskladuilib.R.string.default_error_message))

    protected fun showToastMessage(msg: String) =
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()

    protected fun showMessage(
        msg: String, actionTitle: String? = null, actionListener: (() -> Unit)? = null
    ) = view?.let {
        val snackbar = Snackbar.make(it, msg, Snackbar.LENGTH_LONG)
        snackbar.setTextColor(resources.getColor(com.kophe.leskladuilib.R.color.lightColor))
        if (actionTitle != null && actionListener != null) {
            snackbar.setActionTextColor(resources.getColor(com.kophe.leskladuilib.R.color.colorAccent))
            snackbar.setAction(actionTitle) { actionListener.invoke() }
        }
        snackbar.show()
    }

    protected fun showAlertDialogMessage(
        msg: String, actionTitle: String, actionListener: (() -> Unit)
    ) = context?.let {
        val alertDialog = Builder(it).setMessage(msg).setCancelable(true).setPositiveButton(
            actionTitle
        ) { dialog, _ ->
            actionListener()
            dialog.cancel()
        }.setNegativeButton("Ніт") { dialog, _ ->
            dialog.cancel()
        }.show()
        alertDialog.getButton(BUTTON_POSITIVE)
            .setTextColor(resources.getColor(com.kophe.leskladuilib.R.color.colorPrimary))
        alertDialog.getButton(BUTTON_NEGATIVE)
            .setTextColor(resources.getColor(com.kophe.leskladuilib.R.color.colorPrimary))
    }

    protected fun shareFile(file: File?) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "plain/text"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "export_${file?.name ?: ""}")
        val uri = FileProvider.getUriForFile(
            context ?: return,
            "com.kophe.le_sklad_zradn.provider",
            file ?: run {
                showDefaultErrorMessage()
                return
            })
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(shareIntent, "Надіслати"))
    }

    protected fun navigate(directions: NavDirections) = try {
        hideKeyboard()
        findNavController().navigate(directions)
    } catch (e: Exception) {
//        showDefaultErrorMessage()
    }

    protected fun navigateUp() = findNavController().navigateUp()

    protected fun setStatusBarColor(@ColorRes color: Int) {
        activity?.window?.let { window ->
            window.clearFlags(FLAG_TRANSLUCENT_STATUS)
            window.addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(context ?: return, color)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val layout = view as? ViewGroup ?: return
        changeTextViewsLocale(layout)
        setCustomTitle()
    }

    private fun setCustomTitle() {
        baseActivity?.setTitle(titleRes?.let { getString(it) } ?: return)
    }

    private fun changeTextViewsLocale(parent: ViewGroup) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child is ViewGroup) {
                changeTextViewsLocale(child)
            } else {
                if (child == null) continue
                val textView = (child as? TextView) ?: continue
                val tag = textView.tag as? String ?: continue
                val resId = resources.getIdentifier(tag, "string", context?.packageName)
                try {
                    textView.text = getString(resId)
                } catch (_: Exception) {/*no resource found*/
                }
            }
        }
    }

    protected fun setupBackKeyDialogHandler(shouldShowDialog: Boolean) {
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { _: View?, keyCode: Int, event: KeyEvent? ->
            if (event?.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                handleBackPressed(shouldShowDialog)
                return@setOnKeyListener true
            }
            false
        }
    }

    protected fun handleBackPressed(shouldShowDialog: Boolean) {
        if (shouldShowDialog) {
            showAlertDialogMessage("Ви впевнені, що хочете вийти без збереження змін?",
                "Так",
                actionListener = { navigateUp() })
        } else navigateUp()
    }
}

abstract class BaseViewModelFragment<T : ViewDataBinding, V : BaseViewModel> : BaseFragment<T>() {
    protected val viewModel by lazy { createViewModel() }
    protected fun observeRequestStatus() {
        viewModel.requestStatus.observe(viewLifecycleOwner) {
            when (it) {
                is StatusFailed -> showMessage(
                    it.error?.message
                        ?: getString(com.kophe.leskladuilib.R.string.default_error_message)
                )

                StatusInProgress -> hideKeyboard()
                else -> {
                    //just ignore
                }
            }
        }
    }

    abstract fun createViewModel(): V
}
