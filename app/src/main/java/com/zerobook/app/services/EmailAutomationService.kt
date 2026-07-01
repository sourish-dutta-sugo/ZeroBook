package com.zerobook.app.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.common.Scopes
import com.google.android.gms.tasks.Tasks
import com.zerobook.app.BuildConfig
import com.zerobook.app.data.EmailAccount
import com.zerobook.app.data.EmailAutomationRule
import com.zerobook.app.data.EmailHistory
import com.zerobook.app.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

object EmailAutomationService {
    private const val PREFS_NAME = "email_automation_secure"
    private const val KEY_CONSENT = "consent_accepted"
    private const val KEY_ENABLED = "automation_enabled"
    private const val KEY_ACCOUNT_ID = "account_id"
    private const val KEY_GMAIL_ADDRESS = "gmail_address"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_OAUTH_STATUS = "oauth_status"

    private const val DEFAULT_TEMPLATE = """
Hello {{customer_name}},

This is a reminder regarding your pending payment.

Invoice: {{invoice_number}}
Amount: {{amount}}
Due Date: {{due_date}}

Thank you.
"""

    data class RecipientUi(
        val customerId: String,
        val customerName: String,
        val customerEmail: String,
        val dueAmount: Double,
        val dueDateLabel: String,
        val invoiceReference: String,
        val transactionReference: String
    )

    fun canEnableAutomation(consentAccepted: Boolean, gmailConnected: Boolean, businessEmailConfigured: Boolean): Boolean =
        consentAccepted && gmailConnected && businessEmailConfigured

    fun defaultTemplate(): String = DEFAULT_TEMPLATE.trim()

    fun defaultSubject(businessName: String): String =
        if (businessName.isBlank()) "Payment Reminder - Pending Invoice" else "Payment Reminder - $businessName"

    fun renderTemplate(template: String, customerName: String, invoiceNumber: String, amount: String, dueDate: String): String =
        template
            .replace("{{customer_name}}", customerName.ifBlank { "Customer" })
            .replace("{{invoice_number}}", invoiceNumber.ifBlank { "N/A" })
            .replace("{{amount}}", amount.ifBlank { "N/A" })
            .replace("{{due_date}}", dueDate.ifBlank { "N/A" })

    fun getConsentAccepted(context: Context): Boolean = securePrefs(context).getBoolean(KEY_CONSENT, false)
    fun setConsentAccepted(context: Context, accepted: Boolean) = securePrefs(context).edit().putBoolean(KEY_CONSENT, accepted).apply()

    fun isAutomationEnabled(context: Context): Boolean = securePrefs(context).getBoolean(KEY_ENABLED, false)
    fun setAutomationEnabled(context: Context, enabled: Boolean) = securePrefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()

    fun buildRecipients(parties: List<com.zerobook.app.data.Party>, bills: List<com.zerobook.app.data.BillReceivable>): List<RecipientUi> =
        bills
            .filter { it.outstandingAmount > 0.0 }
            .groupBy { it.partyId }
            .mapNotNull { (partyId, groupedBills) ->
                val party = parties.find { it.id == partyId } ?: return@mapNotNull null
                if (party.email.isBlank()) return@mapNotNull null
                val dueDateLabel = groupedBills.mapNotNull { it.dueDate }.minOrNull()?.let { millis ->
                    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"))
                }.orEmpty()
                RecipientUi(
                    customerId = party.id,
                    customerName = party.name,
                    customerEmail = party.email,
                    dueAmount = groupedBills.sumOf { it.outstandingAmount },
                    dueDateLabel = dueDateLabel,
                    invoiceReference = groupedBills.joinToString { it.voucherNo.orEmpty().ifBlank { "INV" } },
                    transactionReference = groupedBills.joinToString { it.voucherNo.orEmpty().ifBlank { "TRX" } }
                )
            }
            .sortedBy { it.customerName.lowercase() }

    fun createSignInIntent(context: Context): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/gmail.send"))
            .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        return client.signInIntent
    }

    suspend fun completeSignIn(context: Context, data: Intent?): Result<EmailAccount> = withContext(Dispatchers.IO) {
        runCatching {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).let { task ->
                Tasks.await(task)
            }
            val accountEmail = account.email.orEmpty().ifBlank { account.displayName.orEmpty() }
            val googleAccount = account.account ?: throw IllegalStateException("Google account not found")
            val token = GoogleAuthUtil.getToken(
                context,
                googleAccount,
                "oauth2:https://www.googleapis.com/auth/gmail.send https://www.googleapis.com/auth/userinfo.email"
            )
            val emailAccount = EmailAccount(
                accountId = UUID.randomUUID().toString(),
                gmailAddress = accountEmail,
                oauthStatus = "ACTIVE",
                tokenReference = token
            )
            saveConnectedAccount(context, emailAccount)
            emailAccount
        }
    }

    fun getConnectedAccount(context: Context): EmailAccount? =
        securePrefs(context).run {
            val accountId = getString(KEY_ACCOUNT_ID, null) ?: return null
            val gmailAddress = getString(KEY_GMAIL_ADDRESS, null) ?: return null
            val token = getString(KEY_ACCESS_TOKEN, null) ?: return null
            EmailAccount(
                accountId = accountId,
                gmailAddress = gmailAddress,
                oauthStatus = getString(KEY_OAUTH_STATUS, "ACTIVE") ?: "ACTIVE",
                tokenReference = token
            )
        }

    fun isGmailConnected(context: Context): Boolean = getConnectedAccount(context) != null

    fun disconnectGmail(context: Context) {
        securePrefs(context).edit().remove(KEY_ACCOUNT_ID).remove(KEY_GMAIL_ADDRESS).remove(KEY_ACCESS_TOKEN).remove(KEY_OAUTH_STATUS).apply()
    }

    private fun saveConnectedAccount(context: Context, account: EmailAccount) {
        securePrefs(context).edit()
            .putString(KEY_ACCOUNT_ID, account.accountId)
            .putString(KEY_GMAIL_ADDRESS, account.gmailAddress)
            .putString(KEY_ACCESS_TOKEN, account.tokenReference)
            .putString(KEY_OAUTH_STATUS, account.oauthStatus)
            .apply()
    }

    suspend fun sendAutomationEmail(context: Context, rule: EmailAutomationRule, businessName: String): Result<String> = withContext(Dispatchers.IO) {
        val connectedAccount = getConnectedAccount(context) ?: return@withContext Result.failure(IllegalStateException("No Gmail account connected"))
        val recipient = rule.customerEmail.ifBlank { return@withContext Result.failure(IllegalStateException("Customer email unavailable")) }
        val subject = rule.subject.ifBlank { defaultSubject(businessName) }
        val body = renderTemplate(
            template = rule.template.ifBlank { defaultTemplate() },
            customerName = rule.customerName,
            invoiceNumber = rule.invoiceReference,
            amount = "₹${rule.dueAmount}",
            dueDate = rule.sendMode.takeIf { it != "CUSTOM_DATE" }?.let { "" } ?: ""
        )
        val message = buildMessage(connectedAccount.gmailAddress, recipient, subject, body)
        val raw = Base64.encodeToString(message.toByteArray(StandardCharsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP)
        val payload = JSONObject().put("raw", raw).toString()
        val connection = (URL("https://gmail.googleapis.com/gmail/v1/users/me/messages/send").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Authorization", "Bearer ${connectedAccount.tokenReference}")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }
        connection.outputStream.use { it.write(payload.toByteArray(StandardCharsets.UTF_8)) }
        val code = connection.responseCode
        if (code in 200..299) {
            val db = AppDatabase.getDatabase(context.applicationContext)
            db.emailHistoryDao().insertHistory(
                EmailHistory(
                    id = UUID.randomUUID().toString(),
                    recipient = recipient,
                    subject = subject,
                    timestamp = System.currentTimeMillis(),
                    status = "SENT",
                    attachment = rule.invoiceReference.ifBlank { null },
                    details = "Delivered through Gmail API"
                )
            )
            Result.success("Email sent")
        } else {
            val db = AppDatabase.getDatabase(context.applicationContext)
            db.emailHistoryDao().insertHistory(
                EmailHistory(
                    id = UUID.randomUUID().toString(),
                    recipient = recipient,
                    subject = subject,
                    timestamp = System.currentTimeMillis(),
                    status = "FAILED",
                    attachment = rule.invoiceReference.ifBlank { null },
                    details = connection.errorStream?.bufferedReader()?.readText().orEmpty()
                )
            )
            Result.failure(IllegalStateException("Gmail API request failed with $code"))
        }
    }

    fun buildMessage(from: String, recipient: String, subject: String, body: String): String {
        val headers = listOf(
            "From: $from",
            "To: $recipient",
            "Subject: $subject",
            "MIME-Version: 1.0",
            "Content-Type: text/plain; charset=UTF-8"
        )
        return headers.joinToString("\r\n") + "\r\n\r\n" + body
    }

    private fun securePrefs(context: Context) = EncryptedSharedPreferences.create(
        PREFS_NAME,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
