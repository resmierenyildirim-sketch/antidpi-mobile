# Privacy Policy for AntiDPI Mobile

**Last updated:** September 2026

**AntiDPI Mobile** ("we", "our", or "the app") is developed as an open-source, privacy-preserving network utility. We are deeply committed to protecting your privacy and ensuring you have complete transparency regarding how the app operates.

---

## 1. No Data Collection or Logging
* **Zero Logging:** AntiDPI Mobile does **not** collect, store, log, analyze, or share any personal information, browsing history, visited URLs, IP addresses, DNS queries, or traffic data.
* **100% On-Device Processing:** All packet manipulation (TLS SNI desynchronization, packet fragmentation, and DoH routing) occurs strictly **locally on your device**. The app runs a local SOCKS5 proxy and a local TUN loopback interface. No user traffic is ever proxied to or routed through any external/remote VPN server operated by us.

---

## 2. Permissions and Their Purpose

To function as a local DPI evasion and privacy tool, AntiDPI Mobile requires specific Android permissions:

1. **`android.permission.BIND_VPN_SERVICE` (VpnService):**
   - **Purpose:** Used strictly to create a local loopback VPN interface (`tunFd`) on your device to intercept outgoing packets and pass them to the on-device circumvention core.
   - **Guarantee:** No traffic is routed to external servers; your data stays entirely on your device.
2. **`android.permission.INTERNET` & `ACCESS_NETWORK_STATE`:**
   - **Purpose:** Required to route packets to the internet and monitor connectivity status (Wi-Fi / Mobile data transitions).
3. **`android.permission.POST_NOTIFICATIONS`:**
   - **Purpose:** Required to show the persistent foreground service notification, showing the active status and allowing quick disconnection.
4. **`android.permission.QUERY_ALL_PACKAGES` (Split Tunneling):**
   - **Purpose:** Allows you to view your installed applications solely so you can choose which specific apps (such as banking or government services) bypass the local DPI engine. We never collect or transmit the list of your installed apps.

---

## 3. Third-Party Services
* The app contains **no advertising SDKs, no analytics tools, and no third-party trackers**.
* If you enable DNS-over-HTTPS (DoH), DNS queries are sent encrypted directly to the DNS provider you choose (e.g., Cloudflare, Google, Quad9, or AdGuard) in accordance with their respective privacy policies.

---

## 4. Open Source & Source Code
AntiDPI Mobile is fully open-source. Anyone can review, inspect, and verify the source code at:
[https://github.com/resmierenyildirim-sketch/antidpi-mobile](https://github.com/resmierenyildirim-sketch/antidpi-mobile)

---

## 5. Contact
If you have questions, feedback, or concerns regarding this Privacy Policy, you can open an issue on the GitHub repository or contact the developer via GitHub.

---
---

# AntiDPI Mobile Gizlilik Politikası (Türkçe)

**Son Güncelleme:** Eylül 2026

**AntiDPI Mobile**, kullanıcı gizliliğini en üst düzeyde tutmak amacıyla açık kaynaklı bir ağ güvenlik ve DPI koruma aracı olarak geliştirilmiştir.

### 1. Veri Toplama ve Kayıt (Sıfır Log)
* AntiDPI Mobile; kişisel bilgilerinizi, internet geçmişinizi, ziyaret ettiğiniz web sitelerini, DNS sorgularınızı veya IP adresinizi **kesinlikle toplamaz, kaydetmez, analiz etmez veya üçüncü taraflarla paylaşmaz**.
* Uygulama uzak bir VPN sunucusuna bağlanmaz. Tüm paket işlemleri **100% cihazınızın kendi içinde (yerel)** gerçekleşir.

### 2. İzinler ve Kullanım Amaçları
* **`BIND_VPN_SERVICE` (VpnService):** Cihaz içinde yerel bir tünel oluşturarak paket başlıklarını DPI sansür müdahalelerine karşı korumak için gereklidir. Verileriniz asla harici bir VPN sunucusuna yönlendirilmez.
* **`QUERY_ALL_PACKAGES`:** Sadece tünelden muaf tutmak istediğiniz (bankacılık vb.) uygulamaları seçebilmeniz için kullanılır. Yüklü uygulama listeniz hiçbir yere gönderilmez.
* **`POST_NOTIFICATIONS`:** Uygulamanın arka planda güvenle çalışabilmesi ve bildirim çubuğundan tek tıkla durdurulabilmesi için gereklidir.

### 3. Reklam ve Takipçi İçermez
Uygulamada hiçbir reklam kütüphanesi, analiz aracı veya kullanıcı takip kodu bulunmamaktadır.

### 4. İletişim ve Kaynak Kodu
Kaynak kodları [GitHub deposu](https://github.com/resmierenyildirim-sketch/antidpi-mobile) üzerinden herkese açıktır ve denetlenebilir.
