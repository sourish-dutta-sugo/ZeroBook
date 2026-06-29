package com.zerobook.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessProfileDao {
    @Query("SELECT * FROM business_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<BusinessProfile?>

    @Query("SELECT * FROM business_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): BusinessProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: BusinessProfile)
}

@Dao
interface FinancialYearDao {
    @Query("SELECT * FROM financial_years ORDER BY startDate DESC")
    fun getAllFinancialYears(): Flow<List<FinancialYear>>

    @Query("SELECT * FROM financial_years ORDER BY startDate DESC")
    suspend fun getAllFinancialYearsSync(): List<FinancialYear>

    @Query("SELECT * FROM financial_years WHERE code = :code LIMIT 1")
    suspend fun getFinancialYearByCode(code: String): FinancialYear?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinancialYear(financialYear: FinancialYear)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinancialYears(financialYears: List<FinancialYear>)

    @Query(
        "UPDATE financial_years SET isClosed = :isClosed, isLocked = :isLocked, closedAt = :closedAt, lockedAt = :lockedAt WHERE code = :code"
    )
    suspend fun updateYearStatus(
        code: String,
        isClosed: Boolean,
        isLocked: Boolean,
        closedAt: Long?,
        lockedAt: Long?
    )
}

@Dao
interface PartyDao {
    @Query("SELECT * FROM parties ORDER BY name ASC")
    fun getAllParties(): Flow<List<Party>>

    @Query("SELECT * FROM parties ORDER BY name ASC")
    suspend fun getAllPartiesSync(): List<Party>

    @Query("SELECT * FROM parties WHERE id = :id LIMIT 1")
    suspend fun getPartyById(id: String): Party?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(party: Party)

    @Query("DELETE FROM parties WHERE id = :id")
    suspend fun deleteParty(id: String)
}

@Dao
interface PartyFinancialYearBalanceDao {
    @Query("SELECT * FROM party_financial_year_balances WHERE financialYearCode = :financialYearCode")
    fun getBalancesForYear(financialYearCode: String): Flow<List<PartyFinancialYearBalance>>

    @Query("SELECT * FROM party_financial_year_balances WHERE financialYearCode = :financialYearCode")
    suspend fun getBalancesForYearSync(financialYearCode: String): List<PartyFinancialYearBalance>

    @Query("SELECT * FROM party_financial_year_balances WHERE partyId = :partyId AND financialYearCode = :financialYearCode LIMIT 1")
    suspend fun getBalance(partyId: String, financialYearCode: String): PartyFinancialYearBalance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBalance(balance: PartyFinancialYearBalance)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBalances(balances: List<PartyFinancialYearBalance>)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllProductsSync(): List<Product>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: String)
}

@Dao
interface ProductFinancialYearBalanceDao {
    @Query("SELECT * FROM product_financial_year_balances WHERE financialYearCode = :financialYearCode")
    fun getBalancesForYear(financialYearCode: String): Flow<List<ProductFinancialYearBalance>>

    @Query("SELECT * FROM product_financial_year_balances WHERE financialYearCode = :financialYearCode")
    suspend fun getBalancesForYearSync(financialYearCode: String): List<ProductFinancialYearBalance>

    @Query("SELECT * FROM product_financial_year_balances WHERE productId = :productId AND financialYearCode = :financialYearCode LIMIT 1")
    suspend fun getBalance(productId: String, financialYearCode: String): ProductFinancialYearBalance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBalance(balance: ProductFinancialYearBalance)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBalances(balances: List<ProductFinancialYearBalance>)
}

@Dao
interface VoucherDao {
    @Query("SELECT * FROM vouchers WHERE financialYearCode = :financialYearCode ORDER BY date DESC, createdAt DESC")
    fun getAllVouchersForYear(financialYearCode: String): Flow<List<Voucher>>

    @Query("SELECT * FROM vouchers WHERE financialYearCode = :financialYearCode ORDER BY date DESC, createdAt DESC")
    suspend fun getAllVouchersForYearSync(financialYearCode: String): List<Voucher>

    @Query("SELECT * FROM vouchers WHERE id = :id LIMIT 1")
    suspend fun getVoucherById(id: String): Voucher?

    @Query("SELECT * FROM vouchers WHERE type = :type AND financialYearCode = :financialYearCode ORDER BY date DESC, createdAt DESC")
    fun getVouchersByType(type: String, financialYearCode: String): Flow<List<Voucher>>

    @Query("SELECT voucherNo FROM vouchers WHERE type = :type AND financialYearCode = :financialYearCode AND voucherNo LIKE :pattern ORDER BY voucherNo DESC LIMIT 1")
    suspend fun getLatestVoucherNo(type: String, financialYearCode: String, pattern: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: Voucher)

    @Query("DELETE FROM vouchers WHERE id = :id")
    suspend fun deleteVoucher(id: String)
}

@Dao
interface VoucherItemDao {
    @Query("SELECT * FROM voucher_items WHERE voucherId = :voucherId")
    fun getItemsForVoucher(voucherId: String): Flow<List<VoucherItem>>

    @Query("SELECT * FROM voucher_items WHERE voucherId = :voucherId")
    suspend fun getItemsForVoucherSync(voucherId: String): List<VoucherItem>

    @Query("SELECT * FROM voucher_items WHERE financialYearCode = :financialYearCode")
    suspend fun getAllItemsForYearSync(financialYearCode: String): List<VoucherItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<VoucherItem>)

    @Query("DELETE FROM voucher_items WHERE voucherId = :voucherId")
    suspend fun deleteItemsForVoucher(voucherId: String)
}

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledger_entries WHERE financialYearCode = :financialYearCode ORDER BY date DESC, createdAt DESC")
    fun getAllLedgerEntriesForYear(financialYearCode: String): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE financialYearCode = :financialYearCode ORDER BY date DESC, createdAt DESC")
    suspend fun getAllLedgerEntriesForYearSync(financialYearCode: String): List<LedgerEntry>

    @Query("SELECT * FROM ledger_entries WHERE accountHead = :accountHead AND financialYearCode = :financialYearCode ORDER BY date DESC, createdAt DESC")
    fun getLedgerEntriesByAccount(accountHead: String, financialYearCode: String): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE voucherId = :voucherId")
    suspend fun getLedgerEntriesByVoucherId(voucherId: String): List<LedgerEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntries(entries: List<LedgerEntry>)

    @Query("DELETE FROM ledger_entries WHERE voucherId = :voucherId")
    suspend fun deleteLedgerEntriesForVoucher(voucherId: String)
}

@Dao
interface BankCashDao {
    @Query("SELECT * FROM bank_cash_transactions WHERE financialYearCode = :financialYearCode ORDER BY date DESC, createdAt DESC")
    fun getAllTransactionsForYear(financialYearCode: String): Flow<List<BankCashTransaction>>

    @Query("SELECT * FROM bank_cash_transactions WHERE financialYearCode = :financialYearCode ORDER BY date DESC, createdAt DESC")
    suspend fun getAllTransactionsForYearSync(financialYearCode: String): List<BankCashTransaction>

    @Query("SELECT * FROM bank_cash_transactions WHERE financialYearCode = :financialYearCode AND mode = 'CASH' ORDER BY date DESC, createdAt DESC")
    fun getCashTransactions(financialYearCode: String): Flow<List<BankCashTransaction>>

    @Query("SELECT * FROM bank_cash_transactions WHERE financialYearCode = :financialYearCode AND mode != 'CASH' ORDER BY date DESC, createdAt DESC")
    fun getBankTransactions(financialYearCode: String): Flow<List<BankCashTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: BankCashTransaction)

    @Query("DELETE FROM bank_cash_transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)

    @Query("DELETE FROM bank_cash_transactions WHERE narration LIKE '%' || :voucherId || '%'")
    suspend fun deleteTransactionsByVoucher(voucherId: String)
}

@Dao
interface ReceiptAllocationDao {
    @Query("SELECT * FROM receipt_allocations WHERE receiptId = :receiptId")
    fun getAllocationsForReceipt(receiptId: String): Flow<List<ReceiptAllocation>>

    @Query("SELECT * FROM receipt_allocations WHERE financialYearCode = :financialYearCode")
    fun getAllReceiptAllocations(financialYearCode: String): Flow<List<ReceiptAllocation>>

    @Query("SELECT * FROM receipt_allocations WHERE invoiceId = :invoiceId")
    suspend fun getAllocationsForInvoiceSync(invoiceId: String): List<ReceiptAllocation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocation(allocation: ReceiptAllocation)

    @Query("DELETE FROM receipt_allocations WHERE receiptId = :receiptId")
    suspend fun deleteAllocationsByReceipt(receiptId: String)

    @Query("DELETE FROM receipt_allocations WHERE invoiceId = :invoiceId")
    suspend fun deleteAllocationsByInvoice(invoiceId: String)
}

@Dao
interface LedgerAccountDao {
    @Query("SELECT * FROM ledger_accounts ORDER BY name ASC")
    fun getAllLedgerAccounts(): Flow<List<LedgerAccount>>

    @Query("SELECT * FROM ledger_accounts ORDER BY name ASC")
    suspend fun getAllLedgerAccountsSync(): List<LedgerAccount>

    @Query("SELECT * FROM ledger_accounts WHERE id = :id LIMIT 1")
    suspend fun getLedgerAccountById(id: String): LedgerAccount?

    @Query("SELECT * FROM ledger_accounts WHERE name = :name LIMIT 1")
    suspend fun getLedgerAccountByName(name: String): LedgerAccount?

    @Query("SELECT * FROM ledger_accounts WHERE partyId = :partyId LIMIT 1")
    suspend fun getLedgerAccountByPartyId(partyId: String): LedgerAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerAccount(account: LedgerAccount)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerAccounts(accounts: List<LedgerAccount>)

    @Query("UPDATE ledger_accounts SET openingBalance = :balance WHERE id = :id")
    suspend fun updateOpeningBalance(id: String, balance: Double)

    @Query("DELETE FROM ledger_accounts WHERE id = :id")
    suspend fun deleteLedgerAccount(id: String)

    @Query("DELETE FROM ledger_accounts WHERE partyId = :partyId")
    suspend fun deleteLedgerAccountByParty(partyId: String)
}

@Dao
interface LedgerAccountFinancialYearBalanceDao {
    @Query("SELECT * FROM ledger_account_financial_year_balances WHERE financialYearCode = :financialYearCode")
    fun getBalancesForYear(financialYearCode: String): Flow<List<LedgerAccountFinancialYearBalance>>

    @Query("SELECT * FROM ledger_account_financial_year_balances WHERE financialYearCode = :financialYearCode")
    suspend fun getBalancesForYearSync(financialYearCode: String): List<LedgerAccountFinancialYearBalance>

    @Query("SELECT * FROM ledger_account_financial_year_balances WHERE accountId = :accountId AND financialYearCode = :financialYearCode LIMIT 1")
    suspend fun getBalance(accountId: String, financialYearCode: String): LedgerAccountFinancialYearBalance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBalance(balance: LedgerAccountFinancialYearBalance)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBalances(balances: List<LedgerAccountFinancialYearBalance>)
}

@Dao
interface BillReceivableDao {
    @Query("SELECT * FROM bills_receivable WHERE financialYearCode = :financialYearCode ORDER BY billDate DESC")
    fun getAllBills(financialYearCode: String): Flow<List<BillReceivable>>

    @Query("SELECT * FROM bills_receivable WHERE financialYearCode = :financialYearCode ORDER BY billDate DESC")
    suspend fun getAllBillsSync(financialYearCode: String): List<BillReceivable>

    @Query("SELECT * FROM bills_receivable WHERE voucherId = :voucherId LIMIT 1")
    suspend fun getBillByVoucherId(voucherId: String): BillReceivable?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillReceivable)

    @Query("DELETE FROM bills_receivable WHERE voucherId = :voucherId")
    suspend fun deleteBillByVoucherId(voucherId: String)
}

@Dao
interface FinancialYearAuditLogDao {
    @Query("SELECT * FROM financial_year_audit_logs WHERE financialYearCode = :financialYearCode ORDER BY createdAt DESC")
    fun getLogsForYear(financialYearCode: String): Flow<List<FinancialYearAuditLog>>

    @Query("SELECT * FROM financial_year_audit_logs WHERE financialYearCode = :financialYearCode AND targetFinancialYearCode = :targetFinancialYearCode AND action = :action LIMIT 1")
    suspend fun getLog(
        financialYearCode: String,
        targetFinancialYearCode: String?,
        action: String
    ): FinancialYearAuditLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: FinancialYearAuditLog)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE fyLabel = :financialYearCode ORDER BY date DESC, createdAt DESC")
    fun getExpenses(financialYearCode: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE fyLabel = :financialYearCode ORDER BY date DESC, createdAt DESC")
    suspend fun getExpensesSync(financialYearCode: String): List<Expense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: String)
}
