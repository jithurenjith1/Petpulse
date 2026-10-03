package com.petpulse.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.petpulse.app.data.model.AdminLostPetAlert
import com.petpulse.app.data.model.AdminOrder
import com.petpulse.app.data.model.CommunityPost
import com.petpulse.app.data.model.PartnerApplication
import com.petpulse.app.data.model.RescueReport
import com.petpulse.app.data.model.SupportTicket
import com.petpulse.app.data.model.Dealer
import com.petpulse.app.data.model.OrderItemSnap
import com.petpulse.app.data.model.ServiceBooking
import com.petpulse.app.data.model.ShopProduct
import com.petpulse.app.data.model.VerifiedDoctor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Commerce backend: owner-added products, customer orders, and dealers.
 * Firestore rules enforce that only the account listed in `admins` can
 * write products/dealers or see orders. Everyone can read products.
 */
class FirestoreCommerceRepository(private val appContext: Context) {

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
        @Suppress("UNCHECKED_CAST")
        val photoData: List<String> = (get("photoData") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        ShopProduct(
            id = id,
            name = getString("name") ?: return null,
            listType = getString("listType") ?: "Food",
            category = getString("category") ?: "General",
            priceInr = getDouble("priceInr") ?: 0.0,
            description = getString("description") ?: "",
            petType = getString("petType") ?: "All",
            foodType = getString("foodType") ?: "All",
            lifeStage = getString("lifeStage") ?: "All",
            verified = getBoolean("verified") ?: false,
            photoUris = photoData.mapIndexed { i, b64 -> productPhotoFileFor(id, i, b64) }.filter { it.isNotBlank() }
        )
        } catch (e: Exception) {
            Log.e("FsCommerce", "Skipping malformed product ${id}", e)
            null
        }
    }

    suspend fun addProduct(p: ShopProduct, photoUris: List<Uri> = emptyList()): Result<Unit> {
        return try {
            auth.currentUser ?: return Result.failure(IllegalStateException("NOT_SIGNED_IN"))
            val photos = photoUris.take(3).mapNotNull { compressProductPhotoToBase64(it) }
            db.collection("products").document().set(
                mapOf(
                    "name" to p.name,
                    "listType" to p.listType,
                    "category" to p.category,
                    "priceInr" to p.priceInr,
                    "description" to p.description,
                    "petType" to p.petType,
                    "foodType" to p.foodType,
                    "lifeStage" to p.lifeStage,
                    "verified" to p.verified,
                    "photoData" to photos,
                    "createdAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Decodes a stored base64 sitter photo into a cache file and returns its path for Coil. */
    private fun productPhotoFileFor(docId: String, index: Int, base64Data: String): String {
        return try {
            val dir = File(appContext.cacheDir, "sitter_photos").apply { mkdirs() }
            val f = File(dir, "${docId}_${index}.jpg")
            if (!f.exists()) {
                f.writeBytes(Base64.decode(base64Data, Base64.NO_WRAP))
            }
            f.absolutePath
        } catch (e: Exception) {
            Log.e("FsCommerce", "sitter photo decode failed for ${docId}/${index}", e)
            ""
        }
    }

    /** Downscale + JPEG-compress a picked photo, return base64 (or null). */
    private fun compressProductPhotoToBase64(uri: Uri): String? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            appContext.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 1000 * 2) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            val decoded = appContext.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            } ?: return null
            val scale = minOf(1f, 1000f / maxOf(decoded.width, decoded.height, 1))
            val bmp = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    decoded,
                    (decoded.width * scale).toInt().coerceAtLeast(1),
                    (decoded.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else decoded
            val out = ByteArrayOutputStream()
            var quality = 78
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, out)
            while (out.size() > 110_000 && quality > 30) {
                quality -= 16
                out.reset()
                bmp.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("FsCommerce", "sitter photo compress failed", e)
            null
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

    // ---------- bookings (doctor consults + trainer on-demand) ----------

    /** Live stream of ALL bookings (admin panel). */
    fun observeBookings(): Flow<List<ServiceBooking>> = callbackFlow {
        val sub = db.collection("bookings").addSnapshotListener { snap, err ->
            if (err != null) {
                Log.e("FsCommerce", "bookings listen failed", err)
                trySend(emptyList())
                return@addSnapshotListener
            }
            trySend(
                snap?.documents
                    ?.mapNotNull { it.toServiceBooking() }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()
            )
        }
        awaitClose { sub.remove() }
    }

    /** Live bookings of the signed-in customer only (My Bookings tab). */
    fun observeMyBookings(): Flow<List<ServiceBooking>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val sub = db.collection("bookings").whereEqualTo("ownerId", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e("FsCommerce", "my bookings listen failed", err)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(
                    snap?.documents
                        ?.mapNotNull { it.toServiceBooking() }
                        ?.sortedByDescending { it.createdAt }
                        ?: emptyList()
                )
            }
        awaitClose { sub.remove() }
    }

    private fun DocumentSnapshot.toServiceBooking(): ServiceBooking? = try {
        ServiceBooking(
            id = id,
            type = getString("type") ?: "DOCTOR",
            ownerId = getString("ownerId") ?: "",
            customerName = getString("customerName") ?: "",
            customerPhone = getString("customerPhone") ?: "",
            petName = getString("petName") ?: "",
            providerName = getString("providerName") ?: "",
            serviceInfo = getString("serviceInfo") ?: "",
            dateLabel = getString("dateLabel") ?: "",
            slot = getString("slot") ?: "",
            notes = getString("notes") ?: "",
            feeInr = getDouble("feeInr") ?: 0.0,
            status = getString("status") ?: "NEW",
            assignedName = getString("assignedName") ?: "",
            assignedPhone = getString("assignedPhone") ?: "",
            createdAt = getLong("createdAt") ?: 0L
        )
    } catch (e: Exception) {
        Log.e("FsCommerce", "Skipping malformed booking ${id}", e)
        null
    }

    suspend fun placeBooking(booking: ServiceBooking): Result<String> {
        return try {
            auth.currentUser ?: return Result.failure(IllegalStateException("NOT_SIGNED_IN"))
            val docRef = db.collection("bookings").document()
            docRef.set(
                mapOf(
                    "type" to booking.type,
                    "ownerId" to (auth.currentUser?.uid ?: ""),
                    "customerName" to booking.customerName,
                    "customerPhone" to booking.customerPhone,
                    "petName" to booking.petName,
                    "providerName" to booking.providerName,
                    "serviceInfo" to booking.serviceInfo,
                    "dateLabel" to booking.dateLabel,
                    "slot" to booking.slot,
                    "notes" to booking.notes,
                    "feeInr" to booking.feeInr,
                    "status" to "NEW",
                    "assignedName" to "",
                    "assignedPhone" to "",
                    "createdAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("FsCommerce", "placeBooking failed", e)
            Result.failure(e)
        }
    }

    /** Admin assigns a doctor/trainer → booking becomes CONFIRMED with contact details. */
    suspend fun assignBooking(bookingId: String, name: String, phone: String): Result<Unit> = try {
        db.collection("bookings").document(bookingId).update(
            mapOf("status" to "CONFIRMED", "assignedName" to name, "assignedPhone" to phone)
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateBookingStatus(bookingId: String, status: String): Result<Unit> = try {
        db.collection("bookings").document(bookingId).update("status", status).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ---------- partner vets (admin-managed, public browse) ----------

    /** Live stream of partner vets added by the owner. */
    fun observeVets(): Flow<List<VerifiedDoctor>> = callbackFlow {
        val sub = db.collection("vets").addSnapshotListener { snap, err ->
            if (err != null) {
                Log.e("FsCommerce", "vets listen failed", err)
                trySend(emptyList())
                return@addSnapshotListener
            }
            trySend(
                snap?.documents
                    ?.mapNotNull { it.toVet() }
                    ?.sortedByDescending { (it.experienceYears) }
                    ?: emptyList()
            )
        }
        awaitClose { sub.remove() }
    }

    private fun DocumentSnapshot.toVet(): VerifiedDoctor? {
        return try {
            VerifiedDoctor(
                id = id,
                name = getString("name") ?: return null,
                degrees = getString("degrees") ?: "BVSc & AH",
                ksvcRegNumber = getString("ksvcRegNumber") ?: "",
                specialization = getString("specialization") ?: "Veterinary Physician",
                experienceYears = (getLong("experienceYears") ?: 5L).toInt(),
                clinicName = getString("clinicName") ?: "",
                clinicCity = getString("clinicCity") ?: "Kochi",
                clinicAddress = getString("clinicAddress") ?: "",
                videoConsultFeeInr = getDouble("videoConsultFeeInr") ?: 349.0,
                inPersonConsultFeeInr = getDouble("inPersonConsultFeeInr") ?: 499.0,
                phone = getString("phone") ?: "",
                isOnline = getBoolean("isOnline") ?: false
            )
        } catch (e: Exception) {
            Log.e("FsCommerce", "Skipping malformed vet ${id}", e)
            null
        }
    }

    suspend fun addVet(vet: VerifiedDoctor): Result<Unit> = try {
        db.collection("vets").document().set(
            mapOf(
                "name" to vet.name,
                "degrees" to vet.degrees,
                "ksvcRegNumber" to vet.ksvcRegNumber,
                "specialization" to vet.specialization,
                "experienceYears" to vet.experienceYears,
                "clinicName" to vet.clinicName,
                "clinicCity" to vet.clinicCity,
                "clinicAddress" to vet.clinicAddress,
                "videoConsultFeeInr" to vet.videoConsultFeeInr,
                "inPersonConsultFeeInr" to vet.inPersonConsultFeeInr,
                "phone" to vet.phone,
                "isOnline" to vet.isOnline,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteVet(vetId: String): Result<Unit> = try {
        db.collection("vets").document(vetId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ---------- community posts (Firestore-backed) ----------

    fun observeCommunityPosts(): Flow<List<CommunityPost>> = callbackFlow {
        val sub = db.collection("community_posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e("FsCommerce", "community posts listen failed", err)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(
                    snap?.documents?.mapNotNull { it.toCommunityPost() } ?: emptyList()
                )
            }
        awaitClose { sub.remove() }
    }

    private fun DocumentSnapshot.toCommunityPost(): CommunityPost? {
        return try {
            CommunityPost(
                id = id,
                ownerId = getString("ownerId") ?: "",
                authorName = getString("authorName") ?: "Pet Lover",
                petName = getString("petName") ?: "",
                message = getString("message") ?: return null,
                createdAt = getLong("createdAt") ?: 0L
            )
        } catch (e: Exception) {
            Log.e("FsCommerce", "Skipping malformed community post", e)
            null
        }
    }

    suspend fun addCommunityPost(authorName: String, petName: String, message: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("community_posts").add(
            mapOf(
                "ownerId" to uid,
                "authorName" to authorName,
                "petName" to petName,
                "message" to message,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun deleteCommunityPost(postId: String) {
        if (postId.isBlank()) return
        db.collection("community_posts").document(postId).delete().await()
    }

    // ---------- admin: lost pet SOS alerts ----------

    fun observeLostPetAlerts(): Flow<List<AdminLostPetAlert>> = callbackFlow {
        val sub = db.collection("lost_pet_alerts")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e("FsCommerce", "lost alerts listen failed", err)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(
                    snap?.documents?.mapNotNull { doc ->
                        try {
                            AdminLostPetAlert(
                                id = doc.id,
                                petName = doc.getString("petName") ?: "",
                                species = doc.getString("species") ?: "",
                                breed = doc.getString("breed") ?: "",
                                location = doc.getString("location") ?: "",
                                reward = doc.getString("reward") ?: "",
                                contactPhone = doc.getString("contactPhone") ?: "",
                                date = doc.getString("date") ?: ""
                            )
                        } catch (e: Exception) {
                            Log.e("FsCommerce", "Skipping malformed lost alert", e)
                            null
                        }
                    } ?: emptyList()
                )
            }
        awaitClose { sub.remove() }
    }

    suspend fun deleteLostPetAlert(alertId: String) {
        if (alertId.isBlank()) return
        db.collection("lost_pet_alerts").document(alertId).delete().await()
    }

    // ---------- support tickets (Help & Support) ----------
    fun observeSupportTickets(): Flow<List<SupportTicket>> = callbackFlow {
        val sub = db.collection("support_tickets").addSnapshotListener { snap, err ->
            if (err != null) {
                // Non-admin listeners are denied by rules - just show nothing.
                trySend(emptyList())
                return@addSnapshotListener
            }
            trySend(snap?.documents?.mapNotNull { it.toSupportTicket() } ?: emptyList())
        }
        awaitClose { sub.remove() }
    }

    private fun DocumentSnapshot.toSupportTicket(): SupportTicket? = try {
        SupportTicket(
            id = id,
            category = getString("category") ?: "Other",
            subject = getString("subject") ?: "",
            details = getString("details") ?: "",
            contact = getString("contact") ?: "",
            createdAt = getLong("createdAt") ?: 0L,
            status = getString("status") ?: "New"
        )
    } catch (e: Exception) {
        Log.e("FsCommerce", "Skipping malformed support ticket {id}", e)
        null
    }

    suspend fun submitSupportTicket(t: SupportTicket): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("NOT_SIGNED_IN"))
        return try {
            db.collection("support_tickets").document().set(
                mapOf(
                    "category" to t.category,
                    "subject" to t.subject,
                    "details" to t.details,
                    "contact" to t.contact,
                    "createdAt" to System.currentTimeMillis(),
                    "status" to "New"
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSupportTicket(id: String): Result<Unit> = try {
        db.collection("support_tickets").document(id).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ---------- animal rescue reports ----------
    fun observeRescueReports(): Flow<List<RescueReport>> = callbackFlow {
        val sub = db.collection("rescue_reports").addSnapshotListener { snap, err ->
            if (err != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            trySend(snap?.documents?.mapNotNull { it.toRescueReport() } ?: emptyList())
        }
        awaitClose { sub.remove() }
    }

    private fun DocumentSnapshot.toRescueReport(): RescueReport? = try {
        RescueReport(
            id = id,
            animalType = getString("animalType") ?: "",
            description = getString("description") ?: "",
            location = getString("location") ?: "",
            contact = getString("contact") ?: "",
            createdAt = getLong("createdAt") ?: 0L,
            status = getString("status") ?: "New"
        )
    } catch (e: Exception) {
        Log.e("FsCommerce", "Skipping malformed rescue report {id}", e)
        null
    }

    suspend fun submitRescueReport(r: RescueReport): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("NOT_SIGNED_IN"))
        return try {
            db.collection("rescue_reports").document().set(
                mapOf(
                    "animalType" to r.animalType,
                    "description" to r.description,
                    "location" to r.location,
                    "contact" to r.contact,
                    "createdAt" to System.currentTimeMillis(),
                    "status" to "New"
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRescueReport(id: String): Result<Unit> = try {
        db.collection("rescue_reports").document(id).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ---------- partner applications (business join + featured plan requests) ----------
    fun observePartnerApplications(): Flow<List<PartnerApplication>> = callbackFlow {
        val sub = db.collection("partner_applications").addSnapshotListener { snap, err ->
            if (err != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            trySend(snap?.documents?.mapNotNull { it.toPartnerApplication() } ?: emptyList())
        }
        awaitClose { sub.remove() }
    }

    private fun DocumentSnapshot.toPartnerApplication(): PartnerApplication? = try {
        PartnerApplication(
            id = id,
            kind = getString("kind") ?: "Business Partner",
            name = getString("name") ?: "",
            category = getString("category") ?: "",
            city = getString("city") ?: "",
            phone = getString("phone") ?: "",
            planName = getString("planName") ?: "",
            createdAt = getLong("createdAt") ?: 0L,
            status = getString("status") ?: "NEW"
        )
    } catch (e: Exception) {
        Log.e("FsCommerce", "Skipping malformed partner application", e)
        null
    }

    suspend fun submitPartnerApplication(a: PartnerApplication): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("NOT_SIGNED_IN"))
        return try {
            db.collection("partner_applications").document().set(
                mapOf(
                    "kind" to a.kind,
                    "name" to a.name,
                    "category" to a.category,
                    "city" to a.city,
                    "phone" to a.phone,
                    "planName" to a.planName,
                    "createdAt" to System.currentTimeMillis(),
                    "status" to "NEW"
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePartnerApplication(id: String): Result<Unit> = try {
        db.collection("partner_applications").document(id).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
