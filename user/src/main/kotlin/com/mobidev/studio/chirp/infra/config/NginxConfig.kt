package com.mobidev.studio.chirp.infra.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

// SpringBoot takes this class and use as config data for Nginx
@Configuration
@ConfigurationProperties(prefix = "nginx")
data class NginxConfig(
    var trustedIps: List<String> = emptyList(),
    var requireProxy: Boolean = true,
)
