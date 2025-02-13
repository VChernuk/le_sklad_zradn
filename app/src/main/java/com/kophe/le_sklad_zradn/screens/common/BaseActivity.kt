package com.kophe.le_sklad_zradn.screens.common

import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration

abstract class BaseActivity : AppCompatActivity() {


    fun hideKeyboard(view: View? = null) {
        val windowToken = view?.windowToken ?: return
        val inputManager = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager ?: return
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }

    abstract fun setTitle(title: String?)

    fun hideActionBar() {
        supportActionBar?.hide()
    }

    fun showActionBar() {
        supportActionBar?.show()
    }

    abstract fun updNav(controller: NavController, configuration: AppBarConfiguration)
}
