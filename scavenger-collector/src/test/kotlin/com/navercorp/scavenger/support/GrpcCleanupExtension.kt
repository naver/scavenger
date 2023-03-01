package com.navercorp.scavenger.support

import io.grpc.BindableService
import io.grpc.ManagedChannel
import io.grpc.Server
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class GrpcCleanupExtension : AfterEachCallback {
    private var cleanupTargets: MutableList<CleanupTarget> = mutableListOf()

    companion object {
        private val logger = LoggerFactory.getLogger(GrpcCleanupExtension::class.java)

        const val TERMINATION_TIMEOUT_MS = 250L
        const val MAX_NUM_TERMINATIONS = 10
    }

    fun addService(service: BindableService): ManagedChannel {
        val serverName: String = InProcessServerBuilder.generateName()

        cleanupTargets.add(
            ServerCleanupTarget(
                InProcessServerBuilder
                    .forName(serverName)
                    .directExecutor()
                    .addService(service)
                    .build()
                    .start()
            )
        )

        val channel = InProcessChannelBuilder.forName(serverName)
            .directExecutor()
            .build()

        cleanupTargets.add(ManagedChannelCleanupTarget(channel))

        return channel
    }

    override fun afterEach(context: ExtensionContext?) {
        cleanupTargets.forEach { cleanupTarget ->
            try {
                var count = 0
                cleanupTarget.shutdown()
                do {
                    cleanupTarget.awaitTermination(TERMINATION_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    count++
                    if (count > MAX_NUM_TERMINATIONS) {
                        logger.error("Hit max count $count trying to shut down down cleanupTarget $cleanupTarget")
                        break
                    }
                } while (!cleanupTarget.isTerminated())
            } catch (e: Exception) {
                logger.error("Problem shutting down cleanupTarget $cleanupTarget", e)
            }
        }

        if (isAllTerminated()) {
            cleanupTargets.clear()
        } else {
            logger.error("Not all cleanupTargets are terminated")
        }
    }

    private fun isAllTerminated(): Boolean = cleanupTargets.all { it.isTerminated() }
}

interface CleanupTarget {
    fun shutdown()
    fun awaitTermination(timeout: Long, timeUnit: TimeUnit): Boolean
    fun isTerminated(): Boolean
}

class ServerCleanupTarget(private val server: Server) : CleanupTarget {
    override fun shutdown() {
        server.shutdown()
    }

    override fun awaitTermination(timeout: Long, timeUnit: TimeUnit): Boolean =
        server.awaitTermination(timeout, timeUnit)

    override fun isTerminated(): Boolean = server.isTerminated
}

class ManagedChannelCleanupTarget(private val managedChannel: ManagedChannel) : CleanupTarget {
    override fun shutdown() {
        managedChannel.shutdown()
    }

    override fun awaitTermination(timeout: Long, timeUnit: TimeUnit): Boolean =
        managedChannel.awaitTermination(timeout, timeUnit)

    override fun isTerminated(): Boolean = managedChannel.isTerminated
}
