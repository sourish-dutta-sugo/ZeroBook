package com.zerobook.app.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerobook.app.data.AppDatabase
import com.zerobook.app.data.EmailAutomationRule
import com.zerobook.app.data.EmailHistory
import com.zerobook.app.services.EmailAutomationService
import com.zerobook.app.ui.AppViewModel
import com.zerobook.app.ui.theme.AppColors
import com.zerobook.app.data.Utils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EmailAutomationSection(viewModel: AppViewModel) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val parties by viewModel.parties.collectAsState(initial = emptyList())
    val bills by viewModel.billsReceivable.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    val database = remember { AppDatabase.getDatabase(context.applicationContext) }
    var automationEnabled by remember { mutableStateOf(EmailAutomationService.isAutomationEnabled(context)) }
    var consentAccepted by remember { mutableStateOf(EmailAutomationService.getConsentAccepted(context)) }
    var connectedAccount by remember { mutableStateOf(EmailAutomationService.getConnectedAccount(context)) }
    var showConsentDialog by remember { mutableStateOf(false) }
    var pendingToggle by remember { mutableStateOf(false) }
    var rules by remember { mutableStateOf<List<EmailAutomationRule>>(emptyList()) }
    var history by remember { mutableStateOf<List<EmailHistory>>(emptyList()) }
    var isConnecting by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<EmailAutomationRule?>(null) }

    val recipients = remember(parties, bills) { EmailAutomationService.buildRecipients(parties, bills) }
    val missingEmailRecipients = remember(parties, bills) {
        bills
            .filter { it.outstandingAmount > 0.0 }
            .groupBy { it.partyId }
            .mapNotNull { (partyId, _) ->
                val party = parties.find { it.id == partyId } ?: return@mapNotNull null
                if (party.email.isBlank()) party else null
            }
            .sortedBy { it.name.lowercase() }
    }

    LaunchedEffect(Unit) {
        rules = database.emailAutomationRuleDao().getAllRulesSync()
        history = database.emailHistoryDao().getHistorySync(limit = 6)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            scope.launch {
                isConnecting = true
                val outcome = EmailAutomationService.completeSignIn(context, result.data)
                isConnecting = false
                outcome.onSuccess { account ->
                    connectedAccount = account
                    if (consentAccepted && profile?.email?.isNotBlank() == true) {
                        automationEnabled = true
                        EmailAutomationService.setAutomationEnabled(context, true)
                    }
                    Toast.makeText(context, "Connected to ${account.gmailAddress}", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, it.message ?: "Gmail connection failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = AppColors.primary)
                    Text("Email Automation", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.textPrimary)
                }
                Text(
                    text = if (automationEnabled) "Automation is active and ready to send reminders through your connected Gmail account." else "Automation stays off until consent, Gmail authentication, and a business email are available.",
                    color = AppColors.textSecondary,
                    fontSize = 13.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Enable automation", color = AppColors.textPrimary, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = automationEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                if (!consentAccepted) {
                                    pendingToggle = true
                                    showConsentDialog = true
                                } else if (connectedAccount == null) {
                                    Toast.makeText(context, "Connect your Gmail account first", Toast.LENGTH_SHORT).show()
                                } else if (profile?.email.isNullOrBlank()) {
                                    Toast.makeText(context, "Set your business email in profile before enabling automation", Toast.LENGTH_SHORT).show()
                                } else {
                                    automationEnabled = true
                                    EmailAutomationService.setAutomationEnabled(context, true)
                                }
                            } else {
                                automationEnabled = false
                                EmailAutomationService.setAutomationEnabled(context, false)
                            }
                        }
                    )
                }
                Divider()
                Text("Sender", fontWeight = FontWeight.SemiBold, color = AppColors.textSecondary)
                Text(
                    text = "From: ${profile?.businessName.orEmpty().ifBlank { "Your business" }} <${connectedAccount?.gmailAddress ?: profile?.email.orEmpty().ifBlank { "connect gmail" }}>",
                    fontSize = 13.sp,
                    color = AppColors.textPrimary
                )
                if (connectedAccount == null) {
                    Button(
                        onClick = {
                            if (!consentAccepted) {
                                pendingToggle = false
                                showConsentDialog = true
                            } else {
                                launcher.launch(EmailAutomationService.createSignInIntent(context))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                    ) {
                        if (isConnecting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text("Connect Gmail Account")
                        }
                    }
                } else {
                    OutlinedButton(onClick = { EmailAutomationService.disconnectGmail(context); connectedAccount = null; automationEnabled = false; EmailAutomationService.setAutomationEnabled(context, false) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Disconnect Gmail")
                    }
                }
            }
        }

        if (recipients.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("No pending recipients available", fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)
                    Text("Recipients will appear here when a customer has outstanding dues and an email address saved.", color = AppColors.textSecondary, fontSize = 13.sp)
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Recipients", fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)
                    recipients.forEach { recipient ->
                        val existingRule = rules.find { it.customerId == recipient.customerId }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, AppColors.border, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(recipient.customerName, fontWeight = FontWeight.Medium, color = AppColors.textPrimary)
                                Text(recipient.customerEmail, fontSize = 12.sp, color = AppColors.textSecondary)
                                Text("Due ${Utils.formatIndianCurrency(recipient.dueAmount)} • ${recipient.dueDateLabel.ifBlank { "Due soon" }}", fontSize = 12.sp, color = AppColors.textSecondary)
                                Text("Invoice ${recipient.invoiceReference.ifBlank { "N/A" }}", fontSize = 12.sp, color = AppColors.textSecondary)
                                existingRule?.let { rule ->
                                    Text("Schedule ${rule.schedule} • ${rule.frequency.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }}", fontSize = 12.sp, color = AppColors.primary)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextButton(onClick = { editingRule = existingRule ?: EmailAutomationRule(id = "", customerId = recipient.customerId, customerName = recipient.customerName, customerEmail = recipient.customerEmail, subject = EmailAutomationService.defaultSubject(profile?.businessName.orEmpty()), template = EmailAutomationService.defaultTemplate(), dueAmount = recipient.dueAmount, dueDateLabel = recipient.dueDateLabel, invoiceReference = recipient.invoiceReference) }) {
                                    Text("Edit")
                                }
                                IconButtonDelete(onClick = {
                                    existingRule?.let { rule ->
                                        scope.launch { database.emailAutomationRuleDao().deleteRule(rule.id) ; rules = database.emailAutomationRuleDao().getAllRulesSync() }
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }

        if (missingEmailRecipients.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Customer email unavailable", fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)
                    missingEmailRecipients.forEach { party ->
                        Text("• ${party.name}", fontSize = 13.sp, color = AppColors.textSecondary)
                    }
                    Text("Open the customer profile and add an email address so reminders can be sent automatically.", fontSize = 12.sp, color = AppColors.textSecondary)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Recent activity", fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)
                if (history.isEmpty()) {
                    Text("No automation activity yet. Once a reminder is sent, it will appear here.", color = AppColors.textSecondary, fontSize = 13.sp)
                } else {
                    history.forEach { entry ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.recipient, fontSize = 12.sp, color = AppColors.textPrimary)
                                Text(entry.subject, fontSize = 11.sp, color = AppColors.textSecondary)
                            }
                            Text(entry.status, fontSize = 12.sp, color = if (entry.status == "SENT") AppColors.primary else AppColors.error)
                        }
                    }
                }
            }
        }
    }

    if (showConsentDialog) {
        AlertDialog(
            onDismissRequest = { showConsentDialog = false },
            title = { Text("Email Automation Consent") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("I agree to provide my email information to this application for Gmail automation purposes.", fontWeight = FontWeight.Bold)
                    Text("By enabling this feature, you allow this application to securely connect with your Gmail account through Google Authentication and send automated transaction-related emails, payment reminders, invoices, and notifications to your customers.")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = consentAccepted, onCheckedChange = { consentAccepted = it })
                        Text("I agree")
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = consentAccepted,
                    onClick = {
                        EmailAutomationService.setConsentAccepted(context, consentAccepted)
                        showConsentDialog = false
                        if (pendingToggle) {
                            pendingToggle = false
                            if (connectedAccount == null) {
                                launcher.launch(EmailAutomationService.createSignInIntent(context))
                            } else if (profile?.email.isNullOrBlank()) {
                                Toast.makeText(context, "Set your business email in profile before enabling automation", Toast.LENGTH_SHORT).show()
                            } else {
                                automationEnabled = true
                                EmailAutomationService.setAutomationEnabled(context, true)
                            }
                        }
                    }
                ) { Text("Agree & Continue") }
            },
            dismissButton = {
                TextButton(onClick = { showConsentDialog = false; automationEnabled = false; EmailAutomationService.setAutomationEnabled(context, false) }) { Text("Cancel") }
            }
        )
    }

    editingRule?.let { rule ->
        val draftSubject = remember(rule.id) { mutableStateOf(rule.subject.ifBlank { EmailAutomationService.defaultSubject(profile?.businessName.orEmpty()) }) }
        val draftTemplate = remember(rule.id) { mutableStateOf(rule.template.ifBlank { EmailAutomationService.defaultTemplate() }) }
        val draftSchedule = remember(rule.id) { mutableStateOf(rule.schedule.ifBlank { "09:00" }) }
        val draftFrequency = remember(rule.id) { mutableStateOf(rule.frequency.ifBlank { "DAILY" }) }
        val draftMode = remember(rule.id) { mutableStateOf(rule.sendMode.ifBlank { "IMMEDIATELY" }) }
        AlertDialog(
            onDismissRequest = { editingRule = null },
            title = { Text("Automation Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(rule.customerName, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = draftSubject.value, onValueChange = { draftSubject.value = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = draftTemplate.value, onValueChange = { draftTemplate.value = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
                    OutlinedTextField(value = draftSchedule.value, onValueChange = { draftSchedule.value = it }, label = { Text("Send time") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = draftFrequency.value, onValueChange = { draftFrequency.value = it }, label = { Text("Repeat frequency") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = draftMode.value, onValueChange = { draftMode.value = it }, label = { Text("Send mode") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    val updatedRule = rule.copy(
                        subject = draftSubject.value,
                        template = draftTemplate.value,
                        schedule = draftSchedule.value,
                        frequency = draftFrequency.value,
                        sendMode = draftMode.value
                    )
                    scope.launch {
                        database.emailAutomationRuleDao().upsertRule(updatedRule)
                        rules = database.emailAutomationRuleDao().getAllRulesSync()
                        editingRule = null
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingRule = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun IconButtonDelete(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Icon(Icons.Default.Delete, contentDescription = null, tint = AppColors.error)
    }
}
