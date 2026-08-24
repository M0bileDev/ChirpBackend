package com.mobidev.studio.chirp.infra.ip_resolver

import com.mobidev.studio.chirp.infra.config.NginxConfig
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.stereotype.Component
import java.net.Inet4Address
import java.net.Inet6Address

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

        private const val X_REAL_IP = "X-Real-IP"
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

    private val logger = LoggerFactory.getLogger(IpResolver::class.java)

    fun getClientIp(request: HttpServletRequest): String {
        // address from the last position of the connection chain
        val remoteAddress = request.remoteAddr

        if (!isFromTrustedProxy(remoteAddress)) {
            if (nginxConfig.requireProxy) {
                logger.warn("Direct connection attempt from $remoteAddress")
                throw SecurityException("No valid client IP in proxy headers.")
            }

            //proxy not required, coming for development environment
            return remoteAddress
        }

        //trusted ip, nginx forwarded address to backend
        val clientIp = extractFromXRealIp(request = request, proxyIp = remoteAddress)

        if (clientIp == null) {
            logger.warn("No valid client IP in proxy headers.")

            if (nginxConfig.requireProxy) {
                // client may have to try call endpoint directly on backend
                throw SecurityException("No valid client IP in proxy headers.")
            }
        }

        return clientIp ?: remoteAddress
    }

    private fun extractFromXRealIp(request: HttpServletRequest, proxyIp: String): String? {
        return request.getHeader(X_REAL_IP)?.let { header ->
            validateAndNormalizeIp(header, X_REAL_IP, proxyIp)
        }
    }

    private fun validateAndNormalizeIp(ip: String, headerName: String, proxyIp: String): String? {
        val trimmedIp = ip.trim()

        if (trimmedIp.isBlank() || INVALID_IPS.contains(trimmedIp)) {
            logger.debug("Invalid IP in $headerName: $ip from proxy $proxyIp")
            return null
        }

        return try {
            val inetAddr = when {
                trimmedIp.contains(":") -> Inet6Address.getByName(trimmedIp)
                trimmedIp.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")) ->
                    Inet4Address.getByName(trimmedIp)

                else -> {
                    logger.warn("Invalid IP format in $headerName: $trimmedIp from $proxyIp")
                    return null
                }
            }

            // Not security concern, just let know that it comes from range of private addresses
            if (isPrivateIp(inetAddr.hostAddress)) {
                logger.debug("Private IP in $headerName: $trimmedIp from proxy $proxyIp")
            }

            inetAddr.hostAddress
        } catch (e: Exception) {
            logger.warn("Invalid IP format in $headerName: $trimmedIp from proxy $proxyIp", e)
            null
        }
    }

    private fun isPrivateIp(ipAddress: String): Boolean {
        return PRIVATE_IP_RANGES.any { privateAddress -> privateAddress.matches(ipAddress) }
    }

    private fun isFromTrustedProxy(ipAddress: String): Boolean {
        return trustedMatchers.any { trustedAddress -> trustedAddress.matches(ipAddress) }
    }


}