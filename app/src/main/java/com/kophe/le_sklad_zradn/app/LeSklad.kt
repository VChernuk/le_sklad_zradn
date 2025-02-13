package com.kophe.le_sklad_zradn.app

import androidx.databinding.DataBindingUtil
import com.google.firebase.FirebaseApp
import com.kophe.le_sklad_zradn.di.DaggerAppComponent
import com.kophe.le_sklad_zradn.util.binding.BindingComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class LeSkladApp : DaggerApplication() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        DataBindingUtil.setDefaultComponent(BindingComponent())
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.factory().create(this)
    }

}
