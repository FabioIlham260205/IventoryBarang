package com.example.inventarisbarang.screen

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.inventarisbarang.data.BarangRepository
import com.example.inventarisbarang.model.Barang
import kotlinx.coroutines.launch

/**
 * DetailBarangScreen menampilkan informasi lengkap satu barang.
 *
 * Analogi: Seperti halaman produk di marketplace —
 * menampilkan foto besar, nama, kategori, harga, stok,
 * SKU, berat, status, dan deskripsi lengkap.
 *
 * Fitur:
 * - Gambar besar di atas
 * - Informasi detail dalam baris-baris rapi
 * - Tombol Edit → navigasi ke TambahBarangScreen mode edit
 * - Tombol Hapus → dialog konfirmasi → hapus → kembali ke daftar
 * - Snackbar untuk notifikasi
 *
 * @param navController Controller navigasi untuk berpindah screen
 * @param barangId ID barang yang ingin ditampilkan detailnya
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailBarangScreen(
    navController: NavHostController,
    barangId: Long
) {
    val context = LocalContext.current
    val repository = remember { BarangRepository(context) }

    // Ambil data barang berdasarkan ID
    var barang by remember { mutableStateOf(repository.getBarangById(barangId)) }

    // State untuk dialog konfirmasi hapus
    var showDeleteDialog by remember { mutableStateOf(false) }

    // SnackbarHostState dan CoroutineScope untuk Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Refresh data saat kembali dari screen edit
    LaunchedEffect(navController.currentBackStackEntry) {
        barang = repository.getBarangById(barangId)
    }

    // Jika barang tidak ditemukan (misalnya sudah dihapus)
    if (barang == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Barang tidak ditemukan")
        }
        return
    }

    // Variabel non-null untuk kemudahan akses
    val item = barang!!

    Scaffold(
        // === TOP APP BAR ===
        topBar = {
            TopAppBar(
                title = { Text("Detail Barang") },
                navigationIcon = {
                    // Tombol kembali (←)
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())  // Bisa di-scroll
        ) {
            // === GAMBAR BESAR ===
            if (item.gambarUri.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Uri.parse(item.gambarUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = item.nama,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder jika tidak ada gambar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = item.nama.take(1).uppercase(),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // === NAMA & KATEGORI ===
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = item.nama,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kategori: ${item.kategori}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // === BARIS-BARIS INFORMASI ===
            // Setiap DetailRow menampilkan label di kiri dan nilai di kanan
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow(label = "Harga", value = formatRupiah(item.harga))

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    DetailRow(label = "Stok Tersedia", value = "${item.stok} unit")

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    DetailRow(
                        label = "SKU",
                        value = item.sku.ifEmpty { "-" }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    DetailRow(
                        label = "Berat",
                        value = if (item.berat > 0) "${item.berat} kg" else "-"
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Status berdasarkan jumlah stok
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (item.stok > 0)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = if (item.stok > 0) "Tersedia" else "Habis",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                ),
                                color = if (item.stok > 0)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // === DESKRIPSI ===
            if (item.deskripsi.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Deskripsi",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.deskripsi,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // === TOMBOL EDIT & HAPUS ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tombol Edit — navigasi ke TambahBarangScreen mode edit
                OutlinedButton(
                    onClick = {
                        navController.navigate("edit/${item.id}")
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit")
                }

                // Tombol Hapus — tampilkan dialog konfirmasi
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hapus")
                }
            }
        }

        // === DIALOG KONFIRMASI HAPUS ===
        // Muncul saat tombol Hapus diklik
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Hapus Barang") },
                text = {
                    Text("Apakah Anda yakin ingin menghapus \"${item.nama}\"? " +
                            "Tindakan ini tidak dapat dibatalkan.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Hapus barang dari repository
                            repository.hapusBarang(item.id)
                            showDeleteDialog = false

                            // Tampilkan Snackbar lalu kembali ke daftar
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "${item.nama} berhasil dihapus",
                                    duration = SnackbarDuration.Short
                                )
                            }
                            // Sedikit delay agar user sempat melihat snackbar sebelum pindah screen
                            scope.launch {
                                kotlinx.coroutines.delay(800)
                                navController.popBackStack()
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Hapus")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

/**
 * DetailRow menampilkan satu baris informasi dengan label dan nilai.
 *
 * @param label  Teks label di sebelah kiri (contoh: "Harga")
 * @param value  Teks nilai di sebelah kanan (contoh: "Rp245.000")
 */
@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// === PREVIEW ===
@Preview(showBackground = true, name = "Detail Barang - Light")
@Composable
fun PreviewDetailBarangScreen() {
    MaterialTheme {
        DetailBarangScreen(
            navController = rememberNavController(),
            barangId = 0L
        )
    }
}
