package com.example.glassfinance.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ThemeMode { SYSTEM, LIGHT, DARK }

object SupportedBanks {
    val all = listOf(
        "VCB", "MB Bank", "Techcombank", "BIDV", "ACB",
        "TPBank", "VietinBank", "Agribank", "Sacombank", "VPBank"
    )
    val defaultEnabled = setOf("VCB", "MB Bank")
    const val CASH = "Tiền mặt"
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val hideBalance: Boolean = false,
    val enabledBanks: Set<String> = SupportedBanks.defaultEnabled,
    // Lần xuất / sao lưu dữ liệu gần nhất (0 = chưa lần nào).
    val lastBackupAt: Long = 0L,
    // Độ đặc của thẻ kính (0.3 = rất trong, 0.95 = gần như đặc).
    val glassOpacity: Float = 0.86f,
    // Độ đặc của thanh điều hướng dưới cùng (0.1 = gần như trong suốt, 0.9 = gần đặc).
    val navBarOpacity: Float = 0.06f
)

/** Lưu cấu hình người dùng bằng SharedPreferences, phát ra qua StateFlow. */
class SettingsStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun read(): AppSettings {
        val mode = runCatching {
            ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        }.getOrDefault(ThemeMode.SYSTEM)

        // Không sửa trực tiếp Set do getStringSet trả về → luôn copy.
        val banks = prefs.getStringSet(KEY_BANKS, null)?.toSet() ?: SupportedBanks.defaultEnabled

        return AppSettings(
            themeMode = mode,
            hideBalance = prefs.getBoolean(KEY_HIDE_BALANCE, false),
            enabledBanks = banks,
            lastBackupAt = prefs.getLong(KEY_LAST_BACKUP, 0L),
            glassOpacity = prefs.getFloat(KEY_GLASS, 0.86f),
            navBarOpacity = prefs.getFloat(KEY_NAV_BAR, 0.06f)
        )
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
        _settings.update { it.copy(themeMode = mode) }
    }

    fun setHideBalance(hide: Boolean) {
        prefs.edit().putBoolean(KEY_HIDE_BALANCE, hide).apply()
        _settings.update { it.copy(hideBalance = hide) }
    }

    fun setBankEnabled(bank: String, enabled: Boolean) {
        val updated = if (enabled) _settings.value.enabledBanks + bank else _settings.value.enabledBanks - bank
        prefs.edit().putStringSet(KEY_BANKS, updated).apply()
        _settings.update { it.copy(enabledBanks = updated) }
    }

    fun setLastBackup(time: Long) {
        prefs.edit().putLong(KEY_LAST_BACKUP, time).apply()
        _settings.update { it.copy(lastBackupAt = time) }
    }

    fun setGlassOpacity(value: Float) {
        val v = value.coerceIn(0.3f, 0.95f)
        prefs.edit().putFloat(KEY_GLASS, v).apply()
        _settings.update { it.copy(glassOpacity = v) }
    }

    fun setNavBarOpacity(value: Float) {
        val v = value.coerceIn(0.0f, 0.9f)
        prefs.edit().putFloat(KEY_NAV_BAR, v).apply()
        _settings.update { it.copy(navBarOpacity = v) }
    }

    private companion object {
        const val PREFS_NAME = "glass_finance_settings"
        const val KEY_THEME = "theme_mode"
        const val KEY_HIDE_BALANCE = "hide_balance"
        const val KEY_BANKS = "enabled_banks"
        const val KEY_LAST_BACKUP = "last_backup_at"
        const val KEY_GLASS = "glass_opacity"
        // v3: bỏ giá trị cũ đã lưu để mặc định mới (trong suốt hơn) có hiệu lực.
        const val KEY_NAV_BAR = "nav_bar_opacity_v3"
    }
}