package ru.ifmo.ctddev.peresadin.udp;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by pva701 on 4/27/15.
 */
public class HelloUDPServer implements HelloServer {
    //public static int BUFFER_SIZE = 1500;

    private List<DatagramSocket> servers = new ArrayList<>();
    private List<Thread> loopThreads = new ArrayList<>();

    public static final Charset DEF_CHARSET = Charset.forName("UTF-8");

    @Override
    public void start(int port, int threads) {
        try {
            final DatagramSocket server = new DatagramSocket(port);
            try {
                final int receiveBuffSize = server.getReceiveBufferSize();
                servers.add(server);
                for (int i = 0; i < threads; ++i) {
                    Thread curThread = new Thread(()->{
                        byte[] buf = new byte[receiveBuffSize];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        while (!Thread.interrupted()) {
                            try {
                                server.receive(packet);
                                String msg = "Hello, " + new String(packet.getData(), 0, packet.getLength());
                                InetAddress ip = packet.getAddress();
                                int portPacket = packet.getPort();
                                sendResponse(server, msg, ip, portPacket);
                            } catch (IOException e) {
                                //System.out.println("ex receive");
                                //e.printStackTrace();
                            }
                        }
                    });
                    curThread.start();
                    loopThreads.add(curThread);
                }
            } catch (SocketException e) {
                server.close();
            }
        } catch (SocketException e) {
            //System.out.println("cant create server");
        }
    }

    @Override
    public void close() {
        for (DatagramSocket s : servers)
            s.close();
        for (Thread t : loopThreads)
            t.interrupt();
        servers.clear();
        loopThreads.clear();
    }

    public HelloUDPServer() {
    }

    private void sendResponse(DatagramSocket server, String msg, InetAddress ip, int port) {
        byte[] sendBytes = msg.getBytes(DEF_CHARSET);
        try {
            server.send(new DatagramPacket(sendBytes, 0, sendBytes.length, ip, port));
        } catch (IOException e) {
            //System.out.println("Server can't send response to client!");
            //e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("IN MAIN");
        int port = Integer.parseInt(args[0]);
        int threads = Integer.parseInt(args[1]);
        new HelloUDPServer().start(port, threads);
    }
}
