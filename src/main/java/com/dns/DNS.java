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

            Query.setDatagramID(ResolverHelper.getDatagramId(request));

            Query.setQueryFlags(ResolverHelper.processFlags(request));
            Logger.outputFlags();

            Query.setQueryCounts(ResolverHelper.processCounts(request));

            Query.setQueryQuestion(ResolverHelper.processQuestion(request));
            Logger.outputQuestion();

            byte[] resolvedIP = Master.ResolveIP(Query.getQueryQuestion().get(0));

            ByteArrayOutputStream response = ResolverHelper.createResponse(resolvedIP);
            UDPSocket.sendResponse(response.toByteArray());

            Logger.Info("---------------");
        }
    }
}
