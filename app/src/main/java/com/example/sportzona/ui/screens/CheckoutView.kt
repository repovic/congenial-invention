package com.example.sportzona.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sportzona.ui.components.InputWidget
import com.example.sportzona.ui.components.ScreenHeader
import com.example.sportzona.viewmodel.AppStateController
import com.example.sportzona.viewmodel.MembershipDelivery

@Composable
fun CheckoutView(controller: AppStateController) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(16.dp)) {
            item {
                ScreenHeader(
                    icon = Icons.Default.VerifiedUser,
                    title = "Potvrda rezervacije",
                    subtitle = "Izaberite način izdavanja članske kartice."
                )
            }
            
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OptionItem(controller, MembershipDelivery.RECEPTION, "Standardno preuzimanje", "Besplatno preuzimanje na recepciji sportskog centra.")
                    OptionItem(controller, MembershipDelivery.VIRTUAL, "Digitalno izdavanje", "Popust 10% - Digitalna kartica (SMS / E-mail).")
                    OptionItem(controller, MembershipDelivery.PREMIUM_COURIER, "Premium dostava", "Doplata 20% - Fizička kartica + „welcome“ paket.")
                }
            }

            if (controller.deliverySelection == MembershipDelivery.PREMIUM_COURIER) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth().clickable { controller.customDeliveryAddress.value = !controller.customDeliveryAddress.value }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = controller.customDeliveryAddress.value, onCheckedChange = { controller.customDeliveryAddress.value = it })
                                Spacer(Modifier.width(8.dp))
                                Text("Dostavi na drugu adresu.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            AnimatedVisibility(visible = controller.customDeliveryAddress.value) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                                    InputWidget(controller.altStreet.value, { controller.altStreet.value = it }, "Druga ulica i broj", Icons.Default.Home)
                                    InputWidget(controller.altCity.value, { controller.altCity.value = it }, "Drugi grad", Icons.Default.LocationOn)
                                }
                            }
                        }
                    }
                }
            }
        }

        Surface(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Konačna cena:", style = MaterialTheme.typography.bodyLarge)
                    Text(String.format("%.2f RSD", controller.calculateFinalTotal()), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Vreme do termina:", style = MaterialTheme.typography.bodyLarge)
                    Text("${controller.calculateTotalWait()} dana", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = { controller.executeOrder() }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("POTVRDI I REZERVIŠI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OptionItem(controller: AppStateController, mode: MembershipDelivery, title: String, subtitle: String) {
    val active = controller.deliverySelection == mode
    Card(
        modifier = Modifier.fillMaxWidth().clickable { controller.deliverySelection = mode },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (active) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface),
        border = if (active) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = active, onClick = { controller.deliverySelection = mode })
            Spacer(Modifier.width(8.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
