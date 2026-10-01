package com.example.pantrick.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.core.ui.theme.*
import com.example.pantrick.ui.component.HomeHeader

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    contentPadding: PaddingValues = PaddingValues(),
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ColorSoftCream)
            .padding(contentPadding),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. TOP APP BAR (Tombol kembali + HomeHeader terpadu)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali", tint = ColorDarkChocolate)
                }
                Box(modifier = Modifier.weight(1f)) {
                    HomeHeader(
                        userName = "Alex Morgan",
                        onNotificationClick = { /* TODO */ },
                        onProfileClick = { /* TODO */ }
                    )
                }
            }
        }

        // 2. HERO IMAGE & OVERLAYS
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(220.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(ColorPlaceholder.copy(alpha = 0.3f))
            ) {
                Surface(
                    color = ColorForestGreen.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(12.dp).align(Alignment.TopStart)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Icon(Icons.Rounded.Eco, contentDescription = null, tint = ColorSurfaceWhite, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cocok 90% • 5 dari 6 tersedia", color = ColorSurfaceWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomEnd).padding(12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(color = ColorWarmPeach.copy(alpha = 0.9f), shape = RoundedCornerShape(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = ColorDarkChocolate, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Klasik Nyaman", color = ColorDarkChocolate, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 3. TITLE & DESCRIPTION
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Pasta Krim Bawang Putih",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorDarkChocolate,
                    lineHeight = 28.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Krim emulsi lembut yang diresapi dengan irisan bawang putih panggang dan diakhiri dengan rempah segar yang harum.",
                    fontSize = 13.sp,
                    color = ColorTextSubtitleBrown,
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 4. STATS ROW (INFO CARDS)
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(modifier = Modifier.weight(1f), icon = Icons.Rounded.Timer, label = "WAKTU", value = "25 mnt")
                StatCard(modifier = Modifier.weight(1f), icon = Icons.Rounded.People, label = "PORSI", value = "2 mangkuk")
                StatCard(modifier = Modifier.weight(1f), icon = Icons.Rounded.BarChart, label = "USAHA", value = "Mudah")
                StatCard(modifier = Modifier.weight(1f), icon = Icons.Rounded.LocalFireDepartment, label = "KALORI", value = "420 kkal")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 5. IN YOUR PANTRY (BAHAN TERSEDIA)
        item {
            SectionHeader(title = "Di Dapurmu", badgeText = "5 Tersedia", badgeColor = Color(0xFFE8F2EA), badgeTextColor = ColorForestGreen, trailingText = "Siap digunakan")
            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                IngredientItem(name = "Bawang Putih", desc = "4 siung, cincang halus", tag = "Di Dapur", isAvailable = true)
                IngredientItem(name = "Minyak Zaitun (EVOO)", desc = "2 sdm, perasan pertama", tag = "Di Dapur", isAvailable = true)
                IngredientItem(name = "Pasta Penne", desc = "250g, kering", tag = "Di Dapur", isAvailable = true)
                IngredientItem(name = "Krim Kental", desc = "1/2 cangkir, organik segar", tag = "Di Kulkas", isAvailable = true, tagBgColor = Color(0xFFE8F2EA), tagTextColor = ColorForestGreen)
                IngredientItem(name = "Keju Parmesan", desc = "50g, parut segar", tag = "Di Kulkas", isAvailable = true, tagBgColor = Color(0xFFE8F2EA), tagTextColor = ColorForestGreen)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 6. MISSING ITEMS (BAHAN KURANG)
        item {
            SectionHeader(title = "Bahan Kurang", badgeText = "Butuh 1", badgeColor = Color(0xFFFFEAEA), badgeTextColor = ColorUrgencyRed)
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F2)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ColorWarmPeach)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(modifier = Modifier.size(24.dp).background(ColorWarmPeach, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = ColorUrgencyRed, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Peterseli Segar", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ColorDarkChocolate)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(color = ColorSurfaceWhite, shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, ColorDividerLine)) {
                                    Text("Stok Kosong", fontSize = 9.sp, color = ColorTextSubtitleBrown, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("1 ikat segar dibutuhkan untuk aroma herbal", fontSize = 12.sp, color = ColorTextSubtitleBrown)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 7. COOKING INSTRUCTIONS
        item {
            SectionHeader(title = "Instruksi Memasak", trailingText = "pratinjau 2 langkah")
            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InstructionStep(
                    stepNumber = "1",
                    title = "Rebus Pasta & Sisihkan Air",
                    desc = "Didihkan panci besar berisi air bergaram. Rebus pasta penne sekitar 9 menit hingga al dente. Sisihkan 1/2 cangkir air pasta yang kaya pati sebelum ditiriskan.",
                    tags = listOf(Pair(Icons.Rounded.Timer, "Timer 9 mnt"), Pair(Icons.Rounded.WaterDrop, "Pati mengentalkan saus"))
                )
                InstructionStep(
                    stepNumber = "2",
                    title = "Tumis Bumbu & Kurangi Krim",
                    desc = "Panaskan minyak zaitun di wajan lebar dengan api sedang-kecil. Tumis bawang putih cincang hingga harum (sekitar 60 detik). Aduk rata krim kental dan biarkan mendidih perlahan.",
                    tags = listOf(Pair(Icons.Rounded.LocalFireDepartment, "Tumis 1 mnt"))
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // 8. BOTTOM CTA BUTTON
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorDarkChocolate),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Rounded.OutdoorGrill, contentDescription = null, tint = ColorSurfaceWhite, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Masak & Kurangi Stok", color = ColorSurfaceWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CheckCircleOutline, contentDescription = null, tint = ColorForestGreen, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Secara otomatis mengurangi 5 bahan dari Inventaris", color = ColorTextSubtitleBrown, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, icon: ImageVector, label: String, value: String) {
    Card(
        modifier = modifier.defaultMinSize(minHeight = 84.dp),
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = ColorTextSubtitleBrown, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ColorTextSubtitleBrown, letterSpacing = 0.5.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorDarkChocolate, textAlign = TextAlign.Center, lineHeight = 14.sp)
        }
    }
}

@Composable
fun SectionHeader(title: String, badgeText: String? = null, badgeColor: Color = Color.Transparent, badgeTextColor: Color = Color.Transparent, trailingText: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ColorDarkChocolate)
            if (badgeText != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(color = badgeColor, shape = RoundedCornerShape(12.dp)) {
                    Text(text = badgeText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeTextColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }
        }
        if (trailingText != null) {
            Text(text = trailingText, fontSize = 11.sp, color = ColorTextSubtitleBrown)
        }
    }
}

@Composable
fun IngredientItem(name: String, desc: String, tag: String, isAvailable: Boolean, tagBgColor: Color = ColorSoftCream, tagTextColor: Color = ColorTextSubtitleBrown) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(24.dp).background(if(isAvailable) Color(0xFFE8F2EA) else ColorWarmPeach, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(
                        if(isAvailable) Icons.Rounded.Check else Icons.Rounded.Close,
                        contentDescription = null,
                        tint = if(isAvailable) ColorForestGreen else ColorUrgencyRed,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ColorDarkChocolate)
                    Text(desc, fontSize = 12.sp, color = ColorTextSubtitleBrown)
                }
            }
            Surface(color = tagBgColor, shape = RoundedCornerShape(12.dp)) {
                Text(tag, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tagTextColor, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable
fun InstructionStep(stepNumber: String, title: String, desc: String, tags: List<Pair<ImageVector, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(24.dp).background(ColorForestGreen, CircleShape), contentAlignment = Alignment.Center) {
                Text(stepNumber, color = ColorSurfaceWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ColorDarkChocolate)
                Spacer(modifier = Modifier.height(6.dp))
                Text(desc, fontSize = 12.sp, color = ColorTextSubtitleBrown, lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.forEach { tag ->
                        Surface(
                            color = if(tag.second.contains("mnt")) ColorSoftCream else Color(0xFFE8F2EA),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Icon(tag.first, contentDescription = null, tint = if(tag.second.contains("mnt")) ColorDarkChocolate else ColorForestGreen, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(tag.second, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = if(tag.second.contains("mnt")) ColorDarkChocolate else ColorForestGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}