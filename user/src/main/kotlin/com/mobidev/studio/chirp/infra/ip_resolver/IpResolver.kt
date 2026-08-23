package com.mobidev.studio.chirp.infra.ip_resolver

import com.mobidev.studio.chirp.infra.config.NginxConfig
import org.springframework.stereotype.Component

@Component
class IpResolver(
    private val nginxConfig: NginxConfig,
) {
}