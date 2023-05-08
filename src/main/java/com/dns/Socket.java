package com.dns;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Socket {
    final int PORT = 53;

    byte[] buffer = new byte[1024];

    DatagramSocket socket = new DatagramSocket(PORT);
    DatagramPacket trace = new DatagramPacket(buffer, buffer.length);

    public Socket() throws SocketException {
    }

    public DataInputStream receiveRequest() throws IOException {
        System.out.println("Awaiting...");
        this.socket.receive(this.trace);
        Logger.Info("Se ha recibido una solicitud de " + trace.getLength());
        return new DataInputStream(new ByteArrayInputStream(buffer));
    }
}
