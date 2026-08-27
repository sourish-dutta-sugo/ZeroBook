package com.zerobook.app.services.documents

import com.zerobook.app.services.documents.outbound.CreditNoteGenerator
import com.zerobook.app.services.documents.outbound.DeliveryChallanGenerator
import com.zerobook.app.services.documents.outbound.ProFormaInvoiceGenerator
import com.zerobook.app.services.documents.outbound.QuotationGenerator
import com.zerobook.app.services.documents.outbound.SalesOrderGenerator
import com.zerobook.app.services.documents.outbound.TaxInvoiceGenerator

/**
 * Registry for document generators.
 * Provides access to the appropriate generator for each document type.
 */
object DocumentGeneratorRegistry {
    
    private val generators: Map<DocumentType, DocumentGenerator> = mapOf(
        // Outbound documents (implemented)
        DocumentType.TAX_INVOICE to TaxInvoiceGenerator(),
        DocumentType.QUOTATION to QuotationGenerator(),
        DocumentType.PRO_FORMA_INVOICE to ProFormaInvoiceGenerator(),
        DocumentType.SALES_ORDER to SalesOrderGenerator(),
        DocumentType.DELIVERY_CHALLAN to DeliveryChallanGenerator(),
        DocumentType.CREDIT_NOTE to CreditNoteGenerator()
        
        // Inbound documents - to be implemented later
        // DocumentType.PURCHASE_INVOICE to PurchaseInvoiceGenerator(),
        // DocumentType.PURCHASE_ORDER to PurchaseOrderGenerator(),
        // DocumentType.GOODS_RECEIPT_NOTE to GoodsReceiptNoteGenerator(),
        // DocumentType.DEBIT_NOTE to DebitNoteGenerator(),
        // DocumentType.PURCHASE_RETURN to PurchaseReturnGenerator(),
        // DocumentType.SALES_RETURN to SalesReturnGenerator(),
        
        // Internal documents - to be implemented later
        // DocumentType.RECEIPT to ReceiptGenerator(),
        // DocumentType.PAYMENT to PaymentGenerator(),
        // DocumentType.JOURNAL to JournalGenerator(),
        // DocumentType.MATERIAL_TRANSFER_NOTE to MaterialNoteGenerator(),
        // DocumentType.REJECTION_NOTE to RejectionNoteGenerator(),
        // DocumentType.PETTY_CASH to PettyCashGenerator(),
        // DocumentType.INCOME to IncomeGenerator(),
        // DocumentType.EXPENSE to ExpenseGenerator(),
        // DocumentType.BILLS_RECEIVABLE to BillsReceivableGenerator(),
        // DocumentType.BILLS_PAYABLE to BillsPayableGenerator(),
        // DocumentType.REQUEST_FOR_QUOTATION to InquiryGenerator()
    )
    
    /**
     * Get the generator for a specific document type.
     * 
     * @param documentType The document type to get generator for
     * @return The DocumentGenerator for the specified type
     * @throws IllegalArgumentException if no generator is registered for the type
     */
    fun getGenerator(documentType: DocumentType): DocumentGenerator {
        return generators[documentType] 
            ?: throw IllegalArgumentException("No generator registered for document type: $documentType")
    }
    
    /**
     * Get the generator for a voucher type string.
     * 
     * @param voucherType The voucher type string (e.g., "SALE", "QUOTATION")
     * @return The DocumentGenerator for the voucher type
     */
    fun getGeneratorForVoucherType(voucherType: String): DocumentGenerator {
        val documentType = DocumentType.fromVoucherType(voucherType)
        return getGenerator(documentType)
    }
    
    /**
     * Check if a generator is registered for the given document type.
     */
    fun hasGenerator(documentType: DocumentType): Boolean {
        return generators.containsKey(documentType)
    }
    
    /**
     * Get all registered document types.
     */
    fun getRegisteredTypes(): Set<DocumentType> = generators.keys
}
