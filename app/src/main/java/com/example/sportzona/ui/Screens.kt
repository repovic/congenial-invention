package com.example.sportzona.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportzona.data.SportPackage

@Composable
fun SportZonaAppContent(viewModel: SportZonaViewModel) {
    val context = LocalContext.current
    val screen = viewModel.currentScreen

    Scaffold(
        topBar = {
            SportZonaTopAppBar(viewModel = viewModel)
        },
        floatingActionButton = {
            if (screen is Screen.PackageList) {
                FloatingActionButton(
                    onClick = { viewModel.navigateTo(Screen.AddEditPackage(null)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Dodaj Paket")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (screen) {
                is Screen.UserProfile -> UserProfileScreen(viewModel)
                is Screen.PackageList -> PackageListScreen(viewModel)
                is Screen.PackageDetail -> PackageDetailScreen(viewModel, screen.packageId)
                is Screen.AddEditPackage -> AddEditPackageScreen(viewModel, screen.packageId)
                is Screen.Cart -> CartScreen(viewModel)
                is Screen.Checkout -> CheckoutScreen(viewModel)
            }

            if (viewModel.showOrderConfirmationDialog && viewModel.orderDialogInfo != null) {
                val info = viewModel.orderDialogInfo!!
                AlertDialog(
                    onDismissRequest = { },
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    icon = { 
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(72.dp),
                            tonalElevation = 0.dp
                        ) {
                            Icon(
                                Icons.Default.CheckCircle, 
                                contentDescription = null, 
                                tint = MaterialTheme.colorScheme.primary, 
                                modifier = Modifier.padding(16.dp)
                            ) 
                        }
                    },
                    title = {
                        Text(
                            "Uspešna Rezervacija!", 
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold, 
                            textAlign = TextAlign.Center, 
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp), 
                            horizontalAlignment = Alignment.CenterHorizontally, 
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Vaša porudžbina je primljena. Detalji su prikazani ispod.", 
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    InfoRow("Adresa dostave:", info.address)
                                    InfoRow("Prvi termin:", "${info.waitingDays} dana")
                                }
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                tonalElevation = 0.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Ukupno za uplatu:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                                    Text(
                                        String.format("%.2f RSD", info.finalPrice),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.dismissOrderDialogAndClearCart()
                                Toast.makeText(context, "Hvala na poverenju!", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp)
                        ) {
                            Text("ZATVORI", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f).padding(start = 8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportZonaTopAppBar(viewModel: SportZonaViewModel) {
    val screen = viewModel.currentScreen
    val cartCount = viewModel.cartItems.sumOf { it.cartItem.quantity }

    TopAppBar(
        title = {
            Column {
                Text(
                    text = when (screen) {
                        is Screen.UserProfile -> "Moj Profil"
                        is Screen.PackageList -> "SportZona"
                        is Screen.PackageDetail -> "Detalji"
                        is Screen.AddEditPackage -> if (screen.packageId == null) "Novi Paket" else "Izmeni Paket"
                        is Screen.Cart -> "Moja Korpa"
                        is Screen.Checkout -> "Plaćanje"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (screen is Screen.PackageList) {
                    Text(
                        "Pronađi svoj idealan trening.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            if (screen !is Screen.UserProfile && screen !is Screen.PackageList) {
                IconButton(onClick = {
                    when (screen) {
                        is Screen.PackageDetail -> viewModel.navigateTo(Screen.PackageList)
                        is Screen.AddEditPackage -> viewModel.navigateTo(Screen.PackageList)
                        is Screen.Cart -> viewModel.navigateTo(Screen.PackageList)
                        is Screen.Checkout -> viewModel.navigateTo(Screen.Cart)
                        else -> viewModel.navigateTo(Screen.PackageList)
                    }
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Nazad")
                }
            } else if (screen is Screen.PackageList) {
                IconButton(onClick = { viewModel.navigateTo(Screen.UserProfile) }) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profil",
                            modifier = Modifier.padding(6.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        actions = {
            if (screen is Screen.PackageList) {
                IconButton(onClick = { viewModel.navigateTo(Screen.Cart) }) {
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(cartCount.toString())
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Korpa")
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun UserProfileScreen(viewModel: SportZonaViewModel) {
    val user = viewModel.userProfile
    var firstName by remember { mutableStateOf(user.firstName) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var email by remember { mutableStateOf(user.email) }
    var street by remember { mutableStateOf(user.street) }
    var city by remember { mutableStateOf(user.city) }

    val isFormValid = firstName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank() && street.isNotBlank() && city.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.padding(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Dobrodošli u SportZona",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Unesite svoje podatke za izdavanje članske karte.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                SportTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = "Ime",
                    icon = Icons.Default.Person
                )
            }
            item {
                SportTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = "Prezime",
                    icon = Icons.Default.Person
                )
            }
            item {
                SportTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "E-mail",
                    icon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email
                )
            }
            item {
                SportTextField(
                    value = street,
                    onValueChange = { street = it },
                    label = "Ulica i broj",
                    icon = Icons.Default.Home
                )
            }
            item {
                SportTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = "Grad",
                    icon = Icons.Default.LocationOn
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary,
            tonalElevation = 0.dp
        ) {
            Button(
                onClick = { if (isFormValid) viewModel.saveUserProfile(firstName, lastName, email, street, city) },
                enabled = isFormValid,
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp)
            ) {
                Text("SAČUVAJ I NASTAVI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SportTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
fun PackageListScreen(viewModel: SportZonaViewModel) {
    val packages = viewModel.packagesList
    val userCity = viewModel.userProfile.city

    Box(modifier = Modifier.fillMaxSize()) {
        if (packages.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Info,
                message = "Trenutno nema dostupnih paketa.\nDodajte novi paket pomoću dugmeta ispod."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column {
                        Text(
                            "Zdravo, ${viewModel.userProfile.firstName} 👋",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "Pogledajte dostupne sportske pakete.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(packages) { pkg ->
                    PackageListItem(pkg, userCity, viewModel)
                }
            }
        }
    }
}

@Composable
fun PackageListItem(pkg: SportPackage, userCity: String, viewModel: SportZonaViewModel) {
    val isLocal = userCity.isNotBlank() && pkg.cityAndCenter.lowercase().contains(userCity.lowercase())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.navigateTo(Screen.PackageDetail(pkg.id)) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                pkg.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (isLocal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (isLocal) "${pkg.cityAndCenter} (Lokalno)" else pkg.cityAndCenter,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLocal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isLocal) FontWeight.Bold else FontWeight.Normal
                )
            }
            
            Spacer(Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = String.format("%.2f RSD", pkg.price),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().height(36.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxHeight(),
                        tonalElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                            Text(
                                text = "${pkg.availableSlots} slobodnih mesta",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    FilledTonalIconButton(
                        onClick = { viewModel.addExtraSlotsToPackage(pkg.id, 5) },
                        modifier = Modifier.fillMaxHeight().aspectRatio(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("+5", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                FilledTonalIconButton(
                    onClick = { viewModel.navigateTo(Screen.AddEditPackage(pkg.id)) },
                    modifier = Modifier.fillMaxHeight().aspectRatio(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Izmeni", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                message,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PackageDetailScreen(viewModel: SportZonaViewModel, packageId: Long) {
    val pkg = viewModel.selectedPackage ?: return
    var qty by remember { mutableStateOf(1) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            pkg.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            String.format("%.2f RSD", pkg.price),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape,
                            tonalElevation = 0.dp
                        ) {
                            Text(
                                text = "${pkg.availableSlots} mesta",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(pkg.cityAndCenter, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailInfoCard("Standardni termin", "${pkg.standardDays} dana", Icons.Default.DateRange)
                    DetailInfoCard("Premium (Ekspres) termin", "${pkg.premiumDays} dana", Icons.Default.Star)
                }
            }

            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            item {
                Column {
                    Text(
                        "Rezervišite mesta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilledTonalIconButton(
                            onClick = { if (qty > 1) qty-- },
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, null)
                        }
                        
                        Text(
                            qty.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        FilledTonalIconButton(
                            onClick = { if (qty < pkg.availableSlots) qty++ },
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

        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ukupno za dodavanje:", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = String.format("%.2f RSD", pkg.price * qty),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.addPackageToCart(pkg.id, qty) { success ->
                            if (success) {
                                Toast.makeText(context, "Dodato u korpu.", Toast.LENGTH_SHORT).show()
                                viewModel.navigateTo(Screen.PackageList)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, null)
                    Spacer(Modifier.width(8.dp))
                    Text("DODAJ U KORPU", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DetailInfoCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp),
                tonalElevation = 0.dp
            ) {
                Icon(
                    icon,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AddEditPackageScreen(viewModel: SportZonaViewModel, packageId: Long?) {
    val pkg = viewModel.selectedPackage
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var slots by remember { mutableStateOf("") }
    var std by remember { mutableStateOf("") }
    var prem by remember { mutableStateOf("") }
    var cityCenter by remember { mutableStateOf("") }
    
    var extraSlotsInput by remember { mutableStateOf("") }

    LaunchedEffect(pkg) {
        if (packageId != null && pkg != null) {
            name = pkg.name
            price = pkg.price.toString()
            slots = pkg.availableSlots.toString()
            std = pkg.standardDays.toString()
            prem = pkg.premiumDays.toString()
            cityCenter = pkg.cityAndCenter
        }
    }

    val isFormValid = name.isNotBlank() && price.isNotBlank() && slots.isNotBlank() && cityCenter.isNotBlank()

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    text = if (packageId == null) "Kreiraj novi paket" else "Izmeni postojeći paket",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Popunite sve informacije o sportskom paketu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }

            item { SportTextField(name, { name = it }, "Naziv paketa", Icons.Default.Edit) }
            item { SportTextField(price, { price = it }, "Cena (RSD)", Icons.Default.Info, KeyboardType.Number) }
            item { SportTextField(slots, { slots = it }, "Broj slobodnih mesta", Icons.Default.CheckCircle, KeyboardType.Number) }
            
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        SportTextField(std, { std = it }, "Standard (dani)", Icons.Default.DateRange, KeyboardType.Number)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SportTextField(prem, { prem = it }, "Premium (dani)", Icons.Default.Star, KeyboardType.Number)
                    }
                }
            }
            
            item { SportTextField(cityCenter, { cityCenter = it }, "Grad i sportski centar", Icons.Default.LocationOn) }

            if (packageId != null) {
                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Brzo dodavanje mesta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Dopišite slobodne termine na trenutni kapacitet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = extraSlotsInput,
                                onValueChange = { extraSlotsInput = it },
                                placeholder = { Text("Unesite broj novih mesta") },
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            Button(
                                onClick = {
                                        val extra = extraSlotsInput.toIntOrNull() ?: 0
                                        if (extra > 0) {
                                            viewModel.addExtraSlotsToPackage(packageId, extra)
                                            extraSlotsInput = ""
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxHeight(),
                                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp)
                            ) {
                                Text("Dopiši")
                            }
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Button(
                onClick = {
                    if (isFormValid) {
                        val p = price.toDoubleOrNull() ?: 0.0
                        val s = slots.toIntOrNull() ?: 0
                        val st = std.toIntOrNull() ?: 0
                        val pr = prem.toIntOrNull() ?: 0
                        if (packageId == null) viewModel.addPackage(name, p, s, st, pr, cityCenter)
                        else viewModel.updatePackage(packageId, name, p, s, st, pr, cityCenter)
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp)
            ) {
                Text("SAČUVAJ PROMENE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CartScreen(viewModel: SportZonaViewModel) {
    val items = viewModel.cartItems
    if (items.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                EmptyStateView(
                    icon = Icons.Default.ShoppingCart,
                    message = "Vaša korpa je prazna.\nPregledajte pakete i dodajte ih u korpu."
                )
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 0.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { viewModel.navigateTo(Screen.PackageList) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp)
                    ) {
                        Text("PREGLEDAJ PONUDU", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val basePrice = viewModel.getCartBasePrice()
    val waitingDays = viewModel.getCartWaitingDays()

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    "Pregled rezervacija",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Proverite stavke pre potvrde porudžbine.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }

            items(items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.sportPackage.name, 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${item.cartItem.quantity} x ${String.format("%.2f", item.sportPackage.price)} RSD",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                String.format("%.2f RSD", item.sportPackage.price * item.cartItem.quantity),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        IconButton(
                            onClick = { viewModel.removeCartItem(item.cartItem.id) },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Ukloni")
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Privremena cena:", style = MaterialTheme.typography.bodyLarge)
                    Text(String.format("%.2f RSD", basePrice), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Maksimalno čekanje:", style = MaterialTheme.typography.bodyLarge)
                    Text("$waitingDays dana", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.navigateTo(Screen.Checkout) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp)
                ) {
                    Text("NASTAVI NA POTVRDU", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CheckoutScreen(viewModel: SportZonaViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    "Potvrda rezervacije",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Izaberite način izdavanja članske kartice.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }
            
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DeliveryOption(viewModel, DeliveryMethod.STANDARD_RECEPTION, "Standardno preuzimanje", "Besplatno preuzimanje na recepciji sportskog centra.")
                    DeliveryOption(viewModel, DeliveryMethod.DIGITAL, "Digitalno izdavanje", "Popust 10% - Digitalna kartica (SMS / E-mail).")
                    DeliveryOption(viewModel, DeliveryMethod.PREMIUM_DELIVERY, "Premium dostava", "Doplata 20% - Fizička kartica + „welcome“ paket.")
                }
            }

            if (viewModel.selectedDeliveryMethod == DeliveryMethod.PREMIUM_DELIVERY) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(0.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.differentAddressEnabled = !viewModel.differentAddressEnabled }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = viewModel.differentAddressEnabled,
                                    onCheckedChange = { viewModel.differentAddressEnabled = it }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Dostavi na drugu adresu.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            
                            AnimatedVisibility(visible = viewModel.differentAddressEnabled) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp), 
                                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                                ) {
                                    SportTextField(viewModel.alternateStreet, { viewModel.alternateStreet = it }, "Druga ulica i broj", Icons.Default.Home)
                                    SportTextField(viewModel.alternateCity, { viewModel.alternateCity = it }, "Drugi grad", Icons.Default.LocationOn)
                                }
                            }
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Konačna cena:", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        String.format("%.2f RSD", viewModel.getCartFinalPrice()),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Vreme do termina:", style = MaterialTheme.typography.bodyLarge)
                    Text("${viewModel.getCartWaitingDays()} dana", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.confirmOrder() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp)
                ) {
                    Text("POTVRDI I REZERVIŠI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DeliveryOption(viewModel: SportZonaViewModel, method: DeliveryMethod, label: String, sub: String) {
    val isSelected = viewModel.selectedDeliveryMethod == method
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.selectedDeliveryMethod = method },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = { viewModel.selectedDeliveryMethod = method })
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = label, 
                    style = MaterialTheme.typography.bodyLarge, 
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = sub, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
