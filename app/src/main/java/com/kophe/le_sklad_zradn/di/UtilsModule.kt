package com.kophe.le_sklad_zradn.di

import android.content.Context
import com.kophe.le_sklad_zradn.BuildConfig.DEBUG
import com.kophe.le_sklad_zradn.BuildConfig.VERSION_NAME
import com.kophe.le_sklad_zradn.util.copy.CopyUtil
import com.kophe.le_sklad_zradn.util.copy.DefaultCopyUtil
import com.kophe.le_sklad_zradn.util.devicespecific.DefaultDeviceSpecificFeaturesUtil
import com.kophe.le_sklad_zradn.util.devicespecific.DeviceSpecificFeaturesUtil
import com.kophe.le_sklad_zradn.util.export.DefaultExportUtil
import com.kophe.le_sklad_zradn.util.export.ExportUtil
import com.kophe.leskladlib.connectivity.ConnectionStateMonitor
import com.kophe.leskladlib.connectivity.DefaultConnectionStateMonitor
import com.kophe.leskladlib.logging.DebugLoggingUtil
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.logging.ProdLoggingUtil
import dagger.Module
import dagger.Provides
import dagger.Reusable
import javax.inject.Singleton

@Module
class UtilsModule {

    @Provides
    @Singleton
    fun provideConnectionStateMonitor(context: Context, loggingUtil: LoggingUtil): ConnectionStateMonitor =
        DefaultConnectionStateMonitor(context, loggingUtil)

    @Provides
    @Reusable
    fun provideLoggingUtil(context: Context): LoggingUtil =
        if (DEBUG) DebugLoggingUtil(context, VERSION_NAME, DEBUG)
        else ProdLoggingUtil(context, VERSION_NAME, DEBUG)

    @Provides
    fun provideCopyUtil(context: Context): CopyUtil = DefaultCopyUtil(context)

    @Provides
    fun provideExportUtil(context: Context): ExportUtil = DefaultExportUtil(context)

    @Provides
    @Reusable
    fun provideDeviceSpecificFeaturesUtil(): DeviceSpecificFeaturesUtil =
        DefaultDeviceSpecificFeaturesUtil()

}
