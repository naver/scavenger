package com.navercorp.scavenger.javaagent.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {
    public final String HOST_NAME = getHostname();
    public final String JVM_UUID = UUID.randomUUID().toString();
    public final long JVM_STARTED_AT_MILLIS = System.currentTimeMillis();

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "-unknown-";
        }
    }
}
