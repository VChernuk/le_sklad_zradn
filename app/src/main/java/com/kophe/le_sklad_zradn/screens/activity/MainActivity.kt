package com.kophe.le_sklad_zradn.screens.activity

import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.content.res.Configuration
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.kophe.le_sklad_zradn.R
import com.kophe.le_sklad_zradn.screens.common.BaseActivity
import com.kophe.le_sklad_zradn.util.devicespecific.DeviceSpecificFeaturesUtil
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import dagger.android.AndroidInjection
import java.lang.ref.WeakReference
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var loggingUtil: LoggingUtil

    @Inject
    lateinit var deviceSpecificFeaturesUtil: DeviceSpecificFeaturesUtil

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navControllerWR: WeakReference<NavController>

    override fun onResume() {
        super.onResume()
        loggingUtil.log("${loggingTag()} onResume()")
    }

    override fun onRestart() {
        super.onRestart()
        loggingUtil.log("${loggingTag()} onRestart()")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        loggingUtil.log("${loggingTag()} onCreate()")
        setContentView(R.layout.activity_main)
        requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
        val navController = Navigation.findNavController(this, R.id.home_graph_fragment)
        appBarConfiguration = AppBarConfiguration.Builder(
            R.id.authFragment,
            R.id.splashFragment,
            R.id.itemsFragment,
            R.id.mainFragment,
            R.id.adminFragment,
            R.id.scriptsFragment,
            R.id.locationsFragment,
            R.id.categoriesFragment,
            R.id.backupsFragment,
            R.id.createCategoryFragment,
            R.id.createLocationFragment
        ).build()
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        navControllerWR = WeakReference(navController)
    }

    override fun updNav(controller: NavController, configuration: AppBarConfiguration) {
        loggingUtil.log("${loggingTag()} updNav(...)")
        appBarConfiguration = configuration
        NavigationUI.setupActionBarWithNavController(this, controller, appBarConfiguration)
        navControllerWR = WeakReference(controller)
    }

    override fun onPause() {
        super.onPause()
        loggingUtil.log("${loggingTag()} onPause()")
    }

    override fun onDestroy() {
        super.onDestroy()
        loggingUtil.log("${loggingTag()} onDestroy()")
    }

    override fun setTitle(title: String?) {
        supportActionBar?.title = title
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = navControllerWR.get()
        return navController?.let {
            NavigationUI.navigateUp(it, appBarConfiguration)
        } == true || super.onSupportNavigateUp()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        loggingUtil.log("${loggingTag()} configuration changed $newConfig")
        super.onConfigurationChanged(newConfig)
        val locales = newConfig.locales
        if (locales.isEmpty) return
        loggingUtil.log("${loggingTag()} current locale: ${locales.get(0)}")
    }

}
