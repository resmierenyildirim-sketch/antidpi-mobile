package com.antidpi.mobile.data

data class DpiProfile(
    val id: String,
    val name: String,
    val description: String,
    val mode: Int, // 1: SPLIT, 2: DISORDER, 3: FAKE, 4: FULL_COMBO
    val splitOffset: Int = 2,
    val fakeTtl: Int = 4,
    val disorder: Boolean = false,
    val fakeData: Boolean = false,
    val fakeHost: String = "www.google.com"
) {
    fun toArgs(ip: String = "127.0.0.1", port: Int = 1080): Array<String> {
        val args = mutableListOf("ciadpi", "--ip", ip, "--port", port.toString(), "--auto=torst")
        when (id) {
            "preset_standard" -> {
                args.addAll(listOf("--split", "2"))
            }
            "preset_tt_so" -> {
                args.addAll(listOf("--split", "1", "--disorder", "1", "--fake", "-1", "--ttl", "4"))
            }
            "preset_turkcell_voda" -> {
                args.addAll(listOf("--split", "2", "--fake", "-1", "--ttl", "5"))
            }
            "preset_aggressive" -> {
                args.addAll(listOf("--split", "1", "--disorder", "3+s", "--fake", "-1", "--ttl", "3", "--mod-http=h,d"))
            }
            else -> {
                if (splitOffset > 0) {
                    args.addAll(listOf("--split", splitOffset.toString()))
                }
                if (disorder) {
                    args.addAll(listOf("--disorder", "1"))
                }
                if (fakeData) {
                    args.addAll(listOf("--fake", "-1", "--ttl", fakeTtl.toString()))
                }
            }
        }
        return args.toTypedArray()
    }

    companion object {
        val STANDARD = DpiProfile(
            id = "preset_standard",
            name = "Standart SNI Split",
            description = "TLS ClientHello paketini 2 parçaya böler. Çoğu operatör için en hızlı ve kararlı yöntemdir.",
            mode = 1,
            splitOffset = 2,
            fakeTtl = 4,
            disorder = false,
            fakeData = false
        )

        val TURK_TELEKOM_SUPERONLINE = DpiProfile(
            id = "preset_tt_so",
            name = "Türk Telekom & Superonline",
            description = "1. bayttan parçalama + Sırasız paket (Disorder) + Düşük TTL sahte paket. Katı DPI sistemleri için optimize edilmiştir.",
            mode = 4, // FULL_COMBO
            splitOffset = 1,
            fakeTtl = 4,
            disorder = true,
            fakeData = true,
            fakeHost = "www.google.com"
        )

        val TURKCELL_VODAFONE = DpiProfile(
            id = "preset_turkcell_voda",
            name = "Turkcell & Vodafone Mobil",
            description = "Mobil operatörlerin derin paket filtrelerine karşı sahte paket enjeksiyonu ve dinamik SNI parçalama.",
            mode = 3, // FAKE
            splitOffset = 2,
            fakeTtl = 5,
            disorder = true,
            fakeData = true,
            fakeHost = "cloudflare.com"
        )

        val AGGRESSIVE = DpiProfile(
            id = "preset_aggressive",
            name = "Maksimum Atlatma (Agresif)",
            description = "Tüm DPI atlatma tekniklerini (Split + Disorder + Fake TTL + HTTP Desync) aynı anda uygular.",
            mode = 4,
            splitOffset = 1,
            fakeTtl = 3,
            disorder = true,
            fakeData = true,
            fakeHost = "www.microsoft.com"
        )

        val PRESETS = listOf(
            STANDARD,
            TURK_TELEKOM_SUPERONLINE,
            TURKCELL_VODAFONE,
            AGGRESSIVE
        )

        fun findById(id: String): DpiProfile {
            return PRESETS.firstOrNull { it.id == id } ?: STANDARD
        }
    }
}
