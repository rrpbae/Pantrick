// [Materi: SearchBar & TextField Styling] Kolom pencarian bahan makanan dengan tombol pembersih (X)
package com.example.pantrick.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.data.model.StorageLocation
import com.example.pantrick.util.PantrickConstants

// [Materi: Stateless Composable] Input pencarian dengan placeholder dinamis sesuai lokasi dan tombol bersihkan (X)
@Composable
fun PantrySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    activeLocation: StorageLocation,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val placeholderText = when (activeLocation) {
        StorageLocation.KULKAS -> stringResource(R.string.search_placeholder_kulkas)
        StorageLocation.FREEZER -> stringResource(R.string.search_placeholder_freezer)
    }

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = placeholderText,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = onClearQuery) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Bersihkan pencarian",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PantrickConstants.HOME_HORIZONTAL_PADDING)
    )
}

// [Materi: Preview Light]
@Preview(showBackground = true)
@Composable
fun PantrySearchBarLightPreview() {
    PantrickTheme {
        PantrySearchBar(
            query = "Susu",
            onQueryChange = {},
            activeLocation = StorageLocation.KULKAS,
            onClearQuery = {}
        )
    }
}

// [Materi: Preview Dark]
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PantrySearchBarDarkPreview() {
    PantrickTheme {
        PantrySearchBar(
            query = "",
            onQueryChange = {},
            activeLocation = StorageLocation.FREEZER,
            onClearQuery = {}
        )
    }
}
