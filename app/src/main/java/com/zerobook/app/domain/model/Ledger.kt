package com.zerobook.app.domain.model

data class LedgerEntry(
    val id: String,
    val accountHead: String,
    val partyId: String? = null,
    val voucherId: String = "",
    val date: Long = 0L,
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val narration: String = "",
    val financialYearCode: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class LedgerAccount(
    val id: String,
    val name: String,
    val groupName: String,
    val openingBalance: Double = 0.0,
    val balanceType: String = "DR",
    val isSystem: Int = 0,
    val isParty: Int = 0,
    val partyId: String? = null,
    val gstin: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class FinancialYear(
    val code: String,
    val startDate: Long,
    val endDate: Long,
    val isClosed: Boolean = false,
    val isLocked: Boolean = false,
    val sourceFinancialYearCode: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
    val lockedAt: Long? = null
)
