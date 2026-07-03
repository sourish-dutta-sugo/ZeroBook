package com.zerobook.app.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zerobook.app.data.AdditionalCharge
import com.zerobook.app.data.AppPreferences
import com.zerobook.app.data.AppDatabase
import com.zerobook.app.data.AppRepository
import com.zerobook.app.data.BankCashTransaction
import com.zerobook.app.data.BillReceivable
import com.zerobook.app.data.BusinessProfile
import com.zerobook.app.data.Expense
import com.zerobook.app.data.FinancialYearUtils
import com.zerobook.app.data.LedgerAccount
import com.zerobook.app.data.LedgerEntry
import com.zerobook.app.data.JournalLine
import com.zerobook.app.data.Party
import com.zerobook.app.data.Product
import com.zerobook.app.data.ReceiptAllocation
import com.zerobook.app.data.Voucher
import com.zerobook.app.data.VoucherItem
import com.zerobook.app.data.VoucherSaveExtras
import com.zerobook.app.data.EmailReminderScheduler
import com.zerobook.app.services.ExportStorageManager
import com.zerobook.app.services.ExportTarget
import com.zerobook.app.services.InvoiceGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDate

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AppViewModel(application: Application) : AndroidViewModel(application) {
    data class VoucherPrefillRequest(
        val voucherType: String,
        val partyId: String?,
        val invoiceId: String?,
        val amount: Double?,
        val sourceVoucherId: String? = null
    )

    sealed class DbInitState {
        data object Loading : DbInitState()
        data object Success : DbInitState()
        data class Error(val message: String) : DbInitState()
    }

    val dbInitState = MutableStateFlow<DbInitState>(DbInitState.Loading)

    private val repository: AppRepository

    val profile: StateFlow<BusinessProfile?>
    val availableFinancialYears: StateFlow<List<String>>
    val parties: StateFlow<List<Party>>
    val products: StateFlow<List<Product>>
    val vouchers: StateFlow<List<Voucher>>
    val ledgerEntries: StateFlow<List<LedgerEntry>>
    val transactions: StateFlow<List<BankCashTransaction>>
    val receiptAllocations: StateFlow<List<ReceiptAllocation>>
    val ledgerAccounts: StateFlow<List<LedgerAccount>>
    val billsReceivable: StateFlow<List<BillReceivable>>
    val expenses: StateFlow<List<Expense>>

    val isSetupCompleted = MutableStateFlow(false)
    val setupStatusResolved = MutableStateFlow(false)
    val financialYear = MutableStateFlow(FinancialYearUtils.currentFinancialYearCode())
    val voucherPrefillRequest = MutableStateFlow<VoucherPrefillRequest?>(null)

    init {
        val tempRepo = try {
            val db = AppDatabase.getDatabase(application)
            db.openHelper.writableDatabase
            dbInitState.value = DbInitState.Success
            AppRepository(db)
        } catch (e: Exception) {
            e.printStackTrace()
            dbInitState.value = DbInitState.Error(e.localizedMessage ?: "Failed to initialize SQLite Database.")
            AppRepository(AppDatabase.getDatabase(application))
        }
        repository = tempRepo

        profile = repository.profile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

        availableFinancialYears = repository.observeAvailableFinancialYearCodes().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FinancialYearUtils.buildGeneratedYears()
        )

        parties = financialYear.flatMapLatest(repository::observeParties).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

        products = financialYear.flatMapLatest(repository::observeProducts).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

        vouchers = financialYear.flatMapLatest(repository::observeVouchers).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

        ledgerEntries = financialYear.flatMapLatest(repository::observeLedgerEntries).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

        transactions = financialYear.flatMapLatest(repository::observeTransactions).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

        receiptAllocations = financialYear.flatMapLatest(repository::observeReceiptAllocations).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

        ledgerAccounts = financialYear.flatMapLatest(repository::observeLedgerAccounts).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

        billsReceivable = financialYear.flatMapLatest(repository::observeBillsReceivable).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

        expenses = financialYear.flatMapLatest(repository::observeExpenses).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

        if (dbInitState.value is DbInitState.Success) {
            viewModelScope.launch {
                repository.seedLedgersIfEmpty()
                repository.ensureFinancialYearExists(financialYear.value)
            }

            viewModelScope.launch {
                repository.profile.collect { prof ->
                    setupStatusResolved.value = true
                    isSetupCompleted.value = prof != null
                    val resolvedFy = prof?.fyLabel?.takeIf { it.isNotBlank() } ?: FinancialYearUtils.currentFinancialYearCode()
                    financialYear.value = resolvedFy
                    repository.ensureFinancialYearExists(resolvedFy)
                }
            }
        }
    }

    fun insertAllocation(allocation: ReceiptAllocation) {
        viewModelScope.launch {
            repository.insertAllocation(allocation.copy(financialYearCode = financialYear.value))
        }
    }

    fun insertLedgerAccount(account: LedgerAccount) {
        viewModelScope.launch {
            repository.insertLedgerAccount(account, financialYear.value)
        }
    }

    fun deleteLedgerAccount(id: String) {
        viewModelScope.launch {
            repository.deleteLedgerAccount(id)
        }
    }

    fun saveProfile(profile: BusinessProfile, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.insertProfile(profile.copy(fyLabel = financialYear.value))
            syncBusinessProfileTerms(profile.termsAndConditions)
            setupStatusResolved.value = true
            isSetupCompleted.value = true
            onSuccess()
        }
    }

    fun updateProfile(profile: BusinessProfile, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.insertProfile(profile)
            syncBusinessProfileTerms(profile.termsAndConditions)
            setupStatusResolved.value = true
            onSuccess()
        }
    }

    fun switchFinancialYear(financialYearCode: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.ensureFinancialYearExists(financialYearCode)
            financialYear.value = financialYearCode
            profile.value?.let { repository.insertProfile(it.copy(fyLabel = financialYearCode)) }
            onSuccess()
        }
    }

    fun saveParty(party: Party, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.insertParty(party, financialYear.value)
            onSuccess()
        }
    }

    fun updateParty(party: Party, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateParty(party, financialYear.value)
            onSuccess()
        }
    }

    fun scheduleDueReminder(voucherId: String, creditDueDate: String) {
        EmailReminderScheduler.scheduleDueReminder(
            context = getApplication(),
            voucherId = voucherId,
            partyEmail = "",
            creditDueDate = creditDueDate
        )
    }

    fun getPartyById(partyId: String): Party? = parties.value.find { it.id == partyId }

    fun deleteParty(partyId: String) {
        viewModelScope.launch {
            repository.deleteParty(partyId)
        }
    }

    fun saveProduct(product: Product, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.insertProduct(product, financialYear.value)
            onSuccess()
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    fun saveExpense(expense: Expense, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.insertExpense(expense.copy(fyLabel = financialYear.value))
            onSuccess()
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            repository.deleteExpense(expenseId)
        }
    }

    suspend fun generateNextVoucherNo(type: String, timestamp: Long): String =
        repository.generateNextVoucherNo(type, timestamp)

    suspend fun getVoucherSaveExtras(voucherId: String): VoucherSaveExtras =
        repository.getVoucherSaveExtras(voucherId)

    fun saveVoucher(
        voucher: Voucher,
        items: List<VoucherItem>,
        partyName: String?,
        extras: VoucherSaveExtras = VoucherSaveExtras(),
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.saveAndPostVoucher(
                voucher = voucher.copy(financialYearCode = financialYear.value),
                items = items.map { it.copy(financialYearCode = financialYear.value) },
                partyName = partyName,
                extras = extras
            )
            onSuccess()
        }
    }

    fun saveJournalVoucher(
        voucher: Voucher,
        lines: List<JournalLine>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.saveJournalVoucher(
                voucher = voucher.copy(financialYearCode = financialYear.value),
                lines = lines
            )
            onSuccess()
        }
    }

    fun closeFinancialYear(
        sourceFinancialYearCode: String,
        targetFinancialYearCode: String = FinancialYearUtils.nextFinancialYear(sourceFinancialYearCode),
        lockSourceYear: Boolean,
        onSuccess: (AppRepository.FinancialYearCloseResult) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                repository.closeFinancialYear(
                    sourceFinancialYearCode = sourceFinancialYearCode,
                    targetFinancialYearCode = targetFinancialYearCode,
                    lockSourceYear = lockSourceYear
                )
            }.onSuccess(onSuccess).onFailure(onError)
        }
    }

    fun deleteVoucher(voucherId: String) {
        viewModelScope.launch {
            repository.deleteVoucher(voucherId)
        }
    }

    fun getVoucherById(voucherId: String): Voucher? = vouchers.value.find { it.id == voucherId }

    fun getItemsForVoucher(voucherId: String) = repository.getItemsForVoucher(voucherId)

    suspend fun getInvoiceRenderBundle(voucherId: String): InvoiceGenerator.InvoiceRenderBundle? =
        InvoiceGenerator.buildRenderBundle(getApplication(), voucherId)

    suspend fun getSaleVoucherReferenceFields(voucherId: String): Pair<String, String> {
        val db = AppDatabase.getDatabase(getApplication())
        val cursor = db.openHelper.readableDatabase.query(
            """
            SELECT COALESCE(NULLIF(reference_no, ''), NULLIF(referenceNo, ''), ''),
                   COALESCE(NULLIF(other_references, ''), '')
            FROM vouchers
            WHERE id = ?
            """.trimIndent(),
            arrayOf(voucherId)
        )
        cursor.use {
            return if (it.moveToFirst()) {
                it.getString(0).orEmpty() to it.getString(1).orEmpty()
            } else {
                "" to ""
            }
        }
    }

    fun syncSaleVoucherReferenceFields(voucherId: String, referenceNo: String, otherReferences: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(getApplication())
            db.openHelper.writableDatabase.execSQL(
                """
                UPDATE vouchers
                SET reference_no = ?,
                    other_references = ?
                WHERE id = ?
                """.trimIndent(),
                arrayOf(referenceNo, otherReferences, voucherId)
            )
        }
    }

    private suspend fun syncBusinessProfileTerms(termsAndConditions: String) {
        val db = AppDatabase.getDatabase(getApplication())
        db.openHelper.writableDatabase.execSQL(
            "UPDATE business_profile SET terms_and_conditions = ? WHERE id = 1",
            arrayOf(termsAndConditions)
        )
    }

    fun saveTransaction(tx: BankCashTransaction, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.saveBankCashTransaction(tx.copy(financialYearCode = financialYear.value))
            onSuccess()
        }
    }

    fun deleteTransaction(txId: String) {
        viewModelScope.launch {
            repository.deleteTransaction(txId)
        }
    }

    fun setVoucherPrefillRequest(request: VoucherPrefillRequest?) {
        voucherPrefillRequest.value = request
    }

    fun loadSampleData() {
        viewModelScope.launch {
            repository.insertSampleData()
        }
    }

    fun backupDatabase(context: Context): ExportStorageManager.ExportResult? {
        return try {
            val dbFile = context.getDatabasePath("ZeroBook.db")
            if (dbFile.exists()) {
                ExportStorageManager.exportFile(
                    context = context,
                    sourceFile = dbFile,
                    displayName = "ZeroBook_Backup_${System.currentTimeMillis()}.db",
                    mimeType = "application/octet-stream",
                    target = ExportTarget.Backups
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun restoreDatabase(context: Context, backupFile: File): Boolean {
        return try {
            val dbFile = context.getDatabasePath("ZeroBook.db")
            dbFile.delete()
            FileInputStream(backupFile).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun fetchPinCodeDetails(pincode: String, onResult: (city: String?, state: String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = java.net.URL("https://api.postalpincode.in/pincode/$pincode")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val districtRegex = """"District"\s*:\s*"([^"]+)"""".toRegex()
                val stateRegex = """"State"\s*:\s*"([^"]+)"""".toRegex()

                val district = districtRegex.find(responseText)?.groupValues?.get(1)
                val state = stateRegex.find(responseText)?.groupValues?.get(1)

                viewModelScope.launch(Dispatchers.Main) {
                    onResult(district, state)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                viewModelScope.launch(Dispatchers.Main) {
                    onResult(null, null)
                }
            }
        }
    }

    fun fetchIfscDetails(ifsc: String, onResult: (bankName: String?, branchName: String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = java.net.URL("https://ifsc.razorpay.com/$ifsc")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val bankRegex = """"BANK"\s*:\s*"([^"]+)"""".toRegex()
                val branchRegex = """"BRANCH"\s*:\s*"([^"]+)"""".toRegex()
                val bank = bankRegex.find(responseText)?.groupValues?.get(1)
                val branch = branchRegex.find(responseText)?.groupValues?.get(1)

                viewModelScope.launch(Dispatchers.Main) {
                    onResult(bank, branch)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                viewModelScope.launch(Dispatchers.Main) {
                    onResult(null, null)
                }
            }
        }
    }

    fun fetchGstinDetails(gstin: String, onResult: (tradeName: String?, legalName: String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = java.net.URL("https://api.copreco.com/gstin/$gstin")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val tradeNameRegex = """"tradeNam"\s*:\s*"([^"]+)"""".toRegex()
                val legalNameRegex = """"lgnm"\s*:\s*"([^"]+)"""".toRegex()
                val legalNameAltRegex = """"legalName"\s*:\s*"([^"]+)"""".toRegex()

                val trade = tradeNameRegex.find(responseText)?.groupValues?.get(1)
                val legal = legalNameRegex.find(responseText)?.groupValues?.get(1)
                    ?: legalNameAltRegex.find(responseText)?.groupValues?.get(1)

                viewModelScope.launch(Dispatchers.Main) {
                    onResult(trade, legal)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                viewModelScope.launch(Dispatchers.Main) {
                    onResult(null, null)
                }
            }
        }
    }

    suspend fun autoAdvanceFinancialYearIfNeeded(context: Context): String? {
        val today = LocalDate.now()
        val lastCheckedDate = AppPreferences.getFyLastCheckedDate(context)
        if (lastCheckedDate == today) return null

        val activeProfile = profile.value ?: repository.getProfileSync()
        val currentFyCode = activeProfile?.fyLabel?.ifBlank { FinancialYearUtils.currentFinancialYearCode() }
            ?: FinancialYearUtils.currentFinancialYearCode()
        val fyEndDate = FinancialYearUtils.endDateFor(currentFyCode)
        val updatedLabel = if (today.isAfter(fyEndDate)) {
            val newFyCode = FinancialYearUtils.financialYearCodeFor(today)
            if (newFyCode != currentFyCode) {
                repository.ensureFinancialYearExists(newFyCode)
                financialYear.value = newFyCode
                activeProfile?.let { repository.insertProfile(it.copy(fyLabel = newFyCode)) }
                newFyCode
            } else {
                null
            }
        } else {
            null
        }

        AppPreferences.setFyLastCheckedDate(context, today)
        return updatedLabel
    }
}
