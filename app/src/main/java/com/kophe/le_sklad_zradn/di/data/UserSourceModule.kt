package com.kophe.le_sklad_zradn.di.data

import com.kophe.leskladlib.datasource.LeSkladUserDao
import com.kophe.leskladlib.datasource.currentusersource.CurrentUserSource
import com.kophe.leskladlib.datasource.currentusersource.DefaultCurrentUserSource
import com.kophe.leskladlib.logging.LoggingUtil
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class UserSourceModule {
    @Provides
    @Singleton
    internal fun provideCurrentUserSource(
        usersDao: LeSkladUserDao, loggingUtil: LoggingUtil
    ): CurrentUserSource = DefaultCurrentUserSource(usersDao, loggingUtil)

}
