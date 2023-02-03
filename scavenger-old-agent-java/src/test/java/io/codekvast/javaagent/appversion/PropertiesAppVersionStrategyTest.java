package io.codekvast.javaagent.appversion;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.Test;

@SuppressWarnings("FieldCanBeLocal")
public class PropertiesAppVersionStrategyTest {
    private final Collection<File> VALID_URIS =
        Collections.singletonList(
            new File(System.getProperty("user.dir") + File.separator + "src/test/resources"));

    private final String VALID_ABSOLUTE_PATH =
        getClass().getResource("/PropertiesAppVersionStrategyTest.conf").getPath();
    private final String VALID_RELATIVE_PATH = "PropertiesAppVersionStrategyTest.conf";
    private final String INVALID_FILE = "NON-EXISTING-FILE";

    private final AppVersionStrategy strategy = new PropertiesAppVersionStrategy();

    @Test
    public void should_resolve_when_valid_absolute_path_and_valid_single_property() {
        String[] args = {"property", VALID_ABSOLUTE_PATH, "version"};

        assertThat(strategy.canHandle(args), is(true));
        assertThat(strategy.resolveAppVersion(VALID_URIS, args), is("1.2.3"));
    }

    @Test
    public void should_resolve_when_valid_relative_path_and_valid_single_property() {
        String[] args = {"properties", VALID_RELATIVE_PATH, "version"};

        assertThat(strategy.canHandle(args), is(true));
        assertThat(strategy.resolveAppVersion(VALID_URIS, args), is("1.2.3"));
    }

    @Test
    public void should_resolve_when_valid_absolute_path_and_valid_dual_properties() {
        String[] args = {"properties", VALID_ABSOLUTE_PATH, "version", "build"};

        assertThat(strategy.canHandle(args), is(true));
        assertThat(strategy.resolveAppVersion(VALID_URIS, args), is("1.2.3-4711"));
    }

    @Test
    public void should_resolve_when_valid_absolute_path_and_valid_triple_properties() {
        String[] args = {"properties", VALID_ABSOLUTE_PATH, "version", "build", "qualifier"};

        assertThat(strategy.canHandle(args), is(true));
        assertThat(strategy.resolveAppVersion(VALID_URIS, args), is("1.2.3-4711-all"));
    }

    @Test
    public void should_not_resolve_when_invalidBaseName() {
        String[] args = {"properties", INVALID_FILE, "version", "build"};

        assertThat(strategy.canHandle(args), is(true));
        assertThat(strategy.resolveAppVersion(VALID_URIS, args), is("<unknown>"));
    }

    @Test
    public void should_resolve_when_validFirstProperty_and_invalidSecondProperty() {
        String[] args = {"properties", VALID_ABSOLUTE_PATH, "version", "foobar"};

        assertThat(strategy.canHandle(args), is(true));
        assertThat(strategy.resolveAppVersion(VALID_URIS, args), is("1.2.3"));
    }

    @Test
    public void should_resolve_when_invalidFirstProperty_and_validSecondProperty() {
        String[] args = {"properties", VALID_ABSOLUTE_PATH, "foobar", "version"};

        assertThat(strategy.canHandle(args), is(true));
        assertThat(strategy.resolveAppVersion(VALID_URIS, args), is("1.2.3"));
    }
}
