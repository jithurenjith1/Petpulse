package com.petpulse.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.petpulse.app.data.model.MarketPet
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executors

/**
 * Real marketplace backend on the FREE plan: listings live in the Firestore
 * `market_listings` collection and photos are stored INSIDE the document as
 * compressed base64 (no Firebase Storage / no billing account needed).
 * Photos are downscaled and JPEG-compressed client-side so each listing
 * document stays well under Firestore's 1 MB limit.
 */
class FirestoreMarketplaceRepository(private val appContext: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val bgExecutor = Executors.newSingleThreadExecutor()

    companion object {
        private const val MAX_PHOTOS = 3
        private const val MAX_DIM = 1200
        private const val PHOTO_BUDGET_BYTES = 180_000
    }

    // ---------- read ----------

    /** Live stream of all marketplace listings, newest first. */
    fun observeMarketPets(): Flow<List<MarketPet>> = callbackFlow {
        val subscription = db.collection("market_listings")
            .addSnapshotListener(bgExecutor) { snapshot, error ->
                if (error != null) {
                    Log.e("FsMarketRepo", "Error observing listings", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val pets = snapshot?.documents
                    ?.mapNotNull { doc -> toMarketPet(doc) }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()
                trySend(pets)
            }
        awaitClose { subscription.remove() }
    }

    private fun toMarketPet(doc: DocumentSnapshot): MarketPet? = try {
        val photoData = (doc.get("photoData") as? List<*>)?.filterIsInstance<String>().orEmpty()
        MarketPet(
            id = doc.id,
            name = doc.getString("name") ?: return null,
            species = doc.getString("species") ?: "Dog",
            breed = doc.getString("breed") ?: "",
            age = doc.getString("age") ?: "",
            gender = doc.getString("gender") ?: "Male",
            city = doc.getString("city") ?: "Kochi",
            isImportedExotic = doc.getBoolean("isImportedExotic") ?: false,
            importCountry = doc.getString("importCountry"),
            listingType = doc.getString("listingType") ?: "Sale",
            priceInr = doc.getDouble("priceInr") ?: 0.0,
            originalPriceInr = doc.getDouble("originalPriceInr"),
            isVaccinated = doc.getBoolean("isVaccinated") ?: true,
            isMicrochipped = doc.getBoolean("isMicrochipped") ?: true,
            certificationDetails = doc.getString("certificationDetails") ?: "",
            sellerName = doc.getString("sellerName") ?: "",
            sellerPhone = doc.getString("sellerPhone") ?: "",
            isVerifiedBreeder = doc.getBoolean("isVerifiedBreeder") ?: false,
            description = doc.getString("description") ?: "",
            temperament = doc.getString("temperament") ?: "",
            photoUris = photoData.mapIndexed { i, b64 -> photoFileFor(doc.id, i, b64) }
                .filter { it.isNotBlank() },
            ownerId = doc.getString("ownerId") ?: "",
            createdAt = doc.getLong("createdAt") ?: 0L
        )
    } catch (e: Exception) {
        Log.e("FsMarketRepo", "Skipping malformed listing ${doc.id}", e)
        null
    }

    /** Decodes a base64 photo into a cache file and returns its path for Coil. */
    private fun photoFileFor(docId: String, index: Int, base64Data: String): String = try {
        val dir = File(appContext.cacheDir, "market_photos").apply { mkdirs() }
        val f = File(dir, "${docId}_$index.jpg")
        if (!f.exists()) {
            val bytes = Base64.decode(base64Data, Base64.NO_WRAP)
            f.writeBytes(bytes)
        }
        f.absolutePath
    } catch (e: Exception) {
        Log.e("FsMarketRepo", "photo decode failed for $docId/$index", e)
        ""
    }

    // ---------- write ----------

    /**
     * Compresses the photos, stores them inside the listing document, and saves it.
     * Fails with NOT_SIGNED_IN when the user is not authenticated.
     */
    suspend fun postListing(pet: MarketPet, photoUris: List<Uri>): Result<String> = try {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("NOT_SIGNED_IN"))

        val photos = photoUris.take(MAX_PHOTOS).mapNotNull { compressToBase64(it) }
        val docRef = db.collection("market_listings").document()

        docRef.set(
            mapOf(
                "name" to pet.name,
                "species" to pet.species,
                "breed" to pet.breed,
                "age" to pet.age,
                "gender" to pet.gender,
                "city" to pet.city,
                "isImportedExotic" to pet.isImportedExotic,
                "importCountry" to pet.importCountry,
                "listingType" to pet.listingType,
                "priceInr" to pet.priceInr,
                "originalPriceInr" to pet.originalPriceInr,
                "isVaccinated" to pet.isVaccinated,
                "isMicrochipped" to pet.isMicrochipped,
                "certificationDetails" to pet.certificationDetails,
                "sellerName" to pet.sellerName,
                "sellerPhone" to pet.sellerPhone,
                "isVerifiedBreeder" to pet.isVerifiedBreeder,
                "description" to pet.description,
                "temperament" to pet.temperament,
                "photoData" to photos,
                "ownerId" to uid,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Log.e("FsMarketRepo", "postListing failed", e)
        Result.failure(e)
    }

    /** Downscale + JPEG-compress a picked photo, return base64 (or null). */
    private fun compressToBase64(uri: Uri): String? = try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        appContext.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MAX_DIM * 2) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = appContext.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        } ?: return null

        val scale = minOf(1f, MAX_DIM.toFloat() / maxOf(decoded.width, decoded.height, 1))
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
        while (out.size() > PHOTO_BUDGET_BYTES && quality > 30) {
            quality -= 16
            out.reset()
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }
        Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    } catch (e: Exception) {
        Log.e("FsMarketRepo", "photo compress failed", e)
        null
    }
}
