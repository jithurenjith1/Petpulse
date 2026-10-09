package com.petpulse.app.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.petpulse.app.R
import com.petpulse.app.data.model.PetTag
import com.petpulse.app.data.model.TagScan
import com.petpulse.app.data.model.UserPet

// ---------------------------------------------------------------------------
// Pet QR Tag — shows a large QR code that encodes a SAFE public URL of the form
// https://wagmiya.app/p/<publicId>. The publicId is a random, non-guessable id
// (never the Firestore pet id and never the owner uid), so the public page can
// not be enumerated. The QR encodes ONLY that URL — the owner's phone number is
// never placed in the code or on the page.
// ---------------------------------------------------------------------------

/** Public scan-page base URL. The random publicId is appended. */
private const val TagUrlPrefix = "https://wagmiya.app/p/"

/** Rendered QR bitmap size in pixels (also the zxing matrix size). */
private const val QrSizePx = 640

/** On-screen QR edge length. */
private const val QrViewDp = 240

/**
 * Build a Compose ImageBitmap of a QR code for [content] by rendering the zxing
 * BitMatrix pixel by pixel. Black modules on a white field (required for a
 * reliable scan); the field is baked into the bitmap so it stays legible in both
 * light and dark themes.
 */
private fun qrBitmapFor(content: String, sizePx: Int): ImageBitmap {
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val black = android.graphics.Color.BLACK
    val white = android.graphics.Color.WHITE
    for (x in 0 until sizePx) {
        for (y in 0 until sizePx) {
            bitmap.setPixel(x, y, if (matrix.get(x, y)) black else white)
        }
    }
    return bitmap.asImageBitmap()
}

@Composable
fun PetQrScreen(
    pet: UserPet,
    tag: PetTag?,
    tagScans: List<TagScan>,
    onEnsureTag: () -> Unit,
    onToggleLost: (Boolean) -> Unit,
    onClose: () -> Unit = {}
) {
    val context = LocalContext.current
    val publicId = pet.qrPublicId
    val tagUrl = if (publicId.isBlank()) "" else TagUrlPrefix + publicId

    // Generate the public id + pet_tags document the first time this screen is
    // opened for a pet that has no tag yet.
    LaunchedEffect(publicId) {
        if (publicId.isBlank()) onEnsureTag()
    }

    val qr: ImageBitmap? = remember(tagUrl) {
        if (tagUrl.isBlank()) null else qrBitmapFor(tagUrl, QrSizePx)
    }

    val isLost = tag?.lost == true

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onClose,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.records_close))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.petqr_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = pet.name.ifBlank { stringResource(R.string.mypets_add_pet_title) },
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            if (isLost) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.petqr_lost_banner),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // The QR code itself.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (qr != null) {
                    Image(
                        bitmap = qr,
                        contentDescription = null,
                        modifier = Modifier.size(QrViewDp.dp)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.petqr_preparing),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 48.dp)
                    )
                }
            }

            Text(
                text = tagUrl.ifBlank { stringResource(R.string.petqr_preparing) },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.petqr_scan_help),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = {
                    if (tagUrl.isNotBlank()) {
                        runCatching {
                            context.startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, tagUrl)
                                    },
                                    context.getString(R.string.petqr_share_chooser)
                                )
                            )
                        }
                    }
                },
                enabled = tagUrl.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.petqr_share), fontWeight = FontWeight.Bold)
            }

            Text(
                text = stringResource(R.string.petqr_save_print_hint),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Lost / found toggle (writes the public `lost` flag on pet_tags).
            OutlinedButton(
                onClick = { if (publicId.isNotBlank()) onToggleLost(!isLost) },
                enabled = publicId.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isLost) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = stringResource(if (isLost) R.string.petqr_mark_found else R.string.petqr_mark_lost),
                    fontWeight = FontWeight.Bold
                )
            }

            // "Someone scanned this tag" notifications (owner-only, no push exists).
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 90.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.petqr_scans_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (tagScans.isEmpty()) {
                        Text(
                            text = stringResource(R.string.petqr_scans_empty),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        tagScans.take(10).forEach { scan ->
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = stringResource(
                                        R.string.petqr_scan_line,
                                        pet.name.ifBlank { stringResource(R.string.mypets_add_pet_title) }
                                    ),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (scan.message.isNotBlank()) {
                                    Text(
                                        text = scan.message,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = stringResource(R.string.petqr_scans_note),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
