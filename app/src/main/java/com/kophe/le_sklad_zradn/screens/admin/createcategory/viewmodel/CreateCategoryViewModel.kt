package com.kophe.le_sklad_zradn.screens.admin.createcategory.viewmodel

import androidx.lifecycle.MutableLiveData
import com.kophe.le_sklad_zradn.screens.common.BaseViewModel
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.repository.categories.CategoriesRepository
import com.kophe.leskladlib.repository.common.Category
import com.kophe.leskladlib.repository.common.LSError
import com.kophe.leskladlib.repository.common.Subcategory
import com.kophe.leskladlib.repository.userprofile.UserProfileRepository
import javax.inject.Inject

class CreateCategoryViewModel @Inject constructor(
    loggingUtil: LoggingUtil,
    private val repository: CategoriesRepository,
    private val userProfileRepository: UserProfileRepository
) : BaseViewModel(loggingUtil) {

    val submitAvailable = MutableLiveData<Boolean>()
    val subItems = mutableSetOf<Subcategory>()
    var category: Category? = null
        set(value) {
            field = value
            title = category?.title ?: return
            subItems.addAll(field?.subcategories ?: return)
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
            repository.updateCategory(
                Category(title, subItems.toList(), category?.id ?: "", category?.weight ?: 700)
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
