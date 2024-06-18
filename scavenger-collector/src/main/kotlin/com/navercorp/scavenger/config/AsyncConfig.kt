package com.navercorp.scavenger.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.MDC
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfig : AsyncConfigurer {
    val logger = KotlinLogging.logger {}

    override fun getAsyncExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 30
        executor.maxPoolSize = Int.MAX_VALUE
        executor.setQueueCapacity(30)
        executor.setTaskDecorator {
            val callerThreadContext = MDC.getCopyOfContextMap() ?: mapOf()
            Runnable {
                try {
                    MDC.setContextMap(callerThreadContext)
                    it.run()
                } finally {
                    MDC.clear()
                }
            }
        }
        executor.initialize()
        return executor
    }

    override fun getAsyncUncaughtExceptionHandler() = AsyncUncaughtExceptionHandler { throwable: Throwable, _, _ ->
        logger.catching(throwable)
    }
}
