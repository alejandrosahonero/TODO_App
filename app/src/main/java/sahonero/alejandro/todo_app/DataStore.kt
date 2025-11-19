package sahonero.alejandro.todo_app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object TodoPreferences {
    private val SELECTED_THEME = booleanPreferencesKey("selected_theme") // true = dark, false = light
    private val SELECTED_COLOR = stringPreferencesKey("selected_color")

    fun getTheme(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[SELECTED_THEME] ?: true }

    suspend fun setTheme(context: Context, theme: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_THEME] = theme
        }
    }

    fun getColor(context: Context): Flow<String> =
        context.dataStore.data.map { prefs -> prefs[SELECTED_COLOR] ?: "Default" }

    suspend fun setColor(context: Context, color: String) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_COLOR] = color
        }
    }
}
