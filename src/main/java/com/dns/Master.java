package com.dns;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class Master {
    private static final Map<String, String> IPs = ReadMaster();

    public static Map<String, String> ReadMaster() {
        Map<String, String> map = new HashMap<>();
        try {
            File file = new File("src/main/java/com/dns/MasterFile.txt");
            FileReader reader = new FileReader(file);
            BufferedReader buffer = new BufferedReader(reader);
            String line;
            while ((line = buffer.readLine()) != null) {
                String[] register = line.split(" ");
                map.put(register[0], register[1]);
            }
            return map;
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public static byte[] ResolveIP(String hostname) {
        try {
            String resolve = IPs.get(hostname);
            if (resolve != null) {
                return resolve.getBytes();
            } else {
                InetAddress address = InetAddress.getByName(hostname);
                IPs.put(address.getHostName(), address.getHostAddress());
                WriteToMaster(address.getHostName(), address.getHostAddress());
                return address.getAddress();
            }
        } catch (UnknownHostException e) {
            String errorIP = "0.0.0.0";
            return errorIP.getBytes();
        }
    }

    public static void WriteToMaster(String domain, String ip) {
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            File file = new File("src/main/java/com/dns/MasterFile.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file, true);
            bw = new BufferedWriter(fw);
            String line = domain + " " + ip + "\n";
            bw.write(line);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
