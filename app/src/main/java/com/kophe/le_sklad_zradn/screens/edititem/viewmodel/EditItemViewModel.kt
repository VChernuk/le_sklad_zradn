package com.kophe.le_sklad_zradn.screens.edititem.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.kophe.le_sklad_zradn.screens.common.FilterContainerViewModel
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.Done
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.InfoAvailable
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.repository.categories.CategoriesRepository
import com.kophe.leskladlib.repository.common.CommonItem
import com.kophe.leskladlib.repository.common.Issuance
import com.kophe.leskladlib.repository.common.Item
import com.kophe.leskladlib.repository.common.ItemImage
import com.kophe.leskladlib.repository.common.ItemQuantity
import com.kophe.leskladlib.repository.common.ItemSetOptions
import com.kophe.leskladlib.repository.common.LSError
import com.kophe.leskladlib.repository.common.LSError.SimpleError
import com.kophe.leskladlib.repository.common.Sublocation
import com.kophe.leskladlib.repository.common.TaskResult.TaskError
import com.kophe.leskladlib.repository.common.TaskResult.TaskSuccess
import com.kophe.leskladlib.repository.deliverynote.DeliveryNoteRepository
import com.kophe.leskladlib.repository.images.ImagesRepository
import com.kophe.leskladlib.repository.issuance.IssuanceInfoContainer
import com.kophe.leskladlib.repository.issuance.IssuanceRepository
import com.kophe.leskladlib.repository.items.ItemsRepository
import com.kophe.leskladlib.repository.locations.LocationsRepository
import com.kophe.leskladlib.repository.ownership.OwnershipRepository
import com.kophe.leskladlib.repository.userprofile.UserProfileRepository
import com.kophe.leskladuilib.ItemSelectedListener
import javax.inject.Inject

//TODO: sublocation to history
//TODO: handle barcode
//todo: quantity items to history
class EditItemViewModel @Inject constructor(
    loggingUtil: LoggingUtil,
    private val itemsRepository: ItemsRepository,
    private val ownershipRepository: OwnershipRepository,
    private val locationsRepository: LocationsRepository,
    private val categoriesRepository: CategoriesRepository,
    private val imagesRepository: ImagesRepository,
    private val issuanceRepository: IssuanceRepository,
    private val deliveryNoteRepository: DeliveryNoteRepository,
    private val userProfileRepository: UserProfileRepository
) : FilterContainerViewModel(loggingUtil) {

    val saveAvailable = MutableLiveData<Boolean>()
    val images = MutableLiveData<List<ItemImage>>()
    var currentItem = Item() //TODO: think of mutable data here
        set(value) {
            field = value
            subcategoryEntries.postValue(field.category?.subcategories?.map { it.title })
            images.postValue(field.images ?: emptyList())
        }
    override val locationSelectionListener = object : ItemSelectedListener {
        override fun onItemSelected(item: Any?) {
            log("onLocationSelected(...): $item")
            currentItem.location = locations.find { it.title == item }
            val currentSublocations = currentItem.location?.sublocations
            sublocations = if (!currentSublocations.isNullOrEmpty()) {
                val sublocationsWithEmpty = mutableListOf<Sublocation>()
                sublocationsWithEmpty.add(emptySublocation)
                sublocationsWithEmpty.addAll(currentSublocations)
                sublocationsWithEmpty
            } else {
                emptyList()
            }
            updateSaveAvailability()
        }
    }
    override val sublocationSelectionListener = object : ItemSelectedListener {
        override fun onItemSelected(item: Any?) {
            log("onSublocationSelected(...): $item")
            val sublocation = sublocations.find { it.title == item }
            currentItem.sublocation = if (sublocation == emptySublocation) null else sublocation
            updateSaveAvailability()
        }
    }
    override val ownershipSelectionListener = object : ItemSelectedListener {
        override fun onItemSelected(item: Any?) {
            log("onOwnershipSelected(...): $item")
            currentItem.ownershipType = ownershipTypes.find { it.title == item }
            updateSaveAvailability()
        }
    }
    override val deliveryNoteNumberSelectionListener = object : ItemSelectedListener {
        override fun onItemSelected(item: Any?) {
            log("ondeliveryNoteSelected(...): $item")
            currentItem.deliveryNote = deliveryNotes.find { it.deliveryNoteNumber == item }
            updateSaveAvailability()
        }
    }
    override val categorySelectionListener = object : ItemSelectedListener {
        override fun onItemSelected(item: Any?) {
            log("onCategorySelected(...): $item")
            currentItem.category = categories.find { it.title == item }
            subcategories = currentItem.category?.subcategories ?: emptyList()
            updateSaveAvailability()
        }
    }

    override val subcategorySelectionListener = object : ItemSelectedListener {
        override fun onItemSelected(item: Any?) {
            log("onSubcategorySelected(...): $item")
            currentItem.subcategories =
                currentItem.category?.subcategories?.find { it.title == item }?.let { listOf(it) }
                    ?: emptyList()
            updateSaveAvailability()
        }
    }

    override fun start() {
        super.start()
        doInBackground {
            when (val result = locationsRepository.allLocations()) {
                is TaskError -> postError(result.error)
                is TaskSuccess -> locations = result.result ?: emptyList()
            }
        }
        reloadCategories()
        doInBackground {
            when (val result = ownershipRepository.allOwnershipTypes()) {
                is TaskError -> postError(result.error)
                is TaskSuccess -> {
                    val allOwnershipTypes = mutableListOf(emptyOwnershipType)
                    allOwnershipTypes.addAll(result.result ?: emptyList())
                    ownershipTypes = allOwnershipTypes
                }
            }
        }
    }

    private fun reloadCategories() {
        doInBackground {
            when (val result = categoriesRepository.allCategories()) {
                is TaskError -> postError(result.error)
                is TaskSuccess -> {
                    val allCategories = mutableListOf(emptyCategory)
                    allCategories.addAll(result.result ?: emptyList())
                    categories = allCategories
                }
            }
        }
    }

    fun onBarcodeTextChange(text: CharSequence) {
        currentItem.barcode = text.toString()
        updateSaveAvailability()
    }

    fun onQuantityTextChange(text: CharSequence) {
        log("onQuantityTextChange")
        val quantity = "$text".toIntOrNull() ?: run {
            postError(SimpleError("quanity is fucking number"))
            return
        }
        currentItem.quantity =
            currentItem.quantity?.let { ItemQuantity(it.parentId, quantity, it.measurement) }
                ?: ItemQuantity(null, quantity, "")
    }

    fun addSubItems(subItems: Collection<Item>) {
        log("addSubItem: $subItems")
        val setOptions = currentItem.setOptions ?: ItemSetOptions(null, emptySet())
        val mutableSet = setOptions.subItems.toMutableSet()
        mutableSet.addAll(subItems)
        setOptions.subItems = mutableSet
        currentItem.setOptions = setOptions
    }

    fun removeSubItem(subItem: Item) {
        val result = currentItem.setOptions?.subItems?.toMutableSet() ?: return
        result.remove(subItem)
        currentItem.setOptions?.subItems = result
    }

    fun onMeasurementTextChange(text: CharSequence) {
        currentItem.quantity =
            currentItem.quantity?.let { ItemQuantity(it.parentId, it.quantity, text.toString()) }
                ?: ItemQuantity(null, 0, text.toString())
    }

    fun onTitleTextChange(text: CharSequence) {
        currentItem.title = text.toString()
        updateSaveAvailability()
    }

    fun onSerialTextChange(text: CharSequence) {
        currentItem.sn = text.toString()
        updateSaveAvailability()
    }

    fun onIdTextChange(text: CharSequence) {
        currentItem.id = text.toString()
        updateSaveAvailability()
    }

    fun onCommentTextChange(text: CharSequence) {
        currentItem.notes = text.toString()
        updateSaveAvailability()
    }

    fun updateSaveAvailability() {
        log("current info: $currentItem")
        saveAvailable.postValue(
            currentItem.ownershipType != emptyOwnershipType && currentItem.location != null && currentItem.category != null && currentItem.category != emptyCategory && (!currentItem.title.isNullOrEmpty() || !currentItem.id.isNullOrEmpty())
        )
    }

    fun createItemForIssuance() {
        log("createItemForIssuance(...)")
        saveAvailable.postValue(false)
        doInBackground {
            performTask<Item, LSError>({ itemsRepository.createItemAndGetId(currentItem) },
                success = {
                    currentItem = it ?: run {
                        postErrorMessage("no item found")
                        return@performTask
                    }
                    saveAvailable.postValue(true)
                    viewModelEvent.postValue(Done)
                },
                error = { saveAvailable.postValue(true) })
        }
    }

    fun submit() {
        log("createItem(...)")
        saveAvailable.postValue(false)
        val itemSetOptions = currentItem.setOptions
        val itemsToIssue =
            itemSetOptions?.subItems?.filter { it.location != currentItem.location || it.sublocation != currentItem.sublocation || it.responsibleUnit != currentItem.responsibleUnit || it.quantity?.quantity != null }

        if (itemSetOptions?.subItems.isNullOrEmpty() || itemsToIssue.isNullOrEmpty()) {
            createItem()
            return
        }
        if (itemsToIssue.isNotEmpty()) {
            viewModelEvent.postValue(InfoAvailable(itemsToIssue))
            saveAvailable.postValue(true)
        }
    }

    private fun createItem() {
        doInBackground {
            performTask<Any?, LSError>({ itemsRepository.createItem(currentItem, true, true) },
                success = {
                    saveAvailable.postValue(true)
                    viewModelEvent.postValue(Done)
                },
                error = { saveAvailable.postValue(true) })
        }
    }

    internal fun createIssuanceAndCreateItem(itemsToIssue: List<Item>) {
        doInBackground {
            performTask<Any?, LSError>({
                issuanceRepository.createIssuance(
                    itemsToIssue, IssuanceInfoContainer(
                        userProfileRepository.user()?.login!!,
                        userProfileRepository.user()?.login!!,
                        currentItem.location!!,
                        currentItem.sublocation,
                        currentItem.responsibleUnit!!,
                        "Видача для створення комплекту"
                    ),//TODO: check
                    true
                )
            }, success = {
                createItem()
            })
        }
    }

    fun deleteItem() {
        log("deleteItem()")
        doInBackground {
            performTask<Any?, LSError>({ itemsRepository.deleteItem(currentItem) }, success = {
                viewModelEvent.postValue(Done)
            })
        }
    }

    fun saveChanges() = doInBackground { saveItemChanges() }

    private suspend fun saveItemChanges() {
        log("saveItemChanges(...)")
        saveAvailable.postValue(false)
        performTask<Any?, LSError>({ itemsRepository.editItem(currentItem) }, success = {
            saveAvailable.postValue(true)
            viewModelEvent.postValue(Done)
        }, error = { saveAvailable.postValue(true) })
    }

    fun getIssuanceInfo(historyEntry: CommonItem) {
        doInBackground {
            performTask<Issuance, LSError>({ issuanceRepository.findIssuanceById(historyEntry.firestoreId) },
                success = { issuance ->
                    log("getIssuanceInfo(...) historyEntry=$historyEntry was found")
                    viewModelEvent.postValue(InfoAvailable(issuance))
                })
        }
    }

    fun uploadImage(uri: Uri) {
        doInBackground {
            performTask<ItemImage, LSError>({ imagesRepository.uploadImage(uri) }, {
                it?.let {
                    currentItem.images?.add(it)
                    images.postValue(currentItem.images ?: return@let)
                } ?: run { postError(SimpleError("Чуда не відбулось")) }
            })
        }
    }

    fun switchSet(set: Boolean) {
        if (!set) {
            reloadCategories()
            return
        }
        doInBackground {
            val setsCategory = categoriesRepository.getCategory("sets") ?: run {
                postErrorMessage("Чуда не відбулось: set category not found")
                return@doInBackground
            }
            categories = listOf(setsCategory)
            subcategories = emptyList()
            currentItem.subcategories = emptyList()
            currentItem.category = setsCategory
            currentItem.quantity = null
        }
    }

}
