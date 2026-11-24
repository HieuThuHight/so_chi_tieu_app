package com.example.glassfinance.ui.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.glassfinance.data.model.Categories
import androidx.compose.ui.unit.dp

/** Hàng chip chọn danh mục, dùng ở form giao dịch và form ngân sách. */
@Composable
fun CategoryPicker(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { name ->
            FilterChip(
                selected = selected == name,
                onClick = { onSelect(name) },
                label = { Text("${Categories.emojiOf(name)} $name") }
            )
        }
    }
}
