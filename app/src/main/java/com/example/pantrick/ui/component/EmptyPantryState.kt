// [Materi: Reusable Composable Component] Tampilan kondisi kosong (Empty State) pantry yang ramah dan memandu pengguna
package com.example.pantrick.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.ColorDarkChocolate
import com.example.pantrick.core.ui.theme.ColorWarmPeach
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.util.PantrickConstants

// [Materi: Stateless Composable] Komponen Empty State pantry dengan ilustrasi, teks pemandu, dan tombol aksi
@Composable
fun EmptyPantryState(
    onAddIngredientClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PantrickConstants.HOME_HORIZONTAL_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Ilustrasi Lembut Kulkas / Pantry Kosong
        Image(
            painter = painterResource(id = R.drawable.ic_empty_pantry),
            contentDescription = null,
            modifier = Modifier.size(130.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Judul (Rata Tengah)
        Text(
            text = stringResource(R.string.empty_pantry_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ColorDarkChocolate,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Deskripsi Informatif
        Text(
            text = stringResource(R.string.empty_pantry_desc),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Tombol Besar Lebar Penuh "+ Tambah Bahan" (Latar Dark Chocolate #4A2C21)
        Button(
            onClick = onAddIngredientClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(PantrickConstants.BUTTON_HEIGHT),
            shape = RoundedCornerShape(PantrickConstants.CORNER_RADIUS),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorDarkChocolate,
                contentColor = MaterialTheme.colorScheme.onTertiary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = stringResource(R.string.btn_add_ingredient),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Kartu Info Peach Halus
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(PantrickConstants.CARD_CORNER_RADIUS),
            colors = CardDefaults.cardColors(containerColor = ColorWarmPeach),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = ColorDarkChocolate,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.empty_help_title),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorDarkChocolate
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.empty_help_desc),
                        fontSize = 12.sp,
                        color = ColorDarkChocolate.copy(alpha = 0.8f),
                        lineHeight = 17.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// [Materi: Preview Light] Pratinjau EmptyPantryState mode terang
@Preview(showBackground = true)
@Composable
fun EmptyPantryStatePreview() {
    PantrickTheme {
        EmptyPantryState(onAddIngredientClick = {})
    }
}

// [Materi: Preview Dark] Pratinjau EmptyPantryState mode gelap
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EmptyPantryStateDarkPreview() {
    PantrickTheme(darkTheme = true) {
        EmptyPantryState(onAddIngredientClick = {})
    }
}
