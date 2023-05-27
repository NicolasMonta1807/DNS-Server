package com.dns;

import javax.xml.crypto.Data;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
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
        List<Integer> requestFlags = new ArrayList<>();
        int QR = (flags & 0x10000000) >>> 7;
        requestFlags.add(QR);
        int opCode = (flags & 0x01111000) >>> 3;
        requestFlags.add(opCode);

        int AA = (flags & 0x00000100) >>> 2;
        requestFlags.add(AA);

        int TC = (flags & 0x00000010) >>> 1;
        requestFlags.add(TC);

        int RD = (flags & 0x00000001);
        requestFlags.add(RD);

        flags = datagram.readByte();
        int RA = (flags & 0x10000000) >>> 7;
        int Z = (flags & 0x01110000) >>> 4;
        int RCODE = (flags & 0x00001111);


        // [QR, OPCODE, AA, TC, RD]
        return requestFlags;
    }

    public static List<Short> processCounts(DataInputStream datagram) throws IOException {
        List<Short> requestCounts = new ArrayList<>();
        short QDCOUNT = datagram.readShort();
        requestCounts.add(QDCOUNT);
        short ANCOUNT = datagram.readShort();
        requestCounts.add(ANCOUNT);
        short NSCOUNT = datagram.readShort();
        requestCounts.add(NSCOUNT);
        short ARCOUNT = datagram.readShort();
        requestCounts.add(ARCOUNT);
        // [QDCOUNT, ANCOUNT, NSCOUNT, ARCOUNT]
        return requestCounts;
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

    public static void createResponseHeader(DataOutputStream response) throws IOException {
        String flags = "";
        flags += ("1"); //QR set to 1 due to being a response
        flags += ("0000"); //OPCODE set to 0 due to being a standard query
        flags += ("0"); // AA set to 0 due to not being an answer in this zone
        flags += ("0"); // TC set to 0 since this message should not be truncated
        flags += (String.valueOf(Query.getQueryFlags().get(4))); // RD set to the same option sent in request
        flags += ("0"); // RA set to 0 since recursion is not implemented in this server
        flags += ("000"); // Z set to 0 since it is reserved for future use
        if(Query.getQueryQuestion().get(1).equals("1")) {
            flags += ("0000"); // Response Code set to 0 for no error condition
        } else {
            Logger.Info("\t!Query Type Not Implemented! - Answered with Type A Record");
            flags += ("0100"); // Response Code set to 0 for no error condition
        }

        // Parsing the flags String into an array of bytes to be sent in the response
        int flagsAux = Integer.parseInt(flags, 2);
        byte[] flagsAuxByte = ByteBuffer.allocate(2).putShort((short) flagsAux).array();

        // Setting header counts according to request
        short ANCOUNT = Short.parseShort("0000000000000001"); // Setting answer count to 1 since server is sending only one response



        response.writeShort(Query.getDatagramID());
        response.write(flagsAuxByte);
        response.writeShort(Query.getQueryCounts().get(0));
        response.writeShort(ANCOUNT);
        response.writeShort(Query.getQueryCounts().get(2));
        response.writeShort(Query.getQueryCounts().get(3));
    }

    public static void createResponseQuestion(DataOutputStream response) throws IOException {
        // Writing QNAME to response datagram
        String[] hostname = Query.getQueryQuestion().get(0).split("\\.");
        for (String s : hostname) {
            response.writeByte(s.length());
            response.write(s.getBytes());
        }
        response.writeBytes("\0"); // Finishing QNAME with null character
        response.writeShort(Short.parseShort("0000000000000001")); // Setting QTYPE to 1 referring to a host address
        response.writeShort(Short.parseShort("0000000000000001")); // Setting QCLASS to 1 referring to Internet
    }

    public static void createResponseAnswer(DataOutputStream response, byte[] resolvedIP) throws IOException {
        String PTR = "1100000000001100";
        int answerH = Integer.parseInt(PTR, 2);
        ByteBuffer bAnswerH = ByteBuffer.allocate(2).putShort((short) answerH);
        byte[] finalAnswer = bAnswerH.array();

        response.write(finalAnswer); // Writing response hostname according to request
        response.writeShort(Short.parseShort("0000000000000001")); // Setting Type to 1 : A Host address
        response.writeShort(Short.parseShort("0000000000000001")); // Setting Class to 1 : The internet
        response.writeInt(128); // Setting TTL to 128 seconds
        response.writeShort(4); // Setting RDLENGTH to 4. Specifying the 4 octets of a standard IP
        response.write(resolvedIP);
    }

    public static ByteArrayOutputStream createResponse(byte[] resolvedIP) throws IOException {

        // Writing header information to datagram
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        DataOutputStream response = new DataOutputStream(outBytes);

        createResponseHeader(response);

        createResponseQuestion(response);

        createResponseAnswer(response, resolvedIP);

        return outBytes;
    }
}
