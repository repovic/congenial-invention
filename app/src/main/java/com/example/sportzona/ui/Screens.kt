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

// --- MAIN NAV HOST ---
@Composable
fun SportZonaAppContent(viewModel: SportZonaViewModel) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            SportZonaTopAppBar(viewModel = viewModel)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = viewModel.currentScreen) {
                is Screen.UserProfile -> UserProfileScreen(viewModel)
                is Screen.PackageList -> PackageListScreen(viewModel)
                is Screen.PackageDetail -> PackageDetailScreen(viewModel, screen.packageId)
                is Screen.AddEditPackage -> AddEditPackageScreen(viewModel, screen.packageId)
                is Screen.Cart -> CartScreen(viewModel)
                is Screen.Checkout -> CheckoutScreen(viewModel)
            }

            // Global Confirmation Dialog
            if (viewModel.showOrderConfirmationDialog && viewModel.orderDialogInfo != null) {
                val info = viewModel.orderDialogInfo!!
                AlertDialog(
                    onDismissRequest = { },
                    containerColor = Color.White,
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(48.dp)) },
                    title = {
                        Text("Rezervacija uspešna!", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("Vaša članska kartica je spremna za preuzimanje.", fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Adresa:", fontSize = 13.sp, color = Color.Gray)
                                    Text(info.address, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Dostupno za:", fontSize = 13.sp, color = Color.Gray)
                                    Text("${info.waitingDays} dana", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black)
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Ukupno: ${String.format("%.2f RSD", info.finalPrice)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.dismissOrderDialogAndClearCart()
                                Toast.makeText(context, "Hvala na poverenju!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("ZAVRŠI", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                )
            }
        }
    }
}

// --- SHARED TOP BAR ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportZonaTopAppBar(viewModel: SportZonaViewModel) {
    val screen = viewModel.currentScreen
    val cartCount = viewModel.cartItems.sumOf { it.cartItem.quantity }

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = when (screen) {
                    is Screen.UserProfile -> "Korisnički Profil"
                    is Screen.PackageList -> "SportZona"
                    is Screen.PackageDetail -> "Detalji Paketa"
                    is Screen.AddEditPackage -> if (screen.packageId == null) "Novi Paket" else "Izmeni Paket"
                    is Screen.Cart -> "Korpa Rezervacija"
                    is Screen.Checkout -> "Potvrda"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
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
                    Icon(Icons.Default.Person, contentDescription = "Profil")
                }
            }
        },
        actions = {
            if (screen is Screen.PackageList) {
                IconButton(onClick = { viewModel.navigateTo(Screen.Cart) }) {
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge(containerColor = Color.Black, contentColor = Color.White) {
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
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black,
            navigationIconContentColor = Color.Black,
            actionIconContentColor = Color.Black
        )
    )
}

// --- 1. USER PROFILE SCREEN ---
@Composable
fun UserProfileScreen(viewModel: SportZonaViewModel) {
    val user = viewModel.userProfile
    var firstName by remember { mutableStateOf(user.firstName) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var email by remember { mutableStateOf(user.email) }
    var street by remember { mutableStateOf(user.street) }
    var city by remember { mutableStateOf(user.city) }

    val isFormValid = firstName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank() && street.isNotBlank() && city.isNotBlank()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Unesite vaše podatke za člansku kartu. Ovi podaci će se koristiti za dostavu i personalizaciju vašeg naloga.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Ime") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    supportingText = { Text("Unesite vaše ime.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(0.dp)
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Prezime") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    supportingText = { Text("Unesite vaše prezime.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(0.dp)
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    supportingText = { Text("Adresa na koju ćete primati obaveštenja.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(0.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = street,
                    onValueChange = { street = it },
                    label = { Text("Ulica i broj") },
                    leadingIcon = { Icon(Icons.Default.Home, null) },
                    supportingText = { Text("Adresa za dostavu kartice.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(0.dp)
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Grad") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                    supportingText = { Text("Grad u kome ćete preuzeti karticu.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(0.dp)
                )
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            border = BorderStroke(1.dp, Color.LightGray),
            color = Color.White
        ) {
            Button(
                onClick = { if (isFormValid) viewModel.saveUserProfile(firstName, lastName, email, street, city) },
                enabled = isFormValid,
                modifier = Modifier.padding(16.dp).fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("NASTAVI", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

// --- 2. PACKAGE LIST SCREEN ---
@Composable
fun PackageListScreen(viewModel: SportZonaViewModel) {
    val packages = viewModel.packagesList
    val userCity = viewModel.userProfile.city

    Box(modifier = Modifier.fillMaxSize()) {
        if (packages.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Info, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Text("Nema dostupnih paketa", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Zdravo, ${viewModel.userProfile.firstName}!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                    Text(
                        "Pregledajte dostupne sportske pakete u vašoj blizini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                items(packages) { pkg ->
                    PackageListItem(pkg, userCity, viewModel)
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.navigateTo(Screen.AddEditPackage(null)) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).padding(bottom = 24.dp),
            containerColor = Color.Black,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Dodaj")
        }
    }
}

@Composable
fun PackageListItem(pkg: SportPackage, userCity: String, viewModel: SportZonaViewModel) {
    val isLocal = userCity.isNotBlank() && pkg.cityAndCenter.lowercase().contains(userCity.lowercase())

    Card(
        modifier = Modifier.fillMaxWidth().clickable { viewModel.navigateTo(Screen.PackageDetail(pkg.id)) },
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                pkg.name, 
                fontWeight = FontWeight.ExtraBold, 
                fontSize = 18.sp,
                maxLines = 1, 
                overflow = TextOverflow.Ellipsis,
                color = Color.Black
            )
            
            Text(
                if (isLocal) "${pkg.cityAndCenter} (u vašem gradu)" else pkg.cityAndCenter, 
                fontSize = 12.sp, 
                color = if (isLocal) MaterialTheme.colorScheme.primary else Color.Gray,
                fontWeight = if (isLocal) FontWeight.Bold else FontWeight.Normal
            )
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BadgeMinimal(text = "${pkg.availableSlots} slobodnih termina", color = Color.Black)
                
                Text(
                    String.format("%.2f RSD", pkg.price), 
                    fontWeight = FontWeight.ExtraBold, 
                    color = Color.Black, 
                    fontSize = 18.sp
                )
            }
            
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { viewModel.navigateTo(Screen.PackageDetail(pkg.id)) }, 
                    modifier = Modifier.height(40.dp).weight(1f), 
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    Text("DETALJNIJE", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
                }
                OutlinedButton(
                    onClick = { viewModel.addExtraSlotsToPackage(pkg.id, 5) }, 
                    modifier = Modifier.height(40.dp), 
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    Text("+5", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { viewModel.navigateTo(Screen.AddEditPackage(pkg.id)) }, 
                    modifier = Modifier.size(40.dp), 
                    contentPadding = PaddingValues(0.dp), 
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun BadgeMinimal(text: String, color: Color) {
    Surface(
        border = BorderStroke(1.dp, color),
        shape = RoundedCornerShape(0.dp),
        color = Color.White
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

// --- 3. PACKAGE DETAIL SCREEN ---
@Composable
fun PackageDetailScreen(viewModel: SportZonaViewModel, packageId: Long) {
    val pkg = viewModel.selectedPackage ?: return
    var qty by remember { mutableStateOf(1) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Pregledajte detaljne informacije o izabranom sportskom paketu i rezervišite željeni broj mesta.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                Text(pkg.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                Text(String.format("%.2f RSD", pkg.price), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text(pkg.cityAndCenter, color = Color.Gray, fontSize = 14.sp)
                }
                Spacer(Modifier.height(16.dp))
                
                DetailInfoCard("Kapacitet", "${pkg.availableSlots} slobodnih mesta", Icons.Default.Info)
                DetailInfoCard("Standardni termin", "${pkg.standardDays} dana čekanja", Icons.Default.DateRange)
                DetailInfoCard("Premium termin", "${pkg.premiumDays} dana čekanja", Icons.Default.Star)
                
                Spacer(Modifier.height(24.dp))
                Text("Izaberite broj mesta:", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    IconButton(onClick = { if (qty > 1) qty-- }) { Icon(Icons.Default.KeyboardArrowLeft, null, modifier = Modifier.size(32.dp)) }
                    Text(qty.toString(), fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.padding(horizontal = 24.dp))
                    IconButton(onClick = { if (qty < pkg.availableSlots) qty++ }) { Icon(Icons.Default.KeyboardArrowRight, null, modifier = Modifier.size(32.dp)) }
                }
            }
        }

        Surface(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(), border = BorderStroke(1.dp, Color.LightGray), color = Color.White) {
            Button(
                onClick = { 
                    viewModel.addPackageToCart(pkg.id, qty) { success ->
                        if (success) {
                            Toast.makeText(context, "Dodato u korpu", Toast.LENGTH_SHORT).show()
                            viewModel.navigateTo(Screen.PackageList)
                        }
                    }
                },
                modifier = Modifier.padding(16.dp).fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.ShoppingCart, null)
                Spacer(Modifier.width(8.dp))
                Text("DODAJ U KORPU", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun DetailInfoCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.Black, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, fontSize = 12.sp, color = Color.Gray)
                Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

// --- 4. ADD & EDIT PACKAGE SCREEN ---
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
            name = pkg.name; price = pkg.price.toString(); slots = pkg.availableSlots.toString()
            std = pkg.standardDays.toString(); prem = pkg.premiumDays.toString(); cityCenter = pkg.cityAndCenter
        }
    }

    val isFormValid = name.isNotBlank() && price.isNotBlank() && slots.isNotBlank() && cityCenter.isNotBlank()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Icon(
                    imageVector = if (packageId == null) Icons.Default.AddCircle else Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Unesite detalje sportskog paketa koji želite da dodate ili izmenite u sistemu SportZona.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(24.dp))
            }
            
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Naziv paketa") },
                    leadingIcon = { Icon(Icons.Default.Edit, null) },
                    supportingText = { Text("Primer: Premium Tenis 1h.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(0.dp)
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Cena (RSD)") },
                    leadingIcon = { Icon(Icons.Default.Info, null) },
                    supportingText = { Text("Ukupna cena sa PDV-om.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(0.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = slots,
                    onValueChange = { slots = it },
                    label = { Text("Broj mesta") },
                    leadingIcon = { Icon(Icons.Default.CheckCircle, null) },
                    supportingText = { Text("Ukupan broj slobodnih mesta.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(0.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = std,
                    onValueChange = { std = it },
                    label = { Text("Standard dani") },
                    leadingIcon = { Icon(Icons.Default.DateRange, null) },
                    supportingText = { Text("Dani čekanja za standardnu obradu.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(0.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = prem,
                    onValueChange = { prem = it },
                    label = { Text("Premium dani") },
                    leadingIcon = { Icon(Icons.Default.Star, null) },
                    supportingText = { Text("Dani čekanja za ekspresnu obradu.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(0.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = cityCenter,
                    onValueChange = { cityCenter = it },
                    label = { Text("Grad i sportski centar") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                    supportingText = { Text("Lokacija gde se paket koristi.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(0.dp)
                )
            }

            if (packageId != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.LightGray),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Dopisivanje slobodnih mesta", fontWeight = FontWeight.ExtraBold)
                            Text("Brzo dodavanje novih slobodnih kapaciteta u ovaj paket.", fontSize = 12.sp, color = Color.Gray)
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = extraSlotsInput,
                                    onValueChange = { extraSlotsInput = it },
                                    label = { Text("Broj") },
                                    leadingIcon = { Icon(Icons.Default.Add, null) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(0.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                Button(
                                    onClick = {
                                        val extra = extraSlotsInput.toIntOrNull() ?: 0
                                        if (extra > 0) {
                                            viewModel.addExtraSlotsToPackage(packageId, extra)
                                            extraSlotsInput = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(0.dp),
                                    modifier = Modifier.height(56.dp).offset(y = (-4).dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                                ) {
                                    Text("Dopiši", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Surface(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(), border = BorderStroke(1.dp, Color.LightGray), color = Color.White) {
            Button(
                onClick = {
                    if (isFormValid) {
                        val p = price.toDoubleOrNull() ?: 0.0; val s = slots.toIntOrNull() ?: 0
                        val st = std.toIntOrNull() ?: 0; val pr = prem.toIntOrNull() ?: 0
                        if (packageId == null) viewModel.addPackage(name, p, s, st, pr, cityCenter)
                        else viewModel.updatePackage(packageId, name, p, s, st, pr, cityCenter)
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.padding(16.dp).fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("SAČUVAJ", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

// --- 5. CART SCREEN ---
@Composable
fun CartScreen(viewModel: SportZonaViewModel) {
    val items = viewModel.cartItems
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ShoppingCart, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                Text("Vaša korpa je prazna", color = Color.Gray)
            }
        }
        return
    }

    val basePrice = viewModel.getCartBasePrice()
    val waitingDays = viewModel.getCartWaitingDays()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 140.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Pregledajte pakete koje ste rezervisali pre potvrde i konačnog plaćanja.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(24.dp))
            }
            items(items) { item ->
                Card(elevation = CardDefaults.cardElevation(0.dp), border = BorderStroke(1.dp, Color.LightGray), shape = RoundedCornerShape(0.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(item.sportPackage.name, fontWeight = FontWeight.ExtraBold)
                            Text("${item.cartItem.quantity} mesta x ${String.format("%.2f", item.sportPackage.price)} RSD", fontSize = 12.sp, color = Color.Gray)
                            Text("Ukupno: ${String.format("%.2f RSD", item.sportPackage.price * item.cartItem.quantity)}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { viewModel.removeCartItem(item.cartItem.id) }) { Icon(Icons.Default.Delete, null, tint = Color.Gray) }
                    }
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.LightGray)
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Privremena cena:", color = Color.Gray)
                    Text(String.format("%.2f RSD", basePrice), fontWeight = FontWeight.Bold)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Dani čekanja:", color = Color.Gray)
                    Text("$waitingDays dana", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Surface(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(), border = BorderStroke(1.dp, Color.LightGray), color = Color.White) {
            Button(
                onClick = { viewModel.navigateTo(Screen.Checkout) }, 
                modifier = Modifier.padding(16.dp).fillMaxWidth().height(48.dp), 
                shape = RoundedCornerShape(0.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("NASTAVI", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

// --- 6. CHECKOUT SCREEN ---
@Composable
fun CheckoutScreen(viewModel: SportZonaViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Odaberite način na koji želite da vam se izda članska kartica i potvrdite vašu rezervaciju.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(24.dp))
            }
            
            item {
                DeliveryOption(viewModel, DeliveryMethod.STANDARD_RECEPTION, "Standardno preuzimanje", "Na recepciji centra - besplatno")
                DeliveryOption(viewModel, DeliveryMethod.DIGITAL, "Digitalno izdavanje (SMS/Email)", "Popust 10% na cenu")
                DeliveryOption(viewModel, DeliveryMethod.PREMIUM_DELIVERY, "Premium dostava na adresu", "Doplata 20% (Welcome paket)")
                
                Spacer(Modifier.height(24.dp))
                Card(elevation = CardDefaults.cardElevation(0.dp), border = BorderStroke(1.dp, Color.LightGray), shape = RoundedCornerShape(0.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ukupna cena:", fontWeight = FontWeight.Bold)
                            Text(String.format("%.2f RSD", viewModel.getCartFinalPrice()), fontWeight = FontWeight.ExtraBold, color = Color.Black, fontSize = 20.sp)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Vreme do termina:", fontWeight = FontWeight.Bold)
                            Text("${viewModel.getCartWaitingDays()} dana", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Surface(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(), border = BorderStroke(1.dp, Color.LightGray), color = Color.White) {
            Button(
                onClick = { viewModel.confirmOrder() }, 
                modifier = Modifier.padding(16.dp).fillMaxWidth().height(48.dp), 
                shape = RoundedCornerShape(0.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("POTVRDI REZERVACIJU", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun DeliveryOption(viewModel: SportZonaViewModel, method: DeliveryMethod, label: String, sub: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { viewModel.selectedDeliveryMethod = method },
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, if (viewModel.selectedDeliveryMethod == method) Color.Black else Color.Gray.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = viewModel.selectedDeliveryMethod == method, onClick = { viewModel.selectedDeliveryMethod = method })
            Spacer(Modifier.width(8.dp))
            Column {
                Text(label, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Text(sub, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
