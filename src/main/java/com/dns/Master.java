package com.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Master {
    // TODO: Create resolving IP functionality
    // TODO: Add MasterFile reading and mapping
    public static byte[] resolveIP (String hostname) throws UnknownHostException {
        try {
            byte[] address = InetAddress.getByName(hostname).getAddress();
            return address;
        }
        catch (UnknownHostException e) {
            byte[] address = InetAddress.getLoopbackAddress().getAddress();
            return address;
        }
    }
}
