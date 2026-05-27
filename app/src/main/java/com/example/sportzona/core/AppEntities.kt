package com.example.sportzona.core

data class UserAccount(
    val id: Int = 1,
    val name: String = "",
    val surname: String = "",
    val contactEmail: String = "",
    val locationAddress: String = "",
    val residenceCity: String = ""
) {
    val isComplete: Boolean
        get() = name.isNotBlank() && 
                surname.isNotBlank() && 
                contactEmail.isNotBlank() && 
                locationAddress.isNotBlank() && 
                residenceCity.isNotBlank()
}

data class ActivityPackage(
    val id: Long = 0L,
    val title: String,
    val cost: Double,
    val capacity: Int,
    val waitDaysStandard: Int,
    val waitDaysPremium: Int,
    val facilityInfo: String
)

data class CartEntry(
    val id: Long = 0L,
    val activityId: Long,
    val amount: Int
)

data class DetailedCartEntry(
    val entry: CartEntry,
    val activity: ActivityPackage
)
