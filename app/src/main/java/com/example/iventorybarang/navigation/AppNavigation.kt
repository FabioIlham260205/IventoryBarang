package com.example.inventarisbarang.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.inventarisbarang.screen.DaftarBarangScreen
import com.example.inventarisbarang.screen.DetailBarangScreen
import com.example.inventarisbarang.screen.TambahBarangScreen

/**
 * AppNavigation mengatur alur perpindahan antar screen di aplikasi.
 *
 * Analogi: NavHost itu seperti "peta jalan" aplikasi.
 * Setiap composable() adalah satu "halte" (screen) yang bisa dikunjungi.
 * NavController adalah "sopir" yang membawa user dari satu halte ke halte lain.
 *
 * Alur navigasi:
 *   [Daftar Barang] ──klik item──► [Detail Barang]
 *         │                              │
 *    klik FAB (+)                   klik Edit──► [Tambah/Edit Barang]
 *         │
 *         ▼
 *   [Tambah Barang]
 *
 * Route (rute) adalah "alamat" setiap screen:
 * - "daftar"          → Screen Daftar Barang
 * - "detail/{id}"     → Screen Detail (id = ID barang yang diklik)
 * - "tambah"          → Screen Tambah Barang Baru
 * - "edit/{id}"       → Screen Edit Barang (id = ID barang yang diedit)
 */
@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "daftar"    // Screen pertama yang muncul saat app dibuka
    ) {
        // Screen 1: Daftar Barang (halaman utama)
        composable("daftar") {
            DaftarBarangScreen(navController = navController)
        }

        // Screen 2: Detail Barang
        // {id} adalah parameter dinamis — nilainya berubah sesuai barang yang diklik
        // Contoh: navigate("detail/1717123456789") → id = 1717123456789
        composable(
            route = "detail/{id}",
            arguments = listOf(
                navArgument("id") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            // Ambil nilai id dari URL route
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            DetailBarangScreen(navController = navController, barangId = id)
        }

        // Screen 3a: Tambah Barang Baru
        composable("tambah") {
            TambahBarangScreen(navController = navController)
        }

        // Screen 3b: Edit Barang yang sudah ada
        // Menggunakan screen yang sama (TambahBarangScreen) tapi dengan mode edit
        composable(
            route = "edit/{id}",
            arguments = listOf(
                navArgument("id") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            TambahBarangScreen(navController = navController, editBarangId = id)
        }
    }
}
