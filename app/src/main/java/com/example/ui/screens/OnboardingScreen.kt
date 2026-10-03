package com.petpulse.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petpulse.app.R
import kotlinx.coroutines.launch

/**
 * First-run onboarding shown once after install, before the login screen.
 * Four slides, swipeable, with Skip on every slide and a Get Started button
 * on the last one. The "seen" flag is stored by the caller (MainActivity).
 */
private data class OnboardSlide(
    val imageRes: Int,
    val title: String,
    val body: String
)

private val onboardSlides = listOf(
    OnboardSlide(
        R.drawable.img_app_icon,
        "Welcome to Wagmiya",
        "Your pet's whole world - one app for every pet parent in Kerala."
    ),
    OnboardSlide(
        R.drawable.img_dog_jane,
        "Shop for your pet",
        "Pet food, medicines and accessories - cash on delivery to your door."
    ),
    OnboardSlide(
        R.drawable.img_grooming_studio,
        "Book trusted services",
        "Vet, grooming, boarding, sitter and pet walker - all in one place."
    ),
    OnboardSlide(
        R.drawable.img_gps_tag,
        "Never lose your pet",
        "5 km lost-pet SOS alert plus GPS tracker collars."
    )
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardSlides.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == onboardSlides.size - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onFinish) {
                Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            val slide = onboardSlides[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = slide.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(28.dp))
                Text(
                    text = slide.title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = slide.body,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            onboardSlides.forEachIndexed { index, _ ->
                val active = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (active) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (active) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                )
            }
        }

        Button(
            onClick = {
                if (isLast) {
                    onFinish()
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .height(52.dp),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text(
                text = if (isLast) "Get Started" else "Next",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
