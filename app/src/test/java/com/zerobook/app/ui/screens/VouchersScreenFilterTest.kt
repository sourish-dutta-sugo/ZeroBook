package com.zerobook.app.ui.screens

import com.zerobook.app.data.Voucher
import org.junit.Assert.assertEquals
import org.junit.Test

class VouchersScreenFilterTest {

    @Test
    fun applyVoucherFilters_combinesSearchTypePartyAndAmountAndStatus() {
        val vouchers = listOf(
            Voucher(
                id = "1",
                voucherNo = "INV-100",
                type = "SALE",
                date = 1_700_000_000_000L,
                partyId = "party-1",
                narration = "Rice shipment",
                taxableAmount = 1000.0,
                cgst = 0.0,
                sgst = 0.0,
                igst = 0.0,
                roundOff = 0.0,
                netAmount = 1000.0,
                paymentMode = "BANK",
                chequeNo = null,
                chequeDate = null,
                bankName = null,
                isIgst = false,
                referenceNo = "REF-100",
                status = "POSTED",
                outstandingAmount = 0.0
            ),
            Voucher(
                id = "2",
                voucherNo = "INV-200",
                type = "PURCHASE",
                date = 1_700_000_000_000L,
                partyId = "party-2",
                narration = "Wheat shipment",
                taxableAmount = 5000.0,
                cgst = 0.0,
                sgst = 0.0,
                igst = 0.0,
                roundOff = 0.0,
                netAmount = 5000.0,
                paymentMode = "BANK",
                chequeNo = null,
                chequeDate = null,
                bankName = null,
                isIgst = false,
                referenceNo = "REF-200",
                status = "POSTED",
                outstandingAmount = 2500.0
            )
        )

        val result = applyVoucherFilters(
            vouchers = vouchers,
            searchQuery = "rice",
            filterState = VoucherFilterState(
                type = "SALE",
                paymentStatus = "PAID",
                partyName = "ABC",
                minAmount = 900.0,
                reference = "REF-100"
            ),
            partyNameById = mapOf("party-1" to "ABC Traders", "party-2" to "XYZ Suppliers")
        )

        assertEquals(listOf(vouchers.first()), result)
    }
}
