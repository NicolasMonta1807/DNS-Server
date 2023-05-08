package com.dns;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ResolverHelper {
    public static int getDatagramId(DataInputStream datagram) throws IOException {
        int id = datagram.readShort();
        Logger.Info("ID: " + id);
        return id;
    }

    public static List<Integer> processFlags(DataInputStream datagram) throws IOException {
        byte flags = datagram.readByte();
        List<Integer> basicFlags = new ArrayList<>();
        int QR = (flags & 0x10000000) >>> 7;
        basicFlags.add(QR);
        if (QR == 0) {
            Logger.Info("Type: Query");
        } else {
            Logger.Info("Type: Response");
        }
        int opCode = (flags & 0x01111000) >>> 3;
        basicFlags.add(opCode);
        if (opCode == 0) {
            Logger.Info("Option: Standard");
        } else {
            Logger.Info("Option: Other");
        }
        int AA = (flags & 0x00000100) >>> 2;
        basicFlags.add(AA);
        if (AA == 1) {
            Logger.Info("Authoritative Answer");
        }
        int TC = (flags & 0x00000010) >>> 1;
        basicFlags.add(TC);
        if (TC == 1) {
            Logger.Info("This message was truncated");
        }
        int RD = (flags & 0x00000001);
        basicFlags.add(RD);
        if (RD == 1) {
            Logger.Info("Recursion desired");
        }
        flags = datagram.readByte();
        int RA = (flags & 0x10000000) >>> 7;
        int Z = (flags & 0x01110000) >>> 4;
        int RCODE = (flags & 0x00001111);
        short QDCOUNT = datagram.readShort();
        short ANCOUNT = datagram.readShort();
        short NSCOUNT = datagram.readShort();
        short ARCOUNT = datagram.readShort();
        return basicFlags;
    }
}
