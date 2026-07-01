package com.zerobook.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        FinancialYear::class,
        BusinessProfile::class,
        Party::class,
        PartyFinancialYearBalance::class,
        Product::class,
        ProductFinancialYearBalance::class,
        Voucher::class,
        VoucherItem::class,
        LedgerEntry::class,
        BankCashTransaction::class,
        ReceiptAllocation::class,
        LedgerAccount::class,
        LedgerAccountFinancialYearBalance::class,
        BillReceivable::class,
        FinancialYearAuditLog::class,
        Expense::class,
        EmailAccount::class,
        EmailAutomationRule::class,
        EmailHistory::class
    ],
    version = 14,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financialYearDao(): FinancialYearDao
    abstract fun businessProfileDao(): BusinessProfileDao
    abstract fun partyDao(): PartyDao
    abstract fun partyFinancialYearBalanceDao(): PartyFinancialYearBalanceDao
    abstract fun productDao(): ProductDao
    abstract fun productFinancialYearBalanceDao(): ProductFinancialYearBalanceDao
    abstract fun voucherDao(): VoucherDao
    abstract fun voucherItemDao(): VoucherItemDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun bankCashDao(): BankCashDao
    abstract fun receiptAllocationDao(): ReceiptAllocationDao
    abstract fun ledgerAccountDao(): LedgerAccountDao
    abstract fun ledgerAccountFinancialYearBalanceDao(): LedgerAccountFinancialYearBalanceDao
    abstract fun billReceivableDao(): BillReceivableDao
    abstract fun financialYearAuditLogDao(): FinancialYearAuditLogDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun emailAccountDao(): EmailAccountDao
    abstract fun emailAutomationRuleDao(): EmailAutomationRuleDao
    abstract fun emailHistoryDao(): EmailHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ZeroBook.db"
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        ensureVoucherExtensionColumns(db)
                        ensureBusinessProfileExtensionColumns(db)
                        ensurePartyExtensionColumns(db)
                        ensureProductExtensionColumns(db)
                        ensureFinancialYearColumnsAndIndexes(db)
                        ensureReminderScheduleTable(db)
                        ensureExpenseTable(db)
                        ensureEmailAutomationTables(db)
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        ensureVoucherExtensionColumns(db)
                        ensureBusinessProfileExtensionColumns(db)
                        ensurePartyExtensionColumns(db)
                        ensureProductExtensionColumns(db)
                        ensureFinancialYearColumnsAndIndexes(db)
                        ensureReminderScheduleTable(db)
                        ensureExpenseTable(db)
                        ensureEmailAutomationTables(db)
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

    }
}
