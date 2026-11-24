package com.example.glassfinance.data.model

data class Category(
    val name: String,
    val emoji: String,
    val isIncome: Boolean
)

/** Danh sách danh mục dùng chung cho form nhập, thống kê và ngân sách. */
object Categories {

    /** Danh mục "Khác": khi chọn, form bắt người dùng tự gõ tên danh mục. */
    const val OTHER = "Khác"

    val expense = listOf(
        Category("Ăn uống", "🍜", false),
        Category("Di chuyển", "🛵", false),
        Category("Mua sắm", "🛍️", false),
        Category("Hóa đơn", "💡", false),
        Category("Học tập", "📚", false),
        Category("Giải trí", "🎮", false),
        Category("Sức khỏe", "💊", false),
        Category("Khác", "📦", false)
    )

    val income = listOf(
        Category("Lương", "💼", true),
        Category("Làm thêm", "🧾", true),
        Category("Thưởng", "🎁", true),
        Category("Khác", "💰", true)
    )

    fun forType(isIncome: Boolean): List<Category> = if (isIncome) income else expense

    fun emojiOf(name: String): String =
        (expense + income).firstOrNull { it.name == name }?.emoji
            ?: if (name == "Thu nhập") "💰" else "📦"
}
