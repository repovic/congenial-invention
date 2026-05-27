package com.example.sportzona.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sportzona.ui.components.InfoHighlightCard
import com.example.sportzona.ui.components.ScreenHeader
import com.example.sportzona.viewmodel.AppStateController
import com.example.sportzona.viewmodel.NavigationTarget

@Composable
fun DetailsView(controller: AppStateController) {
    val p = controller.focusedActivity ?: return
    var amount by remember { mutableStateOf(1) }
    val ctx = LocalContext.current

    val userCity = controller.currentUser.residenceCity
    val isLocal = userCity.isNotBlank() && p.facilityInfo.lowercase().contains(userCity.lowercase())

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ScreenHeader(
                    icon = Icons.Default.Info,
                    title = p.title,
                    subtitle = "Kompletan uvid u termine i kapacitete paketa."
                )
            }

            item {
                Text(
                    text = String.format("%.2f RSD", p.cost),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoHighlightCard(
                        desc = "Lokacija centra",
                        valStr = if (isLocal) "${p.facilityInfo} (Lokalno)" else p.facilityInfo,
                        glyph = Icons.Default.LocationOn
                    )
                    InfoHighlightCard(
                        desc = "Dostupni kapacitet",
                        valStr = "${p.capacity} slobodnih mesta",
                        glyph = Icons.Default.Groups
                    )
                    InfoHighlightCard(
                        desc = "Standardni termin",
                        valStr = "${p.waitDaysStandard} dana",
                        glyph = Icons.Default.DateRange
                    )
                    InfoHighlightCard(
                        desc = "Premium (Ekspres) termin",
                        valStr = "${p.waitDaysPremium} dana",
                        glyph = Icons.Default.Star
                    )
                }
            }

            item { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }

            item {
                Column {
                    Text("Rezervišite mesta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        FilledTonalIconButton(
                            onClick = { if (amount > 1) amount-- }, 
                            modifier = Modifier.size(48.dp), 
                            shape = RoundedCornerShape(12.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, null)
                        }
                        Text(amount.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        FilledTonalIconButton(
                            onClick = { if (amount < p.capacity) amount++ }, 
                            modifier = Modifier.size(48.dp), 
                            shape = RoundedCornerShape(12.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.KeyboardArrowRight, null)
                        }
                    }
                }
            }
        }

        Surface(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Ukupno za dodavanje:", style = MaterialTheme.typography.bodyLarge)
                    Text(String.format("%.2f RSD", p.cost * amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        controller.reserveActivity(p.id, amount) { ok ->
                            if (ok) {
                                Toast.makeText(ctx, "Dodato u korpu.", Toast.LENGTH_SHORT).show()
                                controller.jumpTo(NavigationTarget.Catalog)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, null)
                    Spacer(Modifier.width(8.dp))
                    Text("DODAJ U KORPU", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
