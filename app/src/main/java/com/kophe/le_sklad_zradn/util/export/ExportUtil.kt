package com.kophe.le_sklad_zradn.util.export

import com.kophe.leskladlib.repository.common.Issuance
import com.kophe.leskladlib.repository.common.Item
import java.io.File

interface ExportUtil {
    fun exportItemsFile(items: List<Item>, name: String): File?
    fun exportIssuance(issuance: Issuance, name: String): File?

    fun itemToText(item: Item): String
}
