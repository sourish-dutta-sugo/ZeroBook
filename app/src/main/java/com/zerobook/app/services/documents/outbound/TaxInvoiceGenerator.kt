package com.zerobook.app.services.documents.outbound

import com.zerobook.app.data.BusinessProfile
import com.zerobook.app.data.Party
import com.zerobook.app.data.Voucher
import com.zerobook.app.data.VoucherItem
import com.zerobook.app.services.InvoiceGenerator
import com.zerobook.app.services.documents.DocumentGenerator
import com.zerobook.app.services.documents.DocumentHtmlComponents.escapeHtml
import com.zerobook.app.services.documents.DocumentHtmlComponents.formatMoney
import com.zerobook.app.services.documents.DocumentHtmlComponents.formatQty
import com.zerobook.app.services.documents.DocumentHtmlComponents.toFileUrl
import com.zerobook.app.services.documents.DocumentType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generator for Tax Invoice / Sales Invoice documents.
 * 
 * Reference design characteristics:
 * - Large "TAX INVOICE" or "SALES INVOICE" title at top
 * - Business header with logo and address in bordered box
 * - Seller and Buyer information blocks
 * - Invoice number and date
 * - Item table with full details (Description, HSN, Qty, Unit, Rate, Taxable Amount)
 * - GST breakup table (CGST/SGST or IGST)
 * - Amount in words
 * - Balance snapshot for credit/part payment
 * - Bank details for payment
 * - Terms and conditions
 * - Authorized signature with optional signature image
 */
class TaxInvoiceGenerator : DocumentGenerator {
    
    override val documentType = DocumentType.TAX_INVOICE
    
    override fun getDocumentTitle(voucher: Voucher, business: BusinessProfile): String {
        return if (business.gstin.isNotBlank()) "TAX INVOICE" else "INVOICE"
    }
    
    override fun generateHtml(
        voucher: Voucher,
        items: List<VoucherItem>,
        business: BusinessProfile,
        party: Party?,
        extras: InvoiceGenerator.VoucherRenderExtras
    ): String {
        val title = getDocumentTitle(voucher, business)
        val isIntrastate = !voucher.isIgst && party?.stateCode == business.stateCode
        val showLogo = business.showLogo && !business.logoPath.isNullOrBlank() 
            && java.io.File(business.logoPath!!).exists()
        val showSignature = business.showSignature && !business.signaturePath.isNullOrBlank() 
            && java.io.File(business.signaturePath!!).exists()
        
        val dateStr = SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH).format(Date(voucher.date))
        
        // Calculate totals
        val totalQuantity = items.sumOf { it.qty }
        val totalTaxAmount = voucher.cgst + voucher.sgst + voucher.igst
        
        // Build item rows
        val itemRows = items.mapIndexed { index, item ->
            """
            <tbody class='no-break-inside'>
            <tr>
              <td class='center num'>${index + 1}</td>
              <td>${escapeHtml(item.productName)}</td>
              <td class='center num'>${escapeHtml(item.hsnCode)}</td>
              <td class='center num'>${formatQty(item.qty)}</td>
              <td class='center'>${escapeHtml(item.unit)}</td>
              <td class='right num'>${formatMoney(item.rate)}</td>
              <td class='right num'>${formatMoney(item.taxableAmount)}</td>
            </tr>
            </tbody>
            """.trimIndent()
        }.joinToString("\n")
        
        // Build GST rows
        val gstRows = buildString {
            if (voucher.cgst > 0.0 && isIntrastate) {
                append("<tr><td></td><td></td><td></td><td></td><td></td><td class='right bold'>CGST</td><td class='right num'>${formatMoney(voucher.cgst)}</td></tr>")
            }
            if (voucher.sgst > 0.0 && isIntrastate) {
                append("<tr><td></td><td></td><td></td><td></td><td></td><td class='right bold'>SGST</td><td class='right num'>${formatMoney(voucher.sgst)}</td></tr>")
            }
            if (voucher.igst > 0.0 && !isIntrastate) {
                append("<tr><td></td><td></td><td></td><td></td><td></td><td class='right bold'>IGST</td><td class='right num'>${formatMoney(voucher.igst)}</td></tr>")
            }
        }
        
        // Build tax summary table
        val taxSummaryHtml = buildTaxSummaryHtml(items, voucher, isIntrastate)
        
        // Build balance snapshot
        val balanceHtml = buildBalanceSnapshotHtml(voucher, extras)
        
        // Build seller block
        val sellerHtml = buildSellerBlock(business, showLogo)
        
        // Build buyer block
        val buyerHtml = buildBuyerBlock(party, business)
        
        // Build transport details
        val transportHtml = buildTransportDetails(voucher, business)
        
        // Build declaration/terms
        val declarationHtml = buildDeclaration(business.termsAndConditions)
        
        // Signature html
        val signatureHtml = if (showSignature) {
            "<img src='${toFileUrl(business.signaturePath!!)}' style='max-height:62px;max-width:180px;object-fit:contain;display:block;margin-left:auto;'/>"
        } else {
            "<div style='height:62px;'></div>"
        }
        
        // Amount in words
        val amountInWords = amountInWords(voucher.netAmount)
        val taxAmountInWords = amountInWords(totalTaxAmount)
        
        return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset='UTF-8'/>
          <meta name='viewport' content='width=device-width, initial-scale=1.0'/>
          <style>
            * { margin:0; padding:0; box-sizing:border-box; }
            body { font-family:Arial,Helvetica,sans-serif; font-size:11px; color:#111; background:#fff; }
            @page { 
              size: A4 portrait; 
              margin: 10mm 12mm;
            }
            .page { width:100%; max-width:794px; margin:0 auto; padding:14px 18px; background:#fff; }
            table { width:100%; border-collapse:collapse; table-layout:fixed; }
            td, th { border:1px solid #000; padding:5px 6px; vertical-align:top; word-wrap:break-word; overflow-wrap:break-word; }
            .title { text-align:center; font-size:16px; font-weight:700; letter-spacing:0.5px; padding:4px 0 12px 0; }
            .center { text-align:center; }
            .right { text-align:right; }
            .bold { font-weight:700; }
            .num { white-space:nowrap; font-size:10px; }
            .small-label { font-size:9px; color:#666; margin-bottom:4px; }
            .meta-value { font-size:11px; font-weight:700; margin-top:2px; }
            .meta-subtext { font-size:9px; color:#444; margin-top:3px; line-height:1.35; }
            .company-name { font-size:14px; font-weight:700; color:#000; display:block; margin-bottom:4px; }
            .buyer-name { font-size:12px; font-weight:700; margin-bottom:3px; }
            .section-body { line-height:1.45; font-size:11px; }
            .seller-head { font-size:9px; color:#666; margin-bottom:6px; }
            .seller-grid { width:100%; border-collapse:collapse; table-layout:fixed; }
            .seller-grid td { border:none; padding:0; vertical-align:top; }
            .seller-logo { width:86px; padding-right:8px; }
            .seller-logo img { max-width:80px; max-height:80px; object-fit:contain; display:block; }
            .items-header th { background:#eef2fa; font-size:10px; font-weight:700; text-align:center; }
            .amount-words .value { display:block; font-size:11px; font-weight:700; margin-top:4px; line-height:1.45; }
            .snapshot-table td, .gst-table td, .gst-table th, .meta-table td, .meta-table th { font-size:10px; line-height:1.25; }
            .snapshot-table .grand { background:#eef2fa; font-weight:700; }
            .snapshot-table .heading { background:#f8f8f8; font-weight:700; }
            .gst-table th { background:#e8e8e8; font-size:10px; font-weight:700; text-align:center; }
            .meta-table { width:100%; border-collapse:collapse; table-layout:fixed; }
            .meta-table td, .meta-table th { border:1px solid #000; padding:6px 8px; vertical-align:top; }
            .meta-table .wrap-cell { word-wrap:break-word; overflow-wrap:break-word; }
            .terms-line { display:block; line-height:1.5; }
            .signatory { vertical-align:bottom; text-align:right; min-height:80px; }
            .signatory .for-line { font-size:11px; font-weight:700; margin-bottom:22px; }
            .summary-wrap { padding:0; }
            .summary-wrap > table { width:100%; border-collapse:collapse; table-layout:fixed; }
            .page-break { page-break-before:always; }
            .no-break-inside { page-break-inside:avoid; }
            .continuation-header { border-bottom:1px solid #000; padding:4px 0; margin-bottom:8px; font-size:10px; }
            .page-number { text-align:center; font-size:9px; color:#666; margin-top:8px; }
            @media print { 
              body { -webkit-print-color-adjust:exact; }
              .page-break { page-break-before:always; }
              .no-break-inside { page-break-inside:avoid; }
            }
          </style>
        </head>
        <body>
          <div class='page'>
            <div class='title'>$title</div>
            <table>
              <tr>
                <td style='width:58%;'><div class='section-body'>$sellerHtml</div></td>
                <td style='width:42%; padding:0;'>
                  <table class='meta-table' style='width:100%;border-collapse:collapse;'>
                    <tr>
                      <td><div class='small-label'>Invoice No.</div><div class='meta-value'>${escapeHtml(voucher.voucherNo)}</div></td>
                      <td><div class='small-label'>Dated</div><div class='meta-value'>${escapeHtml(dateStr)}</div></td>
                    </tr>
                    <tr>
                      <td><div class='small-label'>Mode / Terms of Payment</div><div class='meta-value'>${escapeHtml(voucher.paymentMode)}</div></td>
                      <td><div class='small-label'>Due Date</div><div class='meta-value'>${extras.creditDueDate.ifBlank { "-" }}</div></td>
                    </tr>
                    <tr>
                      <td class='wrap-cell'><div class='small-label'>Reference No. &amp; Date</div><div class='meta-value'>${escapeHtml(extras.referenceNo.ifBlank { "-" })}</div></td>
                      <td class='wrap-cell'><div class='small-label'>Other References</div><div class='meta-value'>${escapeHtml(extras.otherReferences.ifBlank { "-" })}</div></td>
                    </tr>
                  </table>
                </td>
              </tr>
              <tr>
                <td style='width:58%;'><div class='section-body'>$buyerHtml</div></td>
                <td style='width:42%; padding:6px 8px;'><div class='section-body'>$transportHtml</div></td>
              </tr>
            </table>
            <table>
              <tr class='items-header'>
                <th style='width:4%;'>Sl No.</th>
                <th style='width:30%; text-align:left;'>Description of Goods</th>
                <th style='width:8%;'>HSN/SAC</th>
                <th style='width:10%;'>Quantity</th>
                <th style='width:6%;'>Unit</th>
                <th style='width:8%;'>Rate</th>
                <th style='width:14%;'>Taxable Amount</th>
              </tr>
              $itemRows
              $gstRows
            </table>
            <div class='no-break-inside'>
            <table style='width:100%;border-collapse:collapse;margin-top:0;'>
              <tr>
                <td style='width:50%;border:1px solid #000;padding:5px 6px;vertical-align:top;' class='amount-words'>
                  <div class='small-label'>Amount Chargeable (in words)</div>
                  <span class='value'>${escapeHtml(amountInWords)}</span>
                </td>
                <td style='width:50%;border:1px solid #000;padding:0;vertical-align:top;' class='summary-wrap'>
                  ${buildChargeSummaryHtml(voucher, totalQuantity)}
                </td>
              </tr>
            </table>
            $taxSummaryHtml
            <table style='width:100%;border-collapse:collapse;margin-top:0;'>
              <tr>
                <td style='width:50%;border:1px solid #000;padding:5px 6px;vertical-align:top;' class='amount-words'>
                  <div class='small-label'>Tax Amount (in words)</div>
                  <span class='value'>${escapeHtml(taxAmountInWords)}</span>
                </td>
                <td style='width:50%;border:1px solid #000;padding:0;vertical-align:top;' class='summary-wrap'>
                  $balanceHtml
                </td>
              </tr>
            </table>
            </div>
            <div class='no-break-inside'>
            <table>
              <tr>
                <td style='width:58%; border:1px solid #000; vertical-align:top; padding:8px;'>
                  <div class='small-label'>Declaration / Terms &amp; Conditions</div>
                  $declarationHtml
                </td>
                <td style='width:42%; border:1px solid #000; vertical-align:bottom; text-align:right; padding:8px; height:80px;' class='signatory'>
                  <div class='for-line'>for ${escapeHtml(business.businessName)}</div>
                  $signatureHtml
                  <div>Authorised Signatory</div>
                </td>
              </tr>
            </table>
            </div>
          </div>
        </body>
        </html>
        """.trimIndent()
    }
    
    private fun buildSellerBlock(business: BusinessProfile, showLogo: Boolean): String {
        val detailsHtml = buildString {
            append("<div class='seller-head'>Seller</div>")
            append("<div class='company-name'>${escapeHtml(business.businessName)}</div>")
            if (business.address.isNotBlank()) append("<div>${escapeHtml(business.address).replace("\n", "<br/>")}</div>")
            append("<div>${escapeHtml(listOfNotNull(business.city.takeIf { it.isNotBlank() }, business.pin.takeIf { it.isNotBlank() }).joinToString(" - "))}</div>")
            if (business.pan.isNotBlank()) append("<div>PAN: ${escapeHtml(business.pan)}</div>")
            if (business.phone.isNotBlank()) append("<div>Ph: ${escapeHtml(business.phone)}</div>")
            if (business.gstin.isNotBlank()) append("<div>GSTIN/UIN: ${escapeHtml(business.gstin)}</div>")
            if (business.state.isNotBlank()) append("<div>State Name: ${escapeHtml(business.state)}, Code: ${escapeHtml(business.stateCode)}</div>")
            if (business.email.isNotBlank()) append("<div>E-Mail: ${escapeHtml(business.email)}</div>")
        }
        
        return if (showLogo) {
            """
            <table class='seller-grid'>
              <tr>
                <td class='seller-logo'><img src='${toFileUrl(business.logoPath!!)}'/></td>
                <td>$detailsHtml</td>
              </tr>
            </table>
            """.trimIndent()
        } else {
            detailsHtml
        }
    }
    
    private fun buildBuyerBlock(party: Party?, business: BusinessProfile): String = buildString {
        append("<div class='small-label'>Buyer (Bill to)</div>")
        append("<div class='buyer-name'>${escapeHtml(party?.name ?: "Cash / Walk-in Customer")}</div>")
        if (!party?.address.isNullOrBlank()) append("<div>${escapeHtml(party!!.address).replace("\n", "<br/>")}</div>")
        if (!party?.city.isNullOrBlank() || !party?.pin.isNullOrBlank()) {
            append("<div>${escapeHtml(party?.city.orEmpty())}${if (!party?.pin.isNullOrBlank()) " - ${escapeHtml(party!!.pin)}" else ""}</div>")
        }
        if (!party?.phone.isNullOrBlank()) append("<div>Phone: ${escapeHtml(party!!.phone)}</div>")
        append("<div>Place of Supply: ${escapeHtml(party?.state?.ifBlank { business.state } ?: business.state)}</div>")
        append("<div>State Code: ${escapeHtml(party?.stateCode?.ifBlank { business.stateCode } ?: business.stateCode)}</div>")
    }
    
    private fun buildTransportDetails(voucher: Voucher, business: BusinessProfile): String = buildString {
        append("<div class='small-label'>Transport / Delivery / Payment</div>")
        val transportLines = buildList {
            if (voucher.transporterName.isNotBlank()) add("Transport: ${escapeHtml(voucher.transporterName)}")
            if (voucher.vehicleNo.isNotBlank()) add("Vehicle No.: ${escapeHtml(voucher.vehicleNo)}")
            if (voucher.lrNo.isNotBlank()) add("LR / GR No.: ${escapeHtml(voucher.lrNo)}")
            if (voucher.destination.isNotBlank()) add("Destination: ${escapeHtml(voucher.destination)}")
        }
        append(transportLines.joinToString("<br/>").ifBlank { "-" })
        
        // Bank details
        if (business.bankName.isNotBlank()) {
            append("<div style='height:8px;'></div>")
            append("<div><strong>Seller's bank details</strong></div>")
            append("<div>Bank: ${escapeHtml(business.bankName)}</div>")
            if (business.accountNo.isNotBlank()) append("<div>A/C No: ${escapeHtml(business.accountNo)}</div>")
            if (business.ifsc.isNotBlank()) append("<div>IFSC: ${escapeHtml(business.ifsc)}</div>")
            if (business.branchName.isNotBlank()) append("<div>Branch: ${escapeHtml(business.branchName)}</div>")
        }
    }
    
    private fun buildChargeSummaryHtml(voucher: Voucher, totalQuantity: Double): String {
        return """
        <table style='width:100%;border-collapse:collapse;table-layout:fixed;'>
          <tr><td style='border:1px solid #000;padding:3px 5px;font-size:10px;'>Items Quantity</td><td style='border:1px solid #000;padding:3px 5px;text-align:right;font-size:10px;' class='num'>${formatQty(totalQuantity)}</td></tr>
          <tr><td style='border:1px solid #000;padding:3px 5px;font-size:10px;'>Taxable Amount</td><td style='border:1px solid #000;padding:3px 5px;text-align:right;font-size:10px;' class='num'>${formatMoney(voucher.taxableAmount)}</td></tr>
          <tr><td style='border:1px solid #000;padding:3px 5px;font-size:10px;'>CGST</td><td style='border:1px solid #000;padding:3px 5px;text-align:right;font-size:10px;' class='num'>${formatMoney(voucher.cgst)}</td></tr>
          ${if (!voucher.isIgst) "<tr><td style='border:1px solid #000;padding:3px 5px;font-size:10px;'>SGST / UTGST</td><td style='border:1px solid #000;padding:3px 5px;text-align:right;font-size:10px;' class='num'>${formatMoney(voucher.sgst)}</td></tr>" else ""}
          ${if (voucher.isIgst) "<tr><td style='border:1px solid #000;padding:3px 5px;font-size:10px;'>IGST</td><td style='border:1px solid #000;padding:3px 5px;text-align:right;font-size:10px;' class='num'>${formatMoney(voucher.igst)}</td></tr>" else ""}
          <tr><td style='border:1px solid #000;padding:4px 5px;font-weight:bold;font-size:11px;'>Grand Total</td><td style='border:1px solid #000;padding:4px 5px;text-align:right;font-weight:bold;font-size:13px;' class='num'>${formatMoney(voucher.netAmount)}</td></tr>
        </table>
        """.trimIndent()
    }
    
    private fun buildTaxSummaryHtml(items: List<VoucherItem>, voucher: Voucher, isIntrastate: Boolean): String {
        // Group items by HSN and GST rate
        data class TaxSummaryRow(val hsnCode: String, val gstRate: Double, val taxableValue: Double, val cgstAmount: Double, val sgstAmount: Double, val igstAmount: Double)
        
        val taxSummaries = items.groupBy { "${it.hsnCode}|${it.gstRate}" }
            .entries
            .sortedBy { it.key }
            .map { (_, groupedItems) ->
                val firstItem = groupedItems.first()
                val taxableValue = groupedItems.sumOf { it.taxableAmount }
                val cgstAmount = groupedItems.sumOf { it.cgstAmount }
                val sgstAmount = groupedItems.sumOf { it.sgstAmount }
                val igstAmount = groupedItems.sumOf { it.igstAmount }
                TaxSummaryRow(firstItem.hsnCode, firstItem.gstRate, taxableValue, cgstAmount, sgstAmount, igstAmount)
            }
        
        if (taxSummaries.isEmpty() || (voucher.cgst + voucher.sgst + voucher.igst) <= 0.0) {
            return ""
        }
        
        val taxRows = taxSummaries.joinToString("") { row ->
            """
            <tr>
              <td class='center num'>${escapeHtml(row.hsnCode)}</td>
              <td class='right num'>${formatMoney(row.taxableValue)}</td>
              <td class='center num'>${rateLabel(if (!voucher.isIgst) row.gstRate / 2.0 else 0.0)}</td>
              <td class='right num'>${formatMoney(row.cgstAmount)}</td>
              <td class='center num'>${rateLabel(if (!voucher.isIgst) row.gstRate / 2.0 else 0.0)}</td>
              <td class='right num'>${formatMoney(row.sgstAmount)}</td>
              <td class='center num'>${rateLabel(if (voucher.isIgst) row.gstRate else 0.0)}</td>
              <td class='right num'>${formatMoney(row.igstAmount)}</td>
              <td class='right num'>${formatMoney(row.cgstAmount + row.sgstAmount + row.igstAmount)}</td>
            </tr>
            """.trimIndent()
        }
        
        return """
        <table class='gst-table' style='width:100%;border-collapse:collapse;table-layout:fixed;'>
          <tr>
            <th style='width:10%;'>HSN/SAC</th>
            <th style='width:14%;'>Taxable Value</th>
            <th style='width:7%;'>CGST Rate</th>
            <th style='width:10%;'>CGST Amount</th>
            <th style='width:7%;'>SGST Rate</th>
            <th style='width:10%;'>SGST Amount</th>
            <th style='width:7%;'>IGST Rate</th>
            <th style='width:10%;'>IGST Amount</th>
            <th style='width:15%;'>Total Tax Amount</th>
          </tr>
          $taxRows
          <tr>
            <td class='right bold' style='border:1px solid #000;padding:3px 4px;'>Total</td>
            <td class='right bold num' style='border:1px solid #000;padding:3px 4px;'>${formatMoney(voucher.taxableAmount)}</td>
            <td class='center bold num' style='border:1px solid #000;padding:3px 4px;'>${rateLabel(taxSummaries.firstOrNull()?.let { if (!voucher.isIgst) it.gstRate / 2.0 else 0.0 } ?: 0.0)}</td>
            <td class='right bold num' style='border:1px solid #000;padding:3px 4px;'>${formatMoney(voucher.cgst)}</td>
            <td class='center bold num' style='border:1px solid #000;padding:3px 4px;'>${rateLabel(taxSummaries.firstOrNull()?.let { if (!voucher.isIgst) it.gstRate / 2.0 else 0.0 } ?: 0.0)}</td>
            <td class='right bold num' style='border:1px solid #000;padding:3px 4px;'>${formatMoney(voucher.sgst)}</td>
            <td class='center bold num' style='border:1px solid #000;padding:3px 4px;'>${rateLabel(taxSummaries.firstOrNull()?.let { if (voucher.isIgst) it.gstRate else 0.0 } ?: 0.0)}</td>
            <td class='right bold num' style='border:1px solid #000;padding:3px 4px;'>${formatMoney(voucher.igst)}</td>
            <td class='right bold num' style='border:1px solid #000;padding:3px 4px;'>${formatMoney(voucher.cgst + voucher.sgst + voucher.igst)}</td>
          </tr>
        </table>
        """.trimIndent()
    }
    
    private fun buildBalanceSnapshotHtml(voucher: Voucher, extras: InvoiceGenerator.VoucherRenderExtras): String {
        val paymentMode = extras.paymentModeValue.trim().replace('_', ' ').uppercase(Locale.ENGLISH)
        
        return when (paymentMode) {
            "CREDIT" -> """
            <table style='width:100%;border-collapse:collapse;table-layout:fixed;'>
              <tr><td colspan='2' style='border:1px solid #000;padding:3px 5px;font-weight:bold;background:#f8f8f8;'>Balance Snapshot</td></tr>
              <tr><td style='border:1px solid #000;padding:3px 5px;'>Previous Due</td><td style='border:1px solid #000;padding:3px 5px;text-align:right;' class='num'>${formatMoney(0.0)}</td></tr>
              <tr><td style='border:1px solid #000;padding:3px 5px;'>Current Invoice</td><td style='border:1px solid #000;padding:3px 5px;text-align:right;' class='num'>${formatMoney(voucher.netAmount)}</td></tr>
              <tr><td style='border:1px solid #000;padding:3px 5px;'>Advance Received</td><td style='border:1px solid #000;padding:3px 5px;text-align:right;' class='num'>${formatMoney(if (extras.isAdvance) voucher.netAmount else 0.0)}</td></tr>
              <tr><td style='border:1px solid #000;padding:3px 5px;'>Part Payment</td><td style='border:1px solid #000;padding:3px 5px;text-align:right;' class='num'>${formatMoney(extras.partialAmountPaid.coerceAtLeast(0.0))}</td></tr>
              <tr><td style='border:1px solid #000;padding:3px 5px;font-weight:bold;'>Outstanding</td><td style='border:1px solid #000;padding:3px 5px;text-align:right;font-weight:bold;' class='num'>${formatMoney(voucher.outstandingAmount.coerceAtLeast(0.0))}</td></tr>
            </table>
            """.trimIndent()
            "PART PAYMENT" -> """
            <table style='width:100%;border-collapse:collapse;table-layout:fixed;'>
              <tr><td style='border:1px solid #000;padding:3px 5px;'>Previous Due</td><td style='border:1px solid #000;padding:3px 5px;text-align:right;' class='num'>${formatMoney(0.0)}</td></tr>
              <tr><td style='border:1px solid #000;padding:3px 5px;'>Current Invoice</td><td style='border:1px solid #000;padding:3px 5px;text-align:right;' class='num'>${formatMoney(voucher.netAmount)}</td></tr>
              <tr><td style='border:1px solid #000;padding:3px 5px;'>Part Payment</td><td style='border:1px solid #000;padding:3px 5px;text-align:right;' class='num'>${formatMoney(extras.partialAmountPaid.coerceAtLeast(0.0))}</td></tr>
              <tr><td style='border:1px solid #000;padding:3px 5px;'>Outstanding / Due</td><td style='border:1px solid #000;padding:3px 5px;text-align:right;' class='num'>${formatMoney(extras.remainingCreditAmount.coerceAtLeast(0.0))}</td></tr>
              <tr><td style='border:1px solid #000;padding:3px 5px;font-weight:bold;'>Total Invoice</td><td style='border:1px solid #000;padding:3px 5px;text-align:right;font-weight:bold;' class='num'>${formatMoney(voucher.netAmount)}</td></tr>
            </table>
            """.trimIndent()
            else -> "<div style='height:100%;min-height:40px;'></div>"
        }
    }
    
    private fun buildDeclaration(rawTerms: String): String {
        return rawTerms.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapIndexed { index, line ->
                val prefix = if (line.first().isDigit()) "" else "${index + 1}. "
                "<span class='terms-line'>$prefix${escapeHtml(line)}</span>"
            }
            .joinToString("")
    }
    
    private fun amountInWords(amount: Double): String {
        val safeAmount = amount.coerceAtLeast(0.0)
        val rupees = safeAmount.toLong()
        val paise = ((safeAmount - rupees) * 100).toInt().coerceIn(0, 99)
        var result = "INR ${numToWords(rupees).trim().ifBlank { "Zero" }}"
        if (paise > 0) {
            result += " and ${numToWords(paise.toLong()).trim()} Paise"
        }
        return "$result Only"
    }
    
    private fun numToWords(n: Long): String {
        if (n == 0L) return ""
        val ones = arrayOf("", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen")
        val tens = arrayOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")
        
        return when {
            n < 20 -> ones[n.toInt()] + " "
            n < 100 -> tens[(n / 10).toInt()] + " " + numToWords(n % 10)
            n < 1000 -> ones[(n / 100).toInt()] + " Hundred " + numToWords(n % 100)
            n < 100000 -> numToWords(n / 1000) + "Thousand " + numToWords(n % 1000)
            n < 10000000 -> numToWords(n / 100000) + "Lakh " + numToWords(n % 100000)
            else -> numToWords(n / 10000000) + "Crore " + numToWords(n % 10000000)
        }
    }
    
    private fun rateLabel(value: Double): String {
        return if (value <= 0.0) "" 
        else if (value % 1.0 == 0.0) String.format(Locale.ENGLISH, "%.0f%%", value) 
        else String.format(Locale.ENGLISH, "%.2f%%", value)
    }
}
