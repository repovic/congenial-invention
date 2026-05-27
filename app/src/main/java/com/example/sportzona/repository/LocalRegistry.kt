package com.example.sportzona.repository

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.sportzona.core.ActivityPackage
import com.example.sportzona.core.CartEntry
import com.example.sportzona.core.DetailedCartEntry
import com.example.sportzona.core.UserAccount

class LocalRegistry(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "sportzona_registry.db"
        private const val DB_VERSION = 1

        private const val TBL_CLIENT = "client_info"
        private const val COL_CLIENT_ID = "cid"
        private const val COL_CLIENT_NAME = "name"
        private const val COL_CLIENT_SURNAME = "surname"
        private const val COL_CLIENT_MAIL = "mail"
        private const val COL_CLIENT_STREET = "street_addr"
        private const val COL_CLIENT_CITY = "residence"

        private const val TBL_OFFER = "service_offers"
        private const val COL_OFFER_ID = "oid"
        private const val COL_OFFER_TITLE = "title"
        private const val COL_OFFER_COST = "cost"
        private const val COL_OFFER_SLOTS = "slots"
        private const val COL_OFFER_DAYS_STD = "days_std"
        private const val COL_OFFER_DAYS_PRM = "days_prm"
        private const val COL_OFFER_LOCATION = "location_info"

        private const val TBL_RESERVATION = "booking_cart"
        private const val COL_RES_ID = "rid"
        private const val COL_RES_OFFER_ID = "offer_link"
        private const val COL_RES_QTY = "quantity"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TBL_CLIENT (" +
                "$COL_CLIENT_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COL_CLIENT_NAME TEXT," +
                "$COL_CLIENT_SURNAME TEXT," +
                "$COL_CLIENT_MAIL TEXT," +
                "$COL_CLIENT_STREET TEXT," +
                "$COL_CLIENT_CITY TEXT)")

        db.execSQL("CREATE TABLE $TBL_OFFER (" +
                "$COL_OFFER_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COL_OFFER_TITLE TEXT," +
                "$COL_OFFER_COST REAL," +
                "$COL_OFFER_SLOTS INTEGER," +
                "$COL_OFFER_DAYS_STD INTEGER," +
                "$COL_OFFER_DAYS_PRM INTEGER," +
                "$COL_OFFER_LOCATION TEXT)")

        db.execSQL("CREATE TABLE $TBL_RESERVATION (" +
                "$COL_RES_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COL_RES_OFFER_ID INTEGER," +
                "$COL_RES_QTY INTEGER," +
                "FOREIGN KEY($COL_RES_OFFER_ID) REFERENCES $TBL_OFFER($COL_OFFER_ID) ON DELETE CASCADE)")

        initSeeds(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TBL_RESERVATION")
        db.execSQL("DROP TABLE IF EXISTS $TBL_OFFER")
        db.execSQL("DROP TABLE IF EXISTS $TBL_CLIENT")
        onCreate(db)
    }

    private fun initSeeds(db: SQLiteDatabase) {
        val seeds = listOf(
            ContentValues().apply {
                put(COL_OFFER_TITLE, "Teretana Standard")
                put(COL_OFFER_COST, 3500.00)
                put(COL_OFFER_SLOTS, 15)
                put(COL_OFFER_DAYS_STD, 3)
                put(COL_OFFER_DAYS_PRM, 1)
                put(COL_OFFER_LOCATION, "Beograd, Gym Fit")
            },
            ContentValues().apply {
                put(COL_OFFER_TITLE, "Bazen Relax")
                put(COL_OFFER_COST, 4200.00)
                put(COL_OFFER_SLOTS, 8)
                put(COL_OFFER_DAYS_STD, 5)
                put(COL_OFFER_DAYS_PRM, 2)
                put(COL_OFFER_LOCATION, "Novi Sad, AquaLife")
            },
            ContentValues().apply {
                put(COL_OFFER_TITLE, "Tenis Pro")
                put(COL_OFFER_COST, 6000.00)
                put(COL_OFFER_SLOTS, 4)
                put(COL_OFFER_DAYS_STD, 7)
                put(COL_OFFER_DAYS_PRM, 3)
                put(COL_OFFER_LOCATION, "Niš, Set & Match")
            },
            ContentValues().apply {
                put(COL_OFFER_TITLE, "Zumba i Pilates")
                put(COL_OFFER_COST, 3000.00)
                put(COL_OFFER_SLOTS, 20)
                put(COL_OFFER_DAYS_STD, 2)
                put(COL_OFFER_DAYS_PRM, 1)
                put(COL_OFFER_LOCATION, "Kragujevac, Body & Mind")
            },
            ContentValues().apply {
                put(COL_OFFER_TITLE, "CrossFit Arena")
                put(COL_OFFER_COST, 5000.00)
                put(COL_OFFER_SLOTS, 10)
                put(COL_OFFER_DAYS_STD, 4)
                put(COL_OFFER_DAYS_PRM, 2)
                put(COL_OFFER_LOCATION, "Beograd, Spartan")
            }
        )
        seeds.forEach { db.insert(TBL_OFFER, null, it) }
    }

    fun fetchUser(): UserAccount? {
        val db = readableDatabase
        val c = db.query(TBL_CLIENT, null, null, null, null, null, null)
        var res: UserAccount? = null
        if (c.moveToFirst()) {
            res = UserAccount(
                c.getInt(c.getColumnIndexOrThrow(COL_CLIENT_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_CLIENT_NAME)),
                c.getString(c.getColumnIndexOrThrow(COL_CLIENT_SURNAME)),
                c.getString(c.getColumnIndexOrThrow(COL_CLIENT_MAIL)),
                c.getString(c.getColumnIndexOrThrow(COL_CLIENT_STREET)),
                c.getString(c.getColumnIndexOrThrow(COL_CLIENT_CITY))
            )
        }
        c.close()
        return res
    }

    fun syncUser(u: UserAccount) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_CLIENT_NAME, u.name)
            put(COL_CLIENT_SURNAME, u.surname)
            put(COL_CLIENT_MAIL, u.contactEmail)
            put(COL_CLIENT_STREET, u.locationAddress)
            put(COL_CLIENT_CITY, u.residenceCity)
        }
        val current = fetchUser()
        if (current == null) db.insert(TBL_CLIENT, null, cv)
        else db.update(TBL_CLIENT, cv, "$COL_CLIENT_ID = ?", arrayOf(current.id.toString()))
    }

    fun listAllOffers(): List<ActivityPackage> {
        val db = readableDatabase
        val list = mutableListOf<ActivityPackage>()
        val c = db.query(TBL_OFFER, null, null, null, null, null, "$COL_OFFER_TITLE ASC")
        if (c.moveToFirst()) {
            do {
                list.add(ActivityPackage(
                    c.getLong(c.getColumnIndexOrThrow(COL_OFFER_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_OFFER_TITLE)),
                    c.getDouble(c.getColumnIndexOrThrow(COL_OFFER_COST)),
                    c.getInt(c.getColumnIndexOrThrow(COL_OFFER_SLOTS)),
                    c.getInt(c.getColumnIndexOrThrow(COL_OFFER_DAYS_STD)),
                    c.getInt(c.getColumnIndexOrThrow(COL_OFFER_DAYS_PRM)),
                    c.getString(c.getColumnIndexOrThrow(COL_OFFER_LOCATION))
                ))
            } while (c.moveToNext())
        }
        c.close()
        return list
    }

    fun findOffer(id: Long): ActivityPackage? {
        val db = readableDatabase
        val c = db.query(TBL_OFFER, null, "$COL_OFFER_ID = ?", arrayOf(id.toString()), null, null, null)
        var res: ActivityPackage? = null
        if (c.moveToFirst()) {
            res = ActivityPackage(
                id,
                c.getString(c.getColumnIndexOrThrow(COL_OFFER_TITLE)),
                c.getDouble(c.getColumnIndexOrThrow(COL_OFFER_COST)),
                c.getInt(c.getColumnIndexOrThrow(COL_OFFER_SLOTS)),
                c.getInt(c.getColumnIndexOrThrow(COL_OFFER_DAYS_STD)),
                c.getInt(c.getColumnIndexOrThrow(COL_OFFER_DAYS_PRM)),
                c.getString(c.getColumnIndexOrThrow(COL_OFFER_LOCATION))
            )
        }
        c.close()
        return res
    }

    fun persistOffer(p: ActivityPackage): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_OFFER_TITLE, p.title)
            put(COL_OFFER_COST, p.cost)
            put(COL_OFFER_SLOTS, p.capacity)
            put(COL_OFFER_DAYS_STD, p.waitDaysStandard)
            put(COL_OFFER_DAYS_PRM, p.waitDaysPremium)
            put(COL_OFFER_LOCATION, p.facilityInfo)
        }
        return db.insert(TBL_OFFER, null, cv)
    }

    fun modifyOffer(p: ActivityPackage): Int {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_OFFER_TITLE, p.title)
            put(COL_OFFER_COST, p.cost)
            put(COL_OFFER_SLOTS, p.capacity)
            put(COL_OFFER_DAYS_STD, p.waitDaysStandard)
            put(COL_OFFER_DAYS_PRM, p.waitDaysPremium)
            put(COL_OFFER_LOCATION, p.facilityInfo)
        }
        return db.update(TBL_OFFER, cv, "$COL_OFFER_ID = ?", arrayOf(p.id.toString()))
    }

    fun fetchCart(): List<DetailedCartEntry> {
        val db = readableDatabase
        val results = mutableListOf<DetailedCartEntry>()
        val sql = "SELECT r.$COL_RES_ID, r.$COL_RES_OFFER_ID, r.$COL_RES_QTY, " +
                "o.$COL_OFFER_TITLE, o.$COL_OFFER_COST, o.$COL_OFFER_SLOTS, o.$COL_OFFER_DAYS_STD, o.$COL_OFFER_DAYS_PRM, o.$COL_OFFER_LOCATION " +
                "FROM $TBL_RESERVATION r INNER JOIN $TBL_OFFER o ON r.$COL_RES_OFFER_ID = o.$COL_OFFER_ID"
        val c = db.rawQuery(sql, null)
        if (c.moveToFirst()) {
            do {
                val rId = c.getLong(c.getColumnIndexOrThrow(COL_RES_ID))
                val oId = c.getLong(c.getColumnIndexOrThrow(COL_RES_OFFER_ID))
                val qty = c.getInt(c.getColumnIndexOrThrow(COL_RES_QTY))
                
                val p = ActivityPackage(
                    oId,
                    c.getString(c.getColumnIndexOrThrow(COL_OFFER_TITLE)),
                    c.getDouble(c.getColumnIndexOrThrow(COL_OFFER_COST)),
                    c.getInt(c.getColumnIndexOrThrow(COL_OFFER_SLOTS)),
                    c.getInt(c.getColumnIndexOrThrow(COL_OFFER_DAYS_STD)),
                    c.getInt(c.getColumnIndexOrThrow(COL_OFFER_DAYS_PRM)),
                    c.getString(c.getColumnIndexOrThrow(COL_OFFER_LOCATION))
                )
                results.add(DetailedCartEntry(CartEntry(rId, oId, qty), p))
            } while (c.moveToNext())
        }
        c.close()
        return results
    }

    fun pushToCart(offerId: Long, amount: Int): Boolean {
        val db = writableDatabase
        val p = findOffer(offerId) ?: return false
        val c = db.query(TBL_RESERVATION, null, "$COL_RES_OFFER_ID = ?", arrayOf(offerId.toString()), null, null, null)
        val exists = c.moveToFirst()
        var ok = false
        if (exists) {
            val rid = c.getLong(c.getColumnIndexOrThrow(COL_RES_ID))
            val current = c.getInt(c.getColumnIndexOrThrow(COL_RES_QTY))
            val next = current + amount
            if (next <= p.capacity) {
                val cv = ContentValues().apply { put(COL_RES_QTY, next) }
                db.update(TBL_RESERVATION, cv, "$COL_RES_ID = ?", arrayOf(rid.toString()))
                ok = true
            }
        } else {
            if (amount <= p.capacity) {
                val cv = ContentValues().apply {
                    put(COL_RES_OFFER_ID, offerId)
                    put(COL_RES_QTY, amount)
                }
                db.insert(TBL_RESERVATION, null, cv)
                ok = true
            }
        }
        c.close()
        return ok
    }

    fun adjustCartQty(rid: Long, next: Int): Boolean {
        val db = writableDatabase
        if (next <= 0) {
            db.delete(TBL_RESERVATION, "$COL_RES_ID = ?", arrayOf(rid.toString()))
            return true
        }
        val sql = "SELECT r.$COL_RES_OFFER_ID, o.$COL_OFFER_SLOTS FROM $TBL_RESERVATION r " +
                "INNER JOIN $TBL_OFFER o ON r.$COL_RES_OFFER_ID = o.$COL_OFFER_ID WHERE r.$COL_RES_ID = ?"
        val c = db.rawQuery(sql, arrayOf(rid.toString()))
        var possible = false
        if (c.moveToFirst()) {
            if (next <= c.getInt(c.getColumnIndexOrThrow(COL_OFFER_SLOTS))) possible = true
        }
        c.close()
        if (possible) {
            val cv = ContentValues().apply { put(COL_RES_QTY, next) }
            db.update(TBL_RESERVATION, cv, "$COL_RES_ID = ?", arrayOf(rid.toString()))
            return true
        }
        return false
    }

    fun dropFromCart(rid: Long) {
        writableDatabase.delete(TBL_RESERVATION, "$COL_RES_ID = ?", arrayOf(rid.toString()))
    }

    fun emptyCart() {
        writableDatabase.delete(TBL_RESERVATION, null, null)
    }
}
