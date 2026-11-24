package com.example.glassfinance.utils

import java.text.Normalizer

private val COMBINING_MARKS = Regex("\\p{Mn}+")

/**
 * Bỏ dấu tiếng Việt + về chữ thường để tìm kiếm không phân biệt dấu:
 * "Ăn uống" -> "an uong".
 */
fun String.foldVietnamese(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(COMBINING_MARKS, "")
        .replace('đ', 'd')
        .replace('Đ', 'D')
        .lowercase()
