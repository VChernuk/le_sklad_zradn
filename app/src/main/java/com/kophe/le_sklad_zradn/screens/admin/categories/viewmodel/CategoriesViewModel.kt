package com.kophe.le_sklad_zradn.screens.admin.categories.viewmodel

import androidx.lifecycle.MutableLiveData
import com.kophe.le_sklad_zradn.screens.common.BaseViewModel
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.repository.categories.CategoriesRepository
import com.kophe.leskladlib.repository.common.Category
import com.kophe.leskladlib.repository.common.LSError
import com.kophe.leskladlib.repository.userprofile.UserProfileRepository
import javax.inject.Inject

class CategoriesViewModel @Inject constructor(
    loggingUtil: LoggingUtil,
    private val userProfileRepository: UserProfileRepository,
    private val locationsRepository: CategoriesRepository
) : BaseViewModel(loggingUtil) {
    val refreshingInfo = MutableLiveData<Boolean>()
    val entries = MutableLiveData<List<Category>>()
    val writeAvailable = MutableLiveData<Boolean>()
    override fun start() {
        super.start()
        refreshInfo()
    }

    fun refreshInfo() {
        log("refresh info manually")
        doInBackground {
            val write = userProfileRepository.checkWriteAccess()
            writeAvailable.postValue(write)
        }
        entries.postValue(emptyList())
        doInBackground { startRefresh() }
    }

    private suspend fun startRefresh() {
        log("startRefresh()")
        refreshingInfo.postValue(true)
        performTask<List<Category>, LSError>({ locationsRepository.allCategories(true) },
            { result ->
                entries.postValue(result?.sortedBy { it.title })
                refreshingInfo.postValue(false)
            },
            {
                refreshingInfo.postValue(false)
            })
    }

}
