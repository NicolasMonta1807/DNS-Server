package com.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Master {

    public static byte[] resolveIP (String hostname) throws UnknownHostException {
        // TODO: Create resolving IP functionality
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
