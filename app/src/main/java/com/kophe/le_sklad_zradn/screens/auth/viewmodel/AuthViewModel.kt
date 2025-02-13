package com.kophe.le_sklad_zradn.screens.auth.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import com.kophe.leskladlib.repository.userprofile.UserProfileRepository
import com.kophe.le_sklad_zradn.screens.common.BaseViewModel
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.Main
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.repository.common.LSError
import com.kophe.leskladlib.repository.common.TaskResult.TaskError
import javax.inject.Inject

class AuthViewModel @Inject constructor(
    private val authRepository: UserProfileRepository, loggingUtil: LoggingUtil
) : BaseViewModel(loggingUtil) {

    val submitAvailable = MutableLiveData<Boolean>()
    val restoredLogin = MutableLiveData<String>()
    private var username: String? = null
    private var email: String? = null
    private var password: String? = null

    init {
        submitAvailable.postValue(false)
    }

    fun onLoginTextChange(text: CharSequence) {
        log("onLoginTextChange")
        username = text.toString()
        updateLoginAvailability()
    }

    fun onEmailTextChange(text: CharSequence) {
        log("onEmailTextChange")
        email = text.toString()
        updateLoginAvailability()
    }

    fun onPasswordTextChange(text: CharSequence) {
        log("onPasswordTextChange")
        password = text.toString()
        updateLoginAvailability()
    }

    private fun updateLoginAvailability() {
        submitAvailable.value =
            !(username.isNullOrEmpty() || email.isNullOrEmpty() || password.isNullOrEmpty())
    }

    fun submit() {
        log("submit")
        doInBackground { startAuth() }
    }

    @VisibleForTesting
    suspend fun authWithDB() {
        log("auth with db")
        makeAuthRequest()
    }

    @VisibleForTesting
    suspend fun startAuth() {
        log("start auth")
        authWithDB()
    }

    private suspend fun makeAuthRequest() {
        performTask<Unit, LSError>({
            authRepository.auth(
                username ?: return@performTask TaskError(LSError.SimpleError("no username")),
                email ?: return@performTask TaskError(LSError.SimpleError("no email")),
                password ?: return@performTask TaskError(LSError.SimpleError("no password"))
            )
        }, success = { viewModelEvent.postValue(Main) })
    }

}
