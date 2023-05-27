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
        /*
                                        1  1  1  1  1  1
          0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                      ID                       |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
         */
        return datagram.readShort();
    }

    public static List<Integer> processFlags(DataInputStream datagram) throws IOException {
        /*
                                            1  1  1  1  1  1
              0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |QR|   Opcode  |AA|TC|RD|RA|   Z    |   RCODE   |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
         */
        final byte flags = datagram.readByte();
        List<Integer> requestFlags = new ArrayList<>() {
            {
                add((flags & 0x10000000) >>> 7);    // Query or Response
                add((flags & 0x01111000) >>> 3);    // Query kind
                add((flags & 0x00000100) >>> 2);    // Authoritative Answer
                add((flags & 0x00000010) >>> 1);    // Truncation
                add((flags & 0x00000001));          // Recursion Desired
            }
        };

        /*
            ---
            Reading second half of Short. Not implemented.
            ---
            Final array of flags should follow this order
            [QR, OPCODE, AA, TC, RD]
            Each field is set as Integer for easier management further on
            ---
         */
        datagram.readByte();
        return requestFlags;
    }

    public static List<Short> processCounts(DataInputStream datagram) throws IOException {
        /*
                                            1  1  1  1  1  1
              0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                    QDCOUNT                    |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                    ANCOUNT                    |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                    NSCOUNT                    |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                    ARCOUNT                    |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
         */

        /*
            Final array of query counts should follow this order
            [QDCOUNT, ANCOUNT, NSCOUNT, ARCOUNT]
            This field of datagram will be copied to the response due to the basic implementation based on RFC 1035
         */
        return new ArrayList<>() {
            {
                add(datagram.readShort());
                add(datagram.readShort());
                add(datagram.readShort());
                add(datagram.readShort());
            }
        };
    }

    public static List<String> processQuestion(DataInputStream datagram) throws IOException {
        /*
                                            1  1  1  1  1  1
              0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                                               |
            /                     QNAME                     /
            /                                               /
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                     QTYPE                     |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                     QCLASS                    |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
         */

        /*
            Writing requested domain to a readable string.
            Original one is in the form of: 6domain3com
            And it is parsed to: domain.com
         */
        int recordLen;
        StringBuilder QNAME = new StringBuilder();
        while ((recordLen = datagram.readByte()) > 0) {
            if (QNAME.length() > 0) {
                QNAME.append(".");
            }
            byte[] record = new byte[recordLen];
            datagram.readFully(record);
            QNAME.append(new String(record, StandardCharsets.UTF_8));
        }

        /*
            Final array of query counts should follow this order
            [QNAME, QTYPE, QCLASS]
         */
        return new ArrayList<>() {
            {
                add(String.valueOf(QNAME));
                add(String.valueOf(datagram.readShort()));
                add(String.valueOf(datagram.readShort()));
            }
        };
    }

    public static void createResponseHeader(DataOutputStream response) throws IOException {
        String flags = "";
        flags += ("1");                                                             // Query or Response - 1: Response
        flags += ("0000");                                                          // Query kind - 0: Standard Query
        flags += ("0");                                                             // Authoritative Answer- 0: This zone is not the master of any domain
        flags += ("0");                                                             // Truncation - 0: This message should not be truncated
        flags += (String.valueOf(Query.getQueryFlags().get(4)));                    // Recursion Desired: Same as request
        flags += ("1");                                                             // Recursion Available: 1 - Complete sought response
        flags += ("000");                                                           // Z: Reserved for future use - Should be zero
        if (Query.getQueryQuestion().get(1).equals("1")) {
            flags += ("0000");                                                      // Response Code: 0 - No error condition
        } else {
            Logger.Info("\t!Query Type Not Implemented! - Answered with Type A Record");
            flags += ("0100");                                                      // Response code: 4 - Not implemented
        }

        // Parsing the flags Binary String into an array of bytes to be sent in the response
        int flagsAux = Integer.parseInt(flags, 2);
        byte[] flagsAuxByte = ByteBuffer.allocate(2).putShort((short) flagsAux).array();

        /*
                                            1  1  1  1  1  1
              0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                      ID                       |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |QR|   Opcode  |AA|TC|RD|RA|   Z    |   RCODE   |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                    QDCOUNT                    |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                    ANCOUNT                    |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                    NSCOUNT                    |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                    ARCOUNT                    |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
         */
        response.writeShort(Query.getDatagramID());
        response.write(flagsAuxByte);
        response.writeShort(Query.getQueryCounts().get(0));
        response.writeShort(0b0000000000000001);                                // Answer Count: 1 - This server should return a single answer
        response.writeShort(Query.getQueryCounts().get(2));
        response.writeShort(Query.getQueryCounts().get(3));
    }

    public static void createResponseQuestion(DataOutputStream response) throws IOException {
        /*
                                            1  1  1  1  1  1
              0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                                               |
            /                     QNAME                     /
            /                                               /
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                     QTYPE                     |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                     QCLASS                    |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
         */

        /*
            Inverse process of QNAME extraction
            Original one is in the form of: domain.com
            And it is parsed to: 6domain3com
         */
        String[] hostname = Query.getQueryQuestion().get(0).split("\\.");
        for (String s : hostname) {
            response.writeByte(s.length());
            response.write(s.getBytes());
        }

        response.writeBytes("\0");                                      // Finishing QNAME with null character
        response.writeShort(Short.parseShort("0000000000000001"));      // QTYPE: 1 - Address record
        response.writeShort(Short.parseShort("0000000000000001"));      // QCLASS: 1 - Internet
    }

    public static void createResponseAnswer(DataOutputStream response, byte[] resolvedIP) throws IOException {
        /*
                                            1  1  1  1  1  1
              0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                                               |
            /                                               /
            /                      NAME                     /
            |                                               |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                      TYPE                     |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                     CLASS                     |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                      TTL                      |
            |                                               |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                   RDLENGTH                    |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--|
            /                     RDATA                     /
            /                                               /
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
         */

        /*
            Message Compression: Name Format
                +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
                | 1  1|                OFFSET                   |
                +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            According to RFC1035: In order to reduce the size of messages, the domain system utilizes a
            compression scheme which eliminates the repetition of domain names in a
            message.  In this scheme, an entire domain name or a list of labels at
            the end of a domain name is replaced with a pointer to a prior occurrence
            of the same name.

            This is, the following offset field, that will be written in the NAME field of response
            contains the following binary chain:
                11 - Specifying that this is a pointer
                0x0C - 12 in decimal, meaning that the actual NAME of this response is 12 bytes from
                       the start of the frame, meaning, the QNAME field.
         */
        String PTR = "1100000000001100";
        int nameAux = Integer.parseInt(PTR, 2);
        byte[] NAME = ByteBuffer.allocate(2).putShort((short) nameAux).array();

        response.write(NAME);                                               // NAME: Pointer to QNAME
        response.writeShort(Short.parseShort("0000000000000001"));       // Type: 1 - A host address
        response.writeShort(Short.parseShort("0000000000000001"));       // Class: 1 - Internet
        response.writeInt(128);                                          // TTL: 128 - 2 minutes and 8 seconds (This is a symbolic time for facilitate tests)
        response.writeShort(4);                                          // RDLENGTH: 4 - 4 bytes or, in other words, 32 bits. The size of a standard IPv4 address
        response.write(resolvedIP);                                         // RDATA: The resolved IPv4 address for the request domain
    }

    public static ByteArrayOutputStream createResponse(byte[] resolvedIP) throws IOException {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        DataOutputStream response = new DataOutputStream(outBytes);

        createResponseHeader(response);
        createResponseQuestion(response);
        createResponseAnswer(response, resolvedIP);

        return outBytes;
    }
}
