package com.kophe.le_sklad_zradn.di

import com.kophe.le_sklad_zradn.screens.activity.MainActivity
import com.kophe.le_sklad_zradn.screens.admin.backups.view.BackupsFragment
import com.kophe.le_sklad_zradn.screens.admin.categories.view.CategoriesFragment
import com.kophe.le_sklad_zradn.screens.admin.createcategory.view.CreateCategoryFragment
import com.kophe.le_sklad_zradn.screens.admin.createlocation.view.CreateLocationFragment
import com.kophe.le_sklad_zradn.screens.admin.view.AdminFragment
import com.kophe.le_sklad_zradn.screens.admin.view.ScriptsFragment
import com.kophe.le_sklad_zradn.screens.auth.view.AuthFragment
import com.kophe.le_sklad_zradn.screens.createissuance.view.CreateIssuanceFragment
import com.kophe.le_sklad_zradn.screens.createdeliverynote.view.CreateDeliveryNoteFragment
import com.kophe.le_sklad_zradn.screens.edititem.view.CreateItemFragment
import com.kophe.le_sklad_zradn.screens.edititem.view.EditItemFragment
import com.kophe.le_sklad_zradn.screens.edititem.view.ViewItemFragment
import com.kophe.le_sklad_zradn.screens.filter.view.FilterFragment
import com.kophe.le_sklad_zradn.screens.imageviewer.ImageViewerFragment
import com.kophe.le_sklad_zradn.screens.issuance.view.IssuanceFragment
import com.kophe.le_sklad_zradn.screens.issuancescanner.view.IssuanceScannerFragment
import com.kophe.le_sklad_zradn.screens.deliverynote.view.DeliveryNoteFragment
import com.kophe.le_sklad_zradn.screens.deliverynotescanner.view.DeliveryNoteScannerFragment
import com.kophe.le_sklad_zradn.screens.items.view.ItemsFragment
import com.kophe.le_sklad_zradn.screens.admin.locations.view.LocationsFragment
import com.kophe.le_sklad_zradn.screens.main.MainFragment
import com.kophe.le_sklad_zradn.screens.navdrawer.HomeNavigationFragment
import com.kophe.le_sklad_zradn.screens.selectitems.view.SelectItemsFragment
import com.kophe.le_sklad_zradn.screens.splash.view.SplashFragment
import com.kophe.le_sklad_zradn.screens.support.view.SupportFragment
import com.kophe.le_sklad_zradn.screens.viewissuance.ViewIssuanceFragment
import com.kophe.le_sklad_zradn.screens.viewdeliverynote.ViewDeliveryNoteFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class InjectorsModule {

    @ContributesAndroidInjector
    abstract fun mainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun splashFragment(): SplashFragment

    @ContributesAndroidInjector
    abstract fun scriptsFragment(): ScriptsFragment

    @ContributesAndroidInjector
    abstract fun backupsFragment(): BackupsFragment

    @ContributesAndroidInjector
    abstract fun locationsFragment(): LocationsFragment

    @ContributesAndroidInjector
    abstract fun createLocationFragment(): CreateLocationFragment

    @ContributesAndroidInjector
    abstract fun categoriesFragment(): CategoriesFragment

    @ContributesAndroidInjector
    abstract fun createCategoryFragment(): CreateCategoryFragment

    @ContributesAndroidInjector
    abstract fun authFragment(): AuthFragment

    @ContributesAndroidInjector
    abstract fun itemsFragment(): ItemsFragment

    @ContributesAndroidInjector
    abstract fun editItemFragment(): EditItemFragment

    @ContributesAndroidInjector
    abstract fun createItemFragment(): CreateItemFragment

    @ContributesAndroidInjector
    abstract fun viewItemFragment(): ViewItemFragment

    @ContributesAndroidInjector
    abstract fun filterFragment(): FilterFragment

    @ContributesAndroidInjector
    abstract fun issuanceFragment(): IssuanceFragment

    @ContributesAndroidInjector
    abstract fun viewIssuanceFragment(): ViewIssuanceFragment

    @ContributesAndroidInjector
    abstract fun createIssuanceFragment(): CreateIssuanceFragment

    @ContributesAndroidInjector
    abstract fun issuanceScannerFragment(): IssuanceScannerFragment
    
    @ContributesAndroidInjector
    abstract fun deliverynoteFragment(): DeliveryNoteFragment

    @ContributesAndroidInjector
    abstract fun viewDeliveryNoteFragment(): ViewDeliveryNoteFragment

    @ContributesAndroidInjector
    abstract fun createDeliveryNoteFragment(): CreateDeliveryNoteFragment

    @ContributesAndroidInjector
    abstract fun deliverynoteScannerFragment(): DeliveryNoteScannerFragment

    @ContributesAndroidInjector
    abstract fun homeNavigationFragment(): HomeNavigationFragment

    @ContributesAndroidInjector
    abstract fun selectItemsFragment(): SelectItemsFragment

    @ContributesAndroidInjector
    abstract fun mainFragment(): MainFragment

    @ContributesAndroidInjector
    abstract fun supportFragment(): SupportFragment

    @ContributesAndroidInjector
    abstract fun adminFragment(): AdminFragment

    @ContributesAndroidInjector
    abstract fun imageViewerFragment(): ImageViewerFragment

}
