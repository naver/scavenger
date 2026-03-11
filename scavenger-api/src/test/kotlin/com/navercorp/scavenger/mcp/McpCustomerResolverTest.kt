package com.navercorp.scavenger.mcp

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class McpCustomerResolverTest {
    @Autowired
    private lateinit var sut: McpCustomerResolver

    @Test
    fun `resolveCustomerId returns id for existing customer`() {
        assertThat(sut.resolveCustomerId("demo")).isEqualTo(1L)
    }

    @Test
    fun `resolveCustomerId throws IllegalArgumentException for nonexistent customer`() {
        assertThatThrownBy { sut.resolveCustomerId("nonexistent") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("nonexistent")
    }

    @Test
    fun `getGroupId returns configured default group`() {
        assertThat(sut.getGroupId()).isEqualTo("default-group")
    }
}
