package com.zerobook.app.services

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object EmailComposer {
    data class Draft(
        val recipients: List<String> = emptyList(),
        val subject: String = "",
        val body: String = "",
        val attachments: List<Uri> = emptyList()
    )

    fun compose(context: Context, draft: Draft, chooserTitle: String = "Send Email") {
        val hasAttachments = draft.attachments.isNotEmpty()
        val intent = Intent(if (hasAttachments) Intent.ACTION_SEND_MULTIPLE else Intent.ACTION_SEND).apply {
            type = if (hasAttachments) "*/*" else "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, draft.recipients.toTypedArray())
            putExtra(Intent.EXTRA_SUBJECT, draft.subject)
            putExtra(Intent.EXTRA_TEXT, draft.body)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (hasAttachments) {
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(draft.attachments))
                clipData = ClipData.newRawUri("attachments", draft.attachments.first())
            }
        }

        try {
            context.startActivity(Intent.createChooser(intent, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "No email application found on this device.", Toast.LENGTH_LONG).show()
        }
    }
}
