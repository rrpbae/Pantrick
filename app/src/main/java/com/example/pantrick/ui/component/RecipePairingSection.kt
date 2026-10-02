// [Materi: Reusable Composable Component] Section rekomendasi resep cerdas (Pasangan Resep Pintar)
package com.example.pantrick.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.ColorDarkChocolate
import com.example.pantrick.core.ui.theme.ColorForestGreen
import com.example.pantrick.core.ui.theme.ColorUrgencyRed
import com.example.pantrick.core.ui.theme.ColorWarmPeach
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.data.model.Recipe
import com.example.pantrick.util.PantrickConstants

// [Materi: Stateless Composable] Menerima data resep hasil pencocokan, status favorit, dan callback aksi
@Composable
fun RecipePairingSection(
    recipe: Recipe,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onViewAllClick: () -> Unit = {},
    onCookNowClick: () -> Unit = {},
    onPlanMealClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PantrickConstants.HOME_HORIZONTAL_PADDING)
    ) {
        // [Materi: Row & Alignment] Header Section: Ikon Sparkle + Judul bold + "Lihat Semua ›"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = ColorForestGreen,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.recipe_section_title),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorDarkChocolate
                )
            }

            Text(
                text = stringResource(R.string.attention_view_all),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = ColorForestGreen,
                modifier = Modifier.clickable { onViewAllClick() }
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Subjudul Section
        Text(
            text = stringResource(R.string.recipe_section_sub),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // [Materi: Card & Vertical Layout] Kartu resep besar vertikal
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(PantrickConstants.CARD_CORNER_RADIUS),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // [Materi: Box & Scrim Overlay] Area visual foto lebar penuh
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Image(
                        painter = painterResource(id = recipe.imageRes),
                        contentDescription = recipe.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )

                    // Scrim gradient halus di bagian bawah foto
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0x99000000))
                                )
                            )
                    )

                    // [Materi: Row of Badges] Badge di pojok kiri atas foto (Match % dan Cocok dengan bahanmu)
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopStart),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Badge Match % (Hijau + Ikon Centang)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.recipe_match_badge, recipe.matchPercent),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        // Pill badge bahan yang digunakan
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = stringResource(R.string.recipe_uses_badge),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ColorDarkChocolate,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // [Materi: Row of Metadata] Overlay durasi & porsi di pojok kiri bawah foto
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.BottomStart),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.recipe_minutes, recipe.durationMinutes),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.recipe_servings, recipe.servings),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                // Area konten informasi resep di bawah foto
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Baris judul resep dan tombol toggle favorit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = recipe.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorDarkChocolate,
                            modifier = Modifier.weight(1f)
                        )

                        // [Materi: State-Driven Button] Tombol toggle favorit
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF7EBE1))
                                .clickable { onFavoriteToggle() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = stringResource(R.string.cd_favorite),
                                tint = if (isFavorite) ColorUrgencyRed else ColorDarkChocolate,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Deskripsi resep
                    Text(
                        text = recipe.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // [Materi: Surface Info Box] Kotak info bahan siap vs bahan kurang
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFBF1EB),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sisi kiri: bahan siap
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = ColorForestGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.recipe_ready_summary, recipe.readyCount, recipe.totalCount),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ColorForestGreen
                                )
                            }

                            // Sisi kanan: bahan yang kurang
                            Text(
                                text = stringResource(R.string.recipe_missing_summary, recipe.missingIngredient),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = ColorUrgencyRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // [Materi: Action Buttons Row] Tombol utama "Masak Sekarang" dan tombol kalender peach
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tombol aksi utama "Masak Sekarang" (Dark Chocolate)
                        Button(
                            onClick = onCookNowClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestaurantMenu,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.btn_cook_now),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiary
                            )
                        }

                        // Tombol kotak peach untuk kalender/meal planning
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = ColorWarmPeach,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { onPlanMealClick() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = stringResource(R.string.btn_plan_meal),
                                    tint = ColorDarkChocolate,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// [Materi: Preview Light] Pratinjau section RecipePairingSection mode terang
@Preview(showBackground = true)
@Composable
fun RecipePairingSectionPreview() {
    PantrickTheme {
        RecipePairingSection(
            recipe = Recipe(
                id = "1",
                title = "Pasta Krim Bawang Putih",
                description = "Pasta lezat saus krim gurih.",
                durationMinutes = 20,
                servings = 2,
                matchPercent = 75,
                usesLabel = "Cocok dengan bahanmu",
                readyCount = 3,
                totalCount = 4,
                missingIngredient = "Keju",
                imageRes = R.drawable.ic_placeholder_pasta
            ),
            isFavorite = false,
            onFavoriteToggle = {}
        )
    }
}
