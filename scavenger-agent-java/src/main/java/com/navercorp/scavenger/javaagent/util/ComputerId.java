package com.navercorp.scavenger.javaagent.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ComputerId {
    String value;

    static ComputerId compute() {
        String value = computeComputerIdentity();
        return new ComputerId(value);
    }

    private static String computeComputerIdentity() {
        Set<String> items = new TreeSet<>();

        String hostName = getHostName();
        if (hostName != null)
            items.add(hostName);
        items.addAll(getMacAddresses());

        return Integer.toHexString(items.hashCode()).toLowerCase();
    }

    private static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ignore) {
            // ignore
        }
        return null;
    }

    private static Set<String> getMacAddresses() {
        Set<String> items = new TreeSet<>();
        try {
            for (Enumeration<NetworkInterface> it = NetworkInterface.getNetworkInterfaces();
                 it.hasMoreElements(); ) {
                NetworkInterface ni = it.nextElement();
                if (shouldIncludeInterface(ni)) {
                    items.add(prettyPrintMacAddress(ni.getHardwareAddress()));
                }
            }
        } catch (SocketException ignore) {
            // Cannot enumerate network interfaces
        }
        return items;
    }

    private static boolean shouldIncludeInterface(NetworkInterface ni) throws SocketException {
        if (ni.isLoopback()) {
            return false;
        }
        if (ni.isVirtual()) {
            return false;
        }
        String name = ni.getName();
        if (name.matches("br-[0-9a-f]+$")) {
            return false;
        }
        if (name.matches("lxcbr[0-9]+$")) {
            return false;
        }
        if (name.matches("docker[0-9]+$")) {
            return false;
        }
        return !name.matches("veth[0-9a-f]+$");
    }

    private static String prettyPrintMacAddress(byte[] macAddress) {
        StringBuilder sb = new StringBuilder();
        if (macAddress != null) {
            for (byte b : macAddress) {
                sb.append(String.format("%02x", b));
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return value;
    }
}
