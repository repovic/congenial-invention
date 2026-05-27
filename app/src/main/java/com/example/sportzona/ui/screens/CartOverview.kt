package com.example.sportzona.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sportzona.ui.components.EmptyPlaceholder
import com.example.sportzona.ui.components.ScreenHeader
import com.example.sportzona.viewmodel.AppStateController
import com.example.sportzona.viewmodel.NavigationTarget

@Composable
fun CartOverview(controller: AppStateController) {
    val items = controller.currentCartEntries
    if (items.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) { EmptyPlaceholder(Icons.Default.ShoppingCart, "Vaša korpa je prazna.\nPregledajte pakete i dodajte ih u korpu.") }
            Surface(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Button(onClick = { controller.jumpTo(NavigationTarget.Catalog) }, modifier = Modifier.padding(16.dp).fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("PREGLEDAJ PONUDU", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val base = controller.calculateBaseTotal()
    val wait = controller.calculateTotalWait()

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(16.dp)) {
            item {
                ScreenHeader(
                    icon = Icons.Default.ShoppingCartCheckout,
                    title = "Pregled rezervacija",
                    subtitle = "Proverite stavke pre potvrde porudžbine."
                )
            }
            items(items) { entry ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.activity.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${entry.entry.amount} x ${String.format("%.2f", entry.activity.cost)} RSD", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format("%.2f RSD", entry.activity.cost * entry.entry.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { controller.deleteFromCart(entry.entry.id) }, colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                            Icon(Icons.Default.Delete, null)
                        }
                    }
                }
            }
        }

        Surface(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Privremena cena:", style = MaterialTheme.typography.bodyLarge)
                    Text(String.format("%.2f RSD", base), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Maksimalno čekanje:", style = MaterialTheme.typography.bodyLarge)
                    Text("$wait dana", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = { controller.jumpTo(NavigationTarget.FinalizeOrder) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("NASTAVI NA POTVRDU", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
