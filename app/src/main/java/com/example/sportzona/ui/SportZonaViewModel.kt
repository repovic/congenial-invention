package com.example.sportzona.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportzona.data.DatabaseHelper
import com.example.sportzona.data.SportPackage
import com.example.sportzona.data.CartItemWithPackage
import com.example.sportzona.data.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class Screen {
    object UserProfile : Screen()
    object PackageList : Screen()
    data class PackageDetail(val packageId: Long) : Screen()
    data class AddEditPackage(val packageId: Long?) : Screen()
    object Cart : Screen()
    object Checkout : Screen()
}

enum class DeliveryMethod {
    STANDARD_RECEPTION,
    DIGITAL,
    PREMIUM_DELIVERY
}

class SportZonaViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)

    // Navigation State
    var currentScreen by mutableStateOf<Screen>(Screen.UserProfile)
        private set

    // User Profile State
    var userProfile by mutableStateOf(UserProfile())
        private set

    // Packages State
    var packagesList by mutableStateOf<List<SportPackage>>(emptyList())
        private set

    // Cart State
    var cartItems by mutableStateOf<List<CartItemWithPackage>>(emptyList())
        private set

    // Selected Package (for detail or edit)
    var selectedPackage by mutableStateOf<SportPackage?>(null)
        private set

    // Alternate Delivery State
    var differentAddressEnabled by mutableStateOf(false)
    var alternateStreet by mutableStateOf("")
    var alternateCity by mutableStateOf("")

    // Selected delivery method
    var selectedDeliveryMethod by mutableStateOf(DeliveryMethod.STANDARD_RECEPTION)

    // Dialog state for order confirmation
    var showOrderConfirmationDialog by mutableStateOf(false)
    var orderDialogInfo by mutableStateOf<OrderDialogInfo?>(null)

    data class OrderDialogInfo(
        val address: String,
        val waitingDays: Int,
        val finalPrice: Double
    )

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) { dbHelper.getUserProfile() }
            val list = withContext(Dispatchers.IO) { dbHelper.getAllPackages() }
            val cart = withContext(Dispatchers.IO) { dbHelper.getCartItems() }

            if (user != null) {
                userProfile = user
                // If user already saved their profile, go straight to packages list
                if (user.isValid && currentScreen is Screen.UserProfile) {
                    currentScreen = Screen.PackageList
                }
            }
            packagesList = list
            cartItems = cart
        }
    }

    fun navigateTo(screen: Screen) {
        currentScreen = screen
        if (screen is Screen.PackageDetail) {
            loadPackageDetail(screen.packageId)
        } else if (screen is Screen.AddEditPackage) {
            if (screen.packageId != null) {
                loadPackageDetail(screen.packageId)
            } else {
                selectedPackage = null
            }
        }
    }

    private fun loadPackageDetail(packageId: Long) {
        viewModelScope.launch {
            selectedPackage = withContext(Dispatchers.IO) {
                dbHelper.getPackageById(packageId)
            }
        }
    }

    // --- USER PROFILE ACTIONS ---
    fun saveUserProfile(firstName: String, lastName: String, email: String, street: String, city: String) {
        viewModelScope.launch {
            val updatedUser = UserProfile(
                firstName = firstName,
                lastName = lastName,
                email = email,
                street = street,
                city = city
            )
            withContext(Dispatchers.IO) {
                dbHelper.saveUserProfile(updatedUser)
            }
            userProfile = updatedUser
            navigateTo(Screen.PackageList)
        }
    }

    // --- SPORT PACKAGE ACTIONS ---
    fun addPackage(
        name: String,
        price: Double,
        availableSlots: Int,
        standardDays: Int,
        premiumDays: Int,
        cityAndCenter: String
    ) {
        viewModelScope.launch {
            val pkg = SportPackage(
                name = name,
                price = price,
                availableSlots = availableSlots,
                standardDays = standardDays,
                premiumDays = premiumDays,
                cityAndCenter = cityAndCenter
            )
            withContext(Dispatchers.IO) {
                dbHelper.insertPackage(pkg)
            }
            loadData()
            navigateTo(Screen.PackageList)
        }
    }

    fun updatePackage(
        id: Long,
        name: String,
        price: Double,
        availableSlots: Int,
        standardDays: Int,
        premiumDays: Int,
        cityAndCenter: String
    ) {
        viewModelScope.launch {
            val pkg = SportPackage(
                id = id,
                name = name,
                price = price,
                availableSlots = availableSlots,
                standardDays = standardDays,
                premiumDays = premiumDays,
                cityAndCenter = cityAndCenter
            )
            withContext(Dispatchers.IO) {
                dbHelper.updatePackage(pkg)
            }
            loadData()
            navigateTo(Screen.PackageList)
        }
    }

    fun addExtraSlotsToPackage(packageId: Long, extraSlots: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val pkg = dbHelper.getPackageById(packageId)
                if (pkg != null) {
                    val updatedPkg = pkg.copy(availableSlots = pkg.availableSlots + extraSlots)
                    dbHelper.updatePackage(updatedPkg)
                }
            }
            loadData()
            // Reload selected package if in detail screen
            if (currentScreen is Screen.PackageDetail && (currentScreen as Screen.PackageDetail).packageId == packageId) {
                loadPackageDetail(packageId)
            }
        }
    }

    // --- CART ACTIONS ---
    fun addPackageToCart(packageId: Long, quantity: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                dbHelper.addToCart(packageId, quantity)
            }
            if (success) {
                val cart = withContext(Dispatchers.IO) { dbHelper.getCartItems() }
                cartItems = cart
            }
            onResult(success)
        }
    }

    fun updateCartQuantity(cartItemId: Long, newQuantity: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.updateCartItemQuantity(cartItemId, newQuantity)
            }
            val cart = withContext(Dispatchers.IO) { dbHelper.getCartItems() }
            cartItems = cart
        }
    }

    fun removeCartItem(cartItemId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.removeFromCart(cartItemId)
            }
            val cart = withContext(Dispatchers.IO) { dbHelper.getCartItems() }
            cartItems = cart
        }
    }

    // --- CHECKOUT & CALCULATIONS ---

    // Total base price
    fun getCartBasePrice(): Double {
        return cartItems.sumOf { it.cartItem.quantity * it.sportPackage.price }
    }

    // Total price after delivery adjustments
    fun getCartFinalPrice(): Double {
        val base = getCartBasePrice()
        return when (selectedDeliveryMethod) {
            DeliveryMethod.STANDARD_RECEPTION -> base
            DeliveryMethod.DIGITAL -> base * 0.9 // -10% discount
            DeliveryMethod.PREMIUM_DELIVERY -> base * 1.2 // +20% fee
        }
    }

    // Total waiting days based on standard or premium schedule
    fun getCartWaitingDays(): Int {
        if (cartItems.isEmpty()) return 0

        // Use premium schedule if delivery is premium, otherwise standard
        val isPremiumSchedule = selectedDeliveryMethod == DeliveryMethod.PREMIUM_DELIVERY

        // Find max waiting days among all reserved packages
        val maxBaseDays = cartItems.maxOfOrNull {
            if (isPremiumSchedule) it.sportPackage.premiumDays else it.sportPackage.standardDays
        } ?: 0

        // Check if any package is located in a city different from the user's city
        val userCity = userProfile.city.trim()
        val anyOutsideCity = cartItems.any { item ->
            val pkgCityAndCenter = item.sportPackage.cityAndCenter.lowercase()
            // If the city field of the user is empty, we don't apply, otherwise verify if the package is in user's city
            userCity.isNotEmpty() && !pkgCityAndCenter.contains(userCity.lowercase())
        }

        // Increment by 1 if there is a city mismatch
        return maxBaseDays + if (anyOutsideCity) 1 else 0
    }

    fun confirmOrder() {
        val finalPrice = getCartFinalPrice()
        val waitingDays = getCartWaitingDays()

        val addressText = when (selectedDeliveryMethod) {
            DeliveryMethod.STANDARD_RECEPTION -> "Preuzimanje na recepciji sportskog centra"
            DeliveryMethod.DIGITAL -> "Digitalno izdavanje (SMS na broj telefona / e-mail na adresu: ${userProfile.email})"
            DeliveryMethod.PREMIUM_DELIVERY -> {
                if (differentAddressEnabled && alternateStreet.isNotBlank() && alternateCity.isNotBlank()) {
                    "$alternateStreet, $alternateCity"
                } else {
                    "${userProfile.street}, ${userProfile.city}"
                }
            }
        }

        orderDialogInfo = OrderDialogInfo(
            address = addressText,
            waitingDays = waitingDays,
            finalPrice = finalPrice
        )
        showOrderConfirmationDialog = true
    }

    fun dismissOrderDialogAndClearCart() {
        showOrderConfirmationDialog = false
        orderDialogInfo = null
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Deduct reserved spots from packages available slots in the database!
                // Wait! To make the app feel incredibly alive and functional, let's update package slots upon confirmation!
                for (item in cartItems) {
                    val pkg = dbHelper.getPackageById(item.cartItem.packageId)
                    if (pkg != null) {
                        val newSlots = (pkg.availableSlots - item.cartItem.quantity).coerceAtLeast(0)
                        dbHelper.updatePackage(pkg.copy(availableSlots = newSlots))
                    }
                }
                dbHelper.clearCart()
            }
            // Reset states
            differentAddressEnabled = false
            alternateStreet = ""
            alternateCity = ""
            selectedDeliveryMethod = DeliveryMethod.STANDARD_RECEPTION
            
            loadData()
            // Navigate back to packages list
            currentScreen = Screen.PackageList
        }
    }
}
