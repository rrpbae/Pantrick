// [Materi: Compose Screen & Architecture] Layar Edit Bahan Makanan
// Menerapkan Pola Stateful/Stateless, BackHandler jika dirty, dan validasi keberadaan itemId
package com.example.pantrick.ui.screen

import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.data.model.FoodCategory
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.StorageLocation
import com.example.pantrick.data.model.User
import com.example.pantrick.ui.component.IngredientFormContent
import com.example.pantrick.ui.viewmodel.PantryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val TAG = "EditIngredient"

// [Materi: Stateful Composable] Mengelola penyuntingan bahan makanan dengan verifikasi ID dan deteksi perubahan
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIngredientScreen(
    itemId: String,
    currentUser: User?,
    pantryViewModel: PantryViewModel,
    onSaveSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    val items by pantryViewModel.items.collectAsState()
    val existingItem = remember(items, itemId) { items.find { it.id == itemId } }

    // [Materi: Defensive Navigation] Jika item tidak ditemukan di state, catat log peringatan dan kembali
    if (existingItem == null) {
        LaunchedEffect(itemId) {
            Log.w(TAG, "Item dengan ID '$itemId' tidak ditemukan dalam daftar pantry, kembali ke layar sebelumnya")
            onNavigateBack()
        }
        return
    }

    var name by rememberSaveable(existingItem.id) { mutableStateOf(existingItem.name) }
    var isNameTouched by rememberSaveable { mutableStateOf(false) }

    var category by rememberSaveable(existingItem.id) { mutableStateOf(existingItem.category) }
    var location by rememberSaveable(existingItem.id) { mutableStateOf(existingItem.location) }

    var quantity by rememberSaveable(existingItem.id) { mutableStateOf(existingItem.quantityLabel) }
    var isQuantityTouched by rememberSaveable { mutableStateOf(false) }

    var expiryEpochDay by rememberSaveable(existingItem.id) { mutableStateOf<Long?>(existingItem.expiryEpochDay) }
    var isManualExpiry by rememberSaveable(existingItem.id) { mutableStateOf(!existingItem.isExpiryEstimated) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var isSaving by rememberSaveable { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Validasi form
    val isNameValid = name.isNotBlank()
    val isQuantityValid = quantity.isNotBlank()
    val isExpiryValid = expiryEpochDay != null
    val isFormValid = isNameValid && isQuantityValid && isExpiryValid

    // [Materi: Form Dirty Tracking] Perubahan nilai dibanding data awal memicu konfirmasi pembatalan
    val formDirty = name != existingItem.name ||
            category != existingItem.category ||
            location != existingItem.location ||
            quantity != existingItem.quantityLabel ||
            expiryEpochDay != existingItem.expiryEpochDay ||
            isManualExpiry != !existingItem.isExpiryEstimated

    BackHandler(enabled = formDirty) {
        showDiscardDialog = true
    }

    // AlertDialog Konfirmasi Buang Perubahan
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.discard_dialog_title),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.discard_dialog_desc),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_discard),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }

    // DatePickerDialog Material 3
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = expiryEpochDay?.let {
            LocalDate.ofEpochDay(it).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        } ?: System.currentTimeMillis()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedLocalDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            expiryEpochDay = selectedLocalDate.toEpochDay()
                            isManualExpiry = true
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Pilih", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val idLocale = remember { Locale.forLanguageTag("id-ID") }
    val dateFormatter = remember(idLocale) { DateTimeFormatter.ofPattern("d MMM yyyy", idLocale) }
    val formattedExpiryDate = remember(expiryEpochDay) {
        expiryEpochDay?.let { LocalDate.ofEpochDay(it).format(dateFormatter) } ?: ""
    }

    val updateSuccessMsg = stringResource(R.string.update_item_success)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        IngredientFormContent(
            title = stringResource(R.string.edit_item_screen_title),
            subtitle = stringResource(R.string.edit_item_screen_desc),
            name = name,
            onNameChange = {
                name = it
                isNameTouched = true
            },
            isNameError = isNameTouched && !isNameValid,
            category = category,
            onCategoryChange = { newCat ->
                category = newCat
                if (!isManualExpiry) {
                    expiryEpochDay = com.example.pantrick.util.ExpiryEstimator.estimateExpiryDate(newCat, location, name).toEpochDay()
                }
            },
            location = location,
            onLocationChange = { newLoc ->
                location = newLoc
                if (!isManualExpiry) {
                    expiryEpochDay = com.example.pantrick.util.ExpiryEstimator.estimateExpiryDate(category, newLoc, name).toEpochDay()
                }
            },
            quantity = quantity,
            onQuantityChange = {
                quantity = it
                isQuantityTouched = true
            },
            isQuantityError = isQuantityTouched && !isQuantityValid,
            formattedExpiryDate = formattedExpiryDate,
            isExpiryEstimated = !isManualExpiry,
            onResetToEstimated = {
                isManualExpiry = false
                expiryEpochDay = com.example.pantrick.util.ExpiryEstimator.estimateExpiryDate(category, location, name).toEpochDay()
            },
            onSelectDateClick = { showDatePicker = true },
            isFormValid = isFormValid,
            isSaving = isSaving,
            submitButtonText = stringResource(R.string.btn_update_item),
            onSaveClick = {
                val email = currentUser?.email
                if (isFormValid && !isSaving && email != null) {
                    isSaving = true
                    coroutineScope.launch {
                        val updatedItem = existingItem.copy(
                            name = name.trim(),
                            location = location,
                            quantityLabel = quantity.trim(),
                            expiryEpochDay = expiryEpochDay ?: existingItem.expiryEpochDay,
                            category = category,
                            isExpiryEstimated = !isManualExpiry
                        )
                        pantryViewModel.updateItem(email, updatedItem)
                        snackbarHostState.showSnackbar(updateSuccessMsg)
                        delay(600)
                        onSaveSuccess()
                    }
                }
            },
            onNavigateBack = {
                if (formDirty) {
                    showDiscardDialog = true
                } else {
                    onNavigateBack()
                }
            },
            contentPadding = contentPadding
        )
    }
}

// [Materi: Preview Light]
@Preview(showBackground = true)
@Composable
fun EditIngredientScreenPreview() {
    PantrickTheme {
        IngredientFormContent(
            title = "Edit Bahan",
            subtitle = "Perbarui rincian bahan makanan di pantry kamu",
            name = "Susu Kotak",
            onNameChange = {},
            isNameError = false,
            category = FoodCategory.SUSU_TELUR,
            onCategoryChange = {},
            location = StorageLocation.KULKAS,
            onLocationChange = {},
            quantity = "1 Liter",
            onQuantityChange = {},
            isQuantityError = false,
            formattedExpiryDate = "14 Okt 2026",
            onSelectDateClick = {},
            isFormValid = true,
            isSaving = false,
            submitButtonText = "Simpan Perubahan",
            onSaveClick = {},
            onNavigateBack = {}
        )
    }
}
