package com.zerobook.app.services

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailAutomationServiceTest {
    @Test
    fun rendersTemplateWithPartyAndInvoicePlaceholders() {
        val rendered = EmailAutomationService.renderTemplate(
            template = "Hello {{customer_name}}, invoice {{invoice_number}} due {{due_date}}.",
            customerName = "Asha",
            invoiceNumber = "INV-1001",
            amount = "₹5000",
            dueDate = "01-Jul-2026"
        )

        assertTrue(rendered.contains("Asha"))
        assertTrue(rendered.contains("INV-1001"))
        assertTrue(rendered.contains("₹5000"))
        assertFalse(rendered.contains("{{customer_name}}"))
    }

    @Test
    fun allowsAutomationOnlyWhenConsentAndGmailConnectionExist() {
        assertFalse(
            EmailAutomationService.canEnableAutomation(
                consentAccepted = false,
                gmailConnected = true,
                businessEmailConfigured = true
            )
        )
        assertFalse(
            EmailAutomationService.canEnableAutomation(
                consentAccepted = true,
                gmailConnected = false,
                businessEmailConfigured = true
            )
        )
        assertTrue(
            EmailAutomationService.canEnableAutomation(
                consentAccepted = true,
                gmailConnected = true,
                businessEmailConfigured = true
            )
        )
    }
}
