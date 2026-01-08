package com.parentalguard.parent.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.parentalguard.common.model.AppCategory
import com.parentalguard.parent.R
import com.parentalguard.parent.viewmodel.ChildDevice
import com.parentalguard.parent.viewmodel.DeviceControlViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryLimitsDialog(
    device: ChildDevice,
    viewModel: DeviceControlViewModel,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<AppCategory?>(null) }
    var timeLimit by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.category_limits))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.set_category_limit),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Category selection
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AppCategory.values().filter { it != AppCategory.OTHER }) { category ->
                        CategoryLimitItem(
                            category = category,
                            isSelected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Time input
                if (selectedCategory != null) {
                    OutlinedTextField(
                        value = timeLimit,
                        onValueChange = { timeLimit = it.filter { char -> char.isDigit() } },
                        label = { Text(stringResource(R.string.time_limit_minutes)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedCategory?.let { category ->
                        timeLimit.toIntOrNull()?.let { minutes ->
                            viewModel.setCategoryLimit(device, category, minutes)
                            onDismiss()
                        }
                    }
                },
                enabled = selectedCategory != null && timeLimit.isNotEmpty()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryLimitItem(
    category: AppCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val categoryName = when (category) {
        AppCategory.SOCIAL -> stringResource(R.string.category_social)
        AppCategory.GAMES -> stringResource(R.string.category_games)
        AppCategory.EDUCATION -> stringResource(R.string.category_education)
        AppCategory.PRODUCTIVITY -> stringResource(R.string.category_productivity)
        AppCategory.ENTERTAINMENT -> stringResource(R.string.category_entertainment)
        else -> stringResource(R.string.category_other)
    }
    
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = categoryName,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
