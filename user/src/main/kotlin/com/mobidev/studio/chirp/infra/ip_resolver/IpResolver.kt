package com.mobidev.studio.chirp.infra.ip_resolver

import com.mobidev.studio.chirp.infra.config.NginxConfig
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.stereotype.Component

@Component
class IpResolver(
    private val nginxConfig: NginxConfig,
) {
    companion object {
        // ranges of ip correspond to private list of ip (internal company network)
        private val PRIVATE_IP_RANGES = listOf(
            "10.0.0.0/8",
            "172.16.0.0/12",
            "192.168.0.0/16",
            "127.0.0.0/8",
            "::1/128",
            "fc00::/7",
            "fe80::/10"
        ).map { ipAddress -> IpAddressMatcher(ipAddress) }

        // list of ip to ignore
        private val INVALID_IPS = listOf(
            "unknown",
            "unavailable",
            "0.0.0.0",
            "::"
        )
    }

    private val trustedMatchers: List<IpAddressMatcher> =
        nginxConfig
            .trustedIps
            .filter { ipAddress -> ipAddress.isNotBlank() }
            .map { proxy ->
//                Classless Inter-Domain Routing -> CIDR
//                example, 10.0.0.0/24 -> range from 10.0.0.0 to 10.0.0.255 = Total Host 256
                val cidr = when {
//                  this is already a range
                    proxy.contains("/") -> proxy
//                  working with ipv6 -> map to proper range
                    proxy.contains(":") -> "$proxy/128"
//                  working with ipv4
                    else -> "$proxy/32"
                }

                IpAddressMatcher(cidr)
            }
}