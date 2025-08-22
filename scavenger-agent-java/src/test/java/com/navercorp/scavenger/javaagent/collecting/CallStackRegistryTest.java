package com.navercorp.scavenger.javaagent.collecting;

import com.navercorp.scavenger.javaagent.model.Config;

import com.navercorp.scavenger.model.CallStackDataPublication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class CallStackRegistryTest {
    CallStackRegistry sut;
    Config config;
    String codeBaseFingerprint = "codeBaseFingerprint";
    String caller = "caller";
    String callee = "callee";

    @BeforeEach
    public void setUp() {
        sut = new CallStackRegistry();
        config = new Config(new Properties());
    }

    private List<CallStackDataPublication.CallStackDataEntry> getCallStack() {
        return sut.getPublication(config, codeBaseFingerprint).getEntryList();
    }

    private void registerCallStack() {
        sut.register(caller, callee);
    }

    private void registerCallStack(String caller, String callee) {
        sut.register(caller, callee);
    }

    @Nested
    @DisplayName("register method")
    class RegisterTest {

        @BeforeEach
        public void setup() {
            registerCallStack();
        }

        @Test
        @DisplayName("it contains call stack")
        void containsCallStack() {
            CallStackDataPublication.CallStackDataEntry callStack = getCallStack().getFirst();
            assertAll(
                () -> assertThat(callStack.getCallersList().size()).isEqualTo(1),
                () -> assertThat(callStack.getCallersList().getFirst()).isEqualTo(caller),
                () -> assertThat(callStack.getCallee()).isEqualTo(callee)
            );
        }

        @Test
        @DisplayName("if same methodNode is registered twice, then it contains only one hash")
        void containsHashOnce() {
            registerCallStack();
            assertThat(getCallStack()).hasSize(1);
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
                assertThat(getCallStack()).isEmpty();
            }

            @Test
            @DisplayName("it fills in commonData")
            void commonData() {
                assertThat(sut.getPublication(config, codeBaseFingerprint).getCommonData().getCodeBaseFingerprint())
                    .isEqualTo(codeBaseFingerprint);
            }
        }

        @Nested
        @DisplayName("if call stack is registered")
        class CallStackRegisteredTest {

            @BeforeEach
            public void setup() {
                registerCallStack();
            }

            @Test
            @DisplayName("it returns call stack")
            void callStack() {
                assertThat(getCallStack()).isNotEmpty();
            }

            @Test
            @DisplayName("it clears call stack")
            void clearCallStack() {
                sut.getPublication(config, codeBaseFingerprint);
                List<CallStackDataPublication.CallStackDataEntry> callStack = getCallStack();
                for (CallStackDataPublication.CallStackDataEntry callStackDataEntry : callStack) {
                    assertThat(callStackDataEntry.getCallersList()).isEmpty();
                }
            }
        }

        @Nested
        @DisplayName("if getPublication is invoked multiple times")
        class MultipleGetPublicationTest {

            @BeforeEach
            public void getPublicationMultipleTimes() {
                registerCallStack();
                getCallStack();
                registerCallStack("new-caller", "new-callee");
            }

            @Test
            @DisplayName("it only returns callStack after last getPublication")
            void alternating() {
                CallStackDataPublication.CallStackDataEntry callStack = getCallStack().getFirst();
                assertAll(
                    () -> assertThat(callStack.getCallersList().size()).isEqualTo(1),
                    () -> assertThat(callStack.getCallersList().getFirst()).isEqualTo("new-caller"),
                    () -> assertThat(callStack.getCallee()).isEqualTo("new-callee")
                );
            }
        }
    }
}
