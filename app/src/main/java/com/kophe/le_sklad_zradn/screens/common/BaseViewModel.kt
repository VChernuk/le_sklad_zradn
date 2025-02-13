package com.kophe.le_sklad_zradn.screens.common

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kophe.leskladlib.logging.Logger
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.repository.common.LSError
import com.kophe.leskladlib.repository.common.LSError.SimpleError
import com.kophe.leskladlib.repository.common.TaskResult
import com.kophe.leskladlib.repository.common.TaskResult.TaskError
import com.kophe.leskladlib.repository.common.TaskResult.TaskSuccess
import com.kophe.leskladlib.repository.common.TaskStatus
import com.kophe.leskladlib.repository.common.TaskStatus.StatusFailed
import com.kophe.leskladlib.repository.common.TaskStatus.StatusFinished
import com.kophe.leskladlib.repository.common.TaskStatus.StatusInProgress
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

open class BaseViewModel(override val loggingUtil: LoggingUtil) : ViewModel(), Logger {

    val viewModelEvent = MutableLiveData<ViewModelEvent>()
    val requestStatus = MutableLiveData<TaskStatus>()
    var started = false

    open fun start() {
        started = true
    }

    protected fun postErrorMessage(errorMsg: String? = null) = postError(SimpleError(errorMsg))

    protected fun postError(error: LSError? = null) {
        log("error: ${error?.message}")
        requestStatus.postValue(StatusFailed(error))
    }

    protected suspend fun <T, R> performTask(
        task: suspend () -> TaskResult<T, *>,
        success: ((T?) -> Unit)? = null,
        error: ((LSError?) -> Unit)? = null
    ) {
        requestStatus.postValue(StatusInProgress)
        when (val result = task.invoke()) {
            is TaskSuccess<T, *> -> {
                requestStatus.postValue(StatusFinished)
                success?.invoke(result.result)
            }

            is TaskError<*, *> -> {
                postError(result.error)
                error?.invoke(result.error)
            }
        }
    }

    protected fun doInBackground(task: suspend () -> Unit) =
        viewModelScope.launch(IO) { task.invoke() }

}
