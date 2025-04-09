package com.kophe.le_sklad_zradn.util.export

import android.content.Context
import com.aspose.cells.Cell
import com.aspose.cells.Font
import com.aspose.cells.Style
import com.aspose.cells.Workbook
import com.aspose.cells.Worksheet
import com.kophe.leskladlib.repository.common.Issuance
import com.kophe.leskladlib.repository.common.DeliveryNote
import com.kophe.leskladlib.repository.common.Item
import java.io.File
import java.lang.ref.WeakReference

class DefaultExportUtil(context: Context) : ExportUtil {

    private val appContext = WeakReference(context)

    override fun exportIssuance(issuance: Issuance, name: String): File? {
        val workbook = Workbook()
        setupIssuanceTitlesRow(workbook.worksheets[0])
        issuance.items.forEachIndexed { index, issuanceItem ->
            workbook.worksheets[0].cells["A${index + 2}"].putValue(issuanceItem.title)
            workbook.worksheets[0].cells["B${index + 2}"].putValue(issuance.from)
            workbook.worksheets[0].cells["C${index + 2}"].putValue(issuance.to)
            workbook.worksheets[0].cells["D${index + 2}"].putValue(issuance.date)
            workbook.worksheets[0].cells["E${index + 2}"].putValue(issuance.notes)
        }
        val file = excelFile(name)
        workbook.save(file?.path)
        return file
    }

    override fun exportDeliveryNote(deliverynote: DeliveryNote, name: String): File? {
        val workbook = Workbook()
        setupDeliveryNoteTitlesRow(workbook.worksheets[0])
        deliverynote.items.forEachIndexed { index, deliverynoteItem ->
            workbook.worksheets[0].cells["A${index + 2}"].putValue(deliverynoteItem.title)
            workbook.worksheets[0].cells["B${index + 2}"].putValue(deliverynote.from)
            workbook.worksheets[0].cells["C${index + 2}"].putValue(deliverynote.to)
            workbook.worksheets[0].cells["D${index + 2}"].putValue(deliverynote.date)
            workbook.worksheets[0].cells["E${index + 2}"].putValue(deliverynote.notes)
        }
        val file = excelFile(name)
        workbook.save(file?.path)
        return file
    }

    override fun itemToText(item: Item): String =
        "${item.title ?: "-"}\n" + "ID: ${item.id ?: "-"}\n" + "Barcode: ${item.barcode ?: "-"}\n " + "Категорія: ${item.category?.title ?: "-"}\n " + "Підкатегорія: ${item.subcategories.firstOrNull()?.title ?: "-"}\n " + "Локація: ${item.location?.title ?: "-"}\n " + "Власність: ${item.ownershipType?.title ?: "-"}\n ".replace(
            "[", ""
        ).replace("]", "").replace(",", "") + "\nКоментар: ${item.notes ?: "-"}"

    private fun setupIssuanceTitlesRow(worksheet: Worksheet) {
        val a1 = worksheet.cells["A1"]
        a1.putValue("Майно")
        setBold(a1)

        val b1 = worksheet.cells["B1"]
        b1.putValue("Видав")
        setBold(b1)

        val c1 = worksheet.cells["C1"]
        c1.putValue("Отримувач")
        setBold(c1)

        val d1 = worksheet.cells["D1"]
        d1.putValue("Дата")
        setBold(d1)

        val e1 = worksheet.cells["E1"]
        e1.putValue("Примітки")
        setBold(e1)
    }

    private fun setupDeliveryNoteTitlesRow(worksheet: Worksheet) {
        val a1 = worksheet.cells["A1"]
        a1.putValue("Майно")
        setBold(a1)

        val b1 = worksheet.cells["B1"]
        b1.putValue("Видав")
        setBold(b1)

        val c1 = worksheet.cells["C1"]
        c1.putValue("Отримувач")
        setBold(c1)

        val d1 = worksheet.cells["D1"]
        d1.putValue("Дата")
        setBold(d1)

        val e1 = worksheet.cells["E1"]
        e1.putValue("Примітки")
        setBold(e1)
    }

    //TODO: auto column width
    override fun exportItemsFile(items: List<Item>, name: String): File? {
        val workbook = Workbook()
        setupItemsTitlesRow(workbook.worksheets[0])
        items.forEachIndexed { index, item ->
            fillItemInfo(item, index, workbook.worksheets[0])
        }
        val file = excelFile(name)
        workbook.save(file?.path)
        return file
    }

    private fun setupItemsTitlesRow(worksheet: Worksheet) {
        var cell = worksheet.cells["A1"]
        cell.putValue("Назва")
        setBold(cell)

        cell = worksheet.cells["B1"]
        cell.putValue("Кількість")
        setBold(cell)

        cell = worksheet.cells["C1"]
        cell.putValue("Одиниця виміру")
        setBold(cell)

        cell = worksheet.cells["D1"]
        cell.putValue("Категорія")
        setBold(cell)

        cell = worksheet.cells["E1"]
        cell.putValue("Підкатегорія")
        setBold(cell)

        cell = worksheet.cells["F1"]
        cell.putValue("Серійний номер")
        setBold(cell)

        cell = worksheet.cells["G1"]
        cell.putValue("ID")
        setBold(cell)

        cell = worksheet.cells["H1"]
        cell.putValue("Barcode")
        setBold(cell)

        cell = worksheet.cells["I1"]
        cell.putValue("Локація")
        setBold(cell)

        cell = worksheet.cells["J1"]
        cell.putValue("Підлокація")
        setBold(cell)

        cell = worksheet.cells["K1"]
        cell.putValue("Власність")
        setBold(cell)

        cell = worksheet.cells["L1"]
        cell.putValue("Створено")
        setBold(cell)

        cell = worksheet.cells["M1"]
        cell.putValue("Примітки")
        setBold(cell)
    }

    private fun setBold(cell: Cell) {
        val style: Style = cell.style
        val font: Font = style.font
        font.isBold = true
        cell.style = style
    }

    private fun fillItemInfo(item: Item, index: Int, worksheet: Worksheet) {
        worksheet.cells["A${index + 2}"].putValue(item.title)
        worksheet.cells["B${index + 2}"].putValue(item.quantity?.quantity ?: 1)
        worksheet.cells["C${index + 2}"].putValue(item.quantity?.measurement ?: "")
        worksheet.cells["D${index + 2}"].putValue(item.category?.title)
        worksheet.cells["E${index + 2}"].putValue(item.subcategories.firstOrNull()?.title)
        worksheet.cells["F${index + 2}"].putValue(item.sn)
        worksheet.cells["G${index + 2}"].putValue(item.id)
        worksheet.cells["H${index + 2}"].putValue(item.barcode)
        worksheet.cells["I${index + 2}"].putValue(item.location?.title)
        worksheet.cells["J${index + 2}"].putValue(item.sublocation?.title)
        worksheet.cells["K${index + 2}"].putValue(item.ownershipType?.title)
        worksheet.cells["L${index + 2}"].putValue(item.createdDate)
        worksheet.cells["M${index + 2}"].putValue(item.notes)
    }

    private fun excelFile(title: String): File? {
        val context = appContext.get() ?: return null
        return try {
            val root = File(context.filesDir, "export")
            if (!root.exists()) root.mkdirs()
            val xlsFile = File(root, "$title.xlsx")
            if (!xlsFile.exists()) xlsFile.createNewFile()
            xlsFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
