package com.zerobook.app.services.documents

import com.zerobook.app.data.BusinessProfile
import com.zerobook.app.data.Party
import java.text.DecimalFormat

/**
 * Shared HTML components for document templates.
 * These components are reused across different document types.
 */
object DocumentHtmlComponents {
    
    private val decimalFormat = DecimalFormat("#,##0.00")
    
    /**
     * Escape HTML special characters.
     */
    fun escapeHtml(value: String): String {
        return value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
    
    /**
     * Format a number as currency.
     */
    fun formatMoney(value: Double): String {
        return decimalFormat.format(value)
    }
    
    /**
     * Format quantity with 3 decimal places.
     */
    fun formatQty(value: Double): String {
        return String.format(java.util.Locale.ENGLISH, "%.3f", value)
    }
    
    /**
     * Convert file path to file URL for WebView.
     */
    fun toFileUrl(path: String): String {
        return if (path.startsWith("/")) "file://$path" else "file:///$path"
    }
    
    /**
     * Build the business header section with optional logo.
     * 
     * Reference design: Business name and address in a bordered box with logo on left.
     */
    fun buildBusinessHeader(business: BusinessProfile): String {
        val showLogo = business.showLogo && !business.logoPath.isNullOrBlank() 
            && java.io.File(business.logoPath!!).exists()
        
        val detailsHtml = buildString {
            append("<div style='font-size:13px;font-weight:bold;margin-bottom:4px;'>${escapeHtml(business.businessName)}</div>")
            if (business.address.isNotBlank()) {
                append("<div>${escapeHtml(business.address).replace("\n", "<br/>")}</div>")
            }
            if (business.city.isNotBlank() || business.pin.isNotBlank()) {
                append("<div>${escapeHtml(business.city)}${if (business.pin.isNotBlank()) " - ${escapeHtml(business.pin)}" else ""}</div>")
            }
            if (business.gstin.isNotBlank()) {
                append("<div>GSTIN: ${escapeHtml(business.gstin)}</div>")
            }
        }
        
        return if (showLogo) {
            """
            <table style='width:100%;border-collapse:collapse;border:1px solid #000;'>
              <tr>
                <td style='width:80px;padding:8px;border-right:1px solid #000;vertical-align:top;'>
                  <img src='${toFileUrl(business.logoPath!!)}' style='max-width:70px;max-height:70px;object-fit:contain;display:block;'/>
                </td>
                <td style='padding:8px;vertical-align:top;'>
                  $detailsHtml
                </td>
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
    
    /**
     * Build the party (To:) section.
     */
    fun buildPartySection(party: Party?, label: String = "To:"): String {
        return buildString {
            append("<div style='margin-bottom:8px;'><strong>${escapeHtml(label)}</strong></div>")
            if (party != null) {
                append("<div><strong>${escapeHtml(party.name)}</strong></div>")
                if (party.address.isNotBlank()) {
                    append("<div>${escapeHtml(party.address).replace("\n", "<br/>")}</div>")
                }
                if (party.city.isNotBlank() || party.pin.isNotBlank()) {
                    append("<div>${escapeHtml(party.city)}${if (party.pin.isNotBlank()) " - ${escapeHtml(party.pin)}" else ""}</div>")
                }
                if (party.gstin.isNullOrBlank().not()) {
                    append("<div>GSTIN: ${escapeHtml(party.gstin!!)}</div>")
                }
            } else {
                append("<div>Cash / Walk-in Customer</div>")
            }
        }
    }
    
    /**
     * Build common CSS styles for documents.
     */
    fun buildCommonStyles(): String {
        return """
        * { margin:0; padding:0; box-sizing:border-box; }
        body { font-family:Arial,Helvetica,sans-serif; font-size:11px; color:#111; background:#fff; }
        .page { width:100%; max-width:800px; margin:0 auto; padding:14px 18px; background:#fff; }
        table { width:100%; border-collapse:collapse; }
        td, th { padding:5px 6px; vertical-align:top; }
        .title { text-align:center; font-size:18px; font-weight:700; letter-spacing:0.5px; padding:8px 0 12px 0; }
        .subtitle { text-align:center; font-size:11px; color:#444; margin-bottom:8px; }
        .notice { text-align:center; font-size:11px; font-weight:bold; color:#c00; margin:8px 0; }
        .small-label { font-size:9px; color:#666; margin-bottom:4px; }
        .meta-value { font-size:11px; font-weight:700; margin-top:2px; }
        .section-body { line-height:1.45; font-size:11px; }
        .items-header th { background:#eef2fa; font-size:10px; font-weight:700; text-align:center; border:1px solid #000; }
        .num { white-space:nowrap; font-size:10px; }
        .right { text-align:right; }
        .center { text-align:center; }
        .bold { font-weight:700; }
        .terms-line { display:block; line-height:1.5; font-size:10px; }
        .signature { margin-top:40px; text-align:right; }
        .signature-line { border-top:1px solid #000; width:200px; display:inline-block; margin-top:60px; }
        @media print { body { -webkit-print-color-adjust:exact; } }
        """.trimIndent()
    }
    
    /**
     * Build A4-optimized CSS styles with proper page sizing and pagination support.
     * Use this for documents that need multi-page A4 support.
     */
    fun buildA4Styles(): String {
        return """
        * { margin:0; padding:0; box-sizing:border-box; }
        body { 
            font-family:Arial,Helvetica,sans-serif; 
            font-size:11px; 
            color:#111; 
            background:#fff;
        }
        @page { 
            size: A4 portrait; 
            margin: 10mm 12mm;
        }
        .page { 
            width:100%; 
            max-width:794px; 
            margin:0 auto; 
            padding:14px 18px; 
            background:#fff;
        }
        .continuation-header {
            border-bottom:1px solid #000;
            padding:4px 0;
            margin-bottom:8px;
            font-size:10px;
        }
        .page-number {
            text-align:center;
            font-size:9px;
            color:#666;
            margin-top:8px;
        }
        table { width:100%; border-collapse:collapse; table-layout:fixed; }
        td, th { padding:5px 6px; vertical-align:top; }
        .title { text-align:center; font-size:16px; font-weight:700; letter-spacing:0.5px; padding:4px 0 12px 0; }
        .subtitle { text-align:center; font-size:11px; color:#444; margin-bottom:8px; }
        .notice { text-align:center; font-size:11px; font-weight:bold; text-decoration:underline; margin:8px 0; }
        .small-label { font-size:9px; color:#666; margin-bottom:4px; }
        .meta-value { font-size:11px; font-weight:700; margin-top:2px; }
        .meta-subtext { font-size:9px; color:#444; margin-top:3px; line-height:1.35; }
        .section-body { line-height:1.45; font-size:11px; }
        .items-header th { background:#eef2fa; font-size:9.5px; font-weight:700; text-align:center; }
        .num { white-space:nowrap; font-size:9.4px; }
        .right { text-align:right; }
        .center { text-align:center; }
        .bold { font-weight:700; }
        .company-name { font-size:14px; font-weight:700; color:#000; display:block; margin-bottom:4px; }
        .buyer-name { font-size:12px; font-weight:700; margin-bottom:3px; }
        .seller-head { font-size:9px; color:#666; margin-bottom:6px; }
        .seller-grid { width:100%; border-collapse:collapse; table-layout:fixed; }
        .seller-grid td { border:none; padding:0; vertical-align:top; }
        .seller-logo { width:86px; padding-right:8px; }
        .seller-logo img { max-width:80px; max-height:80px; object-fit:contain; display:block; }
        .amount-words .value { display:block; font-size:11px; font-weight:700; margin-top:4px; line-height:1.45; }
        .terms-line { display:block; line-height:1.5; }
        .signatory { vertical-align:bottom; text-align:right; min-height:80px; }
        .signatory .for-line { font-size:11px; font-weight:700; margin-bottom:22px; }
        .summary-wrap { padding:0; }
        .summary-wrap > table { width:100%; border-collapse:collapse; table-layout:fixed; }
        @media print { 
            body { -webkit-print-color-adjust:exact; }
            .page { page-break-after:always; }
            .page:last-child { page-break-after:auto; }
        }
        """.trimIndent()
    }
    
    /**
     * Build a continuation page header for multi-page documents.
     * Shows business name, document title, and document number.
     */
    fun buildContinuationHeader(
        businessName: String,
        documentTitle: String,
        documentNumber: String
    ): String {
        return """
        <div class='continuation-header'>
            <table style='width:100%;border-collapse:collapse;'>
                <tr>
                    <td style='border:none;padding:2px 0;width:40%;'><strong>${escapeHtml(businessName)}</strong></td>
                    <td style='border:none;padding:2px 0;width:30%;text-align:center;'>${escapeHtml(documentTitle)}</td>
                    <td style='border:none;padding:2px 0;width:30%;text-align:right;'>${escapeHtml(documentNumber)}</td>
                </tr>
            </table>
        </div>
        """.trimIndent()
    }
    
    /**
     * Build page number display.
     */
    fun buildPageNumber(currentPage: Int, totalPages: Int): String {
        return if (totalPages > 1) {
            "<div class='page-number'>Page $currentPage of $totalPages</div>"
        } else {
            ""
        }
    }
}
