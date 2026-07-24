package com.example.inventarisbarang.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
 * TambahBarangScreen digunakan untuk menambah barang baru ATAU mengedit barang yang sudah ada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahBarangScreen(
    navController: NavHostController,
    editBarangId: Long? = null
) {
    val context = LocalContext.current
    val repository = remember { BarangRepository(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isEditMode = editBarangId != null
    val existingBarang = if (isEditMode) {
        remember { repository.getBarangById(editBarangId!!) }
    } else null

    var nama by remember { mutableStateOf(existingBarang?.nama ?: "") }
    var kategori by remember { mutableStateOf(existingBarang?.kategori ?: "") }
    var harga by remember { mutableStateOf(
        if (existingBarang != null && existingBarang.harga > 0)
            existingBarang.harga.toLong().toString()
        else ""
    ) }
    var stok by remember { mutableStateOf(
        if (existingBarang != null && existingBarang.stok > 0)
            existingBarang.stok.toString()
        else ""
    ) }
    var sku by remember { mutableStateOf(existingBarang?.sku ?: "") }
    var berat by remember { mutableStateOf(
        if (existingBarang != null && existingBarang.berat > 0)
            existingBarang.berat.toString()
        else ""
    ) }
    var deskripsi by remember { mutableStateOf(existingBarang?.deskripsi ?: "") }
    var gambarUri by remember { mutableStateOf(existingBarang?.gambarUri ?: "") }

    var expandedKategori by remember { mutableStateOf(false) }

    val daftarKategori = listOf(
        "Elektronik", "Fashion", "Aksesoris",
        "Alat Tulis", "Makanan", "Minuman",
        "Peralatan Rumah", "Olahraga", "Lainnya"
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Ignore
            }
            gambarUri = it.toString()
        }
    }

    fun simpanBarang() {
        if (nama.isBlank() || kategori.isBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Nama dan Kategori wajib diisi!",
                    duration = SnackbarDuration.Short
                )
            }
            return
        }

        val barangBaru = Barang(
            id = existingBarang?.id ?: System.currentTimeMillis(),
            nama = nama.trim(),
            kategori = kategori,
            harga = harga.toDoubleOrNull() ?: 0.0,
            stok = stok.toIntOrNull() ?: 0,
            sku = sku.trim(),
            berat = berat.toDoubleOrNull() ?: 0.0,
            deskripsi = deskripsi.trim(),
            gambarUri = gambarUri
        )

        if (isEditMode) {
            repository.updateBarang(barangBaru)
        } else {
            repository.tambahBarang(barangBaru)
        }

        // Kirim pesan ke screen sebelumnya (DaftarBarang)
        navController.previousBackStackEntry?.savedStateHandle?.set(
            "notifikasi",
            if (isEditMode) "Barang berhasil diperbarui" else "Barang berhasil ditambahkan"
        )
        
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "Edit Barang" else "Tambah Barang")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { simpanBarang() }) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Simpan"
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (gambarUri.isNotEmpty()) 180.dp else 120.dp)
                    .clickable {
                        imagePickerLauncher.launch("image/*")
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                if (gambarUri.isNotEmpty()) {
                    Box {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(Uri.parse(gambarUri))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Gambar barang",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(8.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                        ) {
                            Text(
                                text = "Tap untuk ganti gambar",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 4.dp
                                )
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🖼", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap untuk upload gambar",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Nama Barang *",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = nama,
                onValueChange = { nama = it },
                placeholder = { Text("Masukkan nama barang") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Kategori *",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = expandedKategori,
                onExpandedChange = { expandedKategori = !expandedKategori }
            ) {
                OutlinedTextField(
                    value = kategori,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Pilih kategori") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKategori)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedKategori,
                    onDismissRequest = { expandedKategori = false }
                ) {
                    daftarKategori.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                kategori = item
                                expandedKategori = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Harga (Rp) *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = harga,
                        onValueChange = { harga = it },
                        placeholder = { Text("0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Stok *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = stok,
                        onValueChange = { stok = it },
                        placeholder = { Text("0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SKU",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        placeholder = { Text("Kode SKU") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Berat (kg)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = berat,
                        onValueChange = { berat = it },
                        placeholder = { Text("0.0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Deskripsi",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = deskripsi,
                onValueChange = { deskripsi = it },
                placeholder = { Text("Tulis deskripsi barang...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { simpanBarang() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isEditMode) "Perbarui Barang" else "Simpan Barang",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, name = "Tambah Barang - Light")
@Composable
fun PreviewTambahBarangScreen() {
    MaterialTheme {
        TambahBarangScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "Edit Barang - Light")
@Composable
fun PreviewEditBarangScreen() {
    MaterialTheme {
        TambahBarangScreen(
            navController = rememberNavController(),
            editBarangId = 1L
        )
    }
}
