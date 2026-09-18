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
        // High-performance engine flags:
        // - 64KB buffer for maximum throughput without stutter
        // - 1024 max connections for concurrent multiplexing
        // - drop-sack to prevent DPI middlebox packet reassembly
        // - Immediate desync without --auto=torst (eliminates 3-5s initial timeout freeze)
        val args = mutableListOf(
            "ciadpi",
            "--ip", ip,
            "--port", port.toString(),
            "--max-conn", "1024",
            "--buf-size", "65536",
            "--drop-sack"
        )
        when (id) {
            "preset_standard" -> {
                // Flash-fast direct SNI split: splits precisely at the SNI hostname, zero delay
                args.addAll(listOf("--split", "1+s"))
            }
            "preset_tt_so" -> {
                // Türk Telekom & Superonline: Out-of-order SNI delivery (disorder) + split
                // Bypasses strict DPI state machines cleanly with zero packet drops
                args.addAll(listOf("--split", "1+s", "--disorder", "1+s"))
            }
            "preset_turkcell_voda" -> {
                // Turkcell & Vodafone Mobile: High-speed cellular profile
                args.addAll(listOf("--split", "2+s", "--disorder", "1+s"))
            }
            "preset_aggressive" -> {
                // Maximum bypass for heavily filtered networks
                args.addAll(listOf("--split", "1+s", "--disorder", "1+s", "--fake", "1+s", "--ttl", "4", "--mod-http=h,d"))
            }
            else -> {
                val offset = if (splitOffset > 0) splitOffset else 1
                args.addAll(listOf("--split", "${offset}+s"))
                if (disorder) {
                    args.addAll(listOf("--disorder", "1+s"))
                }
                if (fakeData) {
                    args.addAll(listOf("--fake", "1+s", "--ttl", fakeTtl.toString()))
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
