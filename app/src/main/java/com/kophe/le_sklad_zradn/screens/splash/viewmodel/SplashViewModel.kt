package com.kophe.le_sklad_zradn.screens.splash.viewmodel

import androidx.annotation.VisibleForTesting
import com.kophe.le_sklad_zradn.screens.common.BaseViewModel
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.Auth
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.Main
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.repository.common.LSError
import com.kophe.leskladlib.repository.items.ItemsRepository
import com.kophe.leskladlib.repository.userprofile.UserProfileRepository
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    loggingUtil: LoggingUtil,
    val repository: UserProfileRepository,
    private val itemsRepository: ItemsRepository
) : BaseViewModel(loggingUtil) {

    override fun start() {
        log("start()")
        doInBackground { checkCurrentStateAndNavigate() }
    }

    @VisibleForTesting
    internal suspend fun checkCurrentStateAndNavigate() {
        log("app (re)start: checkCurrentStateAndNavigate()")
        performTask<Unit, LSError>(task = { repository.tryLoginWithCachedUser() }, success = {
            doInBackground { itemsRepository.precacheValues(false) }
            viewModelEvent.postValue(Main)
        }, error = { viewModelEvent.postValue(Auth) })
    }

}
