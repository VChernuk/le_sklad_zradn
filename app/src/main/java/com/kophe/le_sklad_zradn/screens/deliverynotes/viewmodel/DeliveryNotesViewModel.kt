package com.kophe.le_sklad_zradn.screens.deliverynotes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kophe.leskladlib.repository.deliverynotes.DeliveryNotesRepository
import com.kophe.le_sklad_zradn.screens.deliverynotes.view.DeliveryNotesListFragment
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladlib.repository.common.DeliveryNote
import kotlinx.coroutines.launch
import javax.inject.Inject

class DeliveryNotesViewModel @Inject constructor(
    private val repository: DeliveryNotesRepository,
    private val loggingUtil: LoggingUtil
) : ViewModel() {

    private val _deliveryNotes = MutableLiveData<List<DeliveryNote>>()
    val deliveryNotes: LiveData<List<DeliveryNote>> get() = _deliveryNotes

    init {
        loggingUtil.log("${loggingTag()} DeliveryNotesViewModel initialized")
        loadDeliveryNotes()
    }

    private fun loadDeliveryNotes() {
        loggingUtil.log("${loggingTag()} Fetching delivery notes...")
        viewModelScope.launch {
            repository.getDeliveryNotesFlow().collect { notes ->
                loggingUtil.log("${loggingTag()} Received ${notes.size} delivery notes")
                _deliveryNotes.value = notes
            }
        }
    }
    private fun loadDeliveryNotes_old() {
        loggingUtil.log("${loggingTag()} Loading delivery notes...")
        
        viewModelScope.launch {
            val notes = repository.getDeliveryNotes()
            _deliveryNotes.value = notes
            loggingUtil.log("${loggingTag()} Loaded ${notes.size} delivery notes")
        }
    }
}
