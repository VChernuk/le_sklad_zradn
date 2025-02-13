package com.kophe.le_sklad_zradn.screens.navdrawer

import androidx.lifecycle.MutableLiveData
import com.kophe.le_sklad_zradn.screens.common.BaseViewModel
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.Admin
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.Done
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.Support
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.repository.userprofile.UserProfileRepository
import javax.inject.Inject

class HomeNavigationViewModel @Inject constructor(
    val repository: UserProfileRepository, loggingUtil: LoggingUtil
) : BaseViewModel(loggingUtil) {

    val username = MutableLiveData<String>()
    val writeAvailable = MutableLiveData<Boolean>()

    override fun start() {
        super.start()
        doInBackground { username.postValue(repository.user()?.login) }
        doInBackground { checkWriteAccess() }
    }

    fun logout() {
        doInBackground {
            repository.clearData()
            viewModelEvent.postValue(Done)
        }
    }

    fun support() {
        viewModelEvent.postValue(Support)
    }

    private suspend fun checkWriteAccess() {
        val write = repository.checkWriteAccess()
        writeAvailable.postValue(write)
    }

    fun admin() {
        viewModelEvent.postValue(Admin)
    }

}
