package com.navercorp.scavenger.model;

public interface Endpoints {
    interface Agent {
        String V4_INIT_CONFIG = "/javaagent/v4/initConfig";
        String V4_POLL_CONFIG = "/javaagent/v4/pollConfig";
        String V4_UPLOAD_CODEBASE = "/javaagent/v4/uploadCodeBase";
        String V4_UPLOAD_INVOCATION_DATA = "/javaagent/v4/uploadInvocationData";

        String V5_INIT_CONFIG = "/javaagent/v5/initConfig";

        String PARAM_LICENSE_KEY = "licenseKey";
        String PARAM_PUBLICATION_FILE = "publicationFile";
    }
}
