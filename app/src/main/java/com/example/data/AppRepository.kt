package com.example.data

import android.content.Context
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import androidx.sqlite.db.SimpleSQLiteQuery
import org.json.JSONObject

class AppRepository(private val db: AppDatabase) {
    data class FinancialYearCloseResult(
        val sourceFinancialYearCode: String,
        val targetFinancialYearCode: String,
        val inventoryItemsCarried: Int,
        val partyBalancesCarried: Int,
        val ledgerBalancesCarried: Int,
        val lockedSourceYear: Boolean
    )

    val profile = db.businessProfileDao().getProfile()
    val financialYears = db.financialYearDao().getAllFinancialYears()

    fun observeParties(financialYearCode: String): Flow<List<Party>> =
        combine(
            db.partyDao().getAllParties(),
            db.partyFinancialYearBalanceDao().getBalancesForYear(financialYearCode)
        ) { parties, balances ->
            val balanceMap = balances.associateBy { it.partyId }
            parties.map { party ->
                balanceMap[party.id]?.let { yearBalance ->
                    party.copy(
                        openingBalance = yearBalance.openingBalance,
                        balanceType = yearBalance.balanceType
                    )
                } ?: party
            }
        }

    fun observeProducts(financialYearCode: String): Flow<List<Product>> =
        combine(
            db.productDao().getAllProducts(),
            db.productFinancialYearBalanceDao().getBalancesForYear(financialYearCode)
        ) { products, balances ->
            val balanceMap = balances.associateBy { it.productId }
            products.map { product ->
                balanceMap[product.id]?.let { yearBalance ->
                    val derivedPurchaseRate = if (yearBalance.openingStock > 0.0) {
                        yearBalance.openingStockValue / yearBalance.openingStock
                    } else {
                        product.purchaseRate
                    }
                    product.copy(
                        openingStock = yearBalance.openingStock,
                        purchaseRate = derivedPurchaseRate,
                        currentStock = if (product.currentStock == 0.0 && yearBalance.openingStock > 0.0) {
                            yearBalance.openingStock
                        } else {
                            product.currentStock
                        },
                        stockUnit = product.stockUnit.ifBlank { product.unit }
                    )
                } ?: product.copy(stockUnit = product.stockUnit.ifBlank { product.unit })
            }
        }

    fun observeVouchers(financialYearCode: String): Flow<List<Voucher>> =
        db.voucherDao().getAllVouchersForYear(financialYearCode)

    fun observeLedgerEntries(financialYearCode: String): Flow<List<LedgerEntry>> =
        db.ledgerDao().getAllLedgerEntriesForYear(financialYearCode)

    fun observeTransactions(financialYearCode: String): Flow<List<BankCashTransaction>> =
        db.bankCashDao().getAllTransactionsForYear(financialYearCode)

    fun observeReceiptAllocations(financialYearCode: String): Flow<List<ReceiptAllocation>> =
        db.receiptAllocationDao().getAllReceiptAllocations(financialYearCode)

    fun observeLedgerAccounts(financialYearCode: String): Flow<List<LedgerAccount>> =
        combine(
            db.ledgerAccountDao().getAllLedgerAccounts(),
            db.ledgerAccountFinancialYearBalanceDao().getBalancesForYear(financialYearCode)
        ) { accounts, balances ->
            val balanceMap = balances.associateBy { it.accountId }
            accounts.map { account ->
                balanceMap[account.id]?.let { yearBalance ->
                    account.copy(
                        openingBalance = yearBalance.openingBalance,
                        balanceType = yearBalance.balanceType
                    )
                } ?: account
            }
        }

    fun observeBillsReceivable(financialYearCode: String): Flow<List<BillReceivable>> =
        db.billReceivableDao().getAllBills(financialYearCode)

    fun observeExpenses(financialYearCode: String): Flow<List<Expense>> =
        db.expenseDao().getExpenses(financialYearCode)

    fun observeAvailableFinancialYearCodes(): Flow<List<String>> =
        financialYears.map { storedYears ->
            (storedYears.map { it.code } + FinancialYearUtils.buildGeneratedYears()).distinct().sortedDescending()
        }

    suspend fun ensureFinancialYearExists(financialYearCode: String, sourceFinancialYearCode: String? = null) {
        val existing = db.financialYearDao().getFinancialYearByCode(financialYearCode)
        if (existing == null) {
            db.financialYearDao().insertFinancialYear(
                FinancialYear(
                    code = financialYearCode,
                    startDate = FinancialYearUtils.startMillisFor(financialYearCode),
                    endDate = FinancialYearUtils.endMillisFor(financialYearCode),
                    sourceFinancialYearCode = sourceFinancialYearCode
                )
            )
        }
    }

    suspend fun insertAllocation(allocation: ReceiptAllocation) {
        ensureFinancialYearExists(allocation.financialYearCode)
        db.receiptAllocationDao().insertAllocation(allocation)
        recalculateOutstandings()
    }

    // Retrieve specific items
    fun getItemsForVoucher(voucherId: String): Flow<List<VoucherItem>> =
        db.voucherItemDao().getItemsForVoucher(voucherId)

    suspend fun getItemsForVoucherSync(voucherId: String): List<VoucherItem> =
        db.voucherItemDao().getItemsForVoucherSync(voucherId)

    suspend fun getVoucherById(id: String) = db.voucherDao().getVoucherById(id)

    suspend fun getPartyById(id: String) = db.partyDao().getPartyById(id)

    suspend fun getProductById(id: String) = db.productDao().getProductById(id)

    suspend fun getVoucherSaveExtras(voucherId: String): VoucherSaveExtras {
        val cursor = db.openHelper.readableDatabase.query(
            """
            SELECT COALESCE(partial_amount_paid, 0),
                   COALESCE(partial_payment_submode, ''),
                   COALESCE(credit_due_date, ''),
                   COALESCE(remaining_credit_amount, 0),
                   COALESCE(is_advance, 0),
                   COALESCE(advance_for, '')
            FROM vouchers WHERE id = ?
            """.trimIndent(),
            arrayOf(voucherId)
        )
        cursor.use {
            return if (it.moveToFirst()) {
                VoucherSaveExtras(
                    partialAmountPaid = it.getDouble(0),
                    partialPaymentSubmode = it.getString(1).orEmpty(),
                    creditDueDate = it.getString(2).orEmpty(),
                    remainingCreditAmount = it.getDouble(3),
                    isAdvance = it.getInt(4) == 1,
                    advanceFor = it.getString(5).orEmpty()
                )
            } else {
                VoucherSaveExtras()
            }
        }
    }

    // Profile Operations
    suspend fun insertProfile(profile: BusinessProfile) {
        ensureFinancialYearExists(profile.fyLabel)
        db.businessProfileDao().insertProfile(profile)
    }

    suspend fun getProfileSync(): BusinessProfile? {
        return db.businessProfileDao().getProfileSync()
    }

    // New Ledger Account Operations
    suspend fun getLedgerAccountById(id: String) = db.ledgerAccountDao().getLedgerAccountById(id)
    suspend fun getLedgerAccountByName(name: String) = db.ledgerAccountDao().getLedgerAccountByName(name)
    suspend fun insertLedgerAccount(account: LedgerAccount, financialYearCode: String) {
        ensureFinancialYearExists(financialYearCode)
        db.withTransaction {
            db.ledgerAccountDao().insertLedgerAccount(account)
            db.ledgerAccountFinancialYearBalanceDao().upsertBalance(
                LedgerAccountFinancialYearBalance(
                    accountId = account.id,
                    financialYearCode = financialYearCode,
                    openingBalance = account.openingBalance,
                    balanceType = account.balanceType
                )
            )
        }
    }
    suspend fun deleteLedgerAccount(id: String) = db.ledgerAccountDao().deleteLedgerAccount(id)

    suspend fun seedLedgersIfEmpty() {
        db.withTransaction {
            val existing = db.ledgerAccountDao().getAllLedgerAccountsSync()
            if (existing.isEmpty()) {
                val systemLedgers = listOf(
                    LedgerAccount(UUID.randomUUID().toString(), "Cash", "Cash-in-Hand", 0.0, "DR", 1, 0),
                    LedgerAccount(UUID.randomUUID().toString(), "Bank", "Bank Accounts", 0.0, "DR", 1, 0),
                    LedgerAccount(UUID.randomUUID().toString(), "Sales Account", "Sales Accounts", 0.0, "CR", 1, 0),
                    LedgerAccount(UUID.randomUUID().toString(), "Sales Return", "Sales Accounts", 0.0, "DR", 1, 0),
                    LedgerAccount(UUID.randomUUID().toString(), "Purchase Account", "Purchase Accounts", 0.0, "DR", 1, 0),
                    LedgerAccount(UUID.randomUUID().toString(), "Purchase Return", "Purchase Accounts", 0.0, "CR", 1, 0),
                    LedgerAccount(UUID.randomUUID().toString(), "Bills Payable Account", "Current Liabilities", 0.0, "CR", 1, 0),
                    LedgerAccount(UUID.randomUUID().toString(), "Bills Receivable Account", "Current Assets", 0.0, "DR", 1, 0),
                    
                    LedgerAccount(UUID.randomUUID().toString(), "CGST Payable", "Duties & Taxes", 0.0, "CR", 1, 0),
                    LedgerAccount(UUID.randomUUID().toString(), "SGST Payable", "Duties & Taxes", 0.0, "CR", 1, 0),
                    LedgerAccount(UUID.randomUUID().toString(), "IGST Payable", "Duties & Taxes", 0.0, "CR", 1, 0),
                    LedgerAccount(UUID.randomUUID().toString(), "CGST Receivable", "Duties & Taxes", 0.0, "DR", 1, 0),
                    LedgerAccount(UUID.randomUUID().toString(), "SGST Receivable", "Duties & Taxes", 0.0, "DR", 1, 0),
                    LedgerAccount(UUID.randomUUID().toString(), "IGST Receivable", "Duties & Taxes", 0.0, "DR", 1, 0),
                    
                    LedgerAccount(UUID.randomUUID().toString(), "Round Off Account", "Indirect Expenses", 0.0, "DR", 1, 0),
                    LedgerAccount(UUID.randomUUID().toString(), "Rent", "Indirect Expenses", 0.0, "DR", 1, 0),
                    LedgerAccount(UUID.randomUUID().toString(), "Electricity", "Indirect Expenses", 0.0, "DR", 1, 0),
                    LedgerAccount(UUID.randomUUID().toString(), "Discount Allowed", "Indirect Expenses", 0.0, "DR", 1, 0),
                    LedgerAccount(UUID.randomUUID().toString(), "Discount Received", "Indirect Income", 0.0, "CR", 1, 0),
                    LedgerAccount(UUID.randomUUID().toString(), "Freight Charges", "Direct Expenses", 0.0, "DR", 1, 0),
                    LedgerAccount(UUID.randomUUID().toString(), "Salaries", "Direct Expenses", 0.0, "DR", 1, 0),
                    LedgerAccount(UUID.randomUUID().toString(), "Capital Account", "Capital Account", 0.0, "CR", 1, 0)
                )
                db.ledgerAccountDao().insertLedgerAccounts(systemLedgers)
            }
        }
    }

    // Party Operations (Synchronized with Ledger Accounts)
    suspend fun insertParty(party: Party, financialYearCode: String) {
        db.withTransaction {
            ensureFinancialYearExists(financialYearCode)
            db.partyDao().insertParty(party)
            
            // Sync with LedgerAccount
            val existingAct = db.ledgerAccountDao().getLedgerAccountByPartyId(party.id)
            val group = if (party.type == "CUSTOMER") "Sundry Debtors" else "Sundry Creditors"
            val ledgerBalanceType = party.balanceType
            val act = existingAct?.copy(
                name = party.name,
                groupName = group,
                openingBalance = party.openingBalance,
                balanceType = ledgerBalanceType,
                gstin = party.gstin ?: "",
                phone = party.phone,
                email = party.email,
                address = party.address
            ) ?: LedgerAccount(
                id = UUID.randomUUID().toString(),
                name = party.name,
                groupName = group,
                openingBalance = party.openingBalance,
                balanceType = ledgerBalanceType,
                isSystem = 0,
                isParty = 1,
                partyId = party.id,
                gstin = party.gstin ?: "",
                phone = party.phone,
                email = party.email,
                address = party.address
            )
            db.ledgerAccountDao().insertLedgerAccount(act)
            db.partyFinancialYearBalanceDao().upsertBalance(
                PartyFinancialYearBalance(
                    partyId = party.id,
                    financialYearCode = financialYearCode,
                    openingBalance = party.openingBalance,
                    balanceType = party.balanceType
                )
            )
            db.ledgerAccountFinancialYearBalanceDao().upsertBalance(
                LedgerAccountFinancialYearBalance(
                    accountId = act.id,
                    financialYearCode = financialYearCode,
                    openingBalance = party.openingBalance,
                    balanceType = ledgerBalanceType
                )
            )
        }
    }

    suspend fun updateParty(party: Party, financialYearCode: String) {
        db.withTransaction {
            ensureFinancialYearExists(financialYearCode)
            db.partyDao().insertParty(party)
            db.partyFinancialYearBalanceDao().upsertBalance(
                PartyFinancialYearBalance(
                    partyId = party.id,
                    financialYearCode = financialYearCode,
                    openingBalance = party.openingBalance,
                    balanceType = party.balanceType,
                    updatedAt = System.currentTimeMillis()
                )
            )
            db.openHelper.writableDatabase.execSQL(
                """
                UPDATE ledger_accounts
                SET name = ?,
                    openingBalance = ?,
                    balanceType = ?,
                    gstin = ?,
                    phone = ?,
                    email = ?,
                    address = ?
                WHERE partyId = ?
                """.trimIndent(),
                arrayOf<Any?>(
                    party.name,
                    party.openingBalance,
                    party.balanceType,
                    party.gstin.orEmpty(),
                    party.phone,
                    party.email,
                    party.address,
                    party.id
                )
            )
        }
    }

    suspend fun deleteParty(id: String) {
        db.withTransaction {
            db.partyDao().deleteParty(id)
            db.ledgerAccountDao().deleteLedgerAccountByParty(id)
        }
    }

    // Product Operations
    suspend fun insertProduct(product: Product, financialYearCode: String) {
        ensureFinancialYearExists(financialYearCode)
        db.withTransaction {
            db.productDao().insertProduct(
                product.copy(
                    currentStock = if (product.currentStock == 0.0) product.openingStock else product.currentStock,
                    stockUnit = product.stockUnit.ifBlank { product.unit }
                )
            )
            db.productFinancialYearBalanceDao().upsertBalance(
                ProductFinancialYearBalance(
                    productId = product.id,
                    financialYearCode = financialYearCode,
                    openingStock = product.openingStock,
                    openingStockValue = product.openingStock * product.purchaseRate
                )
            )
        }
    }

    suspend fun deleteProduct(id: String) {
        db.productDao().deleteProduct(id)
    }

    suspend fun insertExpense(expense: Expense) {
        ensureFinancialYearExists(expense.fyLabel)
        db.withTransaction {
            db.expenseDao().insertExpense(expense)
            db.ledgerDao().insertLedgerEntries(
                listOf(
                    LedgerEntry(
                        id = UUID.randomUUID().toString(),
                        accountHead = "${expense.category} Expense Account",
                        voucherId = expense.id,
                        date = expense.date,
                        debit = expense.amount,
                        credit = 0.0,
                        narration = expense.description.ifBlank { "Expense entry" },
                        financialYearCode = expense.fyLabel
                    ),
                    LedgerEntry(
                        id = UUID.randomUUID().toString(),
                        accountHead = if (expense.paymentMode == "CASH") "Cash" else "Bank",
                        voucherId = expense.id,
                        date = expense.date,
                        debit = 0.0,
                        credit = expense.amount,
                        narration = expense.description.ifBlank { "Expense payment" },
                        financialYearCode = expense.fyLabel
                    )
                )
            )
            db.bankCashDao().insertTransaction(
                BankCashTransaction(
                    id = UUID.randomUUID().toString(),
                    type = "PAYMENT",
                    mode = expense.paymentMode,
                    amount = expense.amount,
                    date = expense.date,
                    partyId = null,
                    partyName = null,
                    narration = "Expense ${expense.category}: ${expense.description} [${expense.id}]",
                    chequeNo = null,
                    chequeDate = null,
                    bankName = null,
                    receiptImagePath = expense.attachmentPath.ifBlank { null },
                    financialYearCode = expense.fyLabel
                )
            )
        }
    }

    suspend fun deleteExpense(id: String) {
        db.withTransaction {
            db.expenseDao().deleteExpense(id)
            db.ledgerDao().deleteLedgerEntriesForVoucher(id)
            db.bankCashDao().deleteTransactionsByVoucher(id)
        }
    }

    // Helpers
    fun getFinancialYear(timestamp: Long): String {
        return FinancialYearUtils.financialYearCodeFor(timestamp)
    }

    suspend fun generateNextVoucherNo(type: String, timestamp: Long): String {
        val fy = getFinancialYear(timestamp)
        ensureFinancialYearExists(fy)
        val prefix = when (type) {
            "SALE" -> "SAL"
            "PURCHASE" -> "PUR"
            "SALE_RETURN" -> "SRN"
            "PURCHASE_RETURN" -> "PRN"
            "RECEIPT" -> "RCP"
            "PAYMENT" -> "PMT"
            "DEBIT_NOTE" -> "DBN"
            "CREDIT_NOTE" -> "CRN"
            "QUOTATION" -> "QUO"
            "DELIVERY_CHALLAN" -> "DC"
            "JOURNAL" -> "JNL"
            else -> "VCH"
        }
        val pattern = "$prefix/$fy/%"
        val latestNo = db.voucherDao().getLatestVoucherNo(type, fy, pattern)
        val sequenceNum = if (latestNo != null) {
            val parts = latestNo.split("/")
            if (parts.size >= 3) {
                val lastPart = parts.last().toIntOrNull() ?: 0
                lastPart + 1
            } else {
                1
            }
        } else {
            1
        }
        val formattedSeq = String.format("%04d", sequenceNum)
        return "$prefix/$fy/$formattedSeq"
    }

    // Comprehensive Voucher Post Logic
    suspend fun saveAndPostVoucher(
        voucher: Voucher,
        items: List<VoucherItem>,
        partyName: String?,
        extras: VoucherSaveExtras = VoucherSaveExtras()
    ) {
        db.withTransaction {
            val resolvedFinancialYearCode = voucher.financialYearCode.ifBlank { getFinancialYear(voucher.date) }
            ensureFinancialYearExists(resolvedFinancialYearCode)
            val yearState = db.financialYearDao().getFinancialYearByCode(resolvedFinancialYearCode)
            require(yearState?.isLocked != true) { "Financial year $resolvedFinancialYearCode is locked." }
            val resolvedVoucher = voucher.copy(financialYearCode = resolvedFinancialYearCode)

            // 1. Delete if existing
            db.voucherDao().deleteVoucher(resolvedVoucher.id)
            db.voucherItemDao().deleteItemsForVoucher(resolvedVoucher.id)
            db.ledgerDao().deleteLedgerEntriesForVoucher(resolvedVoucher.id)
            db.bankCashDao().deleteTransactionsByVoucher(resolvedVoucher.id)
            db.receiptAllocationDao().deleteAllocationsByReceipt(resolvedVoucher.id)
            db.billReceivableDao().deleteBillByVoucherId(resolvedVoucher.id)

            // 2. Insert Voucher & Items
            db.voucherDao().insertVoucher(resolvedVoucher)
            db.voucherItemDao().insertItems(
                items.map {
                    it.copy(
                        voucherId = resolvedVoucher.id,
                        financialYearCode = resolvedFinancialYearCode
                    )
                }
            )

            // 3. Auto-generate Ledger entries
            val ledgerList = mutableListOf<LedgerEntry>()
            val partyDesc = partyName ?: "Cash/Bank Account"
            val shouldPostAccounts = resolvedVoucher.status == "POSTED" &&
                resolvedVoucher.type !in setOf("QUOTATION", "DELIVERY_CHALLAN")

            if (shouldPostAccounts) when (resolvedVoucher.type) {
                "SALE" -> {
                    if (resolvedVoucher.paymentMode == "PART PAYMENT") {
                        val paidHead = if (extras.partialPaymentSubmode == "CASH") "Cash" else "Bank"
                        if (extras.partialAmountPaid > 0.0) {
                            ledgerList.add(
                                LedgerEntry(
                                    id = UUID.randomUUID().toString(),
                                    accountHead = paidHead,
                                    voucherId = voucher.id,
                                    date = voucher.date,
                                    debit = extras.partialAmountPaid,
                                    credit = 0.0,
                                    narration = "Part payment received for voucher ${voucher.voucherNo}",
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                        if (extras.remainingCreditAmount > 0.0) {
                            ledgerList.add(
                                LedgerEntry(
                                    id = UUID.randomUUID().toString(),
                                    accountHead = "Party: $partyDesc",
                                    voucherId = voucher.id,
                                    date = voucher.date,
                                    debit = extras.remainingCreditAmount,
                                    credit = 0.0,
                                    narration = "Remaining credit for voucher ${voucher.voucherNo}",
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                    } else {
                        val drHead = if (voucher.paymentMode == "CASH") "Cash" else if (voucher.paymentMode == "BANK" || voucher.paymentMode == "UPI") "Bank" else "Party: $partyDesc"
                        ledgerList.add(
                            LedgerEntry(
                                id = UUID.randomUUID().toString(),
                                accountHead = drHead,
                                voucherId = voucher.id,
                                date = voucher.date,
                                debit = voucher.netAmount,
                                credit = 0.0,
                                narration = "Sales entry for voucher ${voucher.voucherNo}",
                                createdAt = System.currentTimeMillis()
                            )
                        )
                    }
                    // CR: Sales
                    ledgerList.add(
                        LedgerEntry(
                            id = UUID.randomUUID().toString(),
                            accountHead = "Sales Account",
                            voucherId = voucher.id,
                            date = voucher.date,
                            debit = 0.0,
                            credit = voucher.taxableAmount,
                            narration = "Sales credit",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    // CR: CGST / SGST / IGST
                    if (voucher.isIgst) {
                        if (voucher.igst > 0) {
                            ledgerList.add(
                                LedgerEntry(
                                    id = UUID.randomUUID().toString(),
                                    accountHead = "IGST Payable",
                                    voucherId = voucher.id,
                                    date = voucher.date,
                                    debit = 0.0,
                                    credit = voucher.igst,
                                    narration = "IGST liability",
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                    } else {
                        if (voucher.cgst > 0) {
                            ledgerList.add(
                                LedgerEntry(
                                    id = UUID.randomUUID().toString(),
                                    accountHead = "CGST Payable",
                                    voucherId = voucher.id,
                                    date = voucher.date,
                                    debit = 0.0,
                                    credit = voucher.cgst,
                                    narration = "CGST liability",
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                        if (voucher.sgst > 0) {
                            ledgerList.add(
                                LedgerEntry(
                                    id = UUID.randomUUID().toString(),
                                    accountHead = "SGST Payable",
                                    voucherId = voucher.id,
                                    date = voucher.date,
                                    debit = 0.0,
                                    credit = voucher.sgst,
                                    narration = "SGST liability",
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                    // DR/CR: Round Off
                    if (voucher.roundOff != 0.0) {
                        ledgerList.add(
                            LedgerEntry(
                                id = UUID.randomUUID().toString(),
                                accountHead = "Round Off Account",
                                voucherId = voucher.id,
                                date = voucher.date,
                                debit = if (voucher.roundOff > 0) voucher.roundOff else 0.0,
                                credit = if (voucher.roundOff < 0) -voucher.roundOff else 0.0,
                                narration = "Round Off adjust",
                                createdAt = System.currentTimeMillis()
                            )
                        )
                    }
                }

                "PURCHASE" -> {
                    // DR: Purchase
                    ledgerList.add(
                        LedgerEntry(
                            id = UUID.randomUUID().toString(),
                            accountHead = "Purchase Account",
                            voucherId = voucher.id,
                            date = voucher.date,
                            debit = voucher.taxableAmount,
                            credit = 0.0,
                            narration = "Purchase debit",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    // DR: CGST / SGST / IGST Receivable
                    if (voucher.isIgst) {
                        if (voucher.igst > 0) {
                            ledgerList.add(
                                LedgerEntry(
                                    id = UUID.randomUUID().toString(),
                                    accountHead = "IGST Receivable",
                                    voucherId = voucher.id,
                                    date = voucher.date,
                                    debit = voucher.igst,
                                    credit = 0.0,
                                    narration = "IGST receivable",
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                    } else {
                        if (voucher.cgst > 0) {
                            ledgerList.add(
                                LedgerEntry(
                                    id = UUID.randomUUID().toString(),
                                    accountHead = "CGST Receivable",
                                    voucherId = voucher.id,
                                    date = voucher.date,
                                    debit = voucher.cgst,
                                    credit = 0.0,
                                    narration = "CGST receivable",
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                        if (voucher.sgst > 0) {
                            ledgerList.add(
                                LedgerEntry(
                                    id = UUID.randomUUID().toString(),
                                    accountHead = "SGST Receivable",
                                    voucherId = voucher.id,
                                    date = voucher.date,
                                    debit = voucher.sgst,
                                    credit = 0.0,
                                    narration = "SGST receivable",
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                    // DR/CR: Round off
                    if (voucher.roundOff != 0.0) {
                        ledgerList.add(
                            LedgerEntry(
                                id = UUID.randomUUID().toString(),
                                accountHead = "Round Off Account",
                                voucherId = voucher.id,
                                date = voucher.date,
                                debit = if (voucher.roundOff > 0) voucher.roundOff else 0.0,
                                credit = if (voucher.roundOff < 0) -voucher.roundOff else 0.0,
                                narration = "Round Off adjust",
                                createdAt = System.currentTimeMillis()
                            )
                        )
                    }
                    // Purchase invoices stay payable to the supplier until a Payment voucher settles them.
                    val crHead = "Party: $partyDesc"
                    ledgerList.add(
                        LedgerEntry(
                            id = UUID.randomUUID().toString(),
                            accountHead = crHead,
                            voucherId = voucher.id,
                            date = voucher.date,
                            debit = 0.0,
                            credit = voucher.netAmount,
                            narration = "Purchase entry for voucher ${voucher.voucherNo}",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }

                "RECEIPT" -> {
                    // DR: Cash/Bank
                    val drHead = if (voucher.paymentMode == "CASH") "Cash" else "Bank"
                    ledgerList.add(
                        LedgerEntry(
                            id = UUID.randomUUID().toString(),
                            accountHead = drHead,
                            voucherId = voucher.id,
                            date = voucher.date,
                            debit = voucher.netAmount,
                            credit = 0.0,
                            narration = "Receipt entry",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    // CR: Party
                    ledgerList.add(
                        LedgerEntry(
                            id = UUID.randomUUID().toString(),
                            accountHead = "Party: $partyDesc",
                            voucherId = voucher.id,
                            date = voucher.date,
                            debit = 0.0,
                            credit = voucher.netAmount,
                            narration = "Receipt from $partyDesc",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }

                "PAYMENT" -> {
                    // DR: Party
                    ledgerList.add(
                        LedgerEntry(
                            id = UUID.randomUUID().toString(),
                            accountHead = "Party: $partyDesc",
                            voucherId = voucher.id,
                            date = voucher.date,
                            debit = voucher.netAmount,
                            credit = 0.0,
                            narration = "Payment to $partyDesc",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    // CR: Cash/Bank
                    val crHead = if (voucher.paymentMode == "CASH") "Cash" else "Bank"
                    ledgerList.add(
                        LedgerEntry(
                            id = UUID.randomUUID().toString(),
                            accountHead = crHead,
                            voucherId = voucher.id,
                            date = voucher.date,
                            debit = 0.0,
                            credit = voucher.netAmount,
                            narration = "Payment entry",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }

                "SALE_RETURN" -> {
                    // Reverse of SALE
                    // DR: Sales Return Account
                    ledgerList.add(
                        LedgerEntry(
                            id = UUID.randomUUID().toString(),
                            accountHead = "Sales Return Account",
                            voucherId = voucher.id,
                            date = voucher.date,
                            debit = voucher.taxableAmount,
                            credit = 0.0,
                            narration = "Sales Return debit",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    // DR: CGST / SGST / IGST Payable
                    if (voucher.isIgst) {
                        if (voucher.igst > 0) {
                            ledgerList.add(
                                LedgerEntry(
                                    id = UUID.randomUUID().toString(),
                                    accountHead = "IGST Payable",
                                    voucherId = voucher.id,
                                    date = voucher.date,
                                    debit = voucher.igst,
                                    credit = 0.0,
                                    narration = "IGST reversal",
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                    } else {
                        if (voucher.cgst > 0) {
                            ledgerList.add(
                                LedgerEntry(
                                    id = UUID.randomUUID().toString(),
                                    accountHead = "CGST Payable",
                                    voucherId = voucher.id,
                                    date = voucher.date,
                                    debit = voucher.cgst,
                                    credit = 0.0,
                                    narration = "CGST reversal",
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                        if (voucher.sgst > 0) {
                            ledgerList.add(
                                LedgerEntry(
                                    id = UUID.randomUUID().toString(),
                                    accountHead = "SGST Payable",
                                    voucherId = voucher.id,
                                    date = voucher.date,
                                    debit = voucher.sgst,
                                    credit = 0.0,
                                    narration = "SGST reversal",
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                    // CR: Party / Cash / Bank
                    val crHead = if (voucher.paymentMode == "CASH") "Cash" else if (voucher.paymentMode == "BANK" || voucher.paymentMode == "UPI") "Bank" else "Party: $partyDesc"
                    ledgerList.add(
                        LedgerEntry(
                            id = UUID.randomUUID().toString(),
                            accountHead = crHead,
                            voucherId = voucher.id,
                            date = voucher.date,
                            debit = 0.0,
                            credit = voucher.netAmount,
                            narration = "Sales Return entry for voucher ${voucher.voucherNo}",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }

                "PURCHASE_RETURN" -> {
                    // Reverse of PURCHASE
                    // DR: Party or Cash/Bank
                    val drHead = if (voucher.paymentMode == "CASH") "Cash" else if (voucher.paymentMode == "BANK" || voucher.paymentMode == "UPI") "Bank" else "Party: $partyDesc"
                    ledgerList.add(
                        LedgerEntry(
                            id = UUID.randomUUID().toString(),
                            accountHead = drHead,
                            voucherId = voucher.id,
                            date = voucher.date,
                            debit = voucher.netAmount,
                            credit = 0.0,
                            narration = "Purchase Return entry for ${voucher.voucherNo}",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    // CR: Purchase Return Account
                    ledgerList.add(
                        LedgerEntry(
                            id = UUID.randomUUID().toString(),
                            accountHead = "Purchase Return Account",
                            voucherId = voucher.id,
                            date = voucher.date,
                            debit = 0.0,
                            credit = voucher.taxableAmount,
                            narration = "Purchase Return credit",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    // CR: CGST / SGST / IGST Receivable
                    if (voucher.isIgst) {
                        if (voucher.igst > 0) {
                            ledgerList.add(
                                LedgerEntry(
                                    id = UUID.randomUUID().toString(),
                                    accountHead = "IGST Receivable",
                                    voucherId = voucher.id,
                                    date = voucher.date,
                                    debit = 0.0,
                                    credit = voucher.igst,
                                    narration = "IGST Receivable reversal",
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                    } else {
                        if (voucher.cgst > 0) {
                            ledgerList.add(
                                LedgerEntry(
                                    id = UUID.randomUUID().toString(),
                                    accountHead = "CGST Receivable",
                                    voucherId = voucher.id,
                                    date = voucher.date,
                                    debit = 0.0,
                                    credit = voucher.cgst,
                                    narration = "CGST Receivable reversal",
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                        if (voucher.sgst > 0) {
                            ledgerList.add(
                                LedgerEntry(
                                    id = UUID.randomUUID().toString(),
                                    accountHead = "SGST Receivable",
                                    voucherId = voucher.id,
                                    date = voucher.date,
                                    debit = 0.0,
                                    credit = voucher.sgst,
                                    narration = "SGST Receivable reversal",
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }

                "BILLS_RECEIVABLE" -> {
                    ledgerList.add(
                        LedgerEntry(
                            id = UUID.randomUUID().toString(),
                            accountHead = "Bills Receivable Account",
                            voucherId = voucher.id,
                            date = voucher.date,
                            debit = voucher.netAmount,
                            credit = 0.0,
                            narration = "Bills Receivable entry",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    ledgerList.add(
                        LedgerEntry(
                            id = UUID.randomUUID().toString(),
                            accountHead = "Party: $partyDesc",
                            voucherId = voucher.id,
                            date = voucher.date,
                            debit = 0.0,
                            credit = voucher.netAmount,
                            narration = "Bills Receivable from $partyDesc",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }

                "BILLS_PAYABLE" -> {
                    ledgerList.add(
                        LedgerEntry(
                            id = UUID.randomUUID().toString(),
                            accountHead = "Party: $partyDesc",
                            voucherId = voucher.id,
                            date = voucher.date,
                            debit = voucher.netAmount,
                            credit = 0.0,
                            narration = "Bills Payable to $partyDesc",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    ledgerList.add(
                        LedgerEntry(
                            id = UUID.randomUUID().toString(),
                            accountHead = "Bills Payable Account",
                            voucherId = voucher.id,
                            date = voucher.date,
                            debit = 0.0,
                            credit = voucher.netAmount,
                            narration = "Bills Payable entry",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            if (shouldPostAccounts && ledgerList.isNotEmpty()) {
                db.ledgerDao().insertLedgerEntries(
                    ledgerList.map { it.copy(financialYearCode = resolvedFinancialYearCode) }
                )
            }

            saveVoucherExtras(resolvedVoucher.id, resolvedVoucher.paymentMode, extras)

            // 4. Auto-register Cash or Bank trans
            val isReceipt = (resolvedVoucher.type == "RECEIPT" || resolvedVoucher.type == "SALE" || resolvedVoucher.type == "PURCHASE_RETURN")
            val isPayment = (resolvedVoucher.type == "PAYMENT" || resolvedVoucher.type == "SALE_RETURN")
            
            if (shouldPostAccounts && (isReceipt || isPayment) && resolvedVoucher.paymentMode != "CREDIT") {
                val txType = if (isReceipt) "RECEIPT" else "PAYMENT"
                val transactionMode = if (resolvedVoucher.paymentMode == "PART PAYMENT") extras.partialPaymentSubmode else resolvedVoucher.paymentMode
                val transactionAmount = if (resolvedVoucher.paymentMode == "PART PAYMENT") extras.partialAmountPaid else resolvedVoucher.netAmount
                if (transactionAmount > 0.0) {
                db.bankCashDao().insertTransaction(
                    BankCashTransaction(
                        id = UUID.randomUUID().toString(),
                        type = txType,
                        mode = transactionMode,
                        amount = transactionAmount,
                        date = voucher.date,
                        partyId = voucher.partyId,
                        partyName = partyDesc,
                        narration = "Auto-posted for voucher ${voucher.voucherNo}. ${voucher.narration}",
                        chequeNo = voucher.chequeNo,
                        chequeDate = voucher.chequeDate,
                        bankName = voucher.bankName,
                        receiptImagePath = null,
                        financialYearCode = resolvedFinancialYearCode,
                        createdAt = System.currentTimeMillis()
                    )
                )
                }
            }

            // 5. Auto-register Credit Sale under BillReceivable
            if (shouldPostAccounts && resolvedVoucher.type == "SALE" && (resolvedVoucher.paymentMode == "CREDIT" || resolvedVoucher.paymentMode == "PART PAYMENT") && resolvedVoucher.partyId != null) {
                val creditPeriodMs = 15L * 24L * 3600L * 1000L // 15 days credit term
                val dueDateVal = extras.creditDueDate.takeIf { it.isNotBlank() }?.let(::parseCreditDueDateToMillis)
                    ?: (resolvedVoucher.date + creditPeriodMs)
                val originalOutstanding = if (resolvedVoucher.paymentMode == "PART PAYMENT") extras.remainingCreditAmount else resolvedVoucher.netAmount
                val bill = BillReceivable(
                    id = UUID.randomUUID().toString(),
                    voucherId = resolvedVoucher.id,
                    voucherNo = resolvedVoucher.voucherNo,
                    partyId = resolvedVoucher.partyId,
                    partyName = partyDesc,
                    billDate = resolvedVoucher.date,
                    dueDate = dueDateVal,
                    originalAmount = resolvedVoucher.netAmount,
                    paidAmount = resolvedVoucher.netAmount - originalOutstanding,
                    outstandingAmount = originalOutstanding,
                    status = if (originalOutstanding < resolvedVoucher.netAmount) "PARTIAL" else "UNPAID",
                    daysOverdue = 0,
                    lastReminderDate = null,
                    financialYearCode = resolvedFinancialYearCode,
                    createdAt = System.currentTimeMillis()
                )
                db.billReceivableDao().insertBill(bill)
            }

            // 6. Recalculate everything
            recalculateOutstandings()
            recalculateProductStocks(resolvedFinancialYearCode)
            updatePartyAnalytics(resolvedFinancialYearCode)
        }
    }

    suspend fun recalculateOutstandings() {
        val yearCodes = db.financialYearDao().getAllFinancialYearsSync().map { it.code }.ifEmpty {
            listOf(FinancialYearUtils.currentFinancialYearCode())
        }
        val bills = yearCodes.flatMap { db.billReceivableDao().getAllBillsSync(it) }
        for (bill in bills) {
            val billAllocations = db.receiptAllocationDao().getAllocationsForInvoiceSync(bill.voucherId)
            val totalAllocated = billAllocations.sumOf { it.allocatedAmount }
            val sourceVoucher = db.voucherDao().getVoucherById(bill.voucherId)
            val initialPartPayment = if (sourceVoucher?.type == "SALE" && sourceVoucher.paymentMode == "PART PAYMENT") {
                getVoucherSaveExtras(bill.voucherId).partialAmountPaid.coerceAtLeast(0.0)
            } else {
                0.0
            }
            val totalPaid = (initialPartPayment + totalAllocated).coerceAtMost(bill.originalAmount)
            val outstanding = maxOf(0.0, bill.originalAmount - totalPaid)
            
            val status = if (outstanding <= 0.0) "PAID" else if (totalPaid > 0.0) "PARTIAL" else {
                if (bill.dueDate != null && System.currentTimeMillis() > bill.dueDate) "OVERDUE" else "UNPAID"
            }
            
            val daysOverdue = if (outstanding > 0.0 && bill.dueDate != null && System.currentTimeMillis() > bill.dueDate) {
                ((System.currentTimeMillis() - bill.dueDate) / (24L * 3600L * 1000L)).toInt()
            } else {
                0
            }
            
            val updatedBill = bill.copy(
                paidAmount = totalPaid,
                outstandingAmount = outstanding,
                status = status,
                daysOverdue = daysOverdue
            )
            db.billReceivableDao().insertBill(updatedBill)
            
            // Sync with Voucher
            val v = db.voucherDao().getVoucherById(bill.voucherId)
            if (v != null) {
                val voucherExtras = getVoucherSaveExtras(v.id)
                db.voucherDao().insertVoucher(v.copy(outstandingAmount = outstanding))
                saveVoucherExtras(v.id, v.paymentMode, voucherExtras)
                db.openHelper.writableDatabase.execSQL(
                    "UPDATE vouchers SET remaining_credit_amount = ? WHERE id = ?",
                    arrayOf<Any?>(outstanding, bill.voucherId)
                )
            }
        }

        val purchaseVoucherIds = mutableListOf<String>()
        val purchaseCursor = db.openHelper.readableDatabase.query(
            "SELECT id FROM vouchers WHERE type = ? AND status = ? AND partyId IS NOT NULL",
            arrayOf("PURCHASE", "POSTED")
        )
        purchaseCursor.use { cursor ->
            while (cursor.moveToNext()) {
                purchaseVoucherIds += cursor.getString(0)
            }
        }
        for (purchaseVoucherId in purchaseVoucherIds) {
            val purchaseVoucher = db.voucherDao().getVoucherById(purchaseVoucherId) ?: continue
            val allocations = db.receiptAllocationDao().getAllocationsForInvoiceSync(purchaseVoucher.id)
            val allocatedAmount = allocations.sumOf { it.allocatedAmount }
            val outstanding = maxOf(0.0, purchaseVoucher.netAmount - allocatedAmount)
            if (purchaseVoucher.outstandingAmount != outstanding) {
                val voucherExtras = getVoucherSaveExtras(purchaseVoucher.id)
                db.voucherDao().insertVoucher(purchaseVoucher.copy(outstandingAmount = outstanding))
                saveVoucherExtras(purchaseVoucher.id, purchaseVoucher.paymentMode, voucherExtras)
            }
        }
    }

    suspend fun deleteVoucher(id: String) {
        db.withTransaction {
            val voucher = db.voucherDao().getVoucherById(id)
            db.voucherDao().deleteVoucher(id)
            db.voucherItemDao().deleteItemsForVoucher(id)
            db.ledgerDao().deleteLedgerEntriesForVoucher(id)
            db.bankCashDao().deleteTransactionsByVoucher(id)
            db.receiptAllocationDao().deleteAllocationsByReceipt(id)
            db.receiptAllocationDao().deleteAllocationsByInvoice(id)
            db.billReceivableDao().deleteBillByVoucherId(id)
            recalculateOutstandings()
            voucher?.financialYearCode?.let {
                recalculateProductStocks(it)
                updatePartyAnalytics(it)
            }
        }
    }

    suspend fun saveJournalVoucher(
        voucher: Voucher,
        lines: List<JournalLine>
    ) {
        db.withTransaction {
            val resolvedFinancialYearCode = voucher.financialYearCode.ifBlank { getFinancialYear(voucher.date) }
            ensureFinancialYearExists(resolvedFinancialYearCode)
            val yearState = db.financialYearDao().getFinancialYearByCode(resolvedFinancialYearCode)
            require(yearState?.isLocked != true) { "Financial year $resolvedFinancialYearCode is locked." }

            db.voucherDao().deleteVoucher(voucher.id)
            db.voucherItemDao().deleteItemsForVoucher(voucher.id)
            db.ledgerDao().deleteLedgerEntriesForVoucher(voucher.id)
            db.bankCashDao().deleteTransactionsByVoucher(voucher.id)

            db.voucherDao().insertVoucher(voucher.copy(type = "JOURNAL", financialYearCode = resolvedFinancialYearCode))
            db.ledgerDao().insertLedgerEntries(
                lines.flatMap { line ->
                    listOf(
                        LedgerEntry(
                            id = UUID.randomUUID().toString(),
                            accountHead = line.accountHead,
                            voucherId = voucher.id,
                            date = voucher.date,
                            debit = line.debit,
                            credit = line.credit,
                            narration = voucher.narration.ifBlank { "Journal Entry ${voucher.voucherNo}" },
                            financialYearCode = resolvedFinancialYearCode,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }
            )
        }
    }

    private suspend fun recalculateProductStocks(financialYearCode: String) {
        val products = db.productDao().getAllProductsSync()
        val vouchersById = db.voucherDao().getAllVouchersForYearSync(financialYearCode).associateBy { it.id }
        val openingBalances = db.productFinancialYearBalanceDao()
            .getBalancesForYearSync(financialYearCode)
            .associateBy { it.productId }
        val movements = db.voucherItemDao().getAllItemsForYearSync(financialYearCode)
            .groupBy { it.productId }
            .mapValues { (_, productItems) ->
                productItems.sumOf { item ->
                    when (vouchersById[item.voucherId]?.type) {
                        "PURCHASE", "SALE_RETURN" -> item.qty
                        "SALE", "PURCHASE_RETURN" -> -item.qty
                        else -> 0.0
                    }
                }
            }

        products.forEach { product ->
            val openingStock = openingBalances[product.id]?.openingStock ?: product.openingStock
            val currentStock = (openingStock + (movements[product.id] ?: 0.0)).coerceAtLeast(0.0)
            db.openHelper.writableDatabase.execSQL(
                """
                UPDATE products
                SET current_stock = ?,
                    stock_unit = ?
                WHERE id = ?
                """.trimIndent(),
                arrayOf<Any?>(currentStock, product.stockUnit.ifBlank { product.unit }, product.id)
            )
        }
    }

    private suspend fun updatePartyAnalytics(financialYearCode: String) {
        val formatter = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
        val saleVouchersByParty = db.voucherDao().getAllVouchersForYearSync(financialYearCode)
            .filter { it.type == "SALE" && it.partyId != null }
            .groupBy { it.partyId.orEmpty() }

        db.partyDao().getAllPartiesSync().forEach { party ->
            val partySales = saleVouchersByParty[party.id].orEmpty().sortedBy { it.date }
            val totalPurchasesAmount = partySales.sumOf { it.netAmount }
            db.openHelper.writableDatabase.execSQL(
                """
                UPDATE parties
                SET total_purchases_amount = ?,
                    total_transactions = ?,
                    first_transaction_date = ?,
                    last_transaction_date = ?,
                    loyalty_points = ?
                WHERE id = ?
                """.trimIndent(),
                arrayOf<Any?>(
                    totalPurchasesAmount,
                    partySales.size,
                    partySales.firstOrNull()?.let { formatter.format(it.date) }.orEmpty(),
                    partySales.lastOrNull()?.let { formatter.format(it.date) }.orEmpty(),
                    (totalPurchasesAmount / 100.0).toInt(),
                    party.id
                )
            )
        }
    }

    private fun saveVoucherExtras(voucherId: String, paymentMode: String, extras: VoucherSaveExtras) {
        db.openHelper.writableDatabase.execSQL(
            """
            UPDATE vouchers
            SET payment_mode = ?,
                partial_amount_paid = ?,
                partial_payment_submode = ?,
                credit_due_date = ?,
                remaining_credit_amount = ?,
                is_advance = ?,
                advance_for = ?,
                reference_no = ?,
                other_references = ?
            WHERE id = ?
            """.trimIndent(),
            arrayOf<Any?>(
                paymentMode,
                extras.partialAmountPaid,
                extras.partialPaymentSubmode,
                extras.creditDueDate,
                extras.remainingCreditAmount,
                if (extras.isAdvance) 1 else 0,
                extras.advanceFor,
                extras.referenceNo,
                extras.otherReferences,
                voucherId
            )
        )
    }

    private fun parseCreditDueDateToMillis(raw: String): Long? {
        raw.toLongOrNull()?.let { return it }
        val patterns = listOf("dd-MMM-yyyy", "dd-MMM-yy", "dd-MM-yyyy")
        return patterns.firstNotNullOfOrNull { pattern ->
            runCatching {
                SimpleDateFormat(pattern, Locale.ENGLISH).parse(raw)?.time
            }.getOrNull()
        }?.let { parsed ->
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = parsed
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            calendar.timeInMillis
        }
    }

    // Direct Cash/Bank manual transaction
    suspend fun saveBankCashTransaction(transaction: BankCashTransaction) {
        db.withTransaction {
            val resolvedFinancialYearCode = transaction.financialYearCode.ifBlank { getFinancialYear(transaction.date) }
            ensureFinancialYearExists(resolvedFinancialYearCode)
            val yearState = db.financialYearDao().getFinancialYearByCode(resolvedFinancialYearCode)
            require(yearState?.isLocked != true) { "Financial year $resolvedFinancialYearCode is locked." }
            val resolvedTransaction = transaction.copy(financialYearCode = resolvedFinancialYearCode)
            db.bankCashDao().insertTransaction(resolvedTransaction)
            
            // Post manual bank/cash to ledger entries
            val drHead = if (resolvedTransaction.type == "RECEIPT") {
                if (resolvedTransaction.mode == "CASH") "Cash" else "Bank"
            } else {
                "Party: ${resolvedTransaction.partyName ?: "General"}"
            }
            val crHead = if (resolvedTransaction.type == "RECEIPT") {
                "Party: ${resolvedTransaction.partyName ?: "General"}"
            } else {
                if (resolvedTransaction.mode == "CASH") "Cash" else "Bank"
            }

            val ledgerEntries = listOf(
                LedgerEntry(
                    id = UUID.randomUUID().toString(),
                    accountHead = drHead,
                    voucherId = resolvedTransaction.id,
                    date = resolvedTransaction.date,
                    debit = resolvedTransaction.amount,
                    credit = 0.0,
                    narration = resolvedTransaction.narration,
                    financialYearCode = resolvedFinancialYearCode,
                    createdAt = System.currentTimeMillis()
                ),
                LedgerEntry(
                    id = UUID.randomUUID().toString(),
                    accountHead = crHead,
                    voucherId = resolvedTransaction.id,
                    date = resolvedTransaction.date,
                    debit = 0.0,
                    credit = resolvedTransaction.amount,
                    narration = resolvedTransaction.narration,
                    financialYearCode = resolvedFinancialYearCode,
                    createdAt = System.currentTimeMillis()
                )
            )
            db.ledgerDao().insertLedgerEntries(ledgerEntries)
        }
    }

    suspend fun deleteTransaction(id: String) {
        db.withTransaction {
            db.bankCashDao().deleteTransaction(id)
            db.ledgerDao().deleteLedgerEntriesForVoucher(id)
        }
    }

    suspend fun closeFinancialYear(
        sourceFinancialYearCode: String,
        targetFinancialYearCode: String = FinancialYearUtils.nextFinancialYear(sourceFinancialYearCode),
        lockSourceYear: Boolean
    ): FinancialYearCloseResult {
        return db.withTransaction {
            ensureFinancialYearExists(sourceFinancialYearCode)
            ensureFinancialYearExists(targetFinancialYearCode, sourceFinancialYearCode)

            val sourceYear = db.financialYearDao().getFinancialYearByCode(sourceFinancialYearCode)
                ?: error("Source financial year not found")
            check(!sourceYear.isLocked) { "Financial year $sourceFinancialYearCode is already locked." }

            val parties = db.partyDao().getAllPartiesSync()
            val partyBalances = db.partyFinancialYearBalanceDao().getBalancesForYearSync(sourceFinancialYearCode)
                .associateBy { it.partyId }
            val products = db.productDao().getAllProductsSync()
            val productBalances = db.productFinancialYearBalanceDao().getBalancesForYearSync(sourceFinancialYearCode)
                .associateBy { it.productId }
            val ledgerAccounts = db.ledgerAccountDao().getAllLedgerAccountsSync()
            val ledgerBalanceRows = db.ledgerAccountFinancialYearBalanceDao().getBalancesForYearSync(sourceFinancialYearCode)
                .associateBy { it.accountId }
            val ledgerEntries = db.ledgerDao().getAllLedgerEntriesForYearSync(sourceFinancialYearCode)
            val vouchers = db.voucherDao().getAllVouchersForYearSync(sourceFinancialYearCode)
            val voucherMap = vouchers.associateBy { it.id }
            val voucherItems = db.voucherItemDao().getAllItemsForYearSync(sourceFinancialYearCode)

            val closingLedgerBalances = mutableMapOf<String, Double>()
            ledgerAccounts.forEach { account ->
                val openingRow = ledgerBalanceRows[account.id]
                val openingSigned = signedAmount(
                    amount = openingRow?.openingBalance ?: account.openingBalance,
                    balanceType = openingRow?.balanceType ?: account.balanceType
                )
                closingLedgerBalances[account.id] = openingSigned
            }
            ledgerEntries.forEach { entry ->
                val accountId = ledgerAccounts.find { it.name == entry.accountHead }?.id ?: return@forEach
                closingLedgerBalances[accountId] = (closingLedgerBalances[accountId] ?: 0.0) + (entry.debit - entry.credit)
            }

            val nextLedgerBalances = ledgerAccounts.map { account ->
                val closingSigned = closingLedgerBalances[account.id] ?: 0.0
                LedgerAccountFinancialYearBalance(
                    accountId = account.id,
                    financialYearCode = targetFinancialYearCode,
                    openingBalance = kotlin.math.abs(closingSigned),
                    balanceType = if (closingSigned < 0) "CR" else "DR"
                )
            }
            db.ledgerAccountFinancialYearBalanceDao().upsertBalances(nextLedgerBalances)

            val nextPartyBalances = parties.map { party ->
                val openingRow = partyBalances[party.id]
                val signedOpening = signedAmount(
                    amount = openingRow?.openingBalance ?: party.openingBalance,
                    balanceType = openingRow?.balanceType ?: party.balanceType
                )
                val movement = ledgerEntries
                    .filter { it.accountHead == "Party: ${party.name}" }
                    .sumOf { it.debit - it.credit }
                val closingSigned = signedOpening + movement
                PartyFinancialYearBalance(
                    partyId = party.id,
                    financialYearCode = targetFinancialYearCode,
                    openingBalance = kotlin.math.abs(closingSigned),
                    balanceType = if (closingSigned < 0) "CR" else "DR"
                )
            }
            db.partyFinancialYearBalanceDao().upsertBalances(nextPartyBalances)

            val nextProductBalances = products.map { product ->
                val openingRow = productBalances[product.id]
                val openingStock = openingRow?.openingStock ?: product.openingStock
                val movementQty = voucherItems
                    .filter { it.productId == product.id }
                    .sumOf { item ->
                        when (voucherMap[item.voucherId]?.type) {
                            "PURCHASE", "PURCHASE_RETURN_IN" -> item.qty
                            "SALE_RETURN" -> item.qty
                            "SALE", "PURCHASE_RETURN" -> -item.qty
                            else -> 0.0
                        }
                    }
                val closingQty = (openingStock + movementQty).coerceAtLeast(0.0)
                ProductFinancialYearBalance(
                    productId = product.id,
                    financialYearCode = targetFinancialYearCode,
                    openingStock = closingQty,
                    openingStockValue = closingQty * product.purchaseRate
                )
            }
            db.productFinancialYearBalanceDao().upsertBalances(nextProductBalances)

            val completedLog = db.financialYearAuditLogDao().getLog(
                financialYearCode = sourceFinancialYearCode,
                targetFinancialYearCode = targetFinancialYearCode,
                action = "YEAR_CLOSE_COMPLETED"
            )
            if (completedLog == null) {
                db.financialYearAuditLogDao().insertLog(
                    FinancialYearAuditLog(
                        id = UUID.randomUUID().toString(),
                        action = "YEAR_CLOSE_COMPLETED",
                        financialYearCode = sourceFinancialYearCode,
                        targetFinancialYearCode = targetFinancialYearCode,
                        detailsJson = JSONObject()
                            .put("inventoryItemsCarried", nextProductBalances.count())
                            .put("partyBalancesCarried", nextPartyBalances.count())
                            .put("ledgerBalancesCarried", nextLedgerBalances.count())
                            .put("lockedSourceYear", lockSourceYear)
                            .toString()
                    )
                )
            }

            val closedAt = System.currentTimeMillis()
            db.financialYearDao().updateYearStatus(
                code = sourceFinancialYearCode,
                isClosed = true,
                isLocked = lockSourceYear,
                closedAt = closedAt,
                lockedAt = if (lockSourceYear) closedAt else null
            )

            FinancialYearCloseResult(
                sourceFinancialYearCode = sourceFinancialYearCode,
                targetFinancialYearCode = targetFinancialYearCode,
                inventoryItemsCarried = nextProductBalances.count(),
                partyBalancesCarried = nextPartyBalances.count(),
                ledgerBalancesCarried = nextLedgerBalances.count(),
                lockedSourceYear = lockSourceYear
            )
        }
    }

    private fun signedAmount(amount: Double, balanceType: String): Double =
        if (balanceType.equals("CR", ignoreCase = true)) -amount else amount

    suspend fun insertSampleData() {
        seedLedgersIfEmpty()
        db.withTransaction {
            // Drop tables state (can be destructive or append)
            // Customers
            val p1 = Party(
                id = UUID.randomUUID().toString(),
                name = "Agarwal Distributors",
                type = "CUSTOMER",
                phone = "9876543210",
                email = "agarwal@test.com",
                address = "12/A MG Road",
                city = "Kolkata",
                state = "West Bengal",
                stateCode = "19",
                gstin = "19AAACA1234A1Z1",
                pan = "AAACA1234A",
                openingBalance = 5000.0,
                balanceType = "DR"
            )
            val p2 = Party(
                id = UUID.randomUUID().toString(),
                name = "Sharma Electronics",
                type = "CUSTOMER",
                phone = "9870011223",
                email = "sharma@test.com",
                address = "56 Sector 18",
                city = "Gurugram",
                state = "Haryana",
                stateCode = "06",
                gstin = "06AAACT9182K2Z0",
                pan = "AAACT9182K",
                openingBalance = 0.0,
                balanceType = "DR"
            )
            // Supplier
            val p3 = Party(
                id = UUID.randomUUID().toString(),
                name = "Bharat Wholesalers",
                type = "SUPPLIER",
                phone = "9001122334",
                email = "contact@bharat.com",
                address = "Flat 4, Link Road",
                city = "Mumbai",
                state = "Maharashtra",
                stateCode = "27",
                gstin = "27AAACB3412B1Z8",
                pan = "AAACB3412B",
                openingBalance = 15000.0,
                balanceType = "CR"
            )

            db.partyDao().insertParty(p1)
            db.partyDao().insertParty(p2)
            db.partyDao().insertParty(p3)

            // Products
            val pr1 = Product(
                id = UUID.randomUUID().toString(),
                name = "Steel Basin 18-inch",
                hsnCode = "73241000",
                unit = "PCS",
                saleRate = 1200.0,
                purchaseRate = 850.0,
                gstRate = 18.0,
                openingStock = 50.0
            )
            val pr2 = Product(
                id = UUID.randomUUID().toString(),
                name = "LED Tube Light 20W",
                hsnCode = "85395000",
                unit = "PCS",
                saleRate = 250.0,
                purchaseRate = 170.0,
                gstRate = 12.0,
                openingStock = 120.0
            )
            val pr3 = Product(
                id = UUID.randomUUID().toString(),
                name = "Organic Detergent Powder",
                hsnCode = "34022010",
                unit = "KG",
                saleRate = 180.0,
                purchaseRate = 120.0,
                gstRate = 5.0,
                openingStock = 200.0
            )

            db.productDao().insertProduct(pr1)
            db.productDao().insertProduct(pr2)
            db.productDao().insertProduct(pr3)

            // Dynamic setup can create some vouchers using helper post logic
            // Add a sale (Intrastate - same state, let's assume business is West Bengal (19))
            // We'll code sample vouchers assuming business state is West Bengal
            val v1Id = UUID.randomUUID().toString()
            val v1 = Voucher(
                id = v1Id,
                voucherNo = "SAL/2026-27/0001",
                type = "SALE",
                date = System.currentTimeMillis() - 5 * 24 * 3600 * 1000, // 5 days ago
                partyId = p1.id,
                narration = "Sales to Agarwal Distributors",
                taxableAmount = 2400.0,
                cgst = 216.0,
                sgst = 216.0,
                igst = 0.0,
                roundOff = 0.0,
                netAmount = 2832.0,
                paymentMode = "BANK",
                chequeNo = null,
                chequeDate = null,
                bankName = null,
                isIgst = false,
                status = "POSTED"
            )
            val v1Items = listOf(
                VoucherItem(
                    id = UUID.randomUUID().toString(),
                    voucherId = v1Id,
                    productId = pr1.id,
                    productName = pr1.name,
                    hsnCode = pr1.hsnCode,
                    qty = 2.0,
                    unit = pr1.unit,
                    rate = 1200.0,
                    discount = 0.0,
                    discountType = "PERCENT",
                    taxableAmount = 2400.0,
                    gstRate = 18.0,
                    cgstAmount = 216.0,
                    sgstAmount = 216.0,
                    igstAmount = 0.0,
                    totalAmount = 2832.0
                )
            )
            saveAndPostVoucher(v1, v1Items, p1.name)

            // Add an Interstate Sale (Haryana (06) from West Bengal (19))
            val v2Id = UUID.randomUUID().toString()
            val v2 = Voucher(
                id = v2Id,
                voucherNo = "SAL/2026-27/0002",
                type = "SALE",
                date = System.currentTimeMillis() - 3 * 24 * 3600 * 1000,
                partyId = p2.id,
                narration = "Interstate sales to Sharma Electronics",
                taxableAmount = 500.0,
                cgst = 0.0,
                sgst = 0.0,
                igst = 60.0,
                roundOff = 0.0,
                netAmount = 560.0,
                paymentMode = "UPI",
                chequeNo = null,
                chequeDate = null,
                bankName = null,
                isIgst = true,
                status = "POSTED"
            )
            val v2Items = listOf(
                VoucherItem(
                    id = UUID.randomUUID().toString(),
                    voucherId = v2Id,
                    productId = pr2.id,
                    productName = pr2.name,
                    hsnCode = pr2.hsnCode,
                    qty = 2.0,
                    unit = pr2.unit,
                    rate = 250.0,
                    discount = 0.0,
                    discountType = "PERCENT",
                    taxableAmount = 500.0,
                    gstRate = 12.0,
                    cgstAmount = 0.0,
                    sgstAmount = 0.0,
                    igstAmount = 60.0,
                    totalAmount = 560.0
                )
            )
            saveAndPostVoucher(v2, v2Items, p2.name)

            // Add a purchase
            val v3Id = UUID.randomUUID().toString()
            val v3 = Voucher(
                id = v3Id,
                voucherNo = "PUR/2026-27/0001",
                type = "PURCHASE",
                date = System.currentTimeMillis() - 1 * 24 * 3600 * 1000,
                partyId = p3.id,
                narration = "Stock purchase from Bharat Wholesalers",
                taxableAmount = 8500.0,
                cgst = 0.0,
                sgst = 0.0,
                igst = 1530.0,
                roundOff = 0.0,
                netAmount = 10030.0,
                paymentMode = "CASH",
                chequeNo = null,
                chequeDate = null,
                bankName = null,
                isIgst = true,
                status = "POSTED"
            )
            val v3Items = listOf(
                VoucherItem(
                    id = UUID.randomUUID().toString(),
                    voucherId = v3Id,
                    productId = pr1.id,
                    productName = pr1.name,
                    hsnCode = pr1.hsnCode,
                    qty = 10.0,
                    unit = pr1.unit,
                    rate = 850.0,
                    discount = 0.0,
                    discountType = "PERCENT",
                    taxableAmount = 8500.0,
                    gstRate = 18.0,
                    cgstAmount = 0.0,
                    sgstAmount = 0.0,
                    igstAmount = 1530.0,
                    totalAmount = 10030.0
                )
            )
            saveAndPostVoucher(v3, v3Items, p3.name)

            // Add a Receipt
            val v4Id = UUID.randomUUID().toString()
            val v4 = Voucher(
                id = v4Id,
                voucherNo = "RCP/2026-27/0001",
                type = "RECEIPT",
                date = System.currentTimeMillis(),
                partyId = p1.id,
                narration = "On account payment from Agarwal Distributors",
                taxableAmount = 0.0,
                cgst = 0.0,
                sgst = 0.0,
                igst = 0.0,
                roundOff = 0.0,
                netAmount = 2000.0,
                paymentMode = "UPI",
                chequeNo = null,
                chequeDate = null,
                bankName = null,
                isIgst = false,
                status = "POSTED"
            )
            saveAndPostVoucher(v4, emptyList(), p1.name)
        }
    }
}
