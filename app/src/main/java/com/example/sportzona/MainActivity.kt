package com.example.sportzona

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sportzona.ui.components.DataDisplayRow
import com.example.sportzona.ui.screens.*
import com.example.sportzona.ui.theme.SportZonaTheme
import com.example.sportzona.viewmodel.AppStateController
import com.example.sportzona.viewmodel.NavigationTarget

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SportZonaTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val appStateController: AppStateController = viewModel()
                    PortalView(appStateController)
                }
            }
        }
    }
}

@Composable
fun PortalView(controller: AppStateController) {
    val ctx = LocalContext.current
    val target = controller.activeScreen

    Scaffold(
        topBar = { PortalHeader(controller) },
        floatingActionButton = {
            if (target is NavigationTarget.Catalog) {
                FloatingActionButton(
                    onClick = { controller.jumpTo(NavigationTarget.Management(null)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) { Icon(Icons.Default.Add, "Novi Paket") }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (target) {
                is NavigationTarget.UserSetup -> UserSetupView(controller)
                is NavigationTarget.Catalog -> CatalogView(controller)
                is NavigationTarget.Details -> DetailsView(controller)
                is NavigationTarget.Management -> EditorView(controller, target.activityId)
                is NavigationTarget.CartView -> CartOverview(controller)
                is NavigationTarget.FinalizeOrder -> CheckoutView(controller)
            }

            if (controller.isOrderCompleted && controller.summaryData != null) {
                val data = controller.summaryData!!
                AlertDialog(
                    onDismissRequest = { },
                    icon = {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(72.dp)) {
                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(16.dp))
                        }
                    },
                    title = { Text("Uspešna Rezervacija!", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("Vaša porudžbina je primljena. Detalji su prikazani ispod.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), shape = RoundedCornerShape(12.dp)) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    DataDisplayRow("Adresa dostave:", data.destination)
                                    DataDisplayRow("Prvi termin:", "${data.estimatedDays} dana")
                                }
                            }
                            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primary) {
                                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Ukupno za uplatu:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                                    Text(String.format("%.2f RSD", data.totalCost), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                controller.closeOrderAndReset()
                                Toast.makeText(ctx, "Hvala na poverenju!", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("ZATVORI", fontWeight = FontWeight.Bold) }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortalHeader(controller: AppStateController) {
    val target = controller.activeScreen
    val cartSize = controller.currentCartEntries.sumOf { it.entry.amount }

    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when (target) {
                        is NavigationTarget.UserSetup -> "Moj Profil"
                        is NavigationTarget.Catalog -> "SportZona"
                        is NavigationTarget.Details -> "Detalji"
                        is NavigationTarget.Management -> if (target.activityId == null) "Novi Paket" else "Izmeni Paket"
                        is NavigationTarget.CartView -> "Moja Korpa"
                        is NavigationTarget.FinalizeOrder -> "Plaćanje"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                if (target is NavigationTarget.Catalog) {
                    // Subtitle removed for cleaner look
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        navigationIcon = {
            if (target !is NavigationTarget.UserSetup && target !is NavigationTarget.Catalog) {
                IconButton(onClick = {
                    when (target) {
                        is NavigationTarget.Details -> controller.jumpTo(NavigationTarget.Catalog)
                        is NavigationTarget.Management -> controller.jumpTo(NavigationTarget.Catalog)
                        is NavigationTarget.CartView -> controller.jumpTo(NavigationTarget.Catalog)
                        is NavigationTarget.FinalizeOrder -> controller.jumpTo(NavigationTarget.CartView)
                        else -> controller.jumpTo(NavigationTarget.Catalog)
                    }
                }) { Icon(Icons.Default.ArrowBack, "Nazad") }
            } else if (target is NavigationTarget.Catalog) {
                IconButton(onClick = { controller.jumpTo(NavigationTarget.UserSetup) }) {
                    Icon(Icons.Default.Person, "Profil")
                }
            }
        },
        actions = {
            if (target is NavigationTarget.Catalog) {
                IconButton(onClick = { controller.jumpTo(NavigationTarget.CartView) }) {
                    BadgedBox(badge = { if (cartSize > 0) Badge { Text(cartSize.toString()) } }) {
                        Icon(Icons.Default.ShoppingCart, "Korpa")
                    }
                }
            }
        }
    )
}
