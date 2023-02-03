package io.codekvast.javaagent.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Verifier;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import io.codekvast.javaagent.util.SignatureUtils;

/**
 * Test for the visibility part of MethodAnalyzer.
 *
 * @author olle.hallin@crisp.se
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
@RunWith(Parameterized.class)
public class MethodAnalyzerTest {
    private final ByteArrayOutputStream capturedSystemErr = new ByteArrayOutputStream();

    @Parameter(0)
    public String input;

    @Parameter(1)
    public boolean selectsPublic;

    @Parameter(2)
    public boolean selectsProtected;

    @Parameter(3)
    public boolean selectsPackagePrivate;

    @Parameter(4)
    public boolean selectsPrivate;

    @Parameter(5)
    public boolean expectsSystemErr;

    @Parameter(6)
    public String expectedToString;
    @Rule
    public Verifier verifier =
        new Verifier() {
            @Override
            public void verify() {
                String output = capturedSystemErr.toString();
                if (!output.isEmpty() && !expectsSystemErr) {
                    fail("Unexpected output on System.err: " + output);
                }
                if (output.isEmpty() && expectsSystemErr) {
                    fail("Expected output on System.err");
                }
            }
        };
    private PrintStream savedSystemErr;

    @Parameters(name = "{index}: {0}")
    public static Object[][] data() {
        return new Object[][] {
            {null, true, false, false, false, false, SignatureUtils.PUBLIC},
            {"   ", true, false, false, false, false, SignatureUtils.PUBLIC},
            {" foobar ", true, false, false, false, true, SignatureUtils.PUBLIC},
            {"   ", true, false, false, false, false, SignatureUtils.PUBLIC},
            {"public", true, false, false, false, false, SignatureUtils.PUBLIC},
            {"PuBlIc", true, false, false, false, false, SignatureUtils.PUBLIC},
            {" public ", true, false, false, false, false, SignatureUtils.PUBLIC},
            {"protected", true, true, false, false, false, SignatureUtils.PROTECTED},
            {" protected ", true, true, false, false, false, SignatureUtils.PROTECTED},
            {" PROTECTED ", true, true, false, false, false, SignatureUtils.PROTECTED},
            {"package-private", true, true, true, false, false, SignatureUtils.PACKAGE_PRIVATE},
            {"!private", true, true, true, false, false, SignatureUtils.PACKAGE_PRIVATE},
            {"private", true, true, true, true, false, SignatureUtils.PRIVATE},
            {"all", true, true, true, true, false, SignatureUtils.PRIVATE},
        };
    }

    @Before
    public void beforeTest() {
        savedSystemErr = System.err;
        System.setErr(new PrintStream(capturedSystemErr));
    }

    @After
    public void afterTest() {
        System.setErr(savedSystemErr);
        System.err.print(capturedSystemErr);
    }

    @Test
    public void shouldParseVisibility() {
        MethodAnalyzer filter = new MethodAnalyzer(input);
        assertThat("Should select public", filter.selectsPublicMethods(), is(selectsPublic));
        assertThat("Should select protected", filter.selectsProtectedMethods(), is(selectsProtected));
        assertThat(
            "Should select package private",
            filter.selectsPackagePrivateMethods(),
            is(selectsPackagePrivate));
        assertThat("Should select private", filter.selectsPrivateMethods(), is(selectsPrivate));

        assertThat("Should normalize toString()", filter.toString(), is(expectedToString));
    }
}
