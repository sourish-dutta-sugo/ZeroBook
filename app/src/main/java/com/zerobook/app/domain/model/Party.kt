package com.zerobook.app.domain.model

data class Party(
    val id: String,
    val name: String,
    val type: String,
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
    val balanceType: String,
    val creditLimit: Double = 0.0,
    val creditDays: Int = 0,
    val notes: String = "",
    val totalPurchasesAmount: Double = 0.0,
    val totalTransactions: Int = 0,
    val firstTransactionDate: String = "",
    val lastTransactionDate: String = "",
    val loyaltyPoints: Int = 0,
    val birthday: String = "",
    val anniversary: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
