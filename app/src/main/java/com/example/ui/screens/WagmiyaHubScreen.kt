package com.petpulse.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Everything that is not the pet, the shop or the partner network.
 *
 * These eleven entry points used to be scattered across the top bar, the profile
 * menu and a "Health Tools" block at the bottom of the pet screen, which is why the
 * app read as a pile of features. They now live in one predictable place so the pet
 * screen can stay about the pet.
 */
enum class WagmiyaHubItem(val title: String, val subtitle: String, val emoji: String) {
    CARE_PLANS("Care plans", "Basic · Care · Premium", "💜"),
    PRICE_LIST("Price list", "Service rates", "📋"),
    INSURANCE("Insurance", "Protect my pet", "🛡️"),
    RECORDS("Records", "Health records", "📁"),
    FIRST_AID("First aid", "Emergency steps", "➕"),
    TOXIC_FOOD("Toxic foods", "Is it safe?", "🚫"),
    SYMPTOMS("Symptoms", "Guided check", "📈"),
    VET_ONLINE("Vet online", "Talk to a vet", "🩺"),
    NEARBY("Nearby", "Vets & shops", "📍"),
    COMMUNITY("Community", "Pet parents", "👥"),
    CARE_TIPS("Care tips", "Daily advice", "💡"),
    WHATS_NEW("What's new", "News & events", "📰"),
    QR_TAG("QR tag", "Lost & found", "▦"),
    REMINDERS("Reminders", "Vaccine alerts", "⏰"),
    HOUSING_HELP("Housing help", "Rent & rules", "🏠"),
    SETTINGS("Settings", "Profile & more", "⚙️")
}

private data class HubGroup(val title: String, val items: List<WagmiyaHubItem>)

private val HUB_GROUPS = listOf(
    HubGroup("Care & plans", listOf(WagmiyaHubItem.CARE_PLANS, WagmiyaHubItem.PRICE_LIST, WagmiyaHubItem.INSURANCE, WagmiyaHubItem.RECORDS)),
    HubGroup("Help, fast", listOf(WagmiyaHubItem.FIRST_AID, WagmiyaHubItem.TOXIC_FOOD, WagmiyaHubItem.SYMPTOMS, WagmiyaHubItem.VET_ONLINE)),
    HubGroup("Near me", listOf(WagmiyaHubItem.NEARBY, WagmiyaHubItem.COMMUNITY, WagmiyaHubItem.CARE_TIPS, WagmiyaHubItem.WHATS_NEW)),
    HubGroup("Account", listOf(WagmiyaHubItem.QR_TAG, WagmiyaHubItem.REMINDERS, WagmiyaHubItem.HOUSING_HELP, WagmiyaHubItem.SETTINGS))
))
)

@Composable
fun WagmiyaHubScreen(
    onOpen: (WagmiyaHubItem) -> Unit,
    onContactSupport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "Wagmiya",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Everything else, in one place",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HUB_GROUPS.forEach { group ->
            Text(
                text = group.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                group.items.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { item ->
                            HubTile(
                                item = item,
                                onClick = { onOpen(item) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable(onClick = onContactSupport),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "💬", fontSize = 15.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Need a hand?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Message us on WhatsApp — we usually reply within a few hours.",
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HubTile(
    item: WagmiyaHubItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(96.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.emoji, fontSize = 14.sp)
            }
            Spacer(Modifier.height(7.dp))
            Text(
                text = item.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = item.subtitle,
                fontSize = 9.sp,
                lineHeight = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
