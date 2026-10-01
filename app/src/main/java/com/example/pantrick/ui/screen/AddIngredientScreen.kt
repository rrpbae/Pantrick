// [Materi: Compose Screen & Architecture] Layar Tambah Bahan Baru
// Menerapkan Pola Stateful/Stateless, BackHandler konfirmasi buang perubahan, dan IngredientFormContent
package com.example.pantrick.ui.screen

import android.content.res.Configuration
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
import java.util.UUID

// [Materi: Stateful Composable] Mengelola form state penambahan bahan baru, validasi, dan dialog konfirmasi batal
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientScreen(
    currentUser: User?,
    pantryViewModel: PantryViewModel,
    initialLocation: StorageLocation = StorageLocation.KULKAS,
    onSaveSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable { mutableStateOf("") }
    var isNameTouched by rememberSaveable { mutableStateOf(false) }

    var category by rememberSaveable { mutableStateOf(FoodCategory.LAINNYA) }
    var location by rememberSaveable { mutableStateOf(initialLocation) }

    var quantity by rememberSaveable { mutableStateOf("") }
    var isQuantityTouched by rememberSaveable { mutableStateOf(false) }

    var expiryEpochDay by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var isSaving by rememberSaveable { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // [Materi: Validasi Form Lokal]
    val isNameValid = name.isNotBlank()
    val isQuantityValid = quantity.isNotBlank()
    val isExpiryValid = expiryEpochDay != null
    val isFormValid = isNameValid && isQuantityValid && isExpiryValid

    // [Materi: Form Dirty Tracking] Perubahan belum tersimpan memicu BackHandler
    val formDirty = name.isNotBlank() || quantity.isNotBlank() || expiryEpochDay != null

    BackHandler(enabled = formDirty) {
        showDiscardDialog = true
    }

    // [Materi: AlertDialog Konfirmasi Buang Perubahan]
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

    // [Materi: Material 3 DatePickerDialog]
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = expiryEpochDay?.let {
            LocalDate.ofEpochDay(it).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
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
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            expiryEpochDay = selectedLocalDate.toEpochDay()
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

    // [Materi: Formatter Tanggal Indonesia]
    val idLocale = remember { Locale.forLanguageTag("id-ID") }
    val dateFormatter = remember(idLocale) { DateTimeFormatter.ofPattern("d MMM yyyy", idLocale) }
    val formattedExpiryDate = remember(expiryEpochDay) {
        expiryEpochDay?.let { LocalDate.ofEpochDay(it).format(dateFormatter) } ?: ""
    }

    val addSuccessMsg = stringResource(R.string.add_item_success)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        IngredientFormContent(
            title = stringResource(R.string.add_item_screen_title),
            subtitle = stringResource(R.string.add_item_screen_desc),
            name = name,
            onNameChange = {
                name = it
                isNameTouched = true
            },
            isNameError = isNameTouched && !isNameValid,
            category = category,
            onCategoryChange = { category = it },
            location = location,
            onLocationChange = { location = it },
            quantity = quantity,
            onQuantityChange = {
                quantity = it
                isQuantityTouched = true
            },
            isQuantityError = isQuantityTouched && !isQuantityValid,
            formattedExpiryDate = formattedExpiryDate,
            onSelectDateClick = { showDatePicker = true },
            isFormValid = isFormValid,
            isSaving = isSaving,
            submitButtonText = stringResource(R.string.btn_save_item),
            onSaveClick = {
                val email = currentUser?.email
                if (isFormValid && !isSaving && email != null) {
                    isSaving = true
                    coroutineScope.launch {
                        val newItem = PantryItem(
                            id = UUID.randomUUID().toString(),
                            name = name.trim(),
                            location = location,
                            quantityLabel = quantity.trim(),
                            expiryEpochDay = expiryEpochDay ?: LocalDate.now().toEpochDay(),
                            category = category
                        )
                        pantryViewModel.addItem(email, newItem)
                        snackbarHostState.showSnackbar(addSuccessMsg)
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
fun AddIngredientScreenPreview() {
    PantrickTheme {
        IngredientFormContent(
            title = "Tambah Bahan",
            subtitle = "Catat bahan makanan baru ke dalam pantry kamu",
            name = "",
            onNameChange = {},
            isNameError = false,
            category = FoodCategory.LAINNYA,
            onCategoryChange = {},
            location = StorageLocation.KULKAS,
            onLocationChange = {},
            quantity = "",
            onQuantityChange = {},
            isQuantityError = false,
            formattedExpiryDate = "",
            onSelectDateClick = {},
            isFormValid = false,
            isSaving = false,
            submitButtonText = "Simpan Bahan",
            onSaveClick = {},
            onNavigateBack = {}
        )
    }
}
