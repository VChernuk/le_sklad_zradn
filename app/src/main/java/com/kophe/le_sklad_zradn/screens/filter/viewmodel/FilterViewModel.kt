package com.kophe.le_sklad_zradn.screens.filter.viewmodel

import com.kophe.le_sklad_zradn.screens.common.FilterContainerViewModel
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.Done
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.repository.categories.CategoriesRepository
import com.kophe.leskladlib.repository.common.Category
import com.kophe.leskladlib.repository.common.Filter
import com.kophe.leskladlib.repository.common.LSError
import com.kophe.leskladlib.repository.common.Location
import com.kophe.leskladlib.repository.common.OwnershipType
import com.kophe.leskladlib.repository.locations.LocationsRepository
import com.kophe.leskladlib.repository.ownership.OwnershipRepository
import com.kophe.leskladuilib.ItemSelectedListener
import javax.inject.Inject

//TODO: use cached options
class FilterViewModel @Inject constructor(
    loggingUtil: LoggingUtil,
    private val locationsRepository: LocationsRepository,
    private val categoriesRepository: CategoriesRepository,
    private val ownershipRepository: OwnershipRepository,
) : FilterContainerViewModel(loggingUtil) {
    var currentFilter = Filter()
        set(value) {
            field = value
            locationSelectionListener.onItemSelected(value.location?.title)
        }
    override val locationSelectionListener = object : ItemSelectedListener {
        override fun onItemSelected(item: Any?) {
            val location = locations.find { it.title == item } ?: return
            onLocationSelected(location)
        }
    }
    override val sublocationSelectionListener = object : ItemSelectedListener {
        override fun onItemSelected(item: Any?) {
            log("onSublocationSelected(...): $item")
            val sublocation = sublocations.find { it.title == item }
            currentFilter.sublocation = if (sublocation == allSubocations) null else sublocation
        }
    }
    override val deliveryNoteNumberSelectionListener = object : ItemSelectedListener {
        override fun onItemSelected(item: Any?) {
            log("onDeliveryNoteNumberSelected(...): $item")
            val deliveryNote = deliveryNotes.find { it.deliveryNoteNumber == item }
            currentFilter.deliveryNote = deliveryNote
        }
    }
    override val ownershipSelectionListener = object : ItemSelectedListener {
        override fun onItemSelected(item: Any?) {
            val ownershipType = ownershipTypes.find { it.title == item }
            currentFilter.ownershipType =
                if (ownershipType == allOwnershipTypes) null else ownershipType
        }
    }

    override val categorySelectionListener = object : ItemSelectedListener {
        override fun onItemSelected(item: Any?) {
            val category = categories.find { it.title == item }
            currentFilter.category = if (category == allCategories) null else category
            val subcategoriesWithAll = mutableListOf(allSubcategories)
            subcategoriesWithAll.addAll(category?.subcategories ?: emptyList())
            subcategories = subcategoriesWithAll
        }
    }
    override val subcategorySelectionListener = object : ItemSelectedListener {
        override fun onItemSelected(item: Any?) {
            val subcategory = subcategories.find { it.title == item } ?: return
            currentFilter.subcategory = if (subcategory == allSubcategories) null else subcategory
        }
    }


    private fun onLocationSelected(location: Location) {
        currentFilter.location = if (location == allLocations) null else location
        val sublocationsWithAll = mutableListOf(allSubocations)
        sublocationsWithAll.addAll(location.sublocations)
        sublocations = sublocationsWithAll
    }

    fun onCurrentFilterAvailable(filter: Filter) {
        currentFilter = filter
        filter.location?.let { onLocationSelected(it) }
    }

    override fun start() {
        super.start()
        doInBackground {
            performTask<List<Location>, LSError>({ locationsRepository.allLocations() }, {
                val locationsWithAll = mutableListOf(allLocations)
                locationsWithAll.addAll(it ?: emptyList())
                locations = locationsWithAll
            })
            performTask<List<OwnershipType>, LSError>({ ownershipRepository.allOwnershipTypes() }, {
                val ownershipTypesWithALl = mutableListOf(allOwnershipTypes)
                ownershipTypesWithALl.addAll(it ?: emptyList())
                ownershipTypes = ownershipTypesWithALl
            })
            performTask<List<Category>, LSError>({ categoriesRepository.allCategories() }, {
                val categoriesWithAll = mutableListOf(allCategories)
                categoriesWithAll.addAll(it ?: emptyList())
                categories = categoriesWithAll
            })
        }
    }

    fun submit() = viewModelEvent.postValue(Done)

    fun clear() {
        currentFilter = Filter()
        locations = locations
        categories = categories
        ownershipTypes = ownershipTypes
    }

}
