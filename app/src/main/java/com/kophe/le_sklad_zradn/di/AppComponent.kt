package com.kophe.le_sklad_zradn.di

import com.kophe.le_sklad_zradn.app.LeSkladApp
import com.kophe.le_sklad_zradn.di.data.DataModule
import com.kophe.le_sklad_zradn.di.data.RoomModule
import com.kophe.le_sklad_zradn.di.data.UserSourceModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [AndroidSupportInjectionModule::class, DataModule::class, RoomModule::class, UserSourceModule::class, UtilsModule::class, ViewModelModule::class, AppModule::class]
)
interface AppComponent : AndroidInjector<LeSkladApp> {

    @Component.Factory
    abstract class Builder : AndroidInjector.Factory<LeSkladApp>

}
