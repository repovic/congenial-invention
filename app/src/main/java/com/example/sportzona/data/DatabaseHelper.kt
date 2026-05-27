package com.example.sportzona.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "sportzona.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_USER = "users"
        private const val KEY_USER_ID = "id"
        private const val KEY_USER_FIRST_NAME = "first_name"
        private const val KEY_USER_LAST_NAME = "last_name"
        private const val KEY_USER_EMAIL = "email"
        private const val KEY_USER_STREET = "street"
        private const val KEY_USER_CITY = "city"

        private const val TABLE_PACKAGES = "packages"
        private const val KEY_PKG_ID = "id"
        private const val KEY_PKG_NAME = "name"
        private const val KEY_PKG_PRICE = "price"
        private const val KEY_PKG_SLOTS = "available_slots"
        private const val KEY_PKG_STANDARD_DAYS = "standard_days"
        private const val KEY_PKG_PREMIUM_DAYS = "premium_days"
        private const val KEY_PKG_CITY_CENTER = "city_and_center"

        private const val TABLE_CART = "cart"
        private const val KEY_CART_ID = "id"
        private const val KEY_CART_PKG_ID = "package_id"
        private const val KEY_CART_QUANTITY = "quantity"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUserTable = ("CREATE TABLE $TABLE_USER (" +
                "$KEY_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$KEY_USER_FIRST_NAME TEXT," +
                "$KEY_USER_LAST_NAME TEXT," +
                "$KEY_USER_EMAIL TEXT," +
                "$KEY_USER_STREET TEXT," +
                "$KEY_USER_CITY TEXT)")
        db.execSQL(createUserTable)

        val createPackagesTable = ("CREATE TABLE $TABLE_PACKAGES (" +
                "$KEY_PKG_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$KEY_PKG_NAME TEXT," +
                "$KEY_PKG_PRICE REAL," +
                "$KEY_PKG_SLOTS INTEGER," +
                "$KEY_PKG_STANDARD_DAYS INTEGER," +
                "$KEY_PKG_PREMIUM_DAYS INTEGER," +
                "$KEY_PKG_CITY_CENTER TEXT)")
        db.execSQL(createPackagesTable)

        val createCartTable = ("CREATE TABLE $TABLE_CART (" +
                "$KEY_CART_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$KEY_CART_PKG_ID INTEGER," +
                "$KEY_CART_QUANTITY INTEGER," +
                "FOREIGN KEY($KEY_CART_PKG_ID) REFERENCES $TABLE_PACKAGES($KEY_PKG_ID) ON DELETE CASCADE)")
        db.execSQL(createCartTable)

        populateInitialPackages(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CART")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PACKAGES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    private fun populateInitialPackages(db: SQLiteDatabase) {
        val initialPackages = listOf(
            ContentValues().apply {
                put(KEY_PKG_NAME, "Teretana Standard")
                put(KEY_PKG_PRICE, 3500.00)
                put(KEY_PKG_SLOTS, 15)
                put(KEY_PKG_STANDARD_DAYS, 3)
                put(KEY_PKG_PREMIUM_DAYS, 1)
                put(KEY_PKG_CITY_CENTER, "Beograd, Gym Fit")
            },
            ContentValues().apply {
                put(KEY_PKG_NAME, "Bazen Relax")
                put(KEY_PKG_PRICE, 4200.00)
                put(KEY_PKG_SLOTS, 8)
                put(KEY_PKG_STANDARD_DAYS, 5)
                put(KEY_PKG_PREMIUM_DAYS, 2)
                put(KEY_PKG_CITY_CENTER, "Novi Sad, AquaLife")
            },
            ContentValues().apply {
                put(KEY_PKG_NAME, "Tenis Pro")
                put(KEY_PKG_PRICE, 6000.00)
                put(KEY_PKG_SLOTS, 4)
                put(KEY_PKG_STANDARD_DAYS, 7)
                put(KEY_PKG_PREMIUM_DAYS, 3)
                put(KEY_PKG_CITY_CENTER, "Niš, Set & Match")
            },
            ContentValues().apply {
                put(KEY_PKG_NAME, "Zumba i Pilates")
                put(KEY_PKG_PRICE, 3000.00)
                put(KEY_PKG_SLOTS, 20)
                put(KEY_PKG_STANDARD_DAYS, 2)
                put(KEY_PKG_PREMIUM_DAYS, 1)
                put(KEY_PKG_CITY_CENTER, "Kragujevac, Body & Mind")
            },
            ContentValues().apply {
                put(KEY_PKG_NAME, "CrossFit Arena")
                put(KEY_PKG_PRICE, 5000.00)
                put(KEY_PKG_SLOTS, 10)
                put(KEY_PKG_STANDARD_DAYS, 4)
                put(KEY_PKG_PREMIUM_DAYS, 2)
                put(KEY_PKG_CITY_CENTER, "Beograd, Spartan")
            }
        )

        for (cv in initialPackages) {
            db.insert(TABLE_PACKAGES, null, cv)
        }
    }

    fun getUserProfile(): UserProfile? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_USER, null, null, null, null, null, null)
        var user: UserProfile? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_ID))
            val firstName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_FIRST_NAME))
            val lastName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_LAST_NAME))
            val email = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_EMAIL))
            val street = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_STREET))
            val city = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_CITY))
            user = UserProfile(id, firstName, lastName, email, street, city)
        }
        cursor.close()
        return user
    }

    fun saveUserProfile(user: UserProfile) {
        val db = this.writableDatabase
        val cv = ContentValues().apply {
            put(KEY_USER_FIRST_NAME, user.firstName)
            put(KEY_USER_LAST_NAME, user.lastName)
            put(KEY_USER_EMAIL, user.email)
            put(KEY_USER_STREET, user.street)
            put(KEY_USER_CITY, user.city)
        }

        val existingUser = getUserProfile()
        if (existingUser == null) {
            db.insert(TABLE_USER, null, cv)
        } else {
            db.update(TABLE_USER, cv, "$KEY_USER_ID = ?", arrayOf(existingUser.id.toString()))
        }
    }

    fun getAllPackages(): List<SportPackage> {
        val db = this.readableDatabase
        val packagesList = mutableListOf<SportPackage>()
        val cursor = db.query(TABLE_PACKAGES, null, null, null, null, null, "$KEY_PKG_NAME ASC")
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_PKG_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PKG_NAME))
                val price = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_PKG_PRICE))
                val slots = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PKG_SLOTS))
                val standardDays = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PKG_STANDARD_DAYS))
                val premiumDays = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PKG_PREMIUM_DAYS))
                val cityCenter = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PKG_CITY_CENTER))
                packagesList.add(SportPackage(id, name, price, slots, standardDays, premiumDays, cityCenter))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return packagesList
    }

    fun getPackageById(id: Long): SportPackage? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_PACKAGES, null, "$KEY_PKG_ID = ?", arrayOf(id.toString()), null, null, null)
        var sportPackage: SportPackage? = null
        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PKG_NAME))
            val price = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_PKG_PRICE))
            val slots = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PKG_SLOTS))
            val standardDays = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PKG_STANDARD_DAYS))
            val premiumDays = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PKG_PREMIUM_DAYS))
            val cityCenter = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PKG_CITY_CENTER))
            sportPackage = SportPackage(id, name, price, slots, standardDays, premiumDays, cityCenter)
        }
        cursor.close()
        return sportPackage
    }

    fun insertPackage(pkg: SportPackage): Long {
        val db = this.writableDatabase
        val cv = ContentValues().apply {
            put(KEY_PKG_NAME, pkg.name)
            put(KEY_PKG_PRICE, pkg.price)
            put(KEY_PKG_SLOTS, pkg.availableSlots)
            put(KEY_PKG_STANDARD_DAYS, pkg.standardDays)
            put(KEY_PKG_PREMIUM_DAYS, pkg.premiumDays)
            put(KEY_PKG_CITY_CENTER, pkg.cityAndCenter)
        }
        return db.insert(TABLE_PACKAGES, null, cv)
    }

    fun updatePackage(pkg: SportPackage): Int {
        val db = this.writableDatabase
        val cv = ContentValues().apply {
            put(KEY_PKG_NAME, pkg.name)
            put(KEY_PKG_PRICE, pkg.price)
            put(KEY_PKG_SLOTS, pkg.availableSlots)
            put(KEY_PKG_STANDARD_DAYS, pkg.standardDays)
            put(KEY_PKG_PREMIUM_DAYS, pkg.premiumDays)
            put(KEY_PKG_CITY_CENTER, pkg.cityAndCenter)
        }
        return db.update(TABLE_PACKAGES, cv, "$KEY_PKG_ID = ?", arrayOf(pkg.id.toString()))
    }

    fun getCartItems(): List<CartItemWithPackage> {
        val db = this.readableDatabase
        val cartList = mutableListOf<CartItemWithPackage>()
        val query = "SELECT c.$KEY_CART_ID AS cart_id, c.$KEY_CART_PKG_ID AS cart_pkg_id, c.$KEY_CART_QUANTITY AS cart_qty, " +
                "p.$KEY_PKG_NAME, p.$KEY_PKG_PRICE, p.$KEY_PKG_SLOTS, p.$KEY_PKG_STANDARD_DAYS, p.$KEY_PKG_PREMIUM_DAYS, p.$KEY_PKG_CITY_CENTER " +
                "FROM $TABLE_CART c INNER JOIN $TABLE_PACKAGES p ON c.$KEY_CART_PKG_ID = p.$KEY_PKG_ID"

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val cartId = cursor.getLong(cursor.getColumnIndexOrThrow("cart_id"))
                val pkgId = cursor.getLong(cursor.getColumnIndexOrThrow("cart_pkg_id"))
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow("cart_qty"))

                val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PKG_NAME))
                val price = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_PKG_PRICE))
                val slots = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PKG_SLOTS))
                val standardDays = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PKG_STANDARD_DAYS))
                val premiumDays = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PKG_PREMIUM_DAYS))
                val cityCenter = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PKG_CITY_CENTER))

                val pkg = SportPackage(pkgId, name, price, slots, standardDays, premiumDays, cityCenter)
                val item = CartItem(cartId, pkgId, quantity)

                cartList.add(CartItemWithPackage(item, pkg))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return cartList
    }

    fun addToCart(packageId: Long, quantity: Int): Boolean {
        val db = this.writableDatabase
        val pkg = getPackageById(packageId) ?: return false

        val cursor = db.query(TABLE_CART, null, "$KEY_CART_PKG_ID = ?", arrayOf(packageId.toString()), null, null, null)
        val exists = cursor.moveToFirst()
        var success = false

        if (exists) {
            val cartId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_CART_ID))
            val currentQty = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CART_QUANTITY))
            val newQty = currentQty + quantity
            if (newQty <= pkg.availableSlots) {
                val cv = ContentValues().apply {
                    put(KEY_CART_QUANTITY, newQty)
                }
                db.update(TABLE_CART, cv, "$KEY_CART_ID = ?", arrayOf(cartId.toString()))
                success = true
            }
        } else {
            if (quantity <= pkg.availableSlots) {
                val cv = ContentValues().apply {
                    put(KEY_CART_PKG_ID, packageId)
                    put(KEY_CART_QUANTITY, quantity)
                }
                db.insert(TABLE_CART, null, cv)
                success = true
            }
        }
        cursor.close()
        return success
    }

    fun updateCartItemQuantity(cartItemId: Long, newQty: Int): Boolean {
        val db = this.writableDatabase
        if (newQty <= 0) {
            db.delete(TABLE_CART, "$KEY_CART_ID = ?", arrayOf(cartItemId.toString()))
            return true
        }
        
        val query = "SELECT c.$KEY_CART_PKG_ID, p.$KEY_PKG_SLOTS FROM $TABLE_CART c " +
                "INNER JOIN $TABLE_PACKAGES p ON c.$KEY_CART_PKG_ID = p.$KEY_PKG_ID WHERE c.$KEY_CART_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(cartItemId.toString()))
        var allowed = false
        if (cursor.moveToFirst()) {
            val maxSlots = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PKG_SLOTS))
            if (newQty <= maxSlots) {
                allowed = true
            }
        }
        cursor.close()

        if (allowed) {
            val cv = ContentValues().apply {
                put(KEY_CART_QUANTITY, newQty)
            }
            db.update(TABLE_CART, cv, "$KEY_CART_ID = ?", arrayOf(cartItemId.toString()))
            return true
        }
        return false
    }

    fun removeFromCart(cartItemId: Long) {
        val db = this.writableDatabase
        db.delete(TABLE_CART, "$KEY_CART_ID = ?", arrayOf(cartItemId.toString()))
    }

    fun clearCart() {
        val db = this.writableDatabase
        db.delete(TABLE_CART, null, null)
    }
}
