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
 * Generator for Credit Note documents.
 * 
 * Reference design characteristics:
 * - Large "CREDIT NOTE" title at top in bordered box
 * - "Issued under Section 34 of CGST Act, 2017" subtitle
 * - Business header with logo and address
 * - Credit Note No. and Date on right side
 * - Original Tax Invoice No. and Date below
 * - Item table with: Reason for issuance, Description of Goods/Services, Original Taxable Value, Revised Taxable Value, Difference (CGST, SGST)
 * - Totals section with CGST, SGST, Total Credit
 * - Authorized Signature at bottom
 */
class CreditNoteGenerator : DocumentGenerator {
    
    override val documentType = DocumentType.CREDIT_NOTE
    
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
        val showSignature = business.showSignature && !business.signaturePath.isNullOrBlank() 
            && java.io.File(business.signaturePath!!).exists()
        
        // Calculate totals
        val totalTaxableAmount = items.sumOf { it.taxableAmount }
        val totalCgst = items.sumOf { it.cgstAmount }
        val totalSgst = items.sumOf { it.sgstAmount }
        val totalIgst = items.sumOf { it.igstAmount }
        val totalCredit = totalTaxableAmount + totalCgst + totalSgst + totalIgst
        
        // Build item rows
        val itemRows = items.mapIndexed { index, item ->
            """
            <tbody class='no-break-inside'>
            <tr>
              <td>${escapeHtml(voucher.narration.ifBlank { "Sales Return" })}</td>
              <td>${escapeHtml(item.productName)}</td>
              <td class='right'>${formatMoney(item.taxableAmount)}</td>
              <td class='right'>${formatMoney(item.taxableAmount)}</td>
              <td class='right'>0.00</td>
              <td class='right'>${formatMoney(item.cgstAmount)}</td>
              <td class='right'>${formatMoney(item.sgstAmount)}</td>
            </tr>
            </tbody>
            """.trimIndent()
        }.joinToString("\n")
        
        // Build business header
        val businessHeaderHtml = buildBusinessHeader(business, showLogo)
        
        // Signature html
        val signatureHtml = if (showSignature) {
            "<img src='${toFileUrl(business.signaturePath!!)}' style='max-height:62px;max-width:180px;object-fit:contain;display:block;margin-left:auto;'/>"
        } else {
            "<div style='height:62px;'></div>"
        }
        
        // Get original invoice reference from extras
        val originalInvoiceRef = extras.referenceNo.ifBlank { extras.otherReferences.ifBlank { "-" } }
        
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
            .title { text-align:center; font-size:18px; font-weight:700; letter-spacing:0.5px; padding:8px 0; border:2px solid #000; margin-bottom:4px; }
            .subtitle { text-align:center; font-size:10px; margin-bottom:8px; }
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
            .totals-table { width:50%; margin-left:auto; }
            .totals-table td { padding:4px 8px; }
            .signature { margin-top:40px; text-align:right; }
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
            <div class='subtitle'>Issued under Section 34 of CGST Act, 2017</div>
            
            <table style='width:100%;border-collapse:collapse;margin:12px 0;'>
              <tr>
                <td style='width:50%;border:none;vertical-align:top;'>
                  $businessHeaderHtml
                </td>
                <td style='width:50%;border:none;vertical-align:top;'>
                  <table style='width:100%;border-collapse:collapse;'>
                    <tr>
                      <td style='border:none;padding:2px 0;'><strong>Credit Note No.</strong></td>
                      <td style='border:none;padding:2px 0;'>: ${escapeHtml(voucher.voucherNo)}</td>
                    </tr>
                    <tr>
                      <td style='border:none;padding:2px 0;'><strong>Date</strong></td>
                      <td style='border:none;padding:2px 0;'>: ${escapeHtml(dateStr)}</td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
            
            <table style='width:100%;border-collapse:collapse;margin:8px 0;'>
              <tr>
                <td style='width:50%;border:none;padding:2px 0;'><strong>Original Tax Invoice No.:</strong> ${escapeHtml(originalInvoiceRef)}</td>
                <td style='width:50%;border:none;padding:2px 0;'><strong>Date:</strong> ${escapeHtml(dateStr)}</td>
              </tr>
            </table>
            
            <table style='width:100%;border-collapse:collapse;margin:12px 0;'>
              <tr class='items-header'>
                <th style='width:18%;'>Reason for issuance</th>
                <th style='width:24%; text-align:left;'>Description of Goods/Services</th>
                <th style='width:14%;'>Original Taxable Value</th>
                <th style='width:14%;'>Revised Taxable Value</th>
                <th style='width:10%;'>Difference</th>
                <th style='width:10%;'>CGST</th>
                <th style='width:10%;'>SGST</th>
              </tr>
              $itemRows
            </table>
            
            <table class='totals-table' style='margin:12px 0;'>
              <tr>
                <td style='border:1px solid #000;padding:4px 8px;text-align:right;'><strong>Total</strong></td>
                <td style='border:1px solid #000;padding:4px 8px;text-align:right;'>${formatMoney(totalTaxableAmount)}</td>
                <td style='border:1px solid #000;padding:4px 8px;text-align:right;'>${formatMoney(totalTaxableAmount)}</td>
                <td style='border:1px solid #000;padding:4px 8px;text-align:right;'>0.00</td>
                <td style='border:1px solid #000;padding:4px 8px;text-align:right;'>${formatMoney(totalCgst)}</td>
                <td style='border:1px solid #000;padding:4px 8px;text-align:right;'>${formatMoney(totalSgst)}</td>
              </tr>
            </table>
            
            <table class='totals-table' style='margin:12px 0;'>
              <tr>
                <td style='border:none;padding:4px 8px;text-align:right;'><strong>CGST</strong></td>
                <td style='border:none;padding:4px 8px;text-align:right;'>${formatMoney(totalCgst)}</td>
              </tr>
              <tr>
                <td style='border:none;padding:4px 8px;text-align:right;'><strong>SGST</strong></td>
                <td style='border:none;padding:4px 8px;text-align:right;'>${formatMoney(totalSgst)}</td>
              </tr>
              <tr>
                <td style='border:1px solid #000;padding:4px 8px;text-align:right;font-weight:bold;'><strong>Total Credit</strong></td>
                <td style='border:1px solid #000;padding:4px 8px;text-align:right;font-weight:bold;'>${formatMoney(totalCredit)}</td>
              </tr>
            </table>
            
            <div class='signature'>
              <div class='sig-line'></div>
              <div style='margin-top:4px;'>Authorized Signature</div>
              $signatureHtml
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
}
