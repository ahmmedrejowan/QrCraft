package com.rejown.qrcraft.presentation.generator.creation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rejown.qrcraft.domain.models.FieldDefinition
import com.rejown.qrcraft.domain.models.FieldType

@Composable
fun DynamicInputForm(
    fields: List<FieldDefinition>,
    values: Map<String, String>,
    errors: Map<String, String>,
    onValueChange: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        fields.forEach { field ->
            InputField(
                field = field,
                value = values[field.key] ?: "",
                error = errors[field.key],
                onValueChange = { onValueChange(field.key, it) }
            )
        }
    }
}

@Composable
private fun InputField(
    field: FieldDefinition,
    value: String,
    error: String?,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when (field.type) {
        FieldType.TEXT -> {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = {
                    Text(field.label + if (field.required) " *" else "")
                },
                placeholder = field.placeholder?.let { { Text(it) } },
                supportingText = if (error != null) {
                    { Text(error, color = MaterialTheme.colorScheme.error) }
                } else if (field.helperText != null) {
                    { Text(field.helperText) }
                } else null,
                isError = error != null,
                modifier = modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        FieldType.MULTILINE_TEXT -> {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = {
                    Text(field.label + if (field.required) " *" else "")
                },
                placeholder = field.placeholder?.let { { Text(it) } },
                supportingText = if (error != null) {
                    { Text(error, color = MaterialTheme.colorScheme.error) }
                } else if (field.helperText != null) {
                    { Text(field.helperText) }
                } else null,
                isError = error != null,
                modifier = modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        }

        FieldType.EMAIL -> {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = {
                    Text(field.label + if (field.required) " *" else "")
                },
                placeholder = field.placeholder?.let { { Text(it) } },
                supportingText = if (error != null) {
                    { Text(error, color = MaterialTheme.colorScheme.error) }
                } else if (field.helperText != null) {
                    { Text(field.helperText) }
                } else null,
                isError = error != null,
                modifier = modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
        }

        FieldType.PHONE -> {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = {
                    Text(field.label + if (field.required) " *" else "")
                },
                placeholder = field.placeholder?.let { { Text(it) } },
                supportingText = if (error != null) {
                    { Text(error, color = MaterialTheme.colorScheme.error) }
                } else if (field.helperText != null) {
                    { Text(field.helperText) }
                } else null,
                isError = error != null,
                modifier = modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        }

        FieldType.URL -> {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = {
                    Text(field.label + if (field.required) " *" else "")
                },
                placeholder = field.placeholder?.let { { Text(it) } },
                supportingText = if (error != null) {
                    { Text(error, color = MaterialTheme.colorScheme.error) }
                } else if (field.helperText != null) {
                    { Text(field.helperText) }
                } else null,
                isError = error != null,
                modifier = modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )
        }

        FieldType.NUMBER -> {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = {
                    Text(field.label + if (field.required) " *" else "")
                },
                placeholder = field.placeholder?.let { { Text(it) } },
                supportingText = if (error != null) {
                    { Text(error, color = MaterialTheme.colorScheme.error) }
                } else if (field.helperText != null) {
                    { Text(field.helperText) }
                } else null,
                isError = error != null,
                modifier = modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        FieldType.DROPDOWN -> {
            // TODO: Implement dropdown with options
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = {
                    Text(field.label + if (field.required) " *" else "")
                },
                modifier = modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = true
            )
        }

        FieldType.CHECKBOX -> {
            // TODO: Implement checkbox
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = value == "true",
                    onCheckedChange = { onValueChange(if (it) "true" else "false") }
                )
                Text(
                    text = field.label,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        else -> {
            // Default to text field for unimplemented types
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = {
                    Text(field.label + if (field.required) " *" else "")
                },
                modifier = modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
