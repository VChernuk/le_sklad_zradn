package com.kophe.le_sklad_zradn.screens.admin.createlocation.viewmodel

import androidx.lifecycle.MutableLiveData
import com.kophe.le_sklad_zradn.screens.common.BaseViewModel
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.repository.common.LSError
import com.kophe.leskladlib.repository.common.Location
import com.kophe.leskladlib.repository.common.Sublocation
import com.kophe.leskladlib.repository.locations.LocationsRepository
import com.kophe.leskladlib.repository.userprofile.UserProfileRepository
import javax.inject.Inject

class CreateLocationViewModel @Inject constructor(
    loggingUtil: LoggingUtil,
    private val locationsRepository: LocationsRepository,
    private val userProfileRepository: UserProfileRepository
) : BaseViewModel(loggingUtil) {

    val submitAvailable = MutableLiveData<Boolean>()
    val sublocationItems = mutableSetOf<Sublocation>()
    var location: Location? = null
        set(value) {
            field = value
            title = location?.title ?: return
            sublocationItems.addAll(field?.sublocations ?: return)
        }
    private var title = ""

    init {
        submitAvailable.postValue(false)
    }

    override fun start() {
        log("start(...)")
        super.start()
    }

    fun onCommentTextChange(text: CharSequence) {
        title = text.toString()
        updateSubmitAvailability()
    }

    fun submit() {
        log("submit(...)")
        doInBackground { submitItem() }
    }

    private suspend fun submitItem() {
        log("submitItem(...)")
        submitAvailable.postValue(false)
        performTask<Any?, LSError>({
            locationsRepository.updateLocation(
                Location(title, location?.id ?: "", sublocationItems.toList())
            )
        }, success = {
            submitAvailable.postValue(true)
            viewModelEvent.postValue(ViewModelEvent.Done)
        }, error = {
            submitAvailable.postValue(true)
        })
    }

    private fun updateSubmitAvailability() {
        submitAvailable.postValue(title.isNotEmpty())
    }

    suspend fun checkWriteAccess(): Boolean = userProfileRepository.checkWriteAccess()

}
