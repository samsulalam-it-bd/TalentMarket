package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.ConfigData

@Composable
fun CategoryMultiSelectGrid(
    selectedCategories: List<String>,
    onCategoriesChanged: (List<String>) -> Unit
) {
    val allCategories = ConfigData.saudiCategories
    
    Column {
        Text(
            text = "Select up to 3 categories (${selectedCategories.size}/3 selected)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            items(allCategories) { category ->
                val isSelected = selectedCategories.contains(category.nameEn)
                val accentColor = ConfigData.getCategoryColor(category.nameEn)
                
                Card(
                    modifier = Modifier
                        .height(90.dp)
                        .clickable {
                            if (isSelected) {
                                onCategoriesChanged(selectedCategories - category.nameEn)
                            } else if (selectedCategories.size < 3) {
                                onCategoriesChanged(selectedCategories + category.nameEn)
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = SolidColor(if (isSelected) accentColor else Color.Gray),
                        width = if (isSelected) 2.dp else 1.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = if (isSelected) accentColor else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = category.nameEn,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) accentColor else Color.Gray
                        )
                    }
                }
            }
        }
    }
}
