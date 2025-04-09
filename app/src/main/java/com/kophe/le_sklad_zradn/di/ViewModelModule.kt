package com.kophe.le_sklad_zradn.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kophe.le_sklad_zradn.screens.admin.backups.viewmodel.BackupsViewModel
import com.kophe.le_sklad_zradn.screens.admin.categories.viewmodel.CategoriesViewModel
import com.kophe.le_sklad_zradn.screens.admin.createcategory.viewmodel.CreateCategoryViewModel
import com.kophe.le_sklad_zradn.screens.admin.createlocation.viewmodel.CreateLocationViewModel
import com.kophe.le_sklad_zradn.screens.admin.viewmodel.AdminViewModel
import com.kophe.le_sklad_zradn.screens.auth.viewmodel.AuthViewModel
import com.kophe.le_sklad_zradn.screens.createissuance.viewmodel.CreateIssuanceViewModel
import com.kophe.le_sklad_zradn.screens.createdeliverynote.viewmodel.CreateDeliveryNoteViewModel
import com.kophe.le_sklad_zradn.screens.edititem.viewmodel.EditItemViewModel
import com.kophe.le_sklad_zradn.screens.filter.viewmodel.FilterViewModel
import com.kophe.le_sklad_zradn.screens.issuance.viewmodel.IssuanceViewModel
import com.kophe.le_sklad_zradn.screens.deliverynote.viewmodel.DeliveryNoteViewModel
import com.kophe.le_sklad_zradn.screens.items.viewmodel.ItemsViewModel
import com.kophe.le_sklad_zradn.screens.admin.locations.viewmodel.LocationsViewModel
import com.kophe.le_sklad_zradn.screens.navdrawer.HomeNavigationViewModel
import com.kophe.le_sklad_zradn.screens.selectitems.viewmodel.SelectItemsViewModel
import com.kophe.le_sklad_zradn.screens.splash.viewmodel.SplashViewModel
import com.kophe.le_sklad_zradn.screens.viewissuance.ViewIssuanceViewModel
import com.kophe.le_sklad_zradn.screens.viewdeliverynote.ViewDeliveryNoteViewModel
import com.kophe.le_sklad_zradn.util.viewmodelfactory.ViewModelFactory
import com.kophe.le_sklad_zradn.util.viewmodelfactory.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    internal abstract fun bindSplashViewModel(viewModel: SplashViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    internal abstract fun bindAuthViewModel(viewModel: AuthViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ItemsViewModel::class)
    internal abstract fun bindItemsViewModel(viewModel: ItemsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditItemViewModel::class)
    internal abstract fun bindEditItemViewModel(viewModel: EditItemViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FilterViewModel::class)
    internal abstract fun filterViewModel(viewModel: FilterViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(IssuanceViewModel::class)
    internal abstract fun issuanceViewModel(viewModel: IssuanceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DeliveryNoteViewModel::class)
    internal abstract fun deliverynoteViewModel(viewModel: DeliveryNoteViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SelectItemsViewModel::class)
    internal abstract fun selectItemsViewModel(viewModel: SelectItemsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateIssuanceViewModel::class)
    internal abstract fun createIssuanceViewModel(viewModel: CreateIssuanceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateDeliveryNoteViewModel::class)
    internal abstract fun createDeliveryNoteViewModel(viewModel: CreateDeliveryNoteViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeNavigationViewModel::class)
    internal abstract fun createHomeNavigationViewModel(viewModel: HomeNavigationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AdminViewModel::class)
    internal abstract fun createAdminViewModel(viewModel: AdminViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ViewIssuanceViewModel::class)
    internal abstract fun createViewIssuanceViewModel(viewModel: ViewIssuanceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ViewDeliveryNoteViewModel::class)
    internal abstract fun createViewDeliveryNoteViewModel(viewModel: ViewDeliveryNoteViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BackupsViewModel::class)
    internal abstract fun createBackupsViewModel(viewModel: BackupsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LocationsViewModel::class)
    internal abstract fun createLocationsViewModel(viewModel: LocationsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateLocationViewModel::class)
    internal abstract fun createCreateLocationViewModel(viewModel: CreateLocationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CategoriesViewModel::class)
    internal abstract fun createCategoriesViewModel(viewModel: CategoriesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateCategoryViewModel::class)
    internal abstract fun createCreateCategoryViewModel(viewModel: CreateCategoryViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

}
