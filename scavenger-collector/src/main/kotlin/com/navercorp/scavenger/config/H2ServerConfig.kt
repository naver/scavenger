package com.navercorp.scavenger.config

import org.h2.tools.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("local")
class H2ServerConfig {
    @Bean
    fun h2TcpServer() = Server.createTcpServer().start()
}
