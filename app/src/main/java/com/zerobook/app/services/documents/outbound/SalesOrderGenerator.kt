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
 * Generator for Sales Order documents.
 * 
 * Reference design characteristics:
 * - Large "SALES ORDER" title at top in bordered box
 * - Business header with logo and address
 * - To: (customer details) on left
 * - Order Number, Date, Customer PO Number on right
 * - "Confirm this order your order." text
 * - Item table with: No., Item Description, Qty, Ordered, Total
 * - Sub-Total at bottom
 * - "To accept this signature's order." text
 * - Buyer signature line on left
 * - Authorized Signature (Business Name) at bottom right
 */
class SalesOrderGenerator : DocumentGenerator {
    
    override val documentType = DocumentType.SALES_ORDER
    
    override fun generateHtml(
        voucher: Voucher,
        items: List<VoucherItem>,
        business: BusinessProfile,
        party: Party?,
        extras: InvoiceGenerator.VoucherRenderExtras
    ): String {
        val title = documentType.displayName
        val dateStr = SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH).format(Date(voucher.date))
        val showLogo = business.showLogo && !business.logoPath.isNullOrBlank() 
            && java.io.File(business.logoPath!!).exists()
        
        // Calculate totals
        val totalQuantity = items.sumOf { it.qty }
        val grandTotal = items.sumOf { it.totalAmount }
        
        // Build item rows
        val itemRows = items.mapIndexed { index, item ->
            """
            <tbody class='no-break-inside'>
            <tr>
              <td class='center'>${index + 1}</td>
              <td>${escapeHtml(item.productName)}</td>
              <td class='center'>${formatQty(item.qty)}</td>
              <td class='right'>${formatMoney(item.rate)}</td>
              <td class='right'>${formatMoney(item.totalAmount)}</td>
            </tr>
            </tbody>
            """.trimIndent()
        }.joinToString("\n")
        
        // Build business header
        val businessHeaderHtml = buildBusinessHeader(business, showLogo)
        
        // Build party section
        val partyHtml = buildPartySection(party)
        
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
            table { width:100%; border-collapse:collapse; }
            td, th { border:1px solid #000; padding:6px 8px; vertical-align:top; word-wrap:break-word; overflow-wrap:break-word; }
            .title { text-align:center; font-size:18px; font-weight:700; letter-spacing:0.5px; padding:8px 0; border:2px solid #000; margin-bottom:12px; }
            .center { text-align:center; }
            .right { text-align:right; }
            .bold { font-weight:700; }
            .items-header th { background:#eef2fa; font-size:10px; font-weight:700; text-align:center; border:1px solid #000; }
            .small-label { font-size:9px; color:#666; margin-bottom:4px; }
            .meta-value { font-size:11px; font-weight:700; margin-top:2px; }
            .company-name { font-size:14px; font-weight:700; color:#000; display:block; margin-bottom:4px; }
            .section-body { line-height:1.45; font-size:11px; }
            .seller-head { font-size:9px; color:#666; margin-bottom:6px; }
            .seller-grid { width:100%; border-collapse:collapse; table-layout:fixed; }
            .seller-grid td { border:none; padding:0; vertical-align:top; }
            .seller-logo { width:86px; padding-right:8px; }
            .seller-logo img { max-width:80px; max-height:80px; object-fit:contain; display:block; }
            .confirm-text { font-size:11px; font-weight:bold; margin:12px 0; }
            .accept-text { font-size:11px; margin:12px 0; }
            .signature-section { display:flex; justify-content:space-between; margin-top:60px; }
            .buyer-sig { text-align:left; }
            .seller-sig { text-align:right; }
            .sig-line { border-top:1px solid #000; width:200px; display:inline-block; margin-top:60px; }
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
            $businessHeaderHtml
            
            <table style='width:100%;border-collapse:collapse;margin:12px 0;'>
              <tr>
                <td style='width:50%;border:none;vertical-align:top;'>
                  $partyHtml
                </td>
                <td style='width:50%;border:none;vertical-align:top;'>
                  <table style='width:100%;border-collapse:collapse;'>
                    <tr>
                      <td style='border:none;padding:2px 0;'><strong>Order Number</strong></td>
                      <td style='border:none;padding:2px 0;'>: ${escapeHtml(voucher.voucherNo)}</td>
                    </tr>
                    <tr>
                      <td style='border:none;padding:2px 0;'><strong>Date</strong></td>
                      <td style='border:none;padding:2px 0;'>: ${escapeHtml(dateStr)}</td>
                    </tr>
                    <tr>
                      <td style='border:none;padding:2px 0;'><strong>Customer PO Number</strong></td>
                      <td style='border:none;padding:2px 0;'>: ${escapeHtml(extras.referenceNo.ifBlank { "-" })}</td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
            
            <div class='confirm-text'>Confirm this order your order.</div>
            
            <table style='width:100%;border-collapse:collapse;margin:12px 0;'>
              <tr class='items-header'>
                <th style='width:6%;'>No.</th>
                <th style='width:40%; text-align:left;'>Item Description</th>
                <th style='width:14%;'>Qty</th>
                <th style='width:18%;'>Ordered</th>
                <th style='width:22%;'>Total</th>
              </tr>
              $itemRows
            </table>
            
            <table style='width:100%;border-collapse:collapse;margin:12px 0;'>
              <tr>
                <td style='width:60%;border:none;'></td>
                <td style='width:20%;border:1px solid #000;padding:4px 8px;text-align:right;font-weight:bold;'>Sub-Total</td>
                <td style='width:20%;border:1px solid #000;padding:4px 8px;text-align:right;font-weight:bold;'>${formatMoney(grandTotal)}</td>
              </tr>
            </table>
            
            <div class='accept-text'>To accept this signature's order.</div>
            
            <div class='signature-section'>
              <div class='buyer-sig'>
                <div class='sig-line'></div>
                <div style='margin-top:4px;'>Buyer</div>
              </div>
              <div class='seller-sig'>
                <div style='margin-bottom:4px;'>Authorized Signature</div>
                <div style='font-weight:bold;'>( ${escapeHtml(business.businessName)} )</div>
              </div>
            </div>
          </div>
        </body>
        </html>
        """.trimIndent()
    }
    
    private fun buildBusinessHeader(business: BusinessProfile, showLogo: Boolean): String {
        val detailsHtml = buildString {
            append("<div class='company-name'>${escapeHtml(business.businessName)}</div>")
            if (business.address.isNotBlank()) append("<div>${escapeHtml(business.address).replace("\n", "<br/>")}</div>")
            if (business.city.isNotBlank() || business.pin.isNotBlank()) {
                append("<div>${escapeHtml(business.city)}${if (business.pin.isNotBlank()) " - ${escapeHtml(business.pin)}" else ""}</div>")
            }
            if (business.gstin.isNotBlank()) {
                append("<div>GSTIN: ${escapeHtml(business.gstin)}</div>")
            }
        }
        
        return if (showLogo) {
            """
            <table class='seller-grid' style='border:1px solid #000;'>
              <tr>
                <td class='seller-logo' style='border-right:1px solid #000;padding:8px;'><img src='${toFileUrl(business.logoPath!!)}'/></td>
                <td style='padding:8px;vertical-align:top;'>$detailsHtml</td>
              </tr>
            </table>
            """.trimIndent()
        } else {
            """
            <div style='border:1px solid #000;padding:8px;'>
              $detailsHtml
            </div>
            """.trimIndent()
        }
    }
    
    private fun buildPartySection(party: Party?): String = buildString {
        append("<div style='margin-bottom:4px;'><strong>To:</strong></div>")
        if (party != null) {
            append("<div><strong>${escapeHtml(party.name)}</strong></div>")
            if (party.address.isNotBlank()) {
                append("<div>${escapeHtml(party.address).replace("\n", "<br/>")}</div>")
            }
            if (party.city.isNotBlank() || party.pin.isNotBlank()) {
                append("<div>${escapeHtml(party.city)}${if (party.pin.isNotBlank()) " - ${escapeHtml(party.pin)}" else ""}</div>")
            }
        } else {
            append("<div>Cash / Walk-in Customer</div>")
        }
    }
}
