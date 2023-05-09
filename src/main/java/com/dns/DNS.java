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
            int datagramId = ResolverHelper.getDatagramId(request);
            List<Integer> flags = ResolverHelper.processFlags(request);
            Logger.outputFlags(flags);
            List<String> question = ResolverHelper.processQuestion(request);
            Logger.outputQuestion(question);
            Logger.Info("---------------");
        }
    }
}
