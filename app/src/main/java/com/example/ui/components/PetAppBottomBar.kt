package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BluePrimary
import com.example.ui.viewmodel.MainNavTab

@Composable
fun PetAppBottomBar(
    currentTab: MainNavTab,
    onTabSelected: (MainNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .testTag("app_bottom_bar")
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        // Tab 1: My Pets
        NavigationBarItem(
            selected = currentTab == MainNavTab.MY_PETS,
            onClick = { onTabSelected(MainNavTab.MY_PETS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainNavTab.MY_PETS) Icons.Filled.Pets else Icons.Outlined.Pets,
                    contentDescription = "My Pets",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "My Pet",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == MainNavTab.MY_PETS) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BluePrimary,
                selectedTextColor = BluePrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_my_pets")
        )

        // Tab 2: Kerala Marketplace
        NavigationBarItem(
            selected = currentTab == MainNavTab.MARKETPLACE,
            onClick = { onTabSelected(MainNavTab.MARKETPLACE) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainNavTab.MARKETPLACE) Icons.Filled.Store else Icons.Outlined.Store,
                    contentDescription = "Kerala Market",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Market (₹)",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == MainNavTab.MARKETPLACE) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BluePrimary,
                selectedTextColor = BluePrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_marketplace")
        )

        // Tab 3: Explore Species & Nutrition
        NavigationBarItem(
            selected = currentTab == MainNavTab.EXPLORE_PETS,
            onClick = { onTabSelected(MainNavTab.EXPLORE_PETS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainNavTab.EXPLORE_PETS) Icons.Filled.Category else Icons.Outlined.Category,
                    contentDescription = "Explore Care",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Guides",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == MainNavTab.EXPLORE_PETS) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BluePrimary,
                selectedTextColor = BluePrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_explore")
        )

        // Tab 4: Partners & Services
        NavigationBarItem(
            selected = currentTab == MainNavTab.PARTNERS_SERVICES,
            onClick = { onTabSelected(MainNavTab.PARTNERS_SERVICES) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainNavTab.PARTNERS_SERVICES) Icons.Filled.Handshake else Icons.Outlined.Handshake,
                    contentDescription = "Services & SOS",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Services",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == MainNavTab.PARTNERS_SERVICES) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BluePrimary,
                selectedTextColor = BluePrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_partners")
        )

        // Tab 5: Profile & Stats
        NavigationBarItem(
            selected = currentTab == MainNavTab.PROFILE_STATS,
            onClick = { onTabSelected(MainNavTab.PROFILE_STATS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainNavTab.PROFILE_STATS) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                    contentDescription = "Profile & Records",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Profile",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == MainNavTab.PROFILE_STATS) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BluePrimary,
                selectedTextColor = BluePrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_profile")
        )
    }
}
