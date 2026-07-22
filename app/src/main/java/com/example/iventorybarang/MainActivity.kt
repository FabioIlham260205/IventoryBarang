package com.example.inventarisbarang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.inventarisbarang.navigation.AppNavigation

/**
 * MainActivity adalah titik masuk (entry point) aplikasi Android.
 *
 * Analogi: MainActivity itu seperti "pintu depan" rumah —
 * semua tamu (user) masuk melalui pintu ini.
 * Di dalam, kita menyiapkan "ruangan" (NavController + NavHost)
 * agar tamu bisa berpindah dari satu ruangan ke ruangan lain.
 *
 * Alur:
 * 1. onCreate() dipanggil saat app pertama kali dibuka
 * 2. enableEdgeToEdge() membuat tampilan full-screen (tanpa status bar hitam)
 * 3. setContent {} menentukan tampilan Compose yang akan ditampilkan
 * 4. rememberNavController() membuat "sopir" navigasi
 * 5. AppNavigation() menampilkan screen sesuai rute aktif
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tampilan edge-to-edge (konten mengisi seluruh layar)
        enableEdgeToEdge()

        // setContent = menentukan UI yang ditampilkan menggunakan Jetpack Compose
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Buat NavController yang akan "mengingat" posisi navigasi
                    val navController = rememberNavController()

                    // Jalankan sistem navigasi dengan NavController ini
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}
