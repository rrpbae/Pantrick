// [Materi: Reusable Composable Component] Kartu sapaan ramah pengguna Pantrick dengan teks dinamis
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.util.PantrickConstants

// [Materi: Stateless Composable] Menerima greeting, totalCount, dan expiringCount yang sudah dihitung di Stateful Screen
@Composable
fun GreetingCard(
    greeting: String,
    totalCount: Int,
    expiringCount: Int,
    modifier: Modifier = Modifier
) {
    val contentColor = MaterialTheme.colorScheme.onPrimary

    // [Materi: Conditional UI Text via when] Menyesuaikan subteks berdasarkan kondisi stok bahan pengguna
    val emptySubtext = stringResource(R.string.greeting_subtext_empty)
    val safeSubtext = stringResource(R.string.greeting_subtext_safe)
    val urgentPrefix = stringResource(R.string.greeting_subtext_urgent_prefix)
    val urgentSuffix = stringResource(R.string.greeting_subtext_urgent_suffix)

    // [Materi: buildAnnotatedString] Memberi gaya tebal dan garis bawah pada kata kunci jumlah bahan mendesak
    val annotatedSubtext = remember(totalCount, expiringCount, contentColor, emptySubtext, safeSubtext, urgentPrefix, urgentSuffix) {
        when {
            totalCount == 0 -> buildAnnotatedString { append(emptySubtext) }
            expiringCount > 0 -> buildAnnotatedString {
                append(urgentPrefix)
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        color = contentColor
                    )
                ) {
                    append("$expiringCount bahan")
                }
                append(urgentSuffix)
            }
            else -> buildAnnotatedString { append(safeSubtext) }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PantrickConstants.HOME_HORIZONTAL_PADDING),
        shape = RoundedCornerShape(PantrickConstants.CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // [Materi: Decorative Image Overlay] Logo samar transparan memakai token tema onPrimary
            Image(
                painter = painterResource(id = R.drawable.logo_pantrick),
                contentDescription = null,
                colorFilter = ColorFilter.tint(contentColor.copy(alpha = 0.08f)),
                modifier = Modifier
                    .size(110.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 10.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                // [Materi: Row & Badge Chip] Chip kecil "TEMAN DAPURMU"
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(contentColor.copy(alpha = 0.16f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Eco,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(R.string.greeting_chip),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // [Materi: Typography] Judul sapaan dinamis berdasarkan jam dan nama depan pengguna
                Text(
                    text = greeting,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // [Materi: AnnotatedString Text Rendering] Teks pendukung dinamis
                Text(
                    text = annotatedSubtext,
                    fontSize = 13.sp,
                    color = contentColor.copy(alpha = 0.9f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// [Materi: Preview Light] Pratinjau GreetingCard mode terang
@Preview(showBackground = true)
@Composable
fun GreetingCardPreview() {
    PantrickTheme {
        GreetingCard(
            greeting = "Selamat pagi, Budi!",
            totalCount = 3,
            expiringCount = 2
        )
    }
}

// [Materi: Preview Dark] Pratinjau GreetingCard mode gelap
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GreetingCardDarkPreview() {
    PantrickTheme(darkTheme = true) {
        GreetingCard(
            greeting = "Selamat malam, Budi!",
            totalCount = 0,
            expiringCount = 0
        )
    }
}
