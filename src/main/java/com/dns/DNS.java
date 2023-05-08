package com.dns;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

public class DNS {

    public static void main(String[] Args) throws IOException {
        Socket UDPSocket = new Socket();

        while (true) {
            Logger.Info("---------------");
            DataInputStream request = UDPSocket.receiveRequest();
            ResolverHelper.getDatagramId(request);
            List<Integer> flags = ResolverHelper.processFlags(request);
            // Logger.Info(flags.toString());
            Logger.Info("---------------");
        }
    }
}
