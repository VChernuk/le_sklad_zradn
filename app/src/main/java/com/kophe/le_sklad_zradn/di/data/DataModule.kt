package com.kophe.le_sklad_zradn.di.data

import android.content.Context
import com.kophe.le_sklad_zradn.util.constants.CATEGORIES
import com.kophe.le_sklad_zradn.util.constants.ISSUANCE
import com.kophe.le_sklad_zradn.util.constants.ITEMS
import com.kophe.le_sklad_zradn.util.constants.LOCATIONS
import com.kophe.le_sklad_zradn.util.constants.OWNERSHIP_TYPES
import com.kophe.le_sklad_zradn.util.constants.SUBCATEGORIES
import com.kophe.le_sklad_zradn.util.constants.SUBLOCATIONS
import com.kophe.leskladlib.connectivity.ConnectionStateMonitor
import com.kophe.leskladlib.datasource.currentusersource.CurrentUserSource
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.repository.admin.AdminRepository
import com.kophe.leskladlib.repository.admin.DefaultAdminRepository
import com.kophe.leskladlib.repository.categories.CategoriesRepository
import com.kophe.leskladlib.repository.categories.DefaultCategoriesRepository
import com.kophe.leskladlib.repository.common.RepositoryBuilder
import com.kophe.leskladlib.repository.images.DefaultImagesRepository
import com.kophe.leskladlib.repository.images.ImagesRepository
import com.kophe.leskladlib.repository.issuance.DefaultIssuanceRepository
import com.kophe.leskladlib.repository.issuance.IssuanceRepository
import com.kophe.leskladlib.repository.items.DefaultItemsRepository
import com.kophe.leskladlib.repository.items.ItemsRepository
import com.kophe.leskladlib.repository.locations.DefaultLocationsRepository
import com.kophe.leskladlib.repository.locations.LocationsRepository
import com.kophe.leskladlib.repository.ownership.DefaultOwnershipRepository
import com.kophe.leskladlib.repository.ownership.OwnershipRepository
import com.kophe.leskladlib.repository.userprofile.DefaultUserProfileRepository
import com.kophe.leskladlib.repository.userprofile.UserProfileRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataModule {

    private val repositoryBuilder by lazy {
        RepositoryBuilder(
            CATEGORIES,
            OWNERSHIP_TYPES,
            LOCATIONS,
            SUBCATEGORIES,
            ITEMS,
            SUBLOCATIONS,
            ISSUANCE,
            "",
            "users",
            "admin_users",
            "units"
        )
    }

    @Provides
    @Singleton
    internal fun provideImagesRepository(loggingUtil: LoggingUtil): ImagesRepository =
        DefaultImagesRepository(loggingUtil)

    @Provides
    @Singleton
    internal fun provideAdminRepository(
        loggingUtil: LoggingUtil, itemsRepository: ItemsRepository, context: Context
    ): AdminRepository = DefaultAdminRepository(
        loggingUtil = loggingUtil, itemsRepository, repositoryBuilder, context.filesDir.absolutePath
    )

    @Provides
    @Singleton
    internal fun provideAuthRepository(
        loggingUtil: LoggingUtil,
        currentUserSource: CurrentUserSource,
        connectionStateMonitor: ConnectionStateMonitor
    ): UserProfileRepository = DefaultUserProfileRepository(
        currentUserSource = currentUserSource,
        loggingUtil = loggingUtil,
        builder = repositoryBuilder,
        connectionStateMonitor
    )

    @Provides
    @Singleton
    internal fun provideLocationsRepository(
        loggingUtil: LoggingUtil, connectionStateMonitor: ConnectionStateMonitor
    ): LocationsRepository =
        DefaultLocationsRepository(loggingUtil, builder = repositoryBuilder, connectionStateMonitor)


    @Provides
    @Singleton
    internal fun provideCategoriesRepository(
        loggingUtil: LoggingUtil, connectionStateMonitor: ConnectionStateMonitor
    ): CategoriesRepository = DefaultCategoriesRepository(
        loggingUtil, builder = repositoryBuilder, connectionStateMonitor
    )

    @Provides
    @Singleton
    internal fun provideOwnershipRepository(
        loggingUtil: LoggingUtil, connectionStateMonitor: ConnectionStateMonitor
    ): OwnershipRepository = DefaultOwnershipRepository(
        loggingUtil, builder = repositoryBuilder, connectionStateMonitor
    )


    @Provides
    @Singleton
    internal fun provideItemsRepository(
        loggingUtil: LoggingUtil,
        locationsRepository: LocationsRepository,
        ownershipRepository: OwnershipRepository,
        categoriesRepository: CategoriesRepository,
        connectionStateMonitor: ConnectionStateMonitor
    ): ItemsRepository = DefaultItemsRepository(
        loggingUtil,
        repositoryBuilder,
        locationsRepository = locationsRepository,
        categoriesRepository = categoriesRepository,
        ownershipRepository = ownershipRepository,
        unitsRepository = null
    )

    @Provides
    @Singleton
    internal fun provideIssuanceRepository(
        loggingUtil: LoggingUtil,
        locationsRepository: LocationsRepository,
        itemsRepository: ItemsRepository,
        userProfileRepository: UserProfileRepository
    ): IssuanceRepository = DefaultIssuanceRepository(
        loggingUtil, repositoryBuilder, locationsRepository, itemsRepository, null, userProfileRepository
    )

}
