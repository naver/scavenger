package com.navercorp.scavenger.javaagent.collecting;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.navercorp.scavenger.javaagent.model.Config;
import com.navercorp.scavenger.model.InvocationDataPublication;

@Nested
@DisplayName("InvocationRegistry class")
public class InvocationRegistryTest {
    InvocationRegistry sut;

    Config config;
    String codeBaseFingerprint = "codeBaseFingerprint";

    @BeforeEach
    public void setUp() {
        sut = new InvocationRegistry();
        config = new Config(new Properties());
    }

    private Collection<InvocationDataPublication.InvocationDataEntry> getInvocations() {
        return sut.getPublication(config, codeBaseFingerprint).getEntryList();
    }

    @Nested
    @DisplayName("register method")
    class RegisterMethodTest {

        @Nested
        @DisplayName("if hash is registered")
        class RegisterTest {
            String hash = "hash";

            @BeforeEach
            public void registerHash() {
                sut.register(hash);
            }

            @Test
            @DisplayName("it contains hash")
            void containsHash() {
                assertThat(getInvocations())
                    .extracting(InvocationDataPublication.InvocationDataEntry::getHash)
                    .containsOnly(hash);
            }
        }

        @Nested
        @DisplayName("if hash is registered twice")
        class RegisterTwiceTest {
            String hash = "hash";

            @BeforeEach
            public void registerHashTwice() {
                sut.register(hash);
                sut.register(hash);
            }

            @Test
            @DisplayName("it contains only one hash")
            void containsHashOnce() {
                assertThat(getInvocations())
                    .extracting(InvocationDataPublication.InvocationDataEntry::getHash)
                    .containsOnlyOnce(hash);
            }
        }
    }

    @Nested
    @DisplayName("getPublication method")
    class GetPublicationMethodTest {

        @Nested
        @DisplayName("if nothing is registered")
        class NothingRegisteredTest {

            @Test
            @DisplayName("it returns empty collection")
            void empty() {
                assertThat(getInvocations()).isEmpty();
            }

            @Test
            @DisplayName("it fills in commonData")
            void commonData() {
                assertThat(sut.getPublication(config, codeBaseFingerprint).getCommonData().getCodeBaseFingerprint())
                    .isEqualTo(codeBaseFingerprint);
            }
        }

        @Nested
        @DisplayName("if hash is registered")
        class HashRegisteredTest {
            String hash = "hash";

            @BeforeEach
            public void registerHash() {
                sut.register(hash);
            }

            @Test
            @DisplayName("it returns collection containing hash")
            void containsHash() {
                assertThat(getInvocations())
                    .extracting(InvocationDataPublication.InvocationDataEntry::getHash)
                    .containsOnly(hash);
            }

            @Test
            @DisplayName("it clears registry")
            void clear() {
                getInvocations();
                assertThat(getInvocations()).isEmpty();
            }

            @Test
            @DisplayName("it fills in commonData")
            void commonData() {
                assertThat(sut.getPublication(config, codeBaseFingerprint).getCommonData().getCodeBaseFingerprint())
                    .isEqualTo(codeBaseFingerprint);
            }
        }

        @Nested
        @DisplayName("if getPublication is invoked multiple times")
        class MultipleGetPublicationTest {
            String hash = "hash";

            @BeforeEach
            public void getPublicationMultipleTimes() {
                sut.register(hash + "-random");
                getInvocations();
                sut.register(hash);
            }

            @Test
            @DisplayName("it only returns invocations after last getPublication")
            void alternating() {
                assertThat(getInvocations())
                    .extracting(InvocationDataPublication.InvocationDataEntry::getHash)
                    .containsOnly(hash);
            }
        }
    }
}
