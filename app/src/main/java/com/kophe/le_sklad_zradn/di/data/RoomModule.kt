package com.kophe.le_sklad_zradn.di.data

import android.content.Context
import androidx.room.Room
import com.kophe.leskladlib.datasource.LE_SKLAD_USERS_DATABASE
import com.kophe.leskladlib.datasource.LeSkladUserDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RoomModule {

    @Provides
    @Singleton
    internal fun provideUsersDatabase(context: Context): LeSkladUserDatabase = Room.databaseBuilder(
        context.applicationContext, LeSkladUserDatabase::class.java, LE_SKLAD_USERS_DATABASE
    ).fallbackToDestructiveMigration().build()

    @Provides
    @Singleton
    internal fun provideUserDao(usersDatabase: LeSkladUserDatabase) = usersDatabase.userDAO()

}
