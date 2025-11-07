package com.rejown.qrcraft.presentation.generator.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rejown.qrcraft.domain.models.ContentType

@Composable
fun InputForm(
    contentType: ContentType,
    content: String,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Enter ${contentType.displayName}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        when (contentType) {
            ContentType.TEXT -> {
                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    label = { Text("Enter text") },
                    placeholder = { Text("Type your text here...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
            }

            ContentType.URL -> {
                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    label = { Text("Website URL") },
                    placeholder = { Text("example.com") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        capitalization = KeyboardCapitalization.None
                    ),
                    supportingText = { Text("https:// will be added automatically") }
                )
            }

            ContentType.EMAIL -> {
                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    label = { Text("Email Address") },
                    placeholder = { Text("example@email.com") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        capitalization = KeyboardCapitalization.None
                    )
                )
            }

            ContentType.PHONE -> {
                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    label = { Text("Phone Number") },
                    placeholder = { Text("+1234567890") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    )
                )
            }

            ContentType.SMS -> {
                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    label = { Text("Phone Number") },
                    placeholder = { Text("+1234567890:Message text") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                    supportingText = { Text("Format: number:message") }
                )
            }

            ContentType.WIFI -> {
                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    label = { Text("WiFi Details") },
                    placeholder = { Text("SSID,Password,WPA") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    supportingText = { Text("Format: SSID,Password,Encryption") }
                )
            }

            ContentType.CONTACT -> {
                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    label = { Text("Contact Details") },
                    placeholder = { Text("Name,Phone,Email") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    supportingText = { Text("Format: Name,Phone,Email,Organization") }
                )
            }

            ContentType.GEO -> {
                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    label = { Text("Location") },
                    placeholder = { Text("40.7128,-74.0060") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    supportingText = { Text("Format: latitude,longitude") }
                )
            }

            else -> {
                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    label = { Text("Enter content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        }
    }
}
