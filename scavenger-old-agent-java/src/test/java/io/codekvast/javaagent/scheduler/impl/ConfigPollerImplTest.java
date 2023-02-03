package io.codekvast.javaagent.scheduler.impl;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import io.codekvast.javaagent.model.v4.GetConfigRequest4;
import io.codekvast.javaagent.model.v4.GetConfigResponse4;

/**
 * @author olle.hallin@crisp.se
 */
public class ConfigPollerImplTest {
    private final Gson gson = new Gson();

    @Test
    public void should_serialize_deserialize_getConfigRequest4() {
        GetConfigRequest4 request4 = GetConfigRequest4.sample();
        String json = gson.toJson(request4);

        assertThat(json, containsString("\"licenseKey\":\"licenseKey\""));

        GetConfigRequest4 fromJson = gson.fromJson(json, GetConfigRequest4.class);
        assertThat(fromJson, is(request4));
    }

    @Test
    public void should_serialize_deserialize_GetConfigResponse4() {
        GetConfigResponse4 response4 = GetConfigResponse4.sample();
        String json = gson.toJson(response4);

        assertThat(json, containsString("\"customerId\":1"));

        GetConfigResponse4 fromJson = gson.fromJson(json, GetConfigResponse4.class);
        assertThat(fromJson, is(response4));
    }
}
