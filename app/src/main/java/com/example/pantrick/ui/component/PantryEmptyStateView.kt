// [Materi: Empty State & Reusable Composable] Tampilan berbagai kondisi kosong dan penanganan error pada Pantry
package com.example.pantrick.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.ColorDarkChocolate
import com.example.pantrick.core.ui.theme.ColorForestGreen
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.data.model.StorageLocation
import com.example.pantrick.util.PantrickConstants

// [Materi: Sealed Interface Type] Pemodelan tipe tampilan kondisi kosong / error pada Pantry
sealed interface PantryEmptyType {
    data object NewUser : PantryEmptyType
    data class EmptyLocation(val location: StorageLocation) : PantryEmptyType
    data class EmptySearch(val query: String) : PantryEmptyType
    data class Error(val message: String) : PantryEmptyType
}

// [Materi: Stateless Composable] Komponen visual kondisi kosong dengan aksi tombol relevan
@Composable
fun PantryEmptyStateView(
    emptyType: PantryEmptyType,
    onPrimaryActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PantrickConstants.HOME_HORIZONTAL_PADDING, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (emptyType) {
            is PantryEmptyType.NewUser -> {
                Image(
                    painter = painterResource(id = R.drawable.ic_empty_pantry),
                    contentDescription = null,
                    modifier = Modifier.size(130.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Pantry Kamu Masih Kosong",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorDarkChocolate
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Mulai kelola bahan makanan agar tidak ada yang terbuang sia-sia.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onPrimaryActionClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorForestGreen,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_add_first_ingredient),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            is PantryEmptyType.EmptyLocation -> {
                Image(
                    painter = painterResource(id = R.drawable.ic_empty_pantry),
                    contentDescription = null,
                    modifier = Modifier.size(110.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                val locationName = emptyType.location.label
                Text(
                    text = stringResource(R.string.empty_location_title, locationName),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorDarkChocolate
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.empty_location_desc, locationName),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onPrimaryActionClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorForestGreen,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_add_to_location, locationName),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            is PantryEmptyType.EmptySearch -> {
                Icon(
                    imageVector = Icons.Outlined.SearchOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.empty_search_title, emptyType.query),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorDarkChocolate
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.empty_search_desc),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))
                OutlinedButton(
                    onClick = onPrimaryActionClick,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_reset_search),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            is PantryEmptyType.Error -> {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Terjadi Kesalahan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = emptyType.message,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onPrimaryActionClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.height(42.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_retry),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// [Materi: Preview Light]
@Preview(showBackground = true)
@Composable
fun PantryEmptyStateViewNewUserPreview() {
    PantrickTheme {
        PantryEmptyStateView(
            emptyType = PantryEmptyType.NewUser,
            onPrimaryActionClick = {}
        )
    }
}

// [Materi: Preview Dark]
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PantryEmptyStateViewEmptySearchPreview() {
    PantrickTheme {
        PantryEmptyStateView(
            emptyType = PantryEmptyType.EmptySearch("Daging Wagyu"),
            onPrimaryActionClick = {}
        )
    }
}
