package com.kophe.le_sklad_zradn.screens.common

import androidx.lifecycle.MutableLiveData
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.repository.common.Category
import com.kophe.leskladlib.repository.common.Location
import com.kophe.leskladlib.repository.common.OwnershipType
import com.kophe.leskladlib.repository.common.DeliveryNote
import com.kophe.leskladlib.repository.common.Subcategory
import com.kophe.leskladlib.repository.common.Sublocation
import com.kophe.leskladuilib.ItemSelectedListener

abstract class FilterContainerViewModel(loggingUtil: LoggingUtil) : BaseViewModel(loggingUtil) {
    abstract val locationSelectionListener: ItemSelectedListener
    abstract val sublocationSelectionListener: ItemSelectedListener
    abstract val ownershipSelectionListener: ItemSelectedListener
    abstract val deliveryNoteNumberSelectionListener: ItemSelectedListener
    abstract val categorySelectionListener: ItemSelectedListener
    abstract val subcategorySelectionListener: ItemSelectedListener

    val locationEntries = MutableLiveData<List<String>>()
    val sublocationEntries = MutableLiveData<List<String>>()
    val categoryEntries = MutableLiveData<List<String>>()
    val subcategoryEntries = MutableLiveData<List<String>>()
    val ownershipTypeEntries = MutableLiveData<List<String>>()
    val deliveryNoteEntries = MutableLiveData<List<String>>()

    protected var ownershipTypes = emptyList<OwnershipType>()
        set(value) {
            field = value
            ownershipTypeEntries.postValue(value.map { it.title })
        }
    protected var deliveryNotes = emptyList<DeliveryNote>()
        set(value) {
            field = value
            deliveryNoteEntries.postValue(value.map { it.deliveryNoteNumber ?: "-" })
        }
    protected var locations = emptyList<Location>()
        set(value) {
            field = value
            locationEntries.postValue(value.map { it.title })
        }

    protected var sublocations = emptyList<Sublocation>()
        set(value) {
            field = value
            sublocationEntries.postValue(value.map { it.title })
        }
    protected var categories = emptyList<Category>()
        set(value) {
            field = value.sorted()
            categoryEntries.postValue(field.map { it.title })
        }
    protected var subcategories = emptyList<Subcategory>()
        set(value) {
            field = value.sorted()
            subcategoryEntries.postValue(field.map { it.title })
        }
    protected val emptyCategory = Category("-", emptyList(), "", 1)
    protected val emptySublocation = Sublocation("-", "")
    protected val emptyOwnershipType = OwnershipType("-", "")
    //protected val emptyDeliveryNote = OwnershipType("-", "")

    protected val allLocations = Location("Всі", "", emptyList())
    protected val allSubocations = Sublocation("Всі", "")
    protected val allOwnershipTypes = OwnershipType("Всі", "")
    //protected val allDeliveryNotes = DeliveryNote("Всі", "")
    protected val allCategories = Category("Всі", emptyList(), "", -1)
    protected val allSubcategories = Subcategory("Всі", "", -1)
}
