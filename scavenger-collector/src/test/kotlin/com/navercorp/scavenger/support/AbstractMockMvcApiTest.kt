package com.navercorp.scavenger.support

import io.restassured.config.EncoderConfig
import io.restassured.module.mockmvc.RestAssuredMockMvc
import io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultHandler
import org.springframework.test.web.servlet.result.PrintingResultHandler
import org.springframework.web.context.WebApplicationContext
import java.io.PrintWriter
import java.io.StringWriter

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = ["spring.datasource.url=jdbc:h2:mem:mockmvc;MODE=MySQL;DB_CLOSE_DELAY=-1;CASE_INSENSITIVE_IDENTIFIERS=TRUE"]
)
@ActiveProfiles(value = ["local", "armeria"])
class AbstractMockMvcApiTest {

    @BeforeEach
    fun setUp(@Autowired wac: WebApplicationContext) {
        RestAssuredMockMvc.webAppContextSetup(wac)
        RestAssuredMockMvc.config = RestAssuredMockMvcConfig.config()
            .encoderConfig(EncoderConfig.encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false))
        try {
            RestAssuredMockMvc.resultHandlers(LoggingResultHandler())
        } catch (ignored: Exception) {
            // ignore
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        private class LoggingResultHandler : ResultHandler {
            override fun handle(result: MvcResult) {
                logger.debug {
                    val stringWriter = StringWriter()
                    val printingResultHandler = PrintWriterPrintingResultHandler(PrintWriter(stringWriter))
                    printingResultHandler.handle(result)

                    "MvcResult details: $stringWriter"
                }
            }
        }

        private class PrintWriterPrintingResultHandler(writer: PrintWriter) : PrintingResultHandler(
            object : ResultValuePrinter {
                override fun printHeading(heading: String) {
                    writer.println()
                    writer.println(String.format("%s:", heading))
                }

                override fun printValue(label: String, value: Any?) {
                    val aValue: Any? = if (value != null && value is Array<*>) {
                        value.toList()
                    } else {
                        value
                    }

                    writer.println(String.format("%17s = %s", label, aValue))
                }
            }
        )
    }
}
