// [Materi: Compose Screen & Architecture] Layar Manajemen Bahan Pantry Pengguna
// Menerapkan LazyVerticalGrid, Header Span Penuh dengan Stable Key, Single Chip Filter, dan UDF
package com.example.pantrick.ui.screen

import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.ColorDarkChocolate
import com.example.pantrick.core.ui.theme.ColorForestGreen
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.data.model.FoodCategory
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.StorageLocation
import com.example.pantrick.data.model.User
import com.example.pantrick.ui.component.ExpiryAlertBanner
import com.example.pantrick.ui.component.LocationTabRow
import com.example.pantrick.ui.component.PantryEmptyStateView
import com.example.pantrick.ui.component.PantryEmptyType
import com.example.pantrick.ui.component.PantryFilterChipRow
import com.example.pantrick.ui.component.PantryItemCard
import com.example.pantrick.ui.component.PantrySearchBar
import com.example.pantrick.ui.component.PantryTopHeader
import com.example.pantrick.ui.viewmodel.PantryUiState
import com.example.pantrick.ui.viewmodel.PantryViewModel
import com.example.pantrick.util.BannerInfo
import com.example.pantrick.util.ChipFilter
import com.example.pantrick.util.PantrickConstants
import com.example.pantrick.util.PantryFilter
import com.example.pantrick.util.SortOrder
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val TAG = "PantryScreen"

// [Materi: Custom Saver] Menyimpan ChipFilter melintasi perubahan konfigurasi perangkat
private val ChipFilterSaver = Saver<ChipFilter, String>(
    save = { chip ->
        when (chip) {
            is ChipFilter.None -> "NONE"
            is ChipFilter.Expiring -> "EXPIRING"
            is ChipFilter.Category -> "CAT:${chip.category.name}"
        }
    },
    restore = { str ->
        when {
            str == "NONE" -> ChipFilter.None
            str == "EXPIRING" -> ChipFilter.Expiring
            str.startsWith("CAT:") -> {
                val catName = str.removePrefix("CAT:")
                val cat = FoodCategory.entries.find { it.name == catName } ?: FoodCategory.LAINNYA
                ChipFilter.Category(cat)
            }
            else -> ChipFilter.None
        }
    }
)

// [Materi: Stateful Composable] Mengelola state pantry, snackbar, dialog konfirmasi, dan navigasi
@Composable
fun PantryScreen(
    currentUser: User?,
    items: List<PantryItem>,
    pantryViewModel: PantryViewModel,
    onNavigateToAdd: (StorageLocation) -> Unit,
    onNavigateToEdit: (String) -> Unit = {},
    onPlanMealClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    val uiState by pantryViewModel.uiState.collectAsState()
    val todayEpochDay = remember { LocalDate.now().toEpochDay() }

    // [Materi: rememberSaveable per-field] Menyimpan tab lokasi, teks pencarian, filter, dan sort
    var selectedLocation by rememberSaveable { mutableStateOf(StorageLocation.KULKAS) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var chipFilter by rememberSaveable(stateSaver = ChipFilterSaver) { mutableStateOf(ChipFilter.None) }
    var sortOrder by rememberSaveable { mutableStateOf(SortOrder.EXPIRING_SOON) }

    // Dialog state
    var itemToDelete by remember { mutableStateOf<PantryItem?>(null) }
    var itemToMove by remember { mutableStateOf<PantryItem?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // [Materi: Dynamic Chip Resolution] Reset chip ke None jika item pada tab aktif sudah tidak ada
    LaunchedEffect(items, selectedLocation) {
        val resolved = PantryFilter.resolveChip(items, selectedLocation, chipFilter, todayEpochDay)
        if (resolved != chipFilter) {
            chipFilter = resolved
        }
    }

    // [Materi: BackHandler Berjenjang] Jika ada pencarian/filter aktif, bersihkan terlebih dahulu
    val isFilterOrSearchActive = searchQuery.isNotBlank() || chipFilter !is ChipFilter.None
    BackHandler(enabled = isFilterOrSearchActive) {
        searchQuery = ""
        chipFilter = ChipFilter.None
    }

    // [Materi: AlertDialog Konfirmasi Hapus Bahan]
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text(
                    text = stringResource(R.string.delete_item_dialog_title),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_item_dialog_desc, item.name),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val email = currentUser?.email
                        itemToDelete = null
                        pantryViewModel.deleteItem(email, item.id)
                        coroutineScope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            val result = snackbarHostState.showSnackbar(
                                message = "\"${item.name}\" dihapus",
                                actionLabel = "Urungkan",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                pantryViewModel.restoreItem(email, item)
                            }
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }

    // [Materi: AlertDialog Pindahkan Lokasi Bahan]
    itemToMove?.let { item ->
        var targetLocation by remember { mutableStateOf(item.location) }
        AlertDialog(
            onDismissRequest = { itemToMove = null },
            title = {
                Text(
                    text = "Pindahkan ${item.name}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    Text(
                        text = "Pilih lokasi penyimpanan baru:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    StorageLocation.entries.forEach { loc ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = loc == targetLocation,
                                onClick = { targetLocation = loc },
                                colors = RadioButtonDefaults.colors(selectedColor = ColorForestGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = loc.label, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val email = currentUser?.email
                        pantryViewModel.moveItem(email, item.id, targetLocation)
                        itemToMove = null
                    }
                ) {
                    Text(text = "Pindahkan", color = ColorForestGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToMove = null }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        )
    }

    // [Materi: Aksi Lonceng Notifikasi]
    val onBellClick: () -> Unit = {
        val urgentOverall = PantryFilter.expiringItems(items, todayEpochDay)
        if (urgentOverall.isNotEmpty()) {
            val firstLocation = urgentOverall.first().location
            selectedLocation = firstLocation
            chipFilter = ChipFilter.Expiring
        } else {
            coroutineScope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = "Tidak ada bahan yang perlu diperhatikan",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // [Materi: Aksi Centang Bahan (Tandai Terpakai)]
    val onMarkUsed: (PantryItem) -> Unit = { item ->
        val email = currentUser?.email
        pantryViewModel.deleteItem(email, item.id)
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = "\"${item.name}\" ditandai terpakai",
                actionLabel = "Urungkan",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                pantryViewModel.restoreItem(email, item)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            // [Materi: Sticky Bottom Action Button] Tombol tambah dinamis sesuai lokasi tab aktif
            Button(
                onClick = { onNavigateToAdd(selectedLocation) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorForestGreen,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PantrickConstants.HOME_HORIZONTAL_PADDING, vertical = 8.dp)
                    .height(48.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_add_to_location, selectedLocation.label),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        StatelessPantryContent(
            currentUser = currentUser,
            uiState = uiState,
            items = items,
            selectedLocation = selectedLocation,
            searchQuery = searchQuery,
            chipFilter = chipFilter,
            sortOrder = sortOrder,
            todayEpochDay = todayEpochDay,
            onLocationSelected = { selectedLocation = it },
            onSearchQueryChange = { searchQuery = it },
            onClearSearchQuery = { searchQuery = "" },
            onChipSelected = { chipFilter = it },
            onSortOrderSelected = { sortOrder = it },
            onResetFilters = {
                searchQuery = ""
                chipFilter = ChipFilter.None
            },
            onBellClick = onBellClick,
            onAvatarClick = onProfileClick,
            onMarkUsed = onMarkUsed,
            onEditClick = { onNavigateToEdit(it.id) },
            onMoveClick = { itemToMove = it },
            onDeleteClick = { itemToDelete = it },
            onPlanMealClick = onPlanMealClick,
            onRetryLoad = { pantryViewModel.retryLoad() },
            onAddNewItemClick = { onNavigateToAdd(selectedLocation) },
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + contentPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + contentPadding.calculateBottomPadding() + 8.dp
            )
        )
    }
}

// [Materi: Stateless Composable] UI murni layar Pantry berbasis LazyVerticalGrid dengan header berkunci stabil
@Composable
fun StatelessPantryContent(
    currentUser: User?,
    uiState: PantryUiState,
    items: List<PantryItem>,
    selectedLocation: StorageLocation,
    searchQuery: String,
    chipFilter: ChipFilter,
    sortOrder: SortOrder,
    todayEpochDay: Long,
    onLocationSelected: (StorageLocation) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearSearchQuery: () -> Unit,
    onChipSelected: (ChipFilter) -> Unit,
    onSortOrderSelected: (SortOrder) -> Unit,
    onResetFilters: () -> Unit,
    onBellClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onMarkUsed: (PantryItem) -> Unit,
    onEditClick: (PantryItem) -> Unit,
    onMoveClick: (PantryItem) -> Unit,
    onDeleteClick: (PantryItem) -> Unit,
    onPlanMealClick: () -> Unit,
    onRetryLoad: () -> Unit,
    onAddNewItemClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    // Perhitungan lokasi tab
    val kulkasCount = remember(items) { PantryFilter.countByLocation(items, StorageLocation.KULKAS) }
    val freezerCount = remember(items) { PantryFilter.countByLocation(items, StorageLocation.FREEZER) }
    val rakKeringCount = remember(items) { PantryFilter.countByLocation(items, StorageLocation.RAK_KERING) }

    // Perhitungan bahan mendesak keseluruhan & per lokasi aktif
    val urgentItemsOverall = remember(items, todayEpochDay) {
        PantryFilter.expiringItems(items, todayEpochDay)
    }
    val hasUrgentItemsOverall = urgentItemsOverall.isNotEmpty()

    val locationItems = remember(items, selectedLocation) {
        items.filter { it.location == selectedLocation }
    }
    val locationUrgentItems = remember(items, selectedLocation, todayEpochDay) {
        PantryFilter.expiringItems(items, selectedLocation, todayEpochDay)
    }
    val availableCategories = remember(items, selectedLocation) {
        PantryFilter.availableCategories(items, selectedLocation)
    }

    // Hasil filter dan pencarian
    val filteredItems = remember(items, selectedLocation, searchQuery, chipFilter, sortOrder, todayEpochDay) {
        PantryFilter.apply(items, selectedLocation, searchQuery, chipFilter, sortOrder, todayEpochDay)
    }

    // Banner info terstruktur
    val bannerInfo = remember(locationUrgentItems, todayEpochDay) {
        PantryFilter.buildBannerInfo(locationUrgentItems, todayEpochDay)
    }

    // Penentuan kondisi kosong
    val isPantryCompletelyEmpty = items.isEmpty()
    val isLocationTabEmpty = locationItems.isEmpty()
    val isSearchResultEmpty = filteredItems.isEmpty()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // [Materi: Stable Key Header 1] Top Header (Avatar, Judul, Lonceng)
        item(key = "header_top", span = { GridItemSpan(2) }) {
            PantryTopHeader(
                currentUser = currentUser,
                hasUrgentItems = hasUrgentItemsOverall,
                onBellClick = onBellClick,
                onAvatarClick = onAvatarClick
            )
        }

        // [Materi: Stable Key Header 2] Tab Lokasi (Kulkas, Freezer, Rak Kering)
        item(key = "header_tabs", span = { GridItemSpan(2) }) {
            LocationTabRow(
                selectedLocation = selectedLocation,
                kulkasCount = kulkasCount,
                freezerCount = freezerCount,
                rakKeringCount = rakKeringCount,
                onLocationSelected = onLocationSelected
            )
        }

        // [Materi: Stable Key Header 3] Kolom Pencarian
        item(key = "header_search", span = { GridItemSpan(2) }) {
            PantrySearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                activeLocation = selectedLocation,
                onClearQuery = onClearSearchQuery
            )
        }

        // [Materi: Stable Key Header 4] Barisan Chip Filter & Pengurutan
        item(key = "header_chips", span = { GridItemSpan(2) }) {
            PantryFilterChipRow(
                chipFilter = chipFilter,
                expiringCount = locationUrgentItems.size,
                availableCategories = availableCategories,
                selectedSortOrder = sortOrder,
                onChipSelected = onChipSelected,
                onSortOrderSelected = onSortOrderSelected
            )
        }

        // [Materi: Stable Key Header 5] Banner Peringatan Bahan Kedaluwarsa
        if (bannerInfo.totalCount > 0 && chipFilter !is ChipFilter.Category) {
            item(key = "header_banner", span = { GridItemSpan(2) }) {
                ExpiryAlertBanner(
                    bannerInfo = bannerInfo,
                    onPlanCookingClick = onPlanMealClick
                )
            }
        }

        // [Materi: Error & Empty States Handling]
        if (uiState is PantryUiState.Error) {
            item(key = "state_error", span = { GridItemSpan(2) }) {
                PantryEmptyStateView(
                    emptyType = PantryEmptyType.Error(uiState.message),
                    onPrimaryActionClick = onRetryLoad
                )
            }
        } else if (isPantryCompletelyEmpty) {
            item(key = "state_new_user", span = { GridItemSpan(2) }) {
                PantryEmptyStateView(
                    emptyType = PantryEmptyType.NewUser,
                    onPrimaryActionClick = onAddNewItemClick
                )
            }
        } else if (isLocationTabEmpty) {
            item(key = "state_empty_tab", span = { GridItemSpan(2) }) {
                PantryEmptyStateView(
                    emptyType = PantryEmptyType.EmptyLocation(selectedLocation),
                    onPrimaryActionClick = onAddNewItemClick
                )
            }
        } else if (isSearchResultEmpty) {
            item(key = "state_empty_search", span = { GridItemSpan(2) }) {
                PantryEmptyStateView(
                    emptyType = PantryEmptyType.EmptySearch(searchQuery.ifBlank { "Filter" }),
                    onPrimaryActionClick = onResetFilters
                )
            }
        } else {
            // [Materi: Grid Items] Kartu bahan makanan dengan key ID stabil
            items(
                items = filteredItems,
                key = { it.id },
                span = { GridItemSpan(1) }
            ) { item ->
                PantryItemCard(
                    item = item,
                    onMarkUsed = onMarkUsed,
                    onEditClick = onEditClick,
                    onMoveClick = onMoveClick,
                    onDeleteClick = onDeleteClick,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        // Ruang tambahan di bagian bawah agar tidak tertutup tombol sticky
        item(key = "bottom_spacer", span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ============================== PREVIEWS (5 KONDISI LIGHT & DARK) ==============================

// Sample test data
private val sampleUser = User(fullName = "Budi Santoso", email = "budi@contoh.com", passwordHash = "")
private val sampleItems = listOf(
    PantryItem("1", "Susu Sapi Segar", StorageLocation.KULKAS, "1 Liter", LocalDate.now().plusDays(1).toEpochDay(), FoodCategory.SUSU_TELUR),
    PantryItem("2", "Telur Ayam", StorageLocation.KULKAS, "10 Butir", LocalDate.now().plusDays(2).toEpochDay(), FoodCategory.SUSU_TELUR),
    PantryItem("3", "Bayam Hijau", StorageLocation.KULKAS, "1 Ikat", LocalDate.now().plusDays(5).toEpochDay(), FoodCategory.SAYUR_BUAH),
    PantryItem("4", "Daging Sapi", StorageLocation.FREEZER, "500 gr", LocalDate.now().plusDays(20).toEpochDay(), FoodCategory.DAGING_IKAN)
)

// 1. Kondisi Terisi + Banner (Light & Dark)
@Preview(name = "1. Terisi + Banner - Light", showBackground = true)
@Composable
fun PantryPreviewFilledLight() {
    PantrickTheme {
        StatelessPantryContent(
            currentUser = sampleUser,
            uiState = PantryUiState.Success(sampleItems),
            items = sampleItems,
            selectedLocation = StorageLocation.KULKAS,
            searchQuery = "",
            chipFilter = ChipFilter.None,
            sortOrder = SortOrder.EXPIRING_SOON,
            todayEpochDay = LocalDate.now().toEpochDay(),
            onLocationSelected = {},
            onSearchQueryChange = {},
            onClearSearchQuery = {},
            onChipSelected = {},
            onSortOrderSelected = {},
            onResetFilters = {},
            onBellClick = {},
            onAvatarClick = {},
            onMarkUsed = {},
            onEditClick = {},
            onMoveClick = {},
            onDeleteClick = {},
            onPlanMealClick = {},
            onRetryLoad = {},
            onAddNewItemClick = {},
            contentPadding = PaddingValues(0.dp)
        )
    }
}

@Preview(name = "1. Terisi + Banner - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PantryPreviewFilledDark() {
    PantrickTheme {
        StatelessPantryContent(
            currentUser = sampleUser,
            uiState = PantryUiState.Success(sampleItems),
            items = sampleItems,
            selectedLocation = StorageLocation.KULKAS,
            searchQuery = "",
            chipFilter = ChipFilter.None,
            sortOrder = SortOrder.EXPIRING_SOON,
            todayEpochDay = LocalDate.now().toEpochDay(),
            onLocationSelected = {},
            onSearchQueryChange = {},
            onClearSearchQuery = {},
            onChipSelected = {},
            onSortOrderSelected = {},
            onResetFilters = {},
            onBellClick = {},
            onAvatarClick = {},
            onMarkUsed = {},
            onEditClick = {},
            onMoveClick = {},
            onDeleteClick = {},
            onPlanMealClick = {},
            onRetryLoad = {},
            onAddNewItemClick = {},
            contentPadding = PaddingValues(0.dp)
        )
    }
}

// 2. Kondisi User Baru (Pantry Kosong Total)
@Preview(name = "2. User Baru - Light", showBackground = true)
@Composable
fun PantryPreviewNewUserLight() {
    PantrickTheme {
        StatelessPantryContent(
            currentUser = sampleUser,
            uiState = PantryUiState.Success(emptyList()),
            items = emptyList(),
            selectedLocation = StorageLocation.KULKAS,
            searchQuery = "",
            chipFilter = ChipFilter.None,
            sortOrder = SortOrder.EXPIRING_SOON,
            todayEpochDay = LocalDate.now().toEpochDay(),
            onLocationSelected = {},
            onSearchQueryChange = {},
            onClearSearchQuery = {},
            onChipSelected = {},
            onSortOrderSelected = {},
            onResetFilters = {},
            onBellClick = {},
            onAvatarClick = {},
            onMarkUsed = {},
            onEditClick = {},
            onMoveClick = {},
            onDeleteClick = {},
            onPlanMealClick = {},
            onRetryLoad = {},
            onAddNewItemClick = {},
            contentPadding = PaddingValues(0.dp)
        )
    }
}

// 3. Kondisi Tab Kosong (Rak Kering kosong)
@Preview(name = "3. Tab Kosong - Light", showBackground = true)
@Composable
fun PantryPreviewEmptyTabLight() {
    PantrickTheme {
        StatelessPantryContent(
            currentUser = sampleUser,
            uiState = PantryUiState.Success(sampleItems),
            items = sampleItems,
            selectedLocation = StorageLocation.RAK_KERING,
            searchQuery = "",
            chipFilter = ChipFilter.None,
            sortOrder = SortOrder.EXPIRING_SOON,
            todayEpochDay = LocalDate.now().toEpochDay(),
            onLocationSelected = {},
            onSearchQueryChange = {},
            onClearSearchQuery = {},
            onChipSelected = {},
            onSortOrderSelected = {},
            onResetFilters = {},
            onBellClick = {},
            onAvatarClick = {},
            onMarkUsed = {},
            onEditClick = {},
            onMoveClick = {},
            onDeleteClick = {},
            onPlanMealClick = {},
            onRetryLoad = {},
            onAddNewItemClick = {},
            contentPadding = PaddingValues(0.dp)
        )
    }
}

// 4. Kondisi Hasil Pencarian Kosong
@Preview(name = "4. Hasil Kosong - Light", showBackground = true)
@Composable
fun PantryPreviewEmptySearchLight() {
    PantrickTheme {
        StatelessPantryContent(
            currentUser = sampleUser,
            uiState = PantryUiState.Success(sampleItems),
            items = sampleItems,
            selectedLocation = StorageLocation.KULKAS,
            searchQuery = "Cokelat Belgian",
            chipFilter = ChipFilter.None,
            sortOrder = SortOrder.EXPIRING_SOON,
            todayEpochDay = LocalDate.now().toEpochDay(),
            onLocationSelected = {},
            onSearchQueryChange = {},
            onClearSearchQuery = {},
            onChipSelected = {},
            onSortOrderSelected = {},
            onResetFilters = {},
            onBellClick = {},
            onAvatarClick = {},
            onMarkUsed = {},
            onEditClick = {},
            onMoveClick = {},
            onDeleteClick = {},
            onPlanMealClick = {},
            onRetryLoad = {},
            onAddNewItemClick = {},
            contentPadding = PaddingValues(0.dp)
        )
    }
}

// 5. Kondisi Error (Gagal Baca JSON)
@Preview(name = "5. Error State - Light", showBackground = true)
@Composable
fun PantryPreviewErrorLight() {
    PantrickTheme {
        StatelessPantryContent(
            currentUser = sampleUser,
            uiState = PantryUiState.Error("Gagal memuat data pantry. Format penyimpanan tidak valid."),
            items = emptyList(),
            selectedLocation = StorageLocation.KULKAS,
            searchQuery = "",
            chipFilter = ChipFilter.None,
            sortOrder = SortOrder.EXPIRING_SOON,
            todayEpochDay = LocalDate.now().toEpochDay(),
            onLocationSelected = {},
            onSearchQueryChange = {},
            onClearSearchQuery = {},
            onChipSelected = {},
            onSortOrderSelected = {},
            onResetFilters = {},
            onBellClick = {},
            onAvatarClick = {},
            onMarkUsed = {},
            onEditClick = {},
            onMoveClick = {},
            onDeleteClick = {},
            onPlanMealClick = {},
            onRetryLoad = {},
            onAddNewItemClick = {},
            contentPadding = PaddingValues(0.dp)
        )
    }
}
