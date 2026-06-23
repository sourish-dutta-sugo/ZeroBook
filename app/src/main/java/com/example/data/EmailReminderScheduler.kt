package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.services.EmailComposer
import com.example.services.InvoiceGenerator
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import java.util.Properties

private const val REMINDER_PREFS = "zerobook_pref"
private const val KEY_LOGS = "email_logs_list"
private const val KEY_SELECTED_RECIPIENTS = "email_selected_recipients"
private const val KEY_SCHEDULED_AT = "email_scheduled_at"
private const val KEY_USE_CUSTOM_TEMPLATE = "email_use_custom_template"
private const val KEY_CUSTOM_TEMPLATE = "email_custom_template"
private const val UNIVERSAL_WORK_NAME = "universal_reminder"
private const val DUE_REMINDER_CHANNEL_ID = "payment_due_reminders"

private const val DEFAULT_TEMPLATE = """
Dear {party_name},

This is a reminder for invoice {invoice_no}.
Amount due: {amount}
Due date: {due_date}
Outstanding total: {outstanding}

Regards,
{business_name}
{business_phone}
"""

data class ReminderRecipientUi(
    val partyId: String,
    val partyName: String,
    val email: String,
    val dueAmount: Double
)

data class ReminderSchedule(
    val id: String,
    val partyId: String,
    val partyName: String,
    val reminderType: String,
    val scheduledDate: String,
    val scheduledTime: String,
    val intervalDays: Int,
    val isActive: Boolean,
    val lastSent: String,
    val createdAt: Long
)

data class PartyReminderConfig(
    val sendOnDueDate: Boolean = true,
    val daysBeforeDue: Int = 3,
    val remindAfterDueEveryDays: Int = 7,
    val untilPaid: Boolean = true
)

object EmailReminderScheduler {
    fun isUsingCustomTemplate(context: Context): Boolean =
        context.getSharedPreferences(REMINDER_PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_USE_CUSTOM_TEMPLATE, false)

    fun setUseCustomTemplate(context: Context, enabled: Boolean) {
        context.getSharedPreferences(REMINDER_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_USE_CUSTOM_TEMPLATE, enabled)
            .apply()
    }

    fun loadTemplate(context: Context): String =
        context.getSharedPreferences(REMINDER_PREFS, Context.MODE_PRIVATE)
            .getString(KEY_CUSTOM_TEMPLATE, DEFAULT_TEMPLATE)
            .orEmpty()
            .ifBlank { DEFAULT_TEMPLATE }

    fun saveTemplate(context: Context, template: String) {
        context.getSharedPreferences(REMINDER_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CUSTOM_TEMPLATE, template.ifBlank { DEFAULT_TEMPLATE })
            .apply()
    }

    fun loadPartyReminderConfig(context: Context, partyId: String): PartyReminderConfig {
        val prefs = context.getSharedPreferences(REMINDER_PREFS, Context.MODE_PRIVATE)
        return PartyReminderConfig(
            sendOnDueDate = prefs.getBoolean("party_due_$partyId", true),
            daysBeforeDue = prefs.getInt("party_before_$partyId", 3),
            remindAfterDueEveryDays = prefs.getInt("party_after_$partyId", 7),
            untilPaid = prefs.getBoolean("party_until_$partyId", true)
        )
    }

    fun savePartyReminderConfig(context: Context, partyId: String, config: PartyReminderConfig) {
        context.getSharedPreferences(REMINDER_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("party_due_$partyId", config.sendOnDueDate)
            .putInt("party_before_$partyId", config.daysBeforeDue)
            .putInt("party_after_$partyId", config.remindAfterDueEveryDays)
            .putBoolean("party_until_$partyId", config.untilPaid)
            .apply()
    }

    fun previewTemplate(
        template: String = DEFAULT_TEMPLATE,
        partyName: String = "Agarwal Traders",
        invoiceNo: String = "SAL/2026-27/0001",
        amount: String = "Rs 5,000.00",
        dueDate: String = "15-Jun-2026",
        outstanding: String = "Rs 8,500.00",
        businessName: String = "ZeroBook",
        businessPhone: String = "9876543210"
    ): String = template
        .replace("{party_name}", partyName)
        .replace("{invoice_no}", invoiceNo)
        .replace("{amount}", amount)
        .replace("{due_date}", dueDate)
        .replace("{outstanding}", outstanding)
        .replace("{business_name}", businessName)
        .replace("{business_phone}", businessPhone)

    fun scheduleDueReminder(
        context: Context,
        voucherId: String,
        partyEmail: String,
        creditDueDate: String
    ) {
        if (voucherId.isBlank() || creditDueDate.isBlank()) return
        val dueDateMillis = parseReminderDateToMillis(creditDueDate) ?: return
        val dueDelayMs = (dueDateMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        val warningDelayMs = dueDelayMs - TimeUnit.DAYS.toMillis(3)
        val dueData = workDataOf(
            "voucher_id" to voucherId,
            "party_email" to partyEmail,
            "is_warning" to false
        )
        if (dueDelayMs > 0L) {
            val request = OneTimeWorkRequestBuilder<DueReminderWorker>()
                .setInputData(dueData)
                .setInitialDelay(dueDelayMs, TimeUnit.MILLISECONDS)
                .addTag("due_reminder")
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "due_$voucherId",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
        if (warningDelayMs > 0L) {
            val warningRequest = OneTimeWorkRequestBuilder<DueReminderWorker>()
                .setInputData(
                    workDataOf(
                        "voucher_id" to voucherId,
                        "party_email" to partyEmail,
                        "is_warning" to true
                    )
                )
                .setInitialDelay(warningDelayMs, TimeUnit.MILLISECONDS)
                .addTag("due_warning")
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "warn_$voucherId",
                ExistingWorkPolicy.REPLACE,
                warningRequest
            )
        }
    }

    fun schedulePartyPlan(
        context: Context,
        partyId: String,
        partyName: String,
        bills: List<BillReceivable>,
        config: PartyReminderConfig,
        sendTime: LocalTime
    ) {
        savePartyReminderConfig(context, partyId, config)
        val dueFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
        bills.filter { it.outstandingAmount > 0.0 }.forEach { bill ->
            val dueDateMillis = bill.dueDate ?: return@forEach
            val dueDate = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(dueDateMillis),
                ZoneId.systemDefault()
            )
            if (config.sendOnDueDate) {
                scheduleIndividualReminder(
                    context = context,
                    partyId = partyId,
                    partyName = partyName,
                    scheduledAt = dueDate.toLocalDate().atTime(sendTime)
                )
            }
            val beforeDate = dueDate.toLocalDate().minusDays(config.daysBeforeDue.toLong()).atTime(sendTime)
            if (beforeDate.isAfter(LocalDateTime.now())) {
                scheduleIndividualReminder(
                    context = context,
                    partyId = partyId,
                    partyName = partyName,
                    scheduledAt = beforeDate
                )
            }
            if (config.untilPaid) {
                scheduleRecurringReminderForParty(
                    context = context,
                    partyId = partyId,
                    partyName = partyName,
                    intervalDays = config.remindAfterDueEveryDays.coerceAtLeast(1),
                    sendTime = sendTime,
                    firstDate = dueDate.toLocalDate().plusDays(config.remindAfterDueEveryDays.toLong())
                )
            }
        }
    }

    fun scheduleRecurringReminderForParty(
        context: Context,
        partyId: String,
        partyName: String,
        intervalDays: Int,
        sendTime: LocalTime,
        firstDate: LocalDate
    ) {
        val uniqueName = "party_overdue_$partyId"
        val firstRun = firstDate.atTime(sendTime).let { if (it.isAfter(LocalDateTime.now())) it else LocalDateTime.now().plusDays(1) }
        val initialDelayMillis = Duration.between(LocalDateTime.now(), firstRun).toMillis().coerceAtLeast(0L)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(intervalDays.toLong(), TimeUnit.DAYS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "party_id" to partyId,
                    "schedule_id" to uniqueName,
                    "reminder_type" to "RECURRING"
                )
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            uniqueName,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
        upsertSchedule(
            context,
            ReminderSchedule(
                id = uniqueName,
                partyId = partyId,
                partyName = partyName,
                reminderType = "RECURRING",
                scheduledDate = firstRun.toLocalDate().toString(),
                scheduledTime = sendTime.toString(),
                intervalDays = intervalDays,
                isActive = true,
                lastSent = "",
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun sendNowForParty(context: Context, partyId: String): Result<Unit> {
        val db = AppDatabase.getDatabase(context)
        val profile = db.businessProfileDao().getProfileSync() ?: return Result.failure(IllegalStateException("Business profile not found"))
        val party = db.partyDao().getPartyById(partyId) ?: return Result.failure(IllegalStateException("Party not found"))
        val bills = db.billReceivableDao().getAllBillsSync(profile.fyLabel)
            .filter { it.partyId == partyId && it.outstandingAmount > 0.0 }
        if (bills.isEmpty()) return Result.failure(IllegalStateException("No outstanding bills found"))
        val latestBill = bills.maxByOrNull { it.billDate }
        val attachment = latestBill?.voucherId?.let { voucherId ->
            runCatching {
                val bundle = InvoiceGenerator.buildRenderBundle(context, voucherId) ?: return@runCatching null
                val pdf = InvoiceGenerator.renderBundleToPdf(context, bundle)
                FileProvider.getUriForFile(context, context.packageName + ".provider", pdf)
            }.getOrNull()
        }
        val firstBill = bills.first()
        val body = previewTemplate(
            template = loadTemplate(context),
            partyName = party.name,
            invoiceNo = firstBill.voucherNo.orEmpty(),
            amount = Utils.formatIndianCurrency(firstBill.outstandingAmount),
            dueDate = firstBill.dueDate?.let { SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).format(Date(it)) }.orEmpty(),
            outstanding = Utils.formatIndianCurrency(bills.sumOf { it.outstandingAmount }),
            businessName = profile.businessName,
            businessPhone = profile.phone
        )
        EmailComposer.compose(
            context = context,
            draft = EmailComposer.Draft(
                recipients = listOf(party.email),
                subject = "Payment reminder from ${profile.businessName}",
                body = body,
                attachments = listOfNotNull(attachment)
            )
        )
        return Result.success(Unit)
    }

    suspend fun sendTestEmail(
        context: Context,
        toEmail: String
    ): Result<Unit> {
        val db = AppDatabase.getDatabase(context)
        val profile = db.businessProfileDao().getProfileSync() ?: return Result.failure(IllegalStateException("Business profile not found"))
        if (profile.smtpEmail.isBlank() || profile.smtpPassword.isBlank()) {
            return Result.failure(IllegalStateException("SMTP settings are incomplete"))
        }
        return runCatching {
            sendViaSmtp(
                smtpEmail = profile.smtpEmail,
                smtpPassword = profile.smtpPassword,
                smtpHost = profile.smtpHost.ifBlank { "smtp.gmail.com" },
                smtpPort = profile.smtpPort.ifBlank { "587" },
                to = toEmail,
                subject = "ZeroBook SMTP Test",
                body = "This is a test email from ZeroBook.",
                attachmentFile = null
            )
        }
    }

    fun loadSelectedRecipients(context: Context): Set<String> {
        val raw = context.getSharedPreferences(REMINDER_PREFS, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_RECIPIENTS, "")
            .orEmpty()
        return raw.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
    }

    fun saveSelectedRecipients(context: Context, selectedIds: Set<String>) {
        context.getSharedPreferences(REMINDER_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SELECTED_RECIPIENTS, selectedIds.sorted().joinToString(","))
            .apply()
    }

    fun loadScheduledAt(context: Context): Long? {
        val prefs = context.getSharedPreferences(REMINDER_PREFS, Context.MODE_PRIVATE)
        val legacy = prefs.getLong(KEY_SCHEDULED_AT, -1L)
        if (legacy > 0L) return legacy
        val schedule = getUniversalSchedule(context) ?: return null
        if (schedule.scheduledDate.isBlank() || schedule.scheduledTime.isBlank()) return null
        return runCatching {
            LocalDateTime.of(
                LocalDate.parse(schedule.scheduledDate),
                LocalTime.parse(schedule.scheduledTime)
            ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }.getOrNull()
    }

    fun saveSchedule(context: Context, scheduledAt: Long?) {
        context.getSharedPreferences(REMINDER_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_SCHEDULED_AT, scheduledAt ?: -1L)
            .apply()
    }

    fun schedule(context: Context, scheduledAt: Long?) {
        saveSchedule(context, scheduledAt)
        if (scheduledAt == null) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIVERSAL_WORK_NAME)
            return
        }
        val scheduledDateTime = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(scheduledAt),
            ZoneId.systemDefault()
        )
        scheduleIndividualReminder(
            context = context,
            partyId = "all",
            partyName = "All Parties",
            scheduledAt = scheduledDateTime
        )
    }

    fun getSchedules(context: Context): List<ReminderSchedule> {
        val db = AppDatabase.getDatabase(context)
        val cursor = db.openHelper.readableDatabase.query(
            """
            SELECT id, COALESCE(party_id, ''), COALESCE(party_name, ''), COALESCE(reminder_type, ''),
                   COALESCE(scheduled_date, ''), COALESCE(scheduled_time, ''), COALESCE(interval_days, 0),
                   COALESCE(is_active, 0), COALESCE(last_sent, ''), COALESCE(created_at, 0)
            FROM reminder_schedules
            ORDER BY created_at DESC
            """.trimIndent()
        )
        cursor.use {
            val list = mutableListOf<ReminderSchedule>()
            while (it.moveToNext()) {
                list += ReminderSchedule(
                    id = it.getString(0).orEmpty(),
                    partyId = it.getString(1).orEmpty(),
                    partyName = it.getString(2).orEmpty(),
                    reminderType = it.getString(3).orEmpty(),
                    scheduledDate = it.getString(4).orEmpty(),
                    scheduledTime = it.getString(5).orEmpty(),
                    intervalDays = it.getInt(6),
                    isActive = it.getInt(7) == 1,
                    lastSent = it.getString(8).orEmpty(),
                    createdAt = it.getLong(9)
                )
            }
            return list
        }
    }

    fun getLatestScheduleForParty(context: Context, partyId: String): ReminderSchedule? =
        getSchedules(context)
            .filter { it.partyId == partyId && it.reminderType == "INDIVIDUAL" }
            .maxByOrNull { it.createdAt }

    fun getUniversalSchedule(context: Context): ReminderSchedule? =
        getSchedules(context).firstOrNull { it.id == UNIVERSAL_WORK_NAME }

    fun scheduleIndividualReminder(
        context: Context,
        partyId: String,
        partyName: String,
        scheduledAt: LocalDateTime
    ): ReminderSchedule {
        val scheduleId = "individual_${partyId}_${scheduledAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()}"
        upsertSchedule(
            context = context,
            schedule = ReminderSchedule(
                id = scheduleId,
                partyId = partyId,
                partyName = partyName,
                reminderType = "INDIVIDUAL",
                scheduledDate = scheduledAt.toLocalDate().toString(),
                scheduledTime = scheduledAt.toLocalTime().toString(),
                intervalDays = 0,
                isActive = true,
                lastSent = "",
                createdAt = System.currentTimeMillis()
            )
        )
        val delayMillis = Duration.between(LocalDateTime.now(), scheduledAt).toMillis().coerceAtLeast(0L)
        val request = androidx.work.OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "party_id" to partyId,
                    "schedule_id" to scheduleId,
                    "reminder_type" to "INDIVIDUAL"
                )
            )
            .addTag(scheduleId)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(scheduleId, ExistingWorkPolicy.REPLACE, request)
        return getLatestScheduleForParty(context, partyId) ?: error("Failed to store reminder schedule.")
    }

    fun scheduleRecurringReminder(
        context: Context,
        enabled: Boolean,
        intervalDays: Int,
        sendTime: LocalTime
    ): ReminderSchedule? {
        if (!enabled) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIVERSAL_WORK_NAME)
            upsertSchedule(
                context,
                ReminderSchedule(
                    id = UNIVERSAL_WORK_NAME,
                    partyId = "",
                    partyName = "All Parties",
                    reminderType = "RECURRING",
                    scheduledDate = "",
                    scheduledTime = sendTime.toString(),
                    intervalDays = intervalDays,
                    isActive = false,
                    lastSent = "",
                    createdAt = System.currentTimeMillis()
                )
            )
            return getUniversalSchedule(context)
        }

        val now = LocalDateTime.now()
        var firstRun = LocalDate.now().atTime(sendTime)
        if (!firstRun.isAfter(now)) {
            firstRun = firstRun.plusDays(1)
        }
        val initialDelayMillis = Duration.between(now, firstRun).toMillis().coerceAtLeast(0L)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(intervalDays.toLong(), TimeUnit.DAYS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "schedule_id" to UNIVERSAL_WORK_NAME,
                    "reminder_type" to "RECURRING",
                    "interval_days" to intervalDays
                )
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIVERSAL_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
        upsertSchedule(
            context,
            ReminderSchedule(
                id = UNIVERSAL_WORK_NAME,
                partyId = "",
                partyName = "All Parties",
                reminderType = "RECURRING",
                scheduledDate = firstRun.toLocalDate().toString(),
                scheduledTime = sendTime.toString(),
                intervalDays = intervalDays,
                isActive = true,
                lastSent = "",
                createdAt = System.currentTimeMillis()
            )
        )
        return getUniversalSchedule(context)
    }

    suspend fun processReminders(
        context: Context,
        partyId: String? = null,
        scheduleId: String? = null,
        reminderType: String = "MANUAL"
    ): Int {
        val db = AppDatabase.getDatabase(context)
        val profile = db.businessProfileDao().getProfileSync()
        val businessName = profile?.businessName?.ifBlank { "ZeroBook" } ?: "ZeroBook"
        val activeFinancialYearCode = profile?.fyLabel?.takeIf { it.isNotBlank() }
            ?: FinancialYearUtils.currentFinancialYearCode()
        val allBills = db.billReceivableDao().getAllBillsSync(activeFinancialYearCode)
            .filter { it.outstandingAmount > 0.0 }
        val groupedBills = if (partyId.isNullOrBlank()) {
            allBills.groupBy { it.partyId }
        } else {
            allBills.filter { it.partyId == partyId }.groupBy { it.partyId }
        }

        if (groupedBills.isEmpty()) {
            appendLogs(context, listOf("${formatNow()} | $reminderType | No outstanding dues found"))
            updateScheduleLastSent(context, scheduleId, active = reminderType == "RECURRING")
            return 0
        }

        val processedRows = mutableListOf<String>()
        groupedBills.forEach { (duePartyId, bills) ->
            val party = db.partyDao().getPartyById(duePartyId) ?: return@forEach
            val recipientEmail = party.email.trim()
            val body = buildReminderBody(
                context = context,
                partyName = party.name,
                bills = bills,
                businessName = businessName,
                businessPhone = profile?.phone.orEmpty(),
                businessEmail = profile?.email.orEmpty()
            )
            val subject = "Payment reminder from $businessName"
            launchEmailDraft(context, recipientEmail, subject, body)
            processedRows += "${formatNow()} | $reminderType | ${party.name} | Total ${Utils.formatIndianCurrency(bills.sumOf { it.outstandingAmount })}"
        }

        appendLogs(context, processedRows)
        updateScheduleLastSent(context, scheduleId, active = reminderType == "RECURRING")
        return processedRows.size
    }

    private fun launchEmailDraft(context: Context, email: String, subject: String, body: String) {
        if (email.isBlank()) return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(Intent.createChooser(intent, "Send reminder").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    private fun buildReminderBody(
        context: Context,
        partyName: String,
        bills: List<BillReceivable>,
        businessName: String,
        businessPhone: String,
        businessEmail: String
    ): String {
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
        val invoiceLines = bills.joinToString("\n") { bill ->
            val invoiceDate = dateFormat.format(Date(bill.billDate))
            "Invoice ${bill.voucherNo.orEmpty()} dated $invoiceDate - Due ${Utils.formatIndianCurrency(bill.outstandingAmount)}"
        }
        val totalDue = bills.sumOf { it.outstandingAmount }
        return previewTemplate(
            template = loadTemplate(context),
            partyName = partyName,
            invoiceNo = bills.firstOrNull()?.voucherNo.orEmpty(),
            amount = bills.firstOrNull()?.outstandingAmount?.let(Utils::formatIndianCurrency).orEmpty(),
            dueDate = bills.firstOrNull()?.dueDate?.let { dateFormat.format(Date(it)) }.orEmpty(),
            outstanding = Utils.formatIndianCurrency(totalDue),
            businessName = businessName,
            businessPhone = businessPhone
        ) + "\n\nOutstanding invoices:\n$invoiceLines" + if (businessEmail.isNotBlank()) "\n\nEmail: $businessEmail" else ""
    }

    private fun upsertSchedule(context: Context, schedule: ReminderSchedule) {
        val db = AppDatabase.getDatabase(context)
        db.openHelper.writableDatabase.execSQL(
            """
            INSERT OR REPLACE INTO reminder_schedules (
                id, party_id, party_name, reminder_type, scheduled_date, scheduled_time,
                interval_days, is_active, last_sent, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                schedule.id,
                schedule.partyId,
                schedule.partyName,
                schedule.reminderType,
                schedule.scheduledDate,
                schedule.scheduledTime,
                schedule.intervalDays,
                if (schedule.isActive) 1 else 0,
                schedule.lastSent,
                schedule.createdAt
            )
        )
    }

    private fun updateScheduleLastSent(context: Context, scheduleId: String?, active: Boolean) {
        if (scheduleId.isNullOrBlank()) return
        val db = AppDatabase.getDatabase(context)
        db.openHelper.writableDatabase.execSQL(
            """
            UPDATE reminder_schedules
            SET last_sent = ?, is_active = ?
            WHERE id = ?
            """.trimIndent(),
            arrayOf<Any?>(
                LocalDateTime.now().toString(),
                if (active) 1 else 0,
                scheduleId
            )
        )
    }

    private fun appendLogs(context: Context, rows: List<String>) {
        val prefs = context.getSharedPreferences(REMINDER_PREFS, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_LOGS, "").orEmpty()
        val joined = rows.joinToString("\n")
        val finalLogs = if (existing.isBlank()) joined else "$joined\n$existing"
        prefs.edit().putString(KEY_LOGS, finalLogs).apply()
    }

    private fun formatNow(): String =
        SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH).format(Date())

    private fun parseReminderDateToMillis(raw: String): Long? {
        raw.toLongOrNull()?.let {
            return java.util.Calendar.getInstance().apply {
                timeInMillis = it
                set(java.util.Calendar.HOUR_OF_DAY, 9)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        val patterns = listOf("dd-MMM-yyyy", "dd-MMM-yy", "dd-MM-yyyy")
        return patterns.firstNotNullOfOrNull { pattern ->
            runCatching {
                SimpleDateFormat(pattern, Locale.ENGLISH).parse(raw)?.time
            }.getOrNull()
        }?.let { parsed ->
            java.util.Calendar.getInstance().apply {
                timeInMillis = parsed
                set(java.util.Calendar.HOUR_OF_DAY, 9)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
    }
}

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val partyId = inputData.getString("party_id")
        val scheduleId = inputData.getString("schedule_id")
        val reminderType = inputData.getString("reminder_type").orEmpty().ifBlank { "WORKER" }
        return runCatching {
            EmailReminderScheduler.processReminders(
                context = applicationContext,
                partyId = partyId,
                scheduleId = scheduleId,
                reminderType = reminderType
            )
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }
}

class DueReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val voucherId = inputData.getString("voucher_id").orEmpty()
        val fallbackEmail = inputData.getString("party_email").orEmpty()
        val isWarning = inputData.getBoolean("is_warning", false)
        if (voucherId.isBlank()) return Result.success()

        return runCatching {
            val bundle = InvoiceGenerator.buildRenderBundle(applicationContext, voucherId) ?: return Result.success()
            val balanceDue = bundle.document.paymentSnapshot.balanceDue.coerceAtLeast(0.0)
            if (balanceDue <= 0.0) return Result.success()

            val pdfFile = InvoiceGenerator.renderBundleToPdf(applicationContext, bundle)
            val pdfUri = FileProvider.getUriForFile(
                applicationContext,
                applicationContext.packageName + ".provider",
                pdfFile
            )
            val business = bundle.document.business
            val partyName = bundle.document.buyer?.name ?: "Customer"
            val partyEmail = bundle.document.buyer?.email.orEmpty().ifBlank { fallbackEmail }
            if (partyEmail.isBlank()) {
                ensureDueReminderChannel(applicationContext)
                NotificationManagerCompat.from(applicationContext).notify(
                    voucherId.hashCode(),
                    NotificationCompat.Builder(applicationContext, DUE_REMINDER_CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_email)
                        .setContentTitle("No email for $partyName")
                        .setContentText("Party has no email address saved")
                        .setAutoCancel(true)
                        .build()
                )
                return Result.success()
            }
            val currency = DecimalFormat("#,##0.00")
            val subjectPrefix = if (isWarning) "Upcoming Payment Due" else "Payment Due"
            val subject = "$subjectPrefix - Invoice ${bundle.document.invoiceNumber} - \u20B9${currency.format(balanceDue)} - From ${business.businessName}"
            val body = """
                Dear $partyName,

                ${if (isWarning) "This is a friendly reminder that the following invoice will become due soon." else "This is a payment reminder for the following unpaid invoice."}

                Invoice No: ${bundle.document.invoiceNumber}
                Invoice Date: ${bundle.document.issuedAtLabel}
                Invoice Total: \u20B9${currency.format(bundle.document.totals.netAmount)}
                Amount Paid: \u20B9${currency.format(bundle.document.paymentSnapshot.partPaymentReceived)}
                Balance Due: \u20B9${currency.format(balanceDue)}
                Due Date: ${bundle.document.dueDateLabel.ifBlank { "Not specified" }}

                A PDF copy of the invoice is attached for your reference.

                ${if (isWarning) "Please keep the payment ready before the due date." else "Please arrange payment at the earliest."}

                For any queries, please contact us at ${business.phone}${business.email.takeIf { it.isNotBlank() }?.let { " or $it" } ?: ""}.

                Regards,
                ${business.businessName}
                GSTIN: ${business.gstin}
            """.trimIndent()

            val smtpConfigured = business.smtpEmail.isNotBlank() && business.smtpPassword.isNotBlank()
            if (smtpConfigured) {
                runCatching {
                    sendViaSmtp(
                        smtpEmail = business.smtpEmail,
                        smtpPassword = business.smtpPassword,
                        smtpHost = business.smtpHost.ifBlank { "smtp.gmail.com" },
                        smtpPort = business.smtpPort.ifBlank { "587" },
                        to = partyEmail,
                        subject = subject,
                        body = body,
                        attachmentFile = pdfFile
                    )
                }.onSuccess {
                    ensureDueReminderChannel(applicationContext)
                    NotificationManagerCompat.from(applicationContext).notify(
                        voucherId.hashCode(),
                        NotificationCompat.Builder(applicationContext, DUE_REMINDER_CHANNEL_ID)
                            .setSmallIcon(android.R.drawable.ic_dialog_email)
                            .setContentTitle("Reminder sent")
                            .setContentText("Email sent to $partyName")
                            .setAutoCancel(true)
                            .build()
                    )
                    return Result.success()
                }
            }

            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                data = Uri.parse("mailto:")
                type = "application/pdf"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(partyEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                clipData = ClipData.newRawUri("invoice", pdfUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooserIntent = Intent.createChooser(emailIntent, "Send payment reminder")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                voucherId.hashCode(),
                chooserIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            ensureDueReminderChannel(applicationContext)
            val notification = NotificationCompat.Builder(applicationContext, DUE_REMINDER_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle(if (isWarning) "Payment due soon: $partyName" else "Payment due: $partyName")
                .setContentText("Tap to send reminder email. Due: \u20B9${currency.format(balanceDue)}")
                .setStyle(NotificationCompat.BigTextStyle().bigText(subject))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_dialog_email, "Send Email", pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            NotificationManagerCompat.from(applicationContext).notify(voucherId.hashCode(), notification)
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }

    private fun ensureDueReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            DUE_REMINDER_CHANNEL_ID,
            "Payment Due Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for invoice payment reminder emails"
        }
        manager.createNotificationChannel(channel)
    }
}

private fun sendViaSmtp(
    smtpEmail: String,
    smtpPassword: String,
    smtpHost: String,
    smtpPort: String,
    to: String,
    subject: String,
    body: String,
    attachmentFile: java.io.File?
) {
    val props = Properties().apply {
        put("mail.smtp.auth", "true")
        put("mail.smtp.starttls.enable", "true")
        put("mail.smtp.host", smtpHost)
        put("mail.smtp.port", smtpPort)
        put("mail.smtp.ssl.trust", smtpHost)
    }
    val session = Session.getInstance(props, object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication =
            PasswordAuthentication(smtpEmail, smtpPassword)
    })
    val message = MimeMessage(session).apply {
        setFrom(InternetAddress(smtpEmail))
        setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
        setSubject(subject)
        if (attachmentFile != null) {
            val textPart = MimeBodyPart().apply { setText(body) }
            val attachmentPart = MimeBodyPart().apply {
                dataHandler = DataHandler(FileDataSource(attachmentFile))
                fileName = attachmentFile.name
            }
            setContent(MimeMultipart().apply {
                addBodyPart(textPart)
                addBodyPart(attachmentPart)
            })
        } else {
            setText(body)
        }
    }
    Transport.send(message)
}
