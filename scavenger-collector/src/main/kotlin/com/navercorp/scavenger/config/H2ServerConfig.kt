package com.navercorp.scavenger.config

import org.h2.tools.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class H2ServerConfig {
    @Bean
    @Profile("local")
    fun h2TcpServer(): Server {
        return Server.createTcpServer().start()
    }

    @Bean
    @Profile("h2")
    fun h2FileServer(): Server {
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9091").start()
    }
}
