package com.navercorp.scavenger.controller

import com.navercorp.scavenger.support.AbstractMockMvcApiTest
import io.codekvast.javaagent.model.Endpoints.Agent.V4_POLL_CONFIG
import io.codekvast.javaagent.model.v4.GetConfigRequest4
import io.codekvast.javaagent.model.v4.GetConfigResponse4
import io.restassured.module.mockmvc.kotlin.extensions.Extract
import io.restassured.module.mockmvc.kotlin.extensions.Given
import io.restassured.module.mockmvc.kotlin.extensions.Then
import io.restassured.module.mockmvc.kotlin.extensions.When
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

class AgentControllerTest : AbstractMockMvcApiTest() {
    @Test
    fun pollConfig_should_accept_valid_sample_request() {
        val data = GetConfigRequest4.builder()
            .appName("appName")
            .appVersion("appVersion")
            .agentVersion("agentVersion")
            .environment("environment")
            .computerId("computerId")
            .hostname("hostname")
            .jvmUuid("d0dfa3c2-809c-428f-b501-7419197d91c5")
            .licenseKey("4c94e0dd-ad04-4b17-9238-f46bba75c684")
            .build()

        Given {
            contentType(MediaType.APPLICATION_JSON_VALUE)
            body(data)
        } When {
            post(V4_POLL_CONFIG)
        } Then {
            statusCode(HttpStatus.OK.value())
        } Extract {
            `as`(GetConfigResponse4::class.java)
        }
    }

    @Test
    fun pollConfig_should_reject_invalid_request() {
        Given {
            contentType(MediaType.APPLICATION_JSON_VALUE)
        } When {
            post(V4_POLL_CONFIG)
        } Then {
            statusCode(HttpStatus.BAD_REQUEST.value())
        }
    }
}
