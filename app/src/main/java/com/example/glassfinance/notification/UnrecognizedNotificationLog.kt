package com.example.glassfinance.notification

import android.content.Context
import com.example.glassfinance.utils.foldVietnamese
import org.json.JSONArray
import org.json.JSONObject

/**
 * Lưu tạm (chỉ trên máy) các thông báo trông giống tin biến động số dư nhưng chưa có parser,
 * để người dùng sao chép gửi đi viết thêm parser cho ngân hàng mới.
 */
object UnrecognizedNotificationLog {

    private const val PREFS = "glass_finance_notif_log"
    private const val KEY = "items"
    private const val MAX_ITEMS = 20
    private const val MAX_TEXT = 500

    /** Chỉ giữ tin có dáng ngân hàng, tránh lưu tin nhắn riêng tư của app khác. */
    fun looksLikeBankMessage(text: String): Boolean {
        val f = text.foldVietnamese()
        return (f.contains("vnd") || f.contains("so tien gd")) && (f.contains("so du") || f.contains("tk ") || f.contains("gd:") || f.contains("giao dich"))
    }

    @Synchronized
    fun add(context: Context, pkg: String, title: String, text: String) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = runCatching { JSONArray(prefs.getString(KEY, "[]")) }.getOrDefault(JSONArray())
        val body = text.take(MAX_TEXT)
        for (i in 0 until arr.length()) if (arr.getJSONObject(i).optString("text") == body) return
        val out = JSONArray()
        out.put(JSONObject().put("pkg", pkg).put("title", title.take(80)).put("text", body))
        for (i in 0 until minOf(arr.length(), MAX_ITEMS - 1)) out.put(arr.get(i))
        prefs.edit().putString(KEY, out.toString()).apply()
    }

    @Synchronized
    fun all(context: Context): List<String> {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = runCatching { JSONArray(prefs.getString(KEY, "[]")) }.getOrDefault(JSONArray())
        return (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            "[${o.optString("pkg")}] ${o.optString("title")}\n${o.optString("text")}"
        }
    }

    @Synchronized
    fun clear(context: Context) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY).apply()
    }
}
