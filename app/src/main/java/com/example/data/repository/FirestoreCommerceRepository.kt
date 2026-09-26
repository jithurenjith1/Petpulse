package com.petpulse.app.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.petpulse.app.data.model.AdminOrder
import com.petpulse.app.data.model.Dealer
import com.petpulse.app.data.model.OrderItemSnap
import com.petpulse.app.data.model.ShopProduct
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Commerce backend: owner-added products, customer orders, and dealers.
 * Firestore rules enforce that only the account listed in `admins` can
 * write products/dealers or see orders. Everyone can read products.
 */
class FirestoreCommerceRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ---------- admin gate ----------
    fun observeIsAdmin(): Flow<Boolean> = callbackFlow {
        val registration = arrayOfNulls<ListenerRegistration>(1)
        val authListener = FirebaseAuth.AuthStateListener { fa ->
            registration[0]?.remove()
            val email = fa.currentUser?.email?.lowercase()
            if (email == null) {
                trySend(false)
                return@AuthStateListener
            }
            registration[0] = db.collection("admins").document(email)
                .addSnapshotListener { snap, err ->
                    trySend(err == null && snap != null && snap.exists())
                }
        }
        auth.addAuthStateListener(authListener)
        awaitClose {
            auth.removeAuthStateListener(authListener)
            registration[0]?.remove()
        }
    }

    // ---------- products ----------
    fun observeProducts(): Flow<List<ShopProduct>> = callbackFlow {
        val sub = db.collection("products").addSnapshotListener { snap, err ->
            if (err != null) {
                Log.e("FsCommerce", "products listen failed", err)
                trySend(emptyList())
                return@addSnapshotListener
            }
            trySend(snap?.documents?.mapNotNull { it.toShopProduct() } ?: emptyList())
        }
        awaitClose { sub.remove() }
    }

    private fun DocumentSnapshot.toShopProduct(): ShopProduct? {
        return try {
        ShopProduct(
            id = id,
            name = getString("name") ?: return null,
            listType = getString("listType") ?: "Food",
            category = getString("category") ?: "General",
            priceInr = getDouble("priceInr") ?: 0.0,
            description = getString("description") ?: ""
        )
        } catch (e: Exception) {
            Log.e("FsCommerce", "Skipping malformed product ${id}", e)
            null
        }
    }

    suspend fun addProduct(p: ShopProduct): Result<Unit> {
        return try {
            auth.currentUser ?: return Result.failure(IllegalStateException("NOT_SIGNED_IN"))
            db.collection("products").document().set(
                mapOf(
                    "name" to p.name,
                    "listType" to p.listType,
                    "category" to p.category,
                    "priceInr" to p.priceInr,
                    "description" to p.description,
                    "createdAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(id: String): Result<Unit> = try {
        db.collection("products").document(id).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ---------- dealers ----------
    fun observeDealers(): Flow<List<Dealer>> = callbackFlow {
        val sub = db.collection("dealers").addSnapshotListener { snap, err ->
            if (err != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            trySend(snap?.documents?.mapNotNull {
                try {
                    val dealerName = it.getString("name")
                    if (dealerName.isNullOrBlank()) {
                        null
                    } else {
                        Dealer(
                            id = it.id,
                            name = dealerName,
                            phone = it.getString("phone") ?: "",
                            city = it.getString("city") ?: "Kochi"
                        )
                    }
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList())
        }
        awaitClose { sub.remove() }
    }

    suspend fun addDealer(d: Dealer): Result<Unit> {
        return try {
            auth.currentUser ?: return Result.failure(IllegalStateException("NOT_SIGNED_IN"))
            db.collection("dealers").document().set(
                mapOf("name" to d.name, "phone" to d.phone, "city" to d.city)
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDealer(id: String): Result<Unit> = try {
        db.collection("dealers").document(id).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ---------- orders ----------
    fun observeOrders(): Flow<List<AdminOrder>> = callbackFlow {
        val sub = db.collection("orders").addSnapshotListener { snap, err ->
            if (err != null) {
                Log.e("FsCommerce", "orders listen failed", err)
                trySend(emptyList())
                return@addSnapshotListener
            }
            trySend(
                snap?.documents
                    ?.mapNotNull { it.toAdminOrder() }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()
            )
        }
        awaitClose { sub.remove() }
    }

    /** Live orders of the signed-in customer only (for the My Orders screen). */
    fun observeMyOrders(): Flow<List<AdminOrder>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val sub = db.collection("orders").whereEqualTo("ownerId", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e("FsCommerce", "my orders listen failed", err)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(
                    snap?.documents
                        ?.mapNotNull { it.toAdminOrder() }
                        ?.sortedByDescending { it.createdAt }
                        ?: emptyList()
                )
            }
        awaitClose { sub.remove() }
    }

    private fun DocumentSnapshot.toAdminOrder(): AdminOrder? = try {
        val rawItems = (get("items") as? List<*>).orEmpty()
        AdminOrder(
            id = id,
            orderNumber = getString("orderNumber") ?: id.takeLast(6),
            customerName = getString("customerName") ?: "",
            customerPhone = getString("customerPhone") ?: "",
            address = getString("address") ?: "",
            city = getString("city") ?: "",
            items = rawItems.mapNotNull { raw ->
                val m = raw as? Map<*, *> ?: return@mapNotNull null
                OrderItemSnap(
                    name = m["name"] as? String ?: "",
                    priceInr = (m["price"] as? Number)?.toDouble() ?: 0.0,
                    quantity = (m["qty"] as? Number)?.toInt() ?: 1
                )
            },
            totalInr = getDouble("totalInr") ?: 0.0,
            status = getString("status") ?: "NEW",
            dealerName = getString("dealerName") ?: "",
            dealerPhone = getString("dealerPhone") ?: "",
            createdAt = getLong("createdAt") ?: 0L
        )
    } catch (e: Exception) {
        Log.e("FsCommerce", "Skipping malformed order ${id}", e)
        null
    }

    suspend fun placeOrder(
        orderNumber: String,
        items: List<OrderItemSnap>,
        totalInr: Double,
        customerName: String,
        customerPhone: String,
        address: String,
        city: String
    ): Result<String> {
        return try {
            auth.currentUser ?: return Result.failure(IllegalStateException("NOT_SIGNED_IN"))
        val docRef = db.collection("orders").document()
        docRef.set(
            mapOf(
                "ownerId" to (auth.currentUser?.uid ?: ""),
                "orderNumber" to orderNumber,
                "items" to items.map { mapOf("name" to it.name, "price" to it.priceInr, "qty" to it.quantity) },
                "totalInr" to totalInr,
                "customerName" to customerName,
                "customerPhone" to customerPhone,
                "address" to address,
                "city" to city,
                "status" to "NEW",
                "dealerName" to "",
                "dealerPhone" to "",
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("FsCommerce", "placeOrder failed", e)
            Result.failure(e)
        }
    }

    suspend fun assignDealer(orderId: String, dealer: Dealer): Result<Unit> = try {
        db.collection("orders").document(orderId).update(
            mapOf("status" to "ASSIGNED", "dealerName" to dealer.name, "dealerPhone" to dealer.phone)
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> = try {
        db.collection("orders").document(orderId).update("status", status).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
