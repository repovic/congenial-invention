package com.example.sportzona.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportzona.core.ActivityPackage
import com.example.sportzona.core.DetailedCartEntry
import com.example.sportzona.core.UserAccount
import com.example.sportzona.repository.LocalRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class NavigationTarget {
    object UserSetup : NavigationTarget()
    object Catalog : NavigationTarget()
    data class Details(val activityId: Long) : NavigationTarget()
    data class Management(val activityId: Long?) : NavigationTarget()
    object CartView : NavigationTarget()
    object FinalizeOrder : NavigationTarget()
}

enum class MembershipDelivery {
    RECEPTION,
    VIRTUAL,
    PREMIUM_COURIER
}

class AppStateController(application: Application) : AndroidViewModel(application) {
    private val registry = LocalRegistry(application)

    var activeScreen by mutableStateOf<NavigationTarget>(NavigationTarget.UserSetup)
        private set

    var currentUser by mutableStateOf(UserAccount())
        private set

    var availableActivities by mutableStateOf<List<ActivityPackage>>(emptyList())
        private set

    var currentCartEntries by mutableStateOf<List<DetailedCartEntry>>(emptyList())
        private set

    var focusedActivity by mutableStateOf<ActivityPackage?>(null)
        private set

    var customDeliveryAddress = mutableStateOf(false)
    var altStreet = mutableStateOf("")
    var altCity = mutableStateOf("")

    var deliverySelection by mutableStateOf(MembershipDelivery.RECEPTION)

    var isOrderCompleted by mutableStateOf(false)
    var summaryData by mutableStateOf<OrderSummary?>(null)

    data class OrderSummary(
        val destination: String,
        val estimatedDays: Int,
        val totalCost: Double
    )

    init {
        registry.wipeDatabase()
        syncData()
    }

    fun syncData() {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) { registry.fetchUser() }
            val list = withContext(Dispatchers.IO) { registry.listAllOffers() }
            val cart = withContext(Dispatchers.IO) { registry.fetchCart() }

            if (user != null) {
                currentUser = user
                if (user.isComplete && activeScreen is NavigationTarget.UserSetup) {
                    activeScreen = NavigationTarget.Catalog
                }
            }
            availableActivities = list
            currentCartEntries = cart
        }
    }

    fun jumpTo(target: NavigationTarget) {
        activeScreen = target
        when (target) {
            is NavigationTarget.Details -> fetchFocusedActivity(target.activityId)
            is NavigationTarget.Management -> {
                if (target.activityId != null) fetchFocusedActivity(target.activityId)
                else focusedActivity = null
            }
            else -> {}
        }
    }

    private fun fetchFocusedActivity(id: Long) {
        viewModelScope.launch {
            focusedActivity = withContext(Dispatchers.IO) { registry.findOffer(id) }
        }
    }

    fun updateUser(fName: String, lName: String, mail: String, street: String, city: String) {
        viewModelScope.launch {
            val updated = UserAccount(name = fName, surname = lName, contactEmail = mail, locationAddress = street, residenceCity = city)
            withContext(Dispatchers.IO) { registry.syncUser(updated) }
            currentUser = updated
            jumpTo(NavigationTarget.Catalog)
        }
    }

    fun createActivity(title: String, cost: Double, cap: Int, stdD: Int, prmD: Int, loc: String) {
        viewModelScope.launch {
            val p = ActivityPackage(title = title, cost = cost, capacity = cap, waitDaysStandard = stdD, waitDaysPremium = prmD, facilityInfo = loc)
            withContext(Dispatchers.IO) { registry.persistOffer(p) }
            syncData()
            jumpTo(NavigationTarget.Catalog)
        }
    }

    fun updateActivity(id: Long, title: String, cost: Double, cap: Int, stdD: Int, prmD: Int, loc: String) {
        viewModelScope.launch {
            val p = ActivityPackage(id = id, title = title, cost = cost, capacity = cap, waitDaysStandard = stdD, waitDaysPremium = prmD, facilityInfo = loc)
            withContext(Dispatchers.IO) { registry.modifyOffer(p) }
            syncData()
            jumpTo(NavigationTarget.Catalog)
        }
    }

    fun expandCapacity(id: Long, added: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                registry.findOffer(id)?.let {
                    registry.modifyOffer(it.copy(capacity = it.capacity + added))
                }
            }
            syncData()
            if (activeScreen is NavigationTarget.Details && (activeScreen as NavigationTarget.Details).activityId == id) {
                fetchFocusedActivity(id)
            }
        }
    }

    fun reserveActivity(id: Long, qty: Int, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { registry.pushToCart(id, qty) }
            if (ok) currentCartEntries = withContext(Dispatchers.IO) { registry.fetchCart() }
            callback(ok)
        }
    }

    fun modifyCartQuantity(rid: Long, next: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { registry.adjustCartQty(rid, next) }
            currentCartEntries = withContext(Dispatchers.IO) { registry.fetchCart() }
        }
    }

    fun deleteFromCart(rid: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { registry.dropFromCart(rid) }
            currentCartEntries = withContext(Dispatchers.IO) { registry.fetchCart() }
        }
    }

    fun calculateBaseTotal(): Double = currentCartEntries.sumOf { it.entry.amount * it.activity.cost }

    fun calculateFinalTotal(): Double {
        val base = calculateBaseTotal()
        return when (deliverySelection) {
            MembershipDelivery.RECEPTION -> base
            MembershipDelivery.VIRTUAL -> base * 0.9
            MembershipDelivery.PREMIUM_COURIER -> base * 1.2
        }
    }

    fun calculateTotalWait(): Int {
        if (currentCartEntries.isEmpty()) return 0
        val isPremium = deliverySelection == MembershipDelivery.PREMIUM_COURIER
        val maxDays = currentCartEntries.maxOf { if (isPremium) it.activity.waitDaysPremium else it.activity.waitDaysStandard }
        val userCity = currentUser.residenceCity.trim().lowercase()
        val crossCity = currentCartEntries.any { userCity.isNotEmpty() && !it.activity.facilityInfo.lowercase().contains(userCity) }
        return maxDays + if (crossCity) 1 else 0
    }

    fun executeOrder() {
        val total = calculateFinalTotal()
        val days = calculateTotalWait()
        val loc = when (deliverySelection) {
            MembershipDelivery.RECEPTION -> "Prijem na šalteru centra"
            MembershipDelivery.VIRTUAL -> "Elektronska isporuka (${currentUser.contactEmail})"
            MembershipDelivery.PREMIUM_COURIER -> if (customDeliveryAddress.value && altStreet.value.isNotBlank()) "${altStreet.value}, ${altCity.value}" else "${currentUser.locationAddress}, ${currentUser.residenceCity}"
        }
        summaryData = OrderSummary(loc, days, total)
        isOrderCompleted = true
    }

    fun closeOrderAndReset() {
        isOrderCompleted = false
        summaryData = null
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                currentCartEntries.forEach {
                    registry.findOffer(it.entry.activityId)?.let { p ->
                        registry.modifyOffer(p.copy(capacity = (p.capacity - it.entry.amount).coerceAtLeast(0)))
                    }
                }
                registry.emptyCart()
            }
            customDeliveryAddress.value = false
            altStreet.value = ""
            altCity.value = ""
            deliverySelection = MembershipDelivery.RECEPTION
            syncData()
            activeScreen = NavigationTarget.Catalog
        }
    }
}
