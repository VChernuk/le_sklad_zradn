package com.kophe.le_sklad_zradn.screens.createdeliverynote.viewmodel

import androidx.lifecycle.MutableLiveData
import com.kophe.le_sklad_zradn.screens.common.BaseViewModel
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.Done
import com.kophe.leskladlib.datasource.currentusersource.CurrentUserSource
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.repository.common.Item
import com.kophe.leskladlib.repository.common.LSError
import com.kophe.leskladlib.repository.common.Location
import com.kophe.leskladlib.repository.common.ResponsibleUnit
import com.kophe.leskladlib.repository.common.Sublocation
import com.kophe.leskladlib.repository.common.TaskResult.TaskError
import com.kophe.leskladlib.repository.common.TaskResult.TaskSuccess
import com.kophe.leskladlib.repository.common.TaskStatus.StatusFinished
import com.kophe.leskladlib.repository.common.TaskStatus.StatusInProgress
import com.kophe.leskladlib.repository.deliverynote.DeliveryNoteInfoContainer
import com.kophe.leskladlib.repository.deliverynote.DeliveryNoteRepository
import com.kophe.leskladlib.repository.locations.LocationsRepository
import com.kophe.leskladuilib.ItemSelectedListener
import javax.inject.Inject

class CreateDeliveryNoteViewModel @Inject constructor(
    loggingUtil: LoggingUtil,
    private val locationsRepository: LocationsRepository,
    private val deliverynoteRepository: DeliveryNoteRepository,
    private val currentUserSource: CurrentUserSource
) : BaseViewModel(loggingUtil) {

    val submitAvailable = MutableLiveData<Boolean>()
    val selectedItems = mutableSetOf<Item>()
    val originalQuantities = mutableMapOf<String, Int>()
    private var notes = ""
    private var receiver = ""
    private var deliverynotedate = ""
    private var deliverynotenumber = ""
    private var deliveryNotePIB = ""


    //TODO: save location between launches
    val locationEntries = MutableLiveData<List<String>>()
    val sublocationEntries = MutableLiveData<List<String>>()
    var selectedLocation: Location? = null
    var selectedSublocation: Sublocation? = null

    private var locations = emptyList<Location>()
        set(value) {
            field = value
            locationEntries.postValue(value.map { it.title })
        }
    private var sublocations = emptyList<Sublocation>()
        set(value) {
            field = value
            sublocationEntries.postValue(value.map { it.title })
        }

    val locationSelectionListener = object : ItemSelectedListener {
        override fun onItemSelected(item: Any?) {
            selectedLocation = locations.find { it.title == item }
            sublocations = selectedLocation?.sublocations ?: emptyList()
            val currentSublocations = selectedLocation?.sublocations
            sublocations = if (!currentSublocations.isNullOrEmpty()) {
                val sublocationsWithEmpty = mutableListOf<Sublocation>()
                sublocationsWithEmpty.add(emptySublocation)
                sublocationsWithEmpty.addAll(currentSublocations)
                sublocationsWithEmpty
            } else {
                selectedSublocation = emptySublocation
                emptyList()
            }
        }
    }
    val sublocationSelectionListener = object : ItemSelectedListener {
        override fun onItemSelected(item: Any?) {
            selectedSublocation = sublocations.find { it.title == item }
        }
    }
    private val emptySublocation = Sublocation("-", "")

    init {
        submitAvailable.postValue(false)
    }

    override fun start() {
        log("start(...)")
        if (started) return
        requestStatus.postValue(StatusInProgress)
        doInBackground {
            when (val result = locationsRepository.allLocations()) {
                is TaskError -> postError()
                is TaskSuccess -> locations = result.result ?: emptyList()
            }
            requestStatus.postValue(StatusFinished)
        }
        super.start()
    }

    fun onDeliveryNoteNumberTextChange(text: CharSequence) {
        deliverynotenumber = text.toString()
    }

    fun onDeliveryNoteDateTextChange(text: CharSequence) {
        deliverynotedate = text.toString()
    }

    fun onCommentTextChange(text: CharSequence) {
        notes = text.toString()
    }

    fun onReceiverTextChange(text: CharSequence) {
        receiver = text.toString()
        updateSubmitAvailability()
    }

    fun submit() {
        log("submit(...)")
        doInBackground { submitItem() }
    }

    private suspend fun submitItem() {
        log("submitItem(...)")
        val location = selectedLocation ?: run {
            postErrorMessage("no location chosen")
            return
        }
        submitAvailable.postValue(false)
        val from = currentUserSource.currentUser()?.login ?: run {
            postErrorMessage("no user found")
            return
        }

        performTask<Any?, LSError>({
            deliverynoteRepository.createDeliveryNote(
                items = selectedItems.toMutableList(), DeliveryNoteInfoContainer(
                    deliverynotenumber,
                    deliverynotedate,
                    deliveryNotePIB,
                    from,
                    receiver,
                    location,
                    sublocation = if (selectedSublocation == emptySublocation) null else selectedSublocation,
                    responsibleUnit = null,
                    notes
                ), false
            )
        }, success = {
            submitAvailable.postValue(true)
            viewModelEvent.postValue(Done)
        }, error = {
            submitAvailable.postValue(true)
        })
    }

    fun updateSubmitAvailability() {
        //TODO: count quantity
        submitAvailable.postValue(selectedItems.isNotEmpty() && receiver.isNotEmpty())
    }

}
