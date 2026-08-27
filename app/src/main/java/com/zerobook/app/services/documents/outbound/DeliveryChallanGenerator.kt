package com.zerobook.app.services.documents.outbound

import com.zerobook.app.data.BusinessProfile
import com.zerobook.app.data.Party
import com.zerobook.app.data.Voucher
import com.zerobook.app.data.VoucherItem
import com.zerobook.app.services.InvoiceGenerator
import com.zerobook.app.services.documents.DocumentGenerator
import com.zerobook.app.services.documents.DocumentHtmlComponents.escapeHtml
import com.zerobook.app.services.documents.DocumentHtmlComponents.formatQty
import com.zerobook.app.services.documents.DocumentHtmlComponents.toFileUrl
import com.zerobook.app.services.documents.DocumentType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generator for Delivery Challan documents.
 * 
 * Reference design characteristics:
 * - Large "DELIVERY CHALLAN" title at top in bordered box
 * - Business header with logo and address
 * - "DELIVERY CHALLAN" subtitle
 * - "Issued under Rule 55 of CGST Rules, 2017"
 * - "THIS IS NOT A TAX INVOICE" notice
 * - Challan No., Date, Consignee, Vehicle No., GSTIN, Place of Supply
 * - Item table with: Description of Goods, HSN Code, Quantity, UOM
 * - Purpose of Supply checkboxes (Sale, Job Work, Own Use, etc.)
 * - Consignor Signature and Received in good condition by Signature sections
 */
class DeliveryChallanGenerator : DocumentGenerator {
    
    override val documentType = DocumentType.DELIVERY_CHALLAN
    
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
        
        // Build item rows
        val itemRows = items.mapIndexed { index, item ->
            """
            <tbody class='no-break-inside'>
            <tr>
              <td>${escapeHtml(item.productName)}</td>
              <td class='center'>${escapeHtml(item.hsnCode)}</td>
              <td class='center'>${formatQty(item.qty)}</td>
              <td class='center'>${escapeHtml(item.unit)}</td>
            </tr>
            </tbody>
            """.trimIndent()
        }.joinToString("\n")
        
        // Build business header
        val businessHeaderHtml = buildBusinessHeader(business, showLogo)
        
        // Build consignee section
        val consigneeHtml = buildConsigneeSection(party, business)
        
        // Signature html
        val signatureHtml = if (showSignature) {
            "<img src='${toFileUrl(business.signaturePath!!)}' style='max-height:62px;max-width:180px;object-fit:contain;display:block;margin-left:auto;'/>"
        } else {
            "<div style='height:62px;'></div>"
        }
        
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
            .title { text-align:center; font-size:18px; font-weight:700; letter-spacing:0.5px; padding:8px 0; border:2px solid #000; margin-bottom:8px; }
            .subtitle { text-align:center; font-size:12px; font-weight:700; margin-bottom:4px; }
            .notice { text-align:center; font-size:11px; font-weight:bold; text-decoration:underline; margin:8px 0; }
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
            .purpose-section { margin:12px 0; }
            .purpose-label { font-weight:bold; margin-bottom:6px; }
            .checkbox-group { display:flex; gap:16px; flex-wrap:wrap; }
            .checkbox-item { display:flex; align-items:center; gap:4px; }
            .checkbox { width:12px; height:12px; border:1px solid #000; display:inline-block; }
            .signature-section { display:flex; justify-content:space-between; margin-top:40px; }
            .consignor-sig { text-align:left; }
            .receiver-sig { text-align:right; }
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
            
            <div class='subtitle'>$title</div>
            <div style='text-align:center;font-size:10px;margin-bottom:8px;'>Issued under Rule 55 of CGST Rules, 2017</div>
            <div class='notice'>THIS IS NOT A TAX INVOICE</div>
            
            <table style='width:100%;border-collapse:collapse;margin:8px 0;'>
              <tr>
                <td style='width:50%;border:none;vertical-align:top;'>
                  <table style='width:100%;border-collapse:collapse;'>
                    <tr>
                      <td style='border:none;padding:2px 0;width:100px;'><strong>Challan No.:</strong></td>
                      <td style='border:none;padding:2px 0;'>${escapeHtml(voucher.voucherNo)}</td>
                    </tr>
                    <tr>
                      <td style='border:none;padding:2px 0;'><strong>Consignee:</strong></td>
                      <td style='border:none;padding:2px 0;'>${escapeHtml(party?.name ?: "N/A")}</td>
                    </tr>
                    <tr>
                      <td style='border:none;padding:2px 0;'><strong>GSTIN :</strong></td>
                      <td style='border:none;padding:2px 0;'>${escapeHtml(party?.gstin ?: business.gstin)}</td>
                    </tr>
                  </table>
                </td>
                <td style='width:50%;border:none;vertical-align:top;'>
                  <table style='width:100%;border-collapse:collapse;'>
                    <tr>
                      <td style='border:none;padding:2px 0;width:100px;'><strong>Date:</strong></td>
                      <td style='border:none;padding:2px 0;'>${escapeHtml(dateStr)}</td>
                    </tr>
                    <tr>
                      <td style='border:none;padding:2px 0;'><strong>Vehicle No.:</strong></td>
                      <td style='border:none;padding:2px 0;'>${escapeHtml(voucher.vehicleNo.ifBlank { "-" })}</td>
                    </tr>
                    <tr>
                      <td style='border:none;padding:2px 0;'><strong>Place of Supply:</strong></td>
                      <td style='border:none;padding:2px 0;'>${escapeHtml(party?.state?.ifBlank { business.state } ?: business.state)}</td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
            
            <table style='width:100%;border-collapse:collapse;margin:12px 0;'>
              <tr class='items-header'>
                <th style='width:40%; text-align:left;'>Description of Goods</th>
                <th style='width:20%;'>HSN Code</th>
                <th style='width:20%;'>Quantity</th>
                <th style='width:20%;'>UOM</th>
              </tr>
              $itemRows
            </table>
            
            <div class='purpose-section'>
              <div class='purpose-label'>Purpose of Supply:</div>
              <div class='checkbox-group'>
                <div class='checkbox-item'><span class='checkbox'></span> Sale</div>
                <div class='checkbox-item'><span class='checkbox'></span> Job Work</div>
                <div class='checkbox-item'><span class='checkbox'></span> Own Use</div>
                <div class='checkbox-item'><span class='checkbox'></span> Others</div>
              </div>
            </div>
            
            <div class='signature-section'>
              <div class='consignor-sig'>
                <div class='sig-line'></div>
                <div style='margin-top:4px;'>Consignor Signature</div>
                $signatureHtml
              </div>
              <div class='receiver-sig'>
                <div>Received in good condition by</div>
                <div class='sig-line'></div>
                <div style='margin-top:4px;'>Signature and stamp</div>
              </div>
            </div>
          </div>
        </body>
        </html>
        """.trimIndent()
    }
    
    private fun buildBusinessHeader(business: BusinessProfile, showLogo: Boolean): String {
        val detailsHtml = buildString {
            append("<div style='font-size:13px;font-weight:bold;margin-bottom:4px;'>${escapeHtml(business.businessName)}</div>")
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
    
    private fun buildConsigneeSection(party: Party?, business: BusinessProfile): String = buildString {
        append("<div style='margin-bottom:4px;'><strong>Consignee:</strong></div>")
        if (party != null) {
            append("<div><strong>${escapeHtml(party.name)}</strong></div>")
            if (party.address.isNotBlank()) {
                append("<div>${escapeHtml(party.address).replace("\n", "<br/>")}</div>")
            }
            if (party.city.isNotBlank() || party.pin.isNotBlank()) {
                append("<div>${escapeHtml(party.city)}${if (party.pin.isNotBlank()) " - ${escapeHtml(party.pin)}" else ""}</div>")
            }
        } else {
            append("<div>N/A</div>")
        }
    }
}
