package com.navercorp.scavenger.service

import com.navercorp.scavenger.entity.MethodInvocationEntity
import com.navercorp.scavenger.entity.SnapshotEntity
import com.navercorp.scavenger.entity.SnapshotNodeEntity
import com.navercorp.scavenger.repository.SnapshotNodeDao
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.AntPathMatcher

@Service
class SnapshotNodeService(
    private val snapshotNodeDao: SnapshotNodeDao
) {
    fun readSnapshotNode(customerId: Long, snapshotId: Long, parent: String): List<SnapshotNodeEntity> {
        return snapshotNodeDao.findAllByCustomerIdAndSnapshotIdAndParent(customerId, snapshotId, parent)
    }

    @Transactional
    fun createAndSaveSnapshotNodes(snapshotEntity: SnapshotEntity, methodInvocationEntities: List<MethodInvocationEntity>) {
        val filteredMethodInvocations = filterByPackagesAntMatch(methodInvocationEntities, snapshotEntity.packages.trim())

        val root = Node("", Node.Type.ROOT)
        filteredMethodInvocations.forEach {
            updateCountGraph(
                current = root,
                elements = splitSignatureWithType(it),
                isUsed = it.invokedAtMillis > 0 && it.invokedAtMillis >= snapshotEntity.filterInvokedAtMillis,
                invokedAtMillis = it.invokedAtMillis
            )
        }

        serializeGraphAddToNodes(
            currentNode = root,
            parentNode = root,
            snapshotId = checkNotNull(snapshotEntity.id) { "node 저장은 Snaptshot 저장 이후에 일어나므로 id는 null일 수 없음" },
            customerId = checkNotNull(snapshotEntity.customerId) { "customer ID는 null일 수 없음" }
        ).chunked(BATCH_CHUNK_SIZE).forEach {
            snapshotNodeDao.saveAllSnapshotNodes(it)
        }
    }

    fun deleteSnapshotNode(customerId: Long, snapshotId: Long) {
        snapshotNodeDao.deleteAllByCustomerIdAndSnapshotId(customerId, snapshotId)
    }

    fun getSnapshotNodesBySignatureContaining(
        customerId: Long,
        snapshotId: Long,
        signature: String,
        snapshotNodeId: Long? = null
    ): List<SnapshotNodeEntity> {
        return snapshotNodeDao.findAllBySignatureContaining(customerId, snapshotId, signature, snapshotNodeId)
    }

    private fun filterByPackagesAntMatch(
        methodInvocationEntities: List<MethodInvocationEntity>,
        packages: String
    ): List<MethodInvocationEntity> {
        if (packages.isEmpty()) {
            return methodInvocationEntities
        }
        val antPathMatcher = AntPathMatcher(".")
        val patterns = packages.replace(" ", "").split(",")
        return methodInvocationEntities
            .filter { methodInvocationEntity: MethodInvocationEntity ->
                patterns.any { pattern: String ->
                    antPathMatcher.match(
                        pattern,
                        methodInvocationEntity.signature.replace("$", ".")
                    )
                }
            }
    }

    private fun updateCountGraph(
        current: Node,
        elements: List<SignatureWithType>,
        isUsed: Boolean,
        invokedAtMillis: Long
    ) {
        var node = current
        elements.forEach {
            val signature = it.signature
            updateCount(node, isUsed, invokedAtMillis)
            if (signature !in node.signatureChildMap) {
                val delimiter = if (signature.contains("$")) "" else "."
                val nextElementName = if (node.signature.isBlank()) signature else "${node.signature}$delimiter$signature"
                node.signatureChildMap[signature] = Node(
                    signature = nextElementName,
                    type = it.type
                )
            }
            node = node.signatureChildMap.getValue(signature)
        }
        updateCount(node, isUsed, invokedAtMillis)
    }

    private fun updateCount(
        node: Node,
        isUsed: Boolean,
        invokedAtMillis: Long
    ) {
        if (isUsed) {
            node.usedCount += 1
            node.lastInvokedAtMillis = maxOf(node.lastInvokedAtMillis ?: Long.MIN_VALUE, invokedAtMillis)
        } else {
            node.unusedCount += 1
        }
    }

    /*
    * "a.b.c.d(e, f)" to ["a", "b", "c", "d(e, f)"]
    * "a.b.c.D(e, f)" to ["a", "b", "c", "D", "D(e, f)"]
    */
    private fun splitSignatureWithType(methodInvocationEntity: MethodInvocationEntity): List<SignatureWithType> {
        val (nameOnlySignature, arguments) = methodInvocationEntity.signature.split("(", limit = 2)

        val elements = nameOnlySignature.split("(?=\\b[.$])".toRegex())
            .filterNot { it == "" }
            .map { SignatureWithType(it.replace(".", "")) }
            .toMutableList()

        if (isConstructor(methodInvocationEntity)) {
            val constructorSignature = elements.last().signature
            elements.add(SignatureWithType(constructorSignature.replace("$", "")))
        }

        val lastElement = elements.removeLast()
        elements.add(SignatureWithType("${lastElement.signature}($arguments" + if (arguments.last() != ')') ")" else ""))

        elements.forEachIndexed { index, signatureWithType ->
            val signature = signatureWithType.signature
            signatureWithType.type = if (signature.contains("(")) {
                Node.Type.METHOD
            } else if (signature.contains("$") || elements[index + 1].signature.contains("[($]".toRegex())) {
                Node.Type.CLASS
            } else {
                Node.Type.PACKAGE
            }
        }

        return elements
    }

    private fun isConstructor(methodInvocationEntity: MethodInvocationEntity): Boolean {
        return methodInvocationEntity.methodName == "<init>"
    }

    private fun serializeGraphAddToNodes(
        currentNode: Node,
        parentNode: Node,
        snapshotId: Long,
        customerId: Long
    ): List<SnapshotNodeEntity> {
        if (currentNode.type != Node.Type.ROOT && currentNode.signatureChildMap.size == 1) {
            val child = currentNode.signatureChildMap.values.first()
            if (child.type == Node.Type.PACKAGE) {
                return serializeGraphAddToNodes(child, parentNode, snapshotId, customerId)
            }
        }

        val descendants = currentNode.signatureChildMap.values.flatMap {
            serializeGraphAddToNodes(it, currentNode, snapshotId, customerId)
        }

        if (currentNode.type == Node.Type.ROOT) {
            return descendants
        }

        return descendants + SnapshotNodeEntity(
            snapshotId = snapshotId,
            signature = currentNode.signature,
            lastInvokedAtMillis = currentNode.lastInvokedAtMillis,
            usedCount = currentNode.usedCount,
            unusedCount = currentNode.unusedCount,
            parent = parentNode.signature,
            type = currentNode.type.toString(),
            customerId = customerId
        )
    }

    data class SignatureWithType(
        val signature: String,
    ) {
        lateinit var type: Node.Type
    }

    data class Node(
        val signature: String,
        val type: Type,
        var usedCount: Int = 0,
        var unusedCount: Int = 0,
        var lastInvokedAtMillis: Long? = null,
        val signatureChildMap: MutableMap<String, Node> = hashMapOf()
    ) {
        enum class Type {
            ROOT, CLASS, METHOD, PACKAGE
        }
    }

    companion object {
        private const val BATCH_CHUNK_SIZE = 1000
    }
}
