package com.kophe.le_sklad_zradn.screens.items.viewmodel

import androidx.lifecycle.MutableLiveData
import com.kophe.le_sklad_zradn.screens.common.BaseViewModel
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.repository.common.Filter
import com.kophe.leskladlib.repository.common.Item
import com.kophe.leskladlib.repository.common.LSError
import com.kophe.leskladlib.repository.common.LSError.SimpleError
import com.kophe.leskladlib.repository.common.TaskResult.TaskError
import com.kophe.leskladlib.repository.items.ItemsRepository
import com.kophe.leskladlib.repository.userprofile.UserProfileRepository
import javax.inject.Inject

class ItemsViewModel @Inject constructor(
    loggingUtil: LoggingUtil,
    val itemsRepository: ItemsRepository,
    private val userProfileRepository: UserProfileRepository
) : BaseViewModel(loggingUtil) {

    val writeAvailable = MutableLiveData<Boolean>()

    private var allItems = emptyList<Item>()
    private var currentItems = emptyList<Item>()
        set(value) {
            field = value
            entries.postValue(value)
            if (searchQuery.isNotEmpty()) search()
        }
    var searchQuery = ""
        set(value) {
            field = value
            search()
        }
    val refreshingInfo = MutableLiveData<Boolean>()
    val entries = MutableLiveData<List<Item>>()
    var currentFilter: Filter? = null
        set(value) {
            field = value
            currentItems = emptyList()
            refreshingInfo.postValue(true)
            doInBackground { filterItems() }
        }

    override fun start() {
        if (!started) getCachedInfo()
        super.start()
    }

    private fun getCachedInfo() {
        log("getCachedInfo")
        entries.postValue(emptyList())
        doInBackground {
            tryGetCachedItems()
        }
    }

    private suspend fun tryGetCachedItems() {
        refreshingInfo.postValue(true)
        doInBackground { checkWriteAccess() }
        performTask<List<Item>, LSError>({ itemsRepository.tryGetCachedItems() },
            success = { result ->
                refreshingInfo.postValue(false)
                allItems = result ?: emptyList()
                currentItems =
                    allItems.filter { it.setOptions?.parentItem?.firestoreId.isNullOrEmpty() }
            },
            { refreshingInfo.postValue(false) })
    }

    fun refreshInfo() {
        log("refresh info manually")
        entries.postValue(emptyList())
        doInBackground { checkWriteAccess() }
        doInBackground {
            if (currentFilter == null || currentFilter?.isClear() == true) startRefresh() else filterItems()
        }
    }

    private suspend fun checkWriteAccess() {
        val write = userProfileRepository.checkWriteAccess()
        writeAvailable.postValue(write)
    }

    private suspend fun filterItems() {
        performTask<List<Item>, LSError>({
            itemsRepository.filterItems(
                currentFilter ?: return@performTask TaskError(SimpleError("Фільтр не задано"))
            )
        }, { result ->
            currentItems = result ?: emptyList()
            refreshingInfo.postValue(false)
        }, { refreshingInfo.postValue(false) })
    }

    private suspend fun startRefresh() {
        log("startRefresh()")
        refreshingInfo.postValue(true)
        performTask<List<Item>, LSError>({ itemsRepository.allItems() }, { result ->
            refreshingInfo.postValue(false)
            allItems = result ?: emptyList()
            currentItems =
                allItems.filter { it.setOptions?.parentItem?.firestoreId.isNullOrEmpty() }
        }, { refreshingInfo.postValue(false) })
    }

    private fun search() {
        doInBackground {
            if (searchQuery.isEmpty()) {
                entries.postValue(currentItems)
                return@doInBackground
            }
            refreshingInfo.postValue(true)
            val results = mutableSetOf<Item>()
            results.addAll(currentItems.filter {
                it.title?.contains(searchQuery, ignoreCase = true) ?: false || it.id?.contains(
                    searchQuery, ignoreCase = true
                ) ?: false || it.notes?.contains(
                    searchQuery, ignoreCase = true
                ) ?: false || it.barcode?.contains(
                    searchQuery, ignoreCase = true
                ) ?: false || it.sn?.contains(searchQuery, ignoreCase = true) ?: false
            })
            entries.postValue(results.toList())
            refreshingInfo.postValue(false)
        }
    }

}
