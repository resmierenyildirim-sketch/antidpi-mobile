# 🧠 AntiDPI Mobile - Proje Hafızası ve Geliştirme Durumu (PROJECT_STATE.md)

Bu dosya, **AntiDPI Mobile** projesinin mevcut mimarisini, teknik kararlarını, kurulu ortamını, yayın kanallarını ve gelecekteki yol haritasını özetleyen kalıcı bir referans belgesidir. Gelecekteki tüm oturumlarda ve geliştirmelerde bu dosya baz alınmalıdır.

---

## 📌 1. Proje Özeti ve Temel Felsefe
* **Amaç:** Bilgisayarlardaki *GoodbyeDPI / Zapret* mantığını Android platformuna taşımak.
* **Farkı:** Sıradan VPN'ler gibi uzak sunucuya bağlanmaz. **Sıfır hız kaybı**, **sıfır ping artışı** ve **tamamen cihaz içi (%100 yerel) paket manipülasyonu** ile çalışır.
* **Lisans & Durum:** Açık kaynak (MIT Lisansı), tamamen ücretsiz ve reklamsız.

---

## 🏗️ 2. Teknik Mimari ve Kritik Çözümler

### A. Çekirdek Ağ Motoru (C NDK + lwIP + ByeDPI)
1. **lwIP Kullanıcı Alanı TCP/IP Yığını (`libhev-socks5-tunnel.so`):**
   - Android `VpnService` arayüzünün sağladığı sanal ağ arayüzünü (`tunFd`) dinler.
   - İlk prototipteki boş döngüden kaynaklı ağ düşmesi/kesilme sorunu, tam teşekküllü lwIP TCP/IP yığını entegre edilerek çözüldü.
   - Paketleri hiçbir hız kaybı olmadan yerel SOCKS5 proxy'sine (`127.0.0.1:1080`) aktarır.
2. **Resmi ByeDPI Çekirdeği (`libbyedpi.so` / `ciadpi`):**
   - Yerel `127.0.0.1:1080` portunda çalışır.
   - Giden TLS bağlantılarındaki `ClientHello` paketlerini tespit eder; SNI başlığını bayt seviyesinde parçalar (`--split 1` veya `--split 2`).
   - TCP Disorder (sırasız gönderim) ve sahte TTL paket enjeksiyonu (`--fake`, `--ttl 3-5`) ile DPI durum tablolarını aldatır.
3. **Desteklenen Mimari:** 4 temel Android mimarisinin tamamı derlenip `app/src/main/jniLibs` altına entegre edilmiştir (`arm64-v8a`, `armeabi-v7a`, `x86_64`, `x86`).

### B. Android Kotlin Servis ve Arayüz Katmanı
* **`DpiVpnService.kt`:** Android yerel VPN arayüzünü yönetir. MTU 8500 ve döngüsel çakışmaları önleyen `protect(fd)` soket korumasını uygular. Ön plan bildiriminde (Foreground Service) anlık durum ve hızlı durdurma butonu sunar.
* **`AppUpdater.kt`:** Uygulama açılışında GitHub Releases API'sini sorgular. Yeni sürüm geldiğinde kullanıcıya indirme ve tek tıkla kurma diyalogu gösterir (OTA Self-Updater).
* **`profiles.json` (Remote Config):** GitHub'daki `profiles.json` dosyasını uzaktan okur. Operatörler filtre değiştirdiğinde uygulama güncellemesi gerekmeksizin yeni profiller kullanıcılara ulaşır.
* **Modern Arayüz (Jetpack Compose Material 3):**
  - *Ana Sayfa:* Parlayan animasyonlu güç butonu, canlı download/upload hız sayaçları ve anlık ping.
  - *Profiller:* Türk Telekom, Superonline, Turkcell, Vodafone için optimize şablonlar ve özel ayarlar.
  - *Şifreli DNS (DoH):* Cloudflare, Google, Quad9, AdGuard şifreli DNS.
  - *Uygulama Ayracı (Per-App):* Banka ve yerel uygulamaları tünelden hariç tutma (Split Tunneling).
  - *Günlükler (Logs):* Canlı terminal akışı ve tek dokunuşla Discord / Roblox erişim testi.

---

## 🌐 3. Yayın ve Canlı Altyapı Bilgileri

| Kaynak | URL / Konum | Açıklama |
| :--- | :--- | :--- |
| **GitHub Deposu** | [github.com/resmierenyildirim-sketch/antidpi-mobile](https://github.com/resmierenyildirim-sketch/antidpi-mobile) | Ana kod deposu |
| **Web Sitesi (Landing Page)** | [resmierenyildirim-sketch.github.io/antidpi-mobile](https://resmierenyildirim-sketch.github.io/antidpi-mobile/) | GitHub Pages ile canlıda |
| **İlk Sürüm (Release)** | [v1.0.0 Release](https://github.com/resmierenyildirim-sketch/antidpi-mobile/releases/tag/v1.0.0) | Resmi v1.0.0 sürümü |
| **Doğrudan APK İndir** | [AntiDPI-Mobile.apk İndir](https://github.com/resmierenyildirim-sketch/antidpi-mobile/releases/download/v1.0.0/AntiDPI-Mobile.apk) | Güncel kurulabilir paket (~18.4 MB) |
| **Yerel APK Dosyası** | `C:\Users\ereny\Documents\GoodByeDPI For Mobile\AntiDPI-Mobile.apk` | Bilgisayardaki derlenmiş dosya |
| **Google Search Console Doğrulama** | `https://resmierenyildirim-sketch.github.io/antidpi-mobile/googlecb95e4c7f3e5f347.html` | Mülkiyet doğrulama dosyası canlıda |
| **Site Haritası (Sitemap)** | `https://resmierenyildirim-sketch.github.io/antidpi-mobile/sitemap.xml` | Google bot taraması için |
| **Bot Kuralları (Robots)** | `https://resmierenyildirim-sketch.github.io/antidpi-mobile/robots.txt` | Arama motoru indeks izinleri |
| **Sekme İkonu (Favicon)** | `docs/favicon.svg` | Kalkan + Şimşek neon logo |
| **Sosyal Medya Kapak Görseli** | `docs/og-banner.jpg` | WhatsApp/Twitter paylaşımları için |

---

## 💻 4. Geliştirici Ortamı (Toolchain)
* **İşletim Sistemi:** Windows
* **JDK:** Eclipse Temurin 17 (`C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot`)
* **Android SDK:** `C:\Users\ereny\AppData\Local\Android\Sdk`
  - `platforms;android-34` (Android 14)
  - `build-tools;34.0.0`
  - `ndk;25.1.8937393`
  - `cmake;3.22.1`
* **Gradle:** 8.7 (Gradle Wrapper)
* **GitHub CLI:** `gh` kurulu ve `resmierenyildirim-sketch` kullanıcısı ile yetkilendirilmiş.

---

## ⚡ 5. Sık Kullanılan Komutlar ve İş Akışları

### A. Yeni APK Derlemek İçin:
```powershell
.\gradlew.bat assembleDebug
```
*Çıktı konumu:* `app\build\outputs\apk\debug\app-debug.apk`

### B. Yeni Bir Güncelleme (v1.0.1 vb.) Yayınlamak İçin:
1. `app/build.gradle.kts` içinde `versionCode` (örn: `2`) ve `versionName` (örn: `"1.0.1"`) artırın.
2. APK'yı derleyin:
   ```powershell
   .\gradlew.bat assembleDebug
   Copy-Item "app\build\outputs\apk\debug\app-debug.apk" -Destination "AntiDPI-Mobile.apk" -Force
   ```
3. GitHub'a commit atıp yeni Release oluşturun:
   ```powershell
   git commit -am "chore: release v1.0.1"
   git push origin main
   gh release create v1.0.1 AntiDPI-Mobile.apk --title "AntiDPI Mobile v1.0.1" --notes "Yeni özellikler ve optimizasyonlar."
   ```
   *(Kullanıcıların telefonları açılışta bu güncellemeyi otomatik algılar!)*

### C. Web Sitesini Güncellemek İçin:
`docs/index.html` dosyasını düzenleyip commit ve push yapmak yeterlidir; GitHub Pages 30 saniye içinde otomatik canlıya alır:
```powershell
git add docs/
git commit -m "docs: web sitesi guncellendi"
git push origin main
```

### D. Operatör Kurallarını Uzaktan Güncellemek İçin:
Uygulamayı yeniden derlemeye gerek yoktur. `profiles.json` dosyasını düzenleyip pushlamak yeterlidir:
```powershell
git commit -am "feat: superonline yeni filtre atlatma kurali eklendi"
git push origin main
```

---

## 🔮 6. Gelecek Geliştirme Yol Haritası (Roadmap)
1. **F-Droid Başvurusu:** Açık kaynak resmi Android mağazasında yer almak için metadata hazırlanması.
2. **Hızlı Ayarlar Kutucuğu (Quick Settings Tile):** Android üst bildirim panelinden tek tıkla AntiDPI açıp kapama.
3. **HTTP/3 (QUIC) Bypass:** UDP tabanlı yeni nesil sansür filtreleri için özel atlatma mekanizmaları.
4. **Çoklu Dil Desteği:** Ayarlar ekranından İngilizce / Türkçe dil seçimi.
5. **Otomatik En İyi Profil Seçimi:** Uygulama açıldığında bağlantı testi yapıp kullanıcı için en hızlı çalışan operatör profilini otomatik önerme.
