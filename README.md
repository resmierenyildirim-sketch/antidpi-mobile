# AntiDPI Mobile (GoodByeDPI for Android)

**AntiDPI Mobile**, bilgisayarlardaki GoodbyeDPI / Zapret mantığını mobil cihazlara (Android) taşıyan, uzak bir sunucuya ihtiyaç duymadan doğrudan cihaz üzerinde çalışan bir **DPI (Derin Paket İnceleme) Sansür Atlatıcı** uygulamasıdır.

---

## ⚡ Neden Normal VPN Değil?

| Özellik | Standart VPN (Express, Nord vb.) | AntiDPI Mobile (GoodbyeDPI) |
| :--- | :--- | :--- |
| **Uzak Sunucu** | Var (Almanya, Hollanda vb.) | **YOK (Tamamen Cihaz İçi)** |
| **İnternet Hızı** | Belirgin Hız Kaybı (%30 - %70) | **SIFIR Hız Kaybı (Maksimum Hat Hızı)** |
| **Gecikme / Ping** | Yüksek Ping (+40-100ms) | **Sıfır Ek Ping (Oyunlar için ideal)** |
| **Gizlilik Riski** | Trafik VPN sağlayıcısına gider | **Trafik Asla Cihazı Terk Etmez** |
| **Abonelik / Maliyet** | Aylık/Yıllık Ücretli | **%100 Ücretsiz & Açık Kaynak** |

---

## 🛠️ Nasıl Çalışır?

1. **Yerel Loopback VPN (`VpnService`):**
   Android'de rootsuz tüm cihaz trafiğini işletim sistemi seviyesinde yakalayabilmenin resmi yolu Android'in `VpnService` arayüzüdür. AntiDPI Mobile cihaz içinde yerel bir sanal ağ oluşturur, ancak trafiği dışarıdaki bir sunucuya değil, doğrudan cihazın içinde çalışan **C/C++ DPI manipülasyon çekirdeğine** iletir.
2. **TLS SNI Parçalama (Fragmentation):**
   Discord, YouTube veya Instagram gibi engellenen platformlara bağlanırken telefonunuz ilk olarak hedef alan adını içeren bir `TLS ClientHello` paketi gönderir. İnternet servis sağlayıcısının DPI filtreleri bu paketi inceler ve engeller. AntiDPI, bu paketi bayt düzeyinde 2 veya daha fazla parçaya bölerek gönderir. DPI sistemi parçalı paketi okuyamadığı için engeli fark edemez; hedef sunucu ise standart TCP protokolü gereği parçaları birleştirir.
3. **Sırasız Paket Gönderimi (TCP Disorder):**
   TLS el sıkışma paketinin 2. parçası 1. parçasından önce gönderilir. Sansür filtreleri ilk gelen parçada geçerli bir TLS başlığı göremediği için akışı onaylar.
4. **Düşük TTL Sahte Paketler (Fake Packet Injection):**
   DPI filtresine ulaşacak kadar (örn: 3-5 hop) yaşayan, ancak hedef sunucuya varmadan internet üzerinde zaman aşımına uğrayacak zararsız sahte paketler enjekte edilir.
5. **Dahili Şifreli DNS (DNS-over-HTTPS):**
   Cloudflare (1.1.1.1), Google (8.8.8.8), Quad9 ve AdGuard DoH desteği ile 5651 sayılı kanun engelleme sayfaları ve DNS zehirlemeleri tamamen saf dışı bırakılır.

---

## 🇹🇷 Türkiye Operatörlerine Özel Hazır Profiller

* **Standart SNI Split:** Çoğu ev interneti ve Wi-Fi için önerilen en hafif ve en hızlı profil (`--split 2`).
* **Türk Telekom & Superonline:** Katı DPI filtrelerine sahip sabit hatlar için optimize edilmiş agresif mod (`--split 1 --disorder --fake`).
* **Turkcell & Vodafone Mobil:** Hücresel veri (4.5G/5G) DPI filtrelerine özel düşük TTL ve sahte paket enjeksiyonu.
* **Maksimum Atlatma (Agresif Mod):** Tüm yöntemlerin (Split + Disorder + Fake + HTTP Desync) eş zamanlı çalıştığı en güçlü mod.
* **Özel Mod:** Split ofseti, TTL sınırı ve bayrakların elle ayarlanabildiği uzman modu.

---

## 📱 Uygulama Filtresi (Per-App Proxy / Hariç Tutma)

Bankacılık (İşCep, Garanti, Akbank vb.) veya devlet uygulamaları gibi VPN arayüzü açıkken güvenlik uyarısı veren uygulamaları tek dokunuşla tünelden hariç tutabilirsiniz. Hariç tutulan uygulamalar normal bağlantınız üzerinden engelsizce çalışır.

---

## 🚀 APK Derleme ve Kurulum Kılavuzu

### Yöntem 1: Android Studio ile (En Kolay)
1. [Android Studio](https://developer.android.com/studio)'yu açın.
2. **Open** seçeneğine tıklayıp bu proje klasörünü (`GoodByeDPI For Mobile`) seçin.
3. Gradle senkronizasyonunun tamamlanmasını bekleyin.
4. Telefonunuzu USB hata ayıklama moduyla bağlayın ve **Run 'app'** (Yeşil Oynat Butonu) düğmesine basın veya **Build > Build Bundle(s) / APK(s) > Build APK(s)** menüsünden `.apk` dosyasını oluşturun.

### Yöntem 2: Komut Satırından (Gradle Wrapper)
Bilgisayarınızda Java/JDK ve Android SDK kuruluysa terminalden şu komutu çalıştırarak doğrudan APK çıktısı alabilirsiniz:

```bash
# Debug APK Derleme
gradlew.bat assembleDebug
```

Derlenen APK şu dizinde yer alacaktır:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📂 Proje Dizin Yapısı

```
GoodByeDPI For Mobile/
├── app/
│   ├── build.gradle.kts                     # Android build ve NDK yapılandırması
│   ├── src/main/
│   │   ├── AndroidManifest.xml              # İzinler, VpnService tanımı
│   │   ├── cpp/                             # Native C99 DPI Motoru (libantidpi.so)
│   │   │   ├── CMakeLists.txt               # CMake derleme dosyası
│   │   │   ├── ciadpi/                      # DPI Atlatma Çekirdeği
│   │   │   │   ├── desync.c / desync.h      # SNI parçalama, fake TTL, disorder
│   │   │   │   ├── conntrack.c / conntrack.h# Anlık hız ve paket sayaçları
│   │   │   │   └── proxy.c / proxy.h        # Yerel SOCKS5 sunucusu
│   │   │   ├── tun2socks/                   # TUN arayüzü ile soket köprüsü
│   │   │   └── jni_bridge.c                 # JNI C <-> Kotlin köprüsü
│   │   ├── java/com/antidpi/mobile/
│   │   │   ├── AntiDpiApp.kt                # Application sınıfı
│   │   │   ├── core/                        # Servisler ve motor yöneticisi
│   │   │   │   ├── DpiVpnService.kt         # Android VpnService motoru
│   │   │   │   ├── DpiEngineManager.kt      # Canlı durum ve günlük yönetimi
│   │   │   │   ├── DnsOverHttps.kt          # DoH şifreli DNS istemcisi
│   │   │   │   └── NativeBridge.kt          # JNI arayüzü
│   │   │   ├── data/                        # Veri modelleri ve ayarlar
│   │   │   │   ├── DpiProfile.kt            # Operatör hazır şablonları
│   │   │   │   ├── PreferencesManager.kt    # Tercihlerin saklanması
│   │   │   │   └── NetworkStats.kt          # Hız ve trafik hesaplayıcı
│   │   │   └── ui/                          # Jetpack Compose Modern Arayüz
│   │   │       ├── MainActivity.kt          # Ana aktivite ve navigasyon
│   │   │       ├── theme/                   # Material 3 koyu tema
│   │   │       ├── screens/                 # Ekranlar (Home, Profiles, DNS, Apps, Logs)
│   │   │       └── components/              # Parlayan Güç Butonu, İstatistik Kartları
└── README.md
```
