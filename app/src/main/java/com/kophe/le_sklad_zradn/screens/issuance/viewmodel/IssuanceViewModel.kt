package com.kophe.le_sklad_zradn.screens.issuance.viewmodel

import androidx.lifecycle.MutableLiveData
import com.kophe.le_sklad_zradn.screens.common.BaseViewModel
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.InfoAvailable
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.repository.common.CommonItem
import com.kophe.leskladlib.repository.common.Issuance
import com.kophe.leskladlib.repository.common.Item
import com.kophe.leskladlib.repository.common.LSError
import com.kophe.leskladlib.repository.issuance.IssuanceRepository
import com.kophe.leskladlib.repository.items.ItemsRepository
import com.kophe.leskladlib.repository.userprofile.UserProfileRepository
import javax.inject.Inject

class IssuanceViewModel @Inject constructor(
    loggingUtil: LoggingUtil,
    private val repository: IssuanceRepository,
    private val userProfileRepository: UserProfileRepository,
    private val itemsRepository: ItemsRepository
) : BaseViewModel(loggingUtil) {
    val refreshingInfo = MutableLiveData<Boolean>()
    val entries = MutableLiveData<List<Issuance>>()
    val writeAvailable = MutableLiveData<Boolean>()
    override fun start() {
        if (!started) {
            entries.postValue(emptyList())
            doInBackground { startRefresh(false) }
            precacheItems()
        }
        super.start()
    }

    fun precacheItems() {
        log("precacheItems")
        doInBackground {
            itemsRepository.precacheValues(true)
        }
        doInBackground {
            val write = userProfileRepository.checkWriteAccess()
            writeAvailable.postValue(write)
        }
    }

    fun refreshInfo() {
        log("refresh info manually")
        entries.postValue(emptyList())
        doInBackground { startRefresh(true) }
    }

    private suspend fun startRefresh(forceRefresh: Boolean) {
        log("startRefresh()")
        refreshingInfo.postValue(true)
        performTask<List<Issuance>, LSError>({ repository.issuanceList(forceRefresh) }, { result ->
            entries.postValue(result)
            refreshingInfo.postValue(false)
        }, {
            refreshingInfo.postValue(false)
        })
    }

    fun findItem(issuanceItem: CommonItem) {
        doInBackground {
            performTask<Item, LSError>({
                itemsRepository.findByFirebaseId(issuanceItem.firestoreId ?: "")
            }, success = {
                viewModelEvent.postValue(InfoAvailable(it ?: return@performTask))
            })
        }
    }
}
