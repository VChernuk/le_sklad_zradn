package com.kophe.le_sklad_zradn.screens.admin.backups.viewmodel

import androidx.lifecycle.MutableLiveData
import com.kophe.le_sklad_zradn.screens.common.BaseViewModel
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.repository.admin.AdminRepository
import com.kophe.leskladlib.repository.common.LSError
import com.kophe.leskladlib.repository.userprofile.UserProfileRepository
import javax.inject.Inject

class BackupsViewModel @Inject constructor(
    loggingUtil: LoggingUtil,
    private val userProfileRepository: UserProfileRepository,
    private val adminRepository: AdminRepository
) : BaseViewModel(loggingUtil) {
    val refreshingInfo = MutableLiveData<Boolean>()
    val entries = MutableLiveData<List<String>>()
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
        performTask<List<String>, LSError>({ adminRepository.getBackupsList() }, { result ->
            entries.postValue(result)
            refreshingInfo.postValue(false)
        }, {
            refreshingInfo.postValue(false)
        })
    }

    fun restore(item: String) {
        doInBackground {
            performTask<Any, LSError>({ adminRepository.restore(item) })
        }
    }

}
