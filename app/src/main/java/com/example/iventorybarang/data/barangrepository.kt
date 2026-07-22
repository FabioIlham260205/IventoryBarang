package com.example.inventarisbarang.data

import android.content.Context
import android.content.SharedPreferences
import com.example.inventarisbarang.model.Barang
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * BarangRepository mengelola penyimpanan data barang menggunakan SharedPreferences.
 *
 * Analogi: SharedPreferences itu seperti buku catatan kecil yang selalu
 * dibawa di saku — ringan, cepat diakses, dan datanya tetap ada
 * meskipun aplikasi ditutup. Cocok untuk data kecil-menengah.
 *
 * Cara kerja:
 * 1. Semua data Barang dikonversi ke format JSON menggunakan Gson
 * 2. JSON string disimpan di SharedPreferences dengan key "daftar_barang"
 * 3. Saat membaca, JSON string dikonversi kembali ke List<Barang>
 *
 * Catatan: Untuk data besar atau relasional, gunakan Room Database.
 * SharedPreferences cocok untuk penyimpanan sederhana seperti ini.
 */
class BarangRepository(context: Context) {

    // Inisialisasi SharedPreferences dengan nama file "inventaris_prefs"
    // MODE_PRIVATE = hanya aplikasi ini yang bisa mengakses file ini
    private val prefs: SharedPreferences =
        context.getSharedPreferences("inventaris_prefs", Context.MODE_PRIVATE)

    // Gson digunakan untuk konversi objek <-> JSON
    private val gson = Gson()

    // Key untuk menyimpan daftar barang di SharedPreferences
    private val KEY_DAFTAR = "daftar_barang"

    /**
     * Mengambil semua data barang dari SharedPreferences.
     *
     * Proses:
     * 1. Baca string JSON dari SharedPreferences
     * 2. Jika null (belum ada data), kembalikan list kosong
     * 3. Jika ada, konversi JSON string ke List<Barang> menggunakan Gson
     *
     * TypeToken diperlukan karena Gson perlu tahu tipe generic (List<Barang>)
     * saat melakukan deserialisasi dari JSON.
     */
    fun getSemuaBarang(): List<Barang> {
        val json = prefs.getString(KEY_DAFTAR, null)
        // Jika belum ada data tersimpan, kembalikan list kosong
        if (json == null) return emptyList()

        // TypeToken memberitahu Gson bahwa kita ingin List<Barang>
        val type = object : TypeToken<List<Barang>>() {}.type
        return gson.fromJson(json, type)
    }

    /**
     * Menyimpan seluruh daftar barang ke SharedPreferences.
     *
     * Proses:
     * 1. Konversi List<Barang> ke JSON string menggunakan Gson
     * 2. Simpan JSON string ke SharedPreferences
     *
     * apply() digunakan agar penyimpanan dilakukan secara asinkron
     * (tidak memblokir UI thread). Alternatifnya commit() yang sinkron.
     */
    private fun simpanSemuaBarang(daftar: List<Barang>) {
        val json = gson.toJson(daftar)
        prefs.edit().putString(KEY_DAFTAR, json).apply()
    }

    /**
     * Menambahkan satu barang baru ke daftar.
     *
     * Proses:
     * 1. Ambil daftar yang sudah ada
     * 2. Tambahkan barang baru ke daftar (menggunakan operator +)
     * 3. Simpan kembali seluruh daftar
     */
    fun tambahBarang(barang: Barang) {
        val daftar = getSemuaBarang().toMutableList()
        daftar.add(barang)
        simpanSemuaBarang(daftar)
    }

    /**
     * Mengambil satu barang berdasarkan ID.
     *
     * find {} akan mencari item pertama yang cocok dengan kondisi.
     * Jika tidak ditemukan, mengembalikan null.
     */
    fun getBarangById(id: Long): Barang? {
        return getSemuaBarang().find { it.id == id }
    }

    /**
     * Menghapus satu barang berdasarkan ID.
     *
     * filter {} membuat list baru yang hanya berisi item
     * yang ID-nya TIDAK sama dengan id yang ingin dihapus.
     */
    fun hapusBarang(id: Long) {
        val daftar = getSemuaBarang().filter { it.id != id }
        simpanSemuaBarang(daftar)
    }

    /**
     * Mengupdate data barang yang sudah ada.
     *
     * map {} memeriksa setiap item:
     * - Jika ID cocok → ganti dengan data baru (barang)
     * - Jika ID tidak cocok → biarkan apa adanya (it)
     */
    fun updateBarang(barang: Barang) {
        val daftar = getSemuaBarang().map {
            if (it.id == barang.id) barang else it
        }
        simpanSemuaBarang(daftar)
    }
}
