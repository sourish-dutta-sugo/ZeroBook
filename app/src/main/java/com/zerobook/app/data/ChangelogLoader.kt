package com.zerobook.app.data

import android.content.Context
import org.json.JSONObject

data class ChangelogData(
    val version: String,
    val showOnce: Boolean,
    val changes: List<String>,
    val history: List<ChangelogEntry>
)

data class ChangelogEntry(
    val version: String,
    val changes: List<String>
)

object ChangelogLoader {
    fun load(context: Context): ChangelogData? = runCatching {
        val raw = context.assets.open("changelog.json").bufferedReader().use { it.readText() }
        val root = JSONObject(raw)
        val historyArray = root.optJSONArray("history")
        val history = buildList {
            if (historyArray != null) {
                for (index in 0 until historyArray.length()) {
                    val entry = historyArray.optJSONObject(index) ?: continue
                    val changes = buildList {
                        val changesArray = entry.optJSONArray("changes")
                        if (changesArray != null) {
                            for (changeIndex in 0 until changesArray.length()) {
                                add(changesArray.optString(changeIndex))
                            }
                        }
                    }.filter { it.isNotBlank() }

                    add(
                        ChangelogEntry(
                            version = entry.optString("version"),
                            changes = changes
                        )
                    )
                }
            }
        }.filter { it.version.isNotBlank() || it.changes.isNotEmpty() }

        val latestEntry = history.firstOrNull()
        val fallbackChanges = buildList {
            val changesArray = root.optJSONArray("changes")
            if (changesArray != null) {
                for (index in 0 until changesArray.length()) {
                    add(changesArray.optString(index))
                }
            }
        }.filter { it.isNotBlank() }

        ChangelogData(
            version = latestEntry?.version ?: root.optString("version"),
            showOnce = root.optBoolean("show_once", true),
            changes = latestEntry?.changes ?: fallbackChanges,
            history = if (history.isNotEmpty()) {
                history
            } else {
                listOf(
                    ChangelogEntry(
                        version = root.optString("version"),
                        changes = fallbackChanges
                    )
                ).filter { it.version.isNotBlank() || it.changes.isNotEmpty() }
            }
        )
    }.getOrNull()
}
