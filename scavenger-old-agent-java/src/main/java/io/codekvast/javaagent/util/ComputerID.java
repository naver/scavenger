/*
 * Copyright (c) 2015-2021 Hallin Information Technology AB
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.codekvast.javaagent.util;

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

/**
 * This is a computed value of the computer identity. It uses various stuff for computing the value,
 * that is unlikely to change between reboots.
 *
 * @author olle.hallin@crisp.se
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ComputerID {
    String value;

    static ComputerID compute() {
        String value = computeComputerIdentity();
        return new ComputerID(value);
    }

    private static String computeComputerIdentity() {
        Set<String> items = new TreeSet<>();
        addMacAddresses(items);
        addHostName(items);
        return Integer.toHexString(items.hashCode()).toLowerCase();
    }

    private static void addHostName(Set<String> items) {
        try {
            items.add(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException ignore) {
            // ignore
        }
    }

    private static void addMacAddresses(Set<String> items) {
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
