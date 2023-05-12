package com.dns;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

public class DNS {

    public static void main(String[] Args) throws IOException {
        Socket UDPSocket = new Socket();

        while (true) {
            Logger.Info("---------------");

            DataInputStream request = UDPSocket.receiveRequest();

            // Process request section
            int datagramId = ResolverHelper.getDatagramId(request);

            List<Integer> requestFlags = ResolverHelper.processFlags(request);
            Logger.outputFlags(requestFlags);

            List<Short> requestCounts = ResolverHelper.processCounts(request);


            List<String> requestQuestion = ResolverHelper.processQuestion(request);
            Logger.outputQuestion(requestQuestion);

            // Resolving hostname address
            byte[] resolvedIP = Master.resolveIP(requestQuestion.get(0));
            // TODO: Add error catching for non-implemented queries

            // Send response
            ByteArrayOutputStream response = ResolverHelper.createResponse(datagramId, requestFlags, requestCounts, requestQuestion, resolvedIP);
            UDPSocket.sendResponse(response.toByteArray());

            Logger.Info("---------------");
        }
    }
}
