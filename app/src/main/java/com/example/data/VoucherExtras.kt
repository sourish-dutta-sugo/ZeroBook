package com.example.data

data class VoucherSaveExtras(
    val partialAmountPaid: Double = 0.0,
    val partialPaymentSubmode: String = "",
    val creditDueDate: String = "",
    val remainingCreditAmount: Double = 0.0,
    val isAdvance: Boolean = false,
    val advanceFor: String = "",
    val referenceNo: String = "",
    val otherReferences: String = ""
)
