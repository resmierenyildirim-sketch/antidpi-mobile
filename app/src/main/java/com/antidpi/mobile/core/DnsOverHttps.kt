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

object DnsOverHttpsResolver {

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
            name = "AdGuard (Reklam Engelleyici)",
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
