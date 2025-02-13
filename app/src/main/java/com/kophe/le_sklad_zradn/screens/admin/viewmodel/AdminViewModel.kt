package com.kophe.le_sklad_zradn.screens.admin.viewmodel

import com.kophe.le_sklad_zradn.screens.common.BaseViewModel
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.repository.admin.AdminRepository
import com.kophe.leskladlib.repository.common.LSError
import javax.inject.Inject

class AdminViewModel @Inject constructor(
    loggingUtil: LoggingUtil, private val adminRepository: AdminRepository
) : BaseViewModel(loggingUtil) {

    fun setupOwnership() {
        doInBackground {
            performTask<Any, LSError>({ adminRepository.setAllOwnerTypesToUnknown() })
        }
    }

    fun migrateIssuance() {
        doInBackground {
            performTask<Any, LSError>({ adminRepository.migrateIssuanceToTimestamp() })
        }
    }

    fun backup() {
        doInBackground {
            performTask<Any, LSError>({ adminRepository.createBackupFile() })
        }
    }

}
