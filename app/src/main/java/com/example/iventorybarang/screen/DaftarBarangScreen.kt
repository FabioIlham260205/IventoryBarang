package com.example.inventarisbarang.screen

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.inventarisbarang.data.BarangRepository
import com.example.inventarisbarang.model.Barang
import java.text.NumberFormat
import java.util.Locale

/**
 * DaftarBarangScreen menampilkan daftar semua barang dalam bentuk card.
 *
 * Analogi: Seperti etalase toko — semua barang ditampilkan dalam
 * kotak-kotak (card) yang bisa di-scroll. Setiap card menampilkan
 * foto, nama, kategori, harga, dan stok barang.
 *
 * Fitur:
 * - LazyColumn untuk menampilkan daftar barang (efisien memori)
 * - FAB (Floating Action Button) untuk menambah barang baru
 * - Klik card untuk melihat detail barang
 * - Search bar di TopAppBar
 * - Snackbar untuk notifikasi
 *
 * Komponen utama:
 * - Scaffold: kerangka layout (TopBar + Content + FAB + Snackbar)
 * - LazyColumn: daftar yang hanya me-render item yang terlihat di layar
 * - Card: wadah visual untuk setiap item barang
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarBarangScreen(navController: NavHostController) {
    // Context Android dibutuhkan untuk mengakses SharedPreferences
    val context = LocalContext.current

    // Inisialisasi repository untuk mengakses data
    val repository = remember { BarangRepository(context) }

    // State: daftar barang yang ditampilkan
    // mutableStateOf agar Compose me-recompose saat data berubah
    var daftarBarang by remember { mutableStateOf(repository.getSemuaBarang()) }

    // State: teks pencarian
    var searchQuery by remember { mutableStateOf("") }

    // State: apakah search bar sedang aktif/terbuka
    var isSearchActive by remember { mutableStateOf(false) }

    // SnackbarHostState untuk menampilkan pesan Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // Filter daftar barang berdasarkan query pencarian
    // Jika searchQuery kosong, tampilkan semua barang
    val filteredBarang = if (searchQuery.isEmpty()) {
        daftarBarang
    } else {
        daftarBarang.filter {
            it.nama.contains(searchQuery, ignoreCase = true) ||
                    it.kategori.contains(searchQuery, ignoreCase = true)
        }
    }

    // Refresh data setiap kali screen ini muncul (kembali dari screen lain)
    LaunchedEffect(navController.currentBackStackEntry) {
        // Cek apakah ada pesan notifikasi dari screen sebelumnya (Tambah/Edit/Hapus)
        val message = navController.currentBackStackEntry?.savedStateHandle?.get<String>("notifikasi")
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            // Hapus pesan agar tidak muncul berulang kali (saat rotasi layar)
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("notifikasi")
        }

        daftarBarang = repository.getSemuaBarang()
    }

    Scaffold(
        // === TOP APP BAR ===
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        // Mode pencarian: tampilkan TextField
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari barang...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    } else {
                        // Mode normal: tampilkan judul
                        Text("Inventaris Barang")
                    }
                },
                actions = {
                    // Tombol search di kanan atas
                    IconButton(onClick = {
                        isSearchActive = !isSearchActive
                        if (!isSearchActive) searchQuery = "" // Reset saat tutup
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Cari")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },

        // === FLOATING ACTION BUTTON ===
        // Tombol bulat di kanan bawah untuk menambah barang baru
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Navigasi ke screen Tambah Barang
                    navController.navigate("tambah")
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Tambah Barang",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },

        // === SNACKBAR HOST ===
        // Tempat Snackbar muncul di bagian bawah layar
        snackbarHost = { SnackbarHost(snackbarHostState) }

    ) { paddingValues ->
        // paddingValues = padding dari Scaffold (menghindari overlap dengan TopBar)

        if (filteredBarang.isEmpty()) {
            // === STATE KOSONG ===
            // Tampilkan pesan jika belum ada barang
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "📦",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isEmpty()) "Belum ada barang"
                        else "Barang tidak ditemukan",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Tap tombol + untuk menambah barang baru",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // === DAFTAR BARANG ===
            // LazyColumn = RecyclerView versi Compose
            // Hanya me-render item yang terlihat di layar (hemat memori)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // items() = loop untuk setiap barang dalam daftar
                // key = identifier unik agar Compose tahu item mana yang berubah
                items(
                    items = filteredBarang,
                    key = { it.id }
                ) { barang ->
                    BarangCard(
                        barang = barang,
                        onClick = {
                            // Navigasi ke Detail Barang dengan mengirim ID
                            navController.navigate("detail/${barang.id}")
                        }
                    )
                }
            }
        }
    }
}

/**
 * BarangCard adalah komponen card untuk satu item barang.
 *
 * Analogi: Seperti satu "kartu produk" di marketplace —
 * menampilkan thumbnail, nama, kategori, harga, dan stok.
 *
 * @param barang Data barang yang ditampilkan
 * @param onClick Aksi saat card diklik (navigasi ke detail)
 */
@Composable
fun BarangCard(barang: Barang, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),   // Seluruh card bisa diklik
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // === GAMBAR BARANG ===
            if (barang.gambarUri.isNotEmpty()) {
                // Jika ada gambar: tampilkan dari URI menggunakan Coil
                // Coil = library untuk load gambar secara asinkron
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Uri.parse(barang.gambarUri))
                        .crossfade(true)    // Efek transisi halus
                        .build(),
                    contentDescription = barang.nama,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop   // Gambar di-crop agar pas
                )
            } else {
                // Jika tidak ada gambar: tampilkan placeholder berwarna
                // dengan inisial nama barang
                Surface(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            // Ambil huruf pertama dari nama barang, kapital
                            text = barang.nama.take(1).uppercase(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // === INFO BARANG ===
            Column(modifier = Modifier.weight(1f)) {
                // Nama barang
                Text(
                    text = barang.nama,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis  // "..." jika terlalu panjang
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Kategori barang
                Text(
                    text = barang.kategori,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Harga dan stok dalam satu baris
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Harga diformat ke Rupiah: 245000.0 → "Rp245.000"
                    Text(
                        text = formatRupiah(barang.harga),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Badge stok dengan warna berbeda jika stok rendah
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (barang.stok <= 5)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Stok: ${barang.stok}",
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = if (barang.stok <= 5)
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Panah kanan sebagai indikator "bisa diklik"
            Text(
                text = "›",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Fungsi helper untuk memformat angka ke format Rupiah Indonesia.
 * Contoh: 245000.0 → "Rp245.000"
 *
 * NumberFormat.getCurrencyInstance(Locale("id", "ID")) menggunakan
 * aturan format mata uang Indonesia (titik sebagai pemisah ribuan).
 */
fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    format.maximumFractionDigits = 0  // Tanpa desimal
    return format.format(amount)
}

// === PREVIEW ===
// Preview untuk melihat tampilan di Android Studio tanpa menjalankan app
@Preview(showBackground = true, name = "Daftar Barang - Light")
@Composable
fun PreviewDaftarBarangScreen() {
    MaterialTheme {
        DaftarBarangScreen(navController = rememberNavController())
    }
}
