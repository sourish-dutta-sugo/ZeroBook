package com.example.data

import android.content.Context
import org.json.JSONObject

data class ChangelogData(
    val version: String,
    val showOnce: Boolean,
    val changes: List<String>
)

object ChangelogLoader {
    fun load(context: Context): ChangelogData? = runCatching {
        val raw = context.assets.open("changelog.json").bufferedReader().use { it.readText() }
        val root = JSONObject(raw)
        val changesArray = root.optJSONArray("changes")
        val changes = buildList {
            if (changesArray != null) {
                for (index in 0 until changesArray.length()) {
                    add(changesArray.optString(index))
                }
            }
        }.filter { it.isNotBlank() }
        ChangelogData(
            version = root.optString("version"),
            showOnce = root.optBoolean("show_once", true),
            changes = changes
        )
    }.getOrNull()
}
