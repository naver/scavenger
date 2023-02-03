package io.codekvast.javaagent.model.model.v4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import io.codekvast.javaagent.model.v4.CodeBasePublication4;
import io.codekvast.javaagent.model.v4.InvocationDataPublication4;

/**
 * A test to prove that we can deserialize publications produced by a v2 agent. The test resources
 * are produced by ./gradlew :sample:sample-gradle-application:run
 *
 * @author olle.hallin@crisp.se
 */
class PublicationV4DeserializationTest {
    private static final String CODEBASE_RESOURCE = "/sample-publications/codebase-v4.ser";
    private static final String INVOCATIONS_RESOURCE =
        "/sample-publications/invocations-v4.ser";

    @Test
    void should_deserialize_codebaseV4_file() throws IOException, ClassNotFoundException {
        ObjectInputStream ois =
            new ObjectInputStream(
                new BufferedInputStream(Objects.requireNonNull(getClass().getResourceAsStream(CODEBASE_RESOURCE))));
        CodeBasePublication4 publication = (CodeBasePublication4)ois.readObject();
        assertThat(publication, isA(CodeBasePublication4.class));
    }

    @Test
    void should_deserialize_invocationsV4_file() throws IOException, ClassNotFoundException {
        ObjectInputStream ois =
            new ObjectInputStream(
                new BufferedInputStream(Objects.requireNonNull(getClass().getResourceAsStream(INVOCATIONS_RESOURCE))));
        InvocationDataPublication4 publication = (InvocationDataPublication4)ois.readObject();
        assertThat(publication, isA(InvocationDataPublication4.class));
    }
}
