package com.kophe.le_sklad_zradn.screens.deliverynotes.viewmodel

//import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.kophe.leskladlib.repository.deliverynotes.DeliveryNotesRepository
//import com.kophe.leskladlib.models.CommonItem
//import com.kophe.leskladlib.models.DeliveryNote
//import com.kophe.leskladlib.models.Location
//import com.kophe.le_sklad_zradn.utils.LoggingUtil
//import com.kophe.le_sklad_zradn.utils.loggingTag
//import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
//import javax.inject.Inject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kophe.leskladlib.repository.deliverynotes.DeliveryNotesRepository
import com.kophe.le_sklad_zradn.screens.deliverynotes.view.DeliveryNotesListFragment
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladlib.repository.common.CommonItem
import com.kophe.leskladlib.repository.common.DeliveryNote
import com.kophe.leskladlib.repository.locations.LocationsRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

//@HiltViewModel
class CreateDeliveryNoteViewModel @Inject constructor(
    private val repository: DeliveryNotesRepository,
    private val locationsRepository: LocationsRepository,
    private val loggingUtil: LoggingUtil
) : ViewModel() {

    fun createDeliveryNote(
        number: String,
        date: String,
        toLocationId: String,
        toSublocationId: String,
        responsiblePerson: String,
        items: List<CommonItem>
    ) {
        viewModelScope.launch {
            try {
                val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
                val location = locationsRepository.getLocation(toLocationId)
                val sublocation = locationsRepository.getSublocation(toSublocationId)

                if (parsedDate != null && location != null) {
                    val deliveryNote = DeliveryNote(
                        number = number,
                        date = parsedDate,
                        location = location,
                        sublocation = sublocation,
                        responsiblePerson = responsiblePerson,
                        items = items
                    )
                    repository.addDeliveryNote(deliveryNote)
                    loggingUtil.log("${loggingTag()} Delivery Note created successfully")
                } else {
                    loggingUtil.log("${loggingTag()} Invalid date or location")
                }
            } catch (e: Exception) {
                loggingUtil.log("${loggingTag()} Error creating Delivery Note: ${e.message}")
            }
        }
    }
}
