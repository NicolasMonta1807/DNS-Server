package com.dns;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
        int opCode = (flags & 0x01111000) >>> 3;
        basicFlags.add(opCode);

        int AA = (flags & 0x00000100) >>> 2;
        basicFlags.add(AA);

        int TC = (flags & 0x00000010) >>> 1;
        basicFlags.add(TC);

        int RD = (flags & 0x00000001);
        basicFlags.add(RD);

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

    public static List<String> processQuestion(DataInputStream datagram) throws IOException {
        List<String> question = new ArrayList<>(3);
        int recordLen;
        String QNAME = "";
        while ((recordLen = datagram.readByte()) > 0) {
            if (recordLen != 0 && !QNAME.equals("")) {
                QNAME += ".";
            }
            byte[] record = new byte[recordLen];
            for (int i = 0; i < recordLen; i++) {
                record[i] = datagram.readByte();
            }
            QNAME += new String(record, StandardCharsets.UTF_8);
        }
        short QTYPE = datagram.readShort();
        short QCLASS = datagram.readShort();
        question.add(QNAME);
        question.add(String.format("%s", QTYPE));
        question.add(String.format("%s", QCLASS));
        return question;
    }
}
