package com.navercorp.scavenger.config

import com.linecorp.armeria.common.SessionProtocol
import com.linecorp.armeria.server.ServerBuilder
import com.linecorp.armeria.server.grpc.GrpcService
import com.linecorp.armeria.server.healthcheck.HealthChecker
import com.linecorp.armeria.server.tomcat.TomcatService
import com.linecorp.armeria.spring.ArmeriaServerConfigurator
import com.navercorp.scavenger.controller.GrpcAgentController
import com.navercorp.scavenger.exception.LicenseKeyNotFoundException
import io.grpc.Status
import org.apache.catalina.connector.Connector
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ArmeriaConfiguration(val grpcAgentController: GrpcAgentController) {

    fun getConnector(applicationContext: ServletWebServerApplicationContext): Connector {
        return (applicationContext.webServer as TomcatWebServer)
            .also { it.start() }
            .tomcat.connector
    }

    @Bean
    fun tomcatConnectorHealthChecker(applicationContext: ServletWebServerApplicationContext): HealthChecker {
        return HealthChecker { getConnector(applicationContext).state.isAvailable }
    }

    @Bean
    fun tomcatService(applicationContext: ServletWebServerApplicationContext): TomcatService {
        return TomcatService.of(getConnector(applicationContext))
    }

    @Bean
    fun armeriaServiceInitializer(tomcatService: TomcatService): ArmeriaServerConfigurator {
        return ArmeriaServerConfigurator { serviceBuilder: ServerBuilder ->
            serviceBuilder.port(8080, SessionProtocol.HTTP)
                .service("prefix:/", tomcatService)
                .service(GrpcService.builder()
                    .addService(grpcAgentController)
                    .addExceptionMapping(IllegalArgumentException::class.java, Status.INVALID_ARGUMENT)
                    .addExceptionMapping(LicenseKeyNotFoundException::class.java, Status.UNAUTHENTICATED)
                    .build()
                )
        }
    }
}
