package com.pdfwallet.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pdfwallet.data.db.Document
import com.pdfwallet.data.db.DocumentMetadata
import kotlinx.serialization.json.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketEditBottomSheet(
    document: Document,
    onDismiss: () -> Unit,
    onSave: (Document) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val jsonFormat = Json { encodeDefaults = true; classDiscriminator = "type"; ignoreUnknownKeys = true }
    
    var fields by remember { 
        mutableStateOf(
            if (document.metadata != null) {
                try {
                    val jsonObj = jsonFormat.encodeToJsonElement(DocumentMetadata.serializer(), document.metadata).jsonObject
                    val map = mutableMapOf<String, String>()
                    jsonObj.forEach { (key, value) ->
                        if (key != "type" && key != "customFields" && value is JsonPrimitive && !value.isString && value.contentOrNull != null) {
                            map[key] = value.content
                        } else if (key != "type" && key != "customFields" && value is JsonPrimitive && value.isString) {
                            map[key] = value.content
                        }
                    }
                    val custom = (jsonObj["customFields"] as? JsonObject)?.mapValues { (it.value as? JsonPrimitive)?.content ?: "" } ?: emptyMap()
                    map.putAll(custom)
                    map
                } catch (e: Exception) {
                    mutableMapOf<String, String>()
                }
            } else {
                mutableMapOf<String, String>()
            }
        )
    }

    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = {
                    try {
                        val baseJson = if (document.metadata != null) {
                            jsonFormat.encodeToJsonElement(DocumentMetadata.serializer(), document.metadata).jsonObject.toMutableMap()
                        } else {
                            mutableMapOf()
                        }
                        
                        val customFieldsMap = mutableMapOf<String, JsonElement>()
                        
                        fields.forEach { (k, v) ->
                            if (baseJson.containsKey(k) && k != "customFields" && k != "type") {
                                baseJson[k] = JsonPrimitive(v)
                            } else {
                                customFieldsMap[k] = JsonPrimitive(v)
                            }
                        }
                        
                        baseJson["customFields"] = JsonObject(customFieldsMap)
                        
                        val newMetadata = jsonFormat.decodeFromJsonElement(DocumentMetadata.serializer(), JsonObject(baseJson))
                        onSave(document.copy(metadata = newMetadata))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    onDismiss()
                }) {
                    Text("Save")
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(fields.keys.toList()) { key ->
                    OutlinedTextField(
                        value = fields[key] ?: "",
                        onValueChange = { 
                            fields = fields.toMutableMap().apply { put(key, it) }
                        },
                        label = { Text(key.replace(Regex("([a-z])([A-Z]+)"), "$1 $2").capitalize(Locale.getDefault())) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        trailingIcon = {
                            IconButton(onClick = {
                                fields = fields.toMutableMap().apply { remove(key) }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove field")
                            }
                        }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Custom Field")
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Field") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newKey,
                        onValueChange = { newKey = it },
                        label = { Text("Field Name (e.g. seatNumber)") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newValue,
                        onValueChange = { newValue = it },
                        label = { Text("Value") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newKey.isNotBlank()) {
                        fields = fields.toMutableMap().apply { put(newKey.trim(), newValue) }
                        newKey = ""
                        newValue = ""
                        showAddDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
