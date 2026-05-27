package com.example.sportzona.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sportzona.ui.components.ScreenHeader
import com.example.sportzona.ui.components.InputWidget
import com.example.sportzona.viewmodel.AppStateController

@Composable
fun UserSetupView(controller: AppStateController) {
    val profile = controller.currentUser
    var nameField by remember { mutableStateOf(profile.name) }
    var surnameField by remember { mutableStateOf(profile.surname) }
    var mailField by remember { mutableStateOf(profile.contactEmail) }
    var addrField by remember { mutableStateOf(profile.locationAddress) }
    var cityField by remember { mutableStateOf(profile.residenceCity) }

    val ready = nameField.isNotBlank() && surnameField.isNotBlank() && mailField.isNotBlank() && addrField.isNotBlank() && cityField.isNotBlank()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ScreenHeader(
                    icon = Icons.Default.AccountCircle,
                    title = "Dobrodošli u SportZona",
                    subtitle = "Unesite svoje podatke za izdavanje članske karte."
                )
            }

            item { InputWidget(nameField, { nameField = it }, "Ime", Icons.Default.Person) }
            item { InputWidget(surnameField, { surnameField = it }, "Prezime", Icons.Default.Person) }
            item { InputWidget(mailField, { mailField = it }, "E-mail", Icons.Default.Email, KeyboardType.Email) }
            item { InputWidget(addrField, { addrField = it }, "Ulica i broj", Icons.Default.Home) }
            item { InputWidget(cityField, { cityField = it }, "Grad", Icons.Default.LocationOn) }
        }

        Button(
            onClick = { if (ready) controller.updateUser(nameField, surnameField, mailField, addrField, cityField) },
            enabled = ready,
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp).height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("SAČUVAJ I NASTAVI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}
