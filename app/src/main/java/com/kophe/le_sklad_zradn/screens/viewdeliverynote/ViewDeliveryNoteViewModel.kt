package com.kophe.le_sklad_zradn.screens.viewdeliverynote

import androidx.lifecycle.MutableLiveData
import com.kophe.le_sklad_zradn.screens.common.BaseViewModel
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.repository.common.Item
import com.kophe.leskladlib.repository.common.LSError
import com.kophe.leskladlib.repository.items.ItemsRepository
import com.kophe.leskladlib.repository.userprofile.UserProfileRepository
import javax.inject.Inject

class ViewDeliveryNoteViewModel @Inject constructor(
    loggingUtil: LoggingUtil,
    private val userProfileRepository: UserProfileRepository,
    private val itemsRepository: ItemsRepository
) : BaseViewModel(loggingUtil) {
    val writeAvailable = MutableLiveData<Boolean>()
    val selectedItems = mutableSetOf<Item>()

    override fun start() {
        log("start(...)")
        doInBackground {
            val available = userProfileRepository.checkWriteAccess()
            writeAvailable.postValue(available)
        }
        super.start()
    }

    fun findItem(id: String) {
        doInBackground {
            performTask<Item, LSError>({
                itemsRepository.findByFirebaseId(id)
            }, success = {
                viewModelEvent.postValue(ViewModelEvent.InfoAvailable(it ?: return@performTask))
            })
        }
    }

    fun submit() {
        doInBackground {
            performTask<List<Item>, LSError>({
                val ids = selectedItems.mapNotNull { it.firestoreId }.toList()
                itemsRepository.findByFirebaseId(ids = ids)
            }, { viewModelEvent.postValue(ViewModelEvent.InfoAvailable(it ?: return@performTask)) })
        }
    }

}
