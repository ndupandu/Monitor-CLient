# Monitoring Client by Pandu (APK)

Aplikasi Android yang membuka dashboard web Monitoring Client (WebView). Login, sesi, dan semua fitur tetap
dijalankan oleh server dashboard; APK hanya "jendela" ke alamat server itu. APK dibuat otomatis oleh GitHub Actions.

## Syarat
Dashboard harus bisa dibuka dari HP lewat sebuah alamat, mis. `https://monitor.domainmu.com/` atau
`http://192.168.1.10:8080/` (WiFi yang sama / VPN). Di server, `DASHBOARD_HOST` harus `0.0.0.0` (bukan `127.0.0.1`).
Untuk akses dari internet pakai HTTPS (nginx/caddy) dan `DASHBOARD_TRUST_PROXY=1`.

## Langkah
1. Buat repository GitHub baru (disarankan **Private**, karena APK memuat alamat servermu) lalu unggah seluruh isi folder ini.
2. Di repository: **Settings -> Secrets and variables -> Actions -> tab Variables -> New repository variable**
   - Name: `APP_URL`  Value: alamat dashboard, mis. `https://monitor.domainmu.com/`
3. Buka tab **Actions -> Build APK -> Run workflow** (juga otomatis tiap push ke `main`).
4. Setelah hijau, ambil `MonitoringClient.apk` di **Releases** (atau Artifacts pada run tersebut), kirim ke HP, lalu pasang
   (izinkan "pasang dari sumber tidak dikenal").

Alamat berubah? Ubah variabel `APP_URL`, jalankan ulang workflow, pasang APK baru.

## Tanda tangan sendiri (disarankan)
Tanpa langkah ini APK ditandatangani kunci debug yang berganti-ganti, sehingga pembaruan kadang harus dengan
mencopot aplikasi lama dulu. Buat kunci sekali saja:

    keytool -genkeypair -v -keystore release.jks -alias monitoring -keyalg RSA -keysize 2048 -validity 10000
    base64 -w0 release.jks     # (macOS: base64 -i release.jks)

Simpan sebagai **Secrets** repository: `KEYSTORE_BASE64` (hasil base64), `KEYSTORE_PASSWORD`, `KEY_ALIAS` (monitoring),
`KEY_PASSWORD`. Simpan juga `release.jks` di tempat aman; jangan di-commit.

## Catatan
- Cookie login tersimpan, jadi tidak perlu login tiap buka aplikasi (sesi admin 12 jam, read 7 hari).
- Tombol Back HP mundur di dalam dashboard; tautan ke situs luar dibuka di browser.
- Berkas dari konsol diunduh ke folder Download.
- Bila dashboard sudah HTTPS, ubah `android:usesCleartextTraffic` di `AndroidManifest.xml` menjadi `false`.
