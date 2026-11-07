package com.contracts.app.data.repository

import android.content.Context
import android.net.Uri
import com.contracts.app.data.model.Contract
import com.contracts.app.data.model.ContractStatus
import com.contracts.app.utils.JalaliConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileOutputStream

class ExcelRepository(private val context: Context) {

    private val cache: File get() = File(context.filesDir, "contracts.xlsm")

    fun hasCached() = cache.exists()

    suspend fun saveAndParse(uri: Uri): List<Contract> = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(cache).use { input.copyTo(it) }
        } ?: error("فایل باز نشد")
        parse()
    }

    suspend fun loadCached(): List<Contract> = withContext(Dispatchers.IO) {
        if (!cache.exists()) emptyList() else parse()
    }

    private fun parse(): List<Contract> {
        val wb = WorkbookFactory.create(cache)
        val sheet = wb.getSheet("قراردادها") ?: wb.getSheetAt(0) ?: return emptyList()
        val result = mutableListOf<Contract>()

        fun str(ri: Int, col: Int): String {
            val row = sheet.getRow(ri) ?: return ""
            val c = row.getCell(col) ?: return ""
            return when (c.cellType) {
                CellType.STRING  -> c.stringCellValue.trim()
                CellType.NUMERIC -> {
                    val n = c.numericCellValue
                    if (n == n.toLong().toDouble()) n.toLong().toString() else n.toString()
                }
                else -> ""
            }
        }

        /** مبلغ خام ستون K به ریاله (با نقطه به‌عنوان جداکننده هزارگان، مثل "17.928.000.000")
         *  به تومان تبدیل می‌شه (÷۱۰) و با کاما فرمت می‌شه، مثل "1,792,800,000". */
        fun rialToToman(raw: String): String {
            val digitsOnly = raw.filter { it.isDigit() }
            if (digitsOnly.isBlank()) return ""
            val rial = digitsOnly.toLongOrNull() ?: return ""
            val toman = rial / 10
            return "%,d".format(toman)
        }

        // نگاشت ستون‌ها مطابق قالب واقعی اکسل:
        // A=مدیریت B=شماره C=عنوان D=شرکت E=شروع‌میلادی F=پایان‌میلادی G=شروع‌شمسی H=پایان‌شمسی
        // I=مدت(ماه) J=روزباقیمانده K=مبلغ(ریال) L=روش‌پرداخت M=وضعیت N=نوع O=توضیحات P=لینک
        // Q=تاریخ‌قرارداد R=ایمیل‌مدیریت S=نام‌رابط T=سمت‌رابط U=تلفن‌رابط
        for (ri in 1..sheet.lastRowNum) {
            val endJ = str(ri, 7)   // H – تاریخ پایان شمسی
            val startJ = str(ri, 6) // G – تاریخ شروع شمسی
            if (!JalaliConverter.isValid(endJ)) continue

            val days = JalaliConverter.daysRemaining(endJ)
            val endGreg = JalaliConverter.toGregorian(endJ)
            val startGreg = if (JalaliConverter.isValid(startJ)) JalaliConverter.toGregorian(startJ) else null

            result.add(Contract(
                id             = ri,
                department     = str(ri, 0),
                contractNumber = str(ri, 1),
                title          = str(ri, 2),
                company        = str(ri, 3),
                startDateJalali= startJ,
                endDateJalali  = endJ,
                startDateGreg  = startGreg?.let { JalaliConverter.formatGregorian(it) } ?: "",
                endDateGreg    = endGreg?.let { JalaliConverter.formatGregorian(it) } ?: "",
                durationMonths = JalaliConverter.durationMonths(startJ, endJ),
                daysRemaining  = days,
                amount         = rialToToman(str(ri, 10)),
                paymentMethod  = str(ri, 11),
                status         = ContractStatus.from(days),
                contractType   = str(ri, 13),
                notes          = str(ri, 14),
                fileLink       = str(ri, 15),
                managerEmail   = str(ri, 17), // R – ایمیل مدیریت (مخصوص این قرارداد)
                contactName    = str(ri, 18), // S – نام رابط
                contactRole    = str(ri, 19), // T – سمت رابط
                contactPhone   = str(ri, 20), // U – تلفن رابط
            ))
        }
        wb.close()
        return result
    }
}
