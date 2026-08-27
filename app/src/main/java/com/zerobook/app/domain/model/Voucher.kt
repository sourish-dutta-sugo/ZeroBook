package com.zerobook.app.domain.model

data class Voucher(
    val id: String,
    val voucherNo: String,
    val type: String,
    val date: Long,
    val partyId: String?,
    val narration: String,
    val taxableAmount: Double,
    val cgst: Double,
    val sgst: Double,
    val igst: Double,
    val roundOff: Double,
    val netAmount: Double,
    val paymentMode: String,
    val chequeNo: String?,
    val chequeDate: Long?,
    val bankName: String?,
    val isIgst: Boolean,
    val documentType: String = "",
    val additionalChargesJson: String = "[]",
    val transporterName: String = "",
    val lrNo: String = "",
    val lrDate: Long = 0,
    val vehicleNo: String = "",
    val transportGstin: String = "",
    val dispatchDocNo: String = "",
    val dispatchDocDate: Long = 0,
    val destination: String = "",
    val termsOfDelivery: String = "",
    val buyerOrderNo: String = "",
    val buyerOrderDate: Long = 0,
    val referenceNo: String = "",
    val status: String,
    val receiptImagePath: String? = null,
    val attachmentPath: String? = null,
    val bankIfsc: String? = null,
    val bankAccountHolder: String? = null,
    val bankNameDetail: String? = null,
    val memoNumber: String? = null,
    val branchName: String? = null,
    val outstandingAmount: Double = 0.0,
    val financialYearCode: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class VoucherItem(
    val id: String,
    val voucherId: String,
    val productId: String,
    val productName: String,
    val hsnCode: String,
    val qty: Double,
    val unit: String,
    val rate: Double,
    val discount: Double,
    val discountType: String,
    val taxableAmount: Double,
    val gstRate: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val igstAmount: Double,
    val totalAmount: Double,
    val financialYearCode: String
)

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

data class VoucherSaveExtras(
    val partialAmountPaid: Double = 0.0,
    val partialPaymentSubmode: String = "",
    val creditDueDate: String = "",
    val remainingCreditAmount: Double = 0.0,
    val isAdvance: Boolean = false,
    val advanceFor: String = ""
)
