package com.kophe.le_sklad_zradn.di

import android.content.Context
import com.kophe.le_sklad_zradn.app.LeSkladApp
import dagger.Binds
import dagger.Module
import dagger.android.support.AndroidSupportInjectionModule

@Module(includes = [AndroidSupportInjectionModule::class, InjectorsModule::class])
abstract class AppModule {

    @Binds
    abstract fun applicationContext(app: LeSkladApp): Context

}
