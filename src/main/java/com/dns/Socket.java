package com.dns;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Socket {
    final int PORT = 53;

    byte[] buffer = new byte[1024];

    DatagramSocket socket = new DatagramSocket(PORT);
    DatagramPacket query = new DatagramPacket(buffer, buffer.length);

    public Socket() throws SocketException {
    }

    public DataInputStream receiveRequest() throws IOException {
        System.out.println("Awaiting...");
        this.socket.receive(this.query);
        Logger.Info("Receiving request with length: " + query.getLength());
        return new DataInputStream(new ByteArrayInputStream(buffer));
    }

    public void sendResponse(byte[] response) throws IOException {
        int clientPort = this.query.getPort();
        InetAddress clientAddress = this.query.getAddress();
        DatagramPacket reply = new DatagramPacket(response, response.length, clientAddress, clientPort);
        this.socket.send(reply);
    }
}
