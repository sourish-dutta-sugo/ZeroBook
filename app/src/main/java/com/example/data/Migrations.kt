package com.example.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.ZoneId

fun ensureVoucherExtensionColumns(db: SupportSQLiteDatabase) {
    val voucherColumns = db.query("PRAGMA table_info(vouchers)").use { cursor ->
        buildSet {
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                add(cursor.getString(nameIndex))
            }
        }
    }

    fun ensureColumn(name: String, sql: String) {
        if (name !in voucherColumns) {
            db.execSQL(sql)
        }
    }

    ensureColumn("payment_mode", "ALTER TABLE vouchers ADD COLUMN payment_mode TEXT DEFAULT ''")
    ensureColumn("partial_amount_paid", "ALTER TABLE vouchers ADD COLUMN partial_amount_paid REAL DEFAULT 0")
    ensureColumn("partial_payment_submode", "ALTER TABLE vouchers ADD COLUMN partial_payment_submode TEXT DEFAULT ''")
    ensureColumn("credit_due_date", "ALTER TABLE vouchers ADD COLUMN credit_due_date TEXT DEFAULT ''")
    ensureColumn("reference_no", "ALTER TABLE vouchers ADD COLUMN reference_no TEXT DEFAULT ''")
    ensureColumn("other_references", "ALTER TABLE vouchers ADD COLUMN other_references TEXT DEFAULT ''")
    ensureColumn("remaining_credit_amount", "ALTER TABLE vouchers ADD COLUMN remaining_credit_amount REAL DEFAULT 0")
    ensureColumn("is_advance", "ALTER TABLE vouchers ADD COLUMN is_advance INTEGER DEFAULT 0")
    ensureColumn("advance_for", "ALTER TABLE vouchers ADD COLUMN advance_for TEXT DEFAULT ''")
    ensureColumn("transport_name", "ALTER TABLE vouchers ADD COLUMN transport_name TEXT DEFAULT ''")
    ensureColumn("transport_vehicle", "ALTER TABLE vouchers ADD COLUMN transport_vehicle TEXT DEFAULT ''")
    ensureColumn("transport_lr_no", "ALTER TABLE vouchers ADD COLUMN transport_lr_no TEXT DEFAULT ''")
    ensureColumn("transport_gstin", "ALTER TABLE vouchers ADD COLUMN transport_gstin TEXT DEFAULT ''")
    ensureColumn("transport_destination", "ALTER TABLE vouchers ADD COLUMN transport_destination TEXT DEFAULT ''")
}

fun ensureBusinessProfileExtensionColumns(db: SupportSQLiteDatabase) {
    val profileColumns = db.query("PRAGMA table_info(business_profile)").use { cursor ->
        buildSet {
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                add(cursor.getString(nameIndex))
            }
        }
    }

    fun ensureColumn(name: String, sql: String) {
        if (name !in profileColumns) {
            db.execSQL(sql)
        }
    }

    ensureColumn("bank_branch", "ALTER TABLE business_profile ADD COLUMN bank_branch TEXT NOT NULL DEFAULT ''")
    ensureColumn("upiId", "ALTER TABLE business_profile ADD COLUMN upiId TEXT NOT NULL DEFAULT ''")
    ensureColumn("upiPhone", "ALTER TABLE business_profile ADD COLUMN upiPhone TEXT NOT NULL DEFAULT ''")
    ensureColumn("logoPath", "ALTER TABLE business_profile ADD COLUMN logoPath TEXT")
    ensureColumn("signaturePath", "ALTER TABLE business_profile ADD COLUMN signaturePath TEXT")
    ensureColumn("invoiceTitleDefault", "ALTER TABLE business_profile ADD COLUMN invoiceTitleDefault TEXT NOT NULL DEFAULT 'TAX INVOICE'")
    ensureColumn("invoiceFooterText", "ALTER TABLE business_profile ADD COLUMN invoiceFooterText TEXT NOT NULL DEFAULT 'Thank you for your business!'")
    ensureColumn("showLogo", "ALTER TABLE business_profile ADD COLUMN showLogo INTEGER NOT NULL DEFAULT 1")
    ensureColumn("showSignature", "ALTER TABLE business_profile ADD COLUMN showSignature INTEGER NOT NULL DEFAULT 1")
    ensureColumn("showBankDetails", "ALTER TABLE business_profile ADD COLUMN showBankDetails INTEGER NOT NULL DEFAULT 1")
    ensureColumn("showUpiQr", "ALTER TABLE business_profile ADD COLUMN showUpiQr INTEGER NOT NULL DEFAULT 1")
    ensureColumn("showTransportDetails", "ALTER TABLE business_profile ADD COLUMN showTransportDetails INTEGER NOT NULL DEFAULT 1")
    ensureColumn("showGstBreakup", "ALTER TABLE business_profile ADD COLUMN showGstBreakup INTEGER NOT NULL DEFAULT 1")
    ensureColumn("showHsnColumn", "ALTER TABLE business_profile ADD COLUMN showHsnColumn INTEGER NOT NULL DEFAULT 1")
    ensureColumn("showAmountInWords", "ALTER TABLE business_profile ADD COLUMN showAmountInWords INTEGER NOT NULL DEFAULT 1")
    ensureColumn("showTaxAmountInWords", "ALTER TABLE business_profile ADD COLUMN showTaxAmountInWords INTEGER NOT NULL DEFAULT 1")
    ensureColumn("fyLabel", "ALTER TABLE business_profile ADD COLUMN fyLabel TEXT NOT NULL DEFAULT '2025-26'")
    ensureColumn("termsAndConditions", "ALTER TABLE business_profile ADD COLUMN termsAndConditions TEXT NOT NULL DEFAULT '1. Delay in payment beyond the agreed credit period will attract interest @ 24% p.a.\n2. Goods once sold will not be taken back.\n3. Subject to local jurisdiction only.'")
    ensureColumn("terms_and_conditions", "ALTER TABLE business_profile ADD COLUMN terms_and_conditions TEXT DEFAULT ''")
    ensureColumn("smtp_email", "ALTER TABLE business_profile ADD COLUMN smtp_email TEXT NOT NULL DEFAULT ''")
    ensureColumn("smtp_password", "ALTER TABLE business_profile ADD COLUMN smtp_password TEXT NOT NULL DEFAULT ''")
    ensureColumn("smtp_host", "ALTER TABLE business_profile ADD COLUMN smtp_host TEXT NOT NULL DEFAULT 'smtp.gmail.com'")
    ensureColumn("smtp_port", "ALTER TABLE business_profile ADD COLUMN smtp_port TEXT NOT NULL DEFAULT '587'")
    ensureColumn("createdAt", "ALTER TABLE business_profile ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
}

fun ensurePartyExtensionColumns(db: SupportSQLiteDatabase) {
    val partyColumns = db.query("PRAGMA table_info(parties)").use { cursor ->
        buildSet {
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                add(cursor.getString(nameIndex))
            }
        }
    }

    fun ensureColumn(name: String, sql: String) {
        if (name !in partyColumns) {
            db.execSQL(sql)
        }
    }

    ensureColumn("credit_limit", "ALTER TABLE parties ADD COLUMN credit_limit REAL NOT NULL DEFAULT 0")
    ensureColumn("credit_days", "ALTER TABLE parties ADD COLUMN credit_days INTEGER NOT NULL DEFAULT 0")
    ensureColumn("notes", "ALTER TABLE parties ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
    ensureColumn("total_purchases_amount", "ALTER TABLE parties ADD COLUMN total_purchases_amount REAL NOT NULL DEFAULT 0")
    ensureColumn("total_transactions", "ALTER TABLE parties ADD COLUMN total_transactions INTEGER NOT NULL DEFAULT 0")
    ensureColumn("first_transaction_date", "ALTER TABLE parties ADD COLUMN first_transaction_date TEXT NOT NULL DEFAULT ''")
    ensureColumn("last_transaction_date", "ALTER TABLE parties ADD COLUMN last_transaction_date TEXT NOT NULL DEFAULT ''")
    ensureColumn("loyalty_points", "ALTER TABLE parties ADD COLUMN loyalty_points INTEGER NOT NULL DEFAULT 0")
    ensureColumn("birthday", "ALTER TABLE parties ADD COLUMN birthday TEXT NOT NULL DEFAULT ''")
    ensureColumn("anniversary", "ALTER TABLE parties ADD COLUMN anniversary TEXT NOT NULL DEFAULT ''")
}

fun ensureProductExtensionColumns(db: SupportSQLiteDatabase) {
    val productColumns = db.query("PRAGMA table_info(products)").use { cursor ->
        buildSet {
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                add(cursor.getString(nameIndex))
            }
        }
    }

    fun ensureColumn(name: String, sql: String) {
        if (name !in productColumns) {
            db.execSQL(sql)
        }
    }

    ensureColumn("current_stock", "ALTER TABLE products ADD COLUMN current_stock REAL NOT NULL DEFAULT 0")
    ensureColumn("enable_stock_alert", "ALTER TABLE products ADD COLUMN IF NOT EXISTS enable_stock_alert INTEGER NOT NULL DEFAULT 0")
    ensureColumn("low_stock_threshold", "ALTER TABLE products ADD COLUMN low_stock_threshold REAL NOT NULL DEFAULT 5")
    ensureColumn("stock_unit", "ALTER TABLE products ADD COLUMN stock_unit TEXT NOT NULL DEFAULT 'PCS'")
    ensureColumn("barcode_value", "ALTER TABLE products ADD COLUMN barcode_value TEXT NOT NULL DEFAULT ''")
    ensureColumn("secondary_unit", "ALTER TABLE products ADD COLUMN secondary_unit TEXT NOT NULL DEFAULT ''")
    ensureColumn("conversion_factor", "ALTER TABLE products ADD COLUMN conversion_factor REAL NOT NULL DEFAULT 1.0")
    db.execSQL(
        """
        UPDATE products
        SET current_stock = CASE
            WHEN current_stock = 0 AND openingStock > 0 THEN openingStock
            ELSE current_stock
        END
        """.trimIndent()
    )
    db.execSQL("UPDATE products SET stock_unit = unit WHERE stock_unit IS NULL OR stock_unit = ''")
}

fun ensureReminderScheduleTable(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS reminder_schedules (
            id TEXT PRIMARY KEY,
            party_id TEXT,
            party_name TEXT,
            reminder_type TEXT,
            scheduled_date TEXT,
            scheduled_time TEXT,
            interval_days INTEGER DEFAULT 0,
            is_active INTEGER DEFAULT 1,
            last_sent TEXT DEFAULT '',
            created_at INTEGER DEFAULT 0
        )
        """.trimIndent()
    )
}

fun ensureExpenseTable(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS expenses (
            id TEXT NOT NULL PRIMARY KEY,
            date INTEGER NOT NULL,
            category TEXT NOT NULL,
            description TEXT NOT NULL DEFAULT '',
            amount REAL NOT NULL,
            paymentMode TEXT NOT NULL DEFAULT 'CASH',
            referenceNo TEXT NOT NULL DEFAULT '',
            attachmentPath TEXT NOT NULL DEFAULT '',
            voucherNo TEXT NOT NULL DEFAULT '',
            fyLabel TEXT NOT NULL DEFAULT '',
            createdAt INTEGER NOT NULL DEFAULT 0
        )
        """.trimIndent()
    )
}

fun ensureFinancialYearColumnsAndIndexes(db: SupportSQLiteDatabase) {
    val currentFinancialYear = FinancialYearUtils.currentFinancialYearCode(ZoneId.systemDefault())

    fun tableExists(name: String): Boolean =
        db.query("SELECT name FROM sqlite_master WHERE type='table' AND name = ?", arrayOf(name)).use { it.moveToFirst() }

    fun tableColumns(tableName: String): Set<String> =
        db.query("PRAGMA table_info($tableName)").use { cursor ->
            buildSet {
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    add(cursor.getString(nameIndex))
                }
            }
        }

    fun ensureColumn(tableName: String, columnName: String, sql: String) {
        if (tableExists(tableName) && columnName !in tableColumns(tableName)) {
            db.execSQL(sql)
        }
    }

    ensureColumn(
        tableName = "vouchers",
        columnName = "financialYearCode",
        sql = "ALTER TABLE vouchers ADD COLUMN financialYearCode TEXT NOT NULL DEFAULT '$currentFinancialYear'"
    )
    ensureColumn(
        tableName = "voucher_items",
        columnName = "financialYearCode",
        sql = "ALTER TABLE voucher_items ADD COLUMN financialYearCode TEXT NOT NULL DEFAULT '$currentFinancialYear'"
    )
    ensureColumn(
        tableName = "ledger_entries",
        columnName = "financialYearCode",
        sql = "ALTER TABLE ledger_entries ADD COLUMN financialYearCode TEXT NOT NULL DEFAULT '$currentFinancialYear'"
    )
    ensureColumn(
        tableName = "bank_cash_transactions",
        columnName = "financialYearCode",
        sql = "ALTER TABLE bank_cash_transactions ADD COLUMN financialYearCode TEXT NOT NULL DEFAULT '$currentFinancialYear'"
    )
    ensureColumn(
        tableName = "receipt_allocations",
        columnName = "financialYearCode",
        sql = "ALTER TABLE receipt_allocations ADD COLUMN financialYearCode TEXT NOT NULL DEFAULT '$currentFinancialYear'"
    )
    ensureColumn(
        tableName = "bills_receivable",
        columnName = "financialYearCode",
        sql = "ALTER TABLE bills_receivable ADD COLUMN financialYearCode TEXT NOT NULL DEFAULT '$currentFinancialYear'"
    )

    if (!tableExists("financial_years")) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS financial_years (
                code TEXT NOT NULL PRIMARY KEY,
                startDate INTEGER NOT NULL,
                endDate INTEGER NOT NULL,
                isClosed INTEGER NOT NULL DEFAULT 0,
                isLocked INTEGER NOT NULL DEFAULT 0,
                sourceFinancialYearCode TEXT,
                createdAt INTEGER NOT NULL,
                closedAt INTEGER,
                lockedAt INTEGER
            )
            """.trimIndent()
        )
    }

    if (!tableExists("party_financial_year_balances")) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS party_financial_year_balances (
                partyId TEXT NOT NULL,
                financialYearCode TEXT NOT NULL,
                openingBalance REAL NOT NULL DEFAULT 0,
                balanceType TEXT NOT NULL DEFAULT 'DR',
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                PRIMARY KEY(partyId, financialYearCode)
            )
            """.trimIndent()
        )
    }

    if (!tableExists("product_financial_year_balances")) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS product_financial_year_balances (
                productId TEXT NOT NULL,
                financialYearCode TEXT NOT NULL,
                openingStock REAL NOT NULL DEFAULT 0,
                openingStockValue REAL NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                PRIMARY KEY(productId, financialYearCode)
            )
            """.trimIndent()
        )
    }

    if (!tableExists("ledger_account_financial_year_balances")) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ledger_account_financial_year_balances (
                accountId TEXT NOT NULL,
                financialYearCode TEXT NOT NULL,
                openingBalance REAL NOT NULL DEFAULT 0,
                balanceType TEXT NOT NULL DEFAULT 'DR',
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                PRIMARY KEY(accountId, financialYearCode)
            )
            """.trimIndent()
        )
    }

    if (!tableExists("financial_year_audit_logs")) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS financial_year_audit_logs (
                id TEXT NOT NULL PRIMARY KEY,
                action TEXT NOT NULL,
                financialYearCode TEXT NOT NULL,
                targetFinancialYearCode TEXT,
                detailsJson TEXT NOT NULL DEFAULT '',
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    val financialYearSql =
        """
        CASE
            WHEN CAST(strftime('%m', COLUMN / 1000, 'unixepoch', 'localtime') AS INTEGER) >= 4 THEN
                printf(
                    '%04d-%02d',
                    CAST(strftime('%Y', COLUMN / 1000, 'unixepoch', 'localtime') AS INTEGER),
                    (CAST(strftime('%Y', COLUMN / 1000, 'unixepoch', 'localtime') AS INTEGER) + 1) % 100
                )
            ELSE
                printf(
                    '%04d-%02d',
                    CAST(strftime('%Y', COLUMN / 1000, 'unixepoch', 'localtime') AS INTEGER) - 1,
                    CAST(strftime('%Y', COLUMN / 1000, 'unixepoch', 'localtime') AS INTEGER) % 100
                )
        END
        """.trimIndent()

    if (tableExists("vouchers")) {
        db.execSQL("UPDATE vouchers SET financialYearCode = '$currentFinancialYear' WHERE financialYearCode IS NULL OR financialYearCode = ''")
        db.execSQL("UPDATE vouchers SET financialYearCode = ${financialYearSql.replace("COLUMN", "date")} WHERE date > 0")
    }
    if (tableExists("voucher_items")) {
        db.execSQL(
            """
            UPDATE voucher_items
            SET financialYearCode = COALESCE(
                (SELECT v.financialYearCode FROM vouchers v WHERE v.id = voucher_items.voucherId),
                financialYearCode,
                '$currentFinancialYear'
            )
            """.trimIndent()
        )
    }
    if (tableExists("ledger_entries")) {
        db.execSQL("UPDATE ledger_entries SET financialYearCode = '$currentFinancialYear' WHERE financialYearCode IS NULL OR financialYearCode = ''")
        db.execSQL("UPDATE ledger_entries SET financialYearCode = ${financialYearSql.replace("COLUMN", "date")} WHERE date > 0")
    }
    if (tableExists("bank_cash_transactions")) {
        db.execSQL("UPDATE bank_cash_transactions SET financialYearCode = '$currentFinancialYear' WHERE financialYearCode IS NULL OR financialYearCode = ''")
        db.execSQL("UPDATE bank_cash_transactions SET financialYearCode = ${financialYearSql.replace("COLUMN", "date")} WHERE date > 0")
    }
    if (tableExists("receipt_allocations")) {
        db.execSQL(
            """
            UPDATE receipt_allocations
            SET financialYearCode = COALESCE(
                (SELECT v.financialYearCode FROM vouchers v WHERE v.id = receipt_allocations.receiptId),
                (SELECT v.financialYearCode FROM vouchers v WHERE v.id = receipt_allocations.invoiceId),
                financialYearCode,
                '$currentFinancialYear'
            )
            """.trimIndent()
        )
    }
    if (tableExists("bills_receivable")) {
        db.execSQL("UPDATE bills_receivable SET financialYearCode = '$currentFinancialYear' WHERE financialYearCode IS NULL OR financialYearCode = ''")
        db.execSQL("UPDATE bills_receivable SET financialYearCode = ${financialYearSql.replace("COLUMN", "billDate")} WHERE billDate > 0")
    }

    if (tableExists("financial_years")) {
        db.execSQL(
            """
            INSERT OR IGNORE INTO financial_years(code, startDate, endDate, isClosed, isLocked, sourceFinancialYearCode, createdAt, closedAt, lockedAt)
            SELECT fy.code,
                   fy.startDate,
                   fy.endDate,
                   0,
                   0,
                   NULL,
                   CAST(strftime('%s', 'now') AS INTEGER) * 1000,
                   NULL,
                   NULL
            FROM (
                SELECT DISTINCT
                    financialYearCode AS code,
                    CASE
                        WHEN financialYearCode IS NULL OR financialYearCode = '' THEN 0
                        ELSE CAST(strftime('%s', substr(financialYearCode, 1, 4) || '-04-01 00:00:00') AS INTEGER) * 1000
                    END AS startDate,
                    CASE
                        WHEN financialYearCode IS NULL OR financialYearCode = '' THEN 0
                        ELSE CAST(strftime('%s', (CAST(substr(financialYearCode, 1, 4) AS INTEGER) + 1) || '-03-31 23:59:59') AS INTEGER) * 1000
                    END AS endDate
                FROM vouchers
                WHERE financialYearCode IS NOT NULL AND financialYearCode != ''
            ) fy
            """.trimIndent()
        )
    }

    val businessProfileColumns = if (tableExists("business_profile")) tableColumns("business_profile") else emptySet()
    val defaultYearCode = if ("fyLabel" in businessProfileColumns) {
        db.query("SELECT fyLabel FROM business_profile WHERE id = 1 LIMIT 1").use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0).orEmpty().ifBlank { currentFinancialYear } else currentFinancialYear
        }
    } else {
        currentFinancialYear
    }
    val defaultStart = FinancialYearUtils.startMillisFor(defaultYearCode)
    val defaultEnd = FinancialYearUtils.endMillisFor(defaultYearCode)
    db.execSQL(
        """
        INSERT OR IGNORE INTO financial_years(code, startDate, endDate, isClosed, isLocked, sourceFinancialYearCode, createdAt, closedAt, lockedAt)
        VALUES('$defaultYearCode', $defaultStart, $defaultEnd, 0, 0, NULL, ${System.currentTimeMillis()}, NULL, NULL)
        """.trimIndent()
    )

    if (tableExists("party_financial_year_balances")) {
        db.execSQL(
            """
            INSERT OR IGNORE INTO party_financial_year_balances(partyId, financialYearCode, openingBalance, balanceType, createdAt, updatedAt)
            SELECT id, '$defaultYearCode', openingBalance, balanceType, createdAt, ${System.currentTimeMillis()}
            FROM parties
            """.trimIndent()
        )
    }

    if (tableExists("product_financial_year_balances")) {
        db.execSQL(
            """
            INSERT OR IGNORE INTO product_financial_year_balances(productId, financialYearCode, openingStock, openingStockValue, createdAt, updatedAt)
            SELECT id, '$defaultYearCode', openingStock, openingStock * purchaseRate, createdAt, ${System.currentTimeMillis()}
            FROM products
            """.trimIndent()
        )
    }

    if (tableExists("ledger_account_financial_year_balances")) {
        db.execSQL(
            """
            INSERT OR IGNORE INTO ledger_account_financial_year_balances(accountId, financialYearCode, openingBalance, balanceType, createdAt, updatedAt)
            SELECT id, '$defaultYearCode', openingBalance, balanceType, createdAt, ${System.currentTimeMillis()}
            FROM ledger_accounts
            """.trimIndent()
        )
    }

    db.execSQL("CREATE INDEX IF NOT EXISTS index_vouchers_financialYearCode ON vouchers(financialYearCode)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_voucher_items_financialYearCode ON voucher_items(financialYearCode)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_ledger_entries_financialYearCode ON ledger_entries(financialYearCode)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_bank_cash_transactions_financialYearCode ON bank_cash_transactions(financialYearCode)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_receipt_allocations_financialYearCode ON receipt_allocations(financialYearCode)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_bills_receivable_financialYearCode ON bills_receivable(financialYearCode)")
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE parties ADD COLUMN pin TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE products ADD COLUMN batch_enabled INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE products ADD COLUMN batch_number TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE products ADD COLUMN expiry_enabled INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE products ADD COLUMN expiry_date TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE products ADD COLUMN serial_enabled INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE vouchers ADD COLUMN attachment_path TEXT DEFAULT ''")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val businessProfileColumns = db.query("PRAGMA table_info(business_profile)").use { cursor ->
            buildSet {
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    add(cursor.getString(nameIndex))
                }
            }
        }
        if ("fyLabel" !in businessProfileColumns && "fy_label" !in businessProfileColumns) {
            db.execSQL("ALTER TABLE business_profile ADD COLUMN fyLabel TEXT NOT NULL DEFAULT '2025-26'")
        }

        val voucherColumns = db.query("PRAGMA table_info(vouchers)").use { cursor ->
            buildSet {
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    add(cursor.getString(nameIndex))
                }
            }
        }
        if ("additionalChargesJson" !in voucherColumns && "additional_charges_json" !in voucherColumns) {
            db.execSQL("ALTER TABLE vouchers ADD COLUMN additionalChargesJson TEXT NOT NULL DEFAULT '[]'")
        }
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        ensureVoucherExtensionColumns(db)
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        ensureVoucherExtensionColumns(db)
        ensureBusinessProfileExtensionColumns(db)
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        ensureVoucherExtensionColumns(db)
        ensureBusinessProfileExtensionColumns(db)
        ensureFinancialYearColumnsAndIndexes(db)
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        ensureVoucherExtensionColumns(db)
        ensureBusinessProfileExtensionColumns(db)
        ensurePartyExtensionColumns(db)
        ensureFinancialYearColumnsAndIndexes(db)
        ensureReminderScheduleTable(db)
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        ensureVoucherExtensionColumns(db)
        ensureBusinessProfileExtensionColumns(db)
        ensurePartyExtensionColumns(db)
        ensureProductExtensionColumns(db)
        ensureFinancialYearColumnsAndIndexes(db)
        ensureReminderScheduleTable(db)
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        ensureVoucherExtensionColumns(db)
        ensureBusinessProfileExtensionColumns(db)
        ensurePartyExtensionColumns(db)
        ensureProductExtensionColumns(db)
        ensureFinancialYearColumnsAndIndexes(db)
        ensureReminderScheduleTable(db)
        ensureExpenseTable(db)
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        ensureVoucherExtensionColumns(db)
        ensureBusinessProfileExtensionColumns(db)
        ensurePartyExtensionColumns(db)
        ensureProductExtensionColumns(db)
        ensureFinancialYearColumnsAndIndexes(db)
        ensureReminderScheduleTable(db)
        ensureExpenseTable(db)
    }
}
