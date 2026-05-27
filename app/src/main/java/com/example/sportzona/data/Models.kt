package com.example.sportzona.data

data class UserProfile(
    val id: Int = 1,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val street: String = "",
    val city: String = ""
) {
    val isValid: Boolean
        get() = firstName.isNotBlank() && 
                lastName.isNotBlank() && 
                email.isNotBlank() && 
                street.isNotBlank() && 
                city.isNotBlank()
}

data class SportPackage(
    val id: Long = 0L,
    val name: String,
    val price: Double,
    val availableSlots: Int,
    val standardDays: Int,
    val premiumDays: Int,
    val cityAndCenter: String
)

data class CartItem(
    val id: Long = 0L,
    val packageId: Long,
    val quantity: Int
)

data class CartItemWithPackage(
    val cartItem: CartItem,
    val sportPackage: SportPackage
)
