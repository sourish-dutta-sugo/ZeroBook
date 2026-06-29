package com.zerobook.app.services

import android.content.Context
import android.net.Uri
import com.zerobook.app.data.AppDatabase
import com.zerobook.app.data.FinancialYearUtils
import com.zerobook.app.data.LedgerEntry
import com.zerobook.app.data.Party
import com.zerobook.app.data.Product
import com.zerobook.app.data.Voucher
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

object CsvTransferManager {
    data class ImportSummary(
        val typeLabel: String,
        val importedCount: Int,
        val skippedCount: Int
    )

    private val exportDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun exportAll(
        context: Context,
        vouchers: List<Voucher>,
        parties: List<Party>,
        products: List<Product>,
        ledgerEntries: List<LedgerEntry>,
        partyLookup: Map<String, String>,
        financialYearLabel: String
    ): List<ExportStorageManager.ExportResult> {
        val stamp = financialYearLabel.replace("/", "-")
        val results = mutableListOf<ExportStorageManager.ExportResult>()
        results += exportCsv(
            context,
            "ZeroBook_Vouchers_$stamp.csv",
            listOf("voucherNo", "type", "date", "partyName", "netAmount", "paymentMode", "status"),
            vouchers.map { voucher ->
                listOf(
                    voucher.voucherNo,
                    voucher.type,
                    millisToDate(voucher.date),
                    partyLookup[voucher.partyId].orEmpty(),
                    voucher.netAmount.toString(),
                    voucher.paymentMode,
                    voucher.status
                )
            }
        )
        results += exportCsv(
            context,
            "ZeroBook_Parties_$stamp.csv",
            listOf("name", "type", "phone", "email", "gstin", "openingBalance", "balanceType"),
            parties.map {
                listOf(it.name, it.type, it.phone, it.email, it.gstin.orEmpty(), it.openingBalance.toString(), it.balanceType)
            }
        )
        results += exportCsv(
            context,
            "ZeroBook_Products_$stamp.csv",
            listOf("name", "hsnCode", "unit", "saleRate", "purchaseRate", "gstRate", "openingStock"),
            products.map {
                listOf(it.name, it.hsnCode, it.unit, it.saleRate.toString(), it.purchaseRate.toString(), it.gstRate.toString(), it.openingStock.toString())
            }
        )
        results += exportCsv(
            context,
            "ZeroBook_Ledger_$stamp.csv",
            listOf("accountHead", "date", "debit", "credit", "narration", "voucherNo"),
            ledgerEntries.map {
                listOf(it.accountHead, millisToDate(it.date), it.debit.toString(), it.credit.toString(), it.narration, it.voucherId)
            }
        )
        return results
    }

    suspend fun importCsv(
        context: Context,
        uri: Uri,
        financialYearCode: String
    ): ImportSummary {
        val csvText = context.contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input)).readText()
        }.orEmpty()
        val rows = parseCsv(csvText)
        if (rows.isEmpty()) return ImportSummary("Unknown", 0, 0)
        val headers = rows.first().map { it.trim() }
        val dataRows = rows.drop(1)
        val db = AppDatabase.getDatabase(context)
        return when {
            headers.containsAll(listOf("voucherNo", "type", "date", "partyName", "netAmount", "paymentMode", "status")) -> {
                val existingParties = db.partyDao().getAllPartiesSync().associateBy { it.name.trim().lowercase(Locale.ENGLISH) }
                var imported = 0
                var skipped = 0
                dataRows.forEach { values ->
                    try {
                        val row = headers.zip(values).toMap()
                        val voucherNo = row["voucherNo"].orEmpty()
                        val type = row["type"].orEmpty()
                        if (voucherNo.isBlank() || type.isBlank()) {
                            skipped++
                        } else {
                            db.voucherDao().insertVoucher(
                                Voucher(
                                    id = UUID.randomUUID().toString(),
                                    voucherNo = voucherNo,
                                    type = type,
                                    date = dateToMillis(row["date"].orEmpty()),
                                    partyId = existingParties[row["partyName"].orEmpty().trim().lowercase(Locale.ENGLISH)]?.id,
                                    narration = "",
                                    taxableAmount = row["netAmount"]?.toDoubleOrNull() ?: 0.0,
                                    cgst = 0.0,
                                    sgst = 0.0,
                                    igst = 0.0,
                                    roundOff = 0.0,
                                    netAmount = row["netAmount"]?.toDoubleOrNull() ?: 0.0,
                                    paymentMode = row["paymentMode"].orEmpty(),
                                    chequeNo = null,
                                    chequeDate = null,
                                    bankName = null,
                                    isIgst = false,
                                    status = row["status"].orEmpty().ifBlank { "POSTED" },
                                    financialYearCode = financialYearCode
                                )
                            )
                            imported++
                        }
                    } catch (_: Exception) {
                        skipped++
                    }
                }
                ImportSummary("Vouchers", imported, skipped)
            }
            headers.containsAll(listOf("name", "type", "phone", "email", "gstin", "openingBalance", "balanceType")) -> {
                var imported = 0
                var skipped = 0
                dataRows.forEach { values ->
                    try {
                        val row = headers.zip(values).toMap()
                        val name = row["name"].orEmpty()
                        if (name.isBlank()) skipped++ else {
                            db.partyDao().insertParty(
                                Party(
                                    id = UUID.randomUUID().toString(),
                                    name = name,
                                    type = row["type"].orEmpty().ifBlank { "CUSTOMER" },
                                    phone = row["phone"].orEmpty(),
                                    email = row["email"].orEmpty(),
                                    address = "",
                                    city = "",
                                    state = "",
                                    stateCode = "",
                                    gstin = row["gstin"].orEmpty().ifBlank { null },
                                    pan = null,
                                    openingBalance = row["openingBalance"]?.toDoubleOrNull() ?: 0.0,
                                    balanceType = row["balanceType"].orEmpty().ifBlank { "DR" }
                                )
                            )
                            imported++
                        }
                    } catch (_: Exception) {
                        skipped++
                    }
                }
                ImportSummary("Parties", imported, skipped)
            }
            headers.containsAll(listOf("name", "hsnCode", "unit", "saleRate", "purchaseRate", "gstRate", "openingStock")) -> {
                var imported = 0
                var skipped = 0
                dataRows.forEach { values ->
                    try {
                        val row = headers.zip(values).toMap()
                        val name = row["name"].orEmpty()
                        if (name.isBlank()) skipped++ else {
                            db.productDao().insertProduct(
                                Product(
                                    id = UUID.randomUUID().toString(),
                                    name = name,
                                    hsnCode = row["hsnCode"].orEmpty(),
                                    unit = row["unit"].orEmpty().ifBlank { "PCS" },
                                    saleRate = row["saleRate"]?.toDoubleOrNull() ?: 0.0,
                                    purchaseRate = row["purchaseRate"]?.toDoubleOrNull() ?: 0.0,
                                    gstRate = row["gstRate"]?.toDoubleOrNull() ?: 0.0,
                                    openingStock = row["openingStock"]?.toDoubleOrNull() ?: 0.0
                                )
                            )
                            imported++
                        }
                    } catch (_: Exception) {
                        skipped++
                    }
                }
                ImportSummary("Products", imported, skipped)
            }
            headers.containsAll(listOf("accountHead", "date", "debit", "credit", "narration", "voucherNo")) -> {
                var imported = 0
                var skipped = 0
                dataRows.forEach { values ->
                    try {
                        val row = headers.zip(values).toMap()
                        val accountHead = row["accountHead"].orEmpty()
                        if (accountHead.isBlank()) skipped++ else {
                            db.ledgerDao().insertLedgerEntries(
                                listOf(
                                    LedgerEntry(
                                        id = UUID.randomUUID().toString(),
                                        accountHead = accountHead,
                                        voucherId = row["voucherNo"].orEmpty(),
                                        date = dateToMillis(row["date"].orEmpty()),
                                        debit = row["debit"]?.toDoubleOrNull() ?: 0.0,
                                        credit = row["credit"]?.toDoubleOrNull() ?: 0.0,
                                        narration = row["narration"].orEmpty(),
                                        financialYearCode = financialYearCode
                                    )
                                )
                            )
                            imported++
                        }
                    } catch (_: Exception) {
                        skipped++
                    }
                }
                ImportSummary("Ledger", imported, skipped)
            }
            else -> ImportSummary("Unknown", 0, dataRows.size)
        }
    }

    private fun exportCsv(
        context: Context,
        fileName: String,
        headers: List<String>,
        rows: List<List<String>>
    ): ExportStorageManager.ExportResult {
        val content = buildString {
            append(headers.joinToString(",") { escapeCsv(it) })
            append('\n')
            rows.forEach { row ->
                append(row.joinToString(",") { escapeCsv(it) })
                append('\n')
            }
        }
        return ExportStorageManager.exportBytes(
            context = context,
            bytes = content.toByteArray(),
            displayName = fileName,
            mimeType = "text/csv",
            target = ExportTarget.Exports
        )
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            "\"$escaped\""
        } else {
            escaped
        }
    }

    private fun parseCsv(content: String): List<List<String>> {
        if (content.isBlank()) return emptyList()
        val rows = mutableListOf<List<String>>()
        var currentField = StringBuilder()
        var currentRow = mutableListOf<String>()
        var inQuotes = false
        var index = 0
        while (index < content.length) {
            val char = content[index]
            when {
                char == '"' && inQuotes && index + 1 < content.length && content[index + 1] == '"' -> {
                    currentField.append('"')
                    index++
                }
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    currentRow += currentField.toString()
                    currentField = StringBuilder()
                }
                (char == '\n' || char == '\r') && !inQuotes -> {
                    if (char == '\r' && index + 1 < content.length && content[index + 1] == '\n') index++
                    currentRow += currentField.toString()
                    if (currentRow.any { it.isNotBlank() }) rows += currentRow.toList()
                    currentRow = mutableListOf()
                    currentField = StringBuilder()
                }
                else -> currentField.append(char)
            }
            index++
        }
        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow += currentField.toString()
            if (currentRow.any { it.isNotBlank() }) rows += currentRow.toList()
        }
        return rows
    }

    private fun millisToDate(timestamp: Long): String =
        LocalDate.ofInstant(java.time.Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).format(exportDateFormatter)

    private fun dateToMillis(value: String): Long =
        runCatching {
            LocalDate.parse(value.trim(), exportDateFormatter)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrElse { System.currentTimeMillis() }
}
