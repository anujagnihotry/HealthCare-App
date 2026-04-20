package com.healthcare.app.ui.auth.branding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthcare.app.ui.theme.splashBackgroundBrush

private val GlossBlueA = Color(0xFF60A5FA)
private val GlossBlueB = Color(0xFF2563EB)
private val GlossBlueC = Color(0xFF1E40AF)

private val GlossGreenA = Color(0xFF6EE7B7)
private val GlossGreenB = Color(0xFF10B981)
private val GlossGreenC = Color(0xFF047857)

private val GlossPurpleA = Color(0xFFC4B5FD)
private val GlossPurpleB = Color(0xFF9333EA)
private val GlossPurpleC = Color(0xFF6B21A8)

private val GlossTealA = Color(0xFF5EEAD4)
private val GlossTealB = Color(0xFF14B8A6)
private val GlossTealC = Color(0xFF0F766E)

@Composable
fun SplashBrandedBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(splashBackgroundBrush()))
}

@Composable
fun HealthCareHeroCluster(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val s = if (compact) 0.82f else 1f
    val boxW = (140 * s).dp
    val boxH = (130 * s).dp
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(boxW, boxH),
        ) {
            val phoneW = (86 * s).dp
            val phoneH = (116 * s).dp
            val corner = (18 * s).dp
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(phoneW, phoneH)
                    .clip(RoundedCornerShape(corner))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFF8FAFC), Color(0xFFCBD5E1), Color(0xFF94A3B8)),
                            start = Offset(0f, 0f),
                            end = Offset(120f, 200f),
                        ),
                    )
                    .shadow((8 * s).dp, RoundedCornerShape(corner)),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size((54 * s).dp)
                    .offset(y = (-10 * s).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFFF8A8A), Color(0xFFEF4444), Color(0xFFB91C1C)),
                            center = Offset(26f * s, 20f * s),
                            radius = 88f * s,
                        ),
                    )
                    .shadow((10 * s).dp, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size((28 * s).dp),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-2 * s).dp, y = (10 * s).dp)
                    .size((30 * s).dp)
                    .clip(RoundedCornerShape((8 * s).dp))
                    .background(Color.White)
                    .shadow(4.dp, RoundedCornerShape((8 * s).dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = GlossBlueB,
                    modifier = Modifier.size((17 * s).dp),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (8 * s).dp, y = (-12 * s).dp)
                    .size((28 * s).dp, (20 * s).dp)
                    .clip(RoundedCornerShape((6 * s).dp))
                    .background(Color.White)
                    .shadow(3.dp, RoundedCornerShape((6 * s).dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = GlossBlueB,
                    modifier = Modifier.size((13 * s).dp),
                )
            }
        }

        Spacer(modifier = Modifier.width((14 * s).dp))

        HealthCareWordMark(compact = compact)
    }
}

@Composable
fun HealthCareWordMark(compact: Boolean) {
    val titleSize = if (compact) 30.sp else 36.sp
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = "Health ",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = titleSize,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.5).sp,
            ),
        )
        Text(
            text = "Care",
            style = TextStyle(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFBAE6FD), Color(0xFF38BDF8), Color(0xFF2563EB)),
                    start = Offset(0f, 0f),
                    end = Offset(120f, 48f),
                ),
                fontSize = titleSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
            ),
        )
    }
}

@Composable
fun BookManageCareTagline(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color.White.copy(alpha = 0.35f),
            thickness = 1.dp,
        )
        Text(
            text = "BOOK • MANAGE • CARE",
            modifier = Modifier.padding(horizontal = 14.dp),
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.2.sp,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color.White.copy(alpha = 0.35f),
            thickness = 1.dp,
        )
    }
}

@Composable
fun GlossyFeatureRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top,
    ) {
        GlossyFeatureOrb(
            label = "Book\nAppointments",
            icon = Icons.Default.CalendarMonth,
            brush = Brush.radialGradient(
                colors = listOf(GlossBlueA, GlossBlueB, GlossBlueC),
                center = Offset(36f, 30f),
                radius = 72f,
            ),
        )
        GlossyFeatureOrb(
            label = "Manage\nRecords",
            icon = Icons.Default.Folder,
            brush = Brush.radialGradient(
                colors = listOf(GlossGreenA, GlossGreenB, GlossGreenC),
                center = Offset(36f, 30f),
                radius = 72f,
            ),
        )
        GlossyFeatureOrb(
            label = "Prescrip-\ntions",
            icon = Icons.Default.Description,
            brush = Brush.radialGradient(
                colors = listOf(GlossPurpleA, GlossPurpleB, GlossPurpleC),
                center = Offset(36f, 30f),
                radius = 72f,
            ),
        )
        GlossyFeatureOrb(
            label = "Health\nTracking",
            icon = Icons.Default.Favorite,
            brush = Brush.radialGradient(
                colors = listOf(GlossTealA, GlossTealB, GlossTealC),
                center = Offset(36f, 30f),
                radius = 72f,
            ),
        )
    }
}

@Composable
private fun GlossyFeatureOrb(
    label: String,
    icon: ImageVector,
    brush: Brush,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(76.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.35f),
                    spotColor = Color.Black.copy(alpha = 0.45f),
                )
                .clip(CircleShape)
                .background(brush),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color.White.copy(alpha = 0.42f), Color.Transparent, Color.Transparent),
                            start = Offset(0f, 0f),
                            end = Offset(90f, 90f),
                        ),
                    ),
            )
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(26.dp),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.88f),
            fontSize = 10.sp,
            lineHeight = 12.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
        )
    }
}
