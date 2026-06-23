package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_years")
data class FinancialYear(
    @PrimaryKey val code: String,
    val startDate: Long,
    val endDate: Long,
    val isClosed: Boolean = false,
    val isLocked: Boolean = false,
    val sourceFinancialYearCode: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
    val lockedAt: Long? = null
)

@Entity(tableName = "business_profile")
data class BusinessProfile(
    @PrimaryKey val id: Int = 1,
    val businessName: String,
    val ownerName: String,
    val address: String,
    val city: String,
    val state: String,
    val pin: String,
    val phone: String,
    val email: String,
    val gstin: String,
    val pan: String,
    val stateCode: String,
    val bankName: String,
    val accountNo: String,
    val ifsc: String,
    @ColumnInfo(name = "bank_branch")
    val branchName: String = "",
    val upiId: String = "",
    val upiPhone: String = "",
    val logoPath: String? = null,
    val signaturePath: String? = null,
    val invoiceTitleDefault: String = "TAX INVOICE",
    val invoiceFooterText: String = "Thank you for your business!",
    val showLogo: Boolean = true,
    val showSignature: Boolean = true,
    val showBankDetails: Boolean = true,
    val showUpiQr: Boolean = true,
    val showTransportDetails: Boolean = true,
    val showGstBreakup: Boolean = true,
    val showHsnColumn: Boolean = true,
    val showAmountInWords: Boolean = true,
    val showTaxAmountInWords: Boolean = true,
    val fyLabel: String = FinancialYearUtils.currentFinancialYearCode(),
    val termsAndConditions: String = DEFAULT_TERMS_AND_CONDITIONS,
    @ColumnInfo(name = "smtp_email")
    val smtpEmail: String = "",
    @ColumnInfo(name = "smtp_password")
    val smtpPassword: String = "",
    @ColumnInfo(name = "smtp_host")
    val smtpHost: String = "smtp.gmail.com",
    @ColumnInfo(name = "smtp_port")
    val smtpPort: String = "587",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "parties")
data class Party(
    @PrimaryKey val id: String, // UUID
    val name: String,
    val type: String, // "CUSTOMER", "SUPPLIER", "BOTH"
    val phone: String,
    val email: String,
    val address: String,
    val city: String,
    val state: String,
    val stateCode: String,
    val pin: String = "",
    val gstin: String?,
    val pan: String?,
    val openingBalance: Double,
    val balanceType: String, // "DR" or "CR"
    @ColumnInfo(name = "credit_limit")
    val creditLimit: Double = 0.0,
    @ColumnInfo(name = "credit_days")
    val creditDays: Int = 0,
    val notes: String = "",
    @ColumnInfo(name = "total_purchases_amount")
    val totalPurchasesAmount: Double = 0.0,
    @ColumnInfo(name = "total_transactions")
    val totalTransactions: Int = 0,
    @ColumnInfo(name = "first_transaction_date")
    val firstTransactionDate: String = "",
    @ColumnInfo(name = "last_transaction_date")
    val lastTransactionDate: String = "",
    @ColumnInfo(name = "loyalty_points")
    val loyaltyPoints: Int = 0,
    val birthday: String = "",
    val anniversary: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String, // UUID
    val name: String,
    val hsnCode: String,
    val unit: String, // "PCS", "KG", "LTR", "MTR", "BOX", "BAG", "NOS"
    val saleRate: Double,
    val purchaseRate: Double,
    val gstRate: Double, // 0.0, 5.0, 12.0, 18.0, 28.0
    val openingStock: Double,
    @ColumnInfo(name = "current_stock")
    val currentStock: Double = 0.0,
    @ColumnInfo(name = "enable_stock_alert")
    val enableStockAlert: Boolean = false,
    @ColumnInfo(name = "low_stock_threshold")
    val lowStockThreshold: Double = 5.0,
    @ColumnInfo(name = "stock_unit")
    val stockUnit: String = "PCS",
    @ColumnInfo(name = "barcode_value")
    val barcodeValue: String = "",
    @ColumnInfo(name = "secondary_unit")
    val secondaryUnit: String = "",
    @ColumnInfo(name = "conversion_factor")
    val conversionFactor: Double = 1.0,
    val batchEnabled: Boolean = false,
    val batchNumber: String = "",
    val expiryEnabled: Boolean = false,
    val expiryDate: String = "",
    val serialEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vouchers")
data class Voucher(
    @PrimaryKey val id: String, // UUID
    val voucherNo: String,
    val type: String, // "SALE", "PURCHASE", "SALE_RETURN", "PURCHASE_RETURN", "RECEIPT", "PAYMENT", "DEBIT_NOTE", "CREDIT_NOTE"
    val date: Long,
    val partyId: String?,
    val narration: String,
    val taxableAmount: Double,
    val cgst: Double,
    val sgst: Double,
    val igst: Double,
    val roundOff: Double,
    val netAmount: Double,
    val paymentMode: String, // "CASH", "BANK", "CHEQUE", "UPI"
    val chequeNo: String?,
    val chequeDate: Long?,
    val bankName: String?,
    val isIgst: Boolean,
    val documentType: String = "",
    val additionalChargesJson: String = "[]",
    @ColumnInfo(name = "transport_name")
    val transporterName: String = "",
    @ColumnInfo(name = "transport_lr_no")
    val lrNo: String = "",
    val lrDate: Long = 0,
    @ColumnInfo(name = "transport_vehicle")
    val vehicleNo: String = "",
    @ColumnInfo(name = "transport_gstin")
    val transportGstin: String = "",
    val dispatchDocNo: String = "",
    val dispatchDocDate: Long = 0,
    @ColumnInfo(name = "transport_destination")
    val destination: String = "",
    val termsOfDelivery: String = "",
    val buyerOrderNo: String = "",
    val buyerOrderDate: Long = 0,
    val referenceNo: String = "",
    val status: String, // "DRAFT", "POSTED"
    val receiptImagePath: String? = null,
    val attachmentPath: String? = null,
    val bankIfsc: String? = null,
    val bankAccountHolder: String? = null,
    val bankNameDetail: String? = null,
    val memoNumber: String? = null,
    val branchName: String? = null,
    val outstandingAmount: Double = 0.0,
    val financialYearCode: String = FinancialYearUtils.currentFinancialYearCode(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "voucher_items")
data class VoucherItem(
    @PrimaryKey val id: String, // UUID
    val voucherId: String,
    val productId: String,
    val productName: String,
    val hsnCode: String,
    val qty: Double,
    val unit: String,
    val rate: Double,
    val discount: Double,
    val discountType: String, // "PERCENT", "AMOUNT"
    val taxableAmount: Double,
    val gstRate: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val igstAmount: Double,
    val totalAmount: Double,
    val financialYearCode: String = FinancialYearUtils.currentFinancialYearCode()
)

// Additional charges can be serialized into voucher.additionalChargesJson
data class AdditionalCharge(
    val label: String,
    val amount: Double,
    val isTaxable: Boolean,
    val gstRate: Double,
    val gstAmount: Double
)

data class JournalLine(
    val accountHead: String,
    val debit: Double,
    val credit: Double
)

@Entity(tableName = "ledger_entries")
data class LedgerEntry(
    @PrimaryKey val id: String, // UUID
    val accountHead: String, // e.g. "Sales Account", "Party Name", "CGST Payable", "Round Off Account", "Cash", "Bank"
    val voucherId: String,
    val date: Long,
    val debit: Double,
    val credit: Double,
    val narration: String,
    val financialYearCode: String = FinancialYearUtils.currentFinancialYearCode(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bank_cash_transactions")
data class BankCashTransaction(
    @PrimaryKey val id: String, // UUID
    val type: String, // "RECEIPT", "PAYMENT"
    val mode: String, // "CASH", "BANK", "CHEQUE", "UPI", "NEFT", "RTGS", "IMPS"
    val amount: Double,
    val date: Long,
    val partyId: String?,
    val partyName: String?,
    val narration: String,
    val chequeNo: String?,
    val chequeDate: Long?,
    val bankName: String?,
    val receiptImagePath: String? = null,
    val financialYearCode: String = FinancialYearUtils.currentFinancialYearCode(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "receipt_allocations")
data class ReceiptAllocation(
    @PrimaryKey val id: String, // UUID
    val receiptId: String, // Voucher ID of RECEIPT or PAYMENT voucher
    val invoiceId: String, // Voucher ID of SALE or PURCHASE voucher
    val allocatedAmount: Double,
    val financialYearCode: String = FinancialYearUtils.currentFinancialYearCode(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ledger_accounts")
data class LedgerAccount(
    @PrimaryKey val id: String, // UUID
    val name: String,
    val groupName: String,
    val openingBalance: Double = 0.0,
    val balanceType: String = "DR", // "DR" or "CR"
    val isSystem: Int = 0, // 0=no, 1=yes
    val isParty: Int = 0, // 0=no, 1=yes
    val partyId: String? = null,
    val gstin: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bills_receivable")
data class BillReceivable(
    @PrimaryKey val id: String, // UUID
    val voucherId: String,
    val voucherNo: String?,
    val partyId: String,
    val partyName: String?,
    val billDate: Long,
    val dueDate: Long?,
    val originalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val outstandingAmount: Double = 0.0,
    val status: String = "UNPAID", // UNPAID / PARTIAL / PAID / OVERDUE
    val daysOverdue: Int = 0,
    val lastReminderDate: Long? = null,
    val financialYearCode: String = FinancialYearUtils.currentFinancialYearCode(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "party_financial_year_balances",
    primaryKeys = ["partyId", "financialYearCode"]
)
data class PartyFinancialYearBalance(
    val partyId: String,
    val financialYearCode: String,
    val openingBalance: Double = 0.0,
    val balanceType: String = "DR",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "product_financial_year_balances",
    primaryKeys = ["productId", "financialYearCode"]
)
data class ProductFinancialYearBalance(
    val productId: String,
    val financialYearCode: String,
    val openingStock: Double = 0.0,
    val openingStockValue: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "ledger_account_financial_year_balances",
    primaryKeys = ["accountId", "financialYearCode"]
)
data class LedgerAccountFinancialYearBalance(
    val accountId: String,
    val financialYearCode: String,
    val openingBalance: Double = 0.0,
    val balanceType: String = "DR",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "financial_year_audit_logs")
data class FinancialYearAuditLog(
    @PrimaryKey val id: String,
    val action: String,
    val financialYearCode: String,
    val targetFinancialYearCode: String? = null,
    val detailsJson: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val id: String,
    val date: Long,
    val category: String,
    val description: String = "",
    val amount: Double,
    val paymentMode: String = "CASH",
    val referenceNo: String = "",
    val attachmentPath: String = "",
    val voucherNo: String = "",
    val fyLabel: String = FinancialYearUtils.currentFinancialYearCode(),
    val createdAt: Long = System.currentTimeMillis()
)
