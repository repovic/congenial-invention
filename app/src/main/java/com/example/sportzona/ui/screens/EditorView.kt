package com.example.sportzona.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sportzona.ui.components.InputWidget
import com.example.sportzona.ui.components.ScreenHeader
import com.example.sportzona.viewmodel.AppStateController

@Composable
fun EditorView(controller: AppStateController, id: Long?) {
    val existing = controller.focusedActivity
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var slots by remember { mutableStateOf("") }
    var stdD by remember { mutableStateOf("") }
    var prmD by remember { mutableStateOf("") }
    var locInfo by remember { mutableStateOf("") }
    
    var extraByInput by remember { mutableStateOf("") }

    LaunchedEffect(existing) {
        if (id != null && existing != null) {
            name = existing.title
            price = existing.cost.toString()
            slots = existing.capacity.toString()
            stdD = existing.waitDaysStandard.toString()
            prmD = existing.waitDaysPremium.toString()
            locInfo = existing.facilityInfo
        }
    }

    val valid = name.isNotBlank() && price.isNotBlank() && slots.isNotBlank() && locInfo.isNotBlank()

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(16.dp)) {
            item {
                ScreenHeader(
                    icon = if (id == null) Icons.Default.AddBox else Icons.Default.EditNote,
                    title = if (id == null) "Kreiraj novi paket" else "Izmeni postojeći paket",
                    subtitle = "Popunite sve informacije o sportskom paketu."
                )
            }

            item { InputWidget(name, { name = it }, "Naziv paketa", Icons.Default.Edit) }
            item { InputWidget(price, { price = it }, "Cena (RSD)", Icons.Default.Info, KeyboardType.Number) }
            item { InputWidget(slots, { slots = it }, "Broj slobodnih mesta", Icons.Default.CheckCircle, KeyboardType.Number) }
            
            item { InputWidget(stdD, { stdD = it }, "Standard (dani)", Icons.Default.DateRange, KeyboardType.Number) }
            item { InputWidget(prmD, { prmD = it }, "Premium (dani)", Icons.Default.Star, KeyboardType.Number) }
            
            item { InputWidget(locInfo, { locInfo = it }, "Grad i sportski centar", Icons.Default.LocationOn) }

            if (id != null) {
                item { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Brzo dodavanje mesta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Dopišite slobodne termine na trenutni kapacitet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = extraByInput,
                                onValueChange = { extraByInput = it },
                                placeholder = { Text("Unesite broj novih mesta") },
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline)
                            )
                            Button(
                                onClick = {
                                    extraByInput.toIntOrNull()?.let { if (it > 0) { controller.expandCapacity(id, it); extraByInput = "" } }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Text("Dopiši")
                            }
                        }
                    }
                }
            }
        }

        Surface(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
            Button(
                onClick = {
                    if (valid) {
                        val c = price.toDoubleOrNull() ?: 0.0
                        val s = slots.toIntOrNull() ?: 0
                        val sd = stdD.toIntOrNull() ?: 0
                        val pd = prmD.toIntOrNull() ?: 0
                        if (id == null) controller.createActivity(name, c, s, sd, pd, locInfo)
                        else controller.updateActivity(id, name, c, s, sd, pd, locInfo)
                    }
                },
                enabled = valid,
                modifier = Modifier.padding(16.dp).fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SAČUVAJ PROMENE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
