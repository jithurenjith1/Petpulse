package com.petpulse.app.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Single shared uploader that sends a picked image to Cloudinary over an
 * UNSIGNED upload preset and returns the hosted `secure_url`.
 *
 * Why this exists: the app used to squeeze every photo into a small base64 JPEG
 * and store it INSIDE the Firestore document. That kept quality low and risked
 * Firestore's 1 MiB per-document limit. With Cloudinary the app stores a good
 * size photo (long edge ~1600 px, JPEG quality ~85) on Cloudinary's free plan
 * and keeps only the returned URL in Firestore.
 *
 * It uses OkHttp (already on the classpath) and org.json (built into Android),
 * so NO new Gradle dependency and NO Cloudinary SDK is needed.
 *
 * ===================== OWNER: PASTE YOUR CLOUDINARY VALUES HERE =====================
 * Create a free Cloudinary account (25 GB storage + 25 GB bandwidth, no card), then:
 *   1. Copy the "Cloud name" from the Cloudinary dashboard and paste it into
 *      CLOUD_NAME below.
 *   2. Cloudinary Settings -> Upload -> Add upload preset -> set its "Signing Mode"
 *      to "Unsigned" -> paste the preset NAME (not the cloud name) into
 *      UPLOAD_PRESET below.
 * Until both placeholders are replaced, every upload fails fast and the app keeps
 * using the old base64 behaviour, so nothing breaks.
 * ==================================================================================
 */
object CloudinaryUploader {

    private const val CLOUD_NAME = "bero8q5b"
    private const val UPLOAD_PRESET = "wagmiya_unsigned"

    private const val TAG = "CloudinaryUploader"

    // Good size, not tiny: long edge ~1600 px at JPEG quality ~85.
    private const val MAX_DIM = 1600
    private const val JPEG_QUALITY = 85

    private const val PLACEHOLDER_PREFIX = "PASTE_"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    /**
     * True once the owner has pasted a real cloud name and preset name. While this
     * is false [uploadImage] fails fast instead of hitting the network.
     */
    fun isConfigured(): Boolean =
        !CLOUD_NAME.startsWith(PLACEHOLDER_PREFIX) &&
            !UPLOAD_PRESET.startsWith(PLACEHOLDER_PREFIX)

    /**
     * Reads the image at [uri], downscales/compresses it to a good size, and
     * uploads it to Cloudinary with an unsigned multipart POST. Returns the
     * hosted `secure_url` on success, or a failure Result carrying a clear reason
     * (unconfigured placeholders, unreadable image, network/HTTP error, missing
     * secure_url).
     */
    suspend fun uploadImage(context: Context, uri: Uri): Result<String> {
        if (!isConfigured()) {
            return Result.failure(
                IllegalStateException(
                    "Cloudinary is not configured: paste your cloud name and unsigned " +
                        "upload preset into CLOUD_NAME and UPLOAD_PRESET in CloudinaryUploader.kt"
                )
            )
        }
        return withContext(Dispatchers.IO) {
            try {
                val jpeg = compressForUpload(context, uri)
                if (jpeg == null) {
                    Result.failure(IOException("Could not read an image from the picked photo"))
                } else {
                    Result.success(postToCloudinary(jpeg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Cloudinary upload failed", e)
                Result.failure(e)
            }
        }
    }

    /** Downscale to MAX_DIM on the long edge and JPEG-compress at JPEG_QUALITY. */
    private fun compressForUpload(context: Context, uri: Uri): ByteArray? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MAX_DIM * 2) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = context.contentResolver.openInputStream(uri)?.use {
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
        } else {
            decoded
        }

        val out = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        return out.toByteArray()
    }

    /** POST the JPEG as multipart/form-data and pull `secure_url` out of the JSON. */
    private fun postToCloudinary(jpeg: ByteArray): String {
        val endpoint = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
        val filePart = jpeg.toRequestBody("image/jpeg".toMediaType())
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .addFormDataPart("file", "wagmiya_photo.jpg", filePart)
            .build()
        val request = Request.Builder().url(endpoint).post(body).build()

        return client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException("Cloudinary responded with HTTP ${response.code}: $text")
            }
            val url = JSONObject(text).optString("secure_url", "")
            if (url.isBlank()) {
                throw IOException("Cloudinary response had no secure_url: $text")
            }
            url
        }
    }
}
