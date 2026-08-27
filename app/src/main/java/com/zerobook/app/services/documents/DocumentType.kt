package com.zerobook.app.services.documents

/**
 * Enum defining all document types supported by ZeroBook.
 * Each document type maps to a specific voucher type and has its own template.
 */
enum class DocumentType(
    val displayName: String,
    val prefix: String,
    val isDraft: Boolean,
    val postsToLedger: Boolean,
    val voucherType: String
) {
    // Outbound Documents (issued by my business)
    TAX_INVOICE("TAX INVOICE", "SAL", false, true, "SALE"),
    QUOTATION("QUOTATION", "QUO", true, false, "QUOTATION"),
    PRO_FORMA_INVOICE("PRO FORMA INVOICE", "PFI", true, false, "PROFORMA"),
    SALES_ORDER("SALES ORDER", "SOR", true, false, "SALES_ORDER"),
    DELIVERY_CHALLAN("DELIVERY CHALLAN", "DC", true, false, "DELIVERY_CHALLAN"),
    CREDIT_NOTE("CREDIT NOTE", "CRN", false, true, "CREDIT_NOTE"),

    // Inbound Documents (received from supplier) - Not implemented yet
    PURCHASE_INVOICE("PURCHASE INVOICE", "PUR", false, true, "PURCHASE"),
    PURCHASE_ORDER("PURCHASE ORDER", "POR", true, false, "PURCHASE_ORDER"),
    GOODS_RECEIPT_NOTE("GOODS RECEIPT NOTE", "GRN", false, true, "GOODS_RECEIPT_NOTE"),
    DEBIT_NOTE("DEBIT NOTE", "DBN", false, true, "DEBIT_NOTE"),
    PURCHASE_RETURN("PURCHASE RETURN", "PRN", false, true, "PURCHASE_RETURN"),
    SALES_RETURN("SALES RETURN", "SRN", false, true, "SALE_RETURN"),

    // Internal Documents - Not implemented yet
    RECEIPT("RECEIPT", "RCP", false, true, "RECEIPT"),
    PAYMENT("PAYMENT", "PMT", false, true, "PAYMENT"),
    JOURNAL("JOURNAL", "JNL", false, true, "JOURNAL"),
    MATERIAL_TRANSFER_NOTE("MATERIAL TRANSFER NOTE", "STK", false, true, "MATERIAL_NOTE"),
    REJECTION_NOTE("REJECTION NOTE", "REJ", false, true, "REJECTION_NOTE"),
    PETTY_CASH("PETTY CASH VOUCHER", "PCV", false, true, "PETTY_CASH"),
    INCOME("INCOME", "INC", false, true, "INCOME"),
    EXPENSE("EXPENSE", "EXP", false, true, "EXPENSE"),
    BILLS_RECEIVABLE("BILLS RECEIVABLE", "VCH", false, true, "BILLS_RECEIVABLE"),
    BILLS_PAYABLE("BILLS PAYABLE", "VCH", false, true, "BILLS_PAYABLE"),
    REQUEST_FOR_QUOTATION("REQUEST FOR QUOTATION", "RFQ", true, false, "INQUIRY");

    companion object {
        private val voucherTypeToDocumentType: Map<String, DocumentType> = entries.associateBy { it.voucherType }

        /**
         * Resolve a DocumentType from a voucher type string.
         * Falls back to TAX_INVOICE if the voucher type is not recognized.
         */
        fun fromVoucherType(voucherType: String): DocumentType {
            return voucherTypeToDocumentType[voucherType] ?: TAX_INVOICE
        }
    }
}
