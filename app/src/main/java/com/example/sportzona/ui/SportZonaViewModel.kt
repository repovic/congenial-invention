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

    var currentScreen by mutableStateOf<Screen>(Screen.UserProfile)
        private set

    var userProfile by mutableStateOf(UserProfile())
        private set

    var packagesList by mutableStateOf<List<SportPackage>>(emptyList())
        private set

    var cartItems by mutableStateOf<List<CartItemWithPackage>>(emptyList())
        private set

    var selectedPackage by mutableStateOf<SportPackage?>(null)
        private set

    var differentAddressEnabled by mutableStateOf(false)
    var alternateStreet by mutableStateOf("")
    var alternateCity by mutableStateOf("")

    var selectedDeliveryMethod by mutableStateOf(DeliveryMethod.STANDARD_RECEPTION)

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
            if (currentScreen is Screen.PackageDetail && (currentScreen as Screen.PackageDetail).packageId == packageId) {
                loadPackageDetail(packageId)
            } else if (currentScreen is Screen.AddEditPackage && (currentScreen as Screen.AddEditPackage).packageId == packageId) {
                loadPackageDetail(packageId)
            }
        }
    }

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

    fun getCartBasePrice(): Double {
        return cartItems.sumOf { it.cartItem.quantity * it.sportPackage.price }
    }

    fun getCartFinalPrice(): Double {
        val base = getCartBasePrice()
        return when (selectedDeliveryMethod) {
            DeliveryMethod.STANDARD_RECEPTION -> base
            DeliveryMethod.DIGITAL -> base * 0.9
            DeliveryMethod.PREMIUM_DELIVERY -> base * 1.2
        }
    }

    fun getCartWaitingDays(): Int {
        if (cartItems.isEmpty()) return 0

        val isPremiumSchedule = selectedDeliveryMethod == DeliveryMethod.PREMIUM_DELIVERY

        val maxBaseDays = cartItems.maxOfOrNull {
            if (isPremiumSchedule) it.sportPackage.premiumDays else it.sportPackage.standardDays
        } ?: 0

        val userCity = userProfile.city.trim()
        val anyOutsideCity = cartItems.any { item ->
            val pkgCityAndCenter = item.sportPackage.cityAndCenter.lowercase()
            userCity.isNotEmpty() && !pkgCityAndCenter.contains(userCity.lowercase())
        }

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
                for (item in cartItems) {
                    val pkg = dbHelper.getPackageById(item.cartItem.packageId)
                    if (pkg != null) {
                        val newSlots = (pkg.availableSlots - item.cartItem.quantity).coerceAtLeast(0)
                        dbHelper.updatePackage(pkg.copy(availableSlots = newSlots))
                    }
                }
                dbHelper.clearCart()
            }
            differentAddressEnabled = false
            alternateStreet = ""
            alternateCity = ""
            selectedDeliveryMethod = DeliveryMethod.STANDARD_RECEPTION
            
            loadData()
            currentScreen = Screen.PackageList
        }
    }
}
