package com.kophe.le_sklad_zradn.util.injection

import android.content.Context
import dagger.android.HasAndroidInjector

//TODO: review if needed
object ContextInjection {
    
    @JvmStatic
    fun inject(to: Any, with: Context) {
        (with.applicationContext as HasAndroidInjector).androidInjector().inject(to)
    }

}
