package com.antidpi.mobile.core

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import java.net.InetAddress
import java.util.concurrent.TimeUnit

data class DohServer(
    val name: String,
    val url: String,
    val bootstrapIps: List<String>
)

data class AdBlockDnsServer(
    val name: String,
    val description: String,
    val primaryIp: String,
    val secondaryIp: String,
    val dohUrl: String
)

object DnsOverHttpsResolver {

    val ADBLOCK_PROVIDERS = listOf(
        AdBlockDnsServer(
            name = "AdGuard Reklam Engelleyici (Önerilen)",
            description = "Mobil oyunlardaki video/afişler ile web sitelerindeki pop-up ve banner reklamlarını engeller.",
            primaryIp = "94.140.14.14",
            secondaryIp = "94.140.15.15",
            dohUrl = "https://dns.adguard-dns.com/dns-query"
        ),
        AdBlockDnsServer(
            name = "AdGuard Aile Koruması",
            description = "Oyun ve web reklamları + yetişkin içerik ve zararlı siteleri filtreler.",
            primaryIp = "94.140.14.15",
            secondaryIp = "94.140.15.16",
            dohUrl = "https://dns.adguard-dns.com/dns-query"
        ),
        AdBlockDnsServer(
            name = "Mullvad AdBlock DNS",
            description = "Web ve oyunlardaki reklam/izleyicileri engelleyen sıfır kayıtlı gizlilik filtresi.",
            primaryIp = "194.242.2.3",
            secondaryIp = "194.242.2.4",
            dohUrl = "https://adblock.doh.mullvad.net/dns-query"
        ),
        AdBlockDnsServer(
            name = "Control D AdBlock",
            description = "Oyun ve sitelerdeki reklamları filtreleyen ultra düşük pingli Anycast ağı.",
            primaryIp = "76.76.2.2",
            secondaryIp = "76.76.10.2",
            dohUrl = "https://freedns.controld.com/p2"
        )
    )

    val PROVIDERS = listOf(
        DohServer(
            name = "Cloudflare",
            url = "https://1.1.1.1/dns-query",
            bootstrapIps = listOf("1.1.1.1", "1.0.0.1")
        ),
        DohServer(
            name = "Google",
            url = "https://dns.google/dns-query",
            bootstrapIps = listOf("8.8.8.8", "8.8.4.4")
        ),
        DohServer(
            name = "Quad9 (Güvenli)",
            url = "https://dns.quad9.net/dns-query",
            bootstrapIps = listOf("9.9.9.9", "149.112.112.112")
        ),
        DohServer(
            name = "AdGuard",
            url = "https://dns.adguard-dns.com/dns-query",
            bootstrapIps = listOf("94.140.14.14", "94.140.15.15")
        )
    )

    private var currentDns: DnsOverHttps? = null
    private var activeProviderName: String = ""

    fun getResolver(providerName: String, customUrl: String? = null): DnsOverHttps {
        if (currentDns != null && activeProviderName == providerName) {
            return currentDns!!
        }

        val provider = PROVIDERS.firstOrNull { it.name == providerName }
        val targetUrl = customUrl ?: provider?.url ?: "https://1.1.1.1/dns-query"
        val bootstrapIps = provider?.bootstrapIps?.map { InetAddress.getByName(it) }
            ?: listOf(InetAddress.getByName("1.1.1.1"), InetAddress.getByName("8.8.8.8"))

        val appClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()

        val dns = DnsOverHttps.Builder()
            .client(appClient)
            .url(targetUrl.toHttpUrl())
            .bootstrapDnsHosts(bootstrapIps)
            .includeIPv6(false)
            .build()

        currentDns = dns
        activeProviderName = providerName
        return dns
    }

    fun lookup(hostname: String, providerName: String = "Cloudflare"): List<InetAddress> {
        return try {
            getResolver(providerName).lookup(hostname)
        } catch (e: Exception) {
            InetAddress.getAllByName(hostname).toList()
        }
    }
}
