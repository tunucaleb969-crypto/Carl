package com.carl.editor.persistence

import android.content.Context
import com.carl.editor.EditorProjectState

/**
 * Durable single-draft storage for Carl's current project.
 *
 * The media itself is never copied into preferences. Only the edit graph and source URI are stored.
 * The repository is intentionally small until Carl grows a multi-project database.
 */
class CarlProjectRepository(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun load(): ProjectStateSerializer.SavedProject? =
        preferences.getString(PROJECT_JSON_KEY, null)?.let(ProjectStateSerializer::fromJson)

    fun save(projectName: String, sourceUri: String, state: EditorProjectState) {
        preferences.edit()
            .putString(
                PROJECT_JSON_KEY,
                ProjectStateSerializer.toJson(projectName, sourceUri, state)
            )
            .apply()
    }

    fun clear() {
        preferences.edit().remove(PROJECT_JSON_KEY).apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "carl_project_storage"
        private const val PROJECT_JSON_KEY = "latest_project"
    }
}
