// [Materi: Custom Component & Material 3 Navigation] Komponen navigasi bawah kustom Pantrick
package com.example.pantrick.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.ColorForestGreen
import com.example.pantrick.core.ui.theme.ColorWarmPeach
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.navigation.AddRoute
import com.example.pantrick.navigation.HomeRoute
import com.example.pantrick.navigation.PantryRoute
import com.example.pantrick.navigation.ProfileRoute
import com.example.pantrick.navigation.RecipesRoute

// [Materi: data class] Model data representasi tiap item navigasi bawah
data class PantrickNavigationItem(
    val route: Any,
    val label: String,
    val icon: ImageVector
)

// [Materi: Stateless Composable] Menerima route aktif dan callback lambda onItemClick (UDF)
@Composable
fun PantrickBottomBar(
    currentRoute: Any?,
    onItemClick: (Any) -> Unit,
    modifier: Modifier = Modifier
) {
    val homeLabel = stringResource(R.string.nav_home)
    val pantryLabel = stringResource(R.string.nav_pantry)
    val addLabel = stringResource(R.string.nav_add)
    val recipesLabel = stringResource(R.string.nav_recipes)
    val profileLabel = stringResource(R.string.nav_profile)

    val navItems = remember(homeLabel, pantryLabel, addLabel, recipesLabel, profileLabel) {
        listOf(
            PantrickNavigationItem(HomeRoute, homeLabel, Icons.Default.Home),
            PantrickNavigationItem(PantryRoute, pantryLabel, Icons.Default.Kitchen),
            PantrickNavigationItem(AddRoute(), addLabel, Icons.Default.Add),
            PantrickNavigationItem(RecipesRoute, recipesLabel, Icons.AutoMirrored.Filled.MenuBook),
            PantrickNavigationItem(ProfileRoute, profileLabel, Icons.Default.Person)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // [Materi: Surface & Shadow] Bar dasar dengan sudut atas melengkung
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    clip = false
                ),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    if (item.route is AddRoute) {
                        // Ruang kosong untuk tombol Add yang menonjol di tengah
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 28.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onItemClick(AddRoute()) }
                        ) {
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        val isSelected = currentRoute != null && currentRoute::class == item.route::class

                        // [Materi: Conditional UI Styling] Tab aktif memakai pill Warm Peach
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onItemClick(item.route) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                // Pill Peach untuk tab aktif
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(ColorWarmPeach)
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = ColorForestGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = item.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorForestGreen
                                    )
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // [Materi: Floating Action Element] Tombol bulat hijau "Tambah" menonjol di atas baris navigasi
        Box(
            modifier = Modifier
                .offset(y = (-24).dp)
                .size(54.dp)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(ColorForestGreen)
                .clickable { onItemClick(AddRoute()) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.btn_add_ingredient),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// [Materi: Preview Light]
@Preview(showBackground = true)
@Composable
fun PantrickBottomBarPreview() {
    PantrickTheme {
        PantrickBottomBar(
            currentRoute = HomeRoute,
            onItemClick = {}
        )
    }
}

// [Materi: Preview Dark]
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PantrickBottomBarDarkPreview() {
    PantrickTheme(darkTheme = true) {
        PantrickBottomBar(
            currentRoute = HomeRoute,
            onItemClick = {}
        )
    }
}
