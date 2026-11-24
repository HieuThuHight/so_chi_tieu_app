package com.example.glassfinance.notification

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Nhật ký chẩn đoán (chỉ lưu trên máy, KHÔNG lưu nội dung thông báo, KHÔNG đếm thông báo của app khác):
 * lần kết nối gần nhất của listener và kết quả xử lý các tin có dáng ngân hàng
 * (chỉ ghi giờ, tên app và kết quả). Giúp biết vì sao giao dịch chưa được lưu.
 */
object NotificationActivityLog {

    private const val PREFS = "glass_finance_notif_activity"
    private const val KEY_CONNECTED = "connected_at"
    private const val KEY_ITEMS = "items"
    private const val MAX_ITEMS = 8

    data class Entry(val time: Long, val pkg: String, val result: String)

    data class Snapshot(
        val connectedAt: Long,
        val entries: List<Entry>
    )

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Listener vừa được hệ thống kết nối (nghĩa là quyền đọc thông báo đang hoạt động). */
    @Synchronized
    fun markConnected(context: Context) {
        prefs(context).edit().putLong(KEY_CONNECTED, System.currentTimeMillis()).apply()
    }

    /** Ghi kết quả xử lý một tin có dáng ngân hàng: SAVED, DUPLICATE, BANK_DISABLED, UNRECOGNIZED, ERROR:... */
    @Synchronized
    fun record(context: Context, pkg: String, result: String) {
        val p = prefs(context)
        val old = runCatching { JSONArray(p.getString(KEY_ITEMS, "[]")) }.getOrDefault(JSONArray())
        val out = JSONArray()
        out.put(JSONObject().put("t", System.currentTimeMillis()).put("p", pkg).put("r", result.take(120)))
        for (i in 0 until minOf(old.length(), MAX_ITEMS - 1)) out.put(old.get(i))
        p.edit().putString(KEY_ITEMS, out.toString()).apply()
    }

    @Synchronized
    fun snapshot(context: Context): Snapshot {
        val p = prefs(context)
        val arr = runCatching { JSONArray(p.getString(KEY_ITEMS, "[]")) }.getOrDefault(JSONArray())
        val entries = (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            Entry(o.optLong("t"), o.optString("p"), o.optString("r"))
        }
        return Snapshot(
            connectedAt = p.getLong(KEY_CONNECTED, 0L),
            entries = entries
        )
    }

    @Synchronized
    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
