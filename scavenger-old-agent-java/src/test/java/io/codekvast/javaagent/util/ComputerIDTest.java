package io.codekvast.javaagent.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.Test;

/**
 * @author olle.hallin@crisp.se
 */
public class ComputerIDTest {
    @Test
    public void should_ignore_interfaces_without_mac_address() {
        // given
        ComputerID id = ComputerID.compute();

        // when

        // then
        assertThat(id.toString().matches("^[0-9a-h]{4,20}$"), is(true));
    }
}
