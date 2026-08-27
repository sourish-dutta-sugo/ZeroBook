package com.zerobook.app.domain.model

data class Product(
    val id: String,
    val name: String,
    val hsnCode: String,
    val unit: String,
    val saleRate: Double,
    val purchaseRate: Double,
    val gstRate: Double,
    val openingStock: Double,
    val currentStock: Double = 0.0,
    val enableStockAlert: Boolean = false,
    val lowStockThreshold: Double = 5.0,
    val stockUnit: String = "PCS",
    val barcodeValue: String = "",
    val secondaryUnit: String = "",
    val conversionFactor: Double = 1.0,
    val batchEnabled: Boolean = false,
    val batchNumber: String = "",
    val expiryEnabled: Boolean = false,
    val expiryDate: String = "",
    val serialEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
